package cs5625.deferred.scenegraph;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.misc.OpenGLResourceObject;

public class TetMesh extends Mesh implements OpenGLResourceObject {
	private ArrayList<Vert> verts;
	private ArrayList<Face> faces;
	private ArrayList<Tet> tets;
	
	/** A new empty TetMesh. */
	public TetMesh() {
		verts = new ArrayList<Vert>(10);
		faces = new ArrayList<Face>(10);
		tets = new ArrayList<Tet>(10);
	}
	
	/** Set the vertices of this TetMesh to the given list of verts. */
	public void setVerts(ArrayList<Vector3f> verts) {
		this.verts = new ArrayList<Vert>(verts.size());
		this.mVertexData = Buffers.newDirectFloatBuffer(verts.size() * 3);
		
		for (Vector3f v : verts) {
			this.verts.add(new Vert(v));
			mVertexData.put(v.x);
			mVertexData.put(v.y);
			mVertexData.put(v.z);
		}
	}
	
	/** Set the tetrahedrons of this TetMesh to the given list of tets 
	 * (in v0, v1, v2, v3 int order). */
	public void setTets(ArrayList<Integer> tets) {
		this.tets = new ArrayList<Tet>(tets.size() / 4);
		for (int i = 0; i < tets.size() / 4; i++) {
			Vert v0 = verts.get(tets.get(4 * i));
			Vert v1 = verts.get(tets.get(4 * i + 1));
			Vert v2 = verts.get(tets.get(4 * i + 2));
			Vert v3 = verts.get(tets.get(4 * i + 3));
			
			Face f0 = addFace(new Face (v0, v1, v2));
			Face f1 = addFace(new Face (v0, v3, v1));
			Face f2 = addFace(new Face (v0, v2, v3));
			Face f3 = addFace(new Face (v3, v2, v1));
			
			Tet t = new Tet(v0, v1, v2, v3, f0, f1, f2, f3, 1);
			
			//set one of the tets the faces are attached to to t.
			f0.setTet(t);
			f1.setTet(t);
			f2.setTet(t);
			f3.setTet(t);
		}
		
		createSurface();
	}
	
	/** Set this TetMesh's polygons to be present at any interface between
	 * tetrahedrons with different materials (where at least one is non-opaque), or
	 * at boundaries where a Face has only one adjacent Tet.
	 */
	private void createSurface() {
		ArrayList<Face> interFaces = new ArrayList<Face>(10);
		for (Face f: faces) {
			if (f.t1 == null || f.t0.mat != f.t1.mat) { //change to account for transparency..
				interFaces.add(f);
			}
		}
		//copy into an actual array now we know the number of faces required.
		int[] arr = new int[interFaces.size() * 3];
		for (int j = 0; j < interFaces.size(); j++) {
			Face f = interFaces.get(j);
			arr[3 * j] = faces.indexOf(f.v0);
			arr[3 * j + 1] = faces.indexOf(f.v1);
			arr[3 * j + 2] = faces.indexOf(f.v2);
			//may need to play with winding order..
		}
		mPolygonData = IntBuffer.wrap(arr);
	}
	
	/** Discovers if this TetMesh contains a face with the same verts as f. 
	 * 
	 * @param f - the Face to check.
	 * @return the index of a face with the same verts, or -1 if one does not exist.
	 */
	public int hasFace(Face f) {
		return faces.indexOf(f);
	}
	
	/** Add Face f if it is not already listed. 
	 * 
	 * @param f - the Face to check.
	 * @return f if it was not already in faces, or the old Face if it was.
	 */
	public Face addFace (Face f) {
		int i = hasFace(f);
		if (i == -1) {
			faces.add(f);
			return f;
		}
		else {
			return faces.get(i);
		}
		
	}
	
	/** Discovers if this TetMesh contains a tetrahedron with the same verts as t.
	 * 
	 * @param t - the Tet to check.
	 * @return the index of a Tet with the same verts, or -1 if one does not exist.
	 */
	public int hasTet(Tet t) {
		return tets.indexOf(t);
	}

	@Override
	public int getVerticesPerPolygon() {
		return 3;
	}

	@Override
	public FloatBuffer calculateTangentVectors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mesh clone() {
		TetMesh m = new TetMesh();
		
		return null;
	}
	
	/** A vertex. */
	private class Vert {
		public Vector3f pos;
		
		/** Constructor.
		 * 
		 * @param pos - the position of this Vert.
		 */
		public Vert(Vector3f pos) {
			this.pos = pos;
		}
		
		/** Check Vert equality based on position.
		 * 
		 * @param v - the Vert to compare this Vert to.
		 * @return true if this Vert has the same position as v.
		 */
		public boolean equals(Vert v) {
			return pos.equals(v.pos);
		}
	}
	
	/** A tetrahedral face. */
	private class Face {
		public Vert v0, v1, v2;
		public Tet t0, t1;
		
		/** Constructor.
		 * 
		 * @param v0 - the first vertex
		 * @param v1 - the second vertex
		 * @param v2 - the third vertex
		 */
		public Face(Vert v0, Vert v1, Vert v2) {
			this.v0 = v0;
			this.v1 = v1;
			this.v2 = v2;
		}
		
		/** Set one of the two tetrahedra of this Face to t. Does nothing of both tets are already filled.
		 * 
		 * @param t - the Tet to set.
		 */
		public void setTet(Tet t) {
			if (t0 == null) t0 = t;
			else if (t1 == null) t1 = t;
			else {} //tried to set a tet on a face that already had its tets filled.
		}
		
		/** Check face equality based on vertex equality.
		 * 
		 * @param f - the Face to compare to this Face.
		 * @return true if the three vertices are the same (in any order) or false otherwise.
		 */
		public boolean equals(Face f) {
			return 
				((this.v0 == f.v0) || (this.v0 == f.v1) || (this.v0 == f.v2)) &&
				((this.v1 == f.v0) || (this.v1 == f.v1) || (this.v1 == f.v2)) &&
				((this.v2 == f.v0) || (this.v2 == f.v1) || (this.v2 == f.v2));
		}
	}
	
	/** A tetrahedron. */
	private class Tet {
		public Vert v0, v1, v2, v3;
		public Face f0, f1, f2, f3;
		
		public int mat = 0; //tet material
		
		
		/** Constructor. 
		 * 
		 * @param v0 - first vertex
		 * @param v1 - second vertex
		 * @param v2 - third vertex 
		 * @param v3 - fourth vertex
		 * @param f0 - first face
		 * @param f1 - second face
		 * @param f2 - third face 
		 * @param f3 - fourth face
		 */
		public Tet(Vert v0, Vert v1, Vert v2, Vert v3, Face f0, Face f1, Face f2, Face f3, int mat) {
			this.v0 = v0;
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.f0 = f0;
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.mat = mat;
			
			f0.setTet(this);
			f1.setTet(this);
			f2.setTet(this);
			f3.setTet(this);
		}
		
		public boolean equals (Tet t) {
			return 
				((this.v0 == t.v0) || (this.v0 == t.v1) || (this.v0 == t.v2) || (this.v0 == t.v3)) &&
				((this.v1 == t.v0) || (this.v1 == t.v1) || (this.v1 == t.v2) || (this.v1 == t.v3)) &&
				((this.v2 == t.v0) || (this.v2 == t.v1) || (this.v2 == t.v2) || (this.v2 == t.v3)) &&
				((this.v3 == t.v0) || (this.v3 == t.v1) || (this.v3 == t.v2) || (this.v3 == t.v3));
		}
	}
}
