package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SubsystemService;
import org.apache.wicket.model.LoadableDetachableModel;

/**
* @author rpuch
*/
public class SubsystemLoginCssUrlModel extends LoadableDetachableModel<String> {
    private final SubsystemService subsystemService;
    private final SSOLoginData loginData;

    public SubsystemLoginCssUrlModel(SubsystemService subsystemService,
            SSOLoginData loginData) {
        this.subsystemService = subsystemService;
        this.loginData = loginData;
    }

    @Override
    protected String load() {
        UISubsystem subsystem = null;
        if (loginData != null) {
            subsystem = subsystemService.getSubsystemByName(loginData.getSubsystemIdentifier());
        }
        return subsystem == null ? null : subsystem.getLoginFormCssUrl();
    }
}
