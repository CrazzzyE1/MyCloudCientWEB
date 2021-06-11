package clientApp;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import sun.text.normalizer.UTF16;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    private static Client instance;
    private byte[] buffer;
    final String HOST = "localhost";
    final String PORT = "8080";
    private Integer space;
    private long freeSpace;
    private String login = "";
    private String message = "";
    private String currentDir = "";
    private String copyCutTmpFile = "";

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    private Client() {
        this.freeSpace = 0;
        this.space = 15;
        this.buffer = new byte[65536];
    }


    public String getCopyCutTmpFile() {
        return copyCutTmpFile;
    }

    public void setCopyCutTmpFile(String copyCutTmpFile) {
        this.copyCutTmpFile = copyCutTmpFile;
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public Integer getSpace() {
        return space;
    }

    public void setFreeSpace(Integer size) {
        freeSpace = (space * 1000) - size / 1000000;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setSpace(Integer space) {
        this.space = space;
    }

    // Отправка сообщений
    public void sendMessage(String msg) {
        final String PAGE = "api";
        final String QUERY = msg.replace(" ", "%20");
        String url = "http://" + HOST + ":" + PORT + "/" + PAGE + "?query=" + QUERY;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.connect();
            StringBuilder sb = new StringBuilder();
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                message = sb.toString().trim();
            }
        } catch (Throwable cause) {
            cause.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    //Скачивание файла
    public void downloadFile(String name, String currentDir, String pcPath) {
        final String PAGE = "api/download";
        String url = "http://" + HOST + ":" + PORT + "/" + PAGE
                + "?name=" + name.replace(" ", "%20")
                + "&dir=" + currentDir;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
            connection.connect();
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                DataInputStream in = new DataInputStream(connection.getInputStream());
                int bytesRead;
                while (true) {
                    bytesRead = in.read(buffer);
                    if (bytesRead == -1) break;
                    byte[] tmp = new byte[bytesRead];
                    System.arraycopy(buffer, 0, tmp, 0, tmp.length);
                    if (!Files.exists(Paths.get(pcPath + "/" + name))) {
                        Files.createFile(Paths.get(pcPath + "/" + name));
                    }
                    Files.write(Paths.get(pcPath + "/" + name), tmp, StandardOpenOption.APPEND);
                }
            }
        } catch (Throwable cause) {
            cause.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Отправка файла на сервер
    public void uploadFile(String name, String dir, String pcPath) {
        String PAGE = "api/upload";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("name", name.replace("??", " "));
        builder.addTextBody("dir", dir);
        File file = new File(pcPath + "/" + name.replace("??", " "));
        ContentType fileContentType = ContentType.create("image/jpeg");
        String fileName = file.getName();
        builder.addBinaryBody("file", file, fileContentType, fileName);
        HttpEntity entity = builder.build();
        URI uri = URI.create("http://" + HOST + ":" + PORT + "/" + PAGE);
        HttpPost request = new HttpPost(uri);
        request.setEntity(entity);
        HttpClient client = HttpClients.createDefault();
        try {
            client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //     Чтение сообщений
    public String readMessage() {
        return message;
    }
}
