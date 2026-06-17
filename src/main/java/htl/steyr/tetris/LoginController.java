package htl.steyr.tetris;

import javafx.event.ActionEvent;
import javafx.scene.control.*;

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
    public Label warningLabel;

    public static int score;
    public static String username;
    public static String password;

    public void onLoginButtonClicked(ActionEvent actionEvent) {
        username = usernameTextField.getText().trim();
        password = passwordPasswordField.getText();

        if (username.isEmpty()) {
            warningLabel.setText("Username required!");
            return;
        }

        if (!saveDataButton.isSelected()) {
            warningLabel.setText("");
            MainController.getInstance().setDisplayData(username, "0");
            MainController.getInstance().loadContentView("lobby-view.fxml");
            return;
        }

        if (password.isEmpty()) {
            warningLabel.setText("Password required!");
            return;
        }

        String[] row = findUserRow(username);

        if (row != null) {
            String dbPassword = row[1];
            String dbHighscore = row[2];
            score = Integer.parseInt(row[2]);

            if (dbPassword.equals(password)) {
                warningLabel.setText("");
                MainController.getInstance().setDisplayData(username, dbHighscore);
                MainController.getInstance().loadContentView("lobby-view.fxml");
            } else {
                warningLabel.setText("Wrong password!");
            }
        } else {
            boolean signup = showSignupPopup();
            if (signup) {
                saveNewUser(username, password);
                warningLabel.setText("");
                MainController.getInstance().setDisplayData(username, "0");
                MainController.getInstance().loadContentView("lobby-view.fxml");
            }
        }
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