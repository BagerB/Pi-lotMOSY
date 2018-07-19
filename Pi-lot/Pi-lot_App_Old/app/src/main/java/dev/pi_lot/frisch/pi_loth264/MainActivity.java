package dev.pi_lot.frisch.pi_loth264;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    int Vide_W = 640;
    int Video_H = 480;
    int FrameRate = 30;

    SurfaceView mediaSurfaceView;
    SurfaceHolder mediaSurfaceHolder;
    MediaCodec mediaCodec;
    UdpSocket udpSocket;
    NalParser nalParser;
    NalFeeder nalFeeder;

    LinkedBlockingQueue udpPacketList;
    LinkedBlockingQueue nalUnitList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        setMediaCodec();
    }

    @Override
    protected void onStart() {
        super.onStart();
        udpPacketList = new LinkedBlockingQueue<>();
        nalUnitList = new LinkedBlockingQueue<>();
    }

    private void setMediaCodec() {
        mediaSurfaceHolder = mediaSurfaceView.getHolder();
        mediaSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                try {
                    mediaCodec = MediaCodec.createDecoderByType("video/avc");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", Vide_W, Video_H);
                byte[] header_sps = {0, 0, 0, 1, 39, 100, 0, 40, (byte) 172, 43, 64, 60, 1, 19, (byte) 242, (byte) 192, 60, 72, (byte) 154, (byte) 128};
                byte[] header_pps = {0, 0, 0, 1, 40, (byte) 238, 2, 92, (byte) 176, 0};
                mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
                mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
                //mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
                mediaCodec.configure(mediaformat, mediaSurfaceHolder.getSurface(), null, 0);
                //mediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                startThreads();

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    private void startThreads() {
        mediaCodec.start();
        udpSocket = new UdpSocket(5000, udpPacketList);
        udpSocket.start();
        nalParser = new NalParser(udpPacketList, nalUnitList);
        nalParser.start();
        nalFeeder = new NalFeeder(mediaCodec, nalUnitList);
        nalFeeder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
