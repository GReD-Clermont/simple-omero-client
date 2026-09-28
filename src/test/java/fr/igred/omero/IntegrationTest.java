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


import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.io.IOException;
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

    protected static final String HOST;
    protected static final int    PORT;

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");
    @SuppressWarnings("HardcodedFileSeparator")
    private static final GenericContainer<?> OMERO    = new GenericContainer<>("openmicroscopy/omero-server:latest");

    private static final String INIT_SCRIPT = "populate.sh";
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String INIT_PATH   = "/tmp/" + INIT_SCRIPT;

    private static final boolean REUSE = TestcontainersConfiguration.getInstance().environmentSupportsReuse();

    static {
        start();
        PORT = OMERO.getMappedPort(4064);
        HOST = OMERO.getHost();
        populate();
    }


    /**
     * Starts the Docker Compose environment and waits for the OMERO server to be ready.
     */
    private static void start() {
        String dbName = "omero";
        String dbUser = "omero";
        String dbPass = "omero";

        String postgresHost = "postgres";
        String omeroHost    = "omero";

        //noinspection HardcodedFileSeparator
        String startupCheckCmd = "/opt/omero/server/OMERO.server/bin/omero admin status";

        logger.log(Level.INFO, "Starting Postgres container...");
        POSTGRES.withDatabaseName(dbName)
                .withNetworkAliases(postgresHost)
                .withUsername(dbUser)
                .withPassword(dbPass)
                .withReuse(REUSE);
        POSTGRES.start();

        Integer postgresPort = POSTGRES.getMappedPort(5432);
        Testcontainers.exposeHostPorts(postgresPort);

        logger.log(Level.INFO, "Starting OMERO container...");
        OMERO.dependsOn(POSTGRES)
             .withNetworkAliases(omeroHost)
             .withCopyFileToContainer(MountableFile.forClasspathResource(INIT_SCRIPT), INIT_PATH)
             .withEnv("CONFIG_omero_db_name", dbName)
             .withEnv("CONFIG_omero_db_user", dbUser)
             .withEnv("CONFIG_omero_db_pass", dbPass)
             .withEnv("CONFIG_omero_db_host", postgresHost)
             .withExposedPorts(4064)
             .waitingFor(Wait.forSuccessfulCommand(startupCheckCmd))
             .withReuse(REUSE);
        OMERO.addLink(POSTGRES, postgresHost);
        OMERO.start();
    }


    /**
     * Populates the OMERO server with test data.
     */
    private static void populate() {
        boolean populated = false;
        //noinspection HardcodedFileSeparator
        String witnessFile = "/tmp/.populated";
        try {
            var initCheck = OMERO.execInContainer("test", "-f", witnessFile);
            if (initCheck.getExitCode() == 0) {
                populated = true;
                logger.log(Level.CONFIG, "OMERO is already populated.");
            } else {
                logger.log(Level.INFO, "Populating OMERO...");
                var result = OMERO.execInContainer("sh", INIT_PATH);
                if (result.getExitCode() == 0) {
                    populated = true;
                    OMERO.execInContainer("touch", witnessFile);
                    logger.log(Level.INFO, "Initialization successful!");
                }
                logger.log(Level.CONFIG, "Stdout: {0}", result.getStdout());
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Init command failed!", e);
        }
        assumeTrue(populated, "Initialization failed!");
    }

}
