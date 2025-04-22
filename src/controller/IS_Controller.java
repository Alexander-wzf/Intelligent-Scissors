package controller;

import view.IS_View;

public class IS_Controller {

    IS_View isView;

    public IS_Controller(IS_View isView) {
        this.isView = isView;
    }

    public void start(){
        isView.setVisible(true);
    }
}
