package eu.knapsu;

import eu.knapsu.gui.*;
import eu.knapsu.http.*;
import eu.knapsu.vfs.*;

public class FancyFileServer {

	public static final String NAME = "Fancy File Server";
	public static final String VERSION = "1.1.2";
	public static final String AUTHOR = "Krzysztof Knapik";
	public static final String EMAIL = "knapsu@gmail.com";
	public static final String LICENSE = "GNU General Public License";
	public static final String HOMEPAGE = "http://knapsu.eu/fancy_file_server/";

	private static VirtualFileSystem vfs;
	private static HTTPServer httpServer;
	private static GUI gui;

	public static GUI getGUI() {
		return gui;
	}

	public static HTTPServer getHTTPServer() {
		return httpServer;
	}

	public static VirtualFileSystem getVFS() {
		return vfs;
	}

	public static boolean isServerRunning() {
		if ((httpServer == null) || (httpServer.isRunning() == false)) {
			return false;
		} else {
			return true;
		}
	}

	public static void main(String[] args) {
		Settings.loadSettings();
		vfs = new VirtualFileSystem();
		gui = new GUI();

		startServer();
		gui.start();

		stopServer();
		Settings.saveSettings();
	}

	public static void startServer() {
		if (isServerRunning() == false) {
			httpServer = new HTTPServer();
			httpServer.start();
		}
	}

	public static void stopServer() {
		if (httpServer != null) {
			httpServer.shutdown();
			httpServer = null;
		}
	}

	public static void startStopServer() {
		if (FancyFileServer.isServerRunning()) {
			FancyFileServer.stopServer();
		} else {
			FancyFileServer.startServer();
		}
	}

}
