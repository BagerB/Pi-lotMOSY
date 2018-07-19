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

	//Initialisierung der Activity mit der UI (mjpegView) 
	//Setzt die Quelle für mjpegView
	//Setzt das Ziel für den joystickUdpSocket
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSystemUI();
        this.setContentView(R.layout.activity_main);
        mjpegView = (MjpegView) findViewById(R.id.mjpegView);
        mjpegView.setSource("http://192.168.1.1:8080/?action=stream");
        joystickUdpSocket = new JoystickUdpSocket("192.168.1.1", 8888);
        joystickUdpSocket.start();
    }

	//Beim Start der App, wird der JoystickUdpSocket gestartet
    @Override
    protected void onStart() {
        super.onStart();
        joystickUdpSocket.startRunning();
    }

	//Bei Unterbrechung der App wird der JoystickUdpSocket gestoppt und die App beendet
    @Override
    protected void onStop() {
        super.onStop();
        joystickUdpSocket.stopRunning();
        System.exit(0);
    }

	//Vergabe der Tasten für die Veränderung des mjpegView auf der App
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        action = event.getAction();
        keyCode = event.getKeyCode();
		
        switch (keyCode) {
			//Am Mobiltelefon: Die Lautstärkeoben-Taste vergrößert den Abstand der Bilder
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.addWidthOffset(5);
                }
                return true;
				
			//Am Mobiltelefon: Die Lautstärkeunten-Taste verkleinert den Abstand der Bilder
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.subWidthOffset(5);
                }
                return true;
				
			//Am Joystick: Die R1-Taste vergrößert den Abstand der Bilder
            case KeyEvent.KEYCODE_BUTTON_R1:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.addWidthOffset(5);
                }
                return true;
				
			//Am Joystick: Die L1-Taste verkleinert den Abstand der Bilder
            case KeyEvent.KEYCODE_BUTTON_L1:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.subWidthOffset(5);
                }
                return true;
				
			//Am Joystick: Der Select-Knopf wechselt zwischen dem Vollbild-Modus und dem Doppelbild-Modus
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                if (action == KeyEvent.ACTION_DOWN) {
                    mjpegView.toggleDoubleImageMode();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

	//Nimmt das event des Joysticks entgegen
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {
            processJoystickInput(event, -1);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

	//Übergibt die ausgelesenen Werte des Joysticks an den JoystickUdpSocket
    private void processJoystickInput(MotionEvent event, int historyPos) {
        mInputDevice = event.getDevice();

        joySteer = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos);
        joyLook = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
        joyGas = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RTRIGGER, historyPos);
        joyBrake = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_LTRIGGER, historyPos);

        joystickUdpSocket.setControls(joySteer, joyLook, joyGas, joyBrake);
    }

	//Methode um zentrierte Achsen zu bekommen
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

	//Versteckt das Android-UI 
    private void setSystemUI() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
