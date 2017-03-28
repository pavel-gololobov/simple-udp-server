package com.teleofis.udp;

import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
	
	private static Logger LOG = LoggerFactory.getLogger(Application.class);
	
	private static UdpController udpServer;

	public static void main(String[] args) throws SocketException {
		
		int firstArg = 0;
		if (args.length > 0) {
		    try {
		        firstArg = Integer.parseInt(args[0]);
		    } catch (NumberFormatException e) {
		        LOG.error("Port " + args[0] + " must be an integer.");
		        System.exit(1);
		    }
		}
		
		udpServer = new UdpController();
		
		udpServer.startServer(firstArg);
		
		shutdownHookRegister();
		
		LOG.info("Application running");
	}
	
	private static void shutdownHookRegister() {
		final Thread mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				LOG.info("Shutdown application");
				udpServer.stopServer();
				try {
					mainThread.join();
				} catch (InterruptedException e) {
				}
				LOG.info("Bye!");
			}
		});
	}

}
