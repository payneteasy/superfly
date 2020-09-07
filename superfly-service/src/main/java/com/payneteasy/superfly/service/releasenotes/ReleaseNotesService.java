package com.payneteasy.superfly.service.releasenotes;

import java.util.List;

import com.payneteasy.superfly.model.releasenotes.Release;

/**
 * Used to obtain release notes.
 */
public interface ReleaseNotesService {
    List<Release> getReleaseNotes();
}
