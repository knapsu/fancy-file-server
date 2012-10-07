package eu.knapsu.vfs;

import java.io.File;
import java.util.StringTokenizer;

public class VirtualFileSystem {

	private FileObject root;


	public VirtualFileSystem() {
		root = new VirtualFile("/");

		loadSpecialFiles();
	}

	public FileObject getRoot() {
		return root;
	}

	private void loadSpecialFiles() {
		try {
			//root.addChild(new LocalFile(this.getClass().getResource("/eu/knapsu/images/~directory.png").toURI()));
			//root.addChild(new LocalFile(this.getClass().getResource("/eu/knapsu/images/~file.png").toURI()));
			//root.addChild(new LocalFile(this.getClass().getResource("/eu/knapsu/images/~favicon.ico").toURI()));
		} catch (Exception ignore) { }
	}

	public FileObject resolveFile(String path) {
		/** check if path is absolute */
		if (path.startsWith("/") == false) {
			return null;
		}

		StringTokenizer tokenizer = new StringTokenizer(path, "/");
		FileObject file = root;

		while (tokenizer.hasMoreTokens() == true) {
			if (file instanceof LocalFile) {
				file = new LocalFile(new File(file.getPath(), tokenizer.nextToken("")));
				break;
			} else {
				file = file.getChild(tokenizer.nextToken());

				if (file == null) {
					break;
				}
			}
		}

		return file;
	}

}
