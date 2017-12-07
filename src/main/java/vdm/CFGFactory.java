package vdm;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import util.Util;
import vdm.Ticks.Record;
import vdm.Ticks.Tick;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.Util.n;

public class CFGFactory {

    private static final Logger log = Logger.getLogger("lawena");
    private final Path tfPath;
    private final Path moviePath;
    private final Path lawenaPath;

    public CFGFactory(Path tfPath, Path moviePath, Path lawenaPath) {
        this.tfPath = tfPath;
        this.moviePath = moviePath;
        this.lawenaPath = lawenaPath;
    }

    public Path makeCfg(Tick tick, int segmentNumber) throws IOException {
        /*
        CFG file generation from Exec and ExecRecord segments
        -----------------------------------------------------
        Lawena will create CFG files with the commands entered in the template. For CFG generation to
        work, the template must not be empty and also must not begin with "exec ", in which case it's
        assumed that you're calling an already created CFG file that's present in lawena/cfg folder,
        to be moved upon game launch.

        Template variable expansion
        ---------------------------
        You can define certain variables in your exec/exec+record templates. These are:

        - {{BVH_PATH}} resolves into the full path of a BVH named the same as the demo, located in
        your TF dir.
        - {{TF_PATH}} and {{MOVIE_PATH}} for the absolute paths of TF and your movie folder,
        respectively.
        - {{DEMO_NAME}} and {{DEMO_PATH}} for the name of the demo and the full path of it. Also, an
        additional {{DEMO_PATH_NOEXT}} is given with the full path of the demo without the file
        extension. In this way, {{BVH_PATH}} is created by appending ".bvh" to {{DEMO_PATH_NOEXT}}.
        - {{LAWENA_PATH}} resolves into the absolute location of the Lawena folder.
        - {{NEW_LINE}} resolves into a new line.
        */
        String demoCfgName = Util.stripFilenameExtension(tick.getDemoPreview().getFileName());
        Path outputPath = Paths.get("cfg", demoCfgName + "_" + segmentNumber + ".cfg");
        log.info("Generating template #" + segmentNumber + " for Segments " + tick);
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("TF_PATH", tfPath.toAbsolutePath());
        scopes.put("MOVIE_PATH", moviePath.toAbsolutePath());
        scopes.put("DEMO_NAME", demoCfgName);
        scopes.put("DEMO_PATH", tick.getDemoPreview().getAbsoluteFile());
        scopes.put("DEMO_PATH_NOEXT", tfPath.toAbsolutePath().resolve(demoCfgName));
        scopes.put("BVH_PATH", tfPath.toAbsolutePath().resolve(demoCfgName + ".bvh"));
        scopes.put("LAWENA_PATH", lawenaPath.toAbsolutePath());
        scopes.put("NEW_LINE", n);
        Files.deleteIfExists(outputPath);
        try (Writer writer = Files.newBufferedWriter(outputPath, Charset.forName("UTF-8"))) {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new StringReader(tick.getTemplate()), demoCfgName);
            mustache.execute(writer, scopes);
            writer.flush();
        } catch (IOException ex) {
            log.log(Level.WARNING, "Could not generate template", ex);
        }
        return outputPath;
    }
}
