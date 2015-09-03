package com.android.visualmimo.persistence;

import com.qualcomm.vuforia.PIXEL_FORMAT;

/**
 * A frame of video, storing the raw bytes as well as Vuforia data.
 * @author revan
 */
public class MIMOFrame {

	/** The image format that Vuforia will use. We can change this.
	 * Keep all references to image formats as MIMOFrame.IMAGE_FORMAT.
	 */
	public static int IMAGE_FORMAT = PIXEL_FORMAT.RGB888;
	
    /** The byte representation of the frame.*/
    private byte[] raw;
    
    private int height;
    private int width;
    //TODO(revan): add stride?
    
    private float[][] corners;

    /**
     * @param raw the RGB888 encoded byte array
     * @param width
     * @param height
     * @param corners a 4 x 2 array of floats representing camera-space corners
     */
    public MIMOFrame(byte[] raw, int width, int height, float[][] corners) {
        this.raw = raw;
        this.height = height;
        this.width = width;
        
        this.corners = corners;
    }

    public byte[] getRaw() {
        return raw;
    }

    public int getHeight() { return height; }

    public int getWidth() { return width; }
    
    public float[][] getCorners() { return corners; }
}

