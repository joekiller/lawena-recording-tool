package com.github.lawena.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.github.lawena.app.task.LinkRunner;

public class AppView extends JFrame {

  private static final long serialVersionUID = 1L;

  private static final String renderingTutorialURL =
      "http://code.google.com/p/lawenarecordingtool/wiki/RenderingTutorial";
  private static final String releasesURL =
      "https://github.com/iabarca/lawena-recording-tool/releases";
  private static final String projectPageURL = "http://code.google.com/p/lawenarecordingtool/";
  private static final String vdmTutorialURL =
      "http://code.google.com/p/lawenarecordingtool/wiki/VDMtutorial";
  private static final String instructionsURL =
      "http://code.google.com/p/lawenarecordingtool/wiki/Instructions";

  private static class MntmRenderingTutorialActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(renderingTutorialURL).execute();
    }
  }

  private static class MntmPatchNotesActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(releasesURL).execute();
    }
  }

  private static class MntmProjectPageActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(projectPageURL).execute();
    }
  }

  private static class MntmVdmTutorialActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(vdmTutorialURL).execute();
    }
  }

  private static class MntmInstructionsActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(instructionsURL).execute();
    }
  }

  private JComboBox<String> cmbHud;
  private JComboBox<String> cmbQuality;
  private JComboBox<String> cmbSkybox;
  private JSpinner spinnerViewmodelFov;
  private JCheckBox enableMotionBlur;
  private JCheckBox disableCrosshair;
  private JCheckBox disableCrosshairSwitch;
  private JCheckBox disableCombatText;
  private JCheckBox disableHitSounds;
  private JCheckBox disableVoiceChat;
  private JButton btnStartTf;
  private JLabel lblResolution;
  private JLabel lblFrameRate;
  private JButton btnClearMovieFolder;
  private JLabel lblStatus;
  private JComboBox<String> cmbViewmodel;
  private JProgressBar progressBar;
  private JMenuItem mntmExit;
  private JMenuItem mntmAbout;
  private JCheckBox useHudMinmode;
  private JCheckBox usePlayerModel;
  private JComboBox<String> cmbSourceVideoFormat;
  private JSpinner spinnerJpegQuality;
  private JLabel lblViewmodelFov;
  private JLabel lblJpegQuality;
  private JComboBox<String> cmbResolution;
  private JComboBox<String> cmbFramerate;
  private JTable tableCustomContent;
  private JTabbedPane tabbedPane;
  private JMenuItem mntmChangeTfDirectory;
  private JMenuItem mntmChangeMovieDirectory;
  private JMenuItem mntmSaveSettings;
  private JMenuItem mntmSelectEnhancedParticles;
  private JMenuItem mntmAddCustomSettings;
  private JMenuItem mntmOpenMovieFolder;
  private JMenuItem mntmOpenCustomFolder;
  private JMenuItem mntmChangeTfLaunch;
  private JCheckBoxMenuItem chckbxmntmInsecure;
  private JMenuItem mntmRevertToDefault;
  private JCheckBoxMenuItem chckbxmntmBackupMode;
  private JMenuItem checkForUpdatesMenuItem;
  private JMenuItem switchUpdaterBranchMenuItem;
  private JMenuItem showLogMenuItem;
  private JMenuItem customLaunchOptionsMenuItem;
  private JTable resourcesTable;

  public AppView() {
    super();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu mnFile = new JMenu(" File ");
    menuBar.add(mnFile);

    JMenuItem mntmChangeTfDirectory = new JMenuItem("Change TF2 Folder...");
    mnFile.add(mntmChangeTfDirectory);

    JMenuItem mntmChangeMovieDirectory = new JMenuItem("Change Movie Folder...");
    mnFile.add(mntmChangeMovieDirectory);

    JSeparator separator_4 = new JSeparator();
    mnFile.add(separator_4);

    JMenuItem mntmOpenMovieFolder = new JMenuItem("Open Movie Folder");
    mnFile.add(mntmOpenMovieFolder);

    JMenuItem mntmOpenCustomFolder = new JMenuItem("Open Custom Folder");
    mnFile.add(mntmOpenCustomFolder);

    JSeparator separator = new JSeparator();
    mnFile.add(separator);

    JMenuItem mntmSaveSettings = new JMenuItem("Save Settings");
    mntmSaveSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
    mnFile.add(mntmSaveSettings);

    JMenuItem mntmRevertToDefault = new JMenuItem("Revert to Default Settings");
    mnFile.add(mntmRevertToDefault);

    JSeparator separator_1 = new JSeparator();
    mnFile.add(separator_1);

    JMenuItem mntmExit = new JMenuItem("Exit");
    mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
    mnFile.add(mntmExit);

    JMenu mnAdvanced = new JMenu(" Advanced ");
    menuBar.add(mnAdvanced);

    JMenuItem mntmSelectEnhancedParticles = new JMenuItem("Enhanced Particles...");
    mnAdvanced.add(mntmSelectEnhancedParticles);

    JMenuItem mntmAddCustomSettings = new JMenuItem("Custom Settings/CVars...");
    mnAdvanced.add(mntmAddCustomSettings);

    JMenuItem customLaunchOptionsMenuItem = new JMenuItem("Custom Launch Options...");
    mnAdvanced.add(customLaunchOptionsMenuItem);

    JSeparator separator_3 = new JSeparator();
    mnAdvanced.add(separator_3);

    JCheckBoxMenuItem chckbxmntmInsecure = new JCheckBoxMenuItem("Launch using -insecure");
    mnAdvanced.add(chckbxmntmInsecure);

    JCheckBoxMenuItem chckbxmntmBackupMode =
        new JCheckBoxMenuItem("Delete Backup if Files were Restored");
    mnAdvanced.add(chckbxmntmBackupMode);

    JSeparator separator_5 = new JSeparator();
    mnAdvanced.add(separator_5);

    JMenuItem mntmChangeTfLaunch = new JMenuItem("Change TF2 Launch Timeout...");
    mnAdvanced.add(mntmChangeTfLaunch);

    JMenu mnHelp = new JMenu(" Help ");
    menuBar.add(mnHelp);

    JMenuItem showLogMenuItem = new JMenuItem("Show Log");
    showLogMenuItem.setIcon(new ImageIcon(AppView.class
        .getResource("/com/github/lawena/ui/fugue/clock.png")));
    mnHelp.add(showLogMenuItem);

    JSeparator separator_7 = new JSeparator();
    mnHelp.add(separator_7);

    JMenuItem mntmInstructions = new JMenuItem("Instructions and FAQ");
    mntmInstructions.addActionListener(new MntmInstructionsActionListener());
    mnHelp.add(mntmInstructions);

    JMenuItem mntmVdmTutorial = new JMenuItem("VDM Tutorial");
    mntmVdmTutorial.addActionListener(new MntmVdmTutorialActionListener());
    mnHelp.add(mntmVdmTutorial);

    JMenuItem mntmRenderingTutorial = new JMenuItem("Rendering Tutorial");
    mntmRenderingTutorial.addActionListener(new MntmRenderingTutorialActionListener());
    mnHelp.add(mntmRenderingTutorial);

    JSeparator separator_2 = new JSeparator();
    mnHelp.add(separator_2);

    JMenuItem checkForUpdatesMenuItem = new JMenuItem("Check for Updates");
    mnHelp.add(checkForUpdatesMenuItem);

    JMenuItem switchUpdaterBranchMenuItem = new JMenuItem("Switch Updater Branch...");
    mnHelp.add(switchUpdaterBranchMenuItem);

    JMenuItem mntmPatchNotes = new JMenuItem("Release Notes");
    mntmPatchNotes.addActionListener(new MntmPatchNotesActionListener());
    mnHelp.add(mntmPatchNotes);

    JMenuItem mntmProjectPage = new JMenuItem("Project Page");
    mntmProjectPage.addActionListener(new MntmProjectPageActionListener());
    mnHelp.add(mntmProjectPage);

    JSeparator separator_6 = new JSeparator();
    mnHelp.add(separator_6);

    JMenuItem mntmAbout = new JMenuItem("About");
    mnHelp.add(mntmAbout);
    JPanel contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(5, 5));
    contentPane.setPreferredSize(new Dimension(650, 400));
    setContentPane(contentPane);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    contentPane.add(tabbedPane, BorderLayout.CENTER);

    JPanel panelSettings = new JPanel();
    tabbedPane.addTab("Settings", null, panelSettings, null);
    GridBagLayout gbl_panelSettings = new GridBagLayout();
    gbl_panelSettings.columnWidths = new int[] {0, 1, 1, 1, 0, 1, 1, 0};
    gbl_panelSettings.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    gbl_panelSettings.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 10.0};
    gbl_panelSettings.rowWeights =
        new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
    panelSettings.setLayout(gbl_panelSettings);

    JLabel lblResolution = new JLabel("Resolution:");
    GridBagConstraints gbc_lblResolution = new GridBagConstraints();
    gbc_lblResolution.insets = new Insets(5, 5, 5, 5);
    gbc_lblResolution.anchor = GridBagConstraints.EAST;
    gbc_lblResolution.gridx = 0;
    gbc_lblResolution.gridy = 0;
    panelSettings.add(lblResolution, gbc_lblResolution);

    JComboBox<String> cmbResolution = new JComboBox<String>();
    cmbResolution
        .setToolTipText("<html>Set the resolution for TF2, you can choose<br>an option or input a custom one.");
    cmbResolution.setModel(new DefaultComboBoxModel<>(new String[] {"640x360", "854x480",
        "960x540", "1024x576", "1280x720", "1366x768", "1600x900", "1920x1080", "2048x1152",
        "2560x1440"}));
    cmbResolution.setEditable(true);
    GridBagConstraints gbc_cmbResolution = new GridBagConstraints();
    gbc_cmbResolution.gridwidth = 3;
    gbc_cmbResolution.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbResolution.insets = new Insets(5, 0, 5, 5);
    gbc_cmbResolution.gridx = 1;
    gbc_cmbResolution.gridy = 0;
    panelSettings.add(cmbResolution, gbc_cmbResolution);

    JLabel lblFrameRate = new JLabel("FPS:");
    GridBagConstraints gbc_lblFrameRate = new GridBagConstraints();
    gbc_lblFrameRate.anchor = GridBagConstraints.EAST;
    gbc_lblFrameRate.insets = new Insets(5, 0, 5, 5);
    gbc_lblFrameRate.gridx = 4;
    gbc_lblFrameRate.gridy = 0;
    panelSettings.add(lblFrameRate, gbc_lblFrameRate);

    JComboBox<String> cmbFramerate = new JComboBox<>();
    cmbFramerate
        .setToolTipText("<html>Set the frames per second of the recording. This value can be<br>changed in-game with the up and down arrow keys. You can<br>also set a custom FPS value here.");
    cmbFramerate.setModel(new DefaultComboBoxModel<>(new String[] {"60", "120", "240", "480",
        "960", "1920", "3840"}));
    cmbFramerate.setEditable(true);
    GridBagConstraints gbc_cmbFramerate = new GridBagConstraints();
    gbc_cmbFramerate.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbFramerate.insets = new Insets(5, 0, 5, 5);
    gbc_cmbFramerate.gridx = 5;
    gbc_cmbFramerate.gridy = 0;
    panelSettings.add(cmbFramerate, gbc_cmbFramerate);

    JPanel panelCustomContent = new JPanel();
    GridBagConstraints gbc_panelCustomContent = new GridBagConstraints();
    gbc_panelCustomContent.insets = new Insets(5, 0, 3, 0);
    gbc_panelCustomContent.gridwidth = 2;
    gbc_panelCustomContent.gridheight = 10;
    gbc_panelCustomContent.fill = GridBagConstraints.BOTH;
    gbc_panelCustomContent.gridx = 6;
    gbc_panelCustomContent.gridy = 0;
    panelSettings.add(panelCustomContent, gbc_panelCustomContent);
    GridBagLayout gbl_panelCustomContent = new GridBagLayout();
    gbl_panelCustomContent.columnWidths = new int[] {0, 0};
    gbl_panelCustomContent.rowHeights = new int[] {0, 0};
    gbl_panelCustomContent.columnWeights = new double[] {1.0, Double.MIN_VALUE};
    gbl_panelCustomContent.rowWeights = new double[] {1.0, Double.MIN_VALUE};
    panelCustomContent.setLayout(gbl_panelCustomContent);

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setToolTipText("Copy folders to lawena/custom folder to make them appear here");
    GridBagConstraints gbc_scrollPane = new GridBagConstraints();
    gbc_scrollPane.fill = GridBagConstraints.BOTH;
    gbc_scrollPane.gridx = 0;
    gbc_scrollPane.gridy = 0;
    panelCustomContent.add(scrollPane, gbc_scrollPane);

    JTable tableCustomContent = new JTable();
    tableCustomContent.setShowVerticalLines(false);
    tableCustomContent.setGridColor(new Color(0, 0, 0, 30));
    tableCustomContent.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    tableCustomContent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tableCustomContent.getTableHeader().setReorderingAllowed(false);
    scrollPane.setViewportView(tableCustomContent);

    JLabel lblHud = new JLabel("HUD:");
    GridBagConstraints gbc_lblHud = new GridBagConstraints();
    gbc_lblHud.anchor = GridBagConstraints.EAST;
    gbc_lblHud.insets = new Insets(0, 5, 5, 5);
    gbc_lblHud.gridx = 0;
    gbc_lblHud.gridy = 1;
    panelSettings.add(lblHud, gbc_lblHud);

    JComboBox<String> cmbHud = new JComboBox<>();
    cmbHud
        .setToolTipText("<html>Select your preferred HUD here. You can also you use a custom HUD,<br>in which case you should put the HUD folder into lawena/custom folder<br>and then mark it in the Custom Resources sidebar.");
    cmbHud.setModel(new DefaultComboBoxModel<String>(new String[] {"Kill notices only",
        "Medic (hp & ubercharge)", "Default", "Custom"}));
    GridBagConstraints gbc_cmbHud = new GridBagConstraints();
    gbc_cmbHud.gridwidth = 3;
    gbc_cmbHud.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbHud.insets = new Insets(0, 0, 5, 5);
    gbc_cmbHud.gridx = 1;
    gbc_cmbHud.gridy = 1;
    panelSettings.add(cmbHud, gbc_cmbHud);

    JLabel lblDxLevel = new JLabel("DxLevel:");
    GridBagConstraints gbc_lblDxLevel = new GridBagConstraints();
    gbc_lblDxLevel.anchor = GridBagConstraints.EAST;
    gbc_lblDxLevel.insets = new Insets(0, 5, 5, 5);
    gbc_lblDxLevel.gridx = 4;
    gbc_lblDxLevel.gridy = 1;
    panelSettings.add(lblDxLevel, gbc_lblDxLevel);

    JComboBox<String> cmbQuality = new JComboBox<>();
    cmbQuality.setModel(new DefaultComboBoxModel<>(new String[] {"80 (Lowest)", "81 (Low)",
        "90 (Medium)", "95 (High)", "98 (Highest)"}));
    GridBagConstraints gbc_cmbQuality = new GridBagConstraints();
    gbc_cmbQuality.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbQuality.insets = new Insets(0, 0, 5, 5);
    gbc_cmbQuality.gridx = 5;
    gbc_cmbQuality.gridy = 1;
    panelSettings.add(cmbQuality, gbc_cmbQuality);

    JLabel lblSkybox = new JLabel("Skybox:");
    GridBagConstraints gbc_lblSkybox = new GridBagConstraints();
    gbc_lblSkybox.anchor = GridBagConstraints.EAST;
    gbc_lblSkybox.insets = new Insets(0, 5, 5, 5);
    gbc_lblSkybox.gridx = 0;
    gbc_lblSkybox.gridy = 2;
    panelSettings.add(lblSkybox, gbc_lblSkybox);

    JComboBox<String> cmbSkybox = new JComboBox<>();
    GridBagConstraints gbc_cmbSkybox = new GridBagConstraints();
    gbc_cmbSkybox.gridwidth = 5;
    gbc_cmbSkybox.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbSkybox.insets = new Insets(0, 0, 5, 5);
    gbc_cmbSkybox.gridx = 1;
    gbc_cmbSkybox.gridy = 2;
    panelSettings.add(cmbSkybox, gbc_cmbSkybox);

    JLabel lblViewmodels = new JLabel("Viewmodels:");
    GridBagConstraints gbc_lblViewmodels = new GridBagConstraints();
    gbc_lblViewmodels.anchor = GridBagConstraints.EAST;
    gbc_lblViewmodels.insets = new Insets(0, 5, 5, 5);
    gbc_lblViewmodels.gridx = 0;
    gbc_lblViewmodels.gridy = 3;
    panelSettings.add(lblViewmodels, gbc_lblViewmodels);

    JComboBox<String> cmbViewmodel = new JComboBox<>();
    cmbViewmodel.setModel(new DefaultComboBoxModel<String>(new String[] {"On", "Off", "Default"}));
    GridBagConstraints gbc_cmbViewmodel = new GridBagConstraints();
    gbc_cmbViewmodel.insets = new Insets(0, 0, 5, 5);
    gbc_cmbViewmodel.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbViewmodel.gridx = 1;
    gbc_cmbViewmodel.gridy = 3;
    panelSettings.add(cmbViewmodel, gbc_cmbViewmodel);

    JLabel lblViewmodelFov = new JLabel("Vmodel FOV:");
    GridBagConstraints gbc_lblViewmodelFov = new GridBagConstraints();
    gbc_lblViewmodelFov.anchor = GridBagConstraints.EAST;
    gbc_lblViewmodelFov.insets = new Insets(0, 5, 5, 5);
    gbc_lblViewmodelFov.gridx = 2;
    gbc_lblViewmodelFov.gridy = 3;
    panelSettings.add(lblViewmodelFov, gbc_lblViewmodelFov);

    JSpinner spinnerViewmodelFov = new JSpinner();
    GridBagConstraints gbc_spinnerViewmodelFov = new GridBagConstraints();
    gbc_spinnerViewmodelFov.fill = GridBagConstraints.HORIZONTAL;
    gbc_spinnerViewmodelFov.insets = new Insets(0, 0, 5, 5);
    gbc_spinnerViewmodelFov.gridx = 3;
    gbc_spinnerViewmodelFov.gridy = 3;
    panelSettings.add(spinnerViewmodelFov, gbc_spinnerViewmodelFov);
    spinnerViewmodelFov.setModel(new SpinnerNumberModel(70, null, null, 1));

    Component horizontalStrut = Box.createHorizontalStrut(24);
    GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
    gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
    gbc_horizontalStrut.gridx = 4;
    gbc_horizontalStrut.gridy = 3;
    panelSettings.add(horizontalStrut, gbc_horizontalStrut);

    JLabel lblOutput = new JLabel("Frame Output:");
    GridBagConstraints gbc_lblOutput = new GridBagConstraints();
    gbc_lblOutput.anchor = GridBagConstraints.EAST;
    gbc_lblOutput.insets = new Insets(0, 5, 5, 5);
    gbc_lblOutput.gridx = 0;
    gbc_lblOutput.gridy = 4;
    panelSettings.add(lblOutput, gbc_lblOutput);

    JComboBox<String> cmbSourceVideoFormat = new JComboBox<>();
    cmbSourceVideoFormat.setModel(new DefaultComboBoxModel<>(new String[] {"TGA", "JPG"}));
    GridBagConstraints gbc_cmbSourceVideoFormat = new GridBagConstraints();
    gbc_cmbSourceVideoFormat.insets = new Insets(0, 0, 5, 5);
    gbc_cmbSourceVideoFormat.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbSourceVideoFormat.gridx = 1;
    gbc_cmbSourceVideoFormat.gridy = 4;
    panelSettings.add(cmbSourceVideoFormat, gbc_cmbSourceVideoFormat);

    JLabel lblJpegQuality = new JLabel("JPG Quality:");
    GridBagConstraints gbc_lblJpegQuality = new GridBagConstraints();
    gbc_lblJpegQuality.anchor = GridBagConstraints.EAST;
    gbc_lblJpegQuality.insets = new Insets(0, 0, 5, 5);
    gbc_lblJpegQuality.gridx = 2;
    gbc_lblJpegQuality.gridy = 4;
    panelSettings.add(lblJpegQuality, gbc_lblJpegQuality);

    JSpinner spinnerJpegQuality = new JSpinner();
    spinnerJpegQuality.setModel(new SpinnerNumberModel(50, 1, 100, 1));
    GridBagConstraints gbc_spinnerJpegQuality = new GridBagConstraints();
    gbc_spinnerJpegQuality.fill = GridBagConstraints.HORIZONTAL;
    gbc_spinnerJpegQuality.insets = new Insets(0, 0, 5, 5);
    gbc_spinnerJpegQuality.gridx = 3;
    gbc_spinnerJpegQuality.gridy = 4;
    panelSettings.add(spinnerJpegQuality, gbc_spinnerJpegQuality);

    Component verticalStrut = Box.createVerticalStrut(22);
    GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
    gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
    gbc_verticalStrut.gridx = 0;
    gbc_verticalStrut.gridy = 5;
    panelSettings.add(verticalStrut, gbc_verticalStrut);

    JPanel panelCheckboxes = new JPanel();
    panelCheckboxes.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
        "Additional Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    GridBagConstraints gbc_panelCheckboxes = new GridBagConstraints();
    gbc_panelCheckboxes.insets = new Insets(0, 0, 0, 5);
    gbc_panelCheckboxes.gridheight = 4;
    gbc_panelCheckboxes.gridwidth = 6;
    gbc_panelCheckboxes.fill = GridBagConstraints.BOTH;
    gbc_panelCheckboxes.gridx = 0;
    gbc_panelCheckboxes.gridy = 6;
    panelSettings.add(panelCheckboxes, gbc_panelCheckboxes);
    GridBagLayout gbl_panelCheckboxes = new GridBagLayout();
    gbl_panelCheckboxes.columnWidths = new int[] {0, 0, 0};
    gbl_panelCheckboxes.rowHeights = new int[] {0, 0, 0, 0, 0};
    gbl_panelCheckboxes.columnWeights = new double[] {1.0, 1.0, Double.MIN_VALUE};
    gbl_panelCheckboxes.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    panelCheckboxes.setLayout(gbl_panelCheckboxes);

    JCheckBox enableMotionBlur = new JCheckBox("Enable Motion Blur");
    GridBagConstraints gbc_enableMotionBlur = new GridBagConstraints();
    gbc_enableMotionBlur.insets = new Insets(0, 0, 0, 5);
    gbc_enableMotionBlur.anchor = GridBagConstraints.WEST;
    gbc_enableMotionBlur.gridx = 0;
    gbc_enableMotionBlur.gridy = 0;
    panelCheckboxes.add(enableMotionBlur, gbc_enableMotionBlur);

    JCheckBox disableCombatText = new JCheckBox("Disable Combat Text");
    GridBagConstraints gbc_disableCombatText = new GridBagConstraints();
    gbc_disableCombatText.anchor = GridBagConstraints.WEST;
    gbc_disableCombatText.gridx = 1;
    gbc_disableCombatText.gridy = 0;
    panelCheckboxes.add(disableCombatText, gbc_disableCombatText);

    JCheckBox disableHitSounds = new JCheckBox("Disable Hit Sounds");
    GridBagConstraints gbc_disableHitSounds = new GridBagConstraints();
    gbc_disableHitSounds.insets = new Insets(0, 0, 0, 5);
    gbc_disableHitSounds.anchor = GridBagConstraints.WEST;
    gbc_disableHitSounds.gridx = 0;
    gbc_disableHitSounds.gridy = 1;
    panelCheckboxes.add(disableHitSounds, gbc_disableHitSounds);

    JCheckBox disableCrosshair = new JCheckBox("Disable Crosshair");
    GridBagConstraints gbc_disableCrosshair = new GridBagConstraints();
    gbc_disableCrosshair.anchor = GridBagConstraints.WEST;
    gbc_disableCrosshair.gridx = 1;
    gbc_disableCrosshair.gridy = 1;
    panelCheckboxes.add(disableCrosshair, gbc_disableCrosshair);

    JCheckBox disableVoiceChat = new JCheckBox("Disable Voice Chat");
    GridBagConstraints gbc_disableVoiceChat = new GridBagConstraints();
    gbc_disableVoiceChat.insets = new Insets(0, 0, 0, 5);
    gbc_disableVoiceChat.anchor = GridBagConstraints.WEST;
    gbc_disableVoiceChat.gridx = 0;
    gbc_disableVoiceChat.gridy = 2;
    panelCheckboxes.add(disableVoiceChat, gbc_disableVoiceChat);

    JCheckBox disableCrosshairSwitch = new JCheckBox("Disable Crosshair Switching");
    GridBagConstraints gbc_disableCrosshairSwitch = new GridBagConstraints();
    gbc_disableCrosshairSwitch.anchor = GridBagConstraints.WEST;
    gbc_disableCrosshairSwitch.gridx = 1;
    gbc_disableCrosshairSwitch.gridy = 2;
    panelCheckboxes.add(disableCrosshairSwitch, gbc_disableCrosshairSwitch);

    JCheckBox useHudMinmode = new JCheckBox("Minimal HUD");
    useHudMinmode
        .setToolTipText("<html>The minmode version of a HUD primarily reduces the size of the<br>health and ammo displays, moving them closer to the centre of<br>the screen. Ticking this option will add \"cl_hud_minmode 1\" to<br>the config.");
    GridBagConstraints gbc_chckbxUseHudMin = new GridBagConstraints();
    gbc_chckbxUseHudMin.anchor = GridBagConstraints.WEST;
    gbc_chckbxUseHudMin.insets = new Insets(0, 0, 0, 5);
    gbc_chckbxUseHudMin.gridx = 0;
    gbc_chckbxUseHudMin.gridy = 3;
    panelCheckboxes.add(useHudMinmode, gbc_chckbxUseHudMin);

    JCheckBox usePlayerModel = new JCheckBox("3D Player Model in HUD");
    usePlayerModel
        .setToolTipText("<html>\r\nUse player model in player class HUD, selecting this option<br>\r\nwill add \"cl_hud_playerclass_use_playermodel 1\" to the config.<br>\r\nThis option is useful if you use Custom HUDs.");
    GridBagConstraints gbc_usePlayerModel = new GridBagConstraints();
    gbc_usePlayerModel.anchor = GridBagConstraints.WEST;
    gbc_usePlayerModel.gridx = 1;
    gbc_usePlayerModel.gridy = 3;
    panelCheckboxes.add(usePlayerModel, gbc_usePlayerModel);

    JPanel panelBottomLeft = new JPanel();
    FlowLayout fl_panelBottomLeft = (FlowLayout) panelBottomLeft.getLayout();
    fl_panelBottomLeft.setVgap(0);
    fl_panelBottomLeft.setHgap(0);
    GridBagConstraints gbc_panelBottomLeft = new GridBagConstraints();
    gbc_panelBottomLeft.anchor = GridBagConstraints.WEST;
    gbc_panelBottomLeft.gridwidth = 6;
    gbc_panelBottomLeft.fill = GridBagConstraints.VERTICAL;
    gbc_panelBottomLeft.gridx = 0;
    gbc_panelBottomLeft.gridy = 10;
    panelSettings.add(panelBottomLeft, gbc_panelBottomLeft);

    JButton btnClearMovieFolder = new JButton("Delete Movie Segments...");
    panelBottomLeft.add(btnClearMovieFolder);

    JPanel panelBottomRight = new JPanel();
    FlowLayout fl_panelBottomRight = (FlowLayout) panelBottomRight.getLayout();
    fl_panelBottomRight.setVgap(0);
    fl_panelBottomRight.setHgap(0);
    GridBagConstraints gbc_panelBottomRight = new GridBagConstraints();
    gbc_panelBottomRight.gridwidth = 2;
    gbc_panelBottomRight.anchor = GridBagConstraints.EAST;
    gbc_panelBottomRight.fill = GridBagConstraints.VERTICAL;
    gbc_panelBottomRight.gridx = 6;
    gbc_panelBottomRight.gridy = 10;
    panelSettings.add(panelBottomRight, gbc_panelBottomRight);

    JButton btnStartTf = new JButton("Start Team Fortress 2");
    panelBottomRight.add(btnStartTf);

    JPanel panelStatusbar = new JPanel();
    contentPane.add(panelStatusbar, BorderLayout.SOUTH);
    GridBagLayout gbl_panelStatusbar = new GridBagLayout();
    gbl_panelStatusbar.columnWidths = new int[] {31, 0, 0, 0};
    gbl_panelStatusbar.rowHeights = new int[] {12, 0};
    gbl_panelStatusbar.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
    gbl_panelStatusbar.rowWeights = new double[] {0.0, Double.MIN_VALUE};
    panelStatusbar.setLayout(gbl_panelStatusbar);

    JLabel lblStatus = new JLabel("");
    GridBagConstraints gbc_lblStatus = new GridBagConstraints();
    gbc_lblStatus.insets = new Insets(0, 5, 0, 5);
    gbc_lblStatus.anchor = GridBagConstraints.NORTHWEST;
    gbc_lblStatus.gridx = 0;
    gbc_lblStatus.gridy = 0;
    panelStatusbar.add(lblStatus, gbc_lblStatus);

    JProgressBar progressBar = new JProgressBar();
    progressBar.setVisible(false);
    GridBagConstraints gbc_progressBar = new GridBagConstraints();
    gbc_progressBar.anchor = GridBagConstraints.EAST;
    gbc_progressBar.gridx = 2;
    gbc_progressBar.gridy = 0;
    panelStatusbar.add(progressBar, gbc_progressBar);

    this.cmbResolution = cmbResolution;
    this.cmbSkybox = cmbSkybox;
    this.cmbHud = cmbHud;
    this.cmbFramerate = cmbFramerate;
    this.cmbQuality = cmbQuality;
    this.spinnerViewmodelFov = spinnerViewmodelFov;
    this.enableMotionBlur = enableMotionBlur;
    this.disableCrosshair = disableCrosshair;
    this.disableCrosshairSwitch = disableCrosshairSwitch;
    this.disableCombatText = disableCombatText;
    this.disableHitSounds = disableHitSounds;
    this.disableVoiceChat = disableVoiceChat;
    this.btnStartTf = btnStartTf;
    this.lblResolution = lblResolution;
    this.lblFrameRate = lblFrameRate;
    this.btnClearMovieFolder = btnClearMovieFolder;
    this.tableCustomContent = tableCustomContent;
    this.tabbedPane = tabbedPane;
    this.mntmChangeTfDirectory = mntmChangeTfDirectory;
    this.mntmChangeMovieDirectory = mntmChangeMovieDirectory;
    this.lblStatus = lblStatus;
    this.cmbViewmodel = cmbViewmodel;
    this.progressBar = progressBar;
    this.mntmRevertToDefault = mntmRevertToDefault;
    this.mntmExit = mntmExit;
    this.mntmAbout = mntmAbout;
    this.mntmSaveSettings = mntmSaveSettings;
    this.useHudMinmode = useHudMinmode;
    this.mntmSelectEnhancedParticles = mntmSelectEnhancedParticles;
    this.mntmAddCustomSettings = mntmAddCustomSettings;
    this.mntmOpenMovieFolder = mntmOpenMovieFolder;
    this.mntmOpenCustomFolder = mntmOpenCustomFolder;
    this.mntmChangeTfLaunch = mntmChangeTfLaunch;
    this.chckbxmntmInsecure = chckbxmntmInsecure;
    this.usePlayerModel = usePlayerModel;
    this.cmbSourceVideoFormat = cmbSourceVideoFormat;
    this.spinnerJpegQuality = spinnerJpegQuality;
    this.lblViewmodelFov = lblViewmodelFov;
    this.lblJpegQuality = lblJpegQuality;

    JPanel mainPanel = new JPanel();
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    tabbedPane.addTab("Lawena Settings", null, mainPanel, null);
    mainPanel.setLayout(new BorderLayout(5, 5));

    JPanel rightPanel = new JPanel();
    mainPanel.add(rightPanel, BorderLayout.EAST);
    GridBagLayout gbl_rightPanel = new GridBagLayout();
    gbl_rightPanel.columnWidths = new int[] {0, 0, 0};
    gbl_rightPanel.rowHeights = new int[] {0, 0, 0, 0};
    gbl_rightPanel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
    gbl_rightPanel.rowWeights = new double[] {0.0, 0.0, 1.0, Double.MIN_VALUE};
    rightPanel.setLayout(gbl_rightPanel);

    JLabel hudLabel = new JLabel("HUD:");
    GridBagConstraints gbc_hudLabel = new GridBagConstraints();
    gbc_hudLabel.anchor = GridBagConstraints.EAST;
    gbc_hudLabel.insets = new Insets(0, 0, 5, 5);
    gbc_hudLabel.gridx = 0;
    gbc_hudLabel.gridy = 0;
    rightPanel.add(hudLabel, gbc_hudLabel);

    JComboBox<String> hudComboBox = new JComboBox<>();
    GridBagConstraints gbc_hudComboBox = new GridBagConstraints();
    gbc_hudComboBox.insets = new Insets(0, 0, 5, 0);
    gbc_hudComboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_hudComboBox.gridx = 1;
    gbc_hudComboBox.gridy = 0;
    rightPanel.add(hudComboBox, gbc_hudComboBox);

    JLabel skyboxLabel = new JLabel("Skybox:");
    GridBagConstraints gbc_skyboxLabel = new GridBagConstraints();
    gbc_skyboxLabel.anchor = GridBagConstraints.EAST;
    gbc_skyboxLabel.insets = new Insets(0, 0, 5, 5);
    gbc_skyboxLabel.gridx = 0;
    gbc_skyboxLabel.gridy = 1;
    rightPanel.add(skyboxLabel, gbc_skyboxLabel);

    JComboBox<String> skyboxComboBox = new JComboBox<>();
    GridBagConstraints gbc_skyboxComboBox = new GridBagConstraints();
    gbc_skyboxComboBox.insets = new Insets(0, 0, 5, 0);
    gbc_skyboxComboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_skyboxComboBox.gridx = 1;
    gbc_skyboxComboBox.gridy = 1;
    rightPanel.add(skyboxComboBox, gbc_skyboxComboBox);

    JScrollPane resourcesTableScrollPane = new JScrollPane();
    GridBagConstraints gbc_resourcesTableScrollPane = new GridBagConstraints();
    gbc_resourcesTableScrollPane.gridwidth = 2;
    gbc_resourcesTableScrollPane.fill = GridBagConstraints.BOTH;
    gbc_resourcesTableScrollPane.gridx = 0;
    gbc_resourcesTableScrollPane.gridy = 2;
    rightPanel.add(resourcesTableScrollPane, gbc_resourcesTableScrollPane);

    resourcesTable = new JTable();
    resourcesTable.setPreferredScrollableViewportSize(new Dimension(250, 0));
    resourcesTableScrollPane.setViewportView(resourcesTable);

    JPanel centerPanel = new JPanel();
    mainPanel.add(centerPanel, BorderLayout.CENTER);
    GridBagLayout gbl_centerPanel = new GridBagLayout();
    gbl_centerPanel.columnWidths = new int[] {0, 120, 0, 0, 0};
    gbl_centerPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
    gbl_centerPanel.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
    gbl_centerPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    centerPanel.setLayout(gbl_centerPanel);

    JLabel resolutionLabel = new JLabel("Resolution:");
    GridBagConstraints gbc_resolutionLabel = new GridBagConstraints();
    gbc_resolutionLabel.anchor = GridBagConstraints.EAST;
    gbc_resolutionLabel.insets = new Insets(0, 0, 5, 5);
    gbc_resolutionLabel.gridx = 0;
    gbc_resolutionLabel.gridy = 0;
    centerPanel.add(resolutionLabel, gbc_resolutionLabel);

    JComboBox<String> resolutionComboBox = new JComboBox<>();
    resolutionComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"1920x1080"}));
    GridBagConstraints gbc_resolutionComboBox = new GridBagConstraints();
    gbc_resolutionComboBox.insets = new Insets(0, 0, 5, 5);
    gbc_resolutionComboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_resolutionComboBox.gridx = 1;
    gbc_resolutionComboBox.gridy = 0;
    centerPanel.add(resolutionComboBox, gbc_resolutionComboBox);

    JLabel dxlevelLabel = new JLabel("DxLevel:");
    GridBagConstraints gbc_dxlevelLabel = new GridBagConstraints();
    gbc_dxlevelLabel.insets = new Insets(0, 0, 5, 5);
    gbc_dxlevelLabel.anchor = GridBagConstraints.EAST;
    gbc_dxlevelLabel.gridx = 2;
    gbc_dxlevelLabel.gridy = 0;
    centerPanel.add(dxlevelLabel, gbc_dxlevelLabel);

    JComboBox<String> dxlevelComboBox = new JComboBox<>();
    dxlevelComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"98", "95", "90", "81",
        "80"}));
    GridBagConstraints gbc_dxlevelComboBox = new GridBagConstraints();
    gbc_dxlevelComboBox.insets = new Insets(0, 0, 5, 0);
    gbc_dxlevelComboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_dxlevelComboBox.gridx = 3;
    gbc_dxlevelComboBox.gridy = 0;
    centerPanel.add(dxlevelComboBox, gbc_dxlevelComboBox);

    JLabel fpsLabel = new JLabel("Recording FPS:");
    GridBagConstraints gbc_fpsLabel = new GridBagConstraints();
    gbc_fpsLabel.anchor = GridBagConstraints.EAST;
    gbc_fpsLabel.insets = new Insets(0, 0, 5, 5);
    gbc_fpsLabel.gridx = 0;
    gbc_fpsLabel.gridy = 1;
    centerPanel.add(fpsLabel, gbc_fpsLabel);

    JComboBox<String> fpsComboBox = new JComboBox<>();
    GridBagConstraints gbc_fpsComboBox = new GridBagConstraints();
    gbc_fpsComboBox.insets = new Insets(0, 0, 5, 5);
    gbc_fpsComboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_fpsComboBox.gridx = 1;
    gbc_fpsComboBox.gridy = 1;
    centerPanel.add(fpsComboBox, gbc_fpsComboBox);

    JLabel frameOutputLabel = new JLabel("Frame Output:");
    GridBagConstraints gbc_frameOutputLabel = new GridBagConstraints();
    gbc_frameOutputLabel.anchor = GridBagConstraints.EAST;
    gbc_frameOutputLabel.insets = new Insets(0, 0, 5, 5);
    gbc_frameOutputLabel.gridx = 0;
    gbc_frameOutputLabel.gridy = 2;
    centerPanel.add(frameOutputLabel, gbc_frameOutputLabel);

    JComboBox<String> frameOutputComboBox = new JComboBox<>();
    frameOutputComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"TGA", "JPEG"}));
    GridBagConstraints gbc_frameOutputComboBox = new GridBagConstraints();
    gbc_frameOutputComboBox.insets = new Insets(0, 0, 5, 5);
    gbc_frameOutputComboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_frameOutputComboBox.gridx = 1;
    gbc_frameOutputComboBox.gridy = 2;
    centerPanel.add(frameOutputComboBox, gbc_frameOutputComboBox);

    JLabel jpegQualityLabel = new JLabel("Quality:");
    GridBagConstraints gbc_jpegQualityLabel = new GridBagConstraints();
    gbc_jpegQualityLabel.anchor = GridBagConstraints.EAST;
    gbc_jpegQualityLabel.insets = new Insets(0, 0, 5, 5);
    gbc_jpegQualityLabel.gridx = 2;
    gbc_jpegQualityLabel.gridy = 2;
    centerPanel.add(jpegQualityLabel, gbc_jpegQualityLabel);

    JSpinner jpegQualitySpinner = new JSpinner();
    GridBagConstraints gbc_jpegQualitySpinner = new GridBagConstraints();
    gbc_jpegQualitySpinner.insets = new Insets(0, 0, 5, 0);
    gbc_jpegQualitySpinner.fill = GridBagConstraints.HORIZONTAL;
    gbc_jpegQualitySpinner.gridx = 3;
    gbc_jpegQualitySpinner.gridy = 2;
    centerPanel.add(jpegQualitySpinner, gbc_jpegQualitySpinner);

    JLabel viewmodelsLabel = new JLabel("Viewmodels:");
    GridBagConstraints gbc_viewmodelsLabel = new GridBagConstraints();
    gbc_viewmodelsLabel.anchor = GridBagConstraints.EAST;
    gbc_viewmodelsLabel.insets = new Insets(0, 0, 0, 5);
    gbc_viewmodelsLabel.gridx = 0;
    gbc_viewmodelsLabel.gridy = 3;
    centerPanel.add(viewmodelsLabel, gbc_viewmodelsLabel);

    JComboBox<String> viewmodelsComboBox = new JComboBox<>();
    viewmodelsComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Force Enable",
        "Force Disable", "Keep Default"}));
    GridBagConstraints gbc_viewmodelsComboBox = new GridBagConstraints();
    gbc_viewmodelsComboBox.insets = new Insets(0, 0, 0, 5);
    gbc_viewmodelsComboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_viewmodelsComboBox.gridx = 1;
    gbc_viewmodelsComboBox.gridy = 3;
    centerPanel.add(viewmodelsComboBox, gbc_viewmodelsComboBox);

    JLabel vmodelLabel = new JLabel("Vmodel FOV:");
    GridBagConstraints gbc_vmodelLabel = new GridBagConstraints();
    gbc_vmodelLabel.insets = new Insets(0, 0, 0, 5);
    gbc_vmodelLabel.gridx = 2;
    gbc_vmodelLabel.gridy = 3;
    centerPanel.add(vmodelLabel, gbc_vmodelLabel);

    JSpinner vmodelFovSpinner = new JSpinner();
    GridBagConstraints gbc_vmodelFovSpinner = new GridBagConstraints();
    gbc_vmodelFovSpinner.fill = GridBagConstraints.HORIZONTAL;
    gbc_vmodelFovSpinner.gridx = 3;
    gbc_vmodelFovSpinner.gridy = 3;
    centerPanel.add(vmodelFovSpinner, gbc_vmodelFovSpinner);
    this.chckbxmntmBackupMode = chckbxmntmBackupMode;
    this.checkForUpdatesMenuItem = checkForUpdatesMenuItem;
    this.switchUpdaterBranchMenuItem = switchUpdaterBranchMenuItem;
    this.showLogMenuItem = showLogMenuItem;
    this.customLaunchOptionsMenuItem = customLaunchOptionsMenuItem;

    pack();
    setMinimumSize(new Dimension(750, 480));
    setLocationByPlatform(true);
  }

  public JComboBox<String> getCmbResolution() {
    return cmbResolution;
  }

  public JComboBox<String> getCmbSkybox() {
    return cmbSkybox;
  }

  public JComboBox<String> getCmbHud() {
    return cmbHud;
  }

  public JComboBox<String> getCmbFramerate() {
    return cmbFramerate;
  }

  public JComboBox<String> getCmbQuality() {
    return cmbQuality;
  }

  public JSpinner getSpinnerViewmodelFov() {
    return spinnerViewmodelFov;
  }

  public JCheckBox getEnableMotionBlur() {
    return enableMotionBlur;
  }

  public JCheckBox getDisableCrosshair() {
    return disableCrosshair;
  }

  public JCheckBox getDisableCrosshairSwitch() {
    return disableCrosshairSwitch;
  }

  public JCheckBox getDisableCombatText() {
    return disableCombatText;
  }

  public JCheckBox getDisableHitSounds() {
    return disableHitSounds;
  }

  public JCheckBox getDisableVoiceChat() {
    return disableVoiceChat;
  }

  public JButton getBtnStartTf() {
    return btnStartTf;
  }

  public JLabel getLblResolution() {
    return lblResolution;
  }

  public JLabel getLblFrameRate() {
    return lblFrameRate;
  }

  public JButton getBtnClearMovieFolder() {
    return btnClearMovieFolder;
  }

  public JTable getTableCustomContent() {
    return tableCustomContent;
  }

  public JTabbedPane getTabbedPane() {
    return tabbedPane;
  }

  public JMenuItem getMntmChangeTfDirectory() {
    return mntmChangeTfDirectory;
  }

  public JMenuItem getMntmChangeMovieDirectory() {
    return mntmChangeMovieDirectory;
  }

  public JLabel getLblStatus() {
    return lblStatus;
  }

  public JComboBox<String> getCmbViewmodel() {
    return cmbViewmodel;
  }

  public JProgressBar getProgressBar() {
    return progressBar;
  }

  public JMenuItem getMntmRevertToDefault() {
    return mntmRevertToDefault;
  }

  public JMenuItem getMntmExit() {
    return mntmExit;
  }

  public JMenuItem getMntmAbout() {
    return mntmAbout;
  }

  public JMenuItem getMntmSaveSettings() {
    return mntmSaveSettings;
  }

  public JCheckBox getUseHudMinmode() {
    return useHudMinmode;
  }

  public JMenuItem getMntmSelectEnhancedParticles() {
    return mntmSelectEnhancedParticles;
  }

  public JMenuItem getMntmAddCustomSettings() {
    return mntmAddCustomSettings;
  }

  public JMenuItem getMntmOpenMovieFolder() {
    return mntmOpenMovieFolder;
  }

  public JMenuItem getMntmOpenCustomFolder() {
    return mntmOpenCustomFolder;
  }

  public JMenuItem getMntmLaunchTimeout() {
    return mntmChangeTfLaunch;
  }

  public JCheckBoxMenuItem getChckbxmntmInsecure() {
    return chckbxmntmInsecure;
  }

  public JCheckBox getUsePlayerModel() {
    return usePlayerModel;
  }

  public JComboBox<String> getCmbSourceVideoFormat() {
    return cmbSourceVideoFormat;
  }

  public JSpinner getSpinnerJpegQuality() {
    return spinnerJpegQuality;
  }

  public JLabel getLblViewmodelFov() {
    return lblViewmodelFov;
  }

  public JLabel getLblJpegQuality() {
    return lblJpegQuality;
  }

  public JCheckBoxMenuItem getChckbxmntmBackupMode() {
    return chckbxmntmBackupMode;
  }

  public JMenuItem getCheckForUpdatesMenuItem() {
    return checkForUpdatesMenuItem;
  }

  public JMenuItem getSwitchUpdaterBranchMenuItem() {
    return switchUpdaterBranchMenuItem;
  }

  public JMenuItem getShowLogMenuItem() {
    return showLogMenuItem;
  }

  public JMenuItem getCustomLaunchOptionsMenuItem() {
    return customLaunchOptionsMenuItem;
  }

}