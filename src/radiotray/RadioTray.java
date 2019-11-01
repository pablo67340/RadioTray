package radiotray;

/**
 *
 * @author Bryce
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javazoom.jl.decoder.JavaLayerException;

import javazoom.jl.player.advanced.AdvancedPlayer;

public class RadioTray {

    private final TrayIcon play = new TrayIcon(createImage("play.png", "tray icon"));

    private RadioState state = RadioState.STOPPED;

    private AdvancedPlayer player;

    private ConfigFile currentConfig;

    private final File configHolder = new File("C:/Users/" + System.getProperty("user.name") + "/RadioTray");

    private final File config = new File("C:/Users/" + System.getProperty("user.name") + "/RadioTray/conf.json");

    private String currentStation = "";

    private Preferences prefs;

    private Thread t1;

    private static RadioTray INSTANCE;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Menu displayMenu = new Menu("Stations");

    private final Map<String, CheckboxMenuItem> buttons = new HashMap<>();

    private static JEditorPane editorPane;

    public void toggleStop() {
        play.setImage(createImage("stop.png", "tray icon"));
        chooseStation(currentConfig.getStations().get(currentStation));
        t1 = new Thread(() -> {
            try {
                player.play();
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }
        });
        t1.start();
    }

    @SuppressWarnings("CallToThreadStopSuspendOrResumeManager")
    public void togglePlay() {
        play.setImage(createImage("play.png", "tray icon"));

        // Unsafe, but we must destroy that threaddy boi.
        t1.stop();
    }

    public static void main(String[] args) {
        // Objectify our wom... Object. 
        RadioTray tray = new RadioTray();
        tray.run();
    }

    // Selects a station via URL and plays the darn thing
    public void chooseStation(String url) {
        try {
            InputStream is = new URL(url).openStream();
            player = new AdvancedPlayer(is);
        } catch (IOException ex) {
            System.out.println("Exception: " + ex.getMessage());
        } catch (JavaLayerException ex) {
            Logger.getLogger(RadioTray.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Boolean configExists() {
        return config.exists();
    }

    public void loadConfig() {
        System.out.println("Loading Conf");
        try {
            Reader fReader = new FileReader(config);
            JsonReader reader = new JsonReader(fReader);
            currentConfig = gson.fromJson(reader, ConfigFile.class);
        } catch (FileNotFoundException | JsonIOException | JsonSyntaxException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void saveConfig() {
        if (!configHolder.exists()) {
            configHolder.mkdirs();
        }

        if (!configExists()) {
            currentConfig = new ConfigFile();
            try (Writer writer = new FileWriter(config)) {
                gson.toJson(currentConfig, writer);
                System.out.println("Outputting to file");
            } catch (IOException ex) {
                Logger.getLogger(RadioTray.class.getName()).log(Level.SEVERE, null, ex);
            }

            loadConfig();

        } else {

            if (currentConfig == null) {
                loadConfig();
            }

            try (Writer writer = new FileWriter(config)) {
                gson.toJson(currentConfig, writer);
            } catch (IOException ex) {
                Logger.getLogger(RadioTray.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void run() {
        INSTANCE = this;
        prefs = Preferences.userRoot().node(this.getClass().getName());
        saveConfig();

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            System.out.println("Error: " + ex.getMessage());
        }

        UIManager.put("swing.boldMetal", Boolean.FALSE);
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();

        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");

        // Stations Themselves
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem edit = new MenuItem("Edit Stations");

        edit.addActionListener((ActionEvent e) -> {
            openConfig();
        });

        popup.add(displayMenu);
        popup.addSeparator();
        //Add components to popup menu
        popup.add(aboutItem);
        popup.add(edit);

        rebuildButtons();

        play.setPopupMenu(popup);

        try {
            tray.add(play);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        editorPane = new JEditorPane();
        JOptionPane pane = new JOptionPane();

        pane.add(editorPane, BorderLayout.CENTER);
        editorPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        editorPane.setEditable(false);

        editorPane.setBackground(Color.decode("#F0F0F0"));

        editorPane.setText("<p>Developed by Bryce Wilkinson</p><br /><p>RadioTray is programmed in Java and will function on any SystemTray Compatible OS.</p><br /><p>Radio Tray will accept any stream format (mp3, AAC, etc).</p><br /><p>Support:</p><br />"
                + "<a href='https://discord.gg/v7D6pCm'>https://discord.gg/v7D6pCm</a>");

        editorPane.addHyperlinkListener((HyperlinkEvent e) -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        editorPane.setToolTipText("if you click on <b>that link you go to     the stack");

        aboutItem.addActionListener((ActionEvent e) -> {
            pane.show();
            JOptionPane.showMessageDialog(null, editorPane, "About", JOptionPane.PLAIN_MESSAGE);
        });

        play.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {

                    if (state == RadioState.PLAYING) {
                        togglePlay();
                        state = RadioState.STOPPED;
                    } else {
                        toggleStop();
                        state = RadioState.PLAYING;
                    }

                    System.out.println("Play");

                }
            }
        });

        exitItem.addActionListener((ActionEvent e) -> {
            tray.remove(play);
            System.exit(0);
        });

    }

    public void rebuildButtons() {
        displayMenu.removeAll();
        for (Entry<String, String> entry : currentConfig.getStations().entrySet()) {
            CheckboxMenuItem item = new CheckboxMenuItem(entry.getKey());

            if (!prefs.get("currentStation", "").equalsIgnoreCase("")) {
                System.out.println("Item: " + item.getLabel() + " prefs: " + prefs.get("currentStation", ""));
                if (item.getLabel().equalsIgnoreCase(prefs.get("currentStation", ""))) {
                    currentStation = prefs.get("currentStation", "");
                    System.out.println("Setting Current Station to: " + currentStation);
                    item.setState(true);
                }
            }

            item.addItemListener((ItemEvent e) -> {
                buttons.get(currentStation).setState(false);
                currentStation = item.getLabel();
                prefs.put("currentStation", currentStation);

            });

            buttons.put(item.getLabel(), item);
        }

        // Add the stations to the catagory
        buttons.values().stream().forEach((item) -> {
            displayMenu.add(item);
        });

    }

    //Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = RadioTray.class
                .getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    private void openConfig() {
        RadioConfig configUtil = new RadioConfig();
        configUtil.main(new String[0]);
    }

    public static RadioTray getINSTANCE() {
        return INSTANCE;
    }

    public ConfigFile getCurrentConfig() {
        return this.currentConfig;
    }

    public void setCurrentConfig(ConfigFile input) {
        this.currentConfig = input;
    }

    public String getCurrentStation() {
        return this.currentStation;
    }

    public void setCurrentStation(String input) {
        prefs.put("currentStation", input);
    }
}
