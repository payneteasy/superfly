package com.payneteasy.superfly.web.wicket.utils;

import com.payneteasy.superfly.web.wicket.page.user.UserDetailsPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

/**
 * Date: 09.04.13 Time: 13:18
 */
public class WicketComponentHelper {
    public static void clickTableRow(ListItem listItem, Class<? extends Page> pageClass, PageParameters pageParameters, Component component){
        listItem.add(new AttributeModifier("onclick", new Model<String>("javascript:return rowClick('" + component.urlFor(pageClass, pageParameters) + "', event);")));
        listItem.add(new AttributeAppender("class", new Model<String>("tabelRowPointer"), " "));
    }

    public static void tableRowInfoCondition(ListItem listItem, boolean condition){
        if(condition){
            tableRowInfo(listItem);
        }
    }

    public static void tableRowInfo(ListItem listItem){
        listItem.add(new AttributeAppender("class", new Model<String>("bg-info"), " "));
    }
}
