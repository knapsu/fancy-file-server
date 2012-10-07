package eu.knapsu.xhtml;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.http.*;

import eu.knapsu.*;
import eu.knapsu.utils.*;
import eu.knapsu.vfs.*;

public class XHTMLGenerator {

	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private NumberFormat nf;

	private Locale clientLocale;


	public XHTMLGenerator() {
		clientLocale = Locale.getDefault();

		nf = NumberFormat.getNumberInstance(clientLocale);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(1);
	}

	public static String humanReadableDate(long seconds) {
		return df.format(new Date(seconds));
	}

	private String insertChildLinks(FileObject file) {
		StringBuffer listing = new StringBuffer();

		boolean isEmpty = true;

		if (file instanceof VirtualFile) {
			/** listing virtual directory */

			for (FileObject i : ((VirtualFile)file).getChildren()) {
				if (Settings.getPropertyBoolean("show_hidden_files") == false) {
					if (i.getName().startsWith(".") || i.getName().startsWith("~")) {
						continue;
					}
				}

				listing.append("<tr>");
				if (i.isDirectory()) {
					listing.append("<td class=\"name\"><a href=\"" + i.getName() + "/\" class=\"directory\">" + i.getName() + "</a></td>");
					listing.append("<td class=\"center\"><i>-</i></td>");
				} else {
					listing.append("<td class=\"name\"><a href=\"" + i.getName() + "\" class=\"file\">" + i.getName() + "</a></td>");
					listing.append("<td class=\"right\">" + humanReadableSize(i.getSize()) + "</td>");
				}
				listing.append("<td class=\"modified\">" + humanReadableDate(i.getLastModificationTime()) + "</td>");
				listing.append("</tr>\r\n");

				isEmpty = false;
			}
		} else {
			/** listing real directory */;

			File[] fileList = null;

			if (Settings.getPropertyBoolean("show_hidden_files") == false) {
				fileList = ((LocalFile)file).getFile().listFiles(new HiddenFilesFilter());
			} else {
				fileList = ((LocalFile)file).getFile().listFiles();
			}

			java.util.Arrays.sort(fileList, new FileNameComparator());

			for (File i : fileList) {
				listing.append("<tr>");
				if (i.isDirectory()) {
					listing.append("<td class=\"name\"><a href=\"" + i.getName() + "/\" class=\"directory\">" + i.getName() + "</a></td>");
					listing.append("<td class=\"center\"><i>-</i></td>");
				} else {
					listing.append("<td class=\"name\"><a href=\"" + i.getName() + "\" class=\"file\">" + i.getName() + "</a></td>");
					listing.append("<td class=\"right\">" + humanReadableSize(i.length()) + "</td>");
				}
				listing.append("<td class=\"modified\">" + humanReadableDate(i.lastModified()) + "</td>");
				listing.append("</tr>\r\n");

				isEmpty = false;
			}
		}
		
		if (isEmpty) {
			listing.append("<tr>");
			listing.append("<td class=\"name\"><i>" + Messages.getString("empty_directory", clientLocale) + "</i></td>");
			listing.append("<td class=\"center\"></td>");
			listing.append("<td class=\"modified\"></td>");
			listing.append("</tr>\r\n");
		}

		return listing.toString();
	}

	private String insertParentLink(String uri, FileObject file) {
		StringBuffer listing = new StringBuffer();

		int slashIndex = uri.lastIndexOf("/", uri.length() - 2);

		if (slashIndex != -1) {
			String parentUri = uri.substring(0, slashIndex + 1);

			listing.append("<tr>");
			listing.append("<td class=\"name\"><a href=\"" + parentUri + "\" class=\"directory\">..</a></td>");
			listing.append("<td class=\"center\"><i>-</i></td>");
			listing.append("<td class=\"modified\">" + humanReadableDate(file.getLastModificationTime()) + "</td>");
			listing.append("</tr>\r\n");
		}
		return listing.toString();
	}

	public void generateDirectoryListing(OutputStreamWriter writer, String uri, FileObject file) throws IOException {

		writePageBegin(writer, uri);

		//header
		writer.write("<div class=\"header\">\r\n");
		writer.write("<h2>" + uri + "</h2>\r\n");
		writer.write("</div>\r\n");

		//directory listing
		writer.write("<div class=\"listing\">\r\n");
		writer.write("<table>\r\n<thead><tr>\r\n");
		writer.write("<th class=\"name\">" + Messages.getString("name", clientLocale) + "</th>\r\n");
		writer.write("<th class=\"size\">" + Messages.getString("size", clientLocale) + "</th>\r\n");
		writer.write("<th class=\"modified\">" + Messages.getString("modified", clientLocale) + "</th>\r\n");
		writer.write("</tr></thead>\r\n<tbody>\r\n");
		if (uri.length() > 1) {
			// path is not a root directory
			writer.write(insertParentLink(uri, file));
		}
		writer.write(insertChildLinks(file));
		writer.write("</tbody>\r\n");
		writer.write("</table>\r\n");
		writer.write("</div>\r\n");

		//footer
		writer.write("<div class=\"footer\">\r\n");
		writer.write(FancyFileServer.NAME + " " + FancyFileServer.VERSION + "\r\n");
		writer.write("</div>\r\n");

		writePageEnd(writer);

	}

	public void generateError(OutputStreamWriter writer, String uri, int errorCode) throws IOException {
		writePageBegin(writer, Messages.getString("error", clientLocale) + " " + errorCode);
		
		writer.write("<div class=\"header\">\r\n");
		writer.write("<h2>");
		switch (errorCode) {
		case HttpStatus.SC_NOT_FOUND:
			writer.write(Messages.getString("file_not_found", clientLocale) + " " + uri + "</h2>");
			break;
		case HttpStatus.SC_FORBIDDEN:
			writer.write("<h2>" + Messages.getString("forbidden", clientLocale) + " " + uri + "</h2>");
			break;
		}
		writer.write("</h2>\r\n");
		writer.write("</div>\r\n");

		//footer
		writer.write("<div class=\"footer\">\r\n");
		writer.write(FancyFileServer.NAME + " " + FancyFileServer.VERSION + "\r\n");
		writer.write("</div>\r\n");

		writePageEnd(writer);
	}

	public String humanReadableSize(long bytes) {
		double size = bytes;
		int power = 0;

		while (size >= 1024.0d) {
			size /= 1024.0d;
			power++;
		};

		String unit;
		switch (power) {
		case 0:
			unit = " B"; break; //$NON-NLS-1$
		case 1:
			unit = " KiB"; break; //$NON-NLS-1$
		case 2:
			unit = " MiB"; break; //$NON-NLS-1$
		case 3:
			unit = " GiB"; break; //$NON-NLS-1$
		case 4:
			unit = " TiB"; break; //$NON-NLS-1$
		case 5:
			unit = " PiB"; break; //$NON-NLS-1$
		default:
			unit = " ???"; break; //$NON-NLS-1$
		}

		return nf.format(size) + unit;
	}

	private void writePageBegin(OutputStreamWriter writer, String title) throws IOException {
		//doctype
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\r\n");

		//html
		writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"");
		writer.write(clientLocale.toString());
		writer.write("\">\r\n");

		//html head
		writer.write("<head>\r\n");
		writer.write("<title>" + title + "</title>\r\n");
		writer.write("<link rel=\"icon\" type=\"image/vnd.microsoft.icon\" href=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAA1JJREFUOMtVk0toXGUcxc//e93XdDKPTF4T0zSmTVpjo61osaK1NVCxtJBtF11Jl4KuXFlQwa2QhSAIXQREsKJo3Qlmo8amYA0JGjvkNSYzSTMzSWYy9373u5+LFNEDZ3k4HH4cjv+IwCQRAbAWQA+gnucsc8baJAOYPQAhERMAGIDkMHNoC5ACbMSZM1bouvLBKxNXJi5dGveV34G1lR0s/La4/f03U9ONvdn3rcUjAAJATI+7FWAjJQrXj4/dun3h+g26+KrHIpvog3YUpzylSmtgO0sN+mLqvfXl1U/eAMwDAJwDJAGrHSd3uaf33TsnLk/qo+NJ8le5wRfLLb5ZC+XKVov9Ua7hxNP+gRecyZeX6KqJ5j43idnlj7eogaPvfGV6ThfUsEjqYcpZriZoxwSTMIz0u1QPBRZXmyqbC5vVaj4f18r5g3bpawYAvjv6utv97Mmh5zLtj2+eVCLZTUwcEawhaxNa2wlR2thHdb1qZ+Y2AuqMrdczMSl4UGQA4AXDL+m0ABxtH27HqB9IYoJDa2BhNcSTBYXRXpfWGhLZfBpuFpHs7Es7zuBZAQDiSLFfSw0/laJflyM4DkcrPOSTTnGkfY7YEI5kUtAOR5tqCe+UkKknegUAwFFNkc5hc1fYvj2LF0Y97B0cwj076OD+SgjuAKcGHAgpURgeotn5GNv3TEsAgA7Xf6egG5mch0othCc5ujISjiBcPOVDCkK5ZlDLxpCcMFjIiZkHm4hbK4sMAPYrP3/HbNz2XamO9znoyynq72LW64C9s7BnN9raGjK2mPNoIMui2RKEqS/db+78eY8TV8pEjS0i5gydm7xQ9HeaQcZXqbzAeL9HmYwgLRjlOxQ9qrdtwI1tI+B3p27eMLurJQ5rDDEhWn//8iMFxbFnzr823uU1tRGaCn4SJ1aDU2xIx8xlMqmEWfHph2+9XZv/cpq4VBwAYBMQE+7W/N2Zai3MFkfOP9WVy0u4LtfMZf1pj9f2XVpdrlSnP3rz1tLM7c+EVDyJdZv+PSLIIS66rdEpNzf44si5ay8fGzl9zAvSbrVaqZfmf1pYmfv2hyRsLBKX+9boCgBD/3szSHAhc4mJhLVwAAQAJIAIQJMzFoOJlomjOkAJYO0/f0dqUCbkmEIAAAAASUVORK5CYII=\" />");
		writer.write("<meta name=\"robots\" content=\"noindex, nofollow, noarchive\" />");

		//style
		writer.write("<style type=\"text/css\">\r\n");
		writer.write(Settings.getStyleSheet() + "\r\n");
		writer.write("</style>\r\n");
		writer.write("</head>\r\n");

		//html body
		writer.write("<body>\r\n");
	}

	private void writePageEnd(OutputStreamWriter writer) throws IOException {
		//html body
		writer.write("</body>\r\n");

		//html
		writer.write("</html>\r\n");
	}

	public boolean setLocale(String isoCode) {
		Locale locale;

		if (isoCode.length() == 2) {
			locale = new Locale(isoCode);
		} else if (isoCode.length() == 5) {
			locale = new Locale(isoCode.substring(0, 2), isoCode.substring(3));
		} else {
			return false;
		}

		if (Messages.isAvailable(locale) == false) {
			return false;
		}

		clientLocale = locale;

		nf = NumberFormat.getNumberInstance(locale);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(1);

		return true;
	}

}
