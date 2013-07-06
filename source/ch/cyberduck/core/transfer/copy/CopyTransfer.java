package ch.cyberduck.core.transfer.copy;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractStreamListener;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.normalizer.CopyRootPathsNormalizer;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class CopyTransfer extends Transfer {
    private static final Logger log = Logger.getLogger(CopyTransfer.class);

    /**
     * Mapping source to destination files
     */
    protected Map<Path, Path> files = Collections.emptyMap();

    private Session destination;

    /**
     * @param files Source to destination mapping
     */
    public CopyTransfer(final Map<Path, Path> files) {
        this(new CopyRootPathsNormalizer().normalize(files),
                new BandwidthThrottle(Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
    }

    private CopyTransfer(final Map<Path, Path> selected, final BandwidthThrottle bandwidth) {
        super(new ArrayList<Path>(selected.keySet()), bandwidth);
        destination = SessionFactory.createSession(selected.values().iterator().next().getSession().getHost());
        files = new HashMap<Path, Path>();
        for(Map.Entry<Path, Path> e : selected.entrySet()) {
            files.put(e.getKey(), PathFactory.createPath(destination, e.getValue().getAsDictionary()));
        }
    }

    public <T> CopyTransfer(T serialized, Session s) {
        super(serialized, s, new BandwidthThrottle(Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        Object hostObj = dict.objectForKey("Destination");
        if(hostObj != null) {
            destination = SessionFactory.createSession(new Host(hostObj));
        }
        final List destinationsObj = dict.listForKey("Destinations");
        if(destinationsObj != null) {
            this.files = new HashMap<Path, Path>();
            final List<Path> roots = this.getRoots();
            if(destinationsObj.size() == roots.size()) {
                for(int i = 0; i < roots.size(); i++) {
                    this.files.put(roots.get(i), PathFactory.createPath(destination, destinationsObj.get(i)));
                }
            }
        }
    }

    @Override
    public boolean isResumable() {
        return false;
    }

    @Override
    public boolean isReloadable() {
        return true;
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = super.getSerializer();
        dict.setStringForKey(String.valueOf(KIND_COPY), "Kind");
        if(destination != null) {
            dict.setObjectForKey(destination.getHost(), "Destination");
        }
        List<Path> targets = new ArrayList<Path>();
        for(Path root : this.getRoots()) {
            if(files.containsKey(root)) {
                targets.add(files.get(root));
            }
        }
        dict.setListForKey(new ArrayList<Serializable>(targets), "Destinations");
        return dict.getSerialized();
    }

    @Override
    public List<Session<?>> getSessions() {
        final ArrayList<Session<?>> sessions = new ArrayList<Session<?>>(super.getSessions());
        if(destination != null) {
            sessions.add(destination);
        }
        return sessions;
    }

    @Override
    public TransferAction action(boolean resumeRequested, boolean reloadRequested) {
        return TransferAction.ACTION_OVERWRITE;
    }

    @Override
    public TransferPathFilter filter(TransferPrompt prompt, final TransferAction action) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return new CopyTransferFilter(destination, files);
        }
        return super.filter(prompt, action);
    }

    @Override
    public AttributedList<Path> children(final Path parent) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List children for %s", parent));
        }
        if(parent.attributes().isSymbolicLink()
                && new DownloadSymlinkResolver(this.getRoots()).resolve(parent)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Do not list children for symbolic link %s", parent));
            }
            return AttributedList.emptyList();
        }
        else {
            final AttributedList<Path> list = parent.list();
            final Path copy = files.get(parent);
            for(Path p : list) {
                files.put(p, PathFactory.createPath(destination, copy, p.getName(), p.attributes().getType()));
            }
            return list;
        }
    }

    @Override
    public void transfer(final Path source, final TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", source, options));
        }
        final Path copy = files.get(source);
        if(source.attributes().isFile()) {
            session.message(MessageFormat.format(Locale.localizedString("Copying {0} to {1}", "Status"),
                    source.getName(), copy));
            final Copy feature = session.getFeature(Copy.class, null);
            if(feature != null) {
                feature.copy(source, copy);
                addTransferred(source.attributes().getSize());
                status.setComplete();
            }
            else {
                source.copy(copy, bandwidth, new AbstractStreamListener() {
                    @Override
                    public void bytesSent(long bytes) {
                        addTransferred(bytes);
                    }
                }, status);
            }
        }
        else {
            session.message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                    this.getName()));
            copy.mkdir();
        }
    }

    @Override
    public String getName() {
        return MessageFormat.format(Locale.localizedString("Copying {0} to {1}", "Status"),
                files.keySet().iterator().next().getName(), files.values().iterator().next().getName());
    }

    @Override
    public String getStatus() {
        return this.isComplete() ? "Copy complete" : "Transfer incomplete";
    }

    @Override
    public String getLocal() {
        return null;
    }

    @Override
    public String getImage() {
        return "transfer-upload.tiff";
    }
}