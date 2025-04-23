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

        Boolean running = true;
        while (running){
            if (isView.getScaledImage() != null){
                isModel.setImage(isView.getScaledImage());
                System.out.println("got it");
            }else System.out.println("no");
        }
    }
}
