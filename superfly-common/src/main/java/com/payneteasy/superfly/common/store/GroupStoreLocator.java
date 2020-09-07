package com.payneteasy.superfly.common.store;

import com.payneteasy.superfly.common.singleton.SingletonHolder;

/**
 * Simple locator of a group store.
 * 
 * @author Roman Puchkovskiy
 */
public class GroupStoreLocator {
    private static SingletonHolder<StringStore> groupStoreHolder = new SingletonHolder<StringStore>() {
        @Override
        protected StringStore createInstance() {
            return createGroupStore();
        }
    };

    public static StringStore getGroupStore() {
        return groupStoreHolder.getInstance();
    }

    protected static StringStore createGroupStore() {
        return new SimpleStringStore();
    }
}
