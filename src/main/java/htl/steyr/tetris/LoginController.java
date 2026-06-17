package htl.steyr.tetris;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.FileWriter;
import java.util.Optional;
import java.util.Scanner;

public class LoginController {

    public Button loginButton;
    public TextField usernameTextField;
    public PasswordField passwordPasswordField;
    public RadioButton saveDataButton;
    public Label passwordLabel;

    public static int score;
    public static String username;
    public static String password;
    public Label warningLabel;

    public AnchorPane login_pane;

    public void onLoginButtonClicked(ActionEvent actionEvent) {
        username = usernameTextField.getText().trim();
        password = passwordPasswordField.getText();

        if (username.isEmpty()) {
            showWarningAlert("Username required!", "Please enter a username.");
            return;
        }

        if (!saveDataButton.isSelected()) {
            MainController.getInstance().setDisplayData(username, "0");
            MainController.getInstance().loadContentView("lobby-view.fxml");
            return;
        }

        if (password.isEmpty()) {
            showWarningAlert("Password required!", "Please enter a password.");
            return;
        }

        String[] row = findUserRow(username);

        if (row != null) {
            String dbPassword = row[1];
            String dbHighscore = row[2];
            score = Integer.parseInt(row[2]);

            if (dbPassword.equals(password)) {
                MainController.getInstance().setDisplayData(username, dbHighscore);
                MainController.getInstance().loadContentView("lobby-view.fxml");
            } else {
                showWarningAlert("Wrong password!", "Please try again!");
            }
        } else {
            boolean signup = showSignupPopup();
            if (signup) {
                saveNewUser(username, password);
                MainController.getInstance().setDisplayData(username, "0");
                MainController.getInstance().loadContentView("lobby-view.fxml");
            }
        }

    }

    /**
     * Shows a warning alert with the given title and message.
     *
     * @param title   the alert title
     * @param message the alert content text
     */
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String[] findUserRow(String username) {
        File file = new File("userData.csv");
        if (!file.exists()) return null;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");
                if (parts.length == 3 && parts[0].equalsIgnoreCase(username)) {
                    return parts; //[username, password, highscore]
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveNewUser(String username, String password) {
        try (FileWriter writer = new FileWriter("userData.csv", true)) {
            writer.write(username + ";" + password + ";0\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean showSignupPopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sign up");
        alert.setHeaderText("User not found");
        alert.setContentText("Do you want to sign up using provided data?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void onSaveDataButtonClicked(ActionEvent actionEvent) {
        boolean selected = saveDataButton.isSelected();
        passwordLabel.setDisable(!selected);
        passwordPasswordField.setDisable(!selected);
    }
}