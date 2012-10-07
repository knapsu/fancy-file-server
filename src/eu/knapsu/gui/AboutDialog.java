package eu.knapsu.gui;

import eu.knapsu.*;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AboutDialog extends Dialog {

	private Shell parent;
	private Shell dialog;

	public AboutDialog(Shell parent) {
		super(parent, SWT.NONE);
		this.parent = parent;
	}

	private void createContents() {
		GridLayout dialogLayout = new GridLayout();
		dialogLayout.numColumns = 2;
		dialogLayout.marginHeight = 8;
		dialogLayout.marginLeft = 3;
		dialogLayout.marginRight = 8;
		dialogLayout.horizontalSpacing = 10;
		dialog.setLayout(dialogLayout);

		GridData data;

		Label logo = new Label(dialog, SWT.NONE);
		logo.setImage(ImageManager.getImage("logo"));
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		data.verticalSpan = 5;
		logo.setLayoutData(data);

		Label productFullName = new Label(dialog, SWT.NONE);
		productFullName.setText(FancyFileServer.NAME + " " + FancyFileServer.VERSION);
		FontData[] fontData = productFullName.getFont().getFontData();
		for (FontData fd : fontData) {
			fd.setHeight(fd.getHeight() + 1);
			fd.setStyle(SWT.BOLD);
		}
		Font font = new Font(dialog.getDisplay(), fontData);
		productFullName.setFont(font);

		Label homepageLabel = new Label(dialog, SWT.NONE);
		homepageLabel.setText(Messages.getString("homepage_", Settings.getLocale()));
		Link homepage = new Link(dialog, SWT.NONE);
		homepage.setText("<a>" + FancyFileServer.HOMEPAGE + "</a>");

		Label author = new Label(dialog, SWT.NONE);
		author.setText(FancyFileServer.AUTHOR);
		Link email = new Link(dialog, SWT.NONE);
		email.setText("<a>" + FancyFileServer.EMAIL + "</a>");
	}

	public void open() {
		Display display = Display.getDefault();
		dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setImage(ImageManager.getImage("toolbar_about"));
		dialog.setText(Messages.getString("about", Settings.getLocale()));

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
