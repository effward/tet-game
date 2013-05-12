package cs5625.deferred.scenegraph;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.media.opengl.glu.GLU;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.misc.OpenGLResourceObject;

/** A tetrahedral mesh object. */
public class TetMesh extends Mesh implements OpenGLResourceObject {
	private ArrayList<Vert> verts;
	private ArrayList<Face> faces;
	private ArrayList<Face> interfaces;
	private ArrayList<Tet> tets;
	private TreeNode root;
	private int kdCutoff = 10;
	private int ids;
	private Point3f mUpperRight, mLowerLeft;
	private float epsilon = 0.00000000001f;
	
	/** A new empty TetMesh. */
	public TetMesh() {
		verts = new ArrayList<Vert>(10);
		faces = new ArrayList<Face>(10);
		tets = new ArrayList<Tet>(10);
		
		mUpperRight = new Point3f();
		mLowerLeft = new Point3f();
		
		ids = 0;

		BlinnPhongMaterial mat = new BlinnPhongMaterial();
		try {
			Texture2D rock = Texture2D.load(GLU.getCurrentGL().getGL2(), "textures/Rock.png");
			mat.setDiffuseTexture(rock);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		mat.setSpecularColor(new Color3f(0.0f, 0.0f, 0.0f));
		setMaterial(mat);
		//mat.setDiffuseTexture(new Texture2D)
		//root = buildKDTree();
		//printKDTree();
	}
	
	private void printKDTree() {
		printHelper(root, 0);
	}
	
	private void printHelper(TreeNode node, int depth) {
		if (depth > 2) return;
		System.out.println("Depth: " + depth);
		System.out.println("UpperRight: " + node.upperRight);
		System.out.println("LowerLeft: " + node.lowerLeft);
		System.out.println("Faces: " + node.faces);
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		if (node.left != null)
			printHelper(node.left, depth+1);
		if (node.right != null)
			printHelper(node.right, depth+1);
		System.out.println("*****************************");
	}
	
	private TreeNode buildKDTree() {
		ArrayList<Face> all_faces = new ArrayList<Face>(interfaces);
		//System.out.println("Starting KD Tree Construction...");
		//System.out.println(all_faces.size() + " faces to be added");
		return kdTreeHelper(all_faces, 0, mUpperRight, mLowerLeft);
	}
	
	private TreeNode kdTreeHelper(ArrayList<Face> f, int depth, Point3f upRight, Point3f lowLeft) {
		if (f.size() <= 0 || depth >= kdCutoff) {
			return null;
		}
		else if (f.size() == 1) {
			TreeNode node = new TreeNode();
			node.upperRight = f.get(0).upperRight;
			node.lowerLeft = f.get(0).lowerLeft;
			node.faces = f;
			//System.out.println("Leaf Created with one face: " + node.faces);
			return node;
		}
		//System.out.println("Depth: " + depth);
		//System.out.println("UpperRight: " + upRight);
		//System.out.println("LowerLeft: " + lowLeft);
		//System.out.println("Number of Faces: " + f.size());
		//System.out.println("Faces: " + f);
		//System.out.println("^^^^^^^^^^^^^^");
		
		int axis;
		float midpoint;
		
		axis = depth % 3;
		Collections.sort(f, new FaceComparator(axis));
		
		int median = f.size() / 2;
		
		if (f.size() % 2 == 0) {
			Point3f left = f.get(median-1).center;
			Point3f right = f.get(median).center;
			//System.out.println("Median calc");
			//System.out.println("Left: " + f.get(median-1).lowerLeft + ", " + left + ", " + f.get(median-1).upperRight);
			//System.out.println("Right: " + f.get(median).lowerLeft + ", " + right + ", " + f.get(median).upperRight);

			if (axis == 0)
				midpoint = (left.x + right.x) / 2f;
			else if (axis == 1)
				midpoint = (left.y + right.y) / 2f;
			else
				midpoint = (left.z + right.z) / 2f;
		}
		else {
			if (axis == 0)
				midpoint = f.get(median).center.x;
			else if (axis == 1)
				midpoint = f.get(median).center.y;
			else
				midpoint = f.get(median).center.z; 
		}
		
		TreeNode node = new TreeNode();
		node.upperRight = upRight;
		node.lowerLeft = lowLeft;
		ArrayList<Face> leftList = new ArrayList<Face>();
		ArrayList<Face> rightList = new ArrayList<Face>();
		Point3f leftUpRight, rightLowLeft;
		if (axis == 0) {
			leftUpRight = new Point3f(midpoint, upRight.y, upRight.z);
			rightLowLeft = new Point3f(midpoint, lowLeft.y, lowLeft.z);
		}
		else if (axis == 1){
			leftUpRight = new Point3f(upRight.x, midpoint, upRight.z);
			rightLowLeft = new Point3f(lowLeft.x, midpoint, lowLeft.z);
		}
		else {
			leftUpRight = new Point3f(upRight.x, upRight.y, midpoint);
			rightLowLeft = new Point3f(lowLeft.x, lowLeft.y, midpoint);
		}
		
		float volume = (upRight.x - lowLeft.x) * (upRight.y - lowLeft.y)* (upRight.z - lowLeft.z); 
		boolean smallerFound = false;
		
		for(int i = 0; i < f.size(); i++) {
			Point3f left = f.get(i).lowerLeft;
			Point3f right = f.get(i).upperRight;
			Point3f center = f.get(i).center;
			float faceVolume = (right.x - left.x) * (right.y - left.y) * (right.z - left.z); 
			if (faceVolume < volume)
				smallerFound = true;
			if (axis == 0) {
				if (center.x <= midpoint)
					leftList.add(f.get(i));
				else
					rightList.add(f.get(i));
			}
			else if (axis == 1) {
				if (center.y <= midpoint)
					leftList.add(f.get(i));
				else
					rightList.add(f.get(i));
			}
			else {
				if (center.z <= midpoint)
					leftList.add(f.get(i));
				else
					rightList.add(f.get(i));
			}
		}
		if (smallerFound) {
			//System.out.println("Creating Children with faces:");
			//System.out.println("Left: " + leftList);
			//System.out.println("Right: " + rightList);
			node.left = kdTreeHelper(leftList, depth+1, leftUpRight, lowLeft);
			node.right = kdTreeHelper(rightList, depth+1, upRight, rightLowLeft);
		}
		
		if (node.left == null && node.right == null) { //this is a leaf node
			node.faces = f;
			//System.out.println("Leaf created containing faces: " + f);
		}
		//System.out.println("********************************");
		return node;
	}
	
	
	/** Set the vertices of this TetMesh to the given list of verts. */
	public void setVerts(ArrayList<Vector3f> verts) {
		this.verts = new ArrayList<Vert>(verts.size());
		this.mVertexData = Buffers.newDirectFloatBuffer(verts.size() * 3);
		int i = 0;
		float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE, maxZ = Float.MIN_VALUE;
		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
		for (Vector3f v : verts) {
			this.verts.add(new Vert(v));
			mVertexData.put(i++, v.x);
			mVertexData.put(i++, v.y);
			mVertexData.put(i++, v.z);
			maxX = Math.max(maxX, v.x);
			maxY = Math.max(maxY, v.y);
			maxZ = Math.max(maxZ, v.z);
			minX = Math.min(minX, v.x);
			minY = Math.min(minY, v.y);
			minZ = Math.min(minZ, v.z);
		}
		mUpperRight = new Point3f(maxX, maxY, maxZ);
		mLowerLeft = new Point3f(minX, minY, minZ);
	}
	
	/** Set the tetrahedrons of this TetMesh to the given list of tets 
	 * (in v0, v1, v2, v3 int order). */
	public void setTets(ArrayList<Integer> tets) {
		System.out.println("setting tets"); //TODO remove
		this.tets = new ArrayList<Tet>(tets.size() / 4);
		for (int i = 0; i < tets.size() / 4; i++) {
			Vert v0 = verts.get(tets.get(4 * i));
			Vert v1 = verts.get(tets.get(4 * i + 1));
			Vert v2 = verts.get(tets.get(4 * i + 2));
			Vert v3 = verts.get(tets.get(4 * i + 3));
			
			Tet t = new Tet(v0, v1, v2, v3, 1);
			this.tets.add(t);
		}
		createSurface();
		root = buildKDTree();
		//printKDTree();
		
	}
	
	private void computeInterface() {
		interfaces = new ArrayList<Face>(10);
		for(Face f: faces) {
			if (f.t1 == null || f.t0.mat != f.t1.mat) { // change to account for transparency
				interfaces.add(f);
			}
		}
	}
	
	/** Set this TetMesh's polygons to be present at any interface between
	 * tetrahedrons with different materials (where at least one is non-opaque), or
	 * at boundaries where a Face has only one adjacent Tet.
	 */
	private void createSurface() {
		System.out.println("creating surface..."); //TODO remove
		//Go through and find all faces which are interfaces between different materials
		//or are boundaries at the edge of the tetmesh.
		/*
		ArrayList<Face> interFaces = new ArrayList<Face>(10);
		for (Face f: faces) {
			if (f.t1 == null || f.t0.mat != f.t1.mat) { //change to account for transparency..
				interFaces.add(f);
			}
		}*/
		computeInterface();
		
		System.out.println("Faces: " + faces.size()); //TODO remove
		System.out.println("Shown faces: " + interfaces.size()); //TODO remove
		
		//Copy these into an actual array now we know the number of faces required.
		int[] arr = new int[interfaces.size() * 3];
		
		//simultaneously compute normals.
		Vector3f[] norms = new Vector3f[verts.size()];
		float[] normArr = new float[verts.size() * 3];
		
		float[] texArr = new float[verts.size() * 2];
		
		for (int i = 0; i < norms.length; i++) {
			norms[i] = new Vector3f(0.0f, 0.0f, 0.0f); //zero normal
		}
		
		//And texture coords
		for (int i = 0; i < verts.size(); i++) {
			Vector3f pos = verts.get(i).pos;
			texArr[2 * i] = (float)(Math.atan2(pos.y, pos.x) / (2*Math.PI) * 10);
			texArr[2 * i + 1] = (float)(Math.atan2(pos.z, Math.sqrt(pos.x * pos.x + pos.y * pos.y)) / (2*Math.PI) * 10);
		}
		
		int v0, v1, v2;
		Vector3f d0 = new Vector3f(), d1 = new Vector3f();
		for (int j = 0; j < interfaces.size(); j++) {
			Face f = interfaces.get(j);
			v0 = verts.indexOf(f.v0);
			v1 = verts.indexOf(f.v1);
			v2 = verts.indexOf(f.v2);
			arr[3 * j] = v0;
			arr[3 * j + 1] = v1;
			arr[3 * j + 2] = v2;
			
			d0.sub(f.v1.pos, f.v0.pos);
			d1.sub(f.v2.pos, f.v0.pos);
			d0.cross(d1, d0);
			d0.normalize();
			
			norms[v0].add(d0);
			norms[v1].add(d0);
			norms[v2].add(d0);
			//may need to play with winding order..
		}
		
		//Go through and normalize normals (sums of adjacent edges).
		for (int i = 0; i < norms.length; i++) {
			//norms[i].set(verts.get(i).pos);
			//norms[i].normalize();
			
			
			if (norms[i].lengthSquared() == 0) {
				norms[i].set(0.0f, 0.0f, 1.0f); // vert not in any interface edges
			}
			else {
				norms[i].normalize();
			}
			
			normArr[i * 3 + 0] = norms[i].x;
			normArr[i * 3 + 1] = norms[i].y;
			normArr[i * 3 + 2] = norms[i].z;
			
		}
		
		
		//And store in the mesh's polygon and normal data buffer.
		mPolygonData = IntBuffer.wrap(arr);
		mNormalData = FloatBuffer.wrap(normArr);
		mTexCoordData = FloatBuffer.wrap(texArr);
		
	}
	
	/** Discovers if this TetMesh contains a face with the same verts as f. 
	 * 
	 * @param f - the Face to check.
	 * @return the Face with the same verts, or null if one does not exist.
	 */
	public Face hasFace(Face f) {	
		//int i = 0;
		ArrayList<Face> check; //the smallest face list of the three verts
		check = (f.v0.faces.size() < f.v1.faces.size() ? 
				(f.v0.faces.size() < f.v2.faces.size() ? f.v0.faces : f.v2.faces) : 
				(f.v1.faces.size() < f.v2.faces.size() ? f.v1.faces : f.v2.faces)
		);
		
		for (Face a : check) {
			//i++; System.out.println(i);
			if (a.equals(f)) return a;
		}
		return null;
	}
	
	/** Add Face f if it is not already listed. 
	 * 
	 * @param f - the Face to check.
	 * @return f if it was not already in faces, or the old Face if it was.
	 */
	public Face addFace (Face f) {
		Face a = hasFace(f);
		if (a == null) {
			faces.add(f);
			f.v0.addFace(f);
			f.v1.addFace(f);
			f.v2.addFace(f);
			f.id = ids++;
			return f;
		}
		else {
			return a;
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
		public ArrayList<Face> faces;
		
		/** Constructor.
		 * 
		 * @param pos - the position of this Vert.
		 */
		public Vert(Vector3f pos) {
			this.pos = pos;
			faces = new ArrayList<Face>(1);
		}
		
		/** Check Vert equality based on position.
		 * 
		 * @param v - the Vert to compare this Vert to.
		 * @return true if this Vert has the same position as v.
		 */
		public boolean equals(Vert v) {
			return pos.equals(v.pos);
		}
		
		public void addFace(Face f) {
			faces.add(f);
		}
	}
	
	private class TreeNode {
		public Point3f upperRight, lowerLeft;
		public TreeNode left;
		public TreeNode right;
		public ArrayList<Face> faces;

	}
	
	private class FaceComparator implements Comparator<Face> {
		private int axis;
		
		public FaceComparator(int a) {
			if (a < 0 || a > 2)
				axis = 0;
			else
				axis = a;
		}
		@Override
		public int compare(Face o1, Face o2) {
			if (axis == 0) {
				float x1 =o1.center.x;
				float x2 = o2.center.x;
				if (x1 < x2)
					return -1;
				else if (x1 > x2)
					return 1;
				else
					return 0;
			}
			else if (axis == 1) {
				float y1 = o1.center.y;
				float y2 = o2.center.y;
				if (y1 < y2)
					return -1;
				else if (y1 > y2)
					return 1;
				else
					return 0;
			}
			else if (axis == 2) {
				float z1 = o1.center.z;
				float z2 = o2.center.z;
				if (z1 < z2)
					return -1;
				else if (z1 > z2)
					return 1;
				else
					return 0;
			}
			return 0;
		}
		
	}
	
	/** A tetrahedral face. */
	private class Face {
		public Vert v0, v1, v2;
		public Tet t0, t1;
		//public Vector3f center;
		public Point3f upperRight, lowerLeft, center;
		public int id;
		
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
			float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE, maxZ = Float.MIN_VALUE;
			float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
			maxX = Math.max(maxX, v0.pos.x);
			maxY = Math.max(maxY, v0.pos.y);
			maxZ = Math.max(maxZ, v0.pos.z);
			minX = Math.min(minX, v0.pos.x);
			minY = Math.min(minY, v0.pos.y);
			minZ = Math.min(minZ, v0.pos.z);
			
			maxX = Math.max(maxX, v1.pos.x);
			maxY = Math.max(maxY, v1.pos.y);
			maxZ = Math.max(maxZ, v1.pos.z);
			minX = Math.min(minX, v1.pos.x);
			minY = Math.min(minY, v1.pos.y);
			minZ = Math.min(minZ, v1.pos.z);
			
			maxX = Math.max(maxX, v2.pos.x);
			maxY = Math.max(maxY, v2.pos.y);
			maxZ = Math.max(maxZ, v2.pos.z);
			minX = Math.min(minX, v2.pos.x);
			minY = Math.min(minY, v2.pos.y);
			minZ = Math.min(minZ, v2.pos.z);
			
			this.upperRight = new Point3f(maxX, maxY, maxZ);
			this.lowerLeft = new Point3f(minX, minY, minZ);
			this.center = new Point3f();
			this.center.add(this.upperRight, this.lowerLeft);
			this.center.scale(0.5f);
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			String NEW_LINE = System.getProperty("line.separator");
			result.append(this.id);
			return result.toString();
		}
		
		/** Set one of the two tetrahedra of this Face to t. Does nothing if both tets are already filled.
		 * 
		 * @param t - the Tet to set.
		 */
		public void setTet(Tet t) {
			if (t0 == null) {t0 = t;}
			else if (t1 == null) {t1 = t;}
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
		public Tet(Vert v0, Vert v1, Vert v2, Vert v3, int mat) {
			this.v0 = v0;
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			
			this.mat = mat;
			
			//Create/find faces that go with the given verts
			Face f0 = addFace(new Face (v0, v1, v2));
			Face f1 = addFace(new Face (v0, v3, v1));
			Face f2 = addFace(new Face (v0, v2, v3));
			Face f3 = addFace(new Face (v3, v2, v1));
			
			this.f0 = f0;
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			
			//Set one of the two tets each face stores to be this new tet.
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
