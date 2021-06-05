package clientApp.Controllers;

import clientApp.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

// Контроллер окна Смена Пароля
public class ChangePasswordController {


    private Client client;

    public ChangePasswordController() {
        client = Client.getInstance();
    }

    @FXML
    TextField oldpassword;
    @FXML
    TextField newpassword;

    public void change(ActionEvent actionEvent) {
        String oldPass = oldpassword.getText().trim();
        String newPass = newpassword.getText().trim();
        if (oldPass.isEmpty() || newPass.isEmpty()) {
            oldpassword.clear();
            newpassword.clear();
            oldpassword.setPromptText("Empty password");
            return;
        }
        System.out.println("change " + client.getLogin() + " " + oldPass + " " + newPass);
        client.sendMessage("change " + client.getLogin() + " " + oldPass + " " + newPass);
        String answer = client.readMessage();
        if (answer.equals("success")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Password changed");
            alert.showAndWait();
            oldpassword.getScene().getWindow().hide();
        } else {
            oldpassword.clear();
            newpassword.clear();
            oldpassword.setPromptText("Wrong password");
        }
    }
}
