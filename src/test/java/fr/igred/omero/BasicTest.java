/*
 *  Copyright (C) 2020-2021 GReD
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


import loci.common.DebugTools;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.logging.Logger;


@Ignore
public abstract class BasicTest {

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    }

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private long start;


    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            DebugTools.enableLogging("OFF");
            start = System.currentTimeMillis();
        }


        @Override
        protected void skipped(AssumptionViolatedException e, Description description) {
            float  time     = (float) (System.currentTimeMillis() - start) / 1000;
            String status   = ANSI_YELLOW + "SKIPPED" + ANSI_RESET;
            String testName = description.getMethodName() + ":";
            logger.info(String.format("%-40s\t%s (%.3f s)", testName, status, time));
        }


        @Override
        protected void succeeded(Description description) {
            float  time     = (float) (System.currentTimeMillis() - start) / 1000;
            String status   = ANSI_GREEN + "SUCCEEDED" + ANSI_RESET;
            String testName = description.getMethodName() + ":";
            logger.info(String.format("%-40s\t%s (%.3f s)", testName, status, time));
        }


        @Override
        protected void failed(Throwable e, Description description) {
            float  time     = (float) (System.currentTimeMillis() - start) / 1000;
            String testName = description.getMethodName() + ":";
            String status   = ANSI_RED + "FAILED" + ANSI_RESET;
            logger.info(String.format("%-40s\t%s (%.3f s)", testName, status, time));
        }
    };

}
