package procedural;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import cs5625.deferred.scenegraph.Geometry;


/** Represents a set of heights, their points, and accompanying mesh. */
public class Heightmesh extends Geometry {
	private ArrayList<Vertex> verts;
	private ArrayList<Triangle> triangles;
	private int[] frequencyMask;
	private int maxSmooth = 0;
	
	/** Create a new Heightmesh. */
	public Heightmesh() {
		verts = new ArrayList<Vertex>();
		triangles = new ArrayList<Triangle>();
	}
	
	/**
	 * Create a new Heightmesh with the given points and triangles.
	 * @param pts
	 * @param tris
	 */
	public Heightmesh(ArrayList<Vertex> verts, ArrayList<Triangle> tris) {
		this.verts = verts;
		this.triangles = tris;
	}
	
	/** Add a vertex to the Heightmesh. Note, the vertex itself is added, not a copy, so don't change the point later. */
	public void addVert(Vertex vert) {
		verts.add(vert);
	}
	
	/** Add a triangle to the Heightmesh. Make sure p0, p1, and p2 are in the vertex list already. */
	public void addTriangle(int p0, int p1, int p2) {
		triangles.add(new Triangle(p0, p1, p2));
		if (!verts.get(p0).neighbors.contains(verts.get(p1))) {
			verts.get(p0).neighbors.add(verts.get(p1));
		}
		if (!verts.get(p0).neighbors.contains(verts.get(p2))) {
			verts.get(p0).neighbors.add(verts.get(p2));
		}
		if (!verts.get(p1).neighbors.contains(verts.get(p0))) {
			verts.get(p1).neighbors.add(verts.get(p0));
		}
		if (!verts.get(p1).neighbors.contains(verts.get(p2))) {
			verts.get(p1).neighbors.add(verts.get(p2));
		}
		if (!verts.get(p2).neighbors.contains(verts.get(p1))) {
			verts.get(p2).neighbors.add(verts.get(p1));
		}
		if (!verts.get(p2).neighbors.contains(verts.get(p0))) {
			verts.get(p2).neighbors.add(verts.get(p0));
		}
	}
	
	/** Remove a triangle from the Heightmesh. Make sure t is a triangle in the mesh already. */
	public void removeTriangle(Triangle t) {
		triangles.remove(t);
	}
	
	/** Return the list of vertices for this Heightmesh. */
	public ArrayList<Vertex> getVerts() {
		return verts;
	}
	
	/** Return the list of triangles for this Heightmesh. */
	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}
	
	/**
	 * Set the points of this Heightmesh. Make sure you have as many points as are needed by existing triangles.
	 * @param points
	 */
	public void setVerts(ArrayList<Vertex> verts) {
		this.verts = verts;
	}
	
	/**
	 * Set the triangles of this Heightmesh. Make sure no triangle references a point that doesn't exist.
	 * @param triangles
	 */
	public void setTriangles(ArrayList<Triangle> triangles) {
		this.triangles = triangles;
	}
	
	/** Clear the mesh. */
	public void clear() {
		verts.clear();
		triangles.clear();
	}

	/**
	 * Semi-copy.
	 */
	public Heightmesh copy() {
		ArrayList<Vertex> newVerts = new ArrayList<Vertex>(verts.size());
		ArrayList<Triangle> newTriangles = new ArrayList<Triangle>(triangles.size());
		
		for (Vertex v: verts) {
			newVerts.add(v.copy());
			newVerts.get(newVerts.size() - 1).lower = v; //assuming copy is being used for subdivisions.
		}
		//Modify neighbor arrays to point to the newly-created verts instead of old ones.
		for (Vertex v: verts) {
			ArrayList<Vertex> newneighbors = new ArrayList<Vertex>(v.neighbors.size());
			for (Vertex n: v.neighbors) {
				newneighbors.add(newVerts.get(verts.indexOf(n)));
			}
		}
		
		for (Triangle t: triangles) {
			newTriangles.add(t.copy());
		}
		
		return new Heightmesh(newVerts, newTriangles);
	}
	
	/** Turns this Globe into an icosahedron of point-radius radius. Clears pre-existing geometry. */
	public void createIcosa() {	
		verts.clear();
		triangles.clear();
		
		//Create coordinate values.
		float si = (float)(1 + Math.sqrt(5)) / 2;
		float a = si / (float)Math.sqrt(si*si + 1);
		float b = 1 /(float)Math.sqrt(si*si + 1);
		
		//Create all 12 points.
		Vec3f p0 = new Vec3f( 0,  b,  a);
		Vec3f p1 = new Vec3f( 0,  b, -a);
		Vec3f p2 = new Vec3f( 0, -b,  a);
		Vec3f p3 = new Vec3f( 0, -b, -a);
		Vec3f p4 = new Vec3f( b,  a,  0);
		Vec3f p5 = new Vec3f( b, -a,  0);
		Vec3f p6 = new Vec3f(-b,  a,  0);
		Vec3f p7 = new Vec3f(-b, -a,  0);
		Vec3f p8 = new Vec3f( a,  0,  b);
		Vec3f p9 = new Vec3f(-a,  0,  b);
		Vec3f p10 = new Vec3f( a,  0, -b);
		Vec3f p11 = new Vec3f(-a,  0, -b);
		
		//Add points to lowestMesh.
		verts.add(new Vertex(p0)); 
		verts.add(new Vertex(p1)); 
		verts.add(new Vertex(p2)); 
		verts.add(new Vertex(p3)); 
		verts.add(new Vertex(p4)); 
		verts.add(new Vertex(p5)); 
		verts.add(new Vertex(p6)); 
		verts.add(new Vertex(p7)); 
		verts.add(new Vertex(p8)); 
		verts.add(new Vertex(p9)); 
		verts.add(new Vertex(p10)); 
		verts.add(new Vertex(p11)); 
		
		//Create and add all 20 triangles to lowestMesh.
		addTriangle(0, 4, 8);
		addTriangle(0, 8, 2);
		addTriangle(0, 2, 9);
		addTriangle(0, 9, 6);
		addTriangle(0, 6, 4);
		
		addTriangle(2, 8, 5);
		addTriangle(7, 2, 5);
		addTriangle(9, 2, 7);
		addTriangle(11, 9, 7);
		addTriangle(6, 9, 11);
		addTriangle(1, 6, 11);
		addTriangle(4, 6, 1);
		addTriangle(10, 4, 1);
		addTriangle(8, 4, 10);
		addTriangle(5, 8, 10);
		
		addTriangle(3, 10, 1);
		addTriangle(3, 5, 10);
		addTriangle(3, 7, 5);
		addTriangle(3, 11, 7);
		addTriangle(3, 1, 11);	
	}
	
	/**
	 * Subdivide the triangles of this Heightmesh.
	 * @param times - the number of times to do this.
	 * @param norm - whether to normalize at each step.
	 */
	public void subdivide(int times, boolean norm) {
		Vertex middle01, middle12, middle20;
		int index01, index12, index20;
		ArrayList<Triangle> oldTriangles;

		for (int i = 0; i < times; i++) {
			//Wipe existing triangles, since they all get replicated times four.
			oldTriangles = triangles;
			triangles = new ArrayList<Triangle>(triangles.size() * 4);
			
			//Wipe existing adjacency values for each vertex, since they all change.
			for (Vertex v: verts) {
				v.neighbors.clear();
			}
			
			//Subdivide each old triangle
			for (Triangle t: oldTriangles) {
				//Find middle of each pair of points, normalized to unit radius
				
				middle01 = new Vertex(verts.get(t.v0).pt.plus(verts.get(t.v1).pt).times(0.5f));
				middle12 = new Vertex(verts.get(t.v1).pt.plus(verts.get(t.v2).pt).times(0.5f));
				middle20 = new Vertex(verts.get(t.v2).pt.plus(verts.get(t.v0).pt).times(0.5f));
				
				if (norm) {
					middle01.mag = 1.0f;
					middle01.pt.normalizeEquals();
					middle12.mag = 1.0f;
					middle12.pt.normalizeEquals();
					middle20.mag = 1.0f;
					middle20.pt.normalizeEquals();
				}
				
				//Check for midpoints already in points list
				index01 = verts.indexOf(middle01);
				index12 = verts.indexOf(middle12);
				index20 = verts.indexOf(middle20);
				
				//If midpoints not in list, add them
				if (index01 == -1) {
					index01 = verts.size();
					verts.add(middle01);
				}
				if (index12 == -1) {
					index12 = verts.size();
					verts.add(middle12);
				}
				if (index20 == -1) {
					index20 = verts.size();
					verts.add(middle20);
				}
				
				//Now replace current triangle with four subdivided triangles.
				addTriangle(t.v0, index01, index20); //top
				addTriangle(index01, t.v1, index12); //lower right
				addTriangle(index12, t.v2, index20); //lower left
				addTriangle(index01, index12, index20); //middle
			}
		}
	}
	
	/**
	 * Scale this Heightmesh by the given factor.
	 * @param factor - a scaling factor.
	 */
	public void scale(float factor, float intercept) {
		for (Vertex v: verts) {
			v.mag = (v.mag - intercept) * factor + intercept;
			v.pt.set(v.dir);
			v.pt.timesEquals(v.mag);
			Vertex lower = v.lower;
			while(lower != null) {
				lower.mag = (lower.mag - intercept) * factor + intercept;
				lower.pt.set(lower.dir);
				lower.pt.timesEquals(lower.mag);
				lower = lower.lower;
			}
		}
	}
	
	/**
	 * Randomize this Heightmesh's terrain between minimum and maximum values.
	 * @param min - minimum radius
	 * @param max - maximum radius
	 */
	public void randomize(float min, float max) {
		for (Vertex v: verts) {
			randomizePoint(v, min, max);
		}
	}
	
	/**
	 * Randomize this HeightMesh's terrain using the old height as setting the randomization range.
	 * @param min - the minimum possible height
	 * @param max - the maximum possible height
	 */
	public void randomizeRelative(float min, float max) {
		float oldmin = Float.POSITIVE_INFINITY;
		float oldmax = Float.NEGATIVE_INFINITY;
		float half = (max + min) / 2.0f;
	
		for (Vertex v : verts) {
			if (v.mag < oldmin) oldmin = v.mag;
			if (v.mag > oldmax) oldmax = v.mag;
		}
		
		for (Vertex v: verts) {
			float range = (v.mag - oldmin) / (oldmax - oldmin) * (max - min);
			float lower = half - range / 2;
			float upper = half + range / 2;
			randomizePoint(v, lower, upper);
		}
	}
	
	/**
	 * Randomize the given vertex.
	 * @param v - the vertex to randomize
	 * @param min - the minimum value
	 * @param max - the maximum value
	 */
	private void randomizePoint(Vertex v, float min, float max) {
		float diff = max - min;
		v.mag = (float)(Math.random() * diff) + min;
		v.pt.set(v.dir);
		v.pt.timesEquals(v.mag);
		
		Vertex lower = v.lower;
		while(lower != null) {
			lower.mag = v.mag;
			lower.pt.set(lower.dir);
			lower.pt.timesEquals(lower.mag);
			lower = lower.lower;
		}
	}
	
	/** Save some iteration count info... */
	public void saveFrequency(int min, int max) {
		float oldmin = Float.POSITIVE_INFINITY;
		float oldmax = Float.NEGATIVE_INFINITY;
		maxSmooth = 0;
		
		for (Vertex v : verts) {
			if (v.mag < oldmin) oldmin = v.mag;
			if (v.mag > oldmax) oldmax = v.mag;
		}
		//float diff = (float)(max - min) / (oldmax - oldmin);
		frequencyMask = new int[verts.size()];
		
		for (int i = 0; i < verts.size(); i++) {
			//frequencyMask[i] = (int)((verts.get(i).mag - oldmin) * diff) + min;
			frequencyMask[i] = (int)(logistic(verts.get(i).mag, oldmin, oldmax, 5.0f) * (float)(max - min)) + min;
			if (frequencyMask[i] > maxSmooth) maxSmooth = frequencyMask[i];
		}
	}
	
	private float logistic(float in, float min, float max, float form) {
		float lform = (float)(1.0f / (1.0f + Math.pow(Math.E, -form)));
		
		float l = (float)(1.0f / (1.0f + Math.pow(Math.E, -((in - min) * form / (max - min) - form / 2.0f))));
		return (l - 0.5f) * (0.5f / (0.5f - lform)) + 0.5f;
	}
	
	public void erode(int its, float ratio) {
		float maxslope = 0.0f, slope;
		Vertex maxVert, v;
		for (int it = 0; it < its; it++) {
			
			for (Vertex start: verts) {
				v = start;
				
				do {
					maxslope = 0;
					maxVert = null;
					for (Vertex n: v.neighbors) {
						slope = v.mag - n.mag;
						if (slope > maxslope) {
							maxslope = slope;
							maxVert = n;
						}
					}
					if (maxslope > 0) {
						v.mag -= maxslope * ratio / 2;
						v.pt.normalizeEquals();
						v.pt.timesEquals(v.mag);
						maxVert.mag += maxslope * ratio / 2;
						maxVert.pt.normalizeEquals();
						maxVert.pt.timesEquals(maxVert.mag);
						v = maxVert;
					}
					

				} while (maxslope > 0);
			}
		}
		
	}
	
	/**
	 * Smooth this Globe's terrain.
	 * @param its - number of iterations to smooth (if -1, uses saved frequency).
	 */
	public void smooth(int its) {
		float[] shadow = new float[verts.size()];
		
		float tot;
		
		
		if (its == -1 && verts.size() == frequencyMask.length) {
			for (int it = 0; it < maxSmooth; it++) {
				for (int j = 0; j < verts.size(); j++) {
					if (frequencyMask[j] >= it) {
						Vertex v = verts.get(j);
						
						tot = v.mag;
						for (Vertex n: v.neighbors) {
							tot += n.mag;
						}
						shadow[j] = tot / (v.neighbors.size() + 1);
					}
				}
				for (int j = 0; j < verts.size(); j++) {
					if (frequencyMask[j] >= it) {
						Vertex v = verts.get(j);
					
						v.mag = shadow[j];
						v.pt.set(v.dir);
						v.pt.timesEquals(v.mag);
						Vertex lower = v.lower;
						while(lower != null) {
							lower.mag = shadow[j];
							lower.pt.set(lower.dir);
							lower.pt.timesEquals(lower.mag);
							lower = lower.lower;
						}
					}
				}
			}
			
		}
		else {
			if (its < 0) its = 1;
			int i = 0;
			for (int it = 0; it < its; it++) {
				for (Vertex v: verts) {
					tot = v.mag;
					for (Vertex n: v.neighbors) {
						tot += n.mag;
					}
					shadow[i] = tot / (v.neighbors.size() + 1);
					i++;
				}
				i = 0;
				for (Vertex v: verts) {
					v.mag = shadow[i];
					v.pt.set(v.dir);
					v.pt.timesEquals(v.mag);
					Vertex lower = v.lower;
					while(lower != null) {
						lower.mag = shadow[i];
						lower.pt.set(lower.dir);
						lower.pt.timesEquals(lower.mag);
						lower = lower.lower;
					}
					i++;
				}
				i = 0;
			}
		}
	}
}
