package dev.pi_lot.frisch.pi_lotmjpeg;

//Importieren von Java-Pakete
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

//MjpegInputStream erweitert DataInputStream
public class MjpegInputStream extends DataInputStream {
	//Variablen definieren
    private final byte[] SOI_MARKER = {(byte) 0xFF, (byte) 0xD8};				//Markerposition für den Start der Aufzeichnung des Bildes
    private final byte[] EOF_MARKER = {(byte) 0xFF, (byte) 0xD9};				//Markerposition für das Ende der Aufzeichnung des Bildes
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;
    private static URL url;
    private static URLConnection c;
    private static InputStream in = null;
    private ByteArrayInputStream headerIn;
    private Properties props;
    private byte[] header;
    private byte[] frameData;

    public static MjpegInputStream read(String urlString) {
		//Try-Catch Methode: Liest URL und prüft Verbindung. Rückgabe MjpegInputStream(in)
        try {
            url = new URL(urlString);
            c = url.openConnection();
            c.setConnectTimeout(80);
            c.setReadTimeout(40);
            in = new BufferedInputStream(url.openStream());
            return new MjpegInputStream(in);
        } catch (MalformedURLException e) {
            Log.e("Error", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("Error", e.getMessage(), e);
        }
        return null;
    }
	
	
    public MjpegInputStream(InputStream in) {
        super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
    }

	//Ende der Bildaufzeichnung
    private int getEndOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        byte c;
        for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if (c == sequence[seqIndex]) {
                seqIndex++;
                if (seqIndex == sequence.length) return i + 1;
            } else seqIndex = 0;
        }
        return -1;
    }

	//Anfang der Bildaufzeichnung
    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSequence(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

	//Analysiert die Länge des Inhalts
    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        headerIn = new ByteArrayInputStream(headerBytes);
        props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }

	//Liest den header und die frameData aus. Rückgabe ist ein Bild vom Stream
    public Bitmap readMjpegFrame() throws IOException {
        mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        reset();
        header = new byte[headerLen];
        readFully(header);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            mContentLength = getEndOfSequence(this, EOF_MARKER);
        }
        reset();
        frameData = new byte[mContentLength];
        skipBytes(headerLen);
        readFully(frameData);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }
}