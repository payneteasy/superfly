package com.payneteasy.superfly.service.impl.releasenotes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import com.payneteasy.superfly.model.releasenotes.Release;
import com.payneteasy.superfly.model.releasenotes.ReleaseItem;
import com.payneteasy.superfly.service.releasenotes.ReleaseNotesService;

public class ReleaseNotesServiceImpl implements ReleaseNotesService {

	private static Logger logger = LoggerFactory.getLogger(ReleaseNotesServiceImpl.class);
	
	private Resource resource = new ClassPathResource("release-notes.xml");

	private List<Release> listReleaseNotes;
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public synchronized List<Release> getReleaseNotes() {
		if (listReleaseNotes == null) {
			listReleaseNotes = new ArrayList<Release>();
			Digester digester = new Digester();
			digester.push(listReleaseNotes);
			digester.addObjectCreate("news/release", Release.class);
			digester.addSetProperties("news/release", "number", "number");
			digester.addSetProperties("news/release", "date", "date");
			digester.addObjectCreate("news/release/item", ReleaseItem.class);
			digester.addSetProperties("news/release/item", "type", "type");
			digester.addBeanPropertySetter("news/release/item/name", "name");
			digester.addBeanPropertySetter("news/release/item/description", "description");
			digester.addSetNext("news/release/item/name", "addItem");
			digester.addSetNext("news/release", "add", Release.class.getName());
	
			try {
				digester.parse(resource.getInputStream());
			} catch (IOException e) {
				logger.error("Could not parse release-notes.xml", e);
				
			} catch (SAXException e) {
				logger.error("Could not parse release-notes.xml", e);
			}
	
			listReleaseNotes = Collections.unmodifiableList(listReleaseNotes);
		}
		
		return listReleaseNotes;
	}

}
