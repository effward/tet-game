package procedural;

/** Represents a triangle. */
public class Triangle {
	public int v0, v1, v2; //Vertex points
	
	public Triangle(int v0, int v1, int v2) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public Triangle copy() {
		return new Triangle(v0, v1, v2);
	}
}
