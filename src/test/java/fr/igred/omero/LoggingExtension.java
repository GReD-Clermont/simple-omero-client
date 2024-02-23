/*
 *  Copyright (C) 2020-2024 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero;


import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Logger;

import static fr.igred.omero.BasicTest.ANSI_BLUE;
import static fr.igred.omero.BasicTest.ANSI_GREEN;
import static fr.igred.omero.BasicTest.ANSI_RED;
import static fr.igred.omero.BasicTest.ANSI_RESET;
import static fr.igred.omero.BasicTest.ANSI_YELLOW;


public class LoggingExtension implements TestWatcher, BeforeTestExecutionCallback, BeforeAllCallback, AfterAllCallback {

    private static final String FORMAT = "[%-43s]\t%s%-9s%s (%.3f s)";

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static final PrintStream error = System.err;

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static final PrintStream output = System.out;

    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private PrintStream logFile;

    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private Logger logger;

    private long start = System.currentTimeMillis();


    /**
     * Callback that is invoked once <em>before</em> all tests in the current container.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void beforeAll(ExtensionContext context) throws IOException {
        //noinspection AccessOfSystemProperties
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        String klass = context.getRequiredTestClass().getSimpleName();
        logger = Logger.getLogger(klass);

        File file = new File("target" + File.separator + "logs" + File.separator + klass + ".log");
        Files.createDirectories(file.toPath().getParent());
        Files.deleteIfExists(file.toPath());
        logFile = new PrintStream(Files.newOutputStream(file.toPath()), false, StandardCharsets.UTF_8.name());
    }


    /**
     * Callback that is invoked once <em>after</em> all tests in the current container.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void afterAll(ExtensionContext context) {
        logFile.close();
    }


    /**
     * Callback that is invoked <em>immediately before</em> an individual test is executed but after any user-defined
     * setup methods have been executed for that test.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void beforeTestExecution(ExtensionContext context) {
        hideOutputs();
        String methodName  = context.getRequiredTestMethod().getName();
        String displayName = context.getDisplayName();
        displayName = displayName.equals(methodName + "()") ? "" : displayName;
        logFile.printf("%9s: %s %s%n", "STARTING", methodName, displayName);
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
        float time = (float) (System.currentTimeMillis() - start) / 1000;
        logStatus(context.getRequiredTestMethod().getName(), context.getDisplayName(), "DISABLED", ANSI_BLUE, time);
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
        float time = (float) (System.currentTimeMillis() - start) / 1000;
        logStatus(context.getRequiredTestMethod().getName(), context.getDisplayName(), "SUCCEEDED", ANSI_GREEN, time);
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
        float time = (float) (System.currentTimeMillis() - start) / 1000;
        logStatus(context.getRequiredTestMethod().getName(), context.getDisplayName(), "ABORTED", ANSI_YELLOW, time);
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
        float time = (float) (System.currentTimeMillis() - start) / 1000;
        logStatus(context.getRequiredTestMethod().getName(), context.getDisplayName(), "FAILED", ANSI_RED, time);
    }


    /**
     * Logs test status.
     *
     * @param methodName  The method name.
     * @param displayName The test display name.
     * @param status      The test status.
     * @param time        The time it took to run.
     */
    private void logStatus(String methodName, String displayName, String status, String color, float time) {
        showOutputs();
        displayName = displayName.equals(methodName + "()") ? "" : displayName;
        String name = String.format("%s %s", methodName, displayName);
        logger.info(String.format(FORMAT, name, color, status, ANSI_RESET, time));
        logFile.printf("%9s: %s%n", status, name);
    }


    /**
     * Output to console.
     */
    private static void showOutputs() {
        System.setOut(output);
        System.setErr(error);
    }


    /**
     * Output to file.
     */
    private void hideOutputs() {
        System.setOut(logFile);
        System.setErr(logFile);
    }


    @Override
    public String toString() {
        return "LoggingExtension{" +
               "start=" + start +
               "}";
    }

}
