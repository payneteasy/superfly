package com.payneteasy.superfly.model.releasenotes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains info about a release.
 */
public class Release implements Serializable {
	private List<ReleaseItem> items = new ArrayList<ReleaseItem>();
	private String number;
	private String date;
	
	public List<ReleaseItem> getItems() {
		return items;
	}
	
	public void setItems(List<ReleaseItem> items) {
		this.items = items;
	}
	
	public void addItem(ReleaseItem itemBean){
		items.add(itemBean);
	}
		
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
}
