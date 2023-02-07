/*
 *  Copyright (C) 2020-2023 GReD
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

package fr.igred.omero.util;


import fr.igred.omero.BasicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


class LibraryCheckerTest extends BasicTest {

    @Test
    void checkAvailableLibraries() {
        assertTrue(LibraryChecker.areRequirementsAvailable(), "Libraries are unavailable");
    }


    @ParameterizedTest
    @ValueSource(strings = {"omero-gateway",
                            "omero-model",
                            "omero-blitz",
                            "ome-xml",
                            "ome-common",
                            "formats-api"})
    void checkUnavailableLibraries(String excluded) {
        //noinspection ClassLoaderInstantiation
        try (URLClassLoader testClassLoader = new TestClassLoader(excluded)) {
            Class<?> checker = testClassLoader.loadClass("fr.igred.omero.util.LibraryChecker");
            Method   check   = checker.getMethod("areRequirementsAvailable");
            assertFalse((Boolean) check.invoke(null), "Libraries are available");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            fail(String.format("Class or method not found: %s.", e.getMessage()));
        } catch (InvocationTargetException | IllegalAccessException e) {
            fail(String.format("Call to check method failed: %s.", e.getMessage()));
        } catch (IOException e) {
            fail(String.format("Could not create TestClassLoader: %s.", e.getMessage()));
        }
    }


    @SuppressWarnings("CustomClassloader")
    private static class TestClassLoader extends URLClassLoader {

        TestClassLoader(CharSequence excluded) throws MalformedURLException {
            //noinspection ZeroLengthArrayAllocation
            super(new URL[0], getSystemClassLoader().getParent());
            @SuppressWarnings("AccessOfSystemProperties")
            String classpath = System.getProperty("java.class.path");
            String[] entries = classpath.split(File.pathSeparator);
            for (String entry : entries) {
                if (!entry.contains(excluded)) {
                    super.addURL(Paths.get(entry).toAbsolutePath().toUri().toURL());
                }
            }
        }

    }

}
