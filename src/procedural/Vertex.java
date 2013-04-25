package procedural;

import java.util.ArrayList;

/** Represents a triangle vertex. */
public class Vertex {
	public Vec3f dir;
	public Vec3f pt;
	public ArrayList<Vertex> neighbors;
	public Vertex lower;
	public float mag;
	
	/**
	 * Create a new Vertex at the given position. 
	 * @param pt - the position.
	 */
	public Vertex(Vec3f pt) {
		this.pt = pt;
		this.dir = pt.normalize();
		this.mag = pt.magnitude();
		this.neighbors = new ArrayList<Vertex>(5);
		this.lower = null;
	}
	
	/**
	 * Create a new Vertex with the given direction, magnitude, and neighbors.
	 * @param dir - the direction.
	 * @param mag - the magnitude.
	 * @param neighbors - the neighbors array.
	 */
	public Vertex(Vec3f dir, float mag, ArrayList<Vertex> neighbors, Vertex lower) {
		this.pt = dir.times(mag);
		this.dir = dir;
		this.mag = mag;
		this.neighbors = neighbors;
		this.lower = lower;
	}

	public boolean equals(Object o) {
		if (o instanceof Vertex) {
			return this.pt.equals(((Vertex)o).pt);
		}
		else {
			return false;
		}
	}
	
	/**
	 * Produces a near-copy of this Vertex: 
	 * The neighbors will still point to the original Vertex's neighbors.
	 * @return a near-copy of this Vertex.
	 */
	public Vertex copy() {
		ArrayList<Vertex> oldneighbors = new ArrayList<Vertex>(neighbors.size());
		for (Vertex n: neighbors) {
			oldneighbors.add(n);
		}
		return new Vertex(this.dir.copy(), this.mag, oldneighbors, lower);
	}
}
