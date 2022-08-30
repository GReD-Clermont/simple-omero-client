/*
 *  Copyright (C) 2020-2022 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero;


import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;
import java.util.logging.Logger;

import static fr.igred.omero.BasicTest.ANSI_BLUE;
import static fr.igred.omero.BasicTest.ANSI_GREEN;
import static fr.igred.omero.BasicTest.ANSI_RED;
import static fr.igred.omero.BasicTest.ANSI_RESET;
import static fr.igred.omero.BasicTest.ANSI_YELLOW;


public class LoggingExtension implements TestWatcher, BeforeTestExecutionCallback, BeforeAllCallback {

    private static final String FORMAT = "[%-43s]\t%s (%.3f s)";

    private long start = System.currentTimeMillis();

    private Logger logger;


    /**
     * Callback that is invoked once <em>before</em> all tests in the current container.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        //noinspection AccessOfSystemProperties
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        String klass = context.getRequiredTestClass().getSimpleName();
        logger = Logger.getLogger(klass);
    }


    /**
     * Callback that is invoked <em>immediately before</em> an individual test is executed but after any user-defined
     * setup methods have been executed for that test.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void beforeTestExecution(ExtensionContext context) {
        start = System.currentTimeMillis();
    }


    /**
     * Invoked after a disabled test has been skipped.
     *
     * <p>The default implementation does nothing. Concrete implementations can
     * override this method as appropriate.
     *
     * @param context the current extension context; never {@code null}
     * @param reason  the reason the test is disabled; never {@code null} but
     */
    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        float  time   = (float) (System.currentTimeMillis() - start) / 1000;
        String status = String.format("%sDISABLED%s", ANSI_BLUE, ANSI_RESET);
        logStatus(context.getRequiredTestMethod().getName(), context.getDisplayName(), status, time);
    }


    /**
     * Invoked after a test has completed successfully.
     *
     * <p>The default implementation does nothing. Concrete implementations can
     * override this method as appropriate.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void testSuccessful(ExtensionContext context) {
        float  time   = (float) (System.currentTimeMillis() - start) / 1000;
        String status = String.format("%sSUCCEEDED%s", ANSI_GREEN, ANSI_RESET);
        logStatus(context.getRequiredTestMethod().getName(), context.getDisplayName(), status, time);
    }


    /**
     * Invoked after a test has been aborted.
     *
     * <p>The default implementation does nothing. Concrete implementations can
     * override this method as appropriate.
     *
     * @param context the current extension context; never {@code null}
     * @param cause   the throwable responsible for the test being aborted; may be {@code null}
     */
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        float  time   = (float) (System.currentTimeMillis() - start) / 1000;
        String status = String.format("%sABORTED%s", ANSI_YELLOW, ANSI_RESET);
        logStatus(context.getRequiredTestMethod().getName(), context.getDisplayName(), status, time);
    }


    /**
     * Invoked after a test has failed.
     *
     * <p>The default implementation does nothing. Concrete implementations can
     * override this method as appropriate.
     *
     * @param context the current extension context; never {@code null}
     * @param cause   the throwable that caused test failure; may be {@code null}
     */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        float  time   = (float) (System.currentTimeMillis() - start) / 1000;
        String status = String.format("%sFAILED%s", ANSI_RED, ANSI_RESET);
        logStatus(context.getRequiredTestMethod().getName(), context.getDisplayName(), status, time);
    }


    /**
     * Logs test status.
     *
     * @param methodName  The method name.
     * @param displayName The test display name.
     * @param status      The test status.
     * @param time        The time it took to run.
     */
    private void logStatus(String methodName, String displayName, String status, float time) {
        displayName = displayName.equals(methodName + "()") ? "" : displayName;
        String name = String.format("%s %s", methodName, displayName);
        logger.info(String.format(FORMAT, name, status, time));
    }

}
