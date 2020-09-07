package com.payneteasy.superfly.web.wicket.component.label;

import java.util.Date;

import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Factory for DateLabel instances.
 * 
 * @author Roman Puchkovskiy
 */
public class DateLabels {

    private static final String DATE_TIME_PATTERN = "yyyy.MM.dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy.MM.dd";

    /**
     * Creates a label which displays date only (year, month, day).
     *
     * @param id    component id
     * @param date    date to display
     * @return date label
     */
    public static DateLabel forDate(String id, Date date) {
        return DateLabel.forDatePattern(id, new Model<Date>(date), DATE_PATTERN);
    }

    /**
     * Creates a label which displays date only (year, month, day).
     *
     * @param id    component id
     * @param model    model returning date to display
     * @return date label
     */
    public static DateLabel forDate(String id, IModel<Date> model) {
        return DateLabel.forDatePattern(id, model, DATE_PATTERN);
    }

    /**
     * Creates a label which displays date and time.
     *
     * @param id    component id
     * @param date    date to display
     * @return date label
     */
    public static DateLabel forDateTime(String id, Date date) {
        return DateLabel.forDatePattern(id, new Model<Date>(date),
                DATE_TIME_PATTERN);
    }
}
