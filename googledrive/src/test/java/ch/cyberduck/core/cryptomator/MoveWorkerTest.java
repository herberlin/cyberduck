package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.cryptomator.features.CryptoBulkFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoReadFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveAttributesFinderFeature;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveDirectoryFeature;
import ch.cyberduck.core.googledrive.DriveFileidProvider;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveReadFeature;
import ch.cyberduck.core.googledrive.DriveTouchFeature;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.worker.CopyWorker;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MoveWorkerTest extends AbstractDriveTest {

    @Test
    public void testMoveSameFolderCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final Path source = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final byte[] content = RandomUtils.nextBytes(40500);
        final TransferStatus status = new TransferStatus();
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        new CryptoBulkFeature<>(session, new DisabledBulkFeature(), new DriveDeleteFeature(session, fileid), cryptomator).pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(source), status), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), new CryptoWriteFeature<>(session, new DriveWriteFeature(session, fileid), cryptomator).write(source, status.length(content.length), new DisabledConnectionCallback()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(source, new DisabledListProgressListener()));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), cache, new DisabledLoginCallback(), new DisabledProgressListener());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target, new DisabledListProgressListener()));
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        assertEquals(content.length, IOUtils.copy(new CryptoReadFeature(session, new DriveReadFeature(session, fileid), cryptomator).read(target, new TransferStatus().length(content.length), new DisabledConnectionCallback()), out));
        assertArrayEquals(content, out.toByteArray());
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(target, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final Path source = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<VersionId>(new DefaultUploadFeature<VersionId>(new DriveWriteFeature(session, fileid)), new DriveAttributesFinderFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(source, new DisabledListProgressListener()));
        final Path targetFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session, fileid), new DriveWriteFeature(session, fileid), cryptomator).mkdir(
            new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path target = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder, new DisabledListProgressListener()));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), cache, new DisabledLoginCallback(), new DisabledProgressListener());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolderLongFilenameCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final Path source = new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<VersionId>(new DefaultUploadFeature<VersionId>(new DriveWriteFeature(session, fileid)), new DriveAttributesFinderFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(source, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(source, new DisabledListProgressListener()));
        final Path targetFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session, fileid), new DriveWriteFeature(session, fileid), cryptomator).mkdir(
            new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(targetFolder, new DisabledListProgressListener()));
        final Path target = new Path(targetFolder, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(source, target), cache, new DisabledLoginCallback(), new DisabledProgressListener());
        worker.run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(source, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(target, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(target, targetFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFolder() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        final Path folder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session, fileid), new DriveWriteFeature(session, fileid), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(folder, new DisabledListProgressListener()));
        final Path file = new CryptoTouchFeature<>(session, new DefaultTouchFeature<VersionId>(new DefaultUploadFeature<VersionId>(new DriveWriteFeature(session, fileid)), new DriveAttributesFinderFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
            new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(file, new DisabledListProgressListener()));
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        new MoveWorker(Collections.singletonMap(file, fileRenamed), cache, new DisabledLoginCallback(), new DisabledProgressListener()).run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(file, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(fileRenamed, new DisabledListProgressListener()));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new MoveWorker(Collections.singletonMap(folder, folderRenamed), cache, new DisabledLoginCallback(), new DisabledProgressListener()).run(session);
        assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(folder, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(folderRenamed, new DisabledListProgressListener()));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamedInRenamedFolder, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveFileIntoVault() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path clearFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        new DriveTouchFeature(session, fileid).touch(clearFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(clearFile, new DisabledListProgressListener()));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session, fileid), new DriveWriteFeature(session, fileid), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        // move file into vault
        final Path encryptedFile = new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(clearFile, encryptedFile), cache, new DisabledLoginCallback(), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryIntoVault() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path clearFile = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        new DriveDirectoryFeature(session, fileid).mkdir(clearFolder, null, new TransferStatus());
        new DriveTouchFeature(session, fileid).touch(clearFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(clearFolder, new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(clearFile, new DisabledListProgressListener()));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        // move directory into vault
        final Path encryptedFolder = new Path(vault, clearFolder.getName(), EnumSet.of(Path.Type.directory));
        final Path encryptedFile = new Path(encryptedFolder, clearFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(clearFolder, encryptedFolder), new TestSessionPool(session), cache, new DisabledProgressListener(), new DisabledLoginCallback());
        worker.run(session);
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFile, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(encryptedFile, encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveFileOutsideVault() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path clearFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        new DriveDirectoryFeature(session, fileid).mkdir(clearFolder, null, new TransferStatus());
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session, fileid), new DriveWriteFeature(session, fileid), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        final Path encryptedFile = new CryptoTouchFeature<VersionId>(session, new DefaultTouchFeature<>(new DefaultUploadFeature<>(new DriveWriteFeature(session, fileid)), new DriveAttributesFinderFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
                new Path(encryptedFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(encryptedFile, new DisabledListProgressListener()));
        // move file outside vault
        final Path fileRenamed = new Path(clearFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFile, fileRenamed), cache, new DisabledLoginCallback(), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new DefaultFindFeature(session).find(fileRenamed, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(encryptedFolder, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(fileRenamed, clearFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    @Test
    public void testMoveDirectoryOutsideVault() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        final DefaultVaultRegistry registry = new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator);
        session.withRegistry(registry);
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        final Path encryptedFolder = new CryptoDirectoryFeature<>(session, new DriveDirectoryFeature(session, fileid), new DriveWriteFeature(session, fileid), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(encryptedFolder, new DisabledListProgressListener()));
        final String filename = new AlphanumericRandomStringService().random();
        final Path encryptedFile = new CryptoTouchFeature<VersionId>(session, new DefaultTouchFeature<>(new DefaultUploadFeature<>(new DriveWriteFeature(session, fileid)), new DriveAttributesFinderFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
                new Path(encryptedFolder, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(encryptedFile, new DisabledListProgressListener()));
        // move directory outside vault
        final Path directoryRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encryptedFolder, directoryRenamed), cache, new DisabledLoginCallback(), new DisabledProgressListener());
        worker.run(session);
        assertTrue(new DefaultFindFeature(session).find(directoryRenamed, new DisabledListProgressListener()));
        final Path fileRenamed = new Path(directoryRenamed, filename, EnumSet.of(Path.Type.file));
        assertTrue(new DefaultFindFeature(session).find(fileRenamed, new DisabledListProgressListener()));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Collections.singletonList(vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(fileRenamed, directoryRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        registry.clear();
    }

    private static class TestSessionPool implements SessionPool {
        private final Session<?> session;

        public TestSessionPool(final Session<?> session) {
            this.session = session;
        }

        @Override
        public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
            return session;
        }

        @Override
        public void release(final Session<?> session, final BackgroundException failure) {
        }

        @Override
        public void evict() {
        }

        @Override
        public Host getHost() {
            return session.getHost();
        }

        @Override
        public PathCache getCache() {
            return PathCache.empty();
        }

        @Override
        public VaultRegistry getVault() {
            return VaultRegistry.DISABLED;
        }

        @Override
        public Session.State getState() {
            return Session.State.open;
        }

        @Override
        public <T> T getFeature(final Class<T> type) {
            return session.getFeature(type);
        }

        @Override
        public void shutdown() {
        }
    }
}
