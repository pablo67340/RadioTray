/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radiotray;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author Bryce
 */
public class RadioConfigController implements Initializable {

    @FXML
    private ListView lstStations;

    private String currentName, currentURL;

    @FXML
    private TextField txtName, txtUrl;

    @FXML
    private Button btnSave;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lstStations.setOnMouseClicked((MouseEvent event) -> {
            setStation((String) lstStations.getSelectionModel().getSelectedItem());
        });

        RadioTray.getINSTANCE().getCurrentConfig().getStations().keySet().stream().forEach((name) -> {
            System.out.println("Adding: " + name);
            lstStations.getItems().add(name);
        }); // TODO
    }

    public void setStation(String name) {
        btnSave.setDisable(false);
        currentName = name;
        currentURL = RadioTray.getINSTANCE().getCurrentConfig().getStations().get(name);
        txtName.setText(currentName);
        txtUrl.setText(currentURL);
    }

    public void saveStation() {
        ConfigFile file = RadioTray.getINSTANCE().getCurrentConfig();
        if (file.getStations().containsKey(currentName)) {
            file.getStations().remove(currentName);
            lstStations.getItems().remove(currentName);
        }
        // If we happen to edit the station that we were listening too last, update to avoid NPE.
        if (RadioTray.getINSTANCE().getCurrentStation().equalsIgnoreCase(currentName)) {
            RadioTray.getINSTANCE().setCurrentStation(txtName.getText());
        }

        currentName = txtName.getText();
        currentURL = txtUrl.getText();
        file.getStations().put(currentName, currentURL);
        lstStations.getItems().add(currentName);
        lstStations.refresh();
        RadioTray.getINSTANCE().setCurrentConfig(file);
        RadioTray.getINSTANCE().saveConfig();

        txtName.setText("");
        txtUrl.setText("");
        btnSave.setDisable(true);
    }

    public void open() {

    }

    public void btnSaveAction(Event e) {
        saveStation();
    }

    public void btnAddAction(Event e) {
        currentName = "New" + lstStations.getItems().size();
        currentURL = "";
        lstStations.getItems().add(currentName);
    }

}
