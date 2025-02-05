package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.RoutineResult;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.IOException;
import java.io.InputStream;
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
        Process proc = Runtime.getRuntime().exec(new String[]{"src/test/sh/create_test_database.sh"}, new String[]{});
        Thread stdout = new LoggerThread(proc.getInputStream(), new PrintingLoggerSink("STD: "));
        Thread stderr = new LoggerThread(proc.getErrorStream(), new PrintingLoggerSink("ERR: "));
        stdout.start();
        stderr.start();
        stdout.join();
        stderr.join();
        int returnCode = proc.waitFor();
        if (returnCode != 0) {
            throw new IllegalStateException("Return code from create_test_database.sh is not 0 but " + returnCode);
        }
    }

    protected void assertRoutineResult(RoutineResult result) {
        Assert.assertNotNull("Routine result cannot be null", result);
        Assert.assertTrue("Routine result must be OK", result.isOk());
    }

    private static class LoggerThread extends Thread {
        private final Scanner scanner;
        private final LoggerSink loggerSink;

        public LoggerThread(InputStream is, LoggerSink loggerSink) {
            this.scanner = new Scanner(is, StandardCharsets.UTF_8);
            this.loggerSink = loggerSink;
        }

        public void run() {
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                loggerSink.log(line);
            }
        }
    }

    private static interface LoggerSink {
        void log(String line);
    }

    private static class PrintingLoggerSink implements LoggerSink {
        private final String prefix;

        public PrintingLoggerSink(String prefix) {
            this.prefix = prefix;
        }

        public void log(String line) {
            System.out.println(prefix + line);
        }
    }
}
