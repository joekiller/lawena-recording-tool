package lwrt;

import lwrt.CustomPath.PathContents;
import lwrt.SettingsManager.Key;
import ui.*;
import util.*;
import vdm.DemoEditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Lawena {

    private static final Logger log = Logger.getLogger("lawena");
    private static final Logger status = Logger.getLogger("status");
    private static final String n = System.getProperty("line.separator");
    private static StartTfTask startTfTask = null;
    private static ClearMoviesTask clearMoviesTask = null;
    private LawenaView view;
    private SettingsManager settings;
    private MovieManager movies;
    private FileManager files;
    private DemoEditor vdm;
    private CommandLine cl;
    private CustomPathList customPaths;
    private AboutDialog dialog;
    private ParticlesDialog particles;
    private SegmentsDialog segments;
    private CustomSettingsDialog customSettings;
    private HashMap<String, ImageIcon> skyboxMap;
    private JFileChooser chooser;
    private String oDxlevel;
    private Thread watcher;
    private Object lastHud;
    private String version = "4.1";
    private String build;
    private UpdateHelper updater;
    private LaunchOptionsDialog launchOptionsDialog;

    public Lawena(SettingsManager cfg) {
        String impl = this.getClass().getPackage().getImplementationVersion();
        if (impl != null) {
            version = impl;
        }
        build = getManifestString("Implementation-Build", Util.now("yyyyMMddHHmmss"));
        String osname = System.getProperty("os.name");
        if (osname.contains("Windows")) {
            cl = new CLWindows();
        } else if (osname.contains("Linux")) {
            cl = new CLLinux();
        } else if (osname.contains("OS X")) {
            cl = new CLOSX();
        } else {
            throw new UnsupportedOperationException("OS not supported");
        }

        // Perform after-update checks
        updater = new UpdateHelper();
        updater.fileCleanup();
        updater.loadChannels();

        settings = cfg;
        log.fine("Retrieving system dxlevel and Steam path");
        oDxlevel = getOriginalDxlevel();

        if (settings.getBoolean(Key.SetSystemLookAndFeel)) {
            log.fine("Setting system look and feel");
            cl.setLookAndFeel();
        }

        // get SteamPath from registry, this value might be invalid or there might not be a value at all
        Path steampath = cl.getSteamPath();

        // retrieve GamePath, attempt resolving via SteamPath, otherwise ask user for it
        Path tfpath = settings.getTfPath();
        if (tfpath == null || tfpath.toString().isEmpty()) {
            tfpath = steampath.resolve(String.join(File.separator,
                "SteamApps", "common", "Team Fortress 2", "tf"));
        }
        log.fine("Checking for game path at " + tfpath);
        if (!tfpath.getFileName().toString().equalsIgnoreCase("tf") || !Files.exists(tfpath)) {
            tfpath = getChosenTfPath();
            if (tfpath == null) {
                log.info("No game directory specified, exiting.");
                JOptionPane.showMessageDialog(null, "No game directory specified, program will exit.",
                    "Invalid GamePath", JOptionPane.WARNING_MESSAGE);
                throw new IllegalArgumentException("A game directory must be specified");
            }
        }
        settings.setTfPath(tfpath);
        files = new FileManager(settings, cl);

        // retrieve MoviePath, always ask user
        Path moviepath = settings.getMoviePath();
        log.info("Checking for movie path at " + moviepath);
        log.info("moviepath:" + moviepath);
        if (moviepath == null || moviepath.toString().isEmpty() || (!moviepath.toString().equals("") && !Files.exists(moviepath))) {
            moviepath = getChosenMoviePath();
            if (moviepath == null) {
                log.info("No movie directory specified, exiting.");
                JOptionPane.showMessageDialog(null, "No movie directory specified, program will exit.",
                    "Invalid MoviePath", JOptionPane.WARNING_MESSAGE);
                throw new IllegalArgumentException("A segment directory must be specified");
            }
        }
        movies = new MovieManager(settings);
        settings.setMoviePath(moviepath);

        log.fine("Saving settings to file");
        settings.save();
        log.fine("Restoring TF2 user files if needed");
        files.restoreAll();

        customPaths = new CustomPathList(settings, cl);
        files.setCustomPathList(customPaths);

        watcher = new Thread(() -> {
            try {
                WatchDir w = new WatchDir(Paths.get("custom"), false) {
                    @Override
                    public void entryCreated(Path child) {
                        try {
                            customPaths.addPath(child);
                        } catch (IOException e) {
                            log.log(Level.FINE, "Could not add custom path", e);
                        }
                    }

                    @Override
                    public void entryModified(Path child) {
                        customPaths.updatePath(child);
                    }

                    @Override
                    public void entryDeleted(Path child) {
                        customPaths.removePath(child);
                    }
                };
                w.processEvents();
            } catch (IOException e) {
                log.log(Level.FINE, "Problem while watching directory", e);
            }
        }, "FolderWatcher");
        watcher.setDaemon(true);

        vdm = new DemoEditor(settings, cl);

        log.fine("Init complete - Ready to display GUI");
    }

    private static ImageIcon createPreviewIcon(String imageName) throws IOException {
        int size = 96;
        BufferedImage image;
        File input = new File(imageName);
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        image.createGraphics().drawImage(
            ImageIO.read(input).getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
        return new ImageIcon(image);
    }

    private static void registerValidation(JComboBox<String> combo, final String validationRegex,
                                           final JLabel label) {
        final JTextComponent tc = (JTextComponent) combo.getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            private void validateInput() {
                if (tc.getText().matches(validationRegex)) {
                    label.setForeground(Color.BLACK);
                } else {
                    label.setForeground(Color.RED);
                }
            }
        });
    }

    private static void selectComboItem(JComboBox<String> combo, String selectedValue,
                                        List<String> possibleValues) {
        if (possibleValues == null || possibleValues.isEmpty()) {
            combo.setSelectedItem(selectedValue);
        } else {
            int i = possibleValues.indexOf(selectedValue);
            if (i >= 0) {
                combo.setSelectedIndex(i);
            }
        }
    }

    private String getOriginalDxlevel() {
        String level = cl.getSystemDxLevel();
        switch (level) {
            case "62":
                log.info("System dxlevel: 98");
                break;
            case "5f":
                log.info("System dxlevel: 95");
                break;
            case "5a":
                log.info("System dxlevel: 90");
                break;
            case "51":
                log.info("System dxlevel: 81");
                break;
            case "50":
                log.info("System dxlevel: 80");
                break;
            default:
                log.warning("Invalid system dxlevel value found: " + level + ". Reverting to 95");
                cl.setSystemDxLevel("5f");
                return "5f";
        }
        return level;
    }

    private String getManifestString(String key, String defaultValue) {
        try (JarFile jar =
                 new JarFile(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
                     .toURI()))) {
            String value = jar.getManifest().getMainAttributes().getValue(key);
            return (value == null ? "bat." + defaultValue : value);
        } catch (IOException | URISyntaxException ignored) {
        }
        return "custom." + defaultValue;
    }

    private String shortver() {
        String[] arr = version.split("-");
        return arr[0] + (arr.length > 1 ? "-" + arr[1] : "");
    }

    public void start() {
        view = new LawenaView();

        new StartLogger("lawena").toTextComponent(settings.getLogUiLevel(), view.getTextAreaLog());
        new StartLogger("status").toLabel(Level.FINE, view.getLblStatus());
        log.fine("Lawena Recording Tool " + version + " build " + build);
        log.fine("TF2 path: " + settings.getTfPath());
        log.fine("Movie path: " + settings.getMoviePath());
        log.fine("Lawena path: " + Paths.get("").toAbsolutePath());

        view.setTitle("Lawena Recording Tool " + shortver());
        URL url = Lawena.class.getClassLoader().getResource("ui/tf2.png");
        if (url != null) {
            view.setIconImage(new ImageIcon(url).getImage());
        }
        view.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                saveAndExit();
            }

        });
        view.getMntmAbout().addActionListener(e -> {
            if (dialog == null) {
                dialog = new AboutDialog(version, build);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.setModalityType(ModalityType.APPLICATION_MODAL);
                dialog.getBtnUpdater().addActionListener(e1 -> updater.showSwitchUpdateChannelDialog());
            }
            dialog.setVisible(true);
        });
        view.getMntmSelectEnhancedParticles().addActionListener(e -> startParticlesDialog());
        view.getMntmAddCustomSettings().addActionListener(e -> startCustomSettingsDialog());

        final JTable table = view.getTableCustomContent();
        table.setModel(customPaths);
        table.getColumnModel().getColumn(0).setMaxWidth(20);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        table.setDefaultRenderer(CustomPath.class, new TooltipRenderer(settings));
        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() == CustomPathList.Column.SELECTED.ordinal()) {
                int row = e.getFirstRow();
                TableModel model = (TableModel) e.getSource();
                CustomPath cp = (CustomPath) model.getValueAt(row, CustomPathList.Column.PATH.ordinal());
                checkCustomHud(cp);
                if (cp == CustomPathList.particles && cp.isSelected()) {
                    startParticlesDialog();
                }
            }
        });
        table.setDropTarget(new DropTarget() {

            private static final long serialVersionUID = 1L;

            @Override
            public synchronized void dragOver(DropTargetDragEvent dtde) {
                Point point = dtde.getLocation();
                int row = table.rowAtPoint(point);
                if (row < 0) {
                    table.clearSelection();
                } else {
                    table.setRowSelectionInterval(row, row);
                }
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            }

            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = dtde.getTransferable();
                    List<?> fileList = null;
                    try {
                        fileList = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        if (fileList.size() > 0) {
                            table.clearSelection();
                            for (Object value : fileList) {
                                if (value instanceof File) {
                                    File f = (File) value;
                                    log.info("Attempting to copy " + f.toPath());
                                    new PathCopyTask(f.toPath()).execute();
                                }
                            }
                        }
                    } catch (UnsupportedFlavorException e) {
                        log.log(Level.FINE, "Drag and drop operation failed", e);
                    } catch (IOException e) {
                        log.log(Level.FINE, "Drag and drop operation failed", e);
                    }
                } else {
                    dtde.rejectDrop();
                }
            }
        });
        TableRowSorter<CustomPathList> sorter = new TableRowSorter<>(customPaths);
        table.setRowSorter(sorter);
        RowFilter<CustomPathList, Object> filter = new RowFilter<CustomPathList, Object>() {
            @Override
            public boolean include(Entry<? extends CustomPathList, ?> entry) {
                CustomPath cp = (CustomPath) entry.getValue(CustomPathList.Column.PATH.ordinal());
                return !cp.getContents().contains(PathContents.READONLY);
            }
        };
        sorter.setRowFilter(filter);

        SwingWorker<Void, Void> scannerTask = new PathScanTask();
        SwingWorker<Void, Void> skySetupTask = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    configureSkyboxes(view.getCmbSkybox());
                } catch (Exception e) {
                    log.log(Level.INFO, "Problem while configuring skyboxes", e);
                }
                return null;
            }

            @Override
            protected void done() {
                selectSkyboxFromSettings();
            }

        };
        scannerTask.execute();
        skySetupTask.execute();
        waitForTask(scannerTask);
        waitForTask(skySetupTask);

        loadSettings();

        view.getMntmChangeTfDirectory().addActionListener(new Tf2FolderChange());
        view.getMntmChangeMovieDirectory().addActionListener(new MovieFolderChange());
        view.getSelectHlaeLocation().addActionListener(new HlaePathChange());
        view.getMntmRevertToDefault().addActionListener(e -> {
            Path movies = settings.getMoviePath();
            settings.loadDefaults();
            settings.setMoviePath(movies);
            loadSettings();
            customPaths.loadResourceSettings();
            loadHudComboState();
            saveSettings();
        });
        view.getMntmExit().addActionListener(e -> saveAndExit());
        view.getMntmSaveSettings().addActionListener(e -> saveSettings());
        view.getBtnStartTf().addActionListener(e -> new StartTfTask().execute());
        view.getBtnClearMovieFolder().addActionListener(e -> startSegmentsDialog());
        view.getMntmOpenGameFolder().addActionListener(e -> new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Desktop.getDesktop().open(settings.getTfPath().toFile());
                } catch (IOException ex) {
                    log.log(Level.FINE, "Could not open game folder", ex);
                }
                return null;
            }
        }.execute());
        view.getMntmOpenMovieFolder().addActionListener(e -> new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Desktop.getDesktop().open(settings.getMoviePath().toFile());
                } catch (IOException ex) {
                    log.log(Level.FINE, "Could not open movie folder", ex);
                }
                return null;
            }
        }.execute());
        view.getMntmOpenCustomFolder().addActionListener(e -> new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Desktop.getDesktop().open(Paths.get("custom").toFile());
                } catch (IOException ex) {
                    log.log(Level.FINE, "Could not open custom folder", ex);
                }
                return null;
            }
        }.execute());
        view.getMntmLaunchTimeout().addActionListener(e -> {
            Object answer =
                JOptionPane.showInputDialog(view, String.join(n,
                    "Enter the number of seconds to wait",
                    "before interrupting TF2 launch.",
                    "Enter 0 to disable timeout."),
                    "Launch Timeout", JOptionPane.PLAIN_MESSAGE, null, null,
                    settings.getLaunchTimeout());
            if (answer != null) {
                try {
                    int value = Integer.parseInt(answer.toString());
                    settings.setLaunchTimeout(value);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(view, "Invalid value, must be 0 or higher integer.",
                        "Launch Options", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        view.getCustomLaunchOptionsMenuItem().addActionListener(e -> {
            if (launchOptionsDialog == null) {
                launchOptionsDialog = new LaunchOptionsDialog();
            }
            launchOptionsDialog.getOptionsTextField().setText(settings.getString(Key.LaunchOptions));
            int result = launchOptionsDialog.showDialog();
            if (result == JOptionPane.YES_OPTION) {
                String launchOptions = launchOptionsDialog.getOptionsTextField().getText();
                settings.setString(Key.LaunchOptions, launchOptions);
            } else if (result == 1) {
                settings.setString(Key.LaunchOptions, (String) Key.LaunchOptions.defValue());
            }
        });
        view.getCmbViewmodel().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkViewmodelState();
            }
        });
        view.getCmbSourceVideoFormat().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkFrameFormatState();
            }
        });

        view.getTabbedPane().addTab("VDM", null, vdm.start());
        view.setVisible(true);
    }

    private void waitForTask(SwingWorker<?, ?> worker) {
        try {
            worker.get();
        } catch (InterruptedException | ExecutionException e) {
            log.warning("Task was interrupted or cancelled: " + e.toString());
        }
    }

    private void checkViewmodelState() {
        boolean e = view.getCmbViewmodel().getSelectedIndex() != 1;
        view.getLblViewmodelFov().setEnabled(e);
        view.getSpinnerViewmodelFov().setEnabled(e);
    }

    private void checkFrameFormatState() {
        boolean e = view.getCmbSourceVideoFormat().getSelectedIndex() == 1;
        view.getLblJpegQuality().setEnabled(e);
        view.getSpinnerJpegQuality().setEnabled(e);
    }

    private void startCustomSettingsDialog() {
        if (customSettings == null) {
            customSettings = new CustomSettingsDialog();
            customSettings.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            customSettings.setModalityType(ModalityType.APPLICATION_MODAL);
            customSettings.setResizable(true);
            final JTextArea textArea = customSettings.getTextArea();
            customSettings.getOkButton().addActionListener(e -> {
                customSettings.setVisible(false);
                log.info("Saving custom settings");
                saveSettings();
            });
            customSettings.getCancelButton().addActionListener(e -> {
                customSettings.setVisible(false);
                customSettings.getTextArea().setText(settings.getCustomSettings());
            });
            customSettings.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    customSettings.getTextArea().setText(settings.getCustomSettings());
                }
            });
        }
        customSettings.setBounds(100, 100, settings.getInt(Key.CustomSettingsDialogWidth),
            settings.getInt(Key.CustomSettingsDialogHeight));
        customSettings.getTextArea().setText(settings.getCustomSettings());
        customSettings.setVisible(true);
    }

    private void startParticlesDialog() {
        if (particles == null) {
            particles = new ParticlesDialog();
            particles.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            particles.setModalityType(ModalityType.APPLICATION_MODAL);
            DefaultTableModel dtm = new DefaultTableModel(0, 2) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0;
                }

                @Override
                public java.lang.Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Boolean.class : String.class;
                }

                ;

                @Override
                public String getColumnName(int column) {
                    return column == 0 ? "" : "Particle filename";
                }
            };
            final JTable tableParticles = particles.getTableParticles();
            particles.getOkButton().addActionListener(e -> {
                List<String> selected = new ArrayList<>();
                int selectCount = 0;
                for (int i = 0; i < tableParticles.getRowCount(); i++) {
                    if ((boolean) tableParticles.getValueAt(i, 0)) {
                        selectCount++;
                        selected.add((String) tableParticles.getValueAt(i, 1));
                    }
                }
                if (selectCount == 0) {
                    settings.setParticles(Collections.singletonList(""));
                } else if (selectCount == tableParticles.getRowCount()) {
                    settings.setParticles(Collections.singletonList("*"));
                } else {
                    settings.setParticles(selected);
                }
                log.finer("Particles: " + settings.getParticles());
                particles.setVisible(false);
            });
            particles.getCancelButton().addActionListener(e -> {
                List<String> selected = settings.getParticles();
                boolean selectAll = selected.contains("*");
                for (int i = 0; i < tableParticles.getRowCount(); i++) {
                    tableParticles.setValueAt(
                        selectAll || selected.contains(tableParticles.getValueAt(i, 1)), i, 0);
                }
                log.finer("Particles: " + selected);
                particles.setVisible(false);
            });
            tableParticles.setModel(dtm);
            tableParticles.getColumnModel().getColumn(0).setMaxWidth(20);
            List<String> selected = settings.getParticles();
            boolean selectAll = selected.contains("*");
            for (String particle : cl.getVpkContents(settings.getTfPath(),
                CustomPathList.particles.getPath())) {
                dtm.addRow(new Object[]{selectAll || selected.contains(particle), particle});
            }
        }
        particles.setVisible(true);
    }

    private DefaultTableModel newSegmentsModel() {
        return new DefaultTableModel(0, 2) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public java.lang.Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public String getColumnName(int column) {
                return column == 0 ? "" : "Name";
            }
        };
    }

    private SegmentsDialog newSegmentsDialog() {
        final SegmentsDialog d = new SegmentsDialog();
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setModalityType(ModalityType.APPLICATION_MODAL);
        DefaultTableModel dtm = newSegmentsModel();
        final JTable tableSegments = d.getTableSegments();
        d.getOkButton().addActionListener(e -> {
            List<String> selected = new ArrayList<>();
            int selectCount = 0;
            for (int i = 0; i < tableSegments.getRowCount(); i++) {
                if ((boolean) tableSegments.getValueAt(i, 0)) {
                    selectCount++;
                    selected.add((String) tableSegments.getValueAt(i, 1));
                }
            }
            if (selectCount > 0) {
                new ClearMoviesTask(selected).execute();
            } else {
                log.info("No segments selected to remove");
            }
            d.setVisible(false);
        });
        d.getCancelButton().addActionListener(e -> {
            d.setVisible(false);
        });
        tableSegments.setModel(dtm);
        tableSegments.getColumnModel().getColumn(0).setMaxWidth(20);
        return d;
    }

    private void startSegmentsDialog() {
        if (segments == null) {
            segments = newSegmentsDialog();
        }
        DefaultTableModel tmodel = (DefaultTableModel) segments.getTableSegments().getModel();
        tmodel.setRowCount(0);
        List<String> segs = getExistingSegments();
        if (segs.isEmpty()) {
            JOptionPane.showMessageDialog(view, "There are no segments to delete", "Delete Segments",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (String seg : segs) {
                tmodel.addRow(new Object[]{false, seg});
            }
            segments.setVisible(true);
        }
    }

    private List<String> getExistingSegments() {
        List<String> existingSegments = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(settings.getMoviePath(), "*.wav")) {
            for (Path path : stream) {
                String segname = path.getFileName().toString();
                int index = segname.indexOf("_");
                if (index > 0) {
                    String key = segname.substring(0, segname.indexOf("_"));
                    if (!existingSegments.contains(key)) {
                        existingSegments.add(key);
                    }
                }
            }
        } catch (NoSuchFileException e) {
            // TODO: add a check for the reparse point (junction) to confirm it's SrcDemo2
            log.info("Could not scan for existing segments. Is SrcDemo2 running?");
        } catch (IOException e) {
            log.log(Level.INFO, "Problem while scanning movie folder", e);
        }
        return existingSegments;
    }

    private boolean checkCustomHud(CustomPath cp) {
        EnumSet<PathContents> set = cp.getContents();
        if (cp.isSelected()) {
            if (set.contains(PathContents.HUD)) {
                lastHud = view.getCmbHud().getSelectedItem();
                view.getCmbHud().setSelectedItem("Custom");
                log.finer("HUD combobox disabled");
                view.getCmbHud().setEnabled(false);
                return true;
            }
        } else {
            if (set.contains(PathContents.HUD)) {
                if (lastHud != null) {
                    view.getCmbHud().setSelectedItem(lastHud);
                }
                log.finer("HUD combobox enabled");
                view.getCmbHud().setEnabled(true);
                return false;
            }
        }
        return false;
    }

    private void loadHudComboState() {
        boolean detected = false;
        for (CustomPath cp : customPaths.getList()) {
            if (detected) {
                break;
            }
            detected = checkCustomHud(cp);
        }
    }

    private void loadSettings() {
        registerValidation(view.getCmbResolution(), "[1-9][0-9]*x[1-9][0-9]*", view.getLblResolution());
        registerValidation(view.getCmbFramerate(), "[1-9][0-9]*", view.getLblFrameRate());
        selectComboItem(view.getCmbHud(), settings.getHud(), Key.Hud.getAllowedValues());
        selectComboItem(view.getCmbQuality(), settings.getDxlevel(), Key.DxLevel.getAllowedValues());
        selectComboItem(view.getCmbViewmodel(), settings.getViewmodelSwitch(),
            Key.ViewmodelSwitch.getAllowedValues());
        selectSkyboxFromSettings();
        view.getCmbResolution().setSelectedItem(settings.getWidth() + "x" + settings.getHeight());
        view.getCmbFramerate().setSelectedItem(settings.getFramerate() + "");
        try {
            view.getSpinnerViewmodelFov().setValue(settings.getViewmodelFov());
        } catch (IllegalArgumentException ignored) {
        }
        view.getEnableMotionBlur().setSelected(settings.getMotionBlur());
        view.getDisableCombatText().setSelected(!settings.getCombattext());
        view.getDisableCrosshair().setSelected(!settings.getCrosshair());
        view.getDisableCrosshairSwitch().setSelected(!settings.getCrosshairSwitch());
        view.getDisableHitSounds().setSelected(!settings.getHitsounds());
        view.getDisableVoiceChat().setSelected(!settings.getVoice());
        view.getUseHudMinmode().setSelected(settings.getHudMinmode());
        view.getChckbxmntmBackupMode().setSelected(settings.getBoolean(Key.DeleteBackupsWhenRestoring));
        view.getInstallFonts().setSelected(settings.getBoolean(Key.InstallFonts));
        view.getSourceLaunch().setSelected(true);
        view.getSourceLaunch().setSelected(settings.getString(Key.LaunchMode).equals("hl2"));
        view.getSteamLaunch().setSelected(settings.getString(Key.LaunchMode).equals("steam"));
        view.getHlaeLaunch().setSelected(settings.getString(Key.LaunchMode).equals("hlae"));
        view.getCopyUserConfig().setSelected(settings.getBoolean(Key.CopyUserConfig));
        view.getUsePlayerModel().setSelected(settings.getHudPlayerModel());
        view.getCmbSourceVideoFormat().setSelectedItem(
            settings.getString(Key.SourceRecorderVideoFormat).toUpperCase());
        view.getSpinnerJpegQuality().setValue(settings.getInt(Key.SourceRecorderJpegQuality));
        checkViewmodelState();
        checkFrameFormatState();
    }

    private void saveSettings() {
        String selectedResolution = ((String) view.getCmbResolution().getSelectedItem());
        String[] resolution = selectedResolution != null ? selectedResolution.split("x") : new String[]{};
        if (resolution.length == 2) {
            settings.setWidth(Integer.parseInt(resolution[0]));
            settings.setHeight(Integer.parseInt(resolution[1]));
        } else {
            log.fine("Bad resolution format, reverting to previously saved");
            view.getCmbResolution().setSelectedItem(settings.getWidth() + "x" + settings.getHeight());
        }
        String framerate = (String) view.getCmbFramerate().getSelectedItem();
        if (framerate != null) {
            settings.setFramerate(Integer.parseInt(framerate));
        }
        settings.setHud(Key.Hud.getAllowedValues().get(view.getCmbHud().getSelectedIndex()));
        settings.setViewmodelSwitch(Key.ViewmodelSwitch.getAllowedValues().get(
            view.getCmbViewmodel().getSelectedIndex()));
        settings.setViewmodelFov((int) view.getSpinnerViewmodelFov().getValue());
        settings
            .setDxlevel(Key.DxLevel.getAllowedValues().get(view.getCmbQuality().getSelectedIndex()));
        settings.setMotionBlur(view.getEnableMotionBlur().isSelected());
        settings.setCombattext(!view.getDisableCombatText().isSelected());
        settings.setCrosshair(!view.getDisableCrosshair().isSelected());
        settings.setCrosshairSwitch(!view.getDisableCrosshairSwitch().isSelected());
        settings.setHitsounds(!view.getDisableHitSounds().isSelected());
        settings.setVoice(!view.getDisableVoiceChat().isSelected());
        settings.setSkybox((String) view.getCmbSkybox().getSelectedItem());
        Path tfpath = settings.getTfPath();
        List<String> selected = new ArrayList<>();
        for (CustomPath cp : customPaths.getList()) {
            Path path = cp.getPath();
            if (!cp.getContents().contains(PathContents.READONLY) && cp.isSelected()) {
                String key = (path.startsWith(tfpath) ? "tf*" : "");
                key += path.getFileName().toString();
                selected.add(key);
            }
        }
        settings.setCustomResources(selected);
        settings.setHudMinmode(view.getUseHudMinmode().isSelected());
        settings
            .setBoolean(Key.DeleteBackupsWhenRestoring, view.getChckbxmntmBackupMode().isSelected());
        settings.setBoolean(Key.InstallFonts, view.getInstallFonts().isSelected());
        settings.setBoolean(Key.CopyUserConfig, view.getCopyUserConfig().isSelected());
        if (view.getSourceLaunch().isSelected()) {
            settings.setString(Key.LaunchMode, "hl2");
        } else if (view.getSteamLaunch().isSelected()) {
            settings.setString(Key.LaunchMode, "steam");
        } else if (view.getHlaeLaunch().isSelected()) {
            settings.setString(Key.LaunchMode, "hlae");
        }
        settings.setHudPlayerModel(view.getUsePlayerModel().isSelected());
        Object selectedSourceVideoFormat = view.getCmbSourceVideoFormat().getSelectedItem();
        if (selectedSourceVideoFormat != null) {
            settings.setString(Key.SourceRecorderVideoFormat, selectedSourceVideoFormat.toString().toLowerCase());
        }
        settings.setInt(Key.SourceRecorderJpegQuality, (int) view.getSpinnerJpegQuality().getValue());
        if (customSettings != null) {
            settings.setCustomSettings(customSettings.getTextArea().getText());
            settings.setInt(Key.CustomSettingsDialogWidth, customSettings.getWidth());
            settings.setInt(Key.CustomSettingsDialogHeight, customSettings.getHeight());
        }
        settings.save();
        log.fine("Settings saved");
    }

    private void saveAndExit() {
        saveSettings();
        view.setVisible(false);
        if (!cl.isRunningTF2()) {
            files.restoreAll();
        }
        System.exit(0);
    }

    private void configureSkyboxes(final JComboBox<String> combo) {
        final Vector<String> data = new Vector<>();
        Path dir = Paths.get("skybox");
        if (Files.exists(dir)) {
            log.finer("Loading skyboxes from folder");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*up.vtf")) {
                for (Path path : stream) {
                    log.finer("Skybox found at: " + path);
                    String skybox = path.toFile().getName();
                    skybox = skybox.substring(0, skybox.indexOf("up.vtf"));
                    data.add(skybox);
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Problem while loading skyboxes", e);
            }
        }
        skyboxMap = new HashMap<>(data.size());
        new SkyboxPreviewTask(new ArrayList<>(data)).execute();
        data.add(0, (String) Key.Skybox.defValue());
        combo.setModel(new DefaultComboBoxModel<>(data));
        combo.addActionListener(e -> {
            ImageIcon preview = skyboxMap.get(combo.getSelectedItem());
            view.getLblPreview().setText(preview == null ? "" : "Preview:");
            view.getLblSkyboxPreview().setIcon(preview);
        });

    }

    private void selectSkyboxFromSettings() {
        view.getCmbSkybox().setSelectedItem(settings.getSkybox());
    }

    private Path getChosenMoviePath() {
        Path selected = null;
        File curDir = settings.getMoviePath() != null ? settings.getMoviePath().toFile() : null;
        int ret = 0;
        while ((selected == null && ret == 0) || (selected != null && !Files.exists(selected))) {
            chooser = new JFileChooser();
            chooser.setDialogTitle("Choose a directory to store your movie files");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setCurrentDirectory(curDir);
            ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selected = chooser.getSelectedFile().toPath();
            } else {
                selected = null;
            }
            log.finer("Selected MoviePath: " + selected);
        }
        return selected;
    }

    private Path getChosenTfPath() {
        Path selected = null;
        File curDir = settings.getTfPath() != null ? settings.getTfPath().toFile() : null;
        int ret = 0;
        while ((selected == null && ret == 0)
            || (selected != null && (!Files.exists(selected) || !selected.getFileName().toString()
            .equals("tf")))) {
            chooser = new JFileChooser();
            chooser.setDialogTitle("Choose your \"tf\" directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setCurrentDirectory(curDir);
            chooser.setFileHidingEnabled(false);
            ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selected = chooser.getSelectedFile().toPath();
            } else {
                selected = null;
            }
            log.finer("Selected GamePath: " + selected);
        }
        return selected;
    }

    private Path getChosenHlaePath() {
        Path selected = null;
        int ret = 0;
        while ((selected == null && ret == 0) || (selected != null && (!Files.exists(selected)))) {
            chooser = new JFileChooser();
            chooser.setDialogTitle("Choose the HLAE executable");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileHidingEnabled(false);
            chooser.setFileFilter(new FileFilter() {

                @Override
                public String getDescription() {
                    return "HLAE Executable";
                }

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().equalsIgnoreCase("HLAE.exe");
                }
            });
            ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selected = chooser.getSelectedFile().toPath();
            } else {
                selected = null;
            }
            log.finer("Selected HLAE path: " + selected);
        }
        return selected;
    }

    private void setCurrentWorker(final SwingWorker<?, ?> worker, final boolean indeterminate) {
        SwingUtilities.invokeLater(() -> {
            if (worker != null) {
                view.getProgressBar().setVisible(true);
                view.getProgressBar().setIndeterminate(indeterminate);
                view.getProgressBar().setValue(0);
                worker.addPropertyChangeListener(evt -> {
                    if ("progress".equals(evt.getPropertyName())) {
                        view.getProgressBar().setValue((Integer) evt.getNewValue());
                    }
                });
            } else {
                view.getProgressBar().setVisible(false);
                view.getProgressBar().setIndeterminate(indeterminate);
                view.getProgressBar().setValue(0);
            }
        });

    }

    public class MovieFolderChange implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (startTfTask == null) {
                Path newpath = getChosenMoviePath();
                if (newpath != null) {
                    settings.setMoviePath(newpath);
                    JOptionPane.showMessageDialog(view, String.format("New movie folder: %s", newpath),
                        "Change Movie Folder", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(view, "Please wait until TF2 has stopped running");
            }
        }

    }

    public class ClearMoviesTask extends SwingWorker<Void, Path> {

        private int count = 0;
        private List<String> segmentsToDelete;

        public ClearMoviesTask() {
        }

        public ClearMoviesTask(List<String> segmentsToDelete) {
            this.segmentsToDelete = segmentsToDelete;
        }

        @Override
        protected Void doInBackground() throws Exception {
            SwingUtilities.invokeAndWait(() -> view.getBtnClearMovieFolder().setEnabled(false));
            if (clearMoviesTask == null) {
                String segmentsGlob = "";
                if (segmentsToDelete != null && !segmentsToDelete.isEmpty()) {
                    segmentsGlob = segmentsToDelete.toString()
                        .replace("[", "{")
                        .replace("]", "}")
                        .replace(" ", "");
                    log.info("Deleting segments: " + segmentsGlob);
                } else {
                    int answer =
                        JOptionPane.showConfirmDialog(view,
                            "Are you sure you want to clear ALL movie files?", "Clearing Movie Files",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (answer != JOptionPane.YES_NO_OPTION) {
                        return null;
                    }
                }
                try (DirectoryStream<Path> stream =
                         Files.newDirectoryStream(settings.getMoviePath(), segmentsGlob + "*.{tga,wav}")) {

                    clearMoviesTask = this;
                    setCurrentWorker(this, true);
                    SwingUtilities.invokeAndWait(() -> {
                        view.getBtnClearMovieFolder().setEnabled(true);
                        view.getBtnClearMovieFolder().setText("Stop Clearing");
                    });

                    for (Path path : stream) {
                        if (isCancelled()) {
                            break;
                        }
                        try {
                            path.toFile().setWritable(true);
                            Files.delete(path);
                            publish(path);
                        } catch (IOException e) {
                            log.log(Level.INFO, "Could not delete a file", e);
                        }
                    }

                } catch (IOException ex) {
                    log.log(Level.INFO, "Problem while clearing movie folder", ex);
                }
            } else {
                log.fine("Cancelling movie folder clearing task");
                status.info("Cancelling task");
                clearMoviesTask.cancel(true);
            }

            return null;
        }

        @Override
        protected void process(List<Path> chunks) {
            count += chunks.size();
            status.info("Deleting " + count + " files from movie folder...");
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                clearMoviesTask = null;
                setCurrentWorker(null, false);
                if (count > 0) {
                    log.fine("Movie folder cleared: " + count + " files deleted");
                } else {
                    log.fine("Movie folder already clean, no files deleted");
                }
                view.getBtnClearMovieFolder().setEnabled(true);
                view.getBtnClearMovieFolder().setText("Clear Movie Files");
                status.info("");
            }
        }

    }

    public class StartTfTask extends SwingWorker<Boolean, Void> {

        @Override
        protected Boolean doInBackground() throws Exception {
            try {
                return doStuff();
            } catch (Exception e) {
                log.log(Level.WARNING, "Start operation was interrupted or failed", e);
                return false;
            }
        }

        private Boolean doStuff() throws InvocationTargetException, InterruptedException {
            SwingUtilities.invokeAndWait(() -> view.getBtnStartTf().setEnabled(false));
            if (startTfTask == null) {
                startTfTask = this;
                setCurrentWorker(this, false);
                setProgress(0);

                // Checking if the user selects "Custom" HUD in the dropdown,
                // he or she also selects a "hud" in the sidebar
                if (!verifyCustomHud()) {
                    JOptionPane.showMessageDialog(view,
                        String.join(n,
                            "Please select a custom HUD in the",
                            "Custom Resources table and retry"),
                        "Custom HUD",
                        JOptionPane.INFORMATION_MESSAGE);
                    log.info("Launch aborted because the custom HUD to use was not specified");
                    return false;
                }

                // Check for big custom folders, mitigate OOM errors with custom folder > 2 GB
                Path tfpath = settings.getTfPath();
                Path customPath = tfpath.resolve("custom");

                setProgress(20);
                closeOpenHandles();

                // Restoring user files
                status.info("Restoring your files");
                files.restoreAll();
                setProgress(40);

                // Saving ui settings to cfg files
                status.info("Saving settings and generating cfg files");
                try {
                    saveSettings();
                    settings.saveToCfg();
                    movies.createMovienameCfgs();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Problem while saving settings to file", e);
                    status.info("Failed to save lawena settings to file");
                    return false;
                }

                // Allow failing this without cancelling launch, notify user. See #36
                try {
                    movies.movieOffset();
                } catch (IOException e) {
                    log.info("Could not detect current movie slot");
                }

                setProgress(60);

                // Backing up user files and copying lawena files
                status.info("Copying lawena files to cfg and custom...");
                try {
                    files.replaceAll();
                } catch (LawenaException e) {
                    status.info(e.getMessage());
                    return false;
                }
                setProgress(80);

        /*
         * Scan for all .fon, .ttf, .ttc, or .otf files inside custom and get their parent folders
         * to register every font file using the FontReg utility at
         * http://code.kliu.org/misc/fontreg/. This is an attempt to fix the
         * "Windows locking uninstalled fonts used by TF2 custom HUDs" issue.
         */
                if (settings.getBoolean(Key.InstallFonts)) {
                    try {
                        status.info("Registering all custom fonts found...");
                        for (Path folder : FileManager.scanFonts(customPath)) {
                            cl.registerFonts(folder);
                        }
                    } catch (IOException e) {
                        log.warning("Could not scan for custom fonts, you might be susceptible to font locking issue: "
                            + e.toString());
                    }
                } else {
                    log.info("Skipping custom HUD font install");
                }

                // Launching process
                status.info("Launching TF2 process");
                cl.startTf(settings);

                SwingUtilities.invokeAndWait(() -> {
                    view.getBtnStartTf().setEnabled(true);
                    view.getBtnStartTf().setText("Stop Team Fortress 2");
                });
                setProgress(100);

                int timeout = 0;
                int cfgtimeout = settings.getLaunchTimeout();
                int millis = 5000;
                int maxtimeout = cfgtimeout / (millis / 1000);
                setProgress(0);
                status.info("Waiting for TF2 to start...");
                if (cfgtimeout > 0) {
                    log.fine("TF2 launch timeout: around " + cfgtimeout + " seconds");
                } else {
                    log.fine("TF2 launch timeout disabled");
                }
                while (!cl.isRunningTF2() && (cfgtimeout == 0 || timeout < maxtimeout)) {
                    ++timeout;
                    if (cfgtimeout > 0) {
                        setProgress((int) ((double) timeout / maxtimeout * 100));
                    }
                    Thread.sleep(millis);
                }

                if (cfgtimeout > 0 && timeout >= maxtimeout) {
                    int s = timeout * (millis / 1000);
                    log.info("TF2 launch timed out after " + s + " seconds");
                    status.info("TF2 did not start after " + s + " seconds");
                    return false;
                }

                log.fine("TF2 has started running");
                status.info("Waiting for TF2 to finish running...");
                SwingUtilities.invokeLater(() -> view.getProgressBar().setIndeterminate(true));
                while (cl.isRunningTF2()) {
                    Thread.sleep(millis);
                }

                Thread.sleep(5000);
                closeOpenHandles();

            } else {
                if (cl.isRunningTF2()) {
                    status.info("Attempting to finish TF2 process...");
                    cl.killTF2Process();
                    Thread.sleep(5000);
                    if (!cl.isRunningTF2()) {
                        startTfTask.cancel(true);
                    }
                    closeOpenHandles();
                } else {
                    status.info("TF2 was not running, cancelling");
                }
            }

            return true;
        }

        private void closeOpenHandles() {
            status.info("Closing open handles in TF2 'cfg' folder...");
            cl.closeHandles(settings.getTfPath().resolve("cfg"));
            status.info("Closing open handles in TF2 'custom' folder...");
            cl.closeHandles(settings.getTfPath().resolve("custom"));
            if (settings.getString(Key.LaunchMode).equals("hlae")) {
                status.info("Stopping HLAE executable...");
                cl.killHLAEProcess();
            }
        }

        private boolean verifyCustomHud() {
            Object selectedItem = view.getCmbHud().getSelectedItem();
            if ("Custom".equals(selectedItem)) {
                for (CustomPath cp : customPaths.getList()) {
                    customPaths.update(cp);
                    EnumSet<PathContents> set = cp.getContents();
                    if (cp.isSelected() && set.contains(PathContents.HUD)) {
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                startTfTask = null;
                setCurrentWorker(null, false);
                view.getBtnStartTf().setEnabled(false);
                boolean ranTf2Correctly = false;
                try {
                    ranTf2Correctly = get();
                } catch (InterruptedException | ExecutionException ignored) {
                }
                boolean restoredAllFiles = files.restoreAll();
                if (ranTf2Correctly) {
                    if (restoredAllFiles) {
                        status.info("TF2 has finished running. All files restored");
                    } else {
                        status.info("Your files could not be restored correctly. Check log for details");
                    }
                }
                cl.setSystemDxLevel(oDxlevel);
                view.getBtnStartTf().setText("Start Team Fortress 2");
                view.getBtnStartTf().setEnabled(true);
            }
        }

    }

    public class PathScanTask extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            try {
                scan();
                watcher.start();
            } catch (Exception e) {
                log.log(Level.INFO, "Problem while scanning custom paths", e);
            }
            return null;
        }

        private void scan() {
            customPaths.clear();
            customPaths.addPaths(Paths.get("custom"));
            customPaths.addPaths(settings.getTfPath().resolve("custom"));
            customPaths.validateRequired();
        }

        @Override
        protected void done() {
            customPaths.loadResourceSettings();
            loadHudComboState();
        }
    }

    public class PathCopyTask extends SwingWorker<Boolean, Void> {

        private Path from;

        public PathCopyTask(Path from) {
            this.from = from;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            status.info("Copying " + from + " into lawena custom folder...");
            return files.copyToCustom(from);
        }

        @Override
        protected void done() {
            boolean result = false;
            try {
                result = get();
            } catch (CancellationException | InterruptedException | ExecutionException e) {
                log.log(Level.FINE, "Custom path copy task was cancelled", e);
            }
            if (!result) {
                try {
                    customPaths.addPath(from);
                    log.info(from + " added to custom resource list");
                } catch (IOException e) {
                    log.log(Level.FINE, "Problem while loading a custom path", e);
                }
            } else {
                log.info(from + " copied to custom resource folder");
            }
            status.info(from.getFileName() + " was added"
                + (result ? " to lawena custom folder" : " to custom resource list"));
        }

    }

    public class SkyboxPreviewTask extends SwingWorker<Map<String, ImageIcon>, Void> {

        private List<String> data;

        public SkyboxPreviewTask(List<String> data) {
            this.data = data;
        }

        @Override
        protected Map<String, ImageIcon> doInBackground() throws Exception {
            setCurrentWorker(this, false);
            setProgress(0);
            final Map<String, ImageIcon> map = new HashMap<>();
            try {
                int i = 1;
                for (String skybox : data) {
                    setProgress((int) (100 * ((double) i / data.size())));
                    status.fine("Generating skybox preview: " + skybox);
                    String img = "skybox" + File.separator + skybox + "up.png";
                    if (!Files.exists(Paths.get(img))) {
                        String filename = skybox + "up.vtf";
                        cl.generatePreview(filename);
                    }
                    ImageIcon icon = createPreviewIcon(img);
                    map.put(skybox, icon);
                    i++;
                }
            } catch (Exception e) {
                log.log(Level.INFO, "Problem while loading skyboxes", e);
            }
            return map;
        }

        @Override
        protected void done() {
            try {
                skyboxMap.putAll(get());
                selectSkyboxFromSettings();
                log.fine("Skybox loading and preview generation complete");
            } catch (CancellationException | InterruptedException | ExecutionException e) {
                log.info("Skybox preview generator task was cancelled");
            }
            status.info("");
            if (!isCancelled()) {
                setCurrentWorker(null, false);
            }
        }

    }

    public class Tf2FolderChange implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (startTfTask == null) {
                Path newpath = getChosenTfPath();
                if (newpath != null) {
                    settings.setTfPath(newpath);
                    new PathScanTask().execute();
                    JOptionPane.showMessageDialog(view, String.format("New TF2 folder: %s", newpath),
                        "Change TF2 Folder", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(view, "Please wait until TF2 has stopped running");
            }
        }

    }

    public class HlaePathChange implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (startTfTask == null) {
                Path newpath = getChosenHlaePath();
                if (newpath != null) {
                    settings.setString(Key.HlaePath, newpath.toAbsolutePath().toString());
                    JOptionPane.showMessageDialog(view, String.format("HLAE executable: %s", newpath),
                        "Change HLAE Executable Location", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(view, "Please wait until TF2 has stopped running");
            }
        }

    }

}
