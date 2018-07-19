package dev.pi_lot.frisch.pi_loth264;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpSocket extends Thread {
    int port;
    LinkedBlockingQueue<UdpPacket> udpPacket;
    DatagramSocket datagramSocket;
    DatagramPacket datagramPacket;
    byte data;

    public UdpSocket(int port, LinkedBlockingQueue<UdpPacket> udpPacket){
        this.port = port;
        this.udpPacket = udpPacket;
    }

    @Override
    public void run(){
        try {
            datagramSocket = new DatagramSocket( port );
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[1024];

        while (!Thread.currentThread().isInterrupted()){
            datagramPacket = new DatagramPacket( data, data.length );
            try {
                datagramSocket.receive( datagramPacket );
            } catch (IOException e) {
                e.printStackTrace();
            }
            UdpPacket udpData = new UdpPacket(datagramPacket.getData(), datagramPacket.getLength());
            try {
                udpPacket.put(udpData);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}