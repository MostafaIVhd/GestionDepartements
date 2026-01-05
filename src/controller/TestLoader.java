package controller;

import javafx.fxml.FXMLLoader;

import java.net.URL;

public class TestLoader {
    public static void main(String[] args) {
        try {
            URL url = TestLoader.class.getResource("/admin_dashboard.fxml");
            System.out.println("Resource URL: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            loader.load();
            System.out.println("FXML loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
