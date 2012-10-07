package eu.knapsu.utils;

import java.io.File;
import java.io.FilenameFilter;

public class HiddenFilesFilter implements FilenameFilter {
	@Override
	public boolean accept(File dir, String name) {
		if (name.startsWith(".") || name.startsWith("~")) {
			return false;
		} else {
			return true;
		}
	}
}
