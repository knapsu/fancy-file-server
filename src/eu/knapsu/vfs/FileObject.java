package eu.knapsu.vfs;

public interface FileObject {

	public boolean addChild(FileObject child);

	public boolean canRead();

	public boolean exists();

	public FileObject getChild(String childName);

	public long getLastModificationTime();

	public String getName();

	public FileObject getParent();

	public String getPath();

	public long getSize();

	public boolean isDirectory();

	public boolean removeChild(FileObject child);

	public void setParent(FileObject parent);

}
