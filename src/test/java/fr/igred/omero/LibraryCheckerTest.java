package fr.igred.omero;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class LibraryCheckerTest extends BasicTest {

    @Test
    public void checkAvailableLibraries() {
        assertTrue("Libraries are unavailable", LibraryChecker.areRequirementsAvailable());
    }


    @Test
    public void checkUnavailableLibraries() {
        try(URLClassLoader testClassLoader = new TestClassLoader()) {
            Class<?> checker = testClassLoader.loadClass("fr.igred.omero.LibraryChecker");
            Method check = checker.getMethod("areRequirementsAvailable");
            assertFalse("Libraries are available", (Boolean) check.invoke(null));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            fail(String.format("Class or method not found: %s.", e.getMessage()));
        } catch (InvocationTargetException | IllegalAccessException e) {
            fail(String.format("Call to check method failed: %s.", e.getMessage()));
        } catch (IOException e) {
            fail(String.format("Could not create TestClassLoader: %s.", e.getMessage()));
        }
    }


    private static class TestClassLoader extends URLClassLoader {

        TestClassLoader() throws MalformedURLException {
            super(new URL[0], getSystemClassLoader().getParent());
            String classpath = System.getProperty("java.class.path");
            String[] entries = classpath.split(File.pathSeparator);
            for (String entry : entries) {
                super.addURL(Paths.get(entry).toAbsolutePath().toUri().toURL());
            }
        }


        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (!name.startsWith("omero.") && !name.startsWith("ome.") && !name.startsWith("loci.")) {
                return super.loadClass(name);
            } else {
                throw new ClassNotFoundException("Disabled packages");
            }
        }

    }

}
