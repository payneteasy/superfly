package com.payneteasy.superfly.security.mapbuilder;

import com.payneteasy.superfly.api.SSOAction;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * {@link ActionsSource} which takes actions from a file.
 * 
 * @author Roman Puchkovskiy
 */
public class ResourceActionsSource implements ActionsSource {
	
	private Resource resource;
	private Charset charset = StandardCharsets.UTF_8;

	@Required
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setEncoding(String encoding) {
		this.charset = encoding == null ? null : Charset.forName(encoding);
	}

	public SSOAction[] getActions() throws Exception {
		InputStream is = resource.getInputStream();
		Reader reader = new InputStreamReader(is, charset);
		List<SSOAction> actionsList = new ArrayList<>();
		try (Scanner scanner = new Scanner(reader)) {
			while (scanner.hasNext()) {
				String token = scanner.next();
				actionsList.add(new SSOAction(token, false));
			}
		}
		return actionsList.toArray(new SSOAction[actionsList.size()]);
	}

}
