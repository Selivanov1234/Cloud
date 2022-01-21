package com.geekbrains.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    public ListView<String> listView;
    public ListView<String> selectedFile;

    public TextField textField;

    private DataInputStream is;
    private DataOutputStream os;

    private File currentDir;

    private byte[] buf;
    private Window mainStage;

    public void sendMessages(ActionEvent actionEvent) throws IOException {
//        String message = textField.getText();
//        os.writeUTF(message);
//        os.flush();
//        textField.clear();
        String fileName = textField.getText();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        os.writeUTF("#SEND#FILE#");
        os.writeUTF(fileName);
        os.writeLong(currentFile.length());
        try (FileInputStream is = new FileInputStream(currentFile)) {
            while (true) {
                int read = is.read(buf);
                if (read == -1) {
                    break;
                }
                os.write(buf,0,read);
            }
        }
        os.flush();
    }

    private void read() {
        try {
            while (true) {
                String message = is.readUTF();
                Platform.runLater(() ->textField.setText(message));
//                Platform.runLater(() ->listView.getItems().add(message));
            }
        }catch (Exception e) {
            e.printStackTrace();
            // reconnect to server
        }
    }

    private void fillCurrentDirFiles() {
        listView.getItems().clear();
        listView.getItems().add("..");
        listView.getItems().addAll(currentDir.list());
    }

    private void initClickListener() {
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String fileName = listView.getSelectionModel().getSelectedItem();
                System.out.println("File Chosen: " + fileName);
                Path path = currentDir.toPath().resolve(fileName);
                if (Files.isDirectory(path)) {
                    currentDir = path.toFile();
                    fillCurrentDirFiles();
                    textField.clear();
                }else {
                    textField.setText(fileName);
                }
            }
        });
        selectedFile.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String fileName = selectedFile.getSelectionModel().getSelectedItem();
                System.out.println("File Chosen: " + fileName);
                Path path = currentDir.toPath().resolve(fileName);
                if (Files.isDirectory(path)) {
                    currentDir = path.toFile();
                    fillCurrentDirFiles();
                    textField.clear();
                }else {
                    textField.setText(fileName);
                }
            }
        });
        //currentDir.toPath().resolve("...").toFile();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
         try {
             buf = new byte[256];
             currentDir = new File(System.getProperty("user.home"));
             fillCurrentDirFiles();
             initClickListener();
             Socket socket = new Socket("localhost",8189);
             is = new DataInputStream(socket.getInputStream());
             os = new DataOutputStream(socket.getOutputStream());
             Thread readThread = new Thread(this::read);
             readThread.setDaemon(true);
             readThread.start();
         }catch (Exception e) {
             e.printStackTrace();
         }

    }

    public void sendFiles(ActionEvent actionEvent) {

    }

    public void downloadFiles(ActionEvent actionEvent) {

    }

    public void exitProgramme(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void chooseFiles(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"));
        File sf = fileChooser.showOpenDialog(null);
        fileChooser.setInitialDirectory(new File("C:\\Users"));
        if (sf != null) {
            selectedFile.getItems().add(sf.getName());
        }

    }
}
