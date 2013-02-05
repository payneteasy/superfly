package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.HomePage;
import org.apache.wicket.PageParameters;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Page used to change a password.
 */
@Secured("ROLE_ACTION_TEMP_PASSWORD")
public class ChangePasswordPage extends BasePage {

	public ChangePasswordPage(PageParameters params) {
		super(ListUsersPage.class, params);

        add(new ChangePasswordPanel("change-password-panel") {
            @Override
            protected String getCurrentUserName() {
                return SecurityUtils.getUsername();
            }

            @Override
            protected void onPasswordChanged() {
                getRequestCycle().setRedirect(true);
                getRequestCycle().setResponsePage(HomePage.class);
                getRequestCycle().getSession().invalidate();
                SecurityContextHolder.clearContext();
            }
        });
	}

	@Override
	protected String getTitle() {
		return "Change user password";
	}

}