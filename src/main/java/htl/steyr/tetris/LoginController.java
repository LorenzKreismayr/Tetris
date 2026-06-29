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

    public AnchorPane login_pane;

    public void onLoginButtonClicked(ActionEvent actionEvent) {
        // Get the entered username and password
        username = usernameTextField.getText().trim();
        password = passwordPasswordField.getText();

        // Ensure a username was entered
        if (username.isEmpty()) {
            showWarningAlert("Username required!", "Please enter a username.");
            return;
        }

        // If the user doesn't want to save data, continue as a guest
        if (!saveDataButton.isSelected()){
            MainController.getInstance().setDisplayData(username, "0");
            MainController.getInstance().loadContentView("lobby-view.fxml");
            return;
        }

        // Ensure a password was entered
        if (password.isEmpty()) {
            showWarningAlert("Password required!", "Please enter a password.");
            return;
        }

        // Look for the user in the .csv
        String[] row = findUserRow(username);

        // User exists
        if (row != null) {
            String dbPassword = row[1];
            String dbHighscore = row[2];
            score = Integer.parseInt(row[2]);

            // Verify the entered password
            if (dbPassword.equals(password)) {
                // Successful login - load the lobby with saved data
                MainController.getInstance().setDisplayData(username, dbHighscore);
                MainController.getInstance().loadContentView("lobby-view.fxml");
            } else {
                // Incorrect password
                showWarningAlert("Wrong password!", "Please try again!");
            }
        } else {
            // User doesn't exist - ask if they want to create an account
            boolean signup = showSignupPopup();

            if (signup) {
                // Create a new user and start with a high score of 0
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

    // Searches the CSV file for a user and returns their data if found
    private String[] findUserRow(String username) {
        File file = new File("userData.csv");

        // Return null if the user data file does not exist
        if (!file.exists()) return null;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");

                // Match the entered username
                if (parts.length == 3 && parts[0].equalsIgnoreCase(username)) {
                    return parts; //[username, password, highscore]
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // User not found
        return null;
    }

    // Saves a new user to the CSV file with an initial high score of 0
    private void saveNewUser(String username, String password) {
        try (FileWriter writer = new FileWriter("userData.csv", true)) {
            writer.write(username + ";" + password + ";0\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Displays a confirmation dialog asking whether to create a new account
    private boolean showSignupPopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sign up");
        alert.setHeaderText("User not found");
        alert.setContentText("Do you want to sign up using provided data?");

        Optional<ButtonType> result = alert.showAndWait();

        // Return true only if the user clicks OK
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Enables or disables the password field based on the "Save Data" checkbox
    public void onSaveDataButtonClicked(ActionEvent actionEvent) {
        boolean selected = saveDataButton.isSelected();
        passwordLabel.setDisable(!selected);
        passwordPasswordField.setDisable(!selected);
    }
}