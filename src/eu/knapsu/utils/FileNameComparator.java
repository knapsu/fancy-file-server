package eu.knapsu.utils;

import java.io.File;
import java.util.Comparator;

public class FileNameComparator implements Comparator<File> {
	@Override
	public int compare(File fileA, File fileB) {
		int result = fileA.getName().compareToIgnoreCase(fileB.getName());
		if (result != 0) {
			return result;
		} else {
			return fileA.getName().compareTo(fileB.getName());
		}
	}
}
