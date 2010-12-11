package com.payneteasy.superfly.web.wicket.page;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.releasenotes.Release;
import com.payneteasy.superfly.model.releasenotes.ReleaseItem;
import com.payneteasy.superfly.service.releasenotes.ReleaseNotesService;

@Secured("ROLE_ADMIN")
public class HomePage extends BasePage{

	@SpringBean
	private ReleaseNotesService releaseNotesService;



	public HomePage() {
        super(HomePage.class);
        
		List<Release> listReleases = releaseNotesService.getReleaseNotes();

		final ListView<Release> listViewReBean = new ListView<Release>(
				"releases", listReleases) {
			@Override
			protected void populateItem(ListItem<Release> item) {
				Release release = item.getModelObject();
				item.add(new Label("release-number", release.getNumber()));
				item.add(new Label("release-date", release.getDate()));

				ListView<ReleaseItem> itemsListView = new ListView<ReleaseItem>(
						"release-items", release.getItems()) {
					@Override
					protected void populateItem(ListItem<ReleaseItem> item) {
						ReleaseItem releaseItemBean = item.getModelObject();
						item.add(new Label("release-item-name",
								releaseItemBean.getName()));
						item.add(new Label("release-item-description",
								releaseItemBean.getDescription()));
					}

				};
				item.add(itemsListView);
			}

		};
		add(listViewReBean);
	}

	@Override
	protected String getTitle() {
		return "Superfly dashboard";
	}

}