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


import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.logging.Logger;


@ExtendWith(LoggingExtension.class)
public abstract class BasicTest {

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE   = "\u001B[34m";

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


    protected static File createFile(String filename) throws IOException {
        final String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

        File file = new File(tmpdir + File.separator + filename);
        if (!file.createNewFile()) {
            logger.severe("\"" + file.getCanonicalPath() + "\" could not be created.");
        }
        return file;
    }


    protected static File createRandomFile(String filename) throws IOException {
        File file = createFile(filename);

        final byte[] array = new byte[2 * 262144 + 20];
        new SecureRandom().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        try (PrintStream out = new PrintStream(Files.newOutputStream(file.toPath()), false, "UTF-8")) {
            out.print(generatedString);
        }
        return file;
    }


    protected static void removeFile(File file) throws IOException {
        if (!file.delete()) {
            logger.severe("\"" + file.getCanonicalPath() + "\" could not be deleted.");
        }
    }

}
