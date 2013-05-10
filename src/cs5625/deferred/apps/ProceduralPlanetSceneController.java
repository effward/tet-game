package cs5625.deferred.apps;

import java.awt.Point;
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
	private float mMinRadius = .5f, mMaxRadius = 1.5f, mScale = 5.0f;
	
	
	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
	private Point mLastMouseDrag;
	
	public ProceduralPlanetSceneController(float min, float max) {
		mMinRadius = min;
		mMaxRadius = max;
	}
	
	@Override
	public void initializeScene() {
		
		Geometry planet = new Geometry();
		
		
		TetMesh planetMesh = new TetMesh();
		
		Heightmesh planetHM = new Heightmesh();
		planetHM.createIcosa();
		
		System.out.println("starting subdivs");
		planetHM.subdivide(5);
		planetHM.randomize(mMinRadius, mMaxRadius);
		
		System.out.println("starting smoothing");
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
		
		
		for (int i = 0; i < trisHM.size(); i+= 1) {
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
		
		
		planet.addMesh(planetMesh);
		
		
		/* Add an unattenuated point light to provide overall illumination. */
		PointLight light = new PointLight();
		
		light.setConstantAttenuation(1.0f);
		light.setLinearAttenuation(0.0f);
		light.setQuadraticAttenuation(0.0f);
		
		light.setPosition(new Point3f(10.0f, 0.0f, 0.0f));
		light.setName("CameraLight");
		
		
		try {
			
			
			mSceneRoot.addChild(light);	
			
			mSceneRoot.addChild(planet);
			
		} catch (ScenegraphException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Initialize camera position. */
		updateCamera();
	}
	
	protected void updateCamera()
	{
		/* Compose the "horizontal" and "vertical" rotations. */
		Quat4f longitudeQuat = new Quat4f();
		longitudeQuat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, mCameraLongitude * (float)Math.PI / 180.0f));
		
		Quat4f latitudeQuat = new Quat4f();
		latitudeQuat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, mCameraLatitude * (float)Math.PI / 180.0f));

		mCamera.getOrientation().mul(longitudeQuat, latitudeQuat);
		
		/* Set the camera's position so that it looks towards the origin. */
		mCamera.setPosition(new Point3f(0.0f, 0.0f, mCameraRadius));
		Util.rotateTuple(mCamera.getOrientation(), mCamera.getPosition());
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		int c = key.getKeyCode();
		if(c == KeyEvent.VK_W) {
			
		}
	}
	
	@Override
	public void keyReleased(KeyEvent key) {
		
	}
	
	@Override
	public void keyTyped(KeyEvent key) {
		
	}
	

	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheel) {
		/* Zoom in and out by the scroll wheel. */
		if (!isShadowCamMode) {
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
	public void mouseDragged(MouseEvent mouse)
	{
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
