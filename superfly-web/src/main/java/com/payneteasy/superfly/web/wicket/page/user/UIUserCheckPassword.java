package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.ui.user.UIUserForCreate;

public class UIUserCheckPassword extends UIUserForCreate {
    private String password2;

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }
}
