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
import javax.vecmath.GVector;
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
	private int kdCutoff = 100;
	private Point3f mUpperRight, mLowerLeft;
	private float epsilon = 0.00000000001f;
	
	/** A new empty TetMesh. */
	public TetMesh() {
		verts = new ArrayList<Vert>(10);
		faces = new ArrayList<Face>(10);
		tets = new ArrayList<Tet>(10);
		
		mUpperRight = new Point3f();
		mLowerLeft = new Point3f();
		
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
			//System.out.println("Faces empty or cutoff reached");
			return null;
		}
		else if (f.size() == 1) {
			TreeNode node = new TreeNode();
			node.upperRight = upRight;
			node.lowerLeft = lowLeft;
			node.faces = f;
			//System.out.println("Leaf Created with one face: " + node.faces);
			return node;
		}
		//System.out.println("Depth: " + depth);
		//System.out.println("UpperRight: " + upRight);
		//System.out.println("LowerLeft: " + lowLeft);
		//System.out.println("Number of Faces: " + f.size());
		//System.out.println("Faces: " + f);
		
		
		int axis;
		float midpoint;
		
		axis = depth % 3;
		boolean splitPointless;
		int tries = 1;
		TreeNode node = new TreeNode();
		node.upperRight = upRight;
		node.lowerLeft = lowLeft;
		ArrayList<Face> leftList = new ArrayList<Face>();
		ArrayList<Face> rightList = new ArrayList<Face>();
		Point3f leftUpRight, rightLowLeft;
		do {
			splitPointless = false;
			Collections.sort(f, new FaceComparator(axis));
			
			int median = f.size() / 2;
			
			if (f.size() % 2 == 0) {
				Point3f left = f.get(median-1).center;
				Point3f right = f.get(median).center;
	
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
			//System.out.println("axis: " + axis);
			//System.out.println("midpt: " + midpoint);
			//System.out.println("^^^^^^^^^^^^^^");
			

			if (axis == 0) {
				leftUpRight = new Point3f(midpoint, upRight.y, upRight.z);
				rightLowLeft = new Point3f(midpoint, lowLeft.y, lowLeft.z);
				if(upRight.x == midpoint || lowLeft.x == midpoint)
					splitPointless = true;
			}
			else if (axis == 1){
				leftUpRight = new Point3f(upRight.x, midpoint, upRight.z);
				rightLowLeft = new Point3f(lowLeft.x, midpoint, lowLeft.z);
				if(upRight.y == midpoint || lowLeft.y == midpoint)
					splitPointless = true;
			}
			else {
				leftUpRight = new Point3f(upRight.x, upRight.y, midpoint);
				rightLowLeft = new Point3f(lowLeft.x, lowLeft.y, midpoint);
				if(upRight.z == midpoint || lowLeft.z == midpoint)
					splitPointless = true;
			}
			if (splitPointless) {
				axis = (axis + 1) % 3;
				tries++;
			}
		} while (splitPointless && tries < 4);
		
		if (tries == 4) {
			node.upperRight = upRight;
			node.lowerLeft = lowLeft;
			node.faces = f;
			return node;
		}
		
		float volume = (upRight.x - lowLeft.x) * (upRight.y - lowLeft.y)* (upRight.z - lowLeft.z); 
		boolean smallerFound = false;
		
		for(int i = 0; i < f.size(); i++) {
			Point3f left = f.get(i).lowerLeft;
			Point3f right = f.get(i).upperRight;
			float faceVolume = (right.x - left.x) * (right.y - left.y) * (right.z - left.z); 
			if (faceVolume < volume)
				smallerFound = true;
			if (axis == 0) {
				if (left.x <= midpoint)
					leftList.add(f.get(i));
				if (right.x > midpoint)
					rightList.add(f.get(i));
			}
			else if (axis == 1) {
				if (left.y <= midpoint)
					leftList.add(f.get(i));
				if (right.y > midpoint)
					rightList.add(f.get(i));
			}
			else {
				if (left.z <= midpoint)
					leftList.add(f.get(i));
				if (right.z > midpoint)
					rightList.add(f.get(i));
			}
		}
		boolean listsIdentical = false;
		if (leftList.size() == rightList.size()) {
			listsIdentical = true;
			for (int i = 0; i < leftList.size(); i++) {
				if (!leftList.contains(rightList.get(i))) {
					listsIdentical = false;
					break;
				}
			}
		}
		if (smallerFound && !listsIdentical) {
			node.left = kdTreeHelper(leftList, depth+1, leftUpRight, lowLeft);	
			node.right = kdTreeHelper(rightList, depth+1, upRight, rightLowLeft);
		}
		
		if (node.left == null && node.right == null) { //this is a leaf node
			node.faces = f;
		}
		//System.out.println("**********************");
		return node;
	}
	
	private GVector computePluckerCoord(Vector3f start, Vector3f end) {
		GVector ret = new GVector(6);
		ret.setElement(0, start.x * end.y - end.x * start.y);
		ret.setElement(1, start.x * end.z - end.x * start.z);
		ret.setElement(2, start.x - end.x);
		ret.setElement(3, start.y * end.z - end.y * start.z);
		ret.setElement(4, start.z - end.z);
		ret.setElement(5, end.y - start.y);
		return ret;
	}
	
	private float computeSideOperator(GVector a, GVector b) {
		if (a.getSize() != 6 || b.getSize() != 6)
			System.out.println("Vectors of incorrect size supplied");
		return (float) (a.getElement(0) * b.getElement(4) + 
				a.getElement(1) * b.getElement(5) + 
				a.getElement(2) * b.getElement(3) + 
				a.getElement(3) * b.getElement(2) + 
				a.getElement(4) * b.getElement(0) + 
				a.getElement(5) * b.getElement(1));
	}
	
	private ArrayList<Vector3f> intersectFace(Vector3f v0, Vector3f v1, Vector3f v2, GVector line, Vector3f start, Vector3f end, boolean recurrsing) {
		ArrayList<Vector3f> hitPos = new ArrayList<Vector3f>();
		GVector e1, e2, e3;
		Vector3f norm;
		float s1, s2, s3;
		e1 = computePluckerCoord(v0, v1);
		e2 = computePluckerCoord(v1, v2);
		e3 = computePluckerCoord(v2, v0);
		s1 = computeSideOperator(line, e1);
		s2 = computeSideOperator(line, e2);
		s3 = computeSideOperator(line, e3);
		
		if (s1 == 0 && s2 == 0 && s3 == 0) {
			//The line and face are coplanar
			Vector3f edge1 = new Vector3f();
			Vector3f edge2 = new Vector3f();
			edge1.sub(v1, v0);
			edge2.sub(v2, v0);
			norm = new Vector3f();
			norm.cross(edge1, edge2);
			norm.normalize();
			// create a point that is not co-planar
			Vector3f p = new Vector3f();
			p.add(v0, norm);
			if (!recurrsing) {
				ArrayList<Vector3f> interPos1 = intersectFace(v0, v1, p, line, start, end, true);
				ArrayList<Vector3f> interPos2 = intersectFace(v1, v2, p, line, start, end, true);
				ArrayList<Vector3f> interPos3 = intersectFace(v2, v0, p, line, start, end, true);
				if (interPos1 == null && interPos2 == null && interPos3 == null)
					return null;
				if (interPos1 != null)
					hitPos.addAll(interPos1);
				if (interPos2 != null)
					hitPos.addAll(interPos2);
				if (interPos3 != null)
					hitPos.addAll(interPos3);
				for (int i = 0; i < hitPos.size(); i++) {
					for (int j = i+1; j < hitPos.size(); j++) {
						if (hitPos.get(i).equals(hitPos.get(j))) {
							ArrayList<Vector3f> temp = new ArrayList<Vector3f>();
							temp.add(hitPos.get(i));
							return temp;
						}	
					}
				}
				return hitPos;
			}
			else {
				return hitPos;
			}
			
		}
		else {
			// if s1, s2, and s3 do not all have the same sign
			if (!((s1 <= 0 && s2 <= 0 && s3 <= 0) || (s1 >= 0 && s2 >= 0 && s3 >= 0) || (s1 == 0 && s2 == 0 && s3 == 0))) {
				//no intersection
				return null;
			}
			// if s1, s2, and s3 are all less than (or greater than) zero
			//if ((s1 < 0 && s2 < 0 && s3 < 0) || (s1 > 0 && s2 > 0 && s3 > 0)) {
				//line passes through middle of triangle
			Vector3f edge1 = new Vector3f();
			Vector3f edge2 = new Vector3f();
			edge1.sub(v1, v0);
			edge2.sub(v2, v0);
			norm = new Vector3f();
			norm.cross(edge1, edge2);
			norm.normalize();
			Vector3f temp = new Vector3f();
			temp.sub(v0, start);
			float numerator = temp.dot(norm);
			Vector3f temp2 = new Vector3f();
			temp2.sub(end, start);
			temp2.normalize();
			float denominator = temp2.dot(norm);
			if (denominator != 0) {
				Vector3f interPos = new Vector3f();
				temp2.scale(numerator/denominator);
				interPos.add(temp2, start);
				hitPos.add(interPos);
				return hitPos;
			}
			else {
				System.out.println("The two intersection algorithms disagree");
				return null;
			}
		}
	}
	
	public ArrayList<Face> intersectLine(Vector3f start, Vector3f end) {
		GVector l = computePluckerCoord(start, end);
		ArrayList<Face> hit = new ArrayList<Face>();
		ArrayList<Vector3f> hitPos = new ArrayList<Vector3f>();
		for (Face f : faces) {
			ArrayList<Vector3f> interPos = intersectFace(f.v0.pos, f.v1.pos, f.v2.pos, l, start, end, false);
			if (interPos != null && interPos.size() != 0) {
				hit.add(f);
				for (Vector3f pos : interPos)
					hitPos.add(pos);
			}
		}
		return hit; //return hitPos also!!
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
		ArrayList<Face> intersects = intersectLine(new Vector3f(0,0,0), new Vector3f(0,1,0));
		for (Face f: intersects) {
			System.out.println("Hit Face: (" + f.v0.pos + ", " + f.v1.pos + ", " + f.v2.pos + ")");
		}
		System.out.println("Size: " + intersects.size());
		
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
			result.append("Face");
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
