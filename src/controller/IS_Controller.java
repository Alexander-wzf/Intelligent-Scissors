package controller;

import model.IS_Model;
import view.IS_View;

public class IS_Controller {

    IS_View isView;
    IS_Model isModel;

    public IS_Controller(IS_View isView, IS_Model isModel) {
        this.isView = isView;
        this.isModel = isModel;
    }

    public void start(){
        isView.setVisible(true);

        boolean running = true;
        while (running){
            try {
                if (isView.getScaledImage() != null){
                    isModel.setImage(isView.getScaledImage());
                }
            }catch (Exception e){
                running = false;
                isView.showError();
                System.out.println("错误" + e);
            }
        }
    }
}
