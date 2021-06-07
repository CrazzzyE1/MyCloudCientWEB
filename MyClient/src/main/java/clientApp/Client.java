package clientApp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Client implements Closeable {

    private static Client instance;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buffer;
    private final int PORT = 8080;
    private Integer space;
    private Integer DEFAULT_SPACE = 15;
    private long freeSpace;
    private String login = "";
    private String message = "";
    private String currentDir = "";

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    private Client() {
//        try {
        this.freeSpace = 0;
        this.space = DEFAULT_SPACE;
//            this.socket = new Socket("localhost", PORT);
//            this.is = new DataInputStream(socket.getInputStream());
//            this.os = new DataOutputStream(socket.getOutputStream());
//            this.buffer = new byte[65536]; // 64 кбайта
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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

//    // Отправка сообщений
//    public void sendMessage(String msg) {
//        try {
//            os.write(msg.getBytes(StandardCharsets.UTF_8));
//            os.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    // Отправка сообщений
    public void sendMessage(String msg) {
        final String HOST = "localhost";
        final String PORT = "8080";
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
                System.out.println("ANSWER from send message: " + message);
            } else {
                System.out.println("fail " + connection.getResponseCode() + ", " + connection.getResponseMessage());
            }
        } catch (Throwable cause) {
            cause.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    //     Чтение сообщений
    public String readMessage() {
        return message;
    }


//    // Чтение сообщений
//    public String readMessage() {
//        String msg = "";
//        try {
//            int bytesRead = is.read(buffer);
//            msg = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return msg;
//    }

    public boolean getFile(String pcPath, String name, long size) {
        if (Files.exists(Paths.get(pcPath + "/" + name))) {
            name = "copy_".concat(name);
        }
        long count = 0L;
        try {
            while (size != count) {
                int bytesRead = is.read(buffer);
                count += bytesRead;
                System.out.println(count);
                byte[] tmp = new byte[bytesRead];
                System.arraycopy(buffer, 0, tmp, 0, tmp.length);
                if (!Files.exists(Paths.get(pcPath + "/" + name))) {
                    Files.createFile(Paths.get(pcPath + "/" + name));
                }
                Files.write(Paths.get(pcPath + "/" + name), tmp, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean sendFile(String pcPath, String name) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(pcPath + "/" + name.replace("??", " ")));
            System.out.println("Size of bytes: " + bytes.length);
            os.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        is.close();
        os.close();
    }

//    final String HOST = "localhost";
//    final String PORT = "8080";
//    final String PAGE = "api";
//    final String QUERY = "login pass".replace(" ", "%20");
//    String url = "http://" + HOST + ":" + PORT + "/" + PAGE + "?query=" + QUERY;
//    HttpURLConnection connection = null;
//        try {
//        connection = (HttpURLConnection) new URL(url).openConnection();
//        connection.setRequestMethod("GET");
//        connection.setUseCaches(false);
//        connection.setConnectTimeout(250);
//        connection.setReadTimeout(250);
//        connection.connect();
//        StringBuilder sb = new StringBuilder();
//        if(HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
//            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null){
//                sb.append(line);
//                sb.append("\n");
//            }
//            System.out.println(sb.toString().trim());
//        } else {
//            System.out.println("fail " + connection.getResponseCode()+ ", " + connection.getResponseMessage());
//        }
//    } catch (Throwable cause) {
//        cause.printStackTrace();
//    }
//        finally {
//        if(connection != null) {
//            connection.disconnect();
//        }
//    }
}
