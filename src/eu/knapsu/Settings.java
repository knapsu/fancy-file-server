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
		String osName = System.getProperty("os.name");

		if (osName.startsWith("Windows")) {
			settingsDirectory = new File(System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH"), "fancy_file_server");
		} else if (osName.startsWith("Mac OS X")) {
			settingsDirectory = new File(System.getenv("HOME"), "fancy_file_server");
		} else if (osName.startsWith("Linux")) {
			settingsDirectory = new File(System.getProperty("user.home"), ".config/fancy-file-server");
		} else {
			settingsDirectory = new File(System.getProperty("user.home"), ".fancy-file-server");
		}

		loadStyleResource("default.css");
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
				propertyValue = 80;
			}
		}

		return propertyValue;
	}

	public static String getStyleSheet() {
		return stylesheet;
	}

	public static void loadStyle(File file) {
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

	public static void loadStyleResource(String fileName) {
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
			System.out.println("Ustawienia wczytane");
		} catch (FileNotFoundException e) {
			System.out.println("Ustawienia nie zostały wczytane");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveSettings() {
		try {
			if (settingsDirectory.exists() || settingsDirectory.mkdirs()) {
				properties.store(new FileOutputStream(settingsDirectory.getAbsolutePath() + File.separator + "settings"), null);
				System.out.println("Ustawienia zapisane");
			} else {
				System.out.println("Ustawienia nie zostały zapisane");
			}
		} catch (FileNotFoundException e) {
			System.out.println("Ustawienia nie zostały zapisane");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setProperty(String properyName, Object propertyValue) {
		properties.setProperty(properyName, String.valueOf(propertyValue));
	}

}
