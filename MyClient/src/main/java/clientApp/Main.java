package clientApp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/authentication.fxml"));
        primaryStage.setTitle("MyCloudStorage");
        primaryStage.getIcons().add(new Image("/img/icon2.png"));
        primaryStage.setScene(new Scene(root, 1024, 720));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
