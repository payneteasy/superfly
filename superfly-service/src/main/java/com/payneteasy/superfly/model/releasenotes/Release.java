package com.payneteasy.superfly.model.releasenotes;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains info about a release.
 */
@XmlRootElement
public class Release implements Serializable {
    private List<ReleaseItem> items = new ArrayList<>();
    private String number;
    private String date;

    @XmlElement(name = "item")
    public List<ReleaseItem> getItems() {
        return items;
    }

    public void setItems(List<ReleaseItem> items) {
        this.items = items;
    }

    public void addItem(ReleaseItem itemBean){
        items.add(itemBean);
    }

    @XmlAttribute
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @XmlAttribute
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
