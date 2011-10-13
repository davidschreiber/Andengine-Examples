package at.mmf.andengine.examples.flingcamera;

import org.anddev.andengine.engine.camera.ZoomCamera;

/**
 * (c) 2011 David Schreiber 
 * 
 * @author David Schreiber
 * @since 10:25:12 - 13.10.2011
 */
public class VelocityCamera extends ZoomCamera {
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . C O N S T A N T S

	private static final float DEFAULT_MIN_MOVEMENT_SPEED = 0.01f;
	private static final float DEFAULT_DECELERATION_FACTOR = 0.99f;
	
	
	// . . . . . . . . . . . . . . . . . . . . . . .  P R I V A T E  F I E L D S

	// Movement threshold before stopping the camera
	private float mMinMovementVelocityX;
	private float mMinMovementVelocityY;

	// Actual speed (pixels / second)
	private float mCurrentSpeedX;
	private float mCurrentSpeedY;
	
	// Deceleration factor each second
	private float mDecelerationFactor;
	
	// Stops flinging
	private boolean mFlingActivated;
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . C O N S T R U C T O R S
	
	public VelocityCamera(final float pX, final float pY, final float pWidth, final float pHeight) {
		super(pX, pY, pWidth, pHeight);
		
		this.mDecelerationFactor = DEFAULT_DECELERATION_FACTOR;
		this.mMinMovementVelocityX = DEFAULT_MIN_MOVEMENT_SPEED;
		this.mMinMovementVelocityY = DEFAULT_MIN_MOVEMENT_SPEED;
	}
	
	public VelocityCamera(final float pX, final float pY, final float pWidth, final float pHeight, final float pDecelerationFactor) {
		super(pX, pY, pWidth, pHeight);
		
		this.mDecelerationFactor = pDecelerationFactor;
		this.mMinMovementVelocityX = DEFAULT_MIN_MOVEMENT_SPEED;
		this.mMinMovementVelocityY = DEFAULT_MIN_MOVEMENT_SPEED;
	}

	public VelocityCamera(final float pX, final float pY, final float pWidth, final float pHeight, final float pDecelerationFactor, final float pMinMovementVelocityX, final float pMinMovementVelocityY) {
		super(pX, pY, pWidth, pHeight);
		
		this.mDecelerationFactor = pDecelerationFactor;
		this.mMinMovementVelocityX = pMinMovementVelocityX;
		this.mMinMovementVelocityY = pMinMovementVelocityY;
	}
	
	
	// . . . . . . . . . . . . . . . . . . . . . . .  P U B L I C  M E T H O D S

	public void fling(final float pVelocityX, final float pVelocityY) {
		// Set the velocity
		this.mCurrentSpeedX = pVelocityX;
		this.mCurrentSpeedY = pVelocityY;
		
		// Activate fling
		this.mFlingActivated = true;
	}
	
	public void stopFling() {
		// Deactivate fling
		this.mFlingActivated = false;
	}

	
	// . . . . . . . . . . . . . . . . .  S U P E R C L A S S  O V E R R I D E S

	@Override
	public void setCenter(float pCenterX, float pCenterY) {
		// Deactivate Fling
		this.mFlingActivated = false;

		// Set camera center
		super.setCenter(pCenterX, pCenterY);
	}
	
	@Override
	public void onUpdate(final float pSecondsElapsed) {
		super.onUpdate(pSecondsElapsed);
		
		// Is fling activated?
		if(mFlingActivated) {
			// Is movement needed?
			if(mCurrentSpeedX != 0 || mCurrentSpeedY != 0) {

				// Calculate deceleration
				this.mCurrentSpeedX *= this.mDecelerationFactor;
				this.mCurrentSpeedY *= this.mDecelerationFactor;
				
				// Calculate movement
				final float movementX = this.mCurrentSpeedX * pSecondsElapsed;
				final float movementY = this.mCurrentSpeedY * pSecondsElapsed;
					
				// Stop fling if X and Y velocity is within threshold
				if(Math.abs(mCurrentSpeedX) <= mMinMovementVelocityX && Math.abs(mCurrentSpeedY) <= mMinMovementVelocityY) {
					mFlingActivated = false;
					mCurrentSpeedX = 0;
					mCurrentSpeedY = 0;
				}
				
				// Calculate camera coordinates
				final float dX = this.getCenterX() - movementX;
				final float dY = this.getCenterY() - movementY;
				
				// Move camera
				super.setCenter(dX, dY);
			}
		} 
	}
}