package visualmimo.com.visualmimo.persistence;

/**
 * A frame of video, storing the raw bytes as well as Vuforia data.
 * TODO: add Vuforia data, static methods.
 * @author revan
 */
public class Frame {

    private byte[] raw;

    public Frame(byte[] raw) {
        this.raw = raw;
    }

    public byte[] getRaw() {
        return raw;
    }
}
