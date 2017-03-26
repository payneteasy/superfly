package com.payneteasy.superfly.password;

import org.junit.Assert;
import org.junit.Test;

public class PasswordGeneratorImplTest {
    @Test
    public void test() {
        PasswordGeneratorImpl generator = new PasswordGeneratorImpl();
        generator.setPasswordLength(8);
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(8, generator.generate().length());
        }
    }
}
