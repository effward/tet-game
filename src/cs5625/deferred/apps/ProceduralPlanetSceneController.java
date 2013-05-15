package cs5625.deferred.apps;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.media.opengl.glu.GLU;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.GMatrix;
import javax.vecmath.GVector;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.CookTorranceMaterial;
import cs5625.deferred.materials.Material;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.KinematicObject;
import cs5625.deferred.scenegraph.PointLight;
import cs5625.deferred.scenegraph.TetMesh;

import procedural.Heightmesh;
import procedural.Triangle;
import procedural.Vertex;

public class ProceduralPlanetSceneController extends SceneController {
	
	

	/* Used to calculate mouse deltas in mouseDragged(). */ 
	private Point mLastMousePos;
	
	//Default planet values
	private float mMinRadius = 0.5f, mMaxRadius = 1.5f, mScale = 20.0f;

	private int mSubdivs = 6;
	private boolean accelerate = false;

	
	private Geometry planet;
	
	public ProceduralPlanetSceneController(float min, float max) {
		mMinRadius = min;
		mMaxRadius = max;
	}
	
	@Override
	public void initializeScene() {
		
		/*
		Heightmesh waterHM = new Heightmesh();
		waterHM.createIcosa();
		waterHM.subdivide(4, true);
		waterHM.scale(mScale, 0);
		TetMesh waterMesh = waterHM.getTetmesh();
		*/
		
		
		
		//Generate planetary heightmesh...
		Heightmesh planetHM = new Heightmesh();
		planetHM.createIcosa();
		
		planetHM.subdivide(2, true);
		
		/*
		System.out.println("creating variation basis");
		planetHM.subdivide(Math.min(3, mSubdivs), true);
		planetHM.randomize(mMinRadius, mMaxRadius);
		planetHM.smooth(3);
		
		
		
		System.out.println("subdividing");
		planetHM.subdivide(Math.max(mSubdivs - 3, 0), false);
		
		System.out.println("saving frequencies");

		planetHM.saveFrequency(5.0f);
		
		System.out.println("rerandomizing");
		planetHM.randomize(mMinRadius, mMaxRadius);
		planetHM.smooth(3);
		planetHM.randomizeRelative(0.0f, 2.0f);
		
		System.out.println("smoothing according to saved smooth values");

		planetHM.smoothFrequencies(-10, 15);
		
		System.out.println("Eroding");
		planetHM.erode(10, 0.2f);
		
		System.out.println("smoothing one last time");
		planetHM.smoothFrequencies(-10, 20);
		
		planetHM.erode(1, 0.5f);
		
		*/
		
		planetHM.scale(mScale, 0.0f);
		
		
		
		//Convert heightmesh into a tetmesh.
		planet = new Geometry();
		TetMesh planetMesh = planetHM.getTetmesh();
		
		System.out.println("converting to tetmesh");
		
		
		ArrayList<Material> mats1 = new ArrayList<Material>(1);
		ArrayList<Material> mats2 = new ArrayList<Material>(2);
		
		BlinnPhongMaterial rock = new BlinnPhongMaterial();
		BlinnPhongMaterial water = new BlinnPhongMaterial();
		//CookTorranceMaterial water = new CookTorranceMaterial();
		try {
			Texture2D rocktex = Texture2D.load(GLU.getCurrentGL().getGL2(), "textures/Rock.png");
			rock.setDiffuseTexture(rocktex);
			Texture2D watertex = Texture2D.load(GLU.getCurrentGL().getGL2(), "textures/water.jpg");
			water.setDiffuseTexture(watertex);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		rock.setSpecularColor(new Color3f(0.0f, 0.0f, 0.0f));
		mats1.add(rock);
		//water.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
		mats2.add(water);
		//waterMesh.setMats(mats2);
		
		
		
		/*
		planet = new Geometry();
		
		TetMesh planetMesh = new TetMesh();
		ArrayList<Vector3f> vt = new ArrayList<Vector3f>(4);
		ArrayList<Integer> pt = new ArrayList<Integer>(4);
		vt.add(new Vector3f(-1, -1, -1));
		vt.add(new Vector3f(1, -1, -1));
		vt.add(new Vector3f(0, 1, -1));
		vt.add(new Vector3f(0, 0, 1));
		pt.add(0);
		pt.add(1);
		pt.add(2);
		pt.add(3);
		planetMesh.setVerts(vt);
		planetMesh.setTets(pt);
		*/
		
		
		planet.addMesh(planetMesh);
		

		planetMesh.setMats(mats1);
		
		//planet.addMesh(waterMesh);
		
		
		/* Add an unattenuated point light to provide overall illumination. */
		PointLight light = new PointLight();
		
		light.setConstantAttenuation(1.0f);
		light.setLinearAttenuation(0.0f);
		light.setQuadraticAttenuation(0.0f);
		
		light.setPosition(new Point3f(-100.0f, 0.0f, 0.0f));
		light.setName("CameraLight");
		
		
		PointLight light2 = new PointLight();
		
		light2.setConstantAttenuation(1.0f);
		light2.setLinearAttenuation(0.0f);
		light2.setQuadraticAttenuation(0.5f);
		
		light2.setPosition(new Point3f(0.0f, 0.0f, 0.0f));
		light2.setName("CameraLight");
		
		try {
			mSceneRoot.addChild(mCamera);
			
			mCamera.addChild(light2); //light follows the player around
			mSceneRoot.addChild(light);	
			mSceneRoot.addChild(planet);
			
			
		} catch (ScenegraphException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

	
	@Override
	public void keyPressed(KeyEvent key) {
		int c = key.getKeyCode();
		
		switch(mCamera.getMode()) {
		case ORBIT: {
			
		}
		case FLY: {
			if(c == KeyEvent.VK_UP) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, 0.0f, -mCamera.accVal));
			}
			else if(c == KeyEvent.VK_DOWN) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, 0.0f, mCamera.accVal));
			}
			else if (c == KeyEvent.VK_LEFT) {
				mCamera.setLocalAcceleration(new Vector3f(-mCamera.accVal, 0.0f, 0.0f));
			}
			else if (c == KeyEvent.VK_RIGHT) {
				mCamera.setLocalAcceleration(new Vector3f(mCamera.accVal, 0.0f, 0.0f));
			}
			else if (c == KeyEvent.VK_ADD) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, mCamera.accVal, 0.0f));
			}
			else if (c == KeyEvent.VK_SUBTRACT) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, -mCamera.accVal, 0.0f));
			}
			requiresRender();
			
			break;
		}
		case WALK: {
			//...
			break;
		}
		}
		
	}
	
	@Override
	public void keyReleased(KeyEvent key) {
		int c = key.getKeyCode();
		
		switch(mCamera.getMode()) {
		case ORBIT: {
			
			break;
		}
		case FLY: {
			if(c == KeyEvent.VK_UP) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, 0.0f, 0.0f));
			}
			else if(c == KeyEvent.VK_DOWN) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, 0.0f, 0.0f));
			}
			else if (c == KeyEvent.VK_LEFT) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, 0.0f, 0.0f));
			}
			else if (c == KeyEvent.VK_RIGHT) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, 0.0f, 0.0f));
			}
			else if (c == KeyEvent.VK_ADD) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, 0.0f, 0.0f));
			}
			else if (c == KeyEvent.VK_SUBTRACT) {
				mCamera.setLocalAcceleration(new Vector3f(0.0f, 0.0f, 0.0f));
			}
			requiresRender();
			break;
		}
		case WALK: {
			//...
			break;
		}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent key) {
		char c = Character.toLowerCase(key.getKeyChar());
		
		if (c == 'w') {
			mRenderer.setRenderWireframes(!mRenderer.getRenderWireframes());
			requiresRender();
		}	
		else if (c == 'a') {
			accelerate = !accelerate;
		}
		else if (c == 'c') {
			Vector3f dir = new Vector3f(0f, 0f, -1f);
			Util.rotateTuple(mCamera.getOrientation(), dir);
			TetMesh mesh = (TetMesh)(planet.getMeshes().get(0));
			dir.add(mCamera.getPosition(), dir);
			mesh.createTetAtFirstFaceAlongLine(new Vector3f(mCamera.getPosition()), dir, accelerate);
			requiresRender();
		}
		else if (c == 'o') {
			//Swap camera modes...
			switch(mCamera.getMode()) {
			case ORBIT: {
				mCamera.setMode(CamMode.FLY);
				//set kinematic position to orbit location
				
				
				break;
			}
			case FLY: {
				mCamera.setMode(CamMode.ORBIT);
				break;
			}
			case WALK: {
				//...
				break;
			}
			}
		}
		if (c == 'p') {
			System.exit(0);
		}
	}
	

	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheel) {
		/* Zoom in and out by the scroll wheel. */
		switch(mCamera.getMode()) {
		case ORBIT: {
			mCamera.radius += mouseWheel.getUnitsToScroll();
			requiresRender();
			break;
		}
		case FLY: {
			
			break;
		}
		case WALK: {
			
			break;
		}
		}
	}

	@Override
	public void mousePressed(MouseEvent mouse)	{
		/* Remember the starting point of a drag. */
		mLastMousePos = mouse.getPoint();
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		Vector3f dir = new Vector3f(0f, 0f, -1f);
		Util.rotateTuple(mCamera.getOrientation(), dir);
		TetMesh mesh = (TetMesh)(planet.getMeshes().get(0));
		dir.add(mCamera.getPosition(), dir);
		mesh.deleteFirstTetAlongLine(new Vector3f(mCamera.getPosition()), dir, accelerate);
		requiresRender();
	}
	
	
	@Override
	public void mouseDragged(MouseEvent mouse) {
		/* Calculate dragged delta. */
		float deltaX = (mouse.getPoint().x - mLastMousePos.x);
		float deltaY = (mouse.getPoint().y - mLastMousePos.y);
		mLastMousePos = mouse.getPoint();
		
		switch(mCamera.getMode()) {
		case ORBIT: {
			/* Update longitude, wrapping as necessary. */
			mCamera.longitude -= deltaX;
			
			if (mCamera.longitude > 360.0f) {
				mCamera.longitude -= 360.0f;
			}
			else if (mCamera.longitude < 0.0f) {
				mCamera.longitude += 360.0f;
			}
			
			/* Update latitude, clamping as necessary. */
			if (Math.abs(mCamera.latitude - deltaY) <= 89.0f) {
				mCamera.latitude -= deltaY;
			}
			else {
				mCamera.latitude = 89.0f * Math.signum(mCamera.latitude);
			}
		
			requiresRender();
			
			break;
		}
		case FLY: {
			float scale = 0.005f;
			
			//Update camera heading
			mCamera.deltaTheta += deltaX * scale;
			mCamera.deltaPhi -= deltaY * scale;
			
			requiresRender();
			break;
		}
		case WALK: {
			requiresRender();
			break;
		}
		}

	}
	
	/** An enumeration between possible camera modes. */
	public enum CamMode{ORBIT, FLY, WALK};

}
