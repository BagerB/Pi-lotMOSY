package dev.pi_lot.frisch.pi_loth264;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class NalParser extends Thread {
    LinkedBlockingQueue<UdpPacket> udpPacket;
    LinkedBlockingQueue<NalUnit> nalUnit;
    UdpPacket udpPacketBuf;
    int bufferSize;
    int index;
    int belt_index;
    int nalState;
    byte[] belt;
    boolean debug = false;


    public NalParser(LinkedBlockingQueue<UdpPacket> udpPacket, LinkedBlockingQueue<NalUnit> nalUnit) {
        this.udpPacket = udpPacket;
        this.nalUnit = nalUnit;
        bufferSize = 16*1024;
        belt_index = 0;
        nalState = 0;
        belt = new byte[bufferSize];
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                udpPacketBuf = udpPacket.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            for (index = 0; index < udpPacketBuf.length; index++){
                belt[belt_index++] = udpPacketBuf.data[index];

                if(belt_index == bufferSize - 1){
                    Log.d("NAL", "NAL overflow");
                    belt_index = 0;
                }

                switch (nalState){
                    case 0:
                        if(udpPacketBuf.data[index] == 0){
                            nalState++;
                        }
                        break;
                    case 1:
                        if (udpPacketBuf.data[index] == 0){
                            nalState++;
                        } else {
                            nalState = 0;
                        }
                        break;
                    case 2:
                        if (udpPacketBuf.data[index] == 0){
                            nalState++;
                        } else {
                            nalState = 0;
                        }
                        break;
                    case  3:
                        if (udpPacketBuf.data[index] == 1){

                            belt[0] = 0;
                            belt[1] = 0;
                            belt[2] = 0;
                            belt[3] = 1;
                            NalUnit nalUnitUnit = new NalUnit(belt, belt_index - 4);
                            try {
                                nalUnit.put(nalUnitUnit);
                                if (debug) Log.d("NAL Unit", String.valueOf(belt_index));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            belt_index = 4;
                            nalState = 0;
                        } else {
                            nalState = 0;
                        }
                        break;

                        default:
                            break;
                }
            }


        }
    }
}