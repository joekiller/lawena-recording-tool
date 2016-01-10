package com.github.lawena.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lawena.domain.Branch;
import com.github.lawena.domain.Build;
import com.github.lawena.domain.UpdateResult;
import com.github.lawena.util.LwrtUtils;
import com.threerings.getdown.data.Resource;
import com.threerings.getdown.net.Downloader;
import com.threerings.getdown.net.HTTPDownloader;
import com.threerings.getdown.util.ConfigUtil;
import com.threerings.getdown.util.LaunchUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.jar.JarFile;

@Service
public class VersionService {

    private static final Logger log = LoggerFactory.getLogger(VersionService.class);
    private static final String DEFAULT_BRANCHES = "https://dl.dropboxusercontent.com/u/74380/lwrt/5/channels.json";

    private final ObjectMapper mapper;
    private final Map<String, String> version = new LinkedHashMap<>();
    private final Map<String, Object> getdown = new LinkedHashMap<>();
    private final List<Branch> branches = new ArrayList<>();
    private LocalDateTime lastCheck = LocalDateTime.now();
    private boolean standalone = false;

    @Autowired
    public VersionService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    private void configure() {
        Properties gradle = new Properties();
        try {
            gradle.load(new FileInputStream("gradle.properties"));
        } catch (IOException ignored) {
        }
        version.put("Implementation-Version", getManifestString("Implementation-Version", gradle.getProperty("version", "custom-v5")));
        version.put("Implementation-Build", getManifestString("Implementation-Build", LwrtUtils.now("yyyyMMddHHmmss")));
        version.put("Git-Describe", getManifestString("Git-Describe", version.get("Version")));
        version.put("Git-Commit", getManifestString("Git-Commit", "0000000"));
        version.putAll(loadGitData());
        getdown.putAll(loadGetdown(new File("getdown.txt")));
        version.put("Current-Version", getCurrentVersion());
        version.put("Current-Branch", getCurrentBranchName());
        log.info("{}", version);
    }

    private String getManifestString(String key, String defaultValue) {
        try (JarFile jar =
                     new JarFile(
                             new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()))) {
            String value = jar.getManifest().getMainAttributes().getValue(key);
            // if value is null, the jar was not packaged through gradle
            return (value == null ? defaultValue : value);
        } catch (IOException | URISyntaxException ignored) {
            // the application is not packed into a JAR
            return defaultValue;
        }
    }

    private Map<String, String> loadGitData() {
        Map<String, String> result = new HashMap<>();
        File gitDir = new File(".git");
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder()
                    .addCeilingDirectory(gitDir)
                    .findGitDir(gitDir);
            if (repositoryBuilder.getGitDir() != null) {
                Git git = new Git(repositoryBuilder.build());
                String describe = git.describe().call();
                if (describe != null) {
                    result.put("Git-Describe", describe);
                }
                String commit = git.getRepository().resolve("HEAD").getName();
                result.put("Git-Commit", commit.substring(0, 7));
            }
        } catch (IOException | GitAPIException e) {
            log.debug("Could not retrieve Git repository info: {}", e.toString());
        }
        return result;
    }

    private Map<String, Object> loadGetdown(File file) {
        try {
            return ConfigUtil.parseConfig(file, false);
        } catch (IOException e) {
            standalone = true;
            return Collections.emptyMap();
        }
    }

    public String getVersion() {
        return version.get("Current-Version");
    }

    public String getBranch() {
        return version.get("Current-Branch");
    }

    public String getImplementationVersion() {
        return version.get("Implementation-Version");
    }

    public String getImplementationBuild() {
        return version.get("Implementation-Build");
    }

    public String getGitDescribe() {
        return version.get("Git-Describe");
    }

    public String getGitCommit() {
        return version.get("Git-Commit");
    }

    public void clear() {
        branches.clear();
    }

    private static SortedSet<Build> getBuildList(Branch branch) {
        if (branch == null)
            throw new IllegalArgumentException("Must set a branch");
        SortedSet<Build> builds = branch.getBuilds();
        if (builds != null) {
            return builds;
        }
        builds = new TreeSet<>();
        if (branch.equals(Branch.STANDALONE)) {
            return builds;
        }
        if (branch.getUrl() == null || branch.getUrl().isEmpty()) {
            log.warn("Invalid url for branch {}", branch);
            return builds;
        }
        String name = "buildlist.txt";
        builds = new TreeSet<>();
        try {
            File local = new File(name).getAbsoluteFile();
            URL url = new URL(branch.getUrl() + name);
            Resource res = new Resource(local.getName(), url, local, false);
            if (download(res)) {
                try {
                    for (String line : Files.readAllLines(local.toPath(), Charset.forName("UTF-8"))) {
                        String[] data = line.split(";");
                        if (data.length == 2) {
                            builds.add(new Build(data[0], data[1], data[1], Long.parseLong(data[0])));
                        } else if (data.length == 3) {
                            builds.add(new Build(data[0], data[1], data[2], Long.parseLong(data[0])));
                        } else {
                            log.warn("Invalid build format: {}", Arrays.asList(data));
                        }
                    }
                } catch (IOException e) {
                    log.warn("Could not read lines from file: " + e);
                }
                res.erase();
                try {
                    Files.deleteIfExists(local.toPath());
                } catch (IOException e) {
                    log.warn("Could not delete file", e);
                }
            }
        } catch (MalformedURLException e) {
            log.warn("Invalid URL: " + e);
        }
        branch.setBuilds(builds);
        return builds;
    }

    /**
     * Retrieves the current development branch the installation is in. This method might trigger
     * {@link #getBranches()} to update available branches
     *
     * @return the current {@link Branch}
     */
    public Branch getCurrentBranch() {
        String branchName = getCurrentBranchName();
        for (Branch branch : getBranches()) {
            if (branch.getId().equals(branchName)) {
                return branch;
            }
        }
        return Branch.STANDALONE;
    }

    public String getCurrentBranchName() {
        String[] value = getMultiValue(getdown, "channel");
        if (value.length == 0)
            return "standalone";
        return value[0];
    }

    private String getCurrentVersion() {
        String[] value = getMultiValue(getdown, "version");
        if (value.length == 0)
            return "0";
        return value[0];
    }

    private void deleteOutdatedResources() {
        String[] toDelete = getMultiValue(getdown, "delete");
        for (String path : toDelete) {
            try {
                if (Files.deleteIfExists(Paths.get(path))) {
                    log.debug("Deleted outdated file: " + path);
                }
            } catch (IOException e) {
                log.warn("Could not delete outdated file", e);
            }
        }
    }

    private static String[] getMultiValue(Map<String, Object> data, String name) {
        // safe way to call this and avoid NPEs
        String[] array = ConfigUtil.getMultiValue(data, name);
        if (array == null)
            return new String[0];
        return array;
    }

    private static void upgrade(String desc, File oldgd, File curgd, File newgd) {
        if (!newgd.exists() || newgd.length() == curgd.length()
                || LwrtUtils.compareCreationTime(newgd, curgd) == 0) {
            log.debug("Resource {} is up to date", desc);
            return;
        }
        log.info("Upgrade {} with {}...", desc, newgd);
        try {
            Files.deleteIfExists(oldgd.toPath());
        } catch (IOException e) {
            log.warn("Could not delete old path: " + e);
        }
        if (!curgd.exists() || curgd.renameTo(oldgd)) {
            if (newgd.renameTo(curgd)) {
                try {
                    Files.deleteIfExists(oldgd.toPath());
                } catch (IOException e) {
                    log.warn("Could not delete old path: " + e);
                }
                try (InputStream in = new FileInputStream(curgd);
                     OutputStream out = new FileOutputStream(newgd)) {
                    LwrtUtils.copy(in, out);
                } catch (IOException e) {
                    log.warn("Problem copying {} back: {}", desc, e);
                }
                return;
            }
            log.warn("Unable to rename to {}", oldgd);
            if (!oldgd.renameTo(curgd)) {
                log.warn("Could not rename {} to {}", oldgd, curgd);
            }
        }
        log.info("Attempting to upgrade by copying over " + curgd + "...");
        try (InputStream in = new FileInputStream(newgd);
             OutputStream out = new FileOutputStream(curgd)) {
            LwrtUtils.copy(in, out);
        } catch (IOException e) {
            log.warn("Brute force copy method also failed", e);
        }
    }

    private static void upgradeLauncher() {
        File oldgd = new File("../lawena-old.exe");
        File curgd = new File("../lawena.exe");
        File newgd = new File("code/lawena-new.exe");
        upgrade("Lawena launcher", oldgd, curgd, newgd);
    }

    private static void upgradeGetdown() {
        File oldgd = new File("getdown-client-old.jar");
        File curgd = new File("getdown-client.jar");
        File newgd = new File("code/getdown-client-new.exe");
        upgrade("Lawena updater", oldgd, curgd, newgd);
    }

    public List<Branch> getBranches() {
        if (branches.isEmpty()) {
            branches.addAll(loadBranches());
        }
        return branches;
    }

    private List<Branch> loadBranches() {
        String[] value = getMultiValue(getdown, "channels");
        String url = value.length > 0 ? value[0] : DEFAULT_BRANCHES;
        File file = new File("channels.json").getAbsoluteFile();
        List<Branch> list = Collections.emptyList();
        try {
            Resource res = new Resource(file.getName(), new URL(url), file, false);
            lastCheck = LocalDateTime.now();
            if (download(res)) {
                try (Reader reader = Files.newBufferedReader(file.toPath(), Charset.forName("UTF-8"))) {
                    TypeReference token = new TypeReference<List<Branch>>() {
                    };
                    list = mapper.readValue(reader, token);
                    for (Branch branch : list) {
                        if (branch.getType() == Branch.Type.SNAPSHOT) {
                            getBuildList(branch);
                        }
                    }
                } catch (FileNotFoundException e) {
                    log.info("No latest version file found");
                } catch (IOException e) {
                    log.warn("Invalid latest version file found: {}", e.toString());
                }
                res.erase();
            }
        } catch (MalformedURLException e) {
            log.warn("Invalid URL: " + e);
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            log.debug("Could not delete branches file: " + e);
        }
        return list;
    }

    private static boolean download(Resource... res) {
        return new HTTPDownloader(Arrays.asList(res), new Downloader.Observer() {

            @Override
            public void resolvingDownloads() {
            }

            @Override
            public boolean downloadProgress(int percent, long remaining) {
                return !Thread.currentThread().isInterrupted();
            }

            @Override
            public void downloadFailed(Resource rsrc, Exception e) {
                log.warn("Download failed: {}", e.toString());
            }
        }).download();
    }

    public void fileCleanup() {
        deleteOutdatedResources();
        upgradeLauncher();
        upgradeGetdown();
    }

    public UpdateResult checkForUpdates() {
        SortedSet<Build> buildList = getBuildList(getCurrentBranch());
        if (buildList.isEmpty()) {
            return UpdateResult.notFound("No builds were found for the current branch");
        }
        Build latest = buildList.first();
        try {
            long current = Long.parseLong(getCurrentVersion());
            if (current < latest.getTimestamp()) {
                return UpdateResult.found(latest);
            } else {
                return UpdateResult.latest("You already have the latest version");
            }
        } catch (NumberFormatException e) {
            log.warn("Bad version format: {}", getCurrentVersion());
            return UpdateResult.found(latest);
        }
    }

    public boolean upgradeApplication(Build build) {
        try {
            return LaunchUtil.updateVersionAndRelaunch(new File("").getAbsoluteFile(),
                    "getdown-client.jar", build.getName());
        } catch (IOException e) {
            log.warn("Could not complete the upgrade", e);
        }
        return false;
    }

    /**
     * A standalone installation means that no deployment descriptor file was found on the application
     * folder.
     *
     * @return <code>true</code> if this install is standalone or <code>false</code> if it is not
     */
    public boolean isStandalone() {
        return standalone;
    }

    public boolean createVersionFile(String version) {
        Path path = Paths.get("version.txt");
        try {
            path = Files.write(path, Collections.singletonList(version), Charset.defaultCharset());
            log.debug("Version file created at {}", path.toAbsolutePath());
            return true;
        } catch (IOException e) {
            log.warn("Could not create version file", e);
            return false;
        }
    }

    public LocalDateTime getLastCheck() {
        return lastCheck;
    }

    public void switchBranch(Branch newBranch) throws IOException {
        String appbase = newBranch.getUrl() + "latest/";
        Path getdownPath = Paths.get("getdown.txt");
        try {
            Files.copy(getdownPath, Paths.get("getdown.bak.txt"));
        } catch (IOException e) {
            log.warn("Could not backup updater metadata file", e);
        }
        List<String> lines = new ArrayList<>();
        lines.add("appbase = " + appbase);
        try {
            for (String line : Files.readAllLines(getdownPath, Charset.forName("UTF-8"))) {
                if (line.startsWith("ui.")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            log.warn("Could not read current updater metadata file", e);
        }
        Files.write(getdownPath, lines, Charset.defaultCharset());
        log.info("New updater metadata file created");
    }

    public List<String> getChangeLog(Branch branch) {
        List<String> list = branch.getChangeLog();
        if (list != null) {
            return list;
        }
        list = Collections.emptyList();
        String url = branch.getUrl();
        if (url == null) {
            return list;
        }
        url = url + "changelog.txt";
        File file = new File("changelog.txt").getAbsoluteFile();
        try {
            Resource res = new Resource(file.getName(), new URL(url), file, false);
            if (download(res)) {
                try {
                    list = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
                    branch.setChangeLog(list);
                } catch (IOException e) {
                    log.warn("Could not read lines from file: " + e);
                }
                res.erase();
            }
        } catch (MalformedURLException e) {
            log.warn("Invalid URL: " + e);
        }
        return list;
    }
}
