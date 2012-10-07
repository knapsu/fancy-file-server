package eu.knapsu.vfs;

import java.io.File;
import java.net.URI;

public class LocalFile implements FileObject {

	private File file;
	private FileObject parent;


	public LocalFile(File file) {
		this.file = file;
	}

	public LocalFile(String path) {
		this.file = new File(path);
	}
	
	public LocalFile(URI uri) {
		this.file = new File(uri);
	}

	@Override
	public boolean addChild(FileObject child) {
		return false;
	}
	
	@Override
	public void setParent(FileObject parent) {
		this.parent = parent;
	}

	@Override
	public boolean canRead() {
		return file.canRead();
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public FileObject getChild(String childName) {
		return null;
	}

	@Override
	public FileObject getParent() {
		return parent;
	}

	public File getFile() {
		return file;
	}

	@Override
	public long getLastModificationTime() {
		return file.lastModified();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getPath() {
		return file.getAbsolutePath();
	}

	@Override
	public long getSize() {
		return file.length();
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public boolean removeChild(FileObject child) {
		return false;
	}

}
