package eu.knapsu.logging;

import eu.knapsu.*;

public class Logger {

	private static java.util.logging.Logger logger;

	static {
		logger = java.util.logging.Logger.getLogger(FancyFileServer.class.getName());
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.INFO);
		GUIHandler handler = new GUIHandler();
		logger.addHandler(handler);
	}


	public static void log(java.util.logging.Level level, String message) {
		logger.log(level, message);
	}

}
