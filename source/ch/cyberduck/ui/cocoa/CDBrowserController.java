package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.growl.Growl;
import ch.cyberduck.ui.cocoa.odb.Editor;

/**
 * @version $Id$
 */
public class CDBrowserController extends CDController implements Observer {
    private static Logger log = Logger.getLogger(CDBrowserController.class);

    private static final File HISTORY_FOLDER = new File(
            NSPathUtilities.stringByExpandingTildeInPath(
                    "~/Library/Application Support/Cyberduck/History"));

    static {
        HISTORY_FOLDER.mkdirs();
    }

    /**
     * Keep references of controller objects because otherweise they get garbage collected
     * if not referenced here.
     */
    private static NSMutableArray instances = new NSMutableArray();

    // ----------------------------------------------------------
    // Applescriptability
    // ----------------------------------------------------------

    public NSScriptObjectSpecifier objectSpecifier() {
        log.debug("objectSpecifier");
        NSArray orderedDocs = (NSArray) NSKeyValue.valueForKey(NSApplication.sharedApplication(), "orderedBrowsers");
        int index = orderedDocs.indexOfObject(this);
        if ((index >= 0) && (index < orderedDocs.count())) {
            NSScriptClassDescription desc = (NSScriptClassDescription) NSScriptClassDescription.classDescriptionForClass(NSApplication.class);
            return new NSIndexSpecifier(desc, null, "orderedBrowsers", index);
        }
        return null;
    }

    public Object handleMountScriptCommand(NSScriptCommand command) {
        log.debug("handleMountScriptCommand:" + command);
        NSDictionary args = command.evaluatedArguments();
        Host host = null;
        Object portObj = args.objectForKey("Port");
        if (portObj != null) {
            Object protocolObj = args.objectForKey("Protocol");
            if (protocolObj != null) {
                host = new Host((String) args.objectForKey("Protocol"),
                        (String) args.objectForKey("Host"),
                        Integer.parseInt((String) args.objectForKey("Port")));
            }
            else {
                host = new Host((String) args.objectForKey("Host"),
                        Integer.parseInt((String) args.objectForKey("Port")));
            }
        }
        else {
            Object protocolObj = args.objectForKey("Protocol");
            if (protocolObj != null) {
                host = new Host((String) args.objectForKey("Protocol"),
                        (String) args.objectForKey("Host"));
            }
            else {
                host = new Host((String) args.objectForKey("Host"));
            }
        }
        Object pathObj = args.objectForKey("InitialPath");
        if (pathObj != null) {
            host.setDefaultPath((String) args.objectForKey("InitialPath"));
        }
        Object userObj = args.objectForKey("Username");
        if (userObj != null) {
            host.setCredentials((String) args.objectForKey("Username"), (String) args.objectForKey("Password"));
        }
        Session session = SessionFactory.createSession(host);
        session.addObserver((Observer) this);
        session.mount(this.encoding, this.getFileFilter());
        return null;
    }

    public Object handleCloseScriptCommand(NSScriptCommand command) {
        log.debug("handleCloseScriptCommand:" + command);
        this.unmount();
        this.window().close();
        return null;
    }

    public Object handleDisconnectScriptCommand(NSScriptCommand command) {
        log.debug("handleDisconnectScriptCommand:" + command);
        this.unmount();
        return null;
    }

    public NSArray handleListScriptCommand(NSScriptCommand command) {
        log.debug("handleListScriptCommand:" + command);
        NSMutableArray result = new NSMutableArray();
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Object pathObj = args.objectForKey("Path");
            Path path = this.workdir();
            if (pathObj != null) {
                String folder = (String) args.objectForKey("Path");
                if (folder.charAt(0) == '/') {
                    path = PathFactory.createPath(this.workdir().getSession(),
                            folder);
                }
                else {
                    path = PathFactory.createPath(this.workdir().getSession(),
                            this.workdir().getAbsolute(),
                            folder);
                }
            }
            for (Iterator i = path.list(this.encoding, false, this.getFileFilter()).iterator(); i.hasNext();) {
                result.addObject(((Path) i.next()).getName());
            }
        }
        return result;
    }

    public Object handleGotoScriptCommand(NSScriptCommand command) {
        log.debug("handleGotoScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDGotoController c = new CDGotoController(this.workdir());
            c.gotoFolder(this.workdir(), (String) args.objectForKey("Path"));
        }
        return null;
    }

    public Object handleCreateFolderScriptCommand(NSScriptCommand command) {
        log.debug("handleCreateFolderScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDFolderController c = new CDFolderController();
            c.create(this.workdir(), (String) args.objectForKey("Path"));
        }
        return null;
    }

    public Object handleExistsScriptCommand(NSScriptCommand command) {
        log.debug("handleExistsScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.workdir().getSession(),
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            return new Integer(path.exists() ? 1 : 0);
        }
        return new Integer(0);
    }

    public Object handleCreateFileScriptCommand(NSScriptCommand command) {
        log.debug("handleCreateFileScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            CDCreateFileController c = new CDCreateFileController();
            c.create(this.workdir(), (String) args.objectForKey("Path"));
        }
        return null;
    }

    public Object handleEditScriptCommand(NSScriptCommand command) {
        log.debug("handleEditScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.workdir().getSession(),
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            Editor editor = new Editor();
            editor.open(path);
        }
        return null;
    }

    public Object handleDeleteScriptCommand(NSScriptCommand command) {
        log.debug("handleDeleteScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            Path path = PathFactory.createPath(this.workdir().getSession(),
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            path.delete();
            path.getParent().list(this.encoding, true, this.getFileFilter());
        }
        return null;
    }

    public Object handleRefreshScriptCommand(NSScriptCommand command) {
        log.debug("handleRefreshScriptCommand:" + command);
        if (this.isMounted()) {
            this.reloadButtonClicked(null);
        }
        return null;
    }

    public Object handleSyncScriptCommand(NSScriptCommand command) {
        log.debug("handleSyncScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.workdir().getSession(),
                    (String) args.objectForKey("Path"));
            path.setLocal(new Local((String) args.objectForKey("Local")));
            path.attributes.setType(Path.DIRECTORY_TYPE);
            for (Iterator i = new SyncQueue(path).getChilds().iterator(); i.hasNext();) {
                ((Path) i.next()).sync();
            }
			Growl.instance().notify(NSBundle.localizedString("Synchronization complete",
															 "Growl Notification"),
									path.getName());
        }
        return null;
    }

    public Object handleDownloadScriptCommand(NSScriptCommand command) {
        log.debug("handleDownloadScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.workdir().getSession(),
                    this.workdir().getAbsolute(),
                    (String) args.objectForKey("Path"));
            path.attributes.setType(Path.FILE_TYPE);
            Object localObj = args.objectForKey("Local");
            if (localObj != null) {
                path.setLocal(new Local((String) localObj, path.getName()));
            }
            Object nameObj = args.objectForKey("Name");
            if (nameObj != null) {
                path.setLocal(new Local(path.getLocal().getParent(), (String) nameObj));
            }
            for (Iterator i = new DownloadQueue(path).getChilds().iterator(); i.hasNext();) {
                ((Path) i.next()).download();
            }
			Growl.instance().notify(NSBundle.localizedString("Download complete",
															 "Growl Notification"),
									path.getName());
        }
        return null;
    }

    public Object handleUploadScriptCommand(NSScriptCommand command) {
        log.debug("handleUploadScriptCommand:" + command);
        if (this.isMounted()) {
            NSDictionary args = command.evaluatedArguments();
            final Path path = PathFactory.createPath(this.workdir().getSession(),
                    this.workdir().getAbsolute(),
                    new Local((String) args.objectForKey("Path")));
            path.attributes.setType(Path.FILE_TYPE);
            Object remoteObj = args.objectForKey("Remote");
            if (remoteObj != null) {
                path.setPath((String) remoteObj, path.getName());
            }
            Object nameObj = args.objectForKey("Name");
            if (nameObj != null) {
                path.setPath(this.workdir().getAbsolute(), (String) nameObj);
            }
            for (Iterator i = new UploadQueue(path).getChilds().iterator(); i.hasNext();) {
                ((Path) i.next()).upload();
            }
			Growl.instance().notify(NSBundle.localizedString("Upload complete",
															 "Growl Notification"),
									path.getName());
        }
        return null;
    }

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------

    public CDBrowserController() {
        instances.addObject(this);
        if (false == NSApplication.loadNibNamed("Browser", this)) {
            log.fatal("Couldn't load Browser.nib");
        }
    }

    public static CDBrowserController controllerForWindow(NSWindow window) {
        if (window.isVisible()) {
            Object delegate = window.delegate();
            if (delegate != null && delegate instanceof CDBrowserController) {
                return (CDBrowserController) delegate;
            }
        }
        return null;
    }

    public static void updateBrowserTableAttributes() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while (0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                controller._updateBrowserListTableAttributes();
                controller._updateBrowserOutlineTableAttributes();
            }
        }
    }

    public static void updateBrowserTableColumns() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        while (0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                controller._updateBrowserTableColumns();
            }
        }
    }

    public void awakeFromNib() {
        log.debug("awakeFromNib");
        this._updateBrowserTableColumns();

        // Configure window
        this.window().setTitle("Cyberduck " + NSBundle.bundleForClass(this.getClass()).objectForInfoDictionaryKey("CFBundleVersion"));
        this.window().setInitialFirstResponder(quickConnectPopup);
        // Drawer states
        if (Preferences.instance().getBoolean("bookmarkDrawer.isOpen")) {
            this.bookmarkDrawer.open();
        }
        // Configure Toolbar
        this.toolbar = new NSToolbar("Cyberduck Toolbar");
        this.toolbar.setDelegate(this);
        this.toolbar.setAllowsUserCustomization(true);
        this.toolbar.setAutosavesConfiguration(true);
        this.window().setToolbar(toolbar);

        this.browserSwitchClicked(this.browserSwitchView);
		this.window().makeFirstResponder(this.quickConnectPopup);
	}
	
	private String encoding = Preferences.instance().getProperty("browser.charset.encoding");
	
    protected String encoding() {
        return this.encoding;
    }
		
	private Filter filenameFilter;
	
	{
		if(Preferences.instance().getBoolean("browser.showHidden"))
			filenameFilter = new NullFilter();
		else 
			filenameFilter = new HiddenFilesFilter();
	}
	
    protected Filter getFileFilter() {
		return this.filenameFilter;
    }
	
    private CDInfoController inspector = null;
	
	private void getFocus() {
		log.debug("getFocus");
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.window().makeFirstResponder(this.browserListView);
				break;
			}
			case OUTLINE_VIEW: {
				this.window().makeFirstResponder(this.browserOutlineView);
				break;
			}
			case COLUMN_VIEW: {
				this.window().makeFirstResponder(this.browserColumnView);
				break;
			}
		}
	}

	private void reloadData() {
		log.debug("reloadData");
		pathPopupItems.clear();
		pathPopupButton.removeAllItems();
		if(this.isMounted()) {
			this.addPathToPopup(workdir);
			for (Path p = workdir; !p.isRoot();) {
				p = p.getParent();
				this.addPathToPopup(p);
			}
		}
		this.sort();
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.browserListView.reloadData();
				if(this.isMounted()) {
					this.infoLabel.setStringValue(this.browserListView.numberOfRows() + " " +
												  NSBundle.localizedString("files", ""));
				}
				else this.infoLabel.setStringValue("");
				break;
			}
			case OUTLINE_VIEW: {
				this.browserOutlineView.reloadData();
				if(this.isMounted()) {
					this.browserOutlineView.reloadItemAndChildren(this.workdir(), true);
					this.infoLabel.setStringValue(this.browserOutlineView.numberOfRows() + " " +
												  NSBundle.localizedString("files", ""));
				}
				else this.infoLabel.setStringValue("");
				break;
			}
			case COLUMN_VIEW: {
				ThreadUtilities.instance().invokeLater(new Runnable() {
					public void run() {
						if(isMounted()) {
							browserColumnView.setPath(workdir().getAbsolute());
							//browserColumnView.loadColumnZero();
							browserColumnView.reloadColumn(browserColumnView.lastColumn());
							browserColumnView.setPath(workdir().getAbsolute());
							infoLabel.setStringValue(browserListModel.cache(workdir()).size() + " " +
														  NSBundle.localizedString("files", ""));
							browserColumnView.validateVisibleColumns();
						}
						else infoLabel.setStringValue("");
					}
				});
				break;
			}
		}
		this.getFocus();
    }
	
	private void selectRow(int row) {
		log.debug("selectRow:"+row);
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.browserListView.selectRow(row, false);
				break;
			}
			case OUTLINE_VIEW: {
				this.browserOutlineView.selectRow(row, false);
				break;
			}
			case COLUMN_VIEW: {
				break;
			}
		}
		this.getFocus();
	}
	
	private Path getSelectedPath() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				return (Path)this.browserListModel.cache(this.workdir()).get(this.browserListView.selectedRow());
			}
			case OUTLINE_VIEW: {
				return (Path)this.browserOutlineView.itemAtRow(this.browserOutlineView.selectedRow());
			}
			case COLUMN_VIEW: {
				return ((CDBrowserCell)this.browserColumnView.selectedCell()).getPath();
			}
		}
		return null;
	}
	
	private List getSelectedPaths() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				NSEnumerator enum = this.browserListView.selectedRowEnumerator();
				List files = new ArrayList();
				while (enum.hasMoreElements()) {
					int selected = ((Integer) enum.nextElement()).intValue();
					files.add(this.browserListModel.cache(this.workdir()).get(selected));
				}
				return files;
			}
			case OUTLINE_VIEW: {
				NSEnumerator enum = this.browserOutlineView.selectedRowEnumerator();
				List files = new ArrayList();
				while (enum.hasMoreElements()) {
					int selected = ((Integer) enum.nextElement()).intValue();
					files.add(this.browserOutlineView.itemAtRow(selected));
				}
				return files;
			}
			case COLUMN_VIEW: {
				java.util.Enumeration enum = this.browserColumnView.selectedCells().objectEnumerator();
				List files = new ArrayList();
				while (enum.hasMoreElements()) {
					files.add(((CDBrowserCell)enum.nextElement()).getPath());
				}
				return files;
			}
		}
		return null;
	}
	
	private int getSelectionCount() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				return this.browserListView.numberOfSelectedRows();
			}
			case OUTLINE_VIEW: {
				return this.browserOutlineView.numberOfSelectedRows();
			}
			case COLUMN_VIEW: {
				NSArray selectedCells = this.browserColumnView.selectedCells();
				if(selectedCells != null) 
					return selectedCells.count();
			}
		}
		return 0;
	}

	private void deselectAll() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.browserListView.deselectAll(null);
				break;
			}
			case OUTLINE_VIEW: {
				this.browserOutlineView.deselectAll(null);
				break;
			}
			case COLUMN_VIEW: {
				break;
			}
		}
	}
	
	private void sizeToFit() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				this.browserListView.sizeToFit();
				break;
			}
			case OUTLINE_VIEW: {
				this.browserOutlineView.sizeToFit();
				break;
			}
			case COLUMN_VIEW: {
				break;
			}
		}
	}

	public void browserColumnViewRowClicked(Object sender) {
		this.browserSelectionDidChange(null);
		if(!((NSBrowserCell)this.browserColumnView.selectedCell()).isLeaf()) {
			this.browserRowDoubleClicked(sender);
		}
    }
		
	public void browserRowDoubleClicked(Object sender) {
        this.searchField.setStringValue("");
        if (this.getSelectionCount() > 0) {
            Path p = this.getSelectedPath(); //last row selected
            if (p.attributes.isDirectory()) {
                this.deselectAll();
                p.list(this.encoding, false, this.getFileFilter());
            }
            if (p.attributes.isFile() || this.getSelectionCount() > 1) {
                if (Preferences.instance().getBoolean("browser.doubleclick.edit")) {
                    this.editButtonClicked(null);
                }
                else {
                    this.downloadButtonClicked(null);
                }
            }
        }
	}	
	private void sort() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				NSTableColumn selectedColumn = this.browserListModel.selectedColumn() != null ?
				this.browserListModel.selectedColumn() :
				this.browserListView.tableColumnWithIdentifier("FILENAME");
				this.browserListView.setIndicatorImage(this.browserListModel.isSortedAscending() ?
														  NSImage.imageNamed("NSAscendingSortIndicator") :
														  NSImage.imageNamed("NSDescendingSortIndicator"), selectedColumn);
				this.browserListModel.sort(selectedColumn, this.browserListModel.isSortedAscending());
				break;
			}
			case OUTLINE_VIEW: {
				NSTableColumn selectedColumn = this.browserOutlineModel.selectedColumn() != null ?
				this.browserOutlineModel.selectedColumn() :
				this.browserOutlineView.tableColumnWithIdentifier("FILENAME");
				this.browserOutlineView.setIndicatorImage(this.browserOutlineModel.isSortedAscending() ?
														  NSImage.imageNamed("NSAscendingSortIndicator") :
														  NSImage.imageNamed("NSDescendingSortIndicator"), selectedColumn);
				this.browserOutlineModel.sort(selectedColumn, this.browserOutlineModel.isSortedAscending());
				break;
			}
			case COLUMN_VIEW: {
				break;
			}
		}
	}
	
    public void update(Observable o, Object arg) {
        if (arg instanceof Path) {
            this.workdir = (Path) arg;
            this.reloadData();
        }
        else if (arg instanceof Message) {
            final Message msg = (Message) arg;
            if (msg.getTitle().equals(Message.ERROR)) {
                this.progressIndicator.stopAnimation(this);
                this.statusIcon.setImage(NSImage.imageNamed("alert.tiff"));
                this.statusIcon.setNeedsDisplay(true);
                this.statusLabel.setAttributedStringValue(new NSAttributedString((String) msg.getContent(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                this.statusLabel.display();
                NSApplication.sharedApplication().beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Error", "Alert sheet title"), //title
                        (String) msg.getContent(), // message
                        NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                        null, //alternative button
                        null //other button
                ),
                this.window(),
                        null,
                null);
            }
            else if (msg.getTitle().equals(Message.PROGRESS)) {
                this.statusLabel.setAttributedStringValue(new NSAttributedString((String) msg.getContent(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                this.statusLabel.display();
            }
            else if (msg.getTitle().equals(Message.REFRESH)) {
                this.reloadButtonClicked(null);
            }
            else if (msg.getTitle().equals(Message.OPEN)) {
                this.progressIndicator.startAnimation(this);
                this.statusIcon.setImage(null);
                this.statusIcon.setNeedsDisplay(true);
                ThreadUtilities.instance().invokeLater(new Runnable() {
                    public void run() {
                        CDBrowserController.this.toolbar.validateVisibleItems();
                    }
                });
            }
            else if (msg.getTitle().equals(Message.CLOSE)) {
                this.progressIndicator.stopAnimation(this);
                this.statusIcon.setImage(null);
                this.statusIcon.setNeedsDisplay(true);
                ThreadUtilities.instance().invokeLater(new Runnable() {
                    public void run() {
                        CDBrowserController.this.toolbar.validateVisibleItems();
                    }
                });
            }
            else if (msg.getTitle().equals(Message.START)) {
                this.statusIcon.setImage(null);
                this.statusIcon.display();
                this.progressIndicator.startAnimation(this);
                ThreadUtilities.instance().invokeLater(new Runnable() {
                    public void run() {
                        CDBrowserController.this.toolbar.validateVisibleItems();
                    }
                });
            }
            else if (msg.getTitle().equals(Message.STOP)) {
                this.progressIndicator.stopAnimation(this);
                this.statusLabel.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Idle", "No background thread is running"),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                this.statusLabel.display();
                ThreadUtilities.instance().invokeLater(new Runnable() {
                    public void run() {
                        CDBrowserController.this.toolbar.validateVisibleItems();
                    }
                });
            }
        }
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

	private NSToolbar toolbar;

	private NSTabView browserTabView;
	
    public void setBrowserTabView(NSTabView browserTabView) {
        this.browserTabView = browserTabView;
    }
	
    private NSTextView logView;
	
    public void setLogView(NSTextView logView) {
        this.logView = logView;
    }
		
//	private NSButton interruptButton;
//	
//	public void setInterruptButton(NSButton interruptButton) {
//		this.interruptButton = interruptButton;
//		this.interruptButton.setTarget(this);
//      this.interruptButton.setAction(new NSSelector("interruptButtonClicked", new Class[]{Object.class}));
//		this.interruptButton.setEnabled(true);
//	}
//	
//	public void interruptButtonClicked(Object sender) {
//		if(this.isMounted()) {
//			this.workdir().getSession().interrupt();
//		}
//	}
	
	public NSView getSelectedBrowserView() {
		switch(this.browserSwitchView.selectedSegment()) {
			case LIST_VIEW: {
				return this.browserListView;
			}
			case OUTLINE_VIEW: {
				return this.browserOutlineView;
			}
			case COLUMN_VIEW: {
				return this.browserColumnView;
			}
		}
		return null;
	}
	
    private NSSegmentedControl browserSwitchView;

	private static final int LIST_VIEW = 0;
	private static final int OUTLINE_VIEW = 1;
	private static final int COLUMN_VIEW = 2;
		
    public void setBrowserSwitchView(NSSegmentedControl browserSwitchView) {
        this.browserSwitchView = browserSwitchView;
        this.browserSwitchView.setSegmentCount(3); // list, outline, column
		this.browserSwitchView.setImage(NSImage.imageNamed("list.tiff"), LIST_VIEW);
		this.browserSwitchView.setImage(NSImage.imageNamed("outline.tiff"), OUTLINE_VIEW);
		this.browserSwitchView.setImage(NSImage.imageNamed("column.tiff"), COLUMN_VIEW);
        this.browserSwitchView.setTarget(this);
        this.browserSwitchView.setAction(new NSSelector("browserSwitchClicked", new Class[]{Object.class}));
        ((NSSegmentedCell) this.browserSwitchView.cell()).setTrackingMode(NSSegmentedCell.NSSegmentSwitchTrackingSelectOne);
        this.browserSwitchView.cell().setControlSize(NSCell.RegularControlSize);
        this.browserSwitchView.setSelected(Preferences.instance().getInteger("browser.view"));
    }

    public void browserSwitchClicked(Object sender) {
        log.debug("browserSwitchClicked");
        if (sender instanceof NSMenuItem) {
			this.browserSwitchView.setSelected(((NSMenuItem)sender).tag());
			this.browserTabView.selectTabViewItemAtIndex(((NSMenuItem)sender).tag());
			Preferences.instance().setProperty("browser.view", ((NSMenuItem)sender).tag());
        }
        if (sender instanceof NSSegmentedControl) {
			this.browserTabView.selectTabViewItemAtIndex(((NSSegmentedControl)sender).selectedSegment());
			Preferences.instance().setProperty("browser.view", ((NSSegmentedControl)sender).selectedSegment());
		}
		this.reloadData();
    }

    private CDBrowserOutlineViewModel browserOutlineModel;
    private NSOutlineView browserOutlineView; // IBOutlet

    public void setBrowserOutlineView(NSOutlineView browserOutlineView) {
        this.browserOutlineView = browserOutlineView;
		this.browserOutlineView.setAutosaveExpandedItems(false);
        this.browserOutlineView.setTarget(this);
        this.browserOutlineView.setDoubleAction(new NSSelector("browserRowDoubleClicked", new Class[]{Object.class}));
        // receive drag events from types
//        this.browserOutlineView.registerForDraggedTypes(new NSArray(new Object[]{
//            "QueuePboardType",
//            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
//            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
//        ));

        // setting appearance attributes
        this.browserOutlineView.setRowHeight(17f);
        this.browserOutlineView.setAutoresizesAllColumnsToFit(true);
        this._updateBrowserOutlineTableAttributes();
        // selection properties
        this.browserOutlineView.setAllowsMultipleSelection(true);
        this.browserOutlineView.setAllowsEmptySelection(true);
        this.browserOutlineView.setAllowsColumnResizing(true);
        this.browserOutlineView.setAllowsColumnSelection(false);
        this.browserOutlineView.setAllowsColumnReordering(true);

        if (Preferences.instance().getBoolean("browser.info.isInspector")) {
            (NSNotificationCenter.defaultCenter()).addObserver(this,
                    new NSSelector("browserSelectionDidChange", new Class[]{NSNotification.class}),
                    NSOutlineView.OutlineViewSelectionDidChangeNotification,
                    this.browserOutlineView);
        }
        this.browserOutlineView.setDataSource(this.browserOutlineModel = new CDBrowserOutlineViewModel(this));
        this.browserOutlineView.setDelegate(this.browserOutlineModel);
    }

    private CDBrowserListViewModel browserListModel;
    private NSTableView browserListView; // IBOutlet

    public void setBrowserListView(NSTableView browserListView) {
        this.browserListView = browserListView;
        this.browserListView.setTarget(this);
        this.browserListView.setDoubleAction(new NSSelector("browserRowDoubleClicked", new Class[]{Object.class}));
        // receive drag events from types
        this.browserListView.registerForDraggedTypes(new NSArray(new Object[]{
            "QueuePboardType",
            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
        ));

        // setting appearance attributes
        this.browserListView.setRowHeight(17f);
        this.browserListView.setAutoresizesAllColumnsToFit(true);
        this._updateBrowserListTableAttributes();
        // selection properties
        this.browserListView.setAllowsMultipleSelection(true);
        this.browserListView.setAllowsEmptySelection(true);
        this.browserListView.setAllowsColumnResizing(true);
        this.browserListView.setAllowsColumnSelection(false);
        this.browserListView.setAllowsColumnReordering(true);

        if (Preferences.instance().getBoolean("browser.info.isInspector")) {
            (NSNotificationCenter.defaultCenter()).addObserver(this,
                    new NSSelector("browserSelectionDidChange", new Class[]{NSNotification.class}),
                    NSTableView.TableViewSelectionDidChangeNotification,
                    this.browserListView);
        }
        this.browserListView.setDataSource(this.browserListModel = new CDBrowserListViewModel(this));
        this.browserListView.setDelegate(this.browserListModel);
    }

    private CDBrowserColumnViewModel browserColumnModel;
    private NSBrowser browserColumnView; // IBOutlet

    public void setBrowserColumnView(NSBrowser browserColumnView) {
        this.browserColumnView = browserColumnView;
        this.browserColumnView.setTarget(this);
        this.browserColumnView.setAction(new NSSelector("browserColumnViewRowClicked", new Class[]{Object.class}));
        this.browserColumnView.setDoubleAction(new NSSelector("browserRowDoubleClicked", new Class[]{Object.class}));
        this.browserColumnView.setAcceptsArrowKeys(true);
		this.browserColumnView.setSendsActionOnArrowKeys(true);
        this.browserColumnView.setMaxVisibleColumns(5);
        this.browserColumnView.setAllowsEmptySelection(true);
        this.browserColumnView.setAllowsMultipleSelection(false);
		this.browserColumnView.setAllowsBranchSelection(true);
        this.browserColumnView.setPathSeparator("/");
        this.browserColumnView.setReusesColumns(false);
        this.browserColumnView.setSeparatesColumns(false);
        this.browserColumnView.setTitled(false);
        this.browserColumnView.setHasHorizontalScroller(false);

        this.browserColumnView.setDelegate(this.browserColumnModel = new CDBrowserColumnViewModel(this));
        // Make the browser user our custom browser cell.
        this.browserColumnView.setNewCellClass(CDBrowserCell.class);
        this.browserColumnView.setNewMatrixClass(CDBrowserMatrix.class);
    }

    public void browserSelectionDidChange(NSNotification notification) {
        if (this.inspector != null && this.inspector.window().isVisible()) {
            List files = new ArrayList();
			for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
                files.add(i.next());
            }
            this.inspector.setFiles(files);
        }
    }

    protected void _updateBrowserOutlineTableAttributes() {
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.browserOutlineView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            if (Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines")) {
                this.browserOutlineView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getBoolean("browser.verticalLines")) {
                this.browserOutlineView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getBoolean("browser.horizontalLines")) {
                this.browserOutlineView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
            }
            else {
                this.browserOutlineView.setGridStyleMask(NSTableView.GridNone);
            }
        }
    }

    protected void _updateBrowserListTableAttributes() {
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.browserListView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            if (Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines")) {
                this.browserListView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getBoolean("browser.verticalLines")) {
                this.browserListView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getBoolean("browser.horizontalLines")) {
                this.browserListView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
            }
            else {
                this.browserListView.setGridStyleMask(NSTableView.GridNone);
            }
        }
    }

    protected void _updateBrowserTableColumns() {
        log.debug("_updateBrowserTableColumns");
		{
			java.util.Enumeration enum = this.browserOutlineView.tableColumns().objectEnumerator();
			while (enum.hasMoreElements()) {
				this.browserOutlineView.removeTableColumn((NSTableColumn) enum.nextElement());
			}
			this.browserOutlineView.setOutlineTableColumn(null);
		}
		{
			java.util.Enumeration enum = this.browserListView.tableColumns().objectEnumerator();
			while (enum.hasMoreElements()) {
				this.browserListView.removeTableColumn((NSTableColumn) enum.nextElement());
			}
		}
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("TYPE");
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            c.setResizable(true);
            c.setEditable(false);
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            this.browserListView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier("FILENAME");
            c.setMinWidth(100f);
            c.setWidth(250f);
            c.setMaxWidth(1000f);
            c.setResizable(true);
            c.setDataCell(new CDOutlineCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.browserOutlineView.addTableColumn(c);
            this.browserOutlineView.setOutlineTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier("FILENAME");
            c.setMinWidth(100f);
            c.setWidth(250f);
            c.setMaxWidth(1000f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.browserListView.addTableColumn(c);
        }
        if (Preferences.instance().getBoolean("browser.columnSize")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Size", "A column in the browser"));
            c.setIdentifier("SIZE");
            c.setMinWidth(50f);
            c.setWidth(80f);
            c.setMaxWidth(100f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.RightTextAlignment);
            this.browserOutlineView.addTableColumn(c);
            this.browserListView.addTableColumn(c);
        }
        if (Preferences.instance().getBoolean("browser.columnModification")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Modified", "A column in the browser"));
            c.setIdentifier("MODIFIED");
            c.setMinWidth(100f);
            c.setWidth(180f);
            c.setMaxWidth(500f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            c.dataCell().setFormatter(new NSGregorianDateFormatter((String) NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.ShortTimeDateFormatString),
                    true));
            this.browserOutlineView.addTableColumn(c);
            this.browserListView.addTableColumn(c);
        }
        if (Preferences.instance().getBoolean("browser.columnOwner")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Owner", "A column in the browser"));
            c.setIdentifier("OWNER");
            c.setMinWidth(100f);
            c.setWidth(80f);
            c.setMaxWidth(500f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.browserOutlineView.addTableColumn(c);
            this.browserListView.addTableColumn(c);
        }
        if (Preferences.instance().getBoolean("browser.columnPermissions")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Permissions", "A column in the browser"));
            c.setIdentifier("PERMISSIONS");
            c.setMinWidth(100f);
            c.setWidth(100f);
            c.setMaxWidth(800f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.browserOutlineView.addTableColumn(c);
            this.browserListView.addTableColumn(c);
        }
		this.sizeToFit();
        this.reloadData();
    }
	
    private CDBookmarkTableDataSource bookmarkModel;
    private NSTableView bookmarkTable; // IBOutlet

    public void setBookmarkTable(NSTableView bookmarkTable) {
        this.bookmarkTable = bookmarkTable;
        this.bookmarkTable.setTarget(this);
        this.bookmarkTable.setDoubleAction(new NSSelector("bookmarkTableRowDoubleClicked", new Class[]{Object.class}));

        // receive drag events from types
        this.bookmarkTable.registerForDraggedTypes(new NSArray(new Object[]
        {
            NSPasteboard.FilenamesPboardType, //accept bookmark files dragged from the Finder
            NSPasteboard.FilesPromisePboardType,
            "HostPBoardType" //moving bookmarks
        }));
        this.bookmarkTable.setRowHeight(45f);

        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("ICON");
            c.headerCell().setStringValue("");
            c.setMinWidth(32f);
            c.setWidth(32f);
            c.setMaxWidth(32f);
            c.setEditable(false);
            c.setResizable(true);
            c.setDataCell(new NSImageCell());
            this.bookmarkTable.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("BOOKMARK");
            c.headerCell().setStringValue(NSBundle.localizedString("Bookmarks", "A column in the browser"));
            c.setMinWidth(50f);
            c.setWidth(200f);
            c.setMaxWidth(500f);
            c.setEditable(false);
            c.setResizable(true);
            c.setDataCell(new CDBookmarkCell());
            this.bookmarkTable.addTableColumn(c);
        }

        // setting appearance attributes
        this.bookmarkTable.setAutoresizesAllColumnsToFit(true);
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.bookmarkTable.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            this.bookmarkTable.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
        }
        this.bookmarkTable.setAutoresizesAllColumnsToFit(true);

        // selection properties
        this.bookmarkTable.setAllowsMultipleSelection(true);
        this.bookmarkTable.setAllowsEmptySelection(true);
        this.bookmarkTable.setAllowsColumnResizing(false);
        this.bookmarkTable.setAllowsColumnSelection(false);
        this.bookmarkTable.setAllowsColumnReordering(false);
        this.bookmarkTable.sizeToFit();

        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("bookmarkSelectionDidChange", new Class[]{NSNotification.class}),
                NSTableView.TableViewSelectionDidChangeNotification,
                this.bookmarkTable);
        this.bookmarkTable.setDataSource(this.bookmarkModel = CDBookmarkTableDataSource.instance());
        this.bookmarkTable.setDelegate(this.bookmarkModel);
    }

    public void bookmarkSelectionDidChange(NSNotification notification) {
        log.debug("bookmarkSelectionDidChange");
        editBookmarkButton.setEnabled(bookmarkTable.numberOfSelectedRows() == 1);
        deleteBookmarkButton.setEnabled(bookmarkTable.selectedRow() != -1);
    }

    public void bookmarkTableRowDoubleClicked(Object sender) {
        log.debug("bookmarkTableRowDoubleClicked");
        if (this.bookmarkTable.selectedRow() != -1) {
            Host h = (Host) bookmarkModel.get(bookmarkTable.selectedRow());
            this.mount(h, h.getEncoding());
        }
    }

	private NSPopUpButton actionPopupButton;
	
	public void setActionPopupButton(NSPopUpButton actionPopupButton) {
		this.actionPopupButton = actionPopupButton;
		this.actionPopupButton.setPullsDown(true);
		this.actionPopupButton.setAutoenablesItems(true);
		this.actionPopupButton.itemAtIndex(0).setImage(NSImage.imageNamed("gear.tiff"));
	}
	
    private NSComboBox quickConnectPopup; // IBOutlet
	private Object quickConnectPopupDataSource;

    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
        this.quickConnectPopup = quickConnectPopup;
        this.quickConnectPopup.setTarget(this);
        this.quickConnectPopup.setCompletes(true);
        this.quickConnectPopup.setAction(new NSSelector("quickConnectSelectionChanged", new Class[]{Object.class}));
        this.quickConnectPopup.setUsesDataSource(true);
        this.quickConnectPopup.setDataSource(this.quickConnectPopupDataSource = new Object() {
            public int numberOfItemsInComboBox(NSComboBox combo) {
                return CDBookmarkTableDataSource.instance().size();
            }

            public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
                if (row < this.numberOfItemsInComboBox(combo)) {
                    return ((Host) CDBookmarkTableDataSource.instance().get(row)).getHostname();
                }
                return null;
            }
        });
    }

    public void quickConnectSelectionChanged(Object sender) {
        log.debug("quickConnectSelectionChanged");
        try {
            String input = ((NSControl) sender).stringValue();
            for (Iterator iter = bookmarkModel.iterator(); iter.hasNext();) {
                Host h = (Host) iter.next();
                if (h.getHostname().equals(input)) {
                    this.mount(h);
                    return;
                }
            }
            this.mount(Host.parse(input));
        }
        catch (java.net.MalformedURLException e) {
            NSAlertPanel.beginCriticalAlertSheet("Error", //title
                    "OK", // defaultbutton
                    null, //alternative button
                    null, //other button
                    this.window(), //docWindow
                    null, //modalDelegate
                    null, //didEndSelector
                    null, // dismiss selector
                    null, // context
                    e.getMessage() // message
            );
        }
    }

    private NSTextField searchField; // IBOutlet

    public void setSearchField(NSTextField searchField) {
        this.searchField = searchField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("searchFieldTextDidChange", new Class[]{Object.class}),
                NSControl.ControlTextDidChangeNotification,
                searchField);
    }

    public void searchFieldTextDidChange(NSNotification notification) {
        NSDictionary userInfo = notification.userInfo();
        if (null != userInfo) {
            Object o = userInfo.allValues().lastObject();
            if (null != o) {
                final String searchString = ((NSText) o).string();
                if (null == searchString || searchString.length() == 0) {
					this.filenameFilter = new HiddenFilesFilter();
                }
                else {
					this.filenameFilter = new Filter() {
						public boolean accept(Path file) {
							return file.getName().toLowerCase().indexOf(searchString.toLowerCase()) != -1;
						}
					};
                }
				this.reloadData();
				this.window().makeFirstResponder(this.searchField);
				this.searchField.setNextKeyView(this.getSelectedBrowserView());
            }
        }
    }

    // ----------------------------------------------------------
    // Manage Bookmarks
    // ----------------------------------------------------------

    private NSButton editBookmarkButton; // IBOutlet

    public void setEditBookmarkButton(NSButton editBookmarkButton) {
        this.editBookmarkButton = editBookmarkButton;
        this.editBookmarkButton.setImage(NSImage.imageNamed("edit.tiff"));
        this.editBookmarkButton.setAlternateImage(NSImage.imageNamed("editPressed.tiff"));
        this.editBookmarkButton.setEnabled(false);
        this.editBookmarkButton.setTarget(this);
        this.editBookmarkButton.setAction(new NSSelector("editBookmarkButtonClicked", new Class[]{Object.class}));
    }

    public void editBookmarkButtonClicked(Object sender) {
        this.bookmarkDrawer.open();
        CDBookmarkController controller = new CDBookmarkController(bookmarkTable,
                (Host) bookmarkModel.get(bookmarkTable.selectedRow()));
        controller.window().makeKeyAndOrderFront(null);
    }

    private NSButton addBookmarkButton; // IBOutlet

    public void setAddBookmarkButton(NSButton addBookmarkButton) {
        this.addBookmarkButton = addBookmarkButton;
        this.addBookmarkButton.setImage(NSImage.imageNamed("add"));
        this.addBookmarkButton.setAlternateImage(NSImage.imageNamed("addPressed.tiff"));
        this.addBookmarkButton.setTarget(this);
        this.addBookmarkButton.setAction(new NSSelector("addBookmarkButtonClicked", new Class[]{Object.class}));
    }

    public void addBookmarkButtonClicked(Object sender) {
        this.bookmarkDrawer.open();
        Host item;
        if (this.isMounted()) {
            item = this.workdir().getSession().getHost().copy();
            item.setDefaultPath(this.workdir().getAbsolute());
        }
        else {
            item = new Host(Preferences.instance().getProperty("connection.protocol.default"),
                    "localhost",
                    Preferences.instance().getInteger("connection.port.default"));
        }
        this.bookmarkModel.add(item);
        this.bookmarkTable.reloadData();
        this.bookmarkTable.selectRow(bookmarkModel.lastIndexOf(item), false);
        this.bookmarkTable.scrollRowToVisible(bookmarkModel.lastIndexOf(item));
        CDBookmarkController controller = new CDBookmarkController(bookmarkTable, item);
        controller.window().makeKeyAndOrderFront(null);
    }

    private NSButton deleteBookmarkButton; // IBOutlet

    public void setDeleteBookmarkButton(NSButton deleteBookmarkButton) {
        this.deleteBookmarkButton = deleteBookmarkButton;
        this.deleteBookmarkButton.setImage(NSImage.imageNamed("remove.tiff"));
        this.deleteBookmarkButton.setAlternateImage(NSImage.imageNamed("removePressed.tiff"));
        this.deleteBookmarkButton.setEnabled(false);
        this.deleteBookmarkButton.setTarget(this);
        this.deleteBookmarkButton.setAction(new NSSelector("deleteBookmarkButtonClicked", new Class[]{Object.class}));
    }

    public void deleteBookmarkButtonClicked(Object sender) {
        this.bookmarkDrawer.open();
        NSEnumerator enum = bookmarkTable.selectedRowEnumerator();
        int j = 0;
        while (enum.hasMoreElements()) {
            int i = ((Integer) enum.nextElement()).intValue();
            Host host = (Host) this.bookmarkModel.get(i - j);
            switch (NSAlertPanel.runCriticalAlert(NSBundle.localizedString("Delete Bookmark", ""),
                    NSBundle.localizedString("Do you want to delete the selected bookmark?", "")
                    + " [" + host.getNickname() + "]",
                    NSBundle.localizedString("Delete", ""),
                    NSBundle.localizedString("Cancel", ""),
                    null)) {
                case NSAlertPanel.DefaultReturn:
                    bookmarkModel.remove(i - j);
                    j++;
                    break;
                case NSAlertPanel.AlternateReturn:
                    break;
            }
        }
        this.bookmarkTable.reloadData();
    }

    // ----------------------------------------------------------
    // Browser navigation
    // ----------------------------------------------------------

    private NSButton upButton; // IBOutlet

    public void setUpButton(NSButton upButton) {
        this.upButton = upButton;
        this.upButton.setImage(NSImage.imageNamed("arrowUpBlack16.tiff"));
        this.upButton.setTarget(this);
        this.upButton.setAction(new NSSelector("upButtonClicked", new Class[]{Object.class}));
    }

    private NSButton backButton; // IBOutlet

    public void setBackButton(NSButton backButton) {
        this.backButton = backButton;
        this.backButton.setImage(NSImage.imageNamed("arrowLeftBlack16.tiff"));
        this.backButton.setTarget(this);
        this.backButton.setAction(new NSSelector("backButtonClicked", new Class[]{Object.class}));
    }

    private static final NSImage DISK_ICON = NSImage.imageNamed("disk.tiff");

    private List pathPopupItems = new ArrayList();
    private Path workdir;

    private NSPopUpButton pathPopupButton; // IBOutlet

    public void setPathPopup(NSPopUpButton pathPopupButton) {
        this.pathPopupButton = pathPopupButton;
        this.pathPopupButton.setTarget(this);
        this.pathPopupButton.setAction(new NSSelector("pathPopupSelectionChanged", new Class[]{Object.class}));
    }

    public void pathPopupSelectionChanged(Object sender) {
        Path p = (Path) pathPopupItems.get(pathPopupButton.indexOfSelectedItem());
        this.deselectAll();
        p.list(this.encoding, false, this.getFileFilter());
    }

    private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");

    private void addPathToPopup(Path p) {
        this.pathPopupItems.add(p);
        this.pathPopupButton.addItem(p.getAbsolute());
        if (p.isRoot()) {
            this.pathPopupButton.itemAtIndex(this.pathPopupButton.numberOfItems() - 1).setImage(DISK_ICON);
        }
        else {
            this.pathPopupButton.itemAtIndex(this.pathPopupButton.numberOfItems() - 1).setImage(FOLDER_ICON);
        }
    }

    private NSPopUpButton encodingPopup;

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setTarget(this);
        this.encodingPopup.setAction(new NSSelector("encodingButtonClicked", new Class[]{Object.class}));
        this.encodingPopup.removeAllItems();
        java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
        String[] items = new String[charsets.size()];
        java.util.Iterator iterator = charsets.values().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            items[i] = ((java.nio.charset.Charset) iterator.next()).name();
            i++;
        }
        this.encodingPopup.addItemsWithTitles(new NSArray(items));
        this.encodingPopup.setTitle(Preferences.instance().getProperty("browser.charset.encoding"));
    }

    public void changeEncoding(String encoding)  {
        this.changeEncoding(encoding, true);
    }

    public void changeEncoding(String encoding, boolean force) {
        this.encoding = encoding;
        log.info("Encoding changed to:" + this.encoding);
        this.encodingPopup.setTitle(this.encoding);
        if(force) {
            if (this.isMounted()) {
                this.workdir().getSession().close();
                this.reloadButtonClicked(null);
            }
        }
    }

    public void encodingButtonClicked(Object sender) {
        if (sender instanceof NSMenuItem) {
            this.changeEncoding(((NSMenuItem) sender).title());
        }
        if (sender instanceof NSPopUpButton) {
            this.changeEncoding(this.encodingPopup.titleOfSelectedItem());
        }
    }

    // ----------------------------------------------------------
    // Drawers
    // ----------------------------------------------------------

    private NSDrawer logDrawer; // IBOutlet

    public void setLogDrawer(NSDrawer logDrawer) {
        this.logDrawer = logDrawer;
    }

    public void toggleLogDrawer(Object sender) {
        this.logDrawer.toggle(this);
    }

    private NSDrawer bookmarkDrawer; // IBOutlet

    public void setBookmarkDrawer(NSDrawer bookmarkDrawer) {
        this.bookmarkDrawer = bookmarkDrawer;
        this.bookmarkDrawer.setDelegate(this);
    }

    public void toggleBookmarkDrawer(Object sender) {
        this.bookmarkDrawer.toggle(this);
        Preferences.instance().setProperty("bookmarkDrawer.isOpen", this.bookmarkDrawer.state() == NSDrawer.OpenState || this.bookmarkDrawer.state() == NSDrawer.OpeningState);
        if (this.bookmarkDrawer.state() == NSDrawer.OpenState || this.bookmarkDrawer.state() == NSDrawer.OpeningState) {
            this.window().makeFirstResponder(this.bookmarkTable);
        }
        else {
            if (this.isMounted()) {
				this.getFocus();
            }
            else {
                this.window().makeFirstResponder(this.quickConnectPopup);
            }
        }
    }

    // ----------------------------------------------------------
    // Status
    // ----------------------------------------------------------

    private NSProgressIndicator progressIndicator; // IBOutlet

    public void setProgressIndicator(NSProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
        this.progressIndicator.setIndeterminate(true);
        this.progressIndicator.setUsesThreadedAnimation(true);
    }

    private NSImageView statusIcon; // IBOutlet

    public void setStatusIcon(NSImageView statusIcon) {
        this.statusIcon = statusIcon;
    }

    private NSTextField statusLabel; // IBOutlet

    public void setStatusLabel(NSTextField statusLabel) {
        this.statusLabel = statusLabel;
        this.statusLabel.setAttributedStringValue(new NSAttributedString(NSBundle.localizedString("Idle", "No background thread is running"),
                TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
    }

    private NSTextField infoLabel; // IBOutlet

    public void setInfoLabel(NSTextField infoLabel) {
        this.infoLabel = infoLabel;
    }

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

	public void reloadButtonClicked(Object sender) {
		log.debug("reloadButtonClicked");
		if (this.isMounted()) {
			this.deselectAll();
			this.workdir().list(this.encoding, true, this.getFileFilter());
		}
	}
	
    public void editButtonClicked(Object sender) {
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
            Path path = (Path) i.next();
            if (path.attributes.isFile()) {
                Editor editor = new Editor();
                editor.open(path);
            }
        }
    }

    public void gotoButtonClicked(Object sender) {
        log.debug("gotoButtonClicked");
        CDGotoController controller = new CDGotoController(this.workdir());
        this.beginSheet(controller.window(), //sheet
                controller, //modal delegate
                new NSSelector("gotoSheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                this.workdir()); //contextInfo
    }

    public void createFileButtonClicked(Object sender) {
        log.debug("createFileButtonClicked:");
        CDFileController controller = new CDCreateFileController();
        this.beginSheet(controller.window(), //sheet
                controller, //modal delegate
                new NSSelector("createFileSheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                this.workdir()); //contextInfo
    }
	
	public void duplicateFileButtonClicked(Object sender) {
        if (this.getSelectionCount() > 0) {
			CDFileController controller = new CDDuplicateFileController(this.getSelectedPath());
			this.beginSheet(controller.window(), //sheet
							controller, //modal delegate
							new NSSelector("duplicateFileSheetDidEnd",
										   new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
							this.workdir()); //contextInfo
		}
	}

    public void createFolderButtonClicked(Object sender) {
        log.debug("createFolderButtonClicked");
        CDFolderController controller = new CDFolderController();
        this.beginSheet(controller.window(), //sheet
                controller, //modal delegate
                new NSSelector("newFolderSheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                this.workdir()); //contextInfo
    }
	
    public void infoButtonClicked(Object sender) {
        log.debug("infoButtonClicked");
        if (this.getSelectionCount() > 0) {
            List files = this.getSelectedPaths();
            if (Preferences.instance().getBoolean("browser.info.isInspector")) {
                if (null == this.inspector) {
                    this.inspector = new CDInfoController();
                }
				this.inspector.setFiles(files);
                this.inspector.window().makeKeyAndOrderFront(null);
            }
            else {
                CDInfoController c = new CDInfoController();
				c.setFiles(files);
                c.window().makeKeyAndOrderFront(null);
            }
        }
    }

    public void deleteKeyPerformed(Object sender) {
        log.debug("deleteKeyPerformed:" + sender);
        if (sender == this.bookmarkTable) {
            this.deleteBookmarkButtonClicked(sender);
        }
		else {
            this.deleteFileButtonClicked(sender);
		}
    }

    public void deleteFileButtonClicked(Object sender) {
        log.debug("deleteFileButtonClicked:" + sender);
        List files = new ArrayList();
        StringBuffer alertText = new StringBuffer(NSBundle.localizedString("Really delete the following files? This cannot be undone.", "Confirm deleting files."));
        if (sender instanceof Path) {
            Path p = (Path) sender;
            files.add(p);
            alertText.append("\n- " + p.getName());
        }
        else if(this.getSelectionCount() > 0) {
			int i = 0;
			Iterator iter = null;
			for(iter = this.getSelectedPaths().iterator(); i < 10 && iter.hasNext(); ) {
				Path p = (Path)iter.next();
                files.add(p);
                alertText.append("\n- " + p.getName());
                i++;
            }
            if(iter.hasNext()) {
                alertText.append("\n- (...)");
                while (iter.hasNext()) {
                    files.add(iter.next());
                }
            }
        }
        if (files.size() > 0) {
            NSApplication.sharedApplication().beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Delete", "Alert sheet title"), //title
                    alertText.toString(),
                    NSBundle.localizedString("Delete", "Alert sheet default button"), // defaultbutton
                    NSBundle.localizedString("Cancel", "Alert sheet alternate button"), //alternative button
                    null //other button
            ),
                    this.window(),
                    files,
                    new NSSelector
                            ("deleteSheetDidEnd",
                                    new Class[]
                                    {
                                        NSWindow.class, int.class, Object.class
                                    })
            );// end selector
        }
    }

	public void deleteSheetDidEnd(NSWindow sheet, int returnCode, Object contextInfo) {
		log.debug("deleteSheetDidEnd");
		sheet.orderOut(null);
		switch(returnCode) {
			case (NSAlertPanel.DefaultReturn):
				final List files = (List)contextInfo;
				if(files.size() > 0) {
					this.deselectAll();
					Iterator i = files.iterator();
					Path p = null;
					while(i.hasNext()) {
						p = (Path)i.next();
						p.delete();
					}
					p.getParent().list(encoding, true, this.getFileFilter());
				}
				break;
			case (NSAlertPanel.AlternateReturn):
				break;
		}
	}

    public void downloadAsButtonClicked(Object sender) {
        Session session = this.workdir().getSession().copy();
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
            Path path = ((Path)i.next()).copy(session);
            NSSavePanel panel = NSSavePanel.savePanel();
            NSSelector setMessageSelector =
                    new NSSelector("setMessage", new Class[]{String.class});
            if (setMessageSelector.implementedByClass(NSOpenPanel.class)) {
                panel.setMessage(NSBundle.localizedString("Download the selected file to...", ""));
            }
            NSSelector setNameFieldLabelSelector =
                    new NSSelector("setNameFieldLabel", new Class[]{String.class});
            if (setNameFieldLabelSelector.implementedByClass(NSOpenPanel.class)) {
                panel.setNameFieldLabel(NSBundle.localizedString("Download As:", ""));
            }
            panel.setPrompt(NSBundle.localizedString("Download", ""));
            panel.setTitle(NSBundle.localizedString("Download", ""));
            panel.setCanCreateDirectories(true);
            panel.beginSheetForDirectory(null,
                    path.getLocal().getName(),
                    this.window(),
                    this,
                    new NSSelector("saveAsPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                    path);
        }
    }

    public void saveAsPanelDidEnd(NSSavePanel sheet, int returnCode, Object contextInfo) {
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn):
                String filename = null;
                if ((filename = sheet.filename()) != null) {
                    Path path = (Path) contextInfo;
                    path.setLocal(new Local(filename));
                    Queue q = new DownloadQueue();
                    q.addRoot(path);
                    CDQueueController.instance().startItem(q);
                }
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }

    public void syncButtonClicked(Object sender) {
        log.debug("syncButtonClicked");
        Path selection;
        if(this.getSelectionCount() == 1 &&
                this.getSelectedPath().attributes.isDirectory()) {
            selection = (this.getSelectedPath().copy(this.workdir().getSession().copy()));
        }
        else {
            selection = this.workdir().copy(this.workdir().getSession().copy());
        }
        NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(selection.attributes.isDirectory());
        panel.setCanChooseFiles(selection.attributes.isFile());
        panel.setCanCreateDirectories(true);
        panel.setAllowsMultipleSelection(false);
        NSSelector setMessageSelector =
                new NSSelector("setMessage", new Class[]{String.class});
        if (setMessageSelector.implementedByClass(NSOpenPanel.class)) {
            panel.setMessage(NSBundle.localizedString("Synchronize", "")
                    + " " + selection.getName() + " "
                    + NSBundle.localizedString("with", "Synchronize <file> with <file>") + "...");
        }
        panel.setPrompt(NSBundle.localizedString("Choose", ""));
        panel.setTitle(NSBundle.localizedString("Synchronize", ""));
        panel.beginSheetForDirectory(null,
                null,
                null,
                this.window(), //parent window
                this,
                new NSSelector("syncPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                selection //context info
        );
    }

    public void syncPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
        sheet.orderOut(null);
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn):
                Path selection = (Path) contextInfo;
                if (sheet.filenames().count() > 0) {
                    selection.setLocal(new Local((String) sheet.filenames().lastObject()));
                    Queue q = new SyncQueue((Observer) this);
                    q.addRoot(selection);
                    CDQueueController.instance().startItem(q);
                }
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }

    public void downloadButtonClicked(Object sender) {
        Queue q = new DownloadQueue();
        Session session = this.workdir().getSession().copy();
        for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
            Path path = ((Path)i.next()).copy(session);
            q.addRoot(path);
        }
        CDQueueController.instance().startItem(q);
    }

    public void uploadButtonClicked(Object sender) {
        log.debug("uploadButtonClicked");
        NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(true);
        panel.setCanCreateDirectories(false);
        panel.setCanChooseFiles(true);
        panel.setAllowsMultipleSelection(true);
        panel.setPrompt(NSBundle.localizedString("Upload", ""));
        panel.setTitle(NSBundle.localizedString("Upload", ""));
        panel.beginSheetForDirectory(null,
                null,
                null,
                this.window(),
                this,
                new NSSelector("uploadPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                null);
    }

    public void uploadPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
        sheet.orderOut(null);
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn):
                Path workdir = this.workdir();
                // selected files on the local filesystem
                NSArray selected = sheet.filenames();
                java.util.Enumeration enum = selected.objectEnumerator();
                Queue q = new UploadQueue((Observer) this);
                Session session = workdir.getSession().copy();
                while (enum.hasMoreElements()) {
                    q.addRoot(PathFactory.createPath(session,
                            workdir.getAbsolute(),
                            new Local((String) enum.nextElement())));
                }
                CDQueueController.instance().startItem(q);
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }

    public void insideButtonClicked(Object sender) {
        log.debug("insideButtonClicked");
		this.browserRowDoubleClicked(null);
	}

    public void backButtonClicked(Object sender) {
        log.debug("backButtonClicked");
		this.deselectAll();
        this.workdir().getSession().getPreviousPath().list(this.encoding, false, this.getFileFilter());
    }

    public void upButtonClicked(Object sender) {
        log.debug("upButtonClicked");
		this.deselectAll();
        Path previous = this.workdir();
        List listing = this.workdir().getParent().list(this.encoding, false, this.getFileFilter());
		this.selectRow(listing.indexOf(previous));
    }

    public void connectButtonClicked(Object sender) {
        log.debug("connectButtonClicked");
        CDConnectionController controller = new CDConnectionController(this);
        this.beginSheet(controller.window());
    }

    public void disconnectButtonClicked(Object sender) {
		this.unmount();
    }

    public void showHiddenFilesClicked(Object sender) {
        if (sender instanceof NSMenuItem) {
            NSMenuItem item = (NSMenuItem) sender;
            if(item.state() == NSCell.OnState) {
				this.filenameFilter = new NullFilter();
			}
			if(item.state() == NSCell.OffState) {
				this.filenameFilter = new HiddenFilesFilter();
			}
            if (this.isMounted()) {
				this.deselectAll();
                this.workdir().list(this.encoding, true, this.getFileFilter());
            }
        }
    }

    public boolean isMounted() {
        return this.workdir() != null;
    }

    public boolean isConnected() {
        boolean connected = false;
        if (this.isMounted()) {
            connected = this.workdir().getSession().isConnected();
        }
        log.info("Connected:" + connected);
        return connected;
    }

    public void paste(Object sender) {
        log.debug("paste");
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
            if (o != null) {
                this.deselectAll();
                NSArray elements = (NSArray) o;
                for (int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    Queue q = Queue.createQueue(dict);
                    Path workdir = this.workdir();
                    for (Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                        Path p = (Path) iter.next();
                        PathFactory.createPath(workdir.getSession(), p.getAbsolute()).rename(workdir.getAbsolute() + "/" + p.getName());
                        p.getParent().invalidate();
                        workdir.list(this.encoding, true, this.getFileFilter());
                    }
                }
                pboard.setPropertyListForType(null, "QueuePBoardType");
                this.reloadData();
            }
        }
    }

    public void copy(Object sender) {
        if (this.getSelectionCount() > 0) {
            Session session = this.workdir().getSession().copy();
            Queue q = new DownloadQueue();
			for(Iterator i = this.getSelectedPaths().iterator(); i.hasNext(); ) {
				Path path = (Path)i.next();
                q.addRoot(path.copy(session));
            }
            // Writing data for private use when the item gets dragged to the transfer queue.
            NSPasteboard queuePboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            queuePboard.declareTypes(new NSArray("QueuePBoardType"), null);
            if (queuePboard.setPropertyListForType(new NSArray(q.getAsDictionary()), "QueuePBoardType")) {
                log.debug("QueuePBoardType data sucessfully written to pasteboard");
            }
            Path p = this.getSelectedPath();
            NSPasteboard pboard = NSPasteboard.pasteboardWithName(NSPasteboard.GeneralPboard);
            pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
            if (!pboard.setStringForType(p.getAbsolute(), NSPasteboard.StringPboardType)) {
                log.error("Error writing absolute path of selected item to NSPasteboard.StringPboardType.");
            }
        }
    }

    public void copyURLButtonClicked(Object sender) {
        log.debug("copyURLButtonClicked");
        Host h = this.workdir().getSession().getHost();
        NSPasteboard pboard = NSPasteboard.pasteboardWithName(NSPasteboard.GeneralPboard);
        pboard.declareTypes(new NSArray(NSPasteboard.StringPboardType), null);
        if (!pboard.setStringForType(h.getURL() + this.workdir().getAbsolute(), NSPasteboard.StringPboardType)) {
            log.error("Error writing URL to NSPasteboard.StringPboardType.");
        }
    }

    protected Path workdir() {
        return this.workdir;
    }

    private void init(Host host, Session session) {
		this.workdir = null;
		this.reloadData();
        TranscriptFactory.addImpl(host.getHostname(), new CDTranscriptImpl(this.logView));
        this.window().setTitle(host.getProtocol() + ":" + host.getCredentials().getUsername() + "@" + host.getHostname());
        File bookmark = new File(HISTORY_FOLDER + "/" + host.getHostname() + ".duck");
        CDBookmarkTableDataSource.instance().exportBookmark(host, bookmark);
        this.window().setRepresentedFilename(bookmark.getAbsolutePath());
        session.addObserver((Observer) this);
    }

    public Session mount(Host host) {
        return this.mount(host, this.encoding);
    }

    public Session mount(Host host, final String encoding) {
        log.debug("mount:" + host);
        if (this.unmount(new NSSelector("mountSheetDidEnd",
                new Class[]{NSWindow.class, int.class, Object.class}), host// end selector
        )) {
            final Session session = SessionFactory.createSession(host);
            this.init(host, session);
            this.changeEncoding(encoding, false);
            if (session instanceof ch.cyberduck.core.sftp.SFTPSession) {
                ((ch.cyberduck.core.sftp.SFTPSession) session).setHostKeyVerificationController(new CDHostKeyController(this));
            }
            if (session instanceof ch.cyberduck.core.ftps.FTPSSession) {
                ((ch.cyberduck.core.ftps.FTPSSession) session).setTrustManager(
                        new CDX509TrustManagerController(this));
            }
            host.setLoginController(new CDLoginController(this));
            new Thread("Session") {
                public void run() {
                    session.mount(encoding, getFileFilter());
                }
            }.start();
            return session;
        }
        return null;
    }

    public void mountSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.unmountSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.mount((Host) contextInfo);
        }
    }

    public void unmount() {
        if (this.isMounted()) {
            this.workdir().getSession().close();
            TranscriptFactory.removeImpl(this.workdir().getSession().getHost().getHostname());
			//this.workdir = null;
        }
    }

    /**
     * @return True if the unmount process has finished, false if the user has to agree first to close the connection
     */
    public boolean unmount(NSSelector selector, Object context) {
        log.debug("unmount");
        if (this.isConnected()) {
            if (Preferences.instance().getBoolean("browser.confirmDisconnect")) {
                NSApplication.sharedApplication().beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Disconnect from", "Alert sheet title") + " " + this.workdir().getSession().getHost().getHostname(), //title
                        NSBundle.localizedString("The connection will be closed.", "Alert sheet text"), // message
                        NSBundle.localizedString("Disconnect", "Alert sheet default button"), // defaultbutton
                        NSBundle.localizedString("Cancel", "Alert sheet alternate button"), // alternate button
                        null //other button
                ),
                        this.window(),
                        this,
                        selector,
                        context);
                return false;
            }
            this.unmount();
        }
        return true;
    }

    public boolean loadDataRepresentation(NSData data, String type) {
        if (type.equals("Cyberduck Bookmark")) {
            String[] errorString = new String[]{null};
            Object propertyListFromXMLData =
                    NSPropertyListSerialization.propertyListFromData(data,
                            NSPropertyListSerialization.PropertyListImmutable,
                            new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                            errorString);
            if (errorString[0] != null) {
                log.error("Problem reading bookmark file: " + errorString[0]);
            }
            else {
                log.debug("Successfully read bookmark file: " + propertyListFromXMLData);
            }
            if (propertyListFromXMLData instanceof NSDictionary) {
                this.mount(new Host((NSDictionary) propertyListFromXMLData));
            }
            return true;
        }
        return false;
    }

    public NSData dataRepresentationOfType(String type) {
        if (this.isMounted()) {
            if (type.equals("Cyberduck Bookmark")) {
                Host bookmark = this.workdir().getSession().getHost();
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(bookmark.getAsDictionary(),
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if (errorString[0] != null) {
                    log.error("Problem writing bookmark file: " + errorString[0]);
                }
                return collection;
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------

    public static int applicationShouldTerminate(NSApplication app) {
        // Determine if there are any open connections
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        // Determine if there are any open connections
        while (0 != count--) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count);
            CDBrowserController controller = CDBrowserController.controllerForWindow(window);
            if (null != controller) {
                if (!controller.unmount(new NSSelector("terminateReviewSheetDidEnd",
                        new Class[]{NSWindow.class, int.class, Object.class}),
                        null)) {
                    return NSApplication.TerminateLater;
                }
            }
        }
        return CDQueueController.applicationShouldTerminate(app);
    }

    public boolean windowShouldClose(NSWindow sender) {
        return this.unmount(new NSSelector("closeSheetDidEnd",
                new Class[]{NSWindow.class, int.class, Object.class}), null // end selector
        );
    }

    public void unmountSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.unmount();
        }
    }

    public void closeSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.unmountSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.window().close();
        }
        if (returncode == NSAlertPanel.AlternateReturn) {
            //
        }
    }

    public void terminateReviewSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.closeSheetDidEnd(sheet, returncode, contextInfo);
        if (returncode == NSAlertPanel.DefaultReturn) { //Disconnect
            CDBrowserController.applicationShouldTerminate(null);
        }
        if (returncode == NSAlertPanel.AlternateReturn) { //Cancel
            NSApplication.sharedApplication().replyToApplicationShouldTerminate(false);
        }
    }

    public void windowWillClose(NSNotification notification) {
        log.debug("windowWillClose");
        NSNotificationCenter.defaultCenter().removeObserver(this);
        if (this.isMounted()) {
            this.workdir().getSession().deleteObserver((Observer) this);
        }
        instances.removeObject(this);
    }

    public boolean validateMenuItem(NSMenuItem item) {
        if (item.action().name().equals("paste:")) {
            if (this.isMounted()) {
                NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null
                        && pboard.propertyListForType("QueuePBoardType") != null) {
                    NSArray elements = (NSArray) pboard.propertyListForType("QueuePBoardType");
                    for (int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                        Queue q = Queue.createQueue(dict);
                        if (q.numberOfRoots() == 1)
                            item.setTitle(NSBundle.localizedString("Paste", "Menu item") + " \"" + q.getRoot().getName() + "\"");
                        else {
                            item.setTitle(NSBundle.localizedString("Paste", "Menu item")
                                    + " " + q.numberOfRoots() + " " +
                                    NSBundle.localizedString("files", ""));
                        }
                    }
                }
                else {
                    item.setTitle(NSBundle.localizedString("Paste", "Menu item"));
                }
            }
        }
        if (item.action().name().equals("copy:")) {
            if (this.isMounted() && this.getSelectionCount() > 0) {
                if (this.getSelectionCount() == 1) {
                    Path p = this.getSelectedPath();
                    item.setTitle(NSBundle.localizedString("Copy", "Menu item") + " \"" + p.getName() + "\"");
                }
                else {
                    item.setTitle(NSBundle.localizedString("Copy", "Menu item")
                            + " " + this.getSelectionCount() + " " +
                            NSBundle.localizedString("files", ""));
				}
            }
            else
                item.setTitle(NSBundle.localizedString("Copy", "Menu item"));
        }
        if (item.action().name().equals("showHiddenFilesClicked:")) {
            item.setState((this.getFileFilter() instanceof NullFilter) ? NSCell.OnState : NSCell.OffState);
        }
        if (item.action().name().equals("encodingButtonClicked:")) {
            item.setState(this.encoding.equalsIgnoreCase(item.title()) ? NSCell.OnState : NSCell.OffState);
        }
        if (item.action().name().equals("browserSwitchClicked:")) {
			if(item.tag() == Preferences.instance().getInteger("browser.view"))
				item.setState(NSCell.OnState);
			else
				item.setState(NSCell.OffState);
        }
        return this.validateItem(item.action().name());
    }

    private boolean validateItem(String identifier) {
        if (identifier.equals("copy:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("paste:")) {
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            return this.isMounted()
                    && pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null
                    && pboard.propertyListForType("QueuePBoardType") != null;
        }
        if (identifier.equals("showHiddenFilesClicked:")) {
            return true;
        }
        if (identifier.equals("encodingButtonClicked:")) {
            return true;
        }
        if (identifier.equals("addBookmarkButtonClicked:")) {
            return true;
        }
        if (identifier.equals("deleteBookmarkButtonClicked:")) {
            return bookmarkTable.selectedRow() != -1;
        }
        if (identifier.equals("editBookmarkButtonClicked:")) {
            return bookmarkTable.numberOfSelectedRows() == 1;
        }
		if(identifier.equals("Edit") || identifier.equals("editButtonClicked:")) {
			if(this.isMounted() && this.getSelectionCount() > 0) {
				Path p = this.getSelectedPath();
				String editorPath = null;
				NSSelector absolutePathForAppBundleWithIdentifierSelector =
				    new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
				if(absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
					editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(Preferences.instance().getProperty("editor.bundleIdentifier"));
				}
				return p.attributes.isFile() && editorPath != null;
			}
			return false;
		}
        if (identifier.equals("gotoButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Get Info") || identifier.equals("infoButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("New Folder") || identifier.equals("createFolderButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("New File") || identifier.equals("createFileButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Duplicate File") || identifier.equals("duplicateFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("Delete") || identifier.equals("deleteFileButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("Refresh") || identifier.equals("reloadButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Download") || identifier.equals("downloadButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("Upload") || identifier.equals("uploadButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Synchronize") || identifier.equals("syncButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Download As") || identifier.equals("downloadAsButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() == 1;
        }
        if (identifier.equals("insideButtonClicked:")) {
            return this.isMounted() && this.getSelectionCount() > 0;
        }
        if (identifier.equals("upButtonClicked:")) {
            return this.isMounted() && !this.workdir().isRoot();
        }
        if (identifier.equals("backButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("copyURLButtonClicked:")) {
            return this.isMounted();
        }
        if (identifier.equals("Disconnect") || identifier.equals("disconnectButtonClicked:")) {
            return this.isMounted() && this.workdir().getSession().isConnected();
        }
        return true; // by default everything is enabled
    }

    // ----------------------------------------------------------
    // Toolbar Delegate
    // ----------------------------------------------------------

    public boolean validateToolbarItem(NSToolbarItem item) {
        boolean enabled = this.pathPopupItems.size() > 0;
        this.backButton.setEnabled(enabled);
        this.upButton.setEnabled(enabled);
        this.pathPopupButton.setEnabled(enabled);
        this.searchField.setEnabled(enabled);
        return this.validateItem(item.itemIdentifier());
    }

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
        NSToolbarItem item = new NSToolbarItem(itemIdentifier);
        if (itemIdentifier.equals("Browser View")) {
            item.setLabel(NSBundle.localizedString("View", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("View", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Switch Browser View", "Toolbar item tooltip"));
            item.setView(browserSwitchView);
            NSMenuItem viewMenu = new NSMenuItem();
			viewMenu.setTitle(NSBundle.localizedString("View", "Toolbar item"));
            NSMenu viewSubmenu = new NSMenu();
			viewSubmenu.addItem(new NSMenuItem(NSBundle.localizedString("List", "Toolbar item"),
											   new NSSelector("browserSwitchClicked", new Class[]{Object.class}),
											   ""));
			viewSubmenu.itemWithTitle(NSBundle.localizedString("List", "Toolbar item")).setTag(0);
			viewSubmenu.addItem(new NSMenuItem(NSBundle.localizedString("Outline", "Toolbar item"),
											   new NSSelector("browserSwitchClicked", new Class[]{Object.class}),
											   ""));
			viewSubmenu.itemWithTitle(NSBundle.localizedString("Outline", "Toolbar item")).setTag(1);
			viewSubmenu.addItem(new NSMenuItem(NSBundle.localizedString("Column", "Toolbar item"),
											   new NSSelector("browserSwitchClicked", new Class[]{Object.class}),
											   ""));
			viewSubmenu.itemWithTitle(NSBundle.localizedString("Column", "Toolbar item")).setTag(2);
            viewMenu.setSubmenu(viewSubmenu);
            item.setMenuFormRepresentation(viewMenu);
			item.setMinSize(browserSwitchView.frame().size());
            item.setMaxSize(browserSwitchView.frame().size());
            return item;
        }
        if (itemIdentifier.equals("New Connection")) {
            item.setLabel(NSBundle.localizedString("New Connection", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("New Connection", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("connect.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("connectButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Bookmarks")) {
            item.setLabel(NSBundle.localizedString("Bookmarks", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Bookmarks", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Toggle Bookmarks", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("drawer.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("toggleBookmarkDrawer", new Class[]{Object.class}));
            return item;
        }
		if (itemIdentifier.equals("Tools")) {
            item.setLabel(NSBundle.localizedString("Action", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Action", "Toolbar item"));
            item.setView(actionPopupButton);
            NSMenuItem toolMenu = new NSMenuItem();
			toolMenu.setTitle(NSBundle.localizedString("Action", "Toolbar item"));
            NSMenu toolSubmenu = new NSMenu();
			for(int i = 1; i < actionPopupButton.menu().numberOfItems(); i++) {
				NSMenuItem template = actionPopupButton.menu().itemAtIndex(i);
				toolSubmenu.addItem(new NSMenuItem(template.title(),
												   template.action(),
												   template.keyEquivalent()));
			}
            toolMenu.setSubmenu(toolSubmenu);
            item.setMenuFormRepresentation(toolMenu);
            item.setMinSize(actionPopupButton.frame().size());
            item.setMaxSize(actionPopupButton.frame().size());
            return item;
		}
        if (itemIdentifier.equals("Quick Connect")) {
            item.setLabel(NSBundle.localizedString("Quick Connect", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Quick Connect", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Connect to server", "Toolbar item tooltip"));
            item.setView(quickConnectPopup);
            item.setMinSize(quickConnectPopup.frame().size());
            item.setMaxSize(quickConnectPopup.frame().size());
            return item;
        }
        if (itemIdentifier.equals("Encoding")) {
            item.setLabel(NSBundle.localizedString("Encoding", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Encoding", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Character Encoding", "Toolbar item tooltip"));
            item.setView(encodingPopup);
            NSMenuItem encodingMenu = new NSMenuItem(NSBundle.localizedString("Encoding", "Toolbar item"),
                    new NSSelector("encodingButtonClicked", new Class[]{Object.class}),
                    "");
            java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
            java.util.Iterator iter = charsets.values().iterator();
            NSMenu charsetMenu = new NSMenu();
            while (iter.hasNext()) {
                charsetMenu.addItem(new NSMenuItem(((java.nio.charset.Charset) iter.next()).name(),
                        new NSSelector("encodingButtonClicked", new Class[]{Object.class}),
                        ""));
            }
            encodingMenu.setSubmenu(charsetMenu);
            item.setMenuFormRepresentation(encodingMenu);
            item.setMinSize(encodingPopup.frame().size());
            item.setMaxSize(encodingPopup.frame().size());
            return item;
        }
        if (itemIdentifier.equals("Refresh")) {
            item.setLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Refresh", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Refresh directory listing", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("reload.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("reloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Download")) {
            item.setLabel(NSBundle.localizedString("Download", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Download", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Download file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("downloadFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("downloadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Upload")) {
            item.setLabel(NSBundle.localizedString("Upload", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Upload", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Upload local file to the remote host", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("uploadFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("uploadButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Synchronize")) {
            item.setLabel(NSBundle.localizedString("Synchronize", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Synchronize", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Synchronize files", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("sync32.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("syncButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Get Info")) {
            item.setLabel(NSBundle.localizedString("Get Info", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Get Info", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Show file attributes", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("info.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("infoButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Edit")) {
            item.setLabel(NSBundle.localizedString("Edit", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Edit", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Edit file in external editor", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("pencil.tiff"));
            NSSelector absolutePathForAppBundleWithIdentifierSelector =
                    new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
            if (absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
                String editorPath = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(Preferences.instance().getProperty("editor.bundleIdentifier"));
                if (editorPath != null) {
                    item.setImage(NSWorkspace.sharedWorkspace().iconForFile(editorPath));
                }
            }
            item.setTarget(this);
            item.setAction(new NSSelector("editButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Delete")) {
            item.setLabel(NSBundle.localizedString("Delete", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Delete", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Delete file", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("deleteFile.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("deleteFileButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("New Folder")) {
            item.setLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("New Folder", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Create New Folder", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("newfolder.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("createFolderButtonClicked", new Class[]{Object.class}));
            return item;
        }
        if (itemIdentifier.equals("Disconnect")) {
            item.setLabel(NSBundle.localizedString("Disconnect", "Toolbar item"));
            item.setPaletteLabel(NSBundle.localizedString("Disconnect", "Toolbar item"));
            item.setToolTip(NSBundle.localizedString("Disconnect from server", "Toolbar item tooltip"));
            item.setImage(NSImage.imageNamed("eject.tiff"));
            item.setTarget(this);
            item.setAction(new NSSelector("disconnectButtonClicked", new Class[]{Object.class}));
            return item;
        }
        // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
            "New Connection",
            NSToolbarItem.SeparatorItemIdentifier,
            "Browser View",
            "Bookmarks",
            "Quick Connect",
			"Tools",
            "Refresh",
            "Get Info",
            "Edit",
            "Download",
            "Upload",
            NSToolbarItem.FlexibleSpaceItemIdentifier,
            "Disconnect"
        });
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
        return new NSArray(new Object[]{
            "New Connection",
            "Browser View",
            "Bookmarks",
            "Quick Connect",
			"Tools",
            "Refresh",
            "Encoding",
            "Synchronize",
            "Download",
            "Upload",
            "Edit",
            "Delete",
            "New Folder",
            "Get Info",
            "Disconnect",
            NSToolbarItem.CustomizeToolbarItemIdentifier,
            NSToolbarItem.SpaceItemIdentifier,
            NSToolbarItem.SeparatorItemIdentifier,
            NSToolbarItem.FlexibleSpaceItemIdentifier
        });
    }
}
