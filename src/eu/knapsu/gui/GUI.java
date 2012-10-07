package eu.knapsu.gui;

import java.util.*;

import eu.knapsu.*;
import eu.knapsu.logging.Level;
import eu.knapsu.logging.Logger;
import eu.knapsu.vfs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class GUI {

	private Display display;
	private Shell shell;

	private ToolBar toolbar;
	private Tree tree;
	private Text text;
	private Composite statusbar;
	private TrayItem tray;

	private TreeItem treeRoot;
	private ToolItem toolItemStartStop;
	private Label connectionsCount;
	private ArrayList<String> logBuffer = new ArrayList<String>(10);

	private void createContents() {
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 3;
		shellLayout.makeColumnsEqualWidth = true;
		shell.setLayout(shellLayout);

		createToolBar();
		createTreeWindow();
		createLogWindow();
		createStatusBar();
		createTrayIcon();

		setupMainWindow();
		setupToolBar();
		setupTreeWindow();
		setupDragAndDrop();
		setupStatusBar();
		setupTrayIcon();
	}

	private void createLogWindow() {
		text = new Text(shell, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);

		while (logBuffer.isEmpty() == false) {
			text.append(logBuffer.get(0) + "\n");
			logBuffer.remove(0);
		}

		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		text.setLayoutData(data);
	}

	private void createStatusBar() {
		statusbar = new Composite(shell, SWT.NONE);

		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		statusbar.setLayoutData(data);

		GridLayout statusbarLayout = new GridLayout();
		statusbarLayout.numColumns = 3;
		statusbarLayout.marginWidth = 0;
		statusbarLayout.marginHeight = 0;
		statusbarLayout.horizontalSpacing = 0;
		statusbarLayout.verticalSpacing = 0;
		statusbar.setLayout(statusbarLayout);
	}

	private void createToolBar() {
		toolbar = new ToolBar(shell, SWT.HORIZONTAL | SWT.RIGHT);

		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		toolbar.setLayoutData(data);
	}

	private void createTrayIcon() {
		Tray systemTray = display.getSystemTray();
		if (systemTray != null) {
			tray = new TrayItem(systemTray, SWT.NONE);
			tray.setVisible(Settings.getPropertyBoolean("use_system_tray"));
			if (FancyFileServer.isServerRunning()) {
				tray.setImage(ImageManager.getImage("tray_online"));
			} else {
				tray.setImage(ImageManager.getImage("tray_offline"));
			}
		}
	}

	private boolean createTreeItem(TreeItem parent, FileObject fileObject) {
		FileObject parentData = (FileObject) parent.getData();
		if (parentData.addChild(fileObject) == false) {
			Logger.log(Level.INFO, Messages.getString("can_not_add_file_directory", Settings.getLocale()));
			return false;
		}

		String childName;
		String newChildName = fileObject.getName();

		int index = 0;
		while (index < parent.getItems().length) {
			childName = parent.getItem(index).getText();
			int compare = newChildName.compareToIgnoreCase(childName);
			if (compare < 0) {
				break;
			} else if (compare == 0) {
				compare = newChildName.compareTo(childName);
				if (compare < 0) {
					break;
				}
			}
			index++;
		}

		TreeItem newItem = new TreeItem(parent, SWT.NONE, index);
		newItem.setText(fileObject.getName());
		if (fileObject instanceof VirtualFile) {
			newItem.setImage(ImageManager.getImage("directory_24"));
		} else if (fileObject.isDirectory()) {
			newItem.setImage(ImageManager.getImage("directory_red_24"));
		} else {
			newItem.setImage(ImageManager.getImage("file_24"));
		}
		newItem.setData(fileObject);

		parent.setExpanded(true);
		return true;
	}

	private void createTreeWindow() {
		tree = new Tree(shell, SWT.SINGLE | SWT.BORDER);

		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		tree.setLayoutData(data);
	}

	private void deleteTreeItem(TreeItem item) {
		if (item != treeRoot) {
			for (TreeItem i : item.getItems()) {
				deleteTreeItem(i);
			}

			((FileObject) item.getParentItem().getData()).removeChild((FileObject) item.getData());
			item.dispose();
		}
	}

	private void setupMainWindow() {
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellIconified(ShellEvent event) {
				if (tray != null && tray.getVisible()) {
					shell.setVisible(false);
				}
				event.doit = false;
			}
		});

	}

	private void setupDragAndDrop() {
		int operations = DND.DROP_DEFAULT | DND.DROP_MOVE;
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] { fileTransfer };

		DropTarget target = new DropTarget(tree, operations);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (fileTransfer.isSupportedType(event.currentDataType)) {
					TreeItem item = (TreeItem) event.item;
					for (String filePath : (String[]) event.data) {
						createTreeItem(item, new LocalFile(filePath));
					}
				}
			}

			@Override
			public void dropAccept(DropTargetEvent event) {
				if ((event.item == null) || (event.item.getData() instanceof LocalFile)) {
					event.detail = DND.DROP_NONE;
				}
			}
		});
	}

	private void setupStatusBar() {
		GridData data;

		Label connectionsLabel = new Label(statusbar, SWT.NONE);
		connectionsLabel.setText(Messages.getString("connections_", Settings.getLocale()));

		connectionsCount = new Label(statusbar, SWT.NONE);
		if (FancyFileServer.getHTTPServer() != null) {
			connectionsCount.setText(String.valueOf(FancyFileServer.getHTTPServer().getConnectionsCount()));
		}

		Label emptySpace = new Label(statusbar, SWT.NONE);
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		emptySpace.setLayoutData(data);
	}

	private void setupToolBar() {
		toolItemStartStop = new ToolItem(toolbar, SWT.PUSH);
		toolItemStartStop.setText(Messages.getString("start_stop", Settings.getLocale()));
		if (FancyFileServer.isServerRunning()) {
			toolItemStartStop.setImage(ImageManager.getImage("toolbar_online"));
		} else {
			toolItemStartStop.setImage(ImageManager.getImage("toolbar_offline"));
		}
		toolItemStartStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FancyFileServer.startStopServer();
			}
		});

		ToolItem toolItemSettings = new ToolItem(toolbar, SWT.PUSH);
		toolItemSettings.setText(Messages.getString("settings", Settings.getLocale()));
		toolItemSettings.setImage(ImageManager.getImage("toolbar_settings"));
		toolItemSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SettingsDialog dialog = new SettingsDialog(shell);
				dialog.open();
			}
		});

		// ToolItem toolItemStatusBar = new ToolItem(toolbar, SWT.CHECK);
		// toolItemStatusBar.setText(Messages.getString("hide_statusbar",
		// Settings.getLocale()));
		// toolItemStatusBar.setImage(ImageManager.getImage("toolbar_statusbar"));
		// toolItemStatusBar.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// GridData data = (GridData) statusbar.getLayoutData();
		// if (data.exclude == false) {
		// statusbar.setVisible(false);
		// data.exclude = true;
		// } else {
		// statusbar.setVisible(true);
		// data.exclude = false;
		// }
		// shell.layout();
		// }
		// });

		ToolItem toolItemTrayIcon = new ToolItem(toolbar, SWT.CHECK);
		toolItemTrayIcon.setText(Messages.getString("use_system_tray", Settings.getLocale()));
		toolItemTrayIcon.setImage(ImageManager.getImage("toolbar_tray"));
		toolItemTrayIcon.setSelection(Settings.getPropertyBoolean("use_system_tray"));
		toolItemTrayIcon.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tray != null) {
					boolean useTray = !Settings.getPropertyBoolean("use_system_tray");
					tray.setVisible(useTray);
					Settings.setProperty("use_system_tray", useTray);
				}
			}
		});

		ToolItem toolItemAbout = new ToolItem(toolbar, SWT.PUSH);
		toolItemAbout.setText(Messages.getString("about", Settings.getLocale()));
		toolItemAbout.setImage(ImageManager.getImage("toolbar_about"));
		toolItemAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(shell);
				dialog.open();
			}
		});
	}

	private void setupTrayIcon() {
		if (tray == null) {
			return;
		}
		final Menu menu = new Menu(shell, SWT.POP_UP);
		MenuItem item;

		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("open", Settings.getLocale()));
		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				shell.setVisible(true);
				shell.setActive();
			}
		});

		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("start_stop", Settings.getLocale()));
		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				FancyFileServer.startStopServer();
			}
		});

		item = new MenuItem(menu, SWT.SEPARATOR);

		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.getString("close", Settings.getLocale()));
		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				shell.close();
			}
		});

		tray.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.setVisible(true);
			}
		});
		tray.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				menu.setVisible(true);
			}
		});
	}

	private void setupTreeWindow() {
		treeRoot = new TreeItem(tree, SWT.NONE);
		treeRoot.setText("/");
		treeRoot.setImage(ImageManager.getImage("directory_24"));
		treeRoot.setData(FancyFileServer.getVFS().getRoot());

		Menu popupMenu = new Menu(shell);

		final MenuItem itemNewDirectory = new MenuItem(popupMenu, SWT.NONE);
		itemNewDirectory.setText(Messages.getString("new_directory", Settings.getLocale()));
		itemNewDirectory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				NewDirectoryDialog dialog = new NewDirectoryDialog(shell);
				String newDirectoryName = dialog.open();

				if (newDirectoryName != null) {
					createTreeItem(tree.getSelection()[0], new VirtualFile(newDirectoryName));
				}
			}
		});

		final MenuItem itemAddDirectory = new MenuItem(popupMenu, SWT.NONE);
		itemAddDirectory.setText(Messages.getString("add_directory", Settings.getLocale()));
		itemAddDirectory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dialog = new DirectoryDialog(shell, SWT.NONE);
				dialog.setText(Messages.getString("add_directory", Settings.getLocale()));
				String path = dialog.open();

				if (path != null) {
					createTreeItem(tree.getSelection()[0], new LocalFile(path));
				}
			}
		});

		final MenuItem itemAddFile = new MenuItem(popupMenu, SWT.NONE);
		itemAddFile.setText(Messages.getString("add_file", Settings.getLocale()));
		itemAddFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FileDialog dialog = new FileDialog(shell, SWT.NONE);
				dialog.setText(Messages.getString("add_file", Settings.getLocale()));
				String path = dialog.open();

				if (path != null) {
					createTreeItem(tree.getSelection()[0], new LocalFile(path));
				}
			}
		});

		new MenuItem(popupMenu, SWT.SEPARATOR);

		final MenuItem itemRemove = new MenuItem(popupMenu, SWT.NONE);
		itemRemove.setText(Messages.getString("remove", Settings.getLocale()));
		itemRemove.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				deleteTreeItem(tree.getSelection()[0]);
			}
		});

		popupMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				if (tree.getSelection()[0] == treeRoot) {
					itemRemove.setEnabled(false);
				} else {
					itemRemove.setEnabled(true);
				}

				if (tree.getSelection()[0].getData() instanceof LocalFile) {
					itemNewDirectory.setEnabled(false);
					itemAddDirectory.setEnabled(false);
					itemAddFile.setEnabled(false);
				} else {
					itemNewDirectory.setEnabled(true);
					itemAddDirectory.setEnabled(true);
					itemAddFile.setEnabled(true);
				}
			}
		});

		tree.setMenu(popupMenu);
	}

	public void start() {
		ImageManager.loadImages();

		display = Display.getDefault();
		shell = new Shell(display, SWT.TITLE | SWT.MIN | SWT.CLOSE | SWT.BORDER);
		shell.setText(FancyFileServer.NAME);
		shell.setSize(800, 500);
		shell.setImage(ImageManager.getImage("logo"));

		createContents();
		updateServerStatus();

		shell.open();
		while (shell.isDisposed() == false) {
			if (display.readAndDispatch() == false) {
				display.sleep();
			}
		}

		shell.dispose();
		display.dispose();
	}

	public void updateConnectionsCounter() {
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (connectionsCount.isDisposed() == false) {
					if (FancyFileServer.getHTTPServer() != null) {
						connectionsCount.setText(String.valueOf(FancyFileServer.getHTTPServer().getConnectionsCount()));
					}
				}
			}
		});
	}

	public void updateLog(final String message) {
		if (display == null || display.isDisposed()) {
			logBuffer.add(message);
			return;
		}
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (text.isDisposed() == false) {
					while (logBuffer.isEmpty() == false) {
						text.append(logBuffer.get(0) + "\n");
						logBuffer.remove(0);
					}
					if (text.getLineCount() > 100) {
						int index = text.getText().indexOf("\n");
						text.setText(text.getText().substring(index + 1));
					}
					text.append(message + "\n");
				} else {
					logBuffer.add(message);
				}
			}
		});
	}

	public void updateServerStatus() {
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (toolItemStartStop.isDisposed() == false) {
					if (FancyFileServer.isServerRunning()) {
						toolItemStartStop.setImage(ImageManager.getImage("toolbar_online"));
					} else {
						toolItemStartStop.setImage(ImageManager.getImage("toolbar_offline"));
					}
				}
				if (tray != null && tray.isDisposed() == false) {
					if (FancyFileServer.isServerRunning()) {
						tray.setToolTipText(Messages.getString("server_started", Settings.getLocale()));
						tray.setImage(ImageManager.getImage("tray_online"));
					} else {
						tray.setToolTipText(Messages.getString("server_stopped", Settings.getLocale()));
						tray.setImage(ImageManager.getImage("tray_offline"));
					}
				}
			}
		});
	}

}
