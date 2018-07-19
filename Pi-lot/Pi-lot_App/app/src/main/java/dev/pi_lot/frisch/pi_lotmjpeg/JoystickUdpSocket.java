package dev.pi_lot.frisch.pi_lotmjpeg;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class JoystickUdpSocket extends Thread {
    private String ipAdresse;
    private int port;
    private DatagramSocket datagramSocket;
    private DatagramPacket packet;
    private InetAddress address;
    private byte[] buffer;
    private float joySteer = 0;
    private float joyLook = 0;
    private float joyGas = 0;
    private float joyBrake = 0;
    private boolean running = false;


    public JoystickUdpSocket(String ipAdresse, int port) {
        this.ipAdresse = ipAdresse;
        this.port = port;
    }

    @Override
    public void run() {
        //Legt datagramSocket an für die UDP-Verbindung
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            address = InetAddress.getByName(ipAdresse);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //ByteBuffer für den UDP-Socket
        buffer = new byte[16];

        while (true) {
            while (running) {
                convertFloatToByteArray(joySteer, buffer, 0);
                convertFloatToByteArray(joyLook, buffer, 4);
                convertFloatToByteArray(joyGas, buffer, 8);
                convertFloatToByteArray(joyBrake, buffer, 12);

                //Erstellt UDP-Paket mit den Daten für die Steuerung des Wagens
                packet = new DatagramPacket(buffer, buffer.length, address, port);

                //UDP-Paket wird an den Pi gesendet
                try {
                    datagramSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Aufgrund der ServoZykluszeit, können neue Befehle erst nach 20ms übernommen werden
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Setzen der Steuerungswerte mit korrigierten Werten
    public void setControls(float joySteer, float joyLook, float joyGas, float joyBrake) {
        this.joySteer = (float) ((joySteer * 0.3) - 0.03);    //30% Ausschlag, Zentrierung leicht nach Links versetzt
        this.joyLook = (float) -(joyLook * 0.5);            //50% Ausschlag
        this.joyGas = (float) (joyGas * 0.5);                //50% Beschleunigung
        this.joyBrake = joyBrake;
    }

    //Methode um einen Float-Wert in einen ByteArray zu konvertieren
    private void convertFloatToByteArray(float f, byte[] b, int offset) {
        ByteBuffer.wrap(b, offset, 4).order(ByteOrder.LITTLE_ENDIAN).putFloat(f);
    }

    //Methode um JoystickUdpSocket zu starten
    public void startRunning() {
        running = true;
    }

    //Methode um JoystickUdpSocket zu stoppen
    public void stopRunning() {
        running = false;
    }
}
