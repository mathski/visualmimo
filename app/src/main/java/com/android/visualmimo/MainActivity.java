/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.android.visualmimo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.visualmimo.persistence.MessageCache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;

/**
 * Activity that handles everything: Vuforia, UI.
 * 
 * @author revan
 *
 */
public class MainActivity extends VuforiaActivity implements Callback{

	/** Set to false to bench under load. */
	private boolean idleBenchMode = false;
	private boolean benchingInProgress = false;
	private boolean recordingMode = false;
	private boolean photoalbumMode = false, whiteboardDemo = false;

	/** Counts how many frames have elapsed since last NDK call. */
	private int frameCounter = 0;

	private GestureDetector mGestureDetector;

	public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

	// Called when the activity first starts or the user navigates back to an activity.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		startLoadingAnimation();
		vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mGestureDetector = new GestureDetector(this, new GestureListener());

		try{
			vuforiaAppSession.startAR();
		}catch(Exception e){e.printStackTrace();}

		loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

		(findViewById(R.id.resume_button)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MessageCache.getInstance().reset();
				((TextView) findViewById(R.id.decoded_data)).setText("");
				findViewById(R.id.resume_button).setVisibility(View.INVISIBLE);
				vuforiaAppSession.mGlView.setVisibility(View.VISIBLE);
				vuforiaAppSession.mGlView.onResume();
			}
		});
	}

	/** GestureListener for tap to focus. */
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		// Used to set autofocus one second after a manual focus is triggered
		private final Handler autofocusHandler = new Handler();

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// Generates a Handler to trigger autofocus after 1 second
			autofocusHandler.postDelayed(new Runnable() {
				public void run() {
					boolean result = CameraDevice.getInstance().setFocusMode(
							CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

					if (!result)
						Log.e("SingleTapUp", "Unable to trigger focus");
				}
			}, 1000L);

			return true;
		}
	}

	/** This helps avoid the app dying when settings change. */
	@Override
	public void onConfigurationChanged(Configuration config) {
		Log.d(LOGTAG, "onConfigurationChanged");
		super.onConfigurationChanged(config);

		vuforiaAppSession.onConfigurationChanged();
	}

	/** Adds ActionBar items. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void startLoadingAnimation() {
		LayoutInflater inflater = LayoutInflater.from(this);
		mUILayout = (RelativeLayout) inflater.inflate(R.layout.activity_main, null, false);

		mUILayout.setVisibility(View.VISIBLE);
		mUILayout.setBackgroundColor(Color.TRANSPARENT);

		loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);
		loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

		addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	/** Create new MIMOFrame, add to FrameCache. */
	public void onQCARUpdate(State state) {
		benchFPS();

		NUM_SAVES = MessageCache.NUM_MESSAGES
				- MessageCache.getInstance().size()
				+ saveCount;

		if (recordingMode)
			frameCounter++;

		boolean shouldTakePicture = ((frameCounter % 20 == 0) && recordingMode)
				|| (benchingInProgress && !idleBenchMode);
//		System.out.println("shouldTakePicture: " + shouldTakePicture);
		onQCARUpdate(state, shouldTakePicture);

		if (MessageCache.getInstance().isReady()) {
			recordingMode = false;
			System.out.println("Stopping recording mode.");
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	private void handleSaveButton() {
		System.out.println("Save button pressed.");
		// enable recording mode after delay
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				recordingMode = true;
				System.out.println("Starting recording mode.");
			}
		}, 1000);
		saveCount = 0;
	}

	private void handleWhiteboardButton(){
		whiteboardDemo = true;
		handleSaveButton();
	}

	private void handleDemoButton() {
		photoalbumMode = true;
		handleSaveButton();
	}


	/** Handles ActionBar presses. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_save:
				handleSaveButton();
				return true;
			case R.id.action_whiteboard:
				handleWhiteboardButton();
				return true;
			case R.id.action_album:
				handleDemoButton();
				return true;

			case R.id.action_idle_fps:
				idleBenchMode = true;
				benchingInProgress = true;
				frameCount = -1;
				return true;

			case R.id.action_load_fps:
				idleBenchMode = false;
				benchingInProgress = true;
				frameCount = -1;
				return true;
			case R.id.action_analytics:
				startActivity(new Intent(getApplicationContext(), AnalyticsLogin.class));
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	/** Handles camera button on Google Glass. */
	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		if (keycode == KeyEvent.KEYCODE_CAMERA) {
			handleSaveButton();
			return true;
		}
		
		return super.onKeyDown(keycode, event);
	}
	
	private void benchFPS() {
		if (benchingInProgress && frameCount++ < 0) {
			final int seconds = 10;
			showToast("Benching " + (idleBenchMode ? "idle" : "load") + " FPS (" + seconds +"seconds)");
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					showToast("FPS: " + frameCount / (double) seconds);
					benchingInProgress = false;
				}
			}, seconds * 1000);
		}
	}
	
	/** Updates message display */
	@Override
	public boolean handleMessage(Message msg) {
		ExtractedMessage extracted = (ExtractedMessage) msg.obj;

		if(whiteboardDemo) {
			queue.add(
					new StringRequest(
							Request.Method.GET,
							"http://192.241.132.79/whiteboard.php?id=" + extracted.binary,
							new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									if (!response.equalsIgnoreCase("null")) {
										try {
											vuforiaAppSession.pauseAR();
										} catch (Exception e) {
											e.printStackTrace();
										}
										showToast("Found ID " + response);
										Intent intent = new Intent(getApplicationContext(), Whiteboard.class);
										intent.putExtra("id", response);
										startActivity(intent);
									} else showToast(response);
								}
							}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							showToast(error.getMessage());
							System.err.println(error.getMessage());
						}
					}));
			return false;
		}

		showToast(extracted.toString());

		if (photoalbumMode) {
			photoalbumMode = false;

			String url = "http://f1436e6.ngrok.com/change/" + extracted.binary;
			queue.add(
					new StringRequest(
							Request.Method.GET,
							url,
							new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									showToast(response);
								}
							}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							showToast(error.getMessage());
							System.err.println(error.getMessage());
						}
					}));
		}
		
		((TextView) findViewById(R.id.message)).setText(extracted.message);
		findViewById(R.id.resume_button).setVisibility(View.VISIBLE);
		vuforiaAppSession.mGlView.setVisibility(View.INVISIBLE);
		vuforiaAppSession.mGlView.onPause();

		return true;
	}
}