package eu.knapsu.logging;

import java.text.*;
import java.util.*;
import java.util.logging.*;

import eu.knapsu.*;

public class GUIHandler extends Handler {

	private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");


	public GUIHandler() {
		super();
	}

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		if (isLoggable(record) == false) {
			return;
		}

		String message = df.format(new Date(record.getMillis())) + " " + record.getMessage();
		if (FancyFileServer.getGUI() != null) {
			FancyFileServer.getGUI().updateLog(message);
		}

		if ((record.getLevel() == java.util.logging.Level.WARNING) || 
				(record.getLevel() == java.util.logging.Level.SEVERE)) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}
	}

}
