package eu.knapsu;

import java.io.*;
import java.util.*;

public class Settings {

	private static Properties properties;
	private static File settingsDirectory;

	private static Locale locale = Locale.getDefault();
	private static String stylesheet = "";

	static {
		properties = new Properties();

		if (isWindows()) {
			settingsDirectory = new File(System.getenv("APPDATA") + "\\Fancy File Server");
		} else if (isOSX()) {
			settingsDirectory = new File(System.getProperty("user.home")+ "/Library/Application Support/Fancy File Server");
		} else if (isLinux()) {
			settingsDirectory = new File(System.getProperty("user.home") + "/.config/fancy-file-server");
		} else {
			settingsDirectory = new File(System.getProperty("user.home") + File.separator + ".fancy-file-server");
		}

		loadStyleSheetResource("default.css");
	}

	public static Locale getLocale() {
		return locale;
	}

	public static boolean getPropertyBoolean(String propertyName) {
		boolean returnDefault = false;

		String propertyString = properties.getProperty(propertyName);
		boolean propertyValue = false;

		if (propertyString == null) {
			returnDefault = true;
		} else {
			if (propertyString.equals("true")) {
				propertyValue = true;
			} else if (propertyString.equals("false")) {
				propertyValue = false;
			} else {
				returnDefault = true;
			}
		}

		if (returnDefault) {
			if (propertyName.equals("show_hidden_files")) {
				propertyValue = false;
			} else if (propertyName.equals("use_system_tray")) {
				propertyValue = true;
			}
		}

		return propertyValue;
	}

	public static int getPropertyInteger(String propertyName) {
		boolean returnDefault = false;

		String propertyString = properties.getProperty(propertyName);
		int propertyValue = 0;

		if (propertyString == null) {
			returnDefault = true;
		} else {
			try {
				propertyValue = Integer.parseInt(propertyString);
			} catch (NumberFormatException e) {
				returnDefault = true;
			}
		}

		if (returnDefault) {
			if (propertyName.equals("server_port")) {
				if (isWindows()) {
					propertyValue = 80;
				} else {
					propertyValue = 8080;
				}
			}
		}

		return propertyValue;
	}

	public static String getStyleSheet() {
		return stylesheet;
	}

	public static void loadStyleSheet(File file) {
		try {
			FileReader fr = new FileReader(file);
			BufferedReader in = new BufferedReader(fr);

			String line;
			StringBuffer text = new StringBuffer((int) file.length());

			while ((line = in.readLine()) != null) {
				text.append(line);
			}

			stylesheet = text.toString();

			in.close();
			fr.close();
		} catch (Exception ignore) {
		}
	}

	public static void loadStyleSheetResource(String fileName) {
		try {
			InputStream is = Settings.class.getResourceAsStream("/eu/knapsu/css/" + fileName);
			if (is != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				StringBuffer text = new StringBuffer();

				String line;
				while ((line = in.readLine()) != null) {
					text.append(line);
				}

				stylesheet = text.toString();
				in.close();
			}
			is.close();
		} catch (Exception ignore) {
		}
	}

	public static void loadSettings() {
		try {
			properties.load(new FileInputStream(settingsDirectory.getAbsolutePath() + File.separator + "settings"));
			System.out.println(Messages.getString("settings_loaded"));
		} catch (FileNotFoundException e) {
			System.out.println(Messages.getString("settings_not_loaded"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveSettings() {
		if (properties.size() == 0) {
			return;
		}
		try {
			if (settingsDirectory.exists() || settingsDirectory.mkdirs()) {
				properties.store(new FileOutputStream(settingsDirectory.getAbsolutePath() + File.separator + "settings"), null);
				System.out.println(Messages.getString("settings_saved"));
			} else {
				System.out.println(Messages.getString("settings_not_saved"));
			}
		} catch (FileNotFoundException e) {
			System.out.println(Messages.getString("settings_not_saved"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setProperty(String properyName, Object propertyValue) {
		properties.setProperty(properyName, String.valueOf(propertyValue));
	}

	private static boolean isLinux() {
		return System.getProperty("os.name").contains("Linux");
	}

	private static boolean isOSX() {
		return System.getProperty("os.name").contains("OS X");
	}

	private static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

}
