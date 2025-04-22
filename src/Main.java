import controller.IS_Controller;
import view.IS_View;

public class Main {
    public static void main(String[] args) {
        IS_Controller isController = new IS_Controller(new IS_View());
        isController.start();
    }
}
