package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class BundleApplicationResourcesFinder implements ApplicationResourcesFinder {
    private static final Logger log = Logger.getLogger(BundleApplicationResourcesFinder.class);

    @Override
    public Local find() {
        final NSBundle b = this.bundle();
        if(null == b) {
            log.warn("No main bundle found");
            return new TemporarySupportDirectoryFinder().find();
        }
        return LocalFactory.get(b.resourcePath());
    }

    public NSBundle bundle() {
        final NSBundle main = NSBundle.mainBundle();
        if(null == main) {
            return null;
        }
        Local executable = LocalFactory.get(main.executablePath());
        if(!executable.isSymbolicLink()) {
            return main;
        }
        while(executable.isSymbolicLink()) {
            try {
                executable = executable.getSymlinkTarget();
            }
            catch(NotfoundException e) {
                return main;
            }
        }
        Local folder = executable.getParent();
        NSBundle b;
        do {
            b = NSBundle.bundleWithPath(folder.getAbsolute());
            folder = folder.getParent();
        }
        while(b.executablePath() == null);
        return b;
    }
}
