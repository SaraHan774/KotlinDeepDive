package design_pattern;

public class ChocolateBoiler2 {
    private boolean empty;
    private boolean boiled;
    private static ChocolateBoiler2 instance;

    private ChocolateBoiler2(boolean empty, boolean boiled) {
        this.empty = empty;
        this.boiled = boiled;
    }

    public static ChocolateBoiler2 getInstance() {
        if (instance == null) {
            instance = new ChocolateBoiler2(true, false);
        }
        return instance;
    }

    public void fill() {
        if (isEmpty()) {
            empty = false;
            boiled = false;
        }
    }

    public boolean isEmpty() { return empty; }

    public boolean boiled() { return boiled; }
}
