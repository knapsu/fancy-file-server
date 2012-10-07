package eu.knapsu.http;

import java.io.*;

import org.apache.http.*;
import org.apache.http.protocol.*;

import eu.knapsu.*;
import eu.knapsu.logging.*;

public class WorkerThread extends Thread {

	private final HTTPServer server;
	private final HttpService service;
	private final HttpServerConnection connection;

	private boolean forcedClose = false;


	public WorkerThread(HTTPServer server, HttpService service, HttpServerConnection connection) {
		super();
		this.server = server;
		this.service = service;
		this.connection = connection;
	}

	public void run() {
		server.addThread(this);

		HttpContext context = new BasicHttpContext(null);
		try {
			while (!Thread.interrupted() && this.connection.isOpen()) {
				this.service.handleRequest(this.connection, context);
			}
		} catch (ConnectionClosedException e) {
			Logger.log(Level.DEBUG, Messages.getString("client_closed_connection", Settings.getLocale()));
		} catch (IOException e) {
			if (!forcedClose) {
				/* Workaround for breaking the connection after sending the content. */
				//Logger.log(Level.WARNING, Messages.getString("io_error_", Settings.getLocale()) + e);
			}
		} catch (HttpException e) {
			Logger.log(Level.WARNING, Messages.getString("http_error_", Settings.getLocale()) + e);
		} catch(Exception e) {
			Logger.log(Level.SEVERE, Messages.getString("critical_error_", Settings.getLocale()) + e);
		} finally {
			try {
				this.connection.shutdown();
			} catch (IOException ignore) { }
		}

		server.removeThread(this);
	}

	public void stopThread() {
		try {
			forcedClose = true;
			connection.close();
		} catch (IOException ignore) { }
	}

}
