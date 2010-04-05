package com.payneteasy.superfly.security.mapbuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import com.payneteasy.superfly.api.SSOAction;

/**
 * {@link ActionsSource} which takes actions from a file.
 * 
 * @author Roman Puchkovskiy
 */
public class ResourceActionsSource implements ActionsSource {
	
	private Resource resource;
	private String encoding = "utf-8";

	@Required
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public SSOAction[] getActions() throws Exception {
		InputStream is = resource.getInputStream();
		Reader reader = new InputStreamReader(is, encoding);
		Scanner scanner = new Scanner(reader);
		List<SSOAction> actionsList = new ArrayList<SSOAction>();
		try {
			while (scanner.hasNext()) {
				String token = scanner.next();
				actionsList.add(new SSOAction(token, false));
			}
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return actionsList.toArray(new SSOAction[actionsList.size()]);
	}

}
