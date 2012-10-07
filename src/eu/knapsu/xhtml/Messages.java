package eu.knapsu.xhtml;

import java.util.*;

public class Messages {

	private static HashMap<Locale, ResourceBundle> resources = new HashMap<Locale, ResourceBundle>();


	public static String getString(String key, Locale locale) {
		if (locale == null) {
			locale = Locale.getDefault();
		}

		if (resources.containsKey(locale) == false) {
			loadBundle(locale);
		}

		try {
			return resources.get(locale).getString(key);
		} catch (Exception e) {
			return '?' + key + '?';
		}
	}

	public static boolean isAvailable(Locale locale) {
		if (resources.containsKey(locale) == false) {
			loadBundle(locale);
		}

		if (resources.get(locale) != null) {
			return true;
		} else {
			return false;
		}
	}

	private static void loadBundle(Locale locale) {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(Messages.class.getName(), locale, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
			resources.put(locale, bundle);
		} catch (MissingResourceException e) {
			resources.put(locale, null);
		}
	}

}
