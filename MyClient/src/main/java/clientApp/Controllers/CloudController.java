package clientApp.Controllers;

import clientApp.Client;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

// Основной класс по работе с окном Клиентского приложения
public class CloudController implements Initializable {
    private ObservableList<String> list;
    private Client client;
    private String listFilesOnServer;
    private String pcPath;
    private boolean sortFlag = true;

    public CloudController() {
        client = Client.getInstance();
        list = FXCollections.observableArrayList();
        pcPath = new File("MyClient").getAbsolutePath().replace("\\", "/");
        File folder = new File(pcPath);
        if (!folder.exists()) folder.mkdir();
    }

    // Создание новой директории
    public void mkdir() {
        String name = folderName.getText().trim().replace(" ", "??");
        if (!name.isEmpty()) {
            client.sendMessage("mkdir ".concat(name.replace("[DIR]", "")).concat(" ").concat(client.getCurrentDir()));
            if (client.readMessage().equals("dirSuccess")) {
                client.sendMessage("ls ".concat(client.getCurrentDir()));
                listFilesOnServer = client.readMessage();
                updateListViewer(list, listFilesOnServer, cloudFilesList);
                folderName.setStyle("-fx-border-color: grey;");
                folderName.clear();
            } else {
                folderName.clear();
                folderName.setPromptText("Name exists");
            }
        } else {
            folderName.setPromptText("Enter new name");
            folderName.setStyle("-fx-border-color: red;");
        }
    }

    //Удаление папки или файла на сервере
    public void remove(ActionEvent actionEvent) {
        try {
            if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                    && !cloudFilesList.getSelectionModel().getSelectedItem().equals("<- Back")
                    && !cloudFilesList.getSelectionModel().getSelectedItem().equals("!Recycle_Bin")) {
                String name = cloudFilesList.getSelectionModel().getSelectedItem();
                if (name.startsWith("[DIR]")) {
                    name = "rm ".concat(name.replace(" ", "??").replace("[DIR]", ""));
                } else {
                    name = "rmf ".concat(name.replace(" ", "??"));
                }
                client.sendMessage(name
                        .concat(" ").concat(client.getCurrentDir()));
                if (client.readMessage().equals("rmSuccess")) {
                    client.sendMessage("ls ".concat(client.getCurrentDir()));
                    listFilesOnServer = client.readMessage();
                    updateListViewer(list, listFilesOnServer, cloudFilesList);
                }
            }
        } catch (RuntimeException ex) {
            System.out.println("Try again");
        }
        checkFreeSpace(client.getSpace());
    }

    //Сортировка списка файлов в списке файлов сервера (ListView). Криво выглядит, но работает. К ней вернусь еще.
    public void sortListView(ActionEvent actionEvent) {
        client.sendMessage("ls ".concat(client.getCurrentDir()));
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
        ArrayList<String> sb1 = new ArrayList<>();
        ArrayList<String> sb2 = new ArrayList<>();
        String tmp;
        ObservableList<String> str = cloudFilesList.getItems();
        for (int i = 0; i < str.size(); i++) {
            String tmpstr = str.get(i).replace(" ", "??");
            if (tmpstr.contains(".")) {
                sb1.add(tmpstr);
            } else if (!tmpstr.contains("<-??Back")) {
                sb2.add(tmpstr);
            }
        }
        if (sortFlag) {
            tmp = (sb1 + " " + sb2);
        } else {
            tmp = (sb2 + " " + sb1);
        }
        sortFlag = !sortFlag;
        tmp = tmp.replace(",", "")
                .replace("[", "")
                .replace("]", "")
                .replace("DIR", "[DIR]");
        updateListViewer(list, tmp, cloudFilesList);
    }

    //Получение списка файлов на ПК пользователя.
    public String getPcFilesList(String dir) {
        File file = new File(dir);
        File[] files = file.listFiles();
        StringBuilder sb = new StringBuilder();
        if (files == null) return sb.toString();
        for (File f : files) {
            sb.append(f.getName().replace(" ", "??")).append(" ");
        }
        return sb.toString();
    }

    // Обновление списка файлов сервера в пользовательском приложении.
    public void updateListViewer(ObservableList<String> list, String listFilesOnServer, ListView<String> listView) {
        listView.getItems().clear();
        list.removeAll(list);
        String[] files = listFilesOnServer.trim().split(" ");
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].replace("??", " ");
        }
        list.addAll("<- Back");
        if (Arrays.asList(files).get(0).isEmpty()) {
            list.addAll("Empty");
        } else {
            list.addAll(files);
        }
        listView.getItems().addAll(list);
        addressLine.setText(getAddressLine());
    }

    //Выбор элемента по двойному клику Cloud
    public void selectItem(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() >= 2 && !cloudFilesList.getSelectionModel().getSelectedItems().isEmpty()) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem()
                    .replace(" ", "??");
            cd(name);
        }
    }

    //Выбор элемента по двойному клику PC
    public void selectItemPC(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() >= 2 && !pcFilesList.getSelectionModel().getSelectedItems().isEmpty()) {
            String name = pcFilesList.getSelectionModel().getSelectedItem();
            if (name.equals("<- Back")) {
                pcPath = getPreviousPath(pcPath);
            } else if (new File(pcPath + "/" + name).isDirectory()) {
                pcPath = pcPath + "/" + name;
            }
            updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
            File file = new File(pcPath);
            pcPath = file.getAbsolutePath().replaceAll("\\\\", "/");
            addressPC.setText(pcPath);
        }
    }

    // Получение строки с адресом для Back
    public String getPreviousPath(String path) {

        int index = -1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                index = i;
            }
        }
        if (index == -1) return path;
        path = path.substring(0, index);
        if (path.equals("C:")) path = "C:/";
        return path;
    }

    //Смена директории
    public void cd(String dir) {
        if (dir.equals("<-??Back")) dir = "back";
        client.sendMessage("cd ".concat(dir.replace("[DIR]", "")).concat(" ").concat(client.getCurrentDir()));
        String answer = client.readMessage();
        if (answer.split(" ")[0].equals("success")) {
            client.setCurrentDir(answer.split(" ")[1]);
        }
        client.sendMessage("ls ".concat(client.getCurrentDir()));
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
    }

    //Получение адреса папки на сервере
    public String getAddressLine() {
        client.sendMessage("getAddress ".concat(client.getCurrentDir()));
        return client.getLogin().concat(client.readMessage());
    }

    //cd на ПК по папкам
    public void cdOnPc(ActionEvent actionEvent) {
        String tmp = addressPC.getText().trim();
        if (!addressPC.getText().trim().isEmpty() && new File(tmp).exists() && new File(tmp).listFiles() != null) {
            pcPath = addressPC.getText().trim();
            if (pcPath.equals("C") || pcPath.equals("C:")) pcPath = "C:/";
            addressPC.setText(pcPath);
            updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
        } else {
            addressPC.clear();
            addressPC.setText(pcPath);
        }
    }

    //Копирование файла
    public void copyFile(ActionEvent actionEvent) {
        if (client.getFreeSpace() < 0) return;
        if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                && !cloudFilesList.getSelectionModel().getSelectedItem().equals("<- Back")) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "??");
            client.setCopyCutTmpFile("copy ".concat(name).concat(" ").concat(client.getCurrentDir()));
        }
    }

    // Вырезание файла
    public void cut(ActionEvent actionEvent) {
        if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                && !cloudFilesList.getSelectionModel().getSelectedItem().equals("<- Back")) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "??");
            client.setCopyCutTmpFile("cut ".concat(name).concat(" ").concat(client.getCurrentDir()));
        }
    }

    // Вставка файла
    public void paste(ActionEvent actionEvent) {
        String answer = "";
        if (!client.getCopyCutTmpFile().isEmpty()) {
            client.sendMessage("paste ".concat(client.getCopyCutTmpFile().concat(" ").concat(client.getCurrentDir())));
            answer = client.readMessage();
        }
        if (answer.equals("pasteSuccess")) {
            client.sendMessage("ls ".concat(client.getCurrentDir()));
            listFilesOnServer = client.readMessage();
            updateListViewer(list, listFilesOnServer, cloudFilesList);
            checkFreeSpace(client.getSpace());
            client.setCopyCutTmpFile("");
        }
    }

    public void download(ActionEvent actionEvent) {
        if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                && !cloudFilesList.getSelectionModel().getSelectedItem().equals("<- Back")
                && !cloudFilesList.getSelectionModel().getSelectedItem().startsWith("[DIR]")) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "??");
            client.downloadFile(name.replace("??", " "), client.getCurrentDir(), pcPath);
            updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
        }
    }

    public void upload(ActionEvent actionEvent) throws RuntimeException {
        if (client.getFreeSpace() < 0) return;
        try {
            if (!pcFilesList.getSelectionModel().getSelectedItem().isEmpty()
                    && !pcFilesList.getSelectionModel().getSelectedItem().equals("<- Back")
                    && !pcFilesList.getSelectionModel().getSelectedItem().equals("Empty")) {
                String name = pcFilesList.getSelectionModel().getSelectedItem().replace(" ", "??");
                if (!Files.exists(Paths.get(pcPath + "/" + name.replace("??", " ")))) return;
                client.uploadFile(name, client.getCurrentDir(), pcPath);

                    client.sendMessage("ls ".concat(client.getCurrentDir()));
                    listFilesOnServer = client.readMessage();
                    updateListViewer(list, listFilesOnServer, cloudFilesList);

            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    // For fun
    public void openWebpage(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().browse(new URL("http://i.mycdn.me/i?r=AzEPZsRbOZEKgBhR0XGMT1RkUQz0tb6GH3YzGNzdL8pyWaaKTM5SRkZCeTgDn6uOyic").toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // поиск файлов на сервере
    public void search(ActionEvent actionEvent) {
        if (searchLabel.getText().trim().isEmpty()) return;
        String searchStr = searchLabel.getText().trim();
        client.sendMessage("search ".concat(searchStr).concat(" ").concat(client.getLogin()));
        searchStr = client.readMessage();
        searchLabel.clear();
        if (searchStr.equals("Not Found")) {
            searchLabel.setPromptText(searchStr);
            return;
        }
        searchLabel.setPromptText("Search file");
        updateListViewer(list, searchStr, cloudFilesList);
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void checkFreeSpace(Integer space) {
        client.sendMessage("checkSpace " + client.getLogin());
        double size = Double.parseDouble(client.readMessage());
        client.setFreeSpace((int) size);
        String tmp = "";
        if (size > 1023) {
            size = size / 1024;
            if (size > 1023) {
                size /= 1024;
                if (size > 1023) {
                    size /= 1024;
                    if (size > 1023) {
                        size /= 1024;
                        tmp = tmp.concat(String.format("%.2f", (size))).concat(" ").concat("TiB");
                    } else {
                        tmp = tmp.concat(String.format("%.2f", (size))).concat(" ").concat("GiB");
                    }
                } else {
                    tmp = tmp.concat(String.format("%.2f", (size))).concat(" ").concat("MiB");
                }
            } else {
                tmp = tmp.concat(String.format("%.2f", (size))).concat(" ").concat("KiB");
            }
        } else {
            tmp = tmp.concat(String.format("%.2f", (size))).concat(" ").concat("B");
        }
        tmp = tmp.concat(" / ").concat(space.toString()).concat(" MiB");
        freeSpace.setText(tmp);
    }

    public void recycleClean(ActionEvent actionEvent) {
        client.sendMessage("recycleClean ".concat(client.getLogin()));
        client.readMessage();
        client.sendMessage("ls ".concat(client.getCurrentDir()));
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
        checkFreeSpace(client.getSpace());
    }


    public void restore(ActionEvent actionEvent) {
        client.sendMessage("restore ".concat(client.getLogin()));
        client.readMessage();
        client.sendMessage("ls ".concat(client.getCurrentDir()));
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
        checkFreeSpace(client.getSpace());
    }

    public void changePassword(ActionEvent actionEvent) {
        changeWindow("changepassword");
    }

    public void removeAccount(ActionEvent actionEvent) {
        changeWindow("remove");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Переход в корзину
    public void recycleBin(ActionEvent actionEvent) {
        client.sendMessage("recycle ".concat(client.getLogin()));
        client.setCurrentDir(client.readMessage());
        client.sendMessage("ls ".concat(client.getCurrentDir()));
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
        updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
        addressPC.setText(pcPath);
        checkFreeSpace(client.getSpace());
        addressLine.setText("Recycle Bin");
    }

    // Инит на старте программы
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        client.sendMessage("ls ".concat(client.getCurrentDir()));
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
        updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
        addressPC.setText(pcPath);
        checkFreeSpace(client.getSpace());
    }

    @FXML
    TextField addressPC;

    @FXML
    private TextField folderName;

    @FXML
    private ListView<String> cloudFilesList;

    @FXML
    private ListView<String> pcFilesList;

    @FXML
    private TextField searchLabel;

    @FXML
    private TextField addressLine;

    @FXML
    private TextField freeSpace;

}
