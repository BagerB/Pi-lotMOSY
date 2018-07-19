package dev.pi_lot.frisch.pi_loth264;

public class UdpPacket {
    byte[] data;
    int length;


    public UdpPacket(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }
}
