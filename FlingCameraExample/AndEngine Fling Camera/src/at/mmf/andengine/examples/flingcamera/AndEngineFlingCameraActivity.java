package at.mmf.andengine.examples.flingcamera;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.view.Display;
import android.view.VelocityTracker;

/**
 * (c) 2011 David Schreiber 
 * 
 * @author David Schreiber
 * @since 10:54:23 - 13.10.2011
 */
public class AndEngineFlingCameraActivity extends BaseGameActivity implements IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener {

	// . . . . . . . . . . . . . . . . . . . . . . .  P R I V A T E  F I E L D S
	
	// Used to calculate velocity while scrolling
	private VelocityTracker mVelocityTracker;

	// Camera object
	private VelocityCamera mCamera;

	// Camera dimensions (based on screen size)
	private int mCameraWidth;
	private int mCameraHeight;

	// Game scene
	private Scene mScene;

	// Used for detecting finger scrolling
	private SurfaceScrollDetector mScrollDetector;

	// Indication whether multitouch is supported or not
	private boolean mMultiTouchSupported;

	// Used for detecting zooming
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;

	// Used to deactivate scrolling while zooming
	private boolean mZooming;


	// Used for smooth scroll start
	private boolean mSmoothStart;


	// . . . . . . . . . . . A N D E N G I N E  L I F E C Y C L E  M E T H O D S

	@Override
	public void onLoadComplete() {

	}

	@Override
	public Engine onLoadEngine() {
		// Fetch the screen size
		Display d = getWindowManager().getDefaultDisplay();

		// Set the camera width and height according to screen size
		this.mCameraWidth = d.getWidth();
		this.mCameraHeight = d.getHeight();

		// Create a new velocity camera object
		this.mCamera = new VelocityCamera(0, 0, mCameraWidth, mCameraHeight, 0.99f);

		// Create engine options
		EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(mCameraWidth, mCameraHeight), this.mCamera);
		Engine engine = new Engine(options);

		// Try to enable multitouch and zooming
		try {
			if(MultiTouch.isSupported(this)) {
				this.mMultiTouchSupported = true;
				engine.setTouchController(new MultiTouchController());
			} else {
				this.mMultiTouchSupported = false;
			}
		} catch(final MultiTouchException e) {
			this.mMultiTouchSupported = false;
		}

		// Create and return engine
		return engine;
	}

	@Override
	public void onLoadResources() {
		// No assets necessary
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		// Create a new scene object
		this.mScene = new Scene();

		// Set grey background
		this.mScene.setBackground(new ColorBackground(0.5f, 0.5f, 0.5f));

		// Register self as touch listener
		this.mScene.setOnSceneTouchListener(this);

		// Register self as scroll listener
		this.mScrollDetector = new SurfaceScrollDetector(1f, this);

		// Create the surface to monitor scrolling
		createSurface();

		// If multitouch is supported, create a new zoom detector
		if(this.mMultiTouchSupported) {
			try {
				this.mPinchZoomDetector = new PinchZoomDetector(this);
			} catch(MultiTouchException e) {
				this.mPinchZoomDetector = null;
			}
		} else {
			this.mPinchZoomDetector = null;
		}

		return this.mScene;
	}

	// . . . . . . . . . . . . . . . . . . . . . .  P R I V A T E  M E T H O D S
	
	private void createSurface() {
		final int from = -10000;
		final int to = 10000;
		final int stepsize = 100;

		for(int i = from; i <= to; i += stepsize) {
			for(int j = from; j <= to; j += stepsize) {
				//this.mScene.attachChild(new Text(i,j, this.mFont, String.format("%d, %d", i, j)));
			}
			this.mScene.attachChild(new Line(from, i, to, i));
			this.mScene.attachChild(new Line(i, from, i, to));
		}
	}


	// . . . . . . . . . . . . . . . . . . . .  I N T E R F A C E  M E T H O D S
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pTouchEvent) {
		// Pass the event to the pinch zoom
		if(this.mMultiTouchSupported)
			this.mPinchZoomDetector.onTouchEvent(pTouchEvent);

		switch(pTouchEvent.getAction()){
		case TouchEvent.ACTION_DOWN:
			// Stop current flinging
			this.mCamera.stopFling();

			// Get a velocity tracker for new fling
			this.mVelocityTracker = VelocityTracker.obtain();

			// Mark finger down -> smooth scroll start
			this.mSmoothStart = true;

			// No break here to pass down event to scroll detector!
		case TouchEvent.ACTION_MOVE:
			// If not zooming start scrolling
			if(mZooming == false)
				this.mScrollDetector.onTouchEvent(pTouchEvent);
			break;
		case TouchEvent.ACTION_UP:
			// If not zooming start flinging!
			if(mZooming == false) {
				// Pixel per second (1000 ms)
				this.mVelocityTracker.computeCurrentVelocity(1000);
				final float velocityX = this.mVelocityTracker.getXVelocity();
				final float velocityY = this.mVelocityTracker.getYVelocity();
				this.mCamera.fling(velocityX / this.mCamera.getZoomFactor(),
					velocityY / this.mCamera.getZoomFactor());
				this.mVelocityTracker.recycle();
			} else {

			}
		}

		return true;
	}


	@Override
	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent, float pDistanceX, float pDistanceY) {

		// Get camera position
		final float cameraX = this.mCamera.getCenterX();
		final float cameraY = this.mCamera.getCenterY();

		// Add movement to velocity
		this.mVelocityTracker.addMovement(pTouchEvent.getMotionEvent());

		// Check if finger just went down
		if(this.mSmoothStart == true) {
			// Clip movement distance to +/- 1 to make scroll start smooth
			pDistanceX = Math.signum(pDistanceX);
			pDistanceY = Math.signum(pDistanceY);
			this.mSmoothStart = false;
		}
		
		// Move camera object (relative to zoom factor)
		this.mCamera.setCenter(cameraX - pDistanceX / this.mCamera.getZoomFactor(),
			cameraY - pDistanceY / this.mCamera.getZoomFactor());
	}

	@Override
	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector, TouchEvent pSceneTouchEvent) {
		// Get current zoom factor
		this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
		// Stop scrolling and flinging while zooming 
		mZooming = true;
	}

	@Override
	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
		// Zoom camera
		this.mCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
	}

	@Override
	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
		// Zoom camera last time
		this.mCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		// Re-enable scrolling and flinging
		mZooming = false;
	}
}