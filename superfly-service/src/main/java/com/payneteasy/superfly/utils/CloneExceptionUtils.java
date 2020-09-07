package com.payneteasy.superfly.utils;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clones exception.
 */
public class CloneExceptionUtils {
    private static Logger logger = LoggerFactory.getLogger(CloneExceptionUtils.class);

    @SuppressWarnings("unchecked")
    public static Throwable cloneException(Throwable aSourceException) {
        return cloneException(aSourceException, new Class[]{});
    }

    public static Throwable cloneException(Throwable aSourceException,
            Class<? extends Exception>[] nonConvertibleClasses) {
        boolean nonConvertible = false;
        for (Class<? extends Exception> clazz : nonConvertibleClasses) {
            if (clazz.isAssignableFrom(aSourceException.getClass())) {
                nonConvertible = true;
                break;
            }
        }
        Throwable clonedException;
        if (aSourceException.getCause() != null) {
            if (nonConvertible) {
                try {
                    Constructor<? extends Throwable> constructor = aSourceException.getClass().getConstructor(String.class, Throwable.class);
                    clonedException = (Throwable) constructor.newInstance(aSourceException.getMessage(), cloneException(aSourceException.getCause()));
                } catch (Exception e) {
                    // xxx
                    logger.error("Could not convert our own exception, that's programming error!", e);
                    clonedException = doCleanCopy(aSourceException);
                }
            } else {
                clonedException = doCleanCopy(aSourceException);
            }
        } else {
            if (nonConvertible) {
                try {
                    Constructor<? extends Throwable> constructor = aSourceException.getClass().getConstructor(String.class);
                    clonedException = (Throwable) constructor.newInstance(aSourceException.getMessage());
                } catch (Exception e) {
                    logger.error("Could not convert our own exception, that's programming error!", e);
                    clonedException = doCleanCopy(aSourceException);
                }
            } else {
                clonedException = new RuntimeException(aSourceException.getClass().getName()+": "+aSourceException.getMessage());
            }
        }

        clonedException.setStackTrace(cloneStackTrace(aSourceException));

        return clonedException;
    }

    private static RuntimeException doCleanCopy(Throwable aSourceException) {
        if (aSourceException.getCause() != null) {
            return new RuntimeException(
                    aSourceException.getClass().getName()+": "+aSourceException.getMessage(), cloneException(aSourceException.getCause())
            );
        } else {
            return new RuntimeException(aSourceException.getClass().getName()+": "+aSourceException.getMessage());
        }
    }

    private static StackTraceElement[] cloneStackTrace(Throwable aException) {
        StackTraceElement[] stack = aException.getStackTrace() ;
        StackTraceElement[] newStack = new StackTraceElement[stack.length];
        for (int i = 0; i < newStack.length; i++) {
            StackTraceElement element = stack[i];
            newStack[i] =
            new StackTraceElement(
                    element.getClassName(), element.getMethodName(), element.getFileName(), element.getLineNumber()
            );
        }
        return newStack ;
    }

}