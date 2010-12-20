package com.payneteasy.superfly.web.wicket.component.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MappingModel<T> implements Serializable {
	private List<T> selectedInMapped = new ArrayList<T>();
	private List<T> selectedInUnMapped = new ArrayList<T>();
	private String searchMappedString;
	private String searchUnMappedString;

	public List<T> getSelectedInMapped() {
		return selectedInMapped;
	}

	public void setSelectedInMapped(List<T> selectedInMapped) {
		this.selectedInMapped = selectedInMapped;
	}

	public List<T> getSelectedInUnMapped() {
		return selectedInUnMapped;
	}

	public void setSelectedInUnMapped(List<T> selectedInUnMapped) {
		this.selectedInUnMapped = selectedInUnMapped;
	}

	public String getSearchMappedString() {
		return searchMappedString;
	}

	public void setSearchMappedString(String searchMappedString) {
		this.searchMappedString = searchMappedString;
	}

	public String getSearchUnMappedString() {
		return searchUnMappedString;
	}

	public void setSearchUnMappedString(String searchUnMappedString) {
		this.searchUnMappedString = searchUnMappedString;
	}

}
