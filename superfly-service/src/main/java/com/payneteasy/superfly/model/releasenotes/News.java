package com.payneteasy.superfly.model.releasenotes;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains info about a release.
 */
@XmlRootElement
public class News implements Serializable {
    private List<Release> releases = new ArrayList<>();

    @XmlElement(name = "release")
    public List<Release> getReleases() {
        return releases;
    }

    public void setReleases(List<Release> releases) {
        this.releases = releases;
    }
}
