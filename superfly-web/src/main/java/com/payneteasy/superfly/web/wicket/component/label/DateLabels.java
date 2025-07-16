package com.payneteasy.superfly.web.wicket.component.label;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.experimental.UtilityClass;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

/**
 * Factory for DateLabel instances.
 *
 * @author Roman Puchkovskiy
 */
@UtilityClass
public class DateLabels {

    private static final String DATE_TIME_PATTERN = "yyyy.MM.dd HH:mm:ss";

    static final SimpleDateFormat DATE_TIME_PATTERN_FORMATTER =  new SimpleDateFormat(DATE_TIME_PATTERN);

    /**
     * Creates a label which displays date and time.
     *
     * @param id    component id
     * @param date    date to display
     * @return date label
     */
    public static Label forDateTime(String id, Date date) {
        IModel<String> model = new LoadableDetachableModel<>() {
            @Override
            protected String load() {
                return date != null ? DATE_TIME_PATTERN_FORMATTER.format(date) : "N/A";
            }
        };
        return new Label(id, model);
    }
}
