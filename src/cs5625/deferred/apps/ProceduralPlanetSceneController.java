package cs5625.deferred.apps;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.PointLight;
import cs5625.deferred.scenegraph.TetMesh;

import procedural.Heightmesh;
import procedural.Triangle;
import procedural.Vertex;

public class ProceduralPlanetSceneController extends SceneController {

	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
	private float mCameraLongitude = 50.0f, mCameraLatitude = -40.0f;
	private float mCameraRadius = 15.0f;
	private Point3f mCameraPosition = new Point3f();
	private Quat4f mCameraOrientation = new Quat4f();
	private float mMinRadius = .5f, mMaxRadius = 1.5f, mScale = 5.0f;
	private boolean mOrbitCameraMode = true;
	private Robot robot;
	
	
	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
	private Point mLastMouseDrag;
	private Point mLastMousePos;
	
	public ProceduralPlanetSceneController(float min, float max) {
		mMinRadius = min;
		mMaxRadius = max;
	}
	
	@Override
	public void initializeScene() {
		try {
			robot = new Robot();
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Geometry planet = new Geometry();
		
		
		TetMesh planetMesh = new TetMesh();
		
		Heightmesh planetHM = new Heightmesh();
		planetHM.createIcosa();
		
		
		planetHM.subdivide(4);
		planetHM.randomize(mMinRadius, mMaxRadius);
		planetHM.smooth(3);
		planetHM.scale(mScale);
		
		ArrayList<Vertex> vertsHM = planetHM.getVerts();
		int numVerts = vertsHM.size() + 1;
		ArrayList<Triangle> trisHM = planetHM.getTriangles();
		
		ArrayList<Vector3f> verts = new ArrayList<Vector3f>(numVerts);
		ArrayList<Integer> tets = new ArrayList<Integer>(trisHM.size()*4);
		for (Vertex v : vertsHM) {
			verts.add(v.pt.v);
		}
		// add center of the planet as a point
		verts.add(new Vector3f(0f,0f,0f));
		
		planetMesh.setVerts(verts);
		
		
		for (int i = 0; i < trisHM.size(); i++) {
			Triangle t = trisHM.get(i);
			tets.add(t.v0);
			tets.add(t.v1);
			tets.add(t.v2);
			tets.add(numVerts-1);
		}
		
		planetMesh.setTets(tets);
		
		 
		 
		/*
		planetMesh = new TetMesh();
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
		
		//planetMesh = new TetMesh(1.0f); //test
		
		planet.addMesh(planetMesh);
		
		try {
			/* Add an unattenuated point light to provide overall illumination. */
			PointLight light = new PointLight();
			
			light.setConstantAttenuation(1.0f);
			light.setLinearAttenuation(0.0f);
			light.setQuadraticAttenuation(0.0f);
			
			light.setPosition(new Point3f(0.0f, 10.0f, 0.0f));
			light.setName("CameraLight");
			mSceneRoot.addChild(light);	
			
			mSceneRoot.addChild(planet);
		} catch (ScenegraphException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Initialize camera position. */
		updateCamera();
		//robot.mouseMove(200,200);
		//mCameraPosition = mCamera.getPosition();
		mCameraOrientation = mCamera.getOrientation();
		System.out.println("CameraPosition: (" + mCameraPosition.x + ", " + mCameraPosition.y + ", " + mCameraPosition.z + ")");
		System.out.println("CameraWorldPosition: (" + mCamera.getWorldspacePosition().x + ", " + mCamera.getWorldspacePosition().y + ", " + mCamera.getWorldspacePosition().z + ")");

	}
	
	protected void updateCamera()
	{
		System.out.println("CameraPosition: (" + mCameraPosition.x + ", " + mCameraPosition.y + ", " + mCameraPosition.z + ")");
		System.out.println("CameraOrientation: (" + mCameraOrientation.x + ", " + mCameraOrientation.y + ", " + mCameraOrientation.z + ", " + mCameraOrientation.w + ")");
		if (mOrbitCameraMode) {
			/* Compose the "horizontal" and "vertical" rotations. */
			Quat4f longitudeQuat = new Quat4f();
			longitudeQuat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, mCameraLongitude * (float)Math.PI / 180.0f));
			
			Quat4f latitudeQuat = new Quat4f();
			latitudeQuat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, mCameraLatitude * (float)Math.PI / 180.0f));
	
			mCamera.getOrientation().mul(longitudeQuat, latitudeQuat);
			
			/* Set the camera's position so that it looks towards the origin. */
			mCamera.setPosition(new Point3f(0.0f, 0.0f, mCameraRadius));
			Util.rotateTuple(mCamera.getOrientation(), mCamera.getPosition());
			//mCameraPosition = mCamera.getPosition();
			mCameraOrientation = mCamera.getOrientation();
		}
		else {
			Point3f worldPos = mCamera.transformPointToWorldSpace(mCameraPosition);
			/*
			Vector3f up = new Vector3f(worldPos);
			up.normalize();
			Vector3f worldCameraUp = new Vector3f(mCamera.transformPointToWorldSpace(new Point3f(0f,1f,0f)));
			worldCameraUp.normalize();
			Vector3f worldCameraLeft = new Vector3f(mCamera.transformPointToWorldSpace(new Point3f(1f,0f,0f)));
			worldCameraLeft.normalize();
			Vector3f left = new Vector3f();
			left.cross(up, worldCameraUp);
			Vector3f forward = new Vector3f();
			forward.cross(up,left);
			float upAngle = (float) Math.acos(up.dot(worldCameraUp));
			float leftAngle = (float) Math.acos(left.dot(worldCameraLeft));
			
			AxisAngle4f upAdjust = new AxisAngle4f(left, upAngle);
			AxisAngle4f leftAdjust = new AxisAngle4f(forward, leftAngle);
			Quat4f upQuat = new Quat4f();
			upQuat.set(upAdjust);
			Quat4f leftQuat = new Quat4f();
			leftQuat.set(leftAdjust);
			
			Quat4f combo = new Quat4f();
			combo.mul(upQuat, leftQuat);
			combo.mul(combo, mCameraOrientation);
			
			
			mCamera.setOrientation(combo);
			*/
			mCamera.setOrientation(mCameraOrientation);
			mCamera.setPosition(worldPos);
			mCameraPosition.scale(0);
		}
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		int c = key.getKeyCode();
		if (mOrbitCameraMode) {
			//nothing?
		}
		else {
			if(c == KeyEvent.VK_W) {
				mCameraPosition.z -= 0.1f;
				System.out.println("W");
			}
			else if(c == KeyEvent.VK_S) {
				mCameraPosition.z += 0.1f;
				System.out.println("S");
			}
			else if (c == KeyEvent.VK_A) {
				mCameraPosition.x -= 0.1f;
				System.out.println("A");
			}
			else if (c == KeyEvent.VK_D) {
				mCameraPosition.x += 0.1f;
				System.out.println("D");
			}
			updateCamera();
			requiresRender();
		}
	}
	
	@Override
	public void keyReleased(KeyEvent key) {
		
	}
	
	@Override
	public void keyTyped(KeyEvent key) {
		char c = key.getKeyChar();
		if (c == 'o' || c == 'O') {
			if (mOrbitCameraMode)
				mOrbitCameraMode = false;
			else
				mOrbitCameraMode = true;
		}
		if (c == 'p' || c == 'P') {
			System.exit(0);
		}
	}
	

	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheel) {
		/* Zoom in and out by the scroll wheel. */
		if (mOrbitCameraMode) {
			mCameraRadius += mouseWheel.getUnitsToScroll();
			updateCamera();
			requiresRender();
		}
	}

	@Override
	public void mousePressed(MouseEvent mouse)
	{
		/* Remember the starting point of a drag. */
		mLastMouseDrag = mouse.getPoint();
	}
	
	@Override
	public void mouseMoved(MouseEvent mouse) {
		
		//float deltaX = -(mouse.getPoint().x);
		//float deltaY = -(mouse.getPoint().y);
		if(mOrbitCameraMode) {
			mLastMousePos = mouse.getPoint();
		}
		else {
			if (mouse.getPoint() != null) {
				float deltaX = -(mouse.getPoint().x - mLastMousePos.x);
				float deltaY = -(mouse.getPoint().y - mLastMousePos.y);
				mLastMousePos = mouse.getPoint();
				System.out.println("DeltaX = " + deltaX);
				System.out.println("DeltaY = " + deltaY);
				
				float scale = 0.1f;
				
				Quat4f horiz = Util.quatFromAngle(0.0f, 1.0f, 0.0f, deltaX*scale*(float)Math.PI/180f);
				Quat4f vert = Util.quatFromAngle(1.0f, 0.0f, 0.0f, deltaY*scale*(float)Math.PI/180f);
				
				mCameraOrientation.mul(horiz, mCameraOrientation);
				mCameraOrientation.mul(vert, mCameraOrientation);
				updateCamera();
				requiresRender();
			}
		}
		//robot.mouseMove(200, 200);
	}
	
	
	
	@Override
	public void mouseDragged(MouseEvent mouse)
	{
		if(mOrbitCameraMode) {
			/* Calculate dragged delta. */
			float deltaX = -(mouse.getPoint().x - mLastMouseDrag.x);
			float deltaY = -(mouse.getPoint().y - mLastMouseDrag.y);
			mLastMouseDrag = mouse.getPoint();
			
			/* Update longitude, wrapping as necessary. */
			mCameraLongitude += deltaX;
			
			if (mCameraLongitude > 360.0f)
			{
				mCameraLongitude -= 360.0f;
			}
			else if (mCameraLongitude < 0.0f)
			{
				mCameraLongitude += 360.0f;
			}
			
			/* Update latitude, clamping as necessary. */
			if (Math.abs(mCameraLatitude + deltaY) <= 89.0f)
			{
				mCameraLatitude += deltaY;
			}
			else
			{
				mCameraLatitude = 89.0f * Math.signum(mCameraLatitude);
			}
		
			updateCamera();
			
			requiresRender();
		}
	}

}
