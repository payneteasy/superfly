package com.payneteasy.superfly.dao;

import org.springframework.test.AbstractTransactionalSpringContextTests;

import com.payneteasy.superfly.model.RoutineResult;

public abstract class AbstractDaoTest extends AbstractTransactionalSpringContextTests {
    protected String[] getConfigLocations() {
        return new String[]{
                   "/spring/test-datasource.xml",
                   "/spring/test-dao.xml"
        };
    }
    
    protected void assertRoutineResult(RoutineResult result) {
    	assertNotNull("Routine result cannot be null", result);
    	assertTrue("Routine result must be OK", result.isOk());
    }
}
