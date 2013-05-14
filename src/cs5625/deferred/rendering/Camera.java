package cs5625.deferred.rendering;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import cs5625.deferred.apps.ProceduralPlanetSceneController.CamMode;
import cs5625.deferred.misc.Util;
import cs5625.deferred.scenegraph.KinematicObject;
import cs5625.deferred.scenegraph.SceneObject;

/**
 * Camera.java
 * 
 * Represents a perspective camera. Since Camera inherits from SceneObject, you could add it as a 
 * child of another object in the scene to have it follow that object, or add geometry or lights 
 * as children of the camera to have those objects follow the camera.   
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), John DeCorato (jd537)
 * @date 2012-03-23
 */
public class Camera extends SceneObject {
	/* Perspective camera attributes. */
	private float mFOV = 45.0f;
	private float mNear = 0.1f;
	private float mFar = 100.0f;
	
	
	//Variety of specific camera attributes...
	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
	public float longitude = 0.0f, latitude = 0.0f;
	public float radius = 50.0f;

	/* Keep track of camera rotation for walking/flying mode. */
	public float deltaTheta = 0.0f;
	public float deltaPhi = 0f;
	public float accVal = 5.0f;
	
	public Vector3f localAcc = new Vector3f();

	
	
	/* Keep track of camera mode. */
	private CamMode camMode = CamMode.ORBIT;
	
	private KinematicObject camKin;
	
	private boolean mIsShadowMapCamera = false;
	
	public Camera() {
		camKin = new KinematicObject(
				new Vector3f(),
				new Vector3f(),
				new Vector3f(),
				new Vector3f(0.3f, 0.3f, 0.3f)
		);
	}
	
	/**
	 * Updates any animation for this node at each frame, if any.
	 * Default implementation calls `animate(dt)` on children.
	 * 
	 * @param dt The time delta (in seconds) since the last frame.
	 */
	public void animate(float dt) {
		switch(camMode) {
		case ORBIT: { 
			/* Compose the "horizontal" and "vertical" rotations. */
			Quat4f longitudeQuat = new Quat4f();
			longitudeQuat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, longitude * (float)Math.PI / 180.0f));
			
			Quat4f latitudeQuat = new Quat4f();
			latitudeQuat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, latitude * (float)Math.PI / 180.0f));
	
			getOrientation().mul(longitudeQuat, latitudeQuat);
			
			/* Set the camera's position so that it looks towards the origin. */
			setPosition(new Point3f(0.0f, 0.0f, radius));
			Util.rotateTuple(getOrientation(), getPosition());
			break;
		}
		case FLY: {
			//Calculate up/down rotation
			Quat4f updown = new Quat4f();
			updown.set(new AxisAngle4f(new Vector3f(-1.0f, 0.0f, 0.0f), deltaPhi));
			deltaPhi = 0.0f;
			
			//Calculate left/right rotation
			Quat4f leftright = new Quat4f();
			leftright.set(new AxisAngle4f(new Vector3f(0.0f, 1.0f, 0.0f), deltaTheta));
			deltaTheta = 0.0f;
			
			//Now rotate the current orientation by the updown/leftright rotations.
			getOrientation().mul(updown);
			getOrientation().mul(leftright);
			
			
			//Move the camera
			camKin.setAcc(localAcc);
			Util.rotateTuple(mOrientation, camKin.getAcc());
			camKin.updateFree(dt);
			setPosition(new Point3f(camKin.getPos()));
			
			
			//Find new up, forward, left vectors..
			Vector3f worldup = new Vector3f(camKin.getPos()); //subtract planet center if not origin
			worldup.normalize();
			
			Vector3f ourup = new Vector3f(0.0f, 1.0f, 0.0f);
			Util.rotateTuple(mOrientation, ourup);
			
			Vector3f ourfwd = new Vector3f(0.0f, 0.0f, -1.0f);
			Util.rotateTuple(mOrientation, ourfwd);
			
			Vector3f ourleft = new Vector3f(-1.0f, 0.0f, 0.0f);
			Util.rotateTuple(mOrientation, ourleft);
			
			Vector3f crossLeft = new Vector3f();
			crossLeft.cross(worldup, ourfwd);
			crossLeft.normalize();
			
			Vector3f crossUp = new Vector3f();
			crossUp.cross(ourfwd, crossLeft);
			crossUp.normalize();
			
			Vector3f crossFwd = new Vector3f();
			crossFwd.cross(ourup, crossUp);
			float d = -(float)Math.signum(crossFwd.dot(ourfwd));
			
			if (crossFwd.lengthSquared() > 0.0f) {
				
				float currentBarrel = (float)Math.acos(Math.max(-1.0f, Math.min(1.0f, ourup.dot(crossUp))));
				//Correct barrel...
				Quat4f barrelCorrect = new Quat4f();
				barrelCorrect.set(new AxisAngle4f(new Vector3f(0.0f, 0.0f, d), currentBarrel));
				
				getOrientation().mul(barrelCorrect);
			}

			
			/*
			//Correct orientation by the movement ('keeps down down')
			Quat4f correction = new Quat4f();
			
			float theta = (float)Math.acos(oldPos.dot(camKin.getPos()) / (oldPos.length() * camKin.getPos().length()));
			Vector3f deltaPos = new Vector3f();
			deltaPos.sub(camKin.getPos(), oldPos);
			deltaPos.normalize();
			
			Vector3f turnAxis = new Vector3f();
			turnAxis.cross(deltaPos, oldPos);
			oldPos.cross(oldPos, up);
			correction.set(new AxisAngle4f())
			*/
			
			/*
			System.out.println("Camera animation === ");
			System.out.println("Pos: " + camKin.getPos());
			System.out.println("Vel: " + camKin.getVel());
			System.out.println("Acc: " + camKin.getAcc());
			System.out.println("Friction: " + camKin.getFriction() + "\n");
			*/
			break;
		}
		case WALK: {
			//...
			break;
		}
		}
		
	
			
		
		super.animate(dt);
	}
	
	public CamMode getMode() {
		return camMode;
	}
	
	public void setMode(CamMode mode) {
		this.camMode = mode;
	}
	
	/** Set the position of the camera. */
	public void setPosition(Point3f pos) {
		super.setPosition(pos);
		camKin.setPos(pos);
	}
	
	/** set acceleration in local camera coords. */
	public void setLocalAcceleration(Vector3f acc) {
		localAcc = acc;
	}
	
	/**
	 * Returns the camera field of view angle, in degrees.
	 * 
	 * This is the full angle, not the half angle.
	 */
	public float getFOV()
	{
		return mFOV;
	}

	/**
	 * Sets the camera's field of view.
	 * @param fov Desired field of view, in degrees. Must be in the interval (0, 180).
	 */
	public void setFOV(float fov)
	{
		mFOV = fov;
	}
	
	/**
	 * Returns the camera near plane distance.
	 */
	public float getNear()
	{
		return mNear;
	}

	/**
	 * Sets the camera near plane distance.
	 * 
	 * @param near The near plane. Must be positive.
	 */
	public void setNear(float near)
	{
		mNear = near;
	}

	/**
	 * Returns the camera far plane distance.
	 */
	public float getFar()
	{
		return mFar;
	}

	/**
	 * Sets the camera far plane distance.
	 * @param far The far plane; must be farther away than the near plane.
	 */
	public void setFar(float far)
	{
		mFar = far;
	}
	
	/**
	 * Returns if this is a shadow map camera
	 */
	public boolean getIsShadowMapCamera()
	{
		return mIsShadowMapCamera;
	}
	
	/**
	 * Sets shadow map camera state
	 */
	public void setIsShadowMapCamera(boolean isShadowMapCamera)
	{
		mIsShadowMapCamera = isShadowMapCamera;
	}
	
	
	/**
	 *  Get the view matrix that send points from world space into this camera local space 
	 */
	public Matrix4f getViewMatrix() {
		Matrix4f mView = getWorldSpaceTransformationMatrix4f();
		mView.invert();
		
		return mView;
	}
	
	public Matrix4f getProjectionMatrix(float width, float height) {
		float aspect = width/ height;
		float s = (float) (1f / (Math.tan(mFOV * 0.5 * Math.PI / 180)));
		return new Matrix4f(
				s/aspect, 0f, 0f, 0f,
				0f, s, 0f, 0f,
				0f, 0f, -(mFar + mNear) / (mFar - mNear), -2 * mFar * mNear / (mFar - mNear),
				0f, 0f, -1f, 0f);
	}
}
