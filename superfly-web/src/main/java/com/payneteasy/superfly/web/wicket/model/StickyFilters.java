package com.payneteasy.superfly.web.wicket.model;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import lombok.Data;

import java.io.Serializable;

/**
 * Model object which holds filters which are 'sticky'. A sticky filter is one
 * that is remembered for all screens when set on one of screens.
 *
 * @author Roman Puchkovskiy
 */
@Data
public class StickyFilters implements Serializable {
    private UISubsystemForFilter subsystem;
    private String               actionNameSubstring = "";
}
