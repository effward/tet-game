package cs5625.deferred.scenegraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL2;
import javax.vecmath.Point2i;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.Material;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;

/**
 * Geometry.java
 * 
 * The Geometry class contains (in the way of Trimesh objects) the geometry of a scene. It also
 * provides functionality to load model files from disk. 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class Geometry extends SceneObject
{
	/* List of meshes in this object. */
	private ArrayList<Mesh> mMeshes = new ArrayList<Mesh>();
	
	/**
	 * Returns the list of meshes in this geometry object.
	 */
	public List<Mesh> getMeshes()
	{
		return mMeshes;
	}
	
	/**
	 * Adds a mesh to this geometry object. 
	 * 
	 * A mesh may be contained in multiple geometry objects, allowing you to 
	 * share data if you have multiple identical objects in your scene.
	 */
	public void addMesh(Mesh mesh)
	{
		mMeshes.add(mesh);
	}

	/**
	 * Adds a list of meshes to this geometry object. 
	 * 
	 * A mesh may be contained in multiple geometry objects, allowing you to 
	 * share data if you have multiple identical objects in your scene.
	 */
	public void addMeshes(List<Mesh> meshes)
	{
		mMeshes.addAll(meshes);
	}
	
	/**
	 * Removes a mesh from this geometry object.
	 * 
	 * @throws ScenegraphException If the given mesh isn't in this object.
	 */
	public void removeMesh(Mesh mesh) throws ScenegraphException
	{
		if (!mMeshes.remove(mesh))
		{
			throw new ScenegraphException("Mesh to remove is not in this Geometry object.");
		}
	}
	
	/**
	 * Returns the first mesh with the given name, or null if none with that name.
	 */
	public Mesh findMeshByName(String name)
	{
		for (Mesh mesh : mMeshes)
		{
			if (mesh.getName().equals(name))
			{
				return mesh;
			}
		}
		
		return null;
	}

	@Override
	public void releaseGPUResources(GL2 gl)
	{
		super.releaseGPUResources(gl);
		
		for (Mesh mesh : mMeshes)
		{
			mesh.releaseGPUResources(gl);
		}
	}
	
	@Override
	public void calculateTangentVectorsForAllGeometry()
	{
		super.calculateTangentVectorsForAllGeometry();
		
		for (Mesh mesh : mMeshes)
		{
			if (!mesh.vertexAttribData.containsKey("VertexTangent"))
			{
				mesh.vertexAttribData.put("VertexTangent", mesh.calculateTangentVectors());
			}
		}
	}
	
	/**
	 * Returns an array of all {v, t, n} vertices which have a given {v} position.
	 * 
	 * @param uniqueVertices The map of {v, t, n} index triplets to final vertex indices.
	 * @param vertexPositionIndex The {v} value to search for.
	 * 
	 * @return List of all final vertex indices whose {v} value equals vertexPositionIndex.
	 */
	private static ArrayList<Integer> findAllVerticesWithPositionIndex(HashMap<Point3i, Integer> uniqueVertices, int vertexPositionIndex)
	{
		ArrayList<Integer> results = new ArrayList<Integer>();
		
		for (Point3i indexTriplet : uniqueVertices.keySet())
		{
			if (indexTriplet.x == vertexPositionIndex)
			{
				results.add(uniqueVertices.get(indexTriplet));
			}
		}
		
		return results;
	}
}
