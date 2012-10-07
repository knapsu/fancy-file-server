package eu.knapsu.vfs;

import java.util.Vector;

public class VirtualFile implements FileObject {

	private String name;
	private Vector<FileObject> children;
	private FileObject parent;
	private long modificationTime;


	public VirtualFile(String name) {
		this.name = name;
		children = new Vector<FileObject>();
		modificationTime = System.currentTimeMillis();
	}

	public boolean addChild(FileObject child) {
		int index = 0;

		while (index < children.size()) {
			int compare = children.get(index).getName().compareToIgnoreCase(child.getName());
			if (compare > 0) {
				break;
			} else if (compare == 0) {
				compare = children.get(index).getName().compareTo(child.getName());
				if (compare > 0) {
					break;
				} else if (compare == 0) {
					System.err.println("File " + child.getName() + " already exists.");
					return false;
				}
			}
			index++;
		}

		children.add(index, child);
		child.setParent(this);

		modificationTime = System.currentTimeMillis();

		//System.err.println("child " + child.getName() + " added to " + getName());
		return true;
	}

	@Override
	public void setParent(FileObject parent) {
		this.parent = parent;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean exists() {
		return true;
	}

	public FileObject getChild(String childName) {
		for (FileObject i : children) {
			if (i.getName().equals(childName)) {
				//System.err.println("child " + childName + " found in " + getName());
				return i;
			}
		}

		//System.err.println("child " + childName + " not found in " + getName());
		return null;
	}

	@Override
	public FileObject getParent() {
		return parent;
	}

	public Vector<FileObject> getChildren() {
		return children;
	}

	@Override
	public long getLastModificationTime() {
		return modificationTime;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public long getSize() {
		return 0L;
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	public boolean removeChild(FileObject child) {
		children.remove((FileObject)child);
		modificationTime = System.currentTimeMillis();

		//System.err.println("child " + child.getName() + " removed from " + getName());
		return true;
	}

}
