package com.payneteasy.superfly.service.mock;

import java.util.ArrayList;
import java.util.List;

import com.payneteasy.superfly.model.ui.UISubsystem;
import com.payneteasy.superfly.model.ui.UISubsystemForList;
import com.payneteasy.superfly.service.SubsystemService;

public class SubsystemServiceMock implements SubsystemService {

	public void createSubsystem(UISubsystem subsystem) {
	}

	public void deleteSubsystem(Long subsystemId) {
	}

	public List<UISubsystemForList> getSubsystems() {
		List<UISubsystemForList> list = new ArrayList<UISubsystemForList>();
		UISubsystemForList ss;
		ss = new UISubsystemForList();
		ss.setIdentifier("test1");
		ss.setName("system one");
		ss.setCallbackInformation("http://system-one.no");
		list.add(ss);
		ss = new UISubsystemForList();
		ss.setIdentifier("test2");
		ss.setName("system two");
		ss.setCallbackInformation("http://system-two.no");
		list.add(ss);
		return list;
	}

	public void updateSubsystem(UISubsystem subsystem) {
	}

}
