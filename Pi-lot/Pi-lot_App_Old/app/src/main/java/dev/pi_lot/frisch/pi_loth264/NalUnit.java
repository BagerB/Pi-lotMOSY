package dev.pi_lot.frisch.pi_loth264;

public class NalUnit {
    byte[] data;
    int length;


    public NalUnit(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }
}
