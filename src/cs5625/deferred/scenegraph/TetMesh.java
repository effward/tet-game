package cs5625.deferred.scenegraph;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Color3f;
import javax.vecmath.GVector;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.Material;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.misc.OpenGLResourceObject;

/** A tetrahedral mesh object. */
public class TetMesh extends Mesh implements OpenGLResourceObject {
	private ArrayList<Vert> verts;
	private ArrayList<Face> faces;
	private ArrayList<Tet> tets;
	private ArrayList<Material> mats;
	private ArrayList<Face> interfaces;
	private ArrayList<Face> boundaries;
	private ArrayList<Face> partiallyCulled;
	private ArrayList<Integer> culledPolygons;
	private ArrayList<Integer> culledVerts;

	private TreeNode root;
	private int kdCutoff = 5;
	private Point3f mUpperRight, mLowerLeft;
	private float epsilon = 0.00000000001f;

	
	/** A new empty TetMesh. */
	public TetMesh() {
		verts = new ArrayList<Vert>(10);
		faces = new ArrayList<Face>(10);
		tets = new ArrayList<Tet>(10);
		mats = new ArrayList<Material>(2);
		partiallyCulled = new ArrayList<Face>();
		culledPolygons = new ArrayList<Integer>();
		culledVerts = new ArrayList<Integer>();
	}
	
	/** Set the materials of this TetMesh to the given list of materials. */
	public void setMats(ArrayList<Material> mats) {
		this.mats = mats;

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
		ArrayList<Face> all_faces = new ArrayList<Face>(boundaries);
		//System.out.println("Starting KD Tree Construction...");
		//System.out.println(all_faces.size() + " faces to be added");
		return kdTreeHelper(all_faces, 0, mUpperRight, mLowerLeft);
		//return kdTreeTemp(all_faces, 0, mUpperRight, mLowerLeft);
	}
	
	private TreeNode kdTreeHelper(ArrayList<Face> f, int depth, Point3f upRight, Point3f lowLeft) {
		if (f.size() <= 0) {
			//System.out.println("Faces empty");
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
		if (depth >= kdCutoff) {
			//System.out.println("cutoff reached, creating node with faces : " + f.size());
			TreeNode node = new TreeNode();
			node.upperRight = upRight;
			node.lowerLeft = lowLeft;
			node.faces = f;
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
		ArrayList<Face> list = new ArrayList<Face>();
		list.addAll(leftList);
		list.addAll(rightList);
		for (int i = 0; i < f.size(); i++) {
			if(!list.contains(f.get(i))) {
				System.out.println("*************HEMORRHAGING FACESSSSSS***************");
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
	
	/** When deleting a tet, used to delete faces. */
	public boolean removeFaceIfNecessary(Face f, Tet t, int i) {
		if (f.t1 == null) { //was a boundary
			faces.remove(f); //cull from TetMesh's faces list.
			return true;
		}
		else { // had a second tet - have to rearrange 
			if (f.t0 == t) {
				//shift face's second tet to first slot.
				f.t0 = f.t1;
				f.t1 = null;
				//Swap vertex order so that the face is now pointing outwards.
				Vert temp = f.v2; 
				f.v2 = f.v1;
				f.v1 = temp;
			}
			else if (f.t1 == t) {
				f.t1 = null;
			}
			else {
				System.out.println("neither of f" + i + "'s tets was toRemove!");
			}
			return false;
		}

	}
	
	/** Remove the given tetrahedron. */
	public void deleteTet(Tet toRemove, Vector3f start, Vector3f end) {
		//remove tet from tets list
		this.tets.remove(toRemove);
		
		//cull faces if necessary
		boolean remove0 = removeFaceIfNecessary(toRemove.f0, toRemove, 0);
		boolean remove1 = removeFaceIfNecessary(toRemove.f1, toRemove, 1);
		boolean remove2 = removeFaceIfNecessary(toRemove.f2, toRemove, 2);
		boolean remove3 = removeFaceIfNecessary(toRemove.f3, toRemove, 3);
		
		//add faces to partially culled list
		if (remove0)
			partiallyCulled.add(toRemove.f0);
		if (remove1)
			partiallyCulled.add(toRemove.f1);
		if (remove2)
			partiallyCulled.add(toRemove.f2);
		if (remove3)
			partiallyCulled.add(toRemove.f3);
		
		
		ArrayList<TreeNode> toVisit = new ArrayList<TreeNode>();
		toVisit.add(root);
		while(toVisit.size() > 0) {
			TreeNode curr = toVisit.remove(0);
			if (intersectLineWithBB(start, end, new Vector3f(curr.lowerLeft), new Vector3f (curr.upperRight), false)) {
				if (curr.left != null)
					toVisit.add(curr.left);
				if (curr.right != null)
					toVisit.add(curr.right);
				if (curr.left == null && curr.right == null) {
					if (remove0)
						curr.faces.remove(toRemove.f0);
					if (remove1)
						curr.faces.remove(toRemove.f1);
					if (remove2)
						curr.faces.remove(toRemove.f2);
					if (remove3)
						curr.faces.remove(toRemove.f3);
				}
			}
		}
	
		//System.out.println("Deleted tet!");
		createSurface(); //would be nice to not have to do this every time.
	}
	

	/** Create a new tet poking out of the first face along the line from start to end. */
	public void createTetAtFirstFaceAlongLine(Vector3f start, Vector3f end, boolean accelerate) {
		Face f = findFirstFaceAlongLine(start, end, true);
		if (f == null) {
			//and no tets were created that day
		}
		else if (f.t1 == null) {
			Vector3f midpt = new Vector3f(f.center);
			//midpt.add(f.v0.pos, f.v1.pos);
			//midpt.scale(.5f);
			//midpt.add(f.v2.pos);
			//midpt.scale(.5f);
		
			
			Vector3f edge1 = new Vector3f();
			edge1.sub(f.v1.pos, f.v0.pos);
			Vector3f edge2 = new Vector3f();
			edge2.sub(f.v2.pos, f.v0.pos);
			Vector3f norm = new Vector3f();
			norm.cross(edge1, edge2);
			norm.normalize();
			float l = -(edge1.length() + edge2.length()) / 2;
			norm.scale(l);
			midpt.add(norm);
			
			//System.out.println("v0: " + f.v0.pos + ", v1: " + f.v1.pos + ", v2: " + f.v2.pos + ", new: " + midpt);
			
			Vert created = new Vert(midpt);
			verts.add(created);
			
			
			Tet tet = new Tet(f.v1, f.v0, f.v2, created, 0);
			System.out.println(tet);
			tets.add(tet);
			
			createSurface();
		}
	}
	
	/** Return the first face encountered along the line between start and end. */
	private Face findFirstFaceAlongLine(Vector3f start, Vector3f end, boolean accelerate) {
		ArrayList<FacePointIntersectionPair> intersections = intersectLine(start, end, accelerate);
		Point3f pos = new Point3f(start);
		Face f = null;
		float min_dist = Float.MAX_VALUE;
		for (FacePointIntersectionPair pair : intersections) {
			for (int i = 0; i < pair.points.size(); i++) {
				float d = pos.distance(new Point3f(pair.points.get(i)));
				if (d < min_dist) {
					min_dist = d;
					f = pair.face;
				}
			}
		}
		return f;
	}
	
	/** Return the first Tet encountered along the line between start and end. */
	private Tet findFirstTetAlongLine(Vector3f start, Vector3f end, boolean accelerate) {
		Face f = findFirstFaceAlongLine(start, end, accelerate);
		if (f == null) {
			return null; //and no tets were found that day
		}
		if (f.t1 != null) {
			System.out.println("Face clicked on had a second tet!");
		}
		return f.t0;
	}
	
	/** Delete first tet encountered along the line between start and end. */
	public void deleteFirstTetAlongLine(Vector3f start, Vector3f end, boolean accelerate) {
		Tet remove = findFirstTetAlongLine(start, end, accelerate);
		if (remove != null) deleteTet(remove, start, end);
	}

	/**********************************************************
	 * Start Intersection Calculation Stuff
	 ********************************************************/
	
	private enum IntersectionType {
		NONE, PROPER, PROPER_END, VERTEX, VERTEX_END, EDGE, EDGE_END, 
		COPLANAR_VERTEX, COPLANAR_TWO_EDGES, COPLANAR_EDGE, COPLANAR_VERTEX_EDGE, COPLANAR_CONTAINED,
		LINE, LINE_LINE_SEGMENT, LINE_CONTAINS_SEGMENT, LINE_END, COLINEAR_SEGMENTS
	}
	
	private class FacePointIntersectionPair {
		public ArrayList<Vector3f> points;
		public Face face;
		public IntersectionType type;
		
		public FacePointIntersectionPair (PointIntersection intersection, Face f) {
			this.type = intersection.type;
			this.face = f;
			this.points = intersection.points;	
		}
	}
	
	private class PointIntersection {
		public ArrayList<Vector3f> points;
		public IntersectionType type;
	}
	
	private float perpProduct(Vector3f u, Vector3f v) {
		return u.x * v.y - u.y * v.x;
	}
	
	
	//returns a float scalar of how far into segment 2 the intersection point is, or -1 if no intersection
	private ArrayList<Float> intersect2DSegments(Vector3f start1, Vector3f end1, Vector3f start2, Vector3f end2, boolean axis) {
		ArrayList<Float> ret = new ArrayList<Float>();
		Vector3f u, v, w;
		if (axis) {
			u = new Vector3f(end1.x-start1.x, end1.y-start1.y, 0);
			v = new Vector3f(end2.x-start2.x, end2.y-start2.y, 0);
			w = new Vector3f(start1.x-start2.x, start1.y-start2.y, 0);
		}
		else {
			u = new Vector3f(0, end1.y-start1.y, end1.z-start1.z);
			v = new Vector3f(0, end2.y-start2.y, end2.z-start2.z);
			w = new Vector3f(0, start1.y-start2.y, start1.z-start2.z);
		}
		float d = perpProduct(u, v);
		if (d == 0) {
			float t0, t1;
			Vector3f w2 = new Vector3f(end1.x-start2.x, end1.y-start2.y, 0);
			if (v.x != 0) {
				t0 = w.x / v.x;
				t1 = w2.x / v.x;
			}
			else {
				t0 = w.y / v.y;
				t1 = w2.y / v.y;
			}
			if (t0 > t1) {
				float t = t0;
				t0 = t1;
				t1 = t;
			}
			if (t0 > 1 || t1 < 0) {
				ret.add(-1f);
				return ret;
			}
			t0 = t0 < 0 ? 0 : t0;
			t1 = t1 > 1 ? 1 : t1;
			if (t0 == t1) {
				ret.add((Float)t0);
				return ret;
			}
			ret.add((Float)t0);
			ret.add((Float)t1);
			return ret;
		}
		float s = perpProduct(v,w) / d;
		if (s < 0 || s > 1) {
			ret.add(-1f);
			return ret;
		}
		
		float t = perpProduct(u,w) / d;
		if (t < 0 || t > 1) {
			ret.add(-1f);
			return ret;
		}
		ret.add(t);
		ret.add(s);
		return ret;
	}
	
	private PointIntersection intersectLineLineSegment(Vector3f start1, Vector3f end1, Vector3f start2, Vector3f end2, boolean needCoord) {
		PointIntersection intersection = new PointIntersection();
		GVector line = computePluckerCoord(start1, end1);
		GVector segment = computePluckerCoord(start2, end2);
		float s0 = computeSideOperator(line, segment);
		if (s0 != 0) {
			intersection.type = IntersectionType.NONE;
			return intersection;
		}
		Vector3f temp1 = new Vector3f();
		Vector3f temp2 = new Vector3f();
		temp1.sub(end1, start1);
		temp2.sub(end2, start2);
		temp2.cross(temp1, temp2);
		temp2.normalize();
		temp1.add(temp2, start1);
		GVector l1 = computePluckerCoord(temp1, start2);
		GVector l2 = computePluckerCoord(end2, temp1);
		float s1 = computeSideOperator(line, l1);
		float s2 = computeSideOperator(line, l2);
		if ((s1 < 0 && s2 > 0) || (s1 > 0 && s2 < 0)) {
			intersection.type = IntersectionType.NONE;
			return intersection;
		}
		else if ((s1 < 0 && s2 < 0) || (s1 > 0 && s2 > 0)) {
			intersection.type = IntersectionType.LINE_LINE_SEGMENT;
		}
		else if (s1 == 0 && s2 == 0) {
			intersection.type = IntersectionType.LINE_CONTAINS_SEGMENT;
		}
		else {
			intersection.type = IntersectionType.LINE_END;
		}
		if (!needCoord)
			return intersection;
		ArrayList<Float> results = intersect2DSegments(start1, end1, start2, end2, true);
		if (results.get(0).equals(-1f)) {
			System.out.println("The two intersection algs disagree");
			intersection.type = IntersectionType.NONE;
			return intersection;
		}
		Vector3f interPos = new Vector3f();
		Vector3f dir = new Vector3f();
		dir.sub(end2, start2);
		dir.scale(results.get(0));
		interPos.add(start2, dir);
		if (intersection.type == IntersectionType.LINE_CONTAINS_SEGMENT) {
			Vector3f dir2 = new Vector3f();
			dir2.sub(interPos, start1);
			dir.sub(end1, start1);
			Vector3f cross = new Vector3f();
			cross.cross(dir, dir2);
			if (cross.equals(new Vector3f(0,0,0))) {
				ArrayList<Vector3f> temp = new ArrayList<Vector3f>();
				temp.add(interPos);
				if (results.size() > 1) {
					Vector3f interPos2 = new Vector3f();
					dir.sub(end2, start2);
					dir.scale(results.get(1));
					interPos2.add(start2, dir);
					temp.add(interPos2);
				}
				intersection.points = temp;
				return intersection;
			}
			else {
				System.out.println("The intersection algs for line segs disagree");
				intersection.type = IntersectionType.NONE;
				return intersection;
			}
		}
		else {
			if (results.size() == 1) {
				//colinear end point intersection in projected case
				Vector3f dir2 = new Vector3f();
				dir2.sub(interPos, start1);
				dir.sub(end1, start1);
				Vector3f cross = new Vector3f();
				cross.cross(dir, dir2);
				if (cross.equals(new Vector3f(0,0,0))) {
					ArrayList<Vector3f> temp = new ArrayList<Vector3f>();
					temp.add(interPos);
					intersection.points = temp;
					return intersection;
				}
				else {
					intersection.type = IntersectionType.NONE;
					return intersection;
				}
			}
			Vector3f interPos2 = new Vector3f();
			Vector3f dir2 = new Vector3f();
			dir2.sub(end1, start1);
			dir2.scale(results.get(1));
			interPos2.add(start1, dir2);
			if(interPos.equals(interPos2)) {
				ArrayList<Vector3f> temp = new ArrayList<Vector3f>();
				temp.add(interPos);
				intersection.points = temp;
				return intersection;
			}
			else {
				System.out.println("The 2 intersection algs disagree for line segs");
				intersection.type = IntersectionType.NONE;
				return intersection;
			}
		}
	}
	
	private PointIntersection intersectLineSegmentLineSegment(Vector3f start1, Vector3f end1, Vector3f start2, Vector3f end2) {
		PointIntersection intersection1 = intersectLineLineSegment(start1, end1, start2, end2, true);
		if (intersection1.type == IntersectionType.NONE)
			return intersection1;
		PointIntersection intersection2 = intersectLineLineSegment(start2, end2, start1, end1, true);
		if (intersection2.type == IntersectionType.NONE)
			return intersection2;
		if (intersection1.points.size() != intersection2.points.size()) {
			System.out.println("Line segments disagree about number of intersections");
			intersection1.type = IntersectionType.NONE;
			return intersection1;
		}
		if (intersection1.points.size() == 1) {
			if(intersection1.points.get(0).equals(intersection2.points.get(0))) {
				if (intersection1.type == IntersectionType.LINE_CONTAINS_SEGMENT)
					intersection1.type = IntersectionType.COLINEAR_SEGMENTS;
				else
					intersection1.type = IntersectionType.LINE;
				return intersection1;
			}
			else {
				System.out.println("Both segments found intersections, but they dont match");
				System.out.println("Intersect1: " + intersection1.points.get(0));
				System.out.println("Intersect2: " + intersection2.points.get(0));
				intersection1.type = IntersectionType.NONE;
				return intersection1;
			}
		}
		else if (intersection1.points.size() == 2) {
			if((intersection1.points.get(0).equals(intersection2.points.get(0)) ||
					intersection1.points.get(0).equals(intersection2.points.get(1))) &&
					(intersection1.points.get(1).equals(intersection2.points.get(0)) ||
					intersection1.points.get(1).equals(intersection2.points.get(1)))) {
				intersection1.type = IntersectionType.COLINEAR_SEGMENTS;
				return intersection1;
			}
			else {
				System.out.println("Both segments found different colinear intersection points");
				intersection1.type = IntersectionType.NONE;
				return intersection1;
			}
		}
		System.out.println("Something really weird happened");
		intersection1.type = IntersectionType.NONE;
		return intersection1;
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
	
	private Vector3f computeIntersectionPoint(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f start, Vector3f end) {
		Vector3f norm;
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
			return interPos;
		}
		else {
			System.out.println("The two intersection algorithms disagree");
			return null;
		}
	}
	
	private PointIntersection computeCoplanarIntersectionPoints(Vector3f v0, Vector3f v1, Vector3f v2, GVector line, Vector3f start, Vector3f end) {
		PointIntersection intersection = new PointIntersection();
		Vector3f norm;
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
		PointIntersection interPos1 = intersectFace(v0, v1, p, line, start, end, true);
		PointIntersection interPos2 = intersectFace(v1, v2, p, line, start, end, true);
		PointIntersection interPos3 = intersectFace(v2, v0, p, line, start, end, true);
		ArrayList<Vector3f> temp = new ArrayList<Vector3f>();
		if (interPos1.type == IntersectionType.NONE && interPos2.type == IntersectionType.NONE && interPos3.type == IntersectionType.NONE) {
			intersection.type = IntersectionType.NONE;
			return intersection;
		}
		else if ((interPos1.type == IntersectionType.NONE && interPos2.type == IntersectionType.VERTEX && interPos3.type == IntersectionType.VERTEX) ||
				(interPos2.type == IntersectionType.NONE && interPos1.type == IntersectionType.VERTEX && interPos3.type == IntersectionType.VERTEX) ||
				(interPos3.type == IntersectionType.NONE && interPos2.type == IntersectionType.VERTEX && interPos1.type == IntersectionType.VERTEX)) {
			intersection.type = IntersectionType.COPLANAR_VERTEX;
			if (interPos1.type != IntersectionType.NONE)
				temp.addAll(interPos1.points);
			else if (interPos2.type != IntersectionType.NONE)
				temp.addAll(interPos2.points);
			else if (interPos3.type != IntersectionType.NONE)
				temp.addAll(interPos3.points);
		}
		else if ((interPos1.type == IntersectionType.NONE && interPos2.type == IntersectionType.EDGE && interPos3.type == IntersectionType.EDGE) ||
				(interPos2.type == IntersectionType.NONE && interPos1.type == IntersectionType.EDGE && interPos3.type == IntersectionType.EDGE) ||
				(interPos3.type == IntersectionType.NONE && interPos2.type == IntersectionType.EDGE && interPos1.type == IntersectionType.EDGE)) {
			intersection.type = IntersectionType.COPLANAR_TWO_EDGES;
			if (interPos1.type != IntersectionType.NONE)
				temp.addAll(interPos1.points);
			if (interPos2.type != IntersectionType.NONE)
				temp.addAll(interPos2.points);
			if (interPos3.type != IntersectionType.NONE)
				temp.addAll(interPos3.points);
		}
		else if ((interPos1.type == IntersectionType.COPLANAR_VERTEX && interPos2.type == IntersectionType.VERTEX && interPos3.type == IntersectionType.VERTEX) ||
				(interPos2.type == IntersectionType.COPLANAR_VERTEX && interPos1.type == IntersectionType.VERTEX && interPos3.type == IntersectionType.VERTEX) ||
				(interPos3.type == IntersectionType.COPLANAR_VERTEX && interPos2.type == IntersectionType.VERTEX && interPos1.type == IntersectionType.VERTEX)) {
			intersection.type = IntersectionType.COPLANAR_EDGE;
			if (interPos1.type == IntersectionType.VERTEX)
				temp.addAll(interPos1.points);
			if (interPos2.type == IntersectionType.VERTEX)
				temp.addAll(interPos2.points);
			if (interPos3.type == IntersectionType.VERTEX)
				temp.addAll(interPos3.points);
		}
		else if ((interPos1.type == IntersectionType.EDGE && interPos2.type == IntersectionType.VERTEX && interPos3.type == IntersectionType.VERTEX) ||
				(interPos2.type == IntersectionType.EDGE && interPos1.type == IntersectionType.VERTEX && interPos3.type == IntersectionType.VERTEX) ||
				(interPos3.type == IntersectionType.EDGE && interPos2.type == IntersectionType.VERTEX && interPos1.type == IntersectionType.VERTEX)) {
			intersection.type = IntersectionType.COPLANAR_VERTEX_EDGE;
			if (interPos1.type == IntersectionType.VERTEX)
				temp.addAll(interPos1.points);
			else if (interPos2.type == IntersectionType.VERTEX)
				temp.addAll(interPos2.points);
			else if (interPos3.type == IntersectionType.VERTEX)
				temp.addAll(interPos3.points);
			if (interPos1.type == IntersectionType.EDGE)
				temp.addAll(interPos1.points);
			else if (interPos2.type == IntersectionType.EDGE)
				temp.addAll(interPos2.points);
			else if (interPos3.type == IntersectionType.EDGE)
				temp.addAll(interPos3.points);
		}
		intersection.points = temp;
		return intersection;
	}
	
	private PointIntersection intersectFace(Vector3f v0, Vector3f v1, Vector3f v2, GVector line, Vector3f start, Vector3f end, boolean recurrsing) {
		ArrayList<Vector3f> hitPos = new ArrayList<Vector3f>();
		PointIntersection intersection = new PointIntersection();
		GVector e1, e2, e3;
		float s1, s2, s3;
		e1 = computePluckerCoord(v0, v1);
		e2 = computePluckerCoord(v1, v2);
		e3 = computePluckerCoord(v2, v0);
		s1 = computeSideOperator(line, e1);
		s2 = computeSideOperator(line, e2);
		s3 = computeSideOperator(line, e3);
		
		if (s1 == 0 && s2 == 0 && s3 == 0) {
			//The line and face are coplanar
			if (recurrsing) {
				intersection.type = IntersectionType.COPLANAR_VERTEX;
				return intersection;
			}
			return computeCoplanarIntersectionPoints(v0, v1, v2, line, start, end);
		}
		else {
			Vector3f interPos = null;
			if ((s1 < 0 && s2 < 0 && s3 < 0) || (s1 > 0 && s2 > 0 && s3 > 0)) {
				intersection.type = IntersectionType.PROPER;
				interPos = computeIntersectionPoint(v0, v1, v2, start, end);
			}
			else if ((s1 == 0 && s2*s3 > 0) || (s2 == 0 && s1*s3 > 0) || (s3 == 0 && s2*s1 > 0)) {
				intersection.type = IntersectionType.EDGE;
				interPos = computeIntersectionPoint(v0, v1, v2, start, end);
			}
			else if ((s1 == 0 && s2 == 0) || (s1 == 0 && s3 == 0) || (s3 == 0 && s2 == 0)) {
				intersection.type = IntersectionType.VERTEX;
				interPos = computeIntersectionPoint(v0, v1, v2, start, end);
			}
			
			if (interPos == null) {
				intersection.type = IntersectionType.NONE;
				return intersection;
			}
			hitPos.add(interPos);
			intersection.points = hitPos;
			return intersection;
		}
	}
	
	public ArrayList<FacePointIntersectionPair> intersectLine(Vector3f start, Vector3f end, boolean accelerate) {
		GVector l = computePluckerCoord(start, end);
		ArrayList<FacePointIntersectionPair> hit = new ArrayList<FacePointIntersectionPair>();
		int faceCount = 0;
		if (accelerate) {
			ArrayList<TreeNode> toVisit = new ArrayList<TreeNode>();
			toVisit.add(root);
			while(toVisit.size() > 0) {
				TreeNode current = toVisit.remove(0);
				if (intersectLineWithBB(start, end, new Vector3f(current.lowerLeft), new Vector3f(current.upperRight), false)) {
					if (current.left != null)
						toVisit.add(current.left);
					if (current.right != null)
						toVisit.add(current.right);
					if (current.left == null && current.right == null) {
						for (Face f : current.faces) {
							if (!partiallyCulled.contains(f)) {
								faceCount++;
								PointIntersection interPos = intersectFace(f.v0.pos, f.v1.pos, f.v2.pos, l, start, end, false);
								if (interPos.type != IntersectionType.NONE) {
									hit.add(new FacePointIntersectionPair(interPos, f));
								}
							}
						}
					}
				}
			}
		}
		else {
		
			for (Face f : faces) {
				PointIntersection interPos = intersectFace(f.v0.pos, f.v1.pos, f.v2.pos, l, start, end, false);
				if (interPos.type != IntersectionType.NONE) {
					hit.add(new FacePointIntersectionPair(interPos, f));
				}
				faceCount++;
			}
		}
		System.out.println(faceCount);
		
		return hit; //return hitPos also!!
	}
	
	public ArrayList<FacePointIntersectionPair> intersectLineSegment(Vector3f start, Vector3f end) {
		GVector l = computePluckerCoord(start, end);
		ArrayList<FacePointIntersectionPair> hit = new ArrayList<FacePointIntersectionPair>();
		
		for (Face f: faces) {
			PointIntersection intersection = intersectFace(f.v0.pos, f.v1.pos, f.v2.pos, l, start, end, false);
			if (intersection.type == IntersectionType.NONE)
				continue;
			else if (intersection.type == IntersectionType.PROPER || 
					intersection.type == IntersectionType.EDGE ||
					intersection.type == IntersectionType.VERTEX) {
				Vector3f vert0 = null;
				Vector3f vert1 = null;
				Vector3f vert2 = null;
				if (intersection.type == IntersectionType.VERTEX) {
					if (!f.v0.pos.equals(intersection.points.get(0))) {
						vert0 = f.v0.pos;
						vert1 = f.v1.pos;
						vert2 = f.v2.pos;
					}
					else if (!f.v1.pos.equals(intersection.points.get(0))) {
						vert0 = f.v1.pos;
						vert1 = f.v0.pos;
						vert2 = f.v2.pos;
					}				}
				else {
					vert0 = f.v0.pos;
					vert1 = f.v1.pos;
					vert2 = f.v2.pos;
				}
				if (vert0 == null || vert1 == null || vert2 == null) {
					System.out.println("************THIS REALLY SHOULDN'T HAPPEN**************");
					continue;
				}
				GVector l2 = computePluckerCoord(start, vert0);
				GVector l3 = computePluckerCoord(vert0, end);
				GVector l4 = computePluckerCoord(vert1, vert2);
				float s1 = computeSideOperator(l4, l3);
				float s2 = computeSideOperator(l4, l2);
				if (s1 == 0 || s2 == 0) {
					if (intersection.type == IntersectionType.PROPER)
						intersection.type = IntersectionType.PROPER_END;
					else if (intersection.type == IntersectionType.VERTEX)
						intersection.type = IntersectionType.VERTEX_END;
					else if (intersection.type == IntersectionType.EDGE)
						intersection.type = IntersectionType.EDGE_END;
				}
				else if ((s1 < 0 && s2 > 0) || (s1 > 0 && s2 < 0)) {
					continue;
				}
				hit.add(new FacePointIntersectionPair(intersection, f));
			}
			else { //coplanar
				PointIntersection inter1 = intersectLineSegmentLineSegment(start, end, f.v0.pos, f.v1.pos);
				PointIntersection inter2 = intersectLineSegmentLineSegment(start, end, f.v1.pos, f.v2.pos);
				PointIntersection inter3 = intersectLineSegmentLineSegment(start, end, f.v2.pos, f.v0.pos);
				ArrayList<Vector3f> temp = new ArrayList<Vector3f>();
				if (inter1.type == IntersectionType.LINE)
					temp.add(inter1.points.get(0));
				if (inter2.type == IntersectionType.LINE)
					temp.add(inter2.points.get(0));
				if (inter3.type == IntersectionType.LINE)
					temp.add(inter3.points.get(0));
				
				if (temp.size() == 3) {
					intersection.type = IntersectionType.COPLANAR_VERTEX_EDGE;
					for(int i = 0; i < temp.size(); i++) {
						for(int j = i+1; j < temp.size(); j++) {
							if(temp.get(i).equals(temp.get(j))) {
								temp.remove(j);
								break;
							}
						}
					}
					intersection.points = temp;
					hit.add(new FacePointIntersectionPair(intersection, f));
					continue;
				}
				else if (temp.size() == 2) {
					intersection.type = IntersectionType.COPLANAR_TWO_EDGES;
					intersection.points = temp;
					hit.add(new FacePointIntersectionPair(intersection, f));
					continue;
				}
				else if (temp.size() == 1) {
					intersection.type = IntersectionType.COPLANAR_EDGE;
					intersection.points = temp;
					hit.add(new FacePointIntersectionPair(intersection, f));
					continue;
				}
				intersection.type = IntersectionType.COPLANAR_CONTAINED;
				intersection.points = null;
				hit.add(new FacePointIntersectionPair(intersection, f));
				continue;
			}	
		}
		return hit;
	}
	
	/** Returns true if the line (just segment if segment is true)
	 * between start and end intersects
	 * the box between corners min and max.
	 */
	public boolean intersectLineWithBB(Vector3f start, Vector3f end, Vector3f min, Vector3f max, boolean segment) {
		
		//Convert into a ray...
		Vector3f dir = new Vector3f(end);
		dir.sub(start);
		float maxLen = dir.length();
		dir.normalize();
		
		//check six faces...
		float t, x, y, z;
		
		
		if (dir.x != 0.0f) {
			//min x
			t = (min.x - start.x) / dir.x;
			if (!segment || (t <= maxLen && t >= 0.0f)) {
				y = dir.y * t + start.y;
				z = dir.z * t + start.z;
				if (y >= min.y && y <= max.y && z >= min.z && z <= max.z) return true;
			}
			
			//max x
			t = (max.x - start.x) / dir.x;
			if (!segment || (t <= maxLen && t >= 0.0f)) {
				y = dir.y * t + start.y;
				z = dir.z * t + start.z;
				if (y >= min.y && y <= max.y && z >= min.z && z <= max.z) return true;
			}
		}
		else {
			System.out.println("zero x");
		}
		
		if (dir.y != 0.0f) {
			//min y
			t = (min.y - start.y) / dir.y;
			if (!segment || (t <= maxLen && t >= 0.0f)) {
				x = dir.x * t + start.x;
				z = dir.z * t + start.z;
				if (x >= min.x && x <= max.x && z >= min.z && z <= max.z) return true;
			}
			
			//max y
			t = (max.y - start.y) / dir.y;
			if (!segment || (t <= maxLen && t >= 0.0f)) {
				x = dir.x * t + start.x;
				z = dir.z * t + start.z;
				if (x >= min.x && x <= max.x && z >= min.z && z <= max.z) return true;
			}
		}
		else {
			System.out.println("zero y");
		}
		
		if (dir.z != 0.0f) {
			//min z
			t = (min.z - start.z) / dir.z;
			if (!segment || (t <= maxLen && t >= 0.0f)) {
				y = dir.y * t + start.y;
				x = dir.x * t + start.x;
				if (y >= min.y && y <= max.y && x >= min.x && x <= max.x) return true;
			}
			
			//max z
			t = (max.z - start.z) / dir.z;
			if (!segment || (t <= maxLen && t >= 0.0f)) {
				y = dir.y * t + start.y;
				x = dir.x * t + start.x;
				if (y >= min.y && y <= max.y && x >= min.x && x <= max.x) return true;
			}
		}

		else {
			System.out.println("zero z");
		}
		
		return false;
	}
	
	
	/**********************************************************
	 * End Intersection Calculation Stuff
	 ********************************************************/
	
	
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
		this.tets = new ArrayList<Tet>(tets.size() / 4);
		for (int i = 0; i < tets.size() / 4; i++) {
			Vert v0 = verts.get(tets.get(4 * i));
			Vert v1 = verts.get(tets.get(4 * i + 1));
			Vert v2 = verts.get(tets.get(4 * i + 2));
			Vert v3 = verts.get(tets.get(4 * i + 3));
			
			Tet t = new Tet(v0, v1, v2, v3, 0);
			this.tets.add(t);
		}
		createSurface();
		root = buildKDTree();
		//printKDTree();
		
		//System.out.println("Starting intersect line segment");
		//ArrayList<FacePointIntersectionPair> list = intersectLineSegment(new Vector3f(0,0,0), new Vector3f(0,1,0));
		//System.out.println(list.size());
		/*
		for (FacePointIntersectionPair pair : list) {
			System.out.println(pair.type + ", " + pair.points);
		}
		*/
		
	}
	
	/** Calculate the interfaces (between different materials) and
	 * boundaries (between a tetrahedron at the edge of the mesh).
	 */
	private void calculateInterfacesAndBoundaries() {
		interfaces = new ArrayList<Face>(10);
		boundaries = new ArrayList<Face>(10);
		for (Face f: faces) {
			if (f.t1 == null){
				boundaries.add(f);
			}
			else if (f.t0.mat != f.t1.mat) { //change to account for transparency..
				interfaces.add(f);
			}
		}
	}
	
	/** Set this TetMesh's polygons to be present at any interface between
	 * tetrahedrons with different materials (where at least one is non-opaque), or
	 * at boundaries where a Face has only one adjacent Tet.
	 */
	private void createSurface() {
		//Go through and find all faces which are interfaces between different materials
		//or are boundaries at the edge of the tetmesh.
		calculateInterfacesAndBoundaries();
		
		
		float[] vtx = new float[verts.size() * 3];
		
		for (int i = 0; i < verts.size(); i++) {
			vtx[3 * i] = verts.get(i).pos.x;
			vtx[3 * i + 1] = verts.get(i).pos.y;
			vtx[3 * i + 2] = verts.get(i).pos.z;
		}
		
		//Copy these into an actual array now we know the number of faces required.
		int[] arr = new int[boundaries.size() * 3];
		
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
			texArr[2 * i] = (float)(Math.atan2(pos.z, pos.x) / (2*Math.PI) * 10);
			texArr[2 * i + 1] = (float)(Math.atan2(pos.y, Math.sqrt(pos.x * pos.x + pos.z * pos.z)) / (2*Math.PI) * 10);
		}
		
		int v0, v1, v2;
		Vector3f d0 = new Vector3f(), d1 = new Vector3f();
		for (int j = 0; j < boundaries.size(); j++) {
			Face f = boundaries.get(j);
			
			v0 = verts.indexOf(f.v0);
			v1 = verts.indexOf(f.v1);
			v2 = verts.indexOf(f.v2);
			arr[3 * j] = v0;
			arr[3 * j + 1] = v1;
			arr[3 * j + 2] = v2;
			
			//find normal for that face
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
		mVertexData = FloatBuffer.wrap(vtx);
		mPolygonData = IntBuffer.wrap(arr);
		mNormalData = FloatBuffer.wrap(normArr);
		mTexCoordData = FloatBuffer.wrap(texArr);
		
		System.out.println("Faces: " + faces.size()); //TODO remove
		System.out.println("Shown faces: " + boundaries.size()); //TODO remove
		
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
		
		public String toString() {
			return pos.toString();
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
		
		public String toString() {
			return "A tet: v0 = " + v0 + ", v1 = " + v1 + ", v2 = " + v2 + ", v3 = " + v3;
		}
		
		public boolean equals (Tet t) {
			return 
				((this.v0 == t.v0) || (this.v0 == t.v1) || (this.v0 == t.v2) || (this.v0 == t.v3)) &&
				((this.v1 == t.v0) || (this.v1 == t.v1) || (this.v1 == t.v2) || (this.v1 == t.v3)) &&
				((this.v2 == t.v0) || (this.v2 == t.v1) || (this.v2 == t.v2) || (this.v2 == t.v3)) &&
				((this.v3 == t.v0) || (this.v3 == t.v1) || (this.v3 == t.v2) || (this.v3 == t.v3));
		}
	}

	@Override
	public void releaseGPUResources(GL2 gl) {
		for (Material m: mats) {
			m.releaseGPUResources(gl);
		}
	}

	@Override
	public Material getMaterial() {
		return mats.get(0);
	}
}
