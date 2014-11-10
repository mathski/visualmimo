package com.android.visualmimo.camera;

import android.hardware.Camera;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;

/**
 * Static methods for working with Matrices / Vectors.
 * @author revan
 */
public class MatrixUtils {

	/**
	 * Converts a point from camera space to screen space.
	 * Adapted from https://developer.vuforia.com/forum/android/get-trackable-angle
	 * @param cameraPoint
	 * @param screenWidth
	 * @param screenHeight
	 * @return
	 */
	public static Vec2F cameraPointToScreenPoint(Vec2F cameraPoint, int screenWidth, int screenHeight)
	{
		VideoMode videoMode = CameraDevice.getInstance().getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
		VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();
		int xOffset = (int) (((int) screenWidth - config.getSize().getData()[0]) / 2.0f + config.getPosition().getData()[0]);
		int yOffset = (int) (((int) screenHeight - config.getSize().getData()[1]) / 2.0f - config.getPosition().getData()[1]);
		boolean isActivityInPortraitMode = (screenWidth < screenHeight);
		
		if (isActivityInPortraitMode)
		{
			// camera image is rotated 90 degrees
			int rotatedX = (int) (videoMode.getHeight() - cameraPoint.getData()[1]);
			int rotatedY = (int) cameraPoint.getData()[0];
			return new Vec2F(rotatedX * config.getSize().getData()[0] / (float) videoMode.getHeight() + xOffset,
			                 rotatedY * config.getSize().getData()[1] / (float) videoMode.getWidth() + yOffset);
		}
		else
		{
			return new Vec2F(cameraPoint.getData()[0] * config.getSize().getData()[0] / (float) videoMode.getWidth() + xOffset,
			                 cameraPoint.getData()[1] * config.getSize().getData()[1] / (float) videoMode.getHeight() + yOffset);
		}
	}
	
	public static void printVector(Vec2F v) {
		printFloatArray(v.getData());
	}
	
	public static void printVector(Vec3F v) {
		printFloatArray(v.getData());
	}
	
	public static void printFloatArray(float[] v) {
		for (int i = 0; i < v.length; i++) {
			System.out.print(v[i] + " ");
		}
		System.out.println();
	}
}
