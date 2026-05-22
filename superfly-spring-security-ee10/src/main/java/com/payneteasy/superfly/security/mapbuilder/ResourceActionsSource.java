package com.payneteasy.superfly.security.mapbuilder;

import com.payneteasy.superfly.api.SSOAction;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * {@link ActionsSource} which takes actions from a file.
 *
 * @author Roman Puchkovskiy
 */
@AllArgsConstructor
public class ResourceActionsSource implements ActionsSource {

    private final Resource resource;
    private final Charset  charset;

    public SSOAction[] getActions() throws Exception {
        InputStream     is          = resource.getInputStream();
        Reader          reader      = new InputStreamReader(is, charset);
        List<SSOAction> actionsList = new ArrayList<>();

        try (Scanner scanner = new Scanner(reader)) {
            while (scanner.hasNext()) {
                String token = scanner.next();
                actionsList.add(new SSOAction(token, false));
            }
        }

        return actionsList.toArray(new SSOAction[0]);
    }
}
