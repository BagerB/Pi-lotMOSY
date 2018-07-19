package dev.pi_lot.frisch.pi_loth264;

import android.media.MediaCodec;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class NalFeeder extends Thread {
    MediaCodec mediaCodec;
    LinkedBlockingQueue<NalUnit> nalUnit;
    NalUnit nalUnitBuf;
    MediaCodec.BufferInfo bufferInfo;
    int outputBufferIndex;
    int inputBufferIndex;
    ByteBuffer inputByteBuffer;
    ByteBuffer outputByteBuffer;
    private long lastFrameTimeStamp;
    byte[] dummyFrame;
    NalUnit dummyVideo;

    public NalFeeder(MediaCodec mediaCodec, LinkedBlockingQueue<NalUnit> nalUnit) {
        this.mediaCodec = mediaCodec;
        this.nalUnit = nalUnit;
    }

    @Override
    public void run() {
        bufferInfo = new MediaCodec.BufferInfo();
        byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
        nalUnitBuf = null;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                nalUnitBuf = nalUnit.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (nalUnitBuf == null) {
                if (dummyVideo == null) {
                    nalUnitBuf = dummyVideo;
                }
            }

            if (dummyVideo == null) {
                dummyVideo = new NalUnit(dummyFrame, dummyFrame.length);
            }

            inputBufferIndex = mediaCodec.dequeueInputBuffer(200);
            if (inputBufferIndex >= 0) {
                inputByteBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                inputByteBuffer.clear();
                inputByteBuffer.put(nalUnitBuf.data, 0, nalUnitBuf.length);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, nalUnitBuf.length, 0, 0);
                nalUnitBuf.data = null;
                nalUnitBuf.length = 0;
                nalUnitBuf = null;
                Log.d("Feeder", "In Buffer");
            }

            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (outputBufferIndex >= 0) {
                boolean doRender = (bufferInfo.size != 0);
                mediaCodec.releaseOutputBuffer(outputBufferIndex, doRender);
                Log.d("Feeder", "Out Buffer");
            }
        }
    }
}
