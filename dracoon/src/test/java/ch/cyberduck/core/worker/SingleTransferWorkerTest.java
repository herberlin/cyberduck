package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.sds.AbstractSDSTest;
import ch.cyberduck.core.sds.SDSAttributesFinderFeature;
import ch.cyberduck.core.sds.SDSDeleteFeature;
import ch.cyberduck.core.sds.SDSDirectoryFeature;
import ch.cyberduck.core.sds.SDSNodeIdProvider;
import ch.cyberduck.core.sds.SDSWriteFeature;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SingleTransferWorkerTest extends AbstractSDSTest {

    @Test
    public void testDownloadVersioned() throws Exception {
        final SDSNodeIdProvider fileid = new SDSNodeIdProvider(session);
        final Path home = new SDSDirectoryFeature(session, fileid).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local localFile = TemporaryFileServiceFactory.get().create(test);
        {
            final byte[] content = RandomUtils.nextBytes(39864);
            final TransferStatus writeStatus = new TransferStatus().length(content.length).withChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
            final StatusOutputStream<VersionId> out = new SDSWriteFeature(session, fileid).write(test, writeStatus, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(writeStatus, writeStatus).withLimit((long) content.length).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final byte[] content = RandomUtils.nextBytes(39864);
        final TransferStatus writeStatus = new TransferStatus().exists(true).length(content.length).withChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()));
        final StatusOutputStream<VersionId> out = new SDSWriteFeature(session, fileid).write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(writeStatus, writeStatus).withLimit((long) content.length).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final String versionId = out.getStatus().id;
        assertEquals(versionId, new SDSAttributesFinderFeature(session, fileid).find(test, new DisabledListProgressListener()).getVersionId());
        assertEquals(versionId, new DefaultAttributesFinderFeature(session).find(test, new DisabledListProgressListener()).getVersionId());
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), Collections.singletonList(new TransferItem(test, localFile)), new NullFilter<>());
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), new DisabledStreamListener(), new DisabledLoginCallback(), new DisabledPasswordCallback(), new DisabledNotificationService()) {

        }.run(session, session));
        byte[] compare = new byte[content.length];
        assertArrayEquals(content, IOUtils.toByteArray(localFile.getInputStream()));
        test.attributes().setVersionId(versionId);
        assertEquals(versionId, new DefaultAttributesFinderFeature(session).find(test, new DisabledListProgressListener()).getVersionId());
        new SDSDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        localFile.delete();
        session.close();
    }
}
