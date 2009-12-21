package org.payneteasy.superfly.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.payneteasy.superfly.client.exception.CollectionException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import com.payneteasy.superfly.api.RoleDescription;

public class XmlRoleDescriptionCollector implements RoleDescriptionCollector {
	
	private Resource resource;

	@Required
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public List<RoleDescription> collect() throws CollectionException {
		List<RoleDescriptionBean> list = new ArrayList<RoleDescriptionBean>();
		Digester digester = new Digester();
		digester.push(list);
		digester.setNamespaceAware(false);
		digester.setValidating(false);

		digester.addObjectCreate("roles/role", RoleDescriptionBean.class);
		digester.addSetProperties("roles/role");
		digester.addSetNext("roles/role", "add", RoleDescriptionBean.class.getName());
		
		try {
			digester.parse(resource.getInputStream());
		} catch (IOException e) {
			throw new CollectionException(e);
		} catch (SAXException e) {
			throw new CollectionException(e);
		}
		
		List<RoleDescription> roles = new ArrayList<RoleDescription>(list.size());
		for (RoleDescriptionBean bean : list) {
			RoleDescription role = new RoleDescription();
			BeanUtils.copyProperties(bean, role);
			roles.add(role);
		}
		
		return roles;
	}

}
