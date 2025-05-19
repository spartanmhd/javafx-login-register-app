package com.example.logingui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private Label loginMessageLabel;
    @FXML
    private ImageView brandingImageView;
    @FXML
    private ImageView lockImageView;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField enterPasswordField;
    @FXML
    private Button registerButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Image brandingImage = new Image(getClass().getResource("/com/example/logingui/Images/NeyseyMqtt.png").toExternalForm());
        brandingImageView.setImage(brandingImage);

        Image lockImage = new Image(getClass().getResource("/com/example/logingui/Images/verrouiller-alt.png").toExternalForm());
        lockImageView.setImage(lockImage);

        // Reset the register button style
        registerButton.setStyle("-fx-background-color: #000000; -fx-text-fill: WHITE;");

        // Clear any previous error messages
        loginMessageLabel.setText("");
    }

    public void loginButtonOnAction(ActionEvent event) {
        // Reset the register button style
        registerButton.setStyle("-fx-background-color: #000000; -fx-text-fill: WHITE;");

        if (!usernameTextField.getText().isBlank() && !enterPasswordField.getText().isBlank()) {
            validateLogin();
        } else {
            loginMessageLabel.setText("Please enter your username and password");
        }
    }

    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void validateLogin() {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        try {
            // Get the entered username and password
            String username = usernameTextField.getText();
            String password = enterPasswordField.getText();

            // First check if the username exists
            String checkUsername = "SELECT * FROM user_account WHERE username = ?";
            PreparedStatement checkUsernameStatement = connectDB.prepareStatement(checkUsername);
            checkUsernameStatement.setString(1, username);
            ResultSet usernameResult = checkUsernameStatement.executeQuery();

            if (!usernameResult.next()) {
                // Username doesn't exist
                loginMessageLabel.setText("User not found. Please register.");
                // Highlight the register button to make it more obvious
                registerButton.setStyle("-fx-background-color: #ff8c00; -fx-text-fill: WHITE;");
                usernameResult.close();
                checkUsernameStatement.close();
                connectNow.closeConnection(); // Use the new method to close the connection
                return;
            }

            // Get the stored password from the database
            String storedPassword = usernameResult.getString("password");

            // Compare the entered password with the stored password directly (no hashing)
            if (password.equals(storedPassword)) {
                // Passwords match, login successful
                loginMessageLabel.setText("Login successful!");

                // Close resources before opening main application
                usernameResult.close();
                checkUsernameStatement.close();
                connectNow.closeConnection();

                openMainApplication();
            } else {
                // Passwords don't match
                loginMessageLabel.setText("Incorrect password. Try again.");

                // Close resources
                usernameResult.close();
                checkUsernameStatement.close();
                connectNow.closeConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loginMessageLabel.setText("Database connection error: " + e.getMessage());

            // Make sure to close the connection even if there's an error
            connectNow.closeConnection();
        }
    }

    /**
     * Hashes a password using SHA-256 algorithm
     * @param password The password to hash
     * @return The hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            // If hashing fails, return the original password (not ideal but prevents login failure)
            return password;
        }
    }

    public void registerButtonOnAction(ActionEvent event) {
        openRegistrationForm();
    }

    public void openMainApplication() {
        try {
            // Close the login window
            Stage loginStage = (Stage) cancelButton.getScene().getWindow();

            // Create a new stage for the welcome message
            Stage welcomeStage = new Stage();
            welcomeStage.initStyle(StageStyle.DECORATED);

            // Create a simple welcome scene
            Label welcomeLabel = new Label("Bienvenu à Neysey Solution");
            welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Button closeButton = new Button("Continuer");
            closeButton.setOnAction(e -> welcomeStage.close());
            closeButton.setStyle("-fx-background-color: #000000; -fx-text-fill: WHITE;");

            VBox layout = new VBox(20);
            layout.setAlignment(javafx.geometry.Pos.CENTER);
            layout.setPadding(new javafx.geometry.Insets(20));
            layout.getChildren().addAll(welcomeLabel, closeButton);

            Scene welcomeScene = new Scene(layout, 400, 200);
            welcomeStage.setScene(welcomeScene);
            welcomeStage.setTitle("Neysey Solution");

            // Show the welcome stage and close the login stage
            welcomeStage.show();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openRegistrationForm() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("register.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 520, 592);
            Stage registerStage = new Stage();
            registerStage.initStyle(StageStyle.UNDECORATED);
            registerStage.setTitle("Registration");
            registerStage.setScene(scene);
            registerStage.show();

            // Hide the login form but don't close it
            // This way it can be shown again when registration is complete
            Stage loginStage = (Stage) cancelButton.getScene().getWindow();
            loginStage.hide();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            loginMessageLabel.setText("Error opening registration form: " + ex.getMessage());
        }
    }
}
