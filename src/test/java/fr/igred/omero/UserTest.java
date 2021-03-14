package fr.igred.omero;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import static org.junit.Assert.assertEquals;


@Ignore
public abstract class UserTest extends BasicTest {

    protected Client client;


    @Before
    public void setUp() {
        boolean failed = false;
        client = new Client();
        try {
            client.connect("omero", 4064, "testUser", "password", 3L);
            assertEquals("Wrong user", 2L, client.getId().longValue());
            assertEquals("Wrong group", 3L, client.getGroupId().longValue());
        } catch (Exception e) {
            failed = true;
            logger.severe(ANSI_RED + "Connection failed." + ANSI_RESET);
        }
        org.junit.Assume.assumeFalse(failed);
    }


    @After
    public void cleanUp() {
        try {
            client.disconnect();
        } catch (Exception e) {
            logger.warning(ANSI_YELLOW + "Disconnection failed." + ANSI_RESET);
        }
    }

}
