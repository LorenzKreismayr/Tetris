package htl.steyr.tetris;

import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class LeaderBoardController implements Initializable {
    public ListView<String> scoreListView;

    private final List<User> userList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try (BufferedReader reader = new BufferedReader(new FileReader("userData.csv"))) {
            try {
                String line = "";

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] tmp = line.split(";");
                    if (tmp.length >= 3) {
                        userList.add(
                                new User(tmp[0],
                                Integer.parseInt(tmp[2]))
                        );
                    }
                }

                userList.sort((u1, u2) -> Integer.compare(u2.score(), u1.score()));

                for (User user : userList) {
                    scoreListView.getItems().add(user.toString());
                }
            } catch (IOException e) {
                System.err.println("Could not open File: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Invalid Score: " + e.getMessage());
            }
        } catch (IOException ex) {
            System.err.println("Reader kas: " + ex.getMessage());
        }
        // Stream brav schließen
    }
}
