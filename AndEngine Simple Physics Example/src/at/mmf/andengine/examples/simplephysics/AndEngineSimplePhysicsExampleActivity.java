package at.mmf.andengine.examples.simplephysics;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class AndEngineSimplePhysicsExampleActivity extends BaseGameActivity implements IAccelerometerListener {

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;

	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

	private Scene mScene;
	private Camera mCamera;

	private PhysicsWorld physicsWorld;

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TextureRegion mBitmapTextureRegion;


	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public Engine onLoadEngine() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);

		return new Engine(options);
	}

	@Override
	public void onLoadResources() { 
		// Create a new texture
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(128, 128, TextureOptions.BILINEAR);

		// Create texture source
		final IBitmapTextureAtlasSource faceTextureSource = new AssetBitmapTextureAtlasSource(this, "gfx/sprite.png");

		// Create texture region
		this.mBitmapTextureRegion = TextureRegionFactory.createFromSource(this.mBitmapTextureAtlas,
			faceTextureSource, 0, 0, true);

		// Load the texture into memory
		this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas);
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		// Create our scene
		this.mScene = new Scene();
		this.mScene.setBackground(new ColorBackground(1f, 1f, 1f));
		// Create our physics world
		this.physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		// Register our physics world to update our scene
		this.mScene.registerUpdateHandler(this.physicsWorld);

		// Enable the accelerometer and bind our own onAccelerometerChanged() method as listener for events
		this.mEngine.enableAccelerometerSensor(this, this);

		// Create walls around our scene
		createWalls();

		// Create the moving body
		createPhysicsBody();

		return this.mScene;
	}

	private void createWalls() {
		final Shape ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
		final Shape roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
		final Shape left = new Rectangle(0, 0, 2, CAMERA_HEIGHT);
		final Shape right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.physicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.physicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.physicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.physicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
	}

	private void createPhysicsBody() {
		// Create the sprite
		final Sprite physicsSprite = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, this.mBitmapTextureRegion);
		// Create the physics body
		final Body body = PhysicsFactory.createBoxBody(this.physicsWorld, physicsSprite,
			BodyType.DynamicBody, FIXTURE_DEF);

		// Add the sprite to the scene 
		this.mScene.attachChild(physicsSprite);

		// Link the sprite and the body
		this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(physicsSprite, body, true, true));
	}

	public void onAccelerometerChanged(final AccelerometerData pAccelerometerData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerometerData.getX(),
			pAccelerometerData.getY());
		this.physicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}
}