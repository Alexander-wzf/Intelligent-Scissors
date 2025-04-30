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
        isView.setIsModel(isModel);
        isModel.setIsView(isView);
    }
}
