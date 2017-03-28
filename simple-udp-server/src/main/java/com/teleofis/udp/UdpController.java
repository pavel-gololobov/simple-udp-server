package com.teleofis.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpController {

    private static Logger LOG = LoggerFactory.getLogger(UdpController.class);

    private DatagramSocket serverSocket;

    private boolean terminated = false;

    private Thread receiveThread;

    class ServerThread implements Runnable {

        byte[] receiveData = new byte[1024];

        @Override
        public void run() {
            while (!terminated) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    InetAddress address = receivePacket.getAddress();
                    int port = receivePacket.getPort();

                    byte[] subArray = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
                    
                    LOG.info("Data received from: " + address.toString() + ":" + port);
                    LOG.info("RCVED BYTES: " + Utils.bytesToHex(subArray));
                    LOG.info("RCVED STRING: " + new String(subArray));

                    sendData(subArray, address, port);
                } catch (IOException e) {
                    terminated = true;
                }
            }
        }

    }

    public void startServer(int port) throws SocketException {
        serverSocket = new DatagramSocket(port);
        receiveThread = new Thread(new ServerThread());
        receiveThread.setName("Server thread on port " + port);
        receiveThread.start();
    }

    public void stopServer() {
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (receiveThread != null) {
            terminated = true;
            try {
                receiveThread.join();
            } catch (InterruptedException e) {
            }
        }
    }
    
    private void sendData(byte[] out, InetAddress address, int port) {
//        DatagramSocket socket = null;
        try {
//            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(out, out.length, address, port);
            serverSocket.send(packet);
            
            LOG.info("Data sent to: " + address.toString() + ":" + port);
            LOG.info("SENT BYTES: " + Utils.bytesToHex(out));
            LOG.info("SENT STRING: " + new String(out));
        } catch (SocketTimeoutException e) {
            LOG.error("Message Ack timeout");
        } catch (Exception e) {
            LOG.error("Socket error", e);
        } finally {
//            if (socket != null) {
//                socket.close();
//            }
        }
    }

    public byte[] sendCommand(byte[] out, String ipAddress, int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(out, out.length, InetAddress.getByName(ipAddress), port);
            socket.send(packet);
            
            LOG.info("SND BYTES: " + Utils.bytesToHex(out));
            LOG.info("SND STRING: " + new String(out));
            
            socket.setSoTimeout(10000);
            
            packet.setData(new byte[256]);
            socket.receive(packet);

            byte[] subArray = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            LOG.info("Ack received from: " + packet.getAddress().toString() + ":" + packet.getPort());
            LOG.info("RCV BYTES: " + Utils.bytesToHex(subArray));
            LOG.info("RCV STRING: " + new String(subArray));
            return subArray;
        } catch (SocketTimeoutException e) {
            LOG.error("Message Ack timeout");
            return null;
        } catch (Exception e) {
            LOG.error("Socket error", e);
            return null;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

}
