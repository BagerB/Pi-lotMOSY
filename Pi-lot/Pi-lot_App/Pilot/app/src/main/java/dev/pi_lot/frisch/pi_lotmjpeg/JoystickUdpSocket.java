package dev.pi_lot.frisch.pi_lotmjpeg;

//Importieren von Java-Pakete
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//Die JoystickUdpSocket erweitert den Thread
public class JoystickUdpSocket extends Thread {
	
	//Variablen definieren
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

	//Konstruktor
    public JoystickUdpSocket(String ipAdresse, int port) {
        this.ipAdresse = ipAdresse;
        this.port = port;
    }
	
	//Try-Catch Methode, um Fehler innerhalb des Codeabschnittes abzufangen
    @Override
    public void run() {
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

		//Definition eines Buffers in Form von einem ByteArray mit 16 Einträgen
        buffer = new byte[16];
		
		//Konvertiert Float-Werte zu ByteArray. Übergibt den Buffer, JoySteer,JoyLook,JoyGas,JoyBrake und einen Offset
        while (true) {
            while (running) {
                convertFloatToByteArray(joySteer, buffer, 0);
                convertFloatToByteArray(joyLook, buffer, 4);
                convertFloatToByteArray(joyGas, buffer, 8);
                convertFloatToByteArray(joyBrake, buffer, 12);
				
				//Defintion des UDP-Pakets mit dem Inhalt: Buffer, Buffer.length, address, port 
                packet = new DatagramPacket(buffer, buffer.length, address, port);
				
				//Try-Catch Methode, um fehlerhafte UDP-Pakete innerhalb des Codeabschnittes abzufangen
                try {
                    datagramSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	//Einstellen der Steuerung mit Korrigierungswerten (JoySteer, JoyLook, JoyGas, JoyBrake)
    public void setControls(float joySteer, float joyLook, float joyGas, float joyBrake) {
        this.joySteer = (float) ((joySteer * 0.3)-0.03);
        this.joyLook = (float) -(joyLook * 0.5);
        this.joyGas = (float) (joyGas * 0.8);
        this.joyBrake = joyBrake;
    }

	//Methode um einen Float-Wert in einen ByteArray zu konvertieren
    private void convertFloatToByteArray(float f, byte[] b, int offset) {
        ByteBuffer.wrap(b, offset, 4).order(ByteOrder.LITTLE_ENDIAN).putFloat(f);
    }
	
	//Methode um JoystickUdpSocket zu starten
    public void startRunning(){
        running = true;
    }

	//Methode um JoystickUdpSocket zu stoppen
    public void stopRunning(){
        running = false;
    }
}
