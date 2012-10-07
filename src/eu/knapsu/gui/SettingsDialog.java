package eu.knapsu.gui;

import eu.knapsu.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class SettingsDialog extends Dialog {

	private Shell parent;
	private Shell dialog;

	private Text serverPort;
	private Button showHiddenFiles;

	public SettingsDialog(Shell parent) {
		super(parent, SWT.NONE);
		this.parent = parent;
	}

	private void createButtons() {
		GridData data;

		Composite buttons = new Composite(dialog, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.CENTER;
		data.grabExcessHorizontalSpace = true;
		buttons.setLayoutData(data);

		GridLayout buttonsLayout = new GridLayout();
		buttonsLayout.numColumns = 2;
		buttonsLayout.marginWidth = 0;
		buttonsLayout.marginHeight = 0;
		buttons.setLayout(buttonsLayout);

		Button buttonCancel = new Button(buttons, SWT.PUSH);
		buttonCancel.setText(Messages.getString("cancel", Settings.getLocale()));
		buttonCancel.setImage(ImageManager.getImage("button_cancel"));
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		data.grabExcessHorizontalSpace = true;
		buttonCancel.setLayoutData(data);
		buttonCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dialog.dispose();
			}
		});

		Button buttonOK = new Button(buttons, SWT.PUSH);
		buttonOK.setText(Messages.getString("ok", Settings.getLocale()));
		buttonOK.setImage(ImageManager.getImage("button_ok"));
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		data.widthHint = buttonCancel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		buttonOK.setLayoutData(data);
		buttonOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Integer.parseInt(serverPort.getText());
					Settings.setProperty("server_port", serverPort.getText());
				} catch (NumberFormatException ignore) {
				}
				;

				Settings.setProperty("show_hidden_files", String.valueOf(showHiddenFiles.getSelection()));
				dialog.dispose();
			}
		});
	}

	private void createContents() {
		GridLayout dialogLayout = new GridLayout();
		dialogLayout.numColumns = 2;
		dialog.setLayout(dialogLayout);

		GridData data;

		Label serverPortLabel = new Label(dialog, SWT.NONE);
		serverPortLabel.setText(Messages.getString("server_port_", Settings.getLocale()));
		data = new GridData();
		data.minimumWidth = 200;
		serverPortLabel.setLayoutData(data);

		serverPort = new Text(dialog, SWT.SINGLE | SWT.BORDER);
		serverPort.setTextLimit(5);
		serverPort.setText(String.valueOf(Settings.getPropertyInteger("server_port")));
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		serverPort.setLayoutData(data);

		Label styleSheetLabel = new Label(dialog, SWT.NONE);
		styleSheetLabel.setText(Messages.getString("style_sheet_", Settings.getLocale()));
		data = new GridData();
		data.minimumWidth = 100;
		styleSheetLabel.setLayoutData(data);

		Combo styleSheet = new Combo(dialog, SWT.READ_ONLY);
		styleSheet.add(Messages.getString("default", Settings.getLocale()));
		styleSheet.select(0);
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		styleSheet.setLayoutData(data);

		showHiddenFiles = new Button(dialog, SWT.CHECK);
		showHiddenFiles.setText(Messages.getString("show_hidden_files", Settings.getLocale()));
		showHiddenFiles.setSelection(Settings.getPropertyBoolean("show_hidden_files"));
		data = new GridData();
		data.horizontalSpan = 2;
		showHiddenFiles.setLayoutData(data);

		createButtons();
	}

	public void open() {
		Display display = Display.getDefault();
		dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setImage(ImageManager.getImage("toolbar_settings"));
		dialog.setText(Messages.getString("settings", Settings.getLocale()));

		createContents();

		dialog.pack();
		dialog.open();
		while (dialog.isDisposed() == false) {
			if (display.readAndDispatch() == false) {
				display.sleep();
			}
		}
		dialog.dispose();
	}

}
