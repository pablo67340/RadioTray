/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radiotray;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Bryce
 */
public class RadioConfig extends Application {

    private RadioConfigController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = getClass().getResource("RadioConfig.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = (Parent) fxmlLoader.load(location.openStream());

        controller = fxmlLoader.getController();
        System.out.println("Stations in config: " + RadioTray.getINSTANCE().getCurrentConfig().getStations());
        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("Edit Stations");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void main(String[] args) {
        launch(args);
    }

}
