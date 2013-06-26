package com.payneteasy.superfly.web.wicket.component.paging;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.LoadableDetachableModel;

public class SuperflyPagingNavigator extends Panel{
	public SuperflyPagingNavigator(String aId, final DataView<?> aPageable){
		super(aId);
        thePageable = aPageable;

        addLink(new LinkInfo("paging-first") {
            long getPage() { return 0; }
            boolean isVisible() { return aPageable.getCurrentPage()>1; }
        });

        addLink(new LinkInfo("paging-previous") {
            long getPage() { return aPageable.getCurrentPage()-1; }
            boolean isVisible() { return aPageable.getCurrentPage()>0; }
        });

        addLabel(new LabelInfo("paging-from") {
            long getPage() { return aPageable.getCurrentPage() * aPageable.getItemsPerPage() + 1; }
        });

        addLabel(new LabelInfo("paging-to") {
            long getPage() {
                if(aPageable.getCurrentPage() == aPageable.getPageCount() - 1) {
                    return aPageable.getItemCount();
                } else {
                    return aPageable.getCurrentPage() * aPageable.getItemsPerPage() + aPageable.getItemsPerPage();
                }
            }
        });

        addLabel(new LabelInfo("paging-count") {
            long getPage() { return aPageable.getItemCount(); }
        });

        addLink(new LinkInfo("paging-next") {
            long getPage() { return aPageable.getCurrentPage() + 1; }
            boolean isVisible() { return aPageable.getCurrentPage() < aPageable.getPageCount() - 1 ; }
        });

        addLink(new LinkInfo("paging-last") {
            long getPage() { return aPageable.getPageCount() - 1; }
            boolean isVisible() { return aPageable.getCurrentPage() < aPageable.getPageCount() - 2 ; }
        });

    }

//    @Override
//    public boolean isVisible() {
//        return thePageable.getPageCount() > 1;
//    }

    private void addLabel(final LabelInfo aInfo) {
        String id = aInfo.getId();

        Label label = new Label(id, new LoadableDetachableModel<String>() {
            protected String load() {
                return String.valueOf(aInfo.getPage());
            }
        });
        add(label);
    }

    private void addLink(final LinkInfo aInfo) {
        String id = aInfo.getId();

        Link<String> link = new Link<String>(id) {

			private static final long serialVersionUID = 1L;

			@Override
            public boolean isVisible() {
                return aInfo.isVisible();
            }

            public void onClick() {
                thePageable.setCurrentPage(aInfo.getPage());
            }
        };
        add(link);

    }

    private abstract class LabelInfo implements Serializable{
        public LabelInfo(String aId) {
            theId = aId;
        }

        String getId() {
            return theId;
        }

        abstract long getPage();

        private final String theId;
    }

    private abstract class LinkInfo implements Serializable {

        public LinkInfo(String aId) {
            theId = aId;
        }

        String getId() {
            return theId;
        }

        abstract long getPage();

        abstract boolean isVisible() ;

        private final String theId;
    }
    
    private final DataView<?> thePageable;
}
