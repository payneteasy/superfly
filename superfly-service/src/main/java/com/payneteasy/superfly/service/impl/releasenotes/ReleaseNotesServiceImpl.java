package com.payneteasy.superfly.service.impl.releasenotes;

import com.payneteasy.superfly.model.releasenotes.News;
import com.payneteasy.superfly.model.releasenotes.Release;
import com.payneteasy.superfly.service.releasenotes.ReleaseNotesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ReleaseNotesServiceImpl implements ReleaseNotesService {

    private static Logger logger = LoggerFactory.getLogger(ReleaseNotesServiceImpl.class);

    private Resource resource = new ClassPathResource("release-notes.xml");

    private List<Release> listReleaseNotes;

    public synchronized List<Release> getReleaseNotes() {
        if (listReleaseNotes != null) {
            return listReleaseNotes;
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(News.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            News news = (News) jaxbUnmarshaller.unmarshal(resource.getFile());
            listReleaseNotes = Collections.unmodifiableList(news.getReleases());
        } catch (JAXBException | IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return listReleaseNotes;
    }

}
