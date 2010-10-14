package com.payneteasy.superfly.web.wicket.page;

import java.util.ArrayList;
import java.util.List;

import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.page.user.ChangePasswordPage;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.releasenotes.Release;
import com.payneteasy.superfly.model.releasenotes.ReleaseItem;
import com.payneteasy.superfly.service.releasenotes.ReleaseNotesService;

@Secured({"ROLE_ADMIN","ROLE_TEMP_PASSWORD"})
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
