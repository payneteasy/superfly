package com.payneteasy.superfly.web.wicket.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.page.user.ChangePasswordPage;

@Secured({"ROLE_ADMIN","ROLE_ACTION_TEMP_PASSWORD"})
public class HomePage extends BasePage{
  private static final List<Class<? extends Page>> entryPages =
      new ArrayList<Class<? extends Page>>();
  static {
    entryPages.add(AdminHomePage.class); // for admin
    entryPages.add(ChangePasswordPage.class); // for admin
  }

    public HomePage(PageParameters params) {
        super(params);
    }

    public HomePage() {
	}

	@Override
	protected String getTitle() {
		return "Superfly home";
	}

   @Override
   protected void onBeforeRender() {
    super.onBeforeRender();
    for (Class<? extends Page> pageClass : entryPages) {
      if (SecurityUtils.isComponentVisible(pageClass)) {
        getRequestCycle().setRedirect(true);
        throw new RestartResponseException(pageClass);
      }
    }
  }
}
