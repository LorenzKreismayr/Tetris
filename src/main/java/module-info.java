module htl.steyr.tetris {
    requires javafx.controls;
    requires javafx.fxml;


    opens htl.steyr.tetris to javafx.fxml;
    exports htl.steyr.tetris;
}