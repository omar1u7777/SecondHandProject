package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Main class for the SecondHandProject JavaFX application.
 * Loads the FXML file to display the GUI with tabs.
 */
public class App extends Application {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Kontrollera att JavaFX är tillgängligt
            if (!isJavaFXAvailable()) {
                throw new RuntimeException("JavaFX runtime components are missing. Please ensure JavaFX is on the module path.");
            }

            // Ladda main.fxml från resurser
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/org/example/main.fxml"));
            if (fxmlLoader.getLocation() == null) {
                throw new IOException("Unable to locate main.fxml in resources");
            }

            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            primaryStage.setTitle("SecondHand Project");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true); // Gör fönstret skalbart
            primaryStage.show();
            logger.info("Application started successfully with main.fxml loaded");
        } catch (IOException e) {
            logger.severe("Failed to load main.fxml: " + e.getMessage() + "\nStacktrace: " + getStackTrace(e));
            showErrorAlert("UI Load Error", "Failed to load the user interface: " + e.getMessage());
            primaryStage.close();
        } catch (RuntimeException e) {
            logger.severe("JavaFX runtime error: " + e.getMessage() + "\nStacktrace: " + getStackTrace(e));
            showErrorAlert("JavaFX Error", e.getMessage());
            primaryStage.close();
        }
    }

    /**
     * Kontrollerar om JavaFX är tillgängligt i körningsmiljön.
     *
     * @return true om JavaFX är tillgängligt, annars false.
     */
    private boolean isJavaFXAvailable() {
        try {
            Class.forName("javafx.application.Application");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Visar ett felmeddelande i en dialogruta.
     *
     * @param title Titeln på dialogrutan.
     * @param contentText Texten i dialogrutan.
     */
    private void showErrorAlert(String title, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(contentText + "\nPlease check the logs or configure JavaFX properly.");
        alert.showAndWait();
    }

    /**
     * Konverterar en exceptions stack trace till en sträng för loggning.
     *
     * @param e Undantaget att bearbeta.
     * @return Stack tracen som en sträng.
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}