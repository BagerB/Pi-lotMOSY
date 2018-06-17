package dev.pi_lot.frisch.pi_lotmjpeg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private MjpegView mjpegView;
    private InputDevice mInputDevice;
    private JoystickUdpSocket joystickUdpSocket;
    private int action;
    private int keyCode;
    private float joySteer;
    private float joyLook;
    private float joyGas;
    private float joyBrake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSystemUI();
        this.setContentView(R.layout.activity_main);
        mjpegView = (MjpegView) findViewById(R.id.mjpegView);
        mjpegView.setSource("http://192.168.1.184:8080/?action=stream");
        joystickUdpSocket = new JoystickUdpSocket("192.168.1.184", 8888);
        joystickUdpSocket.start();
    }

    //Starting the JoystickUDPSocket
    @Override
    protected void onStart() {
        super.onStart();
        joystickUdpSocket.startRunning();
    }

    //Stops the JoystickUDPSocket
    @Override
    protected void onStop() {
        super.onStop();
        joystickUdpSocket.stopRunning();
        System.exit(0);
    }

    //Assign PS3-Controller and Smartphone Keys
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        action = event.getAction();
        keyCode = event.getKeyCode();

        switch (keyCode) {
            //KEYCODE_VOLUME_UP (Smartphone) to move the mjpegView to the edge for the eye distance
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.addWidthOffset(5);
                }
                return true;
            //KEYCODE_VOLUME_DOWN (Smartphone) to move the mjpegView inwards for the eye distance
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.subWidthOffset(5);
                }
                return true;
            //KEYCODE_BUTTON_R1 (Controller) to move the mjpegView to the edge for the eye distance
            case KeyEvent.KEYCODE_BUTTON_R1:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.addWidthOffset(5);
                }
                return true;
            //KEYCODE_BUTTON_L1 (Controller) to move the mjpegView inwards for the eye distance
            case KeyEvent.KEYCODE_BUTTON_L1:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.subWidthOffset(5);
                }
                return true;
            //KEY_BUTTON_SELECT_ (Controller) to toggle fullscreen on the Smartphone
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.toggleDoubleImageMode();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
    //Describes joystick movements
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {
            processJoystickInput(event, -1);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    //Setting up PS3-Controller-Buttons
    private void processJoystickInput(MotionEvent event, int historyPos) {
        mInputDevice = event.getDevice();

        joySteer = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos);
        joyLook = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
        joyGas = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RTRIGGER, historyPos);
        joyBrake = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_LTRIGGER, historyPos);

        joystickUdpSocket.setControls(joySteer, joyLook, joyGas, joyBrake);
    }

    //Getting centered axis
    private static float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
        if (range != null) {
            final float flat = range.getFlat();
            final float value = historyPos < 0 ? event.getAxisValue(axis) :
                    event.getHistoricalAxisValue(axis, historyPos);

            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    private void setSystemUI() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
