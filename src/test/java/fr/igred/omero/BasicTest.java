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


import loci.common.DebugTools;
import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;


@Ignore("Abstract class")
public abstract class BasicTest {

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    protected static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    protected static final String HOST = "omero";
    protected static final int    PORT = 4064;

    protected static final TestObject ROOT   = new TestObject(0L, "root", null);
    protected static final TestObject USER1  = new TestObject(2L, "testUser", null);
    protected static final TestObject GROUP1 = new TestObject(3L, "testGroup", null);

    protected static final TestObject PROJECT1 = new TestObject(1L, "TestProject", "description");
    protected static final TestObject DATASET1 = new TestObject(1L, "TestDataset", "description");
    protected static final TestObject DATASET2 = new TestObject(2L, "TestDatasetImport", "");
    protected static final TestObject IMAGE1   = new TestObject(1L, "image1.fake", "");
    protected static final TestObject IMAGE2   = new TestObject(3L, "image2.fake", "");
    protected static final TestObject SCREEN1  = new TestObject(1L, "TestScreen", "description");
    protected static final TestObject SCREEN2  = new TestObject(2L, "TestScreen2", "");
    protected static final TestObject PLATE1   = new TestObject(1L, "Plate Name 0", "Plate 0 of 1");
    protected static final TestObject PLATE2   = new TestObject(2L, "Plate Name 0", "Plate 0 of 2");
    protected static final TestObject TAG1     = new TestObject(1L, "tag1", "description");
    protected static final TestObject TAG2     = new TestObject(2L, "tag2", "");

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    }

    @Rule
    public final TestRule watcher = new Stopwatch();

    private long start = System.currentTimeMillis();


    long getStart() {
        return start;
    }


    void setStart(long start) {
        this.start = start;
    }


    private class Stopwatch extends TestWatcher {

        @Override
        protected void starting(Description description) {
            DebugTools.enableLogging("OFF");
            setStart(System.currentTimeMillis());
        }


        @Override
        protected void skipped(AssumptionViolatedException e, Description description) {
            float  time     = (float) (System.currentTimeMillis() - getStart()) / 1000;
            String status   = ANSI_YELLOW + "SKIPPED" + ANSI_RESET;
            String testName = description.getMethodName() + ":";
            logger.info(String.format("%-40s\t%s (%.3f s)", testName, status, time));
        }


        @Override
        protected void succeeded(Description description) {
            float  time     = (float) (System.currentTimeMillis() - getStart()) / 1000;
            String status   = ANSI_GREEN + "SUCCEEDED" + ANSI_RESET;
            String testName = description.getMethodName() + ":";
            logger.info(String.format("%-40s\t%s (%.3f s)", testName, status, time));
        }


        @Override
        protected void failed(Throwable e, Description description) {
            float  time     = (float) (System.currentTimeMillis() - getStart()) / 1000;
            String testName = description.getMethodName() + ":";
            String status   = ANSI_RED + "FAILED" + ANSI_RESET;
            logger.info(String.format("%-40s\t%s (%.3f s)", testName, status, time));
        }

    }

}
