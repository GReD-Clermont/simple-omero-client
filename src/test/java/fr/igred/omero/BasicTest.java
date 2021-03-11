package fr.igred.omero;


import loci.common.DebugTools;
import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;


public abstract class BasicTest {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            System.out.println(description.getMethodName() + ": " +
                               ANSI_CYAN +
                               "STARTED" +
                               ANSI_RESET);
            DebugTools.enableLogging("OFF");
        }


        @Override
        protected void skipped(AssumptionViolatedException e, Description description) {
            System.out.println(description.getMethodName() + ": " +
                               ANSI_YELLOW +
                               "SKIPPED" +
                               ANSI_RESET);
        }


        @Override
        protected void succeeded(Description description) {
            System.out.println(description.getMethodName() + ": " +
                               ANSI_GREEN +
                               "SUCCEEDED" +
                               ANSI_RESET);
        }


        @Override
        protected void failed(Throwable e, Description description) {
            System.out.println(description.getMethodName() + ": " +
                               ANSI_RED +
                               "FAILED" +
                               ANSI_RESET);
        }
    };

}
