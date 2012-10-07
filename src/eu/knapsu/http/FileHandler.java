package eu.knapsu.http;

import java.io.*;
import java.net.*;

import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;

import eu.knapsu.*;
import eu.knapsu.logging.*;
import eu.knapsu.vfs.*;
import eu.knapsu.xhtml.*;

public class FileHandler implements HttpRequestHandler  {

	public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException, IOException {

		final String method = request.getRequestLine().getMethod().toUpperCase();
		if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
			throw new MethodNotSupportedException(method + " method not supported"); 
		}

//		final String targetOriginal = URLDecoder.decode(request.getRequestLine().getUri(), "UTF-8");
//		final String target = convertTarget(targetOriginal);
		final String target = URLDecoder.decode(request.getRequestLine().getUri(), "UTF-8");

		if (target.startsWith("/~")) {
			/** icon request */
//			Logger.log(Level.DEBUG, method + " " + target);
		} else {
			Logger.log(Level.INFO, method + " " + target);
		}

		if (request instanceof HttpEntityEnclosingRequest) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
			byte[] entityContent = EntityUtils.toByteArray(entity);
			Logger.log(Level.INFO, Messages.getString("request_contains_entity_bytes_", Settings.getLocale()) + entityContent.length);
		}

		final FileObject file = FancyFileServer.getVFS().resolveFile(target);

		if (file == null || file.exists() == false) {

			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			EntityTemplate body = new EntityTemplate(new ContentProducer() {

				public void writeTo(final OutputStream outstream) throws IOException {
					OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8"); 
					
					XHTMLGenerator g = new XHTMLGenerator();
					g.generateError(writer, target, HttpStatus.SC_NOT_FOUND);

					writer.flush();
				}

			});
			body.setContentType("text/html; charset=UTF-8");
			response.setEntity(body);

			Logger.log(Level.DEBUG, Messages.getString("not_found_", Settings.getLocale()) + target);
		} else if (file.canRead() == false) {

			response.setStatusCode(HttpStatus.SC_FORBIDDEN);
			EntityTemplate body = new EntityTemplate(new ContentProducer() {

				public void writeTo(final OutputStream outstream) throws IOException {
					OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8"); 

					XHTMLGenerator g = new XHTMLGenerator();
					g.generateError(writer, target, HttpStatus.SC_FORBIDDEN);

					writer.flush();
				}

			});
			body.setContentType("text/html; charset=UTF-8");
			response.setEntity(body);

			Logger.log(Level.DEBUG, Messages.getString("forbidden_", Settings.getLocale()) + target);
		} else {
			response.setStatusCode(HttpStatus.SC_OK);

			if (file.isDirectory()) {
				EntityTemplate body = new EntityTemplate(new ContentProducer() {

					public void writeTo(final OutputStream outstream) throws IOException {
						OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");

						XHTMLGenerator g = new XHTMLGenerator();
						for (HeaderElement i : request.getFirstHeader("accept-language").getElements()) {
							if (g.setLocale(i.getName()) == true) {
								break;
							}
						}
						g.generateDirectoryListing(writer, target, file);

						writer.flush();
					}
				});
				body.setContentType("text/html; charset=UTF-8");
				response.setEntity(body);
			} else {
				FileEntity body = new FileEntity(((LocalFile) file).getFile(), "application/octet-stream");
				response.setEntity(body);
			}

			if (target.startsWith("/~")) {
				/** icon request */
//				Logger.log(Level.DEBUG, "Sending file " + target);
			} else {
				Logger.log(Level.DEBUG, "Sending file " + target);
			}
		}
	}

//	private static String convertTarget(String target) {
//		if ("/favicon.ico".equals(target)) {
//			return "/~favicon.ico";
//		}
//		return target;
//	}

}
