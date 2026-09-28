/*
 *  Copyright (C) 2020-2025 GReD
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


import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;
import static java.util.logging.Logger.getLogger;


@ExtendWith(LoggingExtension.class)
public abstract class BasicTest {

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE   = "\u001B[34m";

    protected static final Logger logger = getLogger(MethodHandles.lookup()
                                                                  .lookupClass()
                                                                  .getName());

    protected static final double DOUBLE_PRECISION = 10.0e-15;

    protected static final Random SECURE_RANDOM = new SecureRandom();


    protected static File createFile(String filename) throws IOException {
        String tmpdir = Files.createTempDirectory(null).toString();

        File file = new File(tmpdir + File.separator + filename);
        if (!file.createNewFile()) {
            String template = "\"%s\" could not be created.";
            logger.severe(format(template, file.getCanonicalPath()));
        }
        return file;
    }


    protected static File createRandomFile(String filename) throws IOException {
        final int size = 2 * 262144 + 20;

        File   file  = createFile(filename);
        byte[] array = new byte[size];
        SECURE_RANDOM.nextBytes(array);
        String generatedString = new String(array, UTF_8);
        try (PrintStream out = new PrintStream(newOutputStream(file.toPath()), false, UTF_8)) {
            out.print(generatedString);
        }
        return file;
    }


    protected static void removeFile(File file) throws IOException {
        File parent = file.getParentFile();
        if (file.delete()) {
            if (!parent.delete()) {
                String template = "\"%s\" could not be deleted.";
                logger.warning(format(template, parent.getCanonicalPath()));
            }
        } else {
            String template = "\"%s\" could not be deleted.";
            logger.severe(format(template, file.getCanonicalPath()));
        }
    }

}
