package visualmimo.com.visualmimo.persistence;

/**
 * A frame of video, storing the raw bytes as well as Vuforia data.
 * TODO: add Vuforia data, static methods.
 * @author revan
 */
public class Frame {

    private byte[] raw;
    private int height;
    private int width;

    public Frame(byte[] raw, int width, int height) {
        this.raw = raw;
        this.height = height;
        this.width = width;
    }

    public byte[] getRaw() {
        return raw;
    }

    public int getHeight() { return height; }

    public int getWidth() { return width; }
}
