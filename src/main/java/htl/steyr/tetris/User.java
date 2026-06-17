package htl.steyr.tetris;

public record User(String name, Integer score) {

    @Override
    public String toString() {
        return score + "\t | " + name;
    }
}
