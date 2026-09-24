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


import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assumptions.assumeTrue;


public abstract class IntegrationTest extends BasicTest {

    protected static final TestObject ROOT     = new TestObject(0L, "root", null);
    protected static final TestObject USER1    = new TestObject(2L, "testUser", null);
    protected static final TestObject GROUP1   = new TestObject(3L, "testGroup", null);
    protected static final TestObject GROUP2   = new TestObject(4L, "testGroup1", null);
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

    protected static final File COMPOSE_FILE = new File("src" + File.separator +
                                                        "test" + File.separator +
                                                        "resources" + File.separator +
                                                        "docker-compose.yml");

    protected static final ComposeContainer CONTAINER = new ComposeContainer(COMPOSE_FILE);

    protected static final String HOST;
    protected static final int    PORT;

    static {
        start();
        PORT = CONTAINER.getServicePort("omero-server", 4064);
        HOST = CONTAINER.getServiceHost("omero-server", 4064);
        populate();
    }


    /**
     * Starts the Docker Compose environment and waits for the OMERO server to be ready.
     */
    private static void start() {
        logger.log(Level.INFO, "Starting Docker Compose environment...");
        // Wait for the OMERO server to be ready before populating data
        CONTAINER.withExposedService("omero-server", 4064,
                                     Wait.forHealthcheck()
                                         .withStartupTimeout(Duration.ofMinutes(5)));
        CONTAINER.start();
    }


    /**
     * Populates the OMERO server with test data.
     */
    private static void populate() {
        boolean populated = false;
        logger.log(Level.INFO, "Populating OMERO...");
        try {
            //noinspection HardcodedFileSeparator
            Container.ExecResult result = CONTAINER.getContainerByServiceName("omero-server")
                                                   .orElseThrow()
                                                   .execInContainer("sh", "/tmp/populate.sh");
            if (result.getExitCode() == 0) {
                populated = true;
                logger.log(Level.INFO, "Initialization successful!");
            }
            logger.log(Level.CONFIG, "Stdout: {0}", result.getStdout());
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Initialization failed!", e);
        }
        assumeTrue(populated, "Initialization failed!");
    }

}
