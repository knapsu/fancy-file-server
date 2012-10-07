package eu.knapsu.gui;

import java.io.*;

import eu.knapsu.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NewDirectoryDialog extends Dialog {

	private Shell parent;
	private Shell dialog;

	private String result = null;
	private Text input;

	public NewDirectoryDialog(Shell parent) {
		super(parent, SWT.NONE);
		this.parent = parent;
	}

	private void createButtons() {
		GridData data;

		Composite buttons = new Composite(dialog, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.FILL;
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
				if (input.getText().length() != 0) {
					result = input.getText();
				}
				dialog.dispose();
			}
		});

		dialog.setDefaultButton(buttonOK);
	}

	private void createContents() {
		GridLayout dialogLayout = new GridLayout();
		dialogLayout.numColumns = 2;
		dialog.setLayout(dialogLayout);

		GridData data;

		Label directoryImage = new Label(dialog, SWT.NONE);
		directoryImage.setImage(ImageManager.getImage("directory_48"));
		data = new GridData();
		data.verticalSpan = 2;
		directoryImage.setLayoutData(data);

		Label instructions = new Label(dialog, SWT.NONE);
		instructions.setText(Messages.getString("new_directory_instructions_", Settings.getLocale()));
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.minimumWidth = 300;
		data.grabExcessHorizontalSpace = true;
		instructions.setLayoutData(data);

		input = new Text(dialog, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.minimumWidth = 300;
		data.grabExcessHorizontalSpace = true;
		input.setLayoutData(data);

		input.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				StringBuilder sb = new StringBuilder(e.text);
				int index = 0;
				int lastIndex = 0;
				while (true) {
					index = sb.indexOf(String.valueOf(File.separatorChar), lastIndex);
					if (index < 0) {
						break;
					} else {
						sb.deleteCharAt(index);
					}
				}
				e.text = sb.toString();
			}
		});

		createButtons();
	}

	public String open() {
		Display display = Display.getDefault();
		dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setText(Messages.getString("new_directory", Settings.getLocale()));

		createContents();

		dialog.pack();
		dialog.open();
		while (dialog.isDisposed() == false) {
			if (display.readAndDispatch() == false) {
				display.sleep();
			}
		}
		dialog.dispose();

		return result;
	}

}
