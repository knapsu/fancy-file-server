package eu.knapsu.http;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.http.impl.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;

import eu.knapsu.*;
import eu.knapsu.logging.*;

public class HTTPServer extends Thread {

	private ServerSocket serverSocket;
	private HttpParams parameters;

	private Vector<WorkerThread> threads = new Vector<WorkerThread>();

	private boolean running;


	public void addThread(WorkerThread thread) {
		threads.add(thread);
		Logger.log(Level.DEBUG, Messages.getString("connection_opened", Settings.getLocale()));
		if (FancyFileServer.getGUI() != null) {
			FancyFileServer.getGUI().updateConnectionsCounter();
		}
	}

	public int getConnectionsCount() {
		return threads.size();
	}

	public boolean isRunning() {
		return running;
	}

	public void removeThread(WorkerThread thread) {
		threads.remove(thread);
		Logger.log(Level.DEBUG, Messages.getString("connection_closed", Settings.getLocale()));
		if (FancyFileServer.getGUI() != null) {
			FancyFileServer.getGUI().updateConnectionsCounter();
		}
	}

	public void run() {
		try {
			this.serverSocket = new ServerSocket(Settings.getPropertyInteger("server_port"));
		} catch (Exception e) {
			Logger.log(Level.WARNING, 
					Messages.getString("can_not_open_port_", Settings.getLocale()) + Settings.getPropertyInteger("server_port"));
			if (FancyFileServer.getGUI() != null) {
				FancyFileServer.getGUI().updateServerStatus();
			}
			return;
		}

		running = true;

		this.parameters = new BasicHttpParams();
		this.parameters.setIntParameter(HttpConnectionParams.SO_TIMEOUT, 5000);
		this.parameters.setIntParameter(HttpConnectionParams.SOCKET_BUFFER_SIZE, 8 * 1024);
		this.parameters.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, false);
		this.parameters.setBooleanParameter(HttpConnectionParams.TCP_NODELAY, true);
		this.parameters.setParameter(HttpProtocolParams.ORIGIN_SERVER,
				FancyFileServer.NAME.replaceAll("\\s", "-") + "/" + FancyFileServer.VERSION);

		Logger.log(Level.INFO, Messages.getString("server_started", Settings.getLocale()));

		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface i = (NetworkInterface) interfaces.nextElement();
				if (i.isLoopback() || i.isVirtual()) {
					continue;
				}
				Enumeration<InetAddress> addresses = i.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress a = (InetAddress) addresses.nextElement();
					if (a instanceof Inet4Address) {
						Logger.log(Level.INFO, Messages.getString("server_address_", Settings.getLocale()) +
								"http://" + a.getHostAddress() + ":" + serverSocket.getLocalPort() + "/");
					}
				}
			}
		} catch (Exception e) {
			Logger.log(Level.WARNING, Messages.getString("can_not_get_server_address_", Settings.getLocale()) + e.getMessage());
		}

		if (FancyFileServer.getGUI() != null) {
			FancyFileServer.getGUI().updateServerStatus();
		}

		while (running) {
			try {
				/** Set up HTTP connection */
				Socket socket = this.serverSocket.accept();
				DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
				Logger.log(Level.DEBUG, Messages.getString("incoming_connection_from_", Settings.getLocale()) + socket.getInetAddress().getHostAddress());
				connection.bind(socket, this.parameters);

				/** Set up HTTP protocol processor */
				BasicHttpProcessor processor = new BasicHttpProcessor();
				processor.addInterceptor(new ResponseDate());
				processor.addInterceptor(new ResponseServer());
				processor.addInterceptor(new ResponseContent());
				processor.addInterceptor(new ResponseConnControl());

				/** Set up HTTP request handlers */
				HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
				registry.register("*", new FileHandler());

				/** Set up HTTP service */
				HttpService service = new HttpService(
						processor, 
						new DefaultConnectionReuseStrategy(), 
						new DefaultHttpResponseFactory(),
						registry,
						this.parameters);

				/** Start worker thread */
				WorkerThread t = new WorkerThread(this, service, connection);
				t.setDaemon(true);
				t.start();
			} catch (IOException e) {
				if (running) {
					running = false;
					Logger.log(Level.SEVERE, Messages.getString("i_o_error_", Settings.getLocale()) + e.getMessage());
					break;
				}
			}
		}

		if (FancyFileServer.getGUI() != null) {
			FancyFileServer.getGUI().updateServerStatus();
		}
		Logger.log(Level.INFO, Messages.getString("server_stopped", Settings.getLocale()));
	}

	public void shutdown() {
		try {
			running = false;
			for (WorkerThread t : threads) {
				t.stopThread();
			}
			serverSocket.close();
		} catch (Exception ignore) { }
	}

}
