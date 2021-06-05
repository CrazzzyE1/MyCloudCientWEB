package clientApp.Controllers;

import clientApp.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

// Контроллер окна Удаление аккаунта
public class RemoveController {
    @FXML
    TextField password;

    private Client client;

    public RemoveController() {
        client = Client.getInstance();
    }


    public void cancel(ActionEvent actionEvent) {
        password.getScene().getWindow().hide();
    }

    public void remove(ActionEvent actionEvent) {
        String pass = password.getText().trim();
        if (pass.isEmpty()) {
            password.clear();
            password.setPromptText("Empty password");
            return;
        }
        System.out.println("remove " + client.getLogin() + " " + pass);
        client.sendMessage("remove " + client.getLogin() + " " + pass);
        String answer = client.readMessage();
        if (answer.equals("success")) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Account removed");
            alert.showAndWait();
            Platform.exit();
        } else {
            password.clear();
            password.setPromptText("Wrong password");
        }
    }
}
