package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

public class LoginView {

    // Lab only: hardcoded staff accounts (username -> password)
    private static final Map<String, String> USERS = Map.of(
            "Manager", "admin123",
            "reception", "lodge2026",
            "Rodrique", "juicedarod"
    );

    private static String currentUser;

    private final Scene scene;
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label errorLabel = new Label();

    public LoginView(Runnable onSuccess) {
        Label title = new Label("Lodge Billing Lab");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c5d94"));

        Label subtitle = new Label("Staff login");
        subtitle.setTextFill(Color.GRAY);

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        errorLabel.setTextFill(Color.RED);

        Button loginBtn = new Button("Login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setDefaultButton(true); // Enter key also logs in
        loginBtn.setStyle("-fx-background-color: #367ce8; -fx-text-fill: white; -fx-font-weight: bold;");

        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText();

            if (pass.equals(USERS.get(user))) {
                currentUser = user;
                clear();
                onSuccess.run();
            } else {
                errorLabel.setText("Wrong username or password.");
                passwordField.clear();
            }
        });

        VBox card = new VBox(12, title, subtitle, usernameField, passwordField, loginBtn, errorLabel);
        card.setPadding(new Insets(30));
        card.setMaxWidth(340);
        card.setMaxHeight(VBox.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dce7f5; "
                + "-fx-border-radius: 6; -fx-background-radius: 6;");

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: #f4f8fe;");

        // Same size as the main scene so the window does not jump
        scene = new Scene(root, 1000, 750);
    }

    public Scene getScene() {
        return scene;
    }

    public void clear() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setText("");
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}