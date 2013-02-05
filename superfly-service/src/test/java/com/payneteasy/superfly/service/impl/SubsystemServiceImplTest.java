package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.SubsystemTokenData;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author rpuch
 */
public class SubsystemServiceImplTest extends TestCase {
    private SubsystemDao subsystemDao;
    private SubsystemServiceImpl subsystemService;

    public void setUp() {
        subsystemDao = EasyMock.createStrictMock(SubsystemDao.class);
        subsystemService = new SubsystemServiceImpl();
        subsystemService.setSubsystemDao(subsystemDao);
    }

    public void testGetSubsystemTokenIfCanLogin() throws Exception {
        SubsystemTokenData tokenData = new SubsystemTokenData();
        tokenData.setSubsystemToken("abc");
        tokenData.setLandingUrl("url");
        EasyMock.expect(subsystemDao.issueSubsystemTokenIfCanLogin(EasyMock.eq(1L), EasyMock.eq("subsystem"), EasyMock.anyObject(String.class)))
                .andReturn(tokenData);

        EasyMock.replay(subsystemDao);
        Assert.assertSame(tokenData, subsystemService.issueSubsystemTokenIfCanLogin(1L, "subsystem"));
        EasyMock.verify(subsystemDao);
    }
}
