package procedural;

import javax.vecmath.Vector3f;

/* Vec3f.java
 * By: Noah Warnke
 * 
 */

/** Represents a 3D vector with float-valued components. */
public class Vec3f {
	//The three components.
	//private float x;
	//private float y;
	//private float z;
	public Vector3f v;
	
	//Various static values.
	public static final Vec3f ZERO_VECTOR   = new Vec3f(0.0f, 0.0f, 0.0f);
	public static final Vec3f ONES_VECTOR   = new Vec3f(1.0f, 1.0f, 1.0f);
	public static final Vec3f UNIT_X_VECTOR = new Vec3f(1.0f, 0.0f, 0.0f);
	public static final Vec3f UNIT_Y_VECTOR = new Vec3f(0.0f, 1.0f, 0.0f);
	public static final Vec3f UNIT_Z_VECTOR = new Vec3f(0.0f, 0.0f, 1.0f);
	
	/** Constructor: = a new Vec3f with components <0, 0, 0>. */
	public Vec3f() {
		//x = 0.0f;
		//y = 0.0f;
		//z = 0.0f;
		v = new Vector3f(0f,0f,0f);
	}
	
	/** Constructor: = a new Vec3f with components <inX, inY, inZ>. */
	public Vec3f (float inX, float inY, float inZ) {
		//x = inX;
		//y = inY;
		//z = inZ;
		v = new Vector3f(inX, inY, inZ);
	}
	
	/** Constructor: = a new Vec3f with components <inVec.x, inVec.y, inVec.z>. */
	public Vec3f(Vec3f inVec) {
		//x = inVec.x;
		//y = inVec.y;
		//z = inVec.z;
		v = new Vector3f(inVec.v);
	}
	
	/** = this Vec3f's x component. */
	public float getX() {
		//return x;
		return v.x;
	}
	
	/** = this Vec3f's y component. */
	public float getY() {
		//return y;
		return v.y;
	}
	
	/** = this Vec3f's z component. */
	public float getZ() {
		return v.z;
	}
	
	/** Set this Vec3f's x component to inX. */
	public void setX(float inX) {
		v.x = inX;
	}
	
	/** Set this Vec3f's x component to inX. */
	public void setY(float inY) {
		v.y = inY;
	}
	
	/** Set this Vec3f's x component to inX. */
	public void setZ(float inZ) {
		v.z = inZ;
	}
	
	/** Set this Vec3f's components to x, y, z. */
	public void set(float x, float y, float z) {
		this.v.x = x;
		this.v.y = y;
		this.v.z = z;
	}
	
	/** Set this Vec3f's components to those of v. */
	public void set(Vec3f vec) {
		this.v.x = vec.v.x;
		this.v.y = vec.v.y;
		this.v.z = vec.v.z;
	}
	
	/** = a String representation of this Vec3f. */
	public String toString() {
		return "<" + v.x + ", " + v.y + ", " + v.z + ">";
	}
	
	/** = this Vec3f in a 3-element float array. */
	public float[] toArray() {
		float[] result = {v.x, v.y, v.z};
		return result;
	}
	
	/** = "This Vec3f has the same component values as inVec." */
	public boolean equals(Vec3f inVec) {
		return (v.x == inVec.v.x && v.y == inVec.v.y && v.z == inVec.v.z);
	}
	
	/** = a copy (aka clone) of this Vec3f. */
	public Vec3f copy() {
		return new Vec3f(v.x, v.y, v.z);
	}
	
	/** = a new Vec3f that equals this Vec3f plus inVec. */
	public Vec3f plus(Vec3f rhs) {
		return new Vec3f(v.x + rhs.v.x, v.y + rhs.v.y, v.z + rhs.v.z);
	}
	
	/** Set result to be the sum of this Vec3f and rhs. */
	public void plusSet(Vec3f rhs, Vec3f result) {
		result.set(this.v.x + rhs.v.x, this.v.y + rhs.v.y, this.v.z + rhs.v.z);
	}
	
	/** Add rhs's components to this Vec3f's components. */
	public void plusEquals(Vec3f rhs) {
		v.x += rhs.v.x;
		v.y += rhs.v.y;
		v.z += rhs.v.z;
	}
	
	/** Add dx, dy, dz to x, y, z. */
	public void plusEquals(float dx, float dy, float dz) {
		v.x += dx;
		v.y += dy;
		v.z += dz;
	}
	
	/** Add rhs to x. */
	public void xPlusEquals(float rhs) {
		v.x += rhs;
	}
	
	/** Add rhs to y. */
	public void yPlusEquals(float rhs) {
		v.y += rhs;
	}
	
	/** Add rhs to z. */
	public void zPlusEquals(float rhs) {
		v.z += rhs;
	}
	
	/**  = a new Vec3f that equals this Vec3f minus irhs. */
	public Vec3f minus(Vec3f rhs) {
		return new Vec3f(v.x - rhs.v.x, v.y - rhs.v.y, v.z - rhs.v.z);
	}
	
	/** Sets result to be this Vec3f minus rhs. */
	public void minusSet(Vec3f rhs, Vec3f result) {
		result.set(
			this.v.x - rhs.v.x,
			this.v.y - rhs.v.y,
			this.v.z - rhs.v.z
		);
	}
	
	/** Subtract inVec's components from this Vec3f's components. */
	public void minusEquals(Vec3f rhs) {
		v.x -= rhs.v.x;
		v.y -= rhs.v.y;
		v.z -= rhs.v.z;
	}
	
	/** Subtract dx, dy, dz from x, y, z. */
	public void minusEquals(float dx, float dy, float dz) {
		v.x -= dx;
		v.y -= dy;
		v.z -= dz;
	}
	
	/** Subtract rhs from x. */
	public void xMinusEquals(float rhs) {
		v.x -= rhs;
	}
	
	/** Subtract rhs from y. */
	public void yMinusEquals(float rhs) {
		v.y -= rhs;
	}
	
	/** Subtract rhs from z. */
	public void zMinusEquals(float rhs) {
		v.z -= rhs;
	}
	
	/** = a new Vec3f that equals this Vec3f times factor. */
	public Vec3f times(float factor) {
		return new Vec3f(v.x * factor, v.y * factor, v.z * factor);
	}
	
	/** Multiply this Vec3f's components by factor. */
	public void timesEquals(float factor) {
		v.x *= factor;
		v.y *= factor;
		v.z *= factor;
	}
	
	/** Multiply x by rhs. */
	public void xTimesEquals(float rhs) {
		v.x *= rhs;
	}
	
	/** Multiply y by rhs. */
	public void yTimesEquals(float rhs) {
		v.y *= rhs;
	}
	
	/** Multiply z by rhs. */
	public void zTimesEquals(float rhs) {
		v.z *= rhs;
	}
	
	/** = a new Vec3f that equals this Vec3f divided by factor. */
	public Vec3f div(float factor) {
		return new Vec3f(v.x / factor, v.y / factor, v.z / factor);
	}
	
	/** Divide this Vec3f's components by factor. */
	public void divEquals(float factor) {
		v.x /= factor;
		v.y /= factor;
		v.z /= factor;
	}
	
	/** Divide x by rhs. */
	public void xDivEquals(float rhs) {
		v.x /= rhs;
	}
	
	/** Divide y by rhs. */
	public void yDivEquals(float rhs) {
		v.y /= rhs;
	}
	
	/** Divee z by rhs. */
	public void zDivEquals(float rhs) {
		v.z /= rhs;
	}
	
	/** = the magnitude of this Vec3f. */
	public float magnitude() {
		return (float)Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
	}
	
	/** = the magnitude squared of this Vec3f. Useful where you don't want to use a square root. */
	public float magnitudeSquared() {
		return v.x*v.x + v.y*v.y + v.z*v.z;
	}
	
	/** = a new Vec3f of the unit-vector version of this Vec3f. */
	public Vec3f normalize() {
		float mag = this.magnitude();
		return new Vec3f(v.x / mag, v.y / mag, v.z / mag);
	}
	
	/** Sets this Vec3f to equal its unit-vector version. */
	public void normalizeEquals() {
		float mag = this.magnitude();
		v.x /= mag;
		v.y /= mag;
		v.z /= mag;
	}
	
	/** = the dot product of this Vec3f and rhs. */
	public float dot(Vec3f rhs) {
		return v.x * rhs.v.x + v.y * rhs.v.y + v.z * rhs.v.z;
	}	
	
	/** = the angle between this Vec3f and rhs.
	 *    If either is the zero vector, returns NaN. */
	public float angleBetween(Vec3f rhs) {
		return (float)Math.acos(
			this.dot(rhs) / (
				this.magnitude() * 
				rhs.magnitude()
			)
		);
	}
	
	/**
	 * The distance from this Vec3f to rhs.
	 * @param rhs - another Vec3f.
	 * @return - the distance between this Vec3f and rhs.
	 */
	public float distanceBetween(Vec3f rhs) {
		return (float)Math.sqrt(
				(v.x - rhs.v.x) * (v.x - rhs.v.x) + 
				(v.y - rhs.v.y) * (v.y - rhs.v.y) +
				(v.z - rhs.v.z) * (v.z - rhs.v.z)
		);
	}
	
	/**
	 * The distance squared from this Vec3f to rhs.
	 * @param rhs - another Vec3f
	 * @return the distance between this Vec3f and rhs.
	 */
	public float distanceSquaredBetween(Vec3f rhs) {
		return 
				(v.x - rhs.v.x) * (v.x - rhs.v.x) + 
				(v.y - rhs.v.y) * (v.y - rhs.v.y) +
				(v.z - rhs.v.z) * (v.z - rhs.v.z)
		;
	}
	
	/** = a new Vec3f that equals this Vec3f projected onto rhs. */
	public Vec3f projectedOnto(Vec3f rhs) {
		return rhs.times(
			this.dot(rhs) / rhs.magnitudeSquared()
		);
	}
	
	/** = a new Vec3f that equals this Vec3f reflected over rhs. */
	public Vec3f reflectedOver(Vec3f rhs) {
		return rhs.times(
			2 * this.dot(rhs) / rhs.magnitudeSquared()
		).minus(this);
	}
	
	/** = a new Vec3f that equals this Vec3f rotated around inVec by angle theta.
	 * Assumes: right-handed rotation, theta is in radians, axis is of unit length. */
	public Vec3f rotatedAround(Vec3f axis, float theta) {
		Vec3f proj = this.projectedOnto(axis);
		Vec3f perpendicularComponent = this.minus(proj);
		return proj.plus(
				axis.cross(
					perpendicularComponent
				).times(
					(float)Math.sin(theta)
				).plus(
					perpendicularComponent.times(
						(float)Math.cos(theta)
					)
				)
		);
	}
	
	/** = a new Vec3f that equals the cross product of this Vec3f and inVec. */
	public Vec3f cross(Vec3f inVec) {
		return new Vec3f(
			this.v.y * inVec.v.z - this.v.z * inVec.v.y, 
			this.v.z * inVec.v.x - this.v.x * inVec.v.z,
			this.v.x * inVec.v.y - this.v.y * inVec.v.x
		);
	}
	
	/** Set result to be the cross product of this Vec3f with rhs. 
	 *  Beware of using either rhs or this Vec3f as the result - it will screw things up. */
	public void crossSet(Vec3f rhs, Vec3f result) {
		result.set(
			this.v.y * rhs.v.z - this.v.z * rhs.v.y,
			this.v.z * rhs.v.x - this.v.x * rhs.v.z,
			this.v.x * rhs.v.y - this.v.y * rhs.v.x
		);
	}
}