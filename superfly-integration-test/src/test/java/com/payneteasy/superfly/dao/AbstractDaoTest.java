package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.RoutineResult;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@ContextConfiguration({"/spring/test-datasource.xml", "/spring/test-dao.xml"})
public abstract class AbstractDaoTest extends AbstractJUnit4SpringContextTests {
    private static boolean alreadyRunCreateDb = false;

    static {
        if (!alreadyRunCreateDb) {
            try {
                createDb();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                // to prevent rerunning
                alreadyRunCreateDb = true;
            }
        }
    }

    private static void createDb() throws IOException, InterruptedException {
        if (Boolean.getBoolean("sso.db.skipCreate")) {
            return;
        }
        // surefire sets basedir to the module directory; the script paths are relative to it
        File moduleDir = new File(System.getProperty("basedir", ".")).getAbsoluteFile();
        ProcessBuilder builder = new ProcessBuilder("src/test/sh/create_test_database.sh");
        builder.directory(moduleDir);
        builder.redirectErrorStream(true);
        Process proc = builder.start();
        StringBuilder output = new StringBuilder();
        try (Scanner scanner = new Scanner(proc.getInputStream(), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println("DB: " + line);
                output.append(line).append('\n');
            }
        }
        int returnCode = proc.waitFor();
        if (returnCode != 0) {
            throw new IllegalStateException("Return code from create_test_database.sh is not 0 but " + returnCode
                    + ":\n" + output);
        }
    }

    protected void assertRoutineResult(RoutineResult result) {
        Assert.assertNotNull("Routine result cannot be null", result);
        Assert.assertTrue("Routine result must be OK", result.isOk());
    }
}
