import controller.IS_Controller;
import model.IS_Model;
import view.IS_View;

public class Main {
    public static void main(String[] args) {
        IS_Controller isController = new IS_Controller(new IS_View(), new IS_Model());
        isController.start();
    }
}
