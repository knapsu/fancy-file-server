package eu.knapsu.gui;

import java.io.*;
import java.util.*;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class ImageManager {

	private static class ImageFilesFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			int dotIndex = name.lastIndexOf(".");
			if (dotIndex > 0) {
				String extension = name.substring(dotIndex + 1);
				return extension.equals("png");
			} else {
				return false;
			}
		}
	}

	private static final ImageFilesFilter filter = new ImageFilesFilter();
	private static TreeMap<String, Image> images = new TreeMap<String, Image>();

	public static void addImage(String name, String path) {
		Image image = new Image(Display.getDefault(), new ImageData(path));
		if (images.containsKey(name)) {
			images.get(name).dispose();
		}
		images.put(name, image);
	}

	public static void addImageResource(String name, String path) {
		InputStream is = ImageManager.class.getResourceAsStream(path);
		if (is != null) {
			Image image = new Image(Display.getDefault(), new ImageData(is));
			if (images.containsKey(name)) {
				images.get(name).dispose();
			}
			images.put(name, image);
		}
	}

	public static boolean exists(String name) {
		return images.containsKey(name);
	}

	public static Image getImage(String name) {
		Image image = images.get(name);
		return image;
	}

	public static void loadImagesFromDirectory(String path) {
		File imagesDirectory = new File(path);
		if (imagesDirectory.exists()) {
			File[] imageFiles = imagesDirectory.listFiles(filter);
			for (File f : imageFiles) {
				Image image = new Image(Display.getDefault(), new ImageData(f.getAbsolutePath()));
				int dotIndex = f.getName().lastIndexOf(".");
				String name = f.getName().substring(0, dotIndex);
				images.put(name, image);
			}
		}
	}

	public static void loadImages() {
		addImageResource("button_cancel", "/eu/knapsu/images/button_cancel.png");
		addImageResource("button_ok", "/eu/knapsu/images/button_ok.png");
		addImageResource("directory_24", "/eu/knapsu/images/directory_24.png");
		addImageResource("directory_48", "/eu/knapsu/images/directory_48.png");
		addImageResource("directory_red_24", "/eu/knapsu/images/directory_red_24.png");
		addImageResource("directory_red_48", "/eu/knapsu/images/directory_red_48.png");
		addImageResource("file_24", "/eu/knapsu/images/file_24.png");
		addImageResource("file_48", "/eu/knapsu/images/file_48.png");
		addImageResource("logo", "/eu/knapsu/images/logo.png");
		addImageResource("toolbar_about", "/eu/knapsu/images/toolbar_about.png");
		addImageResource("toolbar_offline", "/eu/knapsu/images/toolbar_offline.png");
		addImageResource("toolbar_online", "/eu/knapsu/images/toolbar_online.png");
		addImageResource("toolbar_settings", "/eu/knapsu/images/toolbar_settings.png");
		addImageResource("toolbar_statusbar", "/eu/knapsu/images/toolbar_statusbar.png");
		addImageResource("toolbar_tray", "/eu/knapsu/images/toolbar_tray.png");
		addImageResource("tray_offline", "/eu/knapsu/images/tray_offline.png");
		addImageResource("tray_online", "/eu/knapsu/images/tray_online.png");
	}

	public static void removeImage(String name) {
		if (images.containsKey(name)) {
			images.get(name).dispose();
		}
	}

}
