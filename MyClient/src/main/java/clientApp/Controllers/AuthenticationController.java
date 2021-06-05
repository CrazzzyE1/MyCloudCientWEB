package clientApp.Controllers;

import clientApp.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

// Контроллер окна Аутентификации
public class AuthenticationController {
    @FXML
    Button authButton;
    @FXML
    TextField login;
    @FXML
    TextField password;
    @FXML
    Hyperlink regLink;

    private Client client;

    public AuthenticationController() {
        client = Client.getInstance();
    }

    // По кнопке Auth отправляем данные для запроса на сервер
    public void auth() {
        if (login.getText().trim().isEmpty() || password.getText().trim().isEmpty()) {
            login.clear();
            password.clear();
            login.setPromptText("Login or Password is Empty");
            return;
        }
        String msg = "auth ".concat(login.getText().trim().replace(" ", "??"))
                .concat(" ").concat(password.getText().trim().replace(" ", "??"));
        client.sendMessage(msg);
        msg = client.readMessage();
        System.out.println(msg);
        // Если сервер ответил  Успешно, перходим в Основное окно приложения
        String space = msg.split(" ")[1];
        client.setSpace(Integer.parseInt(space));

        if (msg.split(" ")[0].equals("authsuccess")) {
            client.setLogin(login.getText().trim());
            System.out.println("Login: " + client.getLogin());
            changeWindow("cloud");
        } else {
            login.clear();
            password.clear();
            login.setPromptText("Wrong Login or Password");
        }
    }

    // Смена окна приложения
    public void changeWindow(String fxmlName) {
        try {
            String fxml = "/fxml/" + fxmlName + ".fxml";
            Parent chat = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle(fxmlName);
            stage.getIcons().add(new Image("/img/icon2.png"));
            stage.setScene(new Scene(chat));
            stage.setResizable(false);
            stage.show();
            login.getScene().getWindow().hide();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Переход в окно Регистрации
    public void registration() {
        changeWindow("registration");
    }
}
