package ch.cyberduck.core.s3;

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Revert;
import ch.cyberduck.core.features.Versioning;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3SessionTest extends AbstractTestCase {

    @Test
    public void testFile() {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, "h"));
        assertFalse(session.isCreateFileSupported(new S3Path(session, null, "/", Path.VOLUME_TYPE)));
        assertTrue(session.isCreateFileSupported(new S3Path(session, new S3Path(session, null, "/", Path.VOLUME_TYPE), "/container", Path.VOLUME_TYPE)));
    }

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertFalse(session.cache().isEmpty());
        assertTrue(session.cache().containsKey(new S3Path(session, "/", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertNotNull(session.cache().lookup(new S3Path(session, "/test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testConnectCnameAnonymous() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, "dist.springframework.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }


    @Test
    public void testConnectBuckenameAnonymous() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, "dist.springframework.org.s3.amazonaws.com", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testFeatures() throws Exception {
        final S3Session aws = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertNotNull(aws.getFeature(Versioning.class, null));
        assertNotNull(aws.getFeature(AnalyticsProvider.class, null));
        assertNotNull(aws.getFeature(Lifecycle.class, null));
        assertNotNull(aws.getFeature(Location.class, null));
        assertNotNull(aws.getFeature(Revert.class, null));
        final S3Session o = new S3Session(new Host(Protocol.S3_SSL, "o"));
        assertNull(o.getFeature(Versioning.class, null));
        assertNull(o.getFeature(AnalyticsProvider.class, null));
        assertNull(o.getFeature(Lifecycle.class, null));
        assertNull(o.getFeature(Location.class, null));
        assertNull(o.getFeature(Revert.class, null));
    }

    @Test
    public void testUri() throws Exception {
        final S3Session aws = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals("https://test.cyberduck.ch.s3.amazonaws.com/key", aws.toURL(new S3Path(aws, "/test.cyberduck.ch/key", Path.FILE_TYPE)));
    }

    @Test
    public void testHttpUri() throws Exception {
        final S3Session aws = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals("http://test.cyberduck.ch.s3.amazonaws.com/key", aws.toHttpURL(new S3Path(aws, "/test.cyberduck.ch/key", Path.FILE_TYPE)));
    }
}
