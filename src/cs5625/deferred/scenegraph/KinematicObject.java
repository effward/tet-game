package cs5625.deferred.scenegraph;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;


/** Represents a moving object in the game. */
public class KinematicObject {
	private Vector3f pos;
	private Vector3f vel;
	private Vector3f acc;
	private Vector3f friction;
	
	/** Constructor.
	 * 
	 * @param pos - the starting position.
	 * @param vel - the starting velocity.
	 * @param acc - the starting acceleration.
	 */
	public KinematicObject(Vector3f pos, Vector3f vel, Vector3f acc, Vector3f friction) {
		this.pos = new Vector3f(pos);
		this.vel = new Vector3f(vel);
		this.acc = new Vector3f(acc);
		this.friction = new Vector3f(friction);
	}
	
	/** Get the position.
	 * 
	 * @return the position.
	 */
	public Vector3f getPos() {
		return pos;
	}
	
	/** Get the velocity.
	 * 
	 * @return the velocity.
	 */
	public Vector3f getVel() {
		return vel;
	}
	
	/** Get the acceleration.
	 * 
	 * @return the acceleration.
	 */
	public Vector3f getAcc() {
		return acc;
	}
	
	/** Get the friction opposing this object's movement (a decay coefficient: fraction of velocity lost per second)
	 * 
	 * @return the friction.
	 */
	public Vector3f getFriction() {
		return friction;
	}
	
	/** Directly set the position.
	 * 
	 * @param pos - the new position.
	 */
	public void setPos(Tuple3f pos) {
		this.pos.set(pos); //hard copy
	}
	
	/** Directly set the velocity.
	 * 
	 * @param vel - the new velocity.
	 */
	public void setVel(Tuple3f vel) {
		this.vel.set(vel); //hard copy
	}
	
	/** Directly set the acceleration.
	 * 
	 * @param acc - the new acceleration.
	 */
	public void setAcc(Tuple3f acc) {
		this.acc.set(acc); //hard copy
	}
	
	/** Directly set the friction.
	 * 
	 * @param friction - the new friction.
	 */
	public void setFriction(Tuple3f friction) {
		this.friction.set(friction);
	}
	
	/**
	 * Apply an impulse to this KinematicObject.
	 * @param impulse - the delta v to add to the current velocity.
	 */
	public void applyImpulse(Vector3f impulse) {
		this.vel.add(impulse);
	}
	
	/** Updates current position and velocity in accord with current position, velocity, and acceleration.
	 *  No collisions are dealt with. 
	 * 
	 * @param t - the period of time the update spans. So 0.5 would be 1/2 second. Assumes acceleration is constant during that time.
	 */
	public void updateFree(float t) {
		pos.setX(pos.getX() + vel.getX() * t + 0.5f * acc.getX() * t * t);
		pos.setY(pos.getY() + vel.getY() * t + 0.5f * acc.getY() * t * t);
		pos.setZ(pos.getZ() + vel.getZ() * t + 0.5f * acc.getZ() * t * t);
		
		vel.setX(vel.getX() * (float)Math.pow(friction.getX(), t) + acc.getX() * t);
		vel.setY(vel.getY() * (float)Math.pow(friction.getY(), t) + acc.getY() * t);
		vel.setZ(vel.getZ() * (float)Math.pow(friction.getZ(), t) + acc.getZ() * t);
	}
}
