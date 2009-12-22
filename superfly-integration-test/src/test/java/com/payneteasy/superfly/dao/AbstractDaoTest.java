package com.payneteasy.superfly.dao;

import org.springframework.test.AbstractTransactionalSpringContextTests;

public abstract class AbstractDaoTest extends AbstractTransactionalSpringContextTests {
    protected String[] getConfigLocations() {
        return new String[]{
                   "/spring/test-datasource.xml",
                   "/spring/test-dao.xml"
        };
    }
}
