/*
* Copyright (C) 2020 Andrew Reid and the ModelGUI Project <http://www.modelgui.org>
* 
* This file is part of ModelGUI[core] (mgui-core).
* 
* ModelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* ModelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with ModelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

package mgui.geometry;

import java.util.ArrayList;

import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Vector2f;

import mgui.geometry.util.GeometryFunctions;

/************************************************************
 * Represents a 2D rectangle. This rectangle is aligned with the x- and y-axes by default, but a
 * rotation can be specified by setting the <code>rotation</code> vector, which represents the
 * orientation of the top and bottom edges.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Rect2D extends Shape2D {

	public Point2f corner1;
	public Point2f corner2;
	protected Vector2f rotation = new Vector2f(1f, 0f);		//orientation of bottom/top edges
															//is x-axis by default
	
	//constants
	/** Bottom-left corner */
	public static final int CNR_BL = 0;	
	/** Top-left corner */
	public static final int CNR_TL = 1;
	/** Bottom-right corner */
	public static final int CNR_BR = 2;
	/** Top-right corner */
	public static final int CNR_TR = 3;
	
	public Rect2D() {	
		this(new Point2f(), new Point2f(), new Vector2f(1f, 0f));
	}
	
	public Rect2D(Point2f pt1, Point2f pt2){
		this(pt1, pt2, new Vector2f(1f, 0f));
	}
	
	public Rect2D(Point2f pt1, Point2f pt2, Vector2f rotation){
		//corner1 = new Point2f(pt1);
		//corner2 = new Point2f(pt2);
		
		//set the corners such that corner1 is the base point
		this.rotation = rotation;
		corner1 = new Point2f(pt1);
		corner2 = new Point2f(pt2);
		Vector2f diag = new Vector2f(pt2);
		diag.sub(pt1);
		float angle = rotation.angle(diag);
		if (Math.abs(angle) > Math.PI / 2f){
			corner1.set(pt2);
			corner2.set(pt1);
			diag.scale(-1f);
			}
	}
	
	public Rect2D(Rect2D copy) {
		this.corner1 = new Point2f(copy.corner1);
		this.corner2 = new Point2f(copy.corner2);
		this.rotation = new Vector2f(copy.rotation);
	}
	
	public Rect2D(float x1, float y1, float x2, float y2){
		this(new Point2f(x1, y1), new Point2f(x2, y2), new Vector2f(1f, 0f));
	}
	
	public Rect2D(float x1, float y1, float x2, float y2, Vector2f rotation){
		this(new Point2f(x1, y1), new Point2f(x2, y2), rotation);
	}
	
	/************************************
	 * Sets the rotation vector of this rectangle, specifying the orientation of its top and bottom edges.
	 * If the angle between this vector and the diagonal between the two corners is greater than PI / 2,
	 * its direction is flipped.
	 * 
	 * @param v the new rotation vector
	 */
	public void setRotation(Vector2f v){
		rotation = new Vector2f(v);
		rotation.normalize();
		/*
		float angle = Math.abs(rotation.angle(getDiagonal()));
		if (angle > Math.PI / 2.0)
			rotation.scale(-1f);
		*/
	}
	
	/************************************
	 * Resets the rotation vector of this rectangle to its default of (1, 0) - the x-axis.
	 * 
	 * @param v
	 */
	public void resetRotation(){
		setRotation(new Vector2f(1f, 0f));
	}
	
	/************************************
	 * Returns a copy of this rectangle's rotation vector.
	 * 
	 * @return a copy of the rotation vector
	 */
	public Vector2f getRotation(){
		return new Vector2f(rotation);
	}
	
	/**********************************
	 * Returns the width of this rectangle, which is a distance along the edge perpendicular to 
	 * <code>rotation</code>.
	 * 
	 * @return the height of this rectangle
	 */
	public float getHeight(){
		if (corner1 == null || corner2 == null) return -1;
		Vector2f diag = getDiagonal();
		if (GeometryFunctions.compareFloat(diag.length(),0) == 0) return 0;
		return Math.abs(diag.length() * (float)Math.sin(diag.angle(rotation)));	//projects diagonal onto edge
	}
	
	/**********************************
	 * Returns the width of this rectangle, which is a distance along <code>rotation</code>.
	 * 
	 * @return the width of this rectangle
	 */
	public float getWidth(){
		if (corner1 == null || corner2 == null) return -1;
		Vector2f diag = getDiagonal();
		if (GeometryFunctions.compareFloat(diag.length(),0) == 0) return 0;
		return Math.abs(diag.length() * (float)Math.cos(diag.angle(rotation)));	//projects diagonal onto edge
	}
	
	protected Vector2f getDiagonal(){
		Vector2f diag = new Vector2f(corner2);
		diag.sub(corner1);
		return diag;
	}
	
	/***********************************************
	 * Returns the four corners of this rectangle, ordered as bottom left, bottom right, top right, and top left.
	 * 
	 * @return
	 */
	public Point2f[] getCorners(){
		Point2f[] corners = new Point2f[4];
		Vector2f diag = getDiagonal();
		
		corners[0] = corner1;
		corners[2] = corner2;
		float width = Math.abs(diag.length() * (float)Math.cos(diag.angle(rotation)));
		Vector2f offset = new Vector2f(rotation);
		offset.scale(width);
		corners[1] = new Point2f(corner1);
		corners[1].add(offset);
		offset.set(corner2);
		offset.sub(corners[1]);
		corners[3] = new Point2f(corner1);
		corners[3].add(offset);
		return corners;
	}
	
	/***********************************
	 * Returns the corner specified by <code>corner</code>, which must be one of the constants <code>CNR_BL</code>,
	 * <code>CNR_TL</code>, <code>CNR_BR</code>, or <code>CNR_TR</code>.
	 * 
	 * @param corner
	 * @return the coordinates of the specified corner
	 */
	public Point2f getCorner(int corner){
		//determine four corners
		Point2f[] corners = getCorners();
		
		switch (corner){
			case CNR_BL:
				return corners[0];
			case CNR_TL:
				return corners[3];	
			case CNR_BR:
				return corners[1];	
			case CNR_TR:
				return corners[2];
			}
		return null;
	}
	
	public ArrayList<LineSegment2D> getSides(){
		ArrayList<LineSegment2D> sides = new ArrayList<LineSegment2D>();
		
		Point2f[] corners = getCorners();
		sides.add(new LineSegment2D(corners[0], corners[1]));
		sides.add(new LineSegment2D(corners[1], corners[2]));
		sides.add(new LineSegment2D(corners[2], corners[3]));
		sides.add(new LineSegment2D(corners[3], corners[0]));

		return sides;
		
	}
	
	@Override
	public boolean contains(Point2f point){
		Vector2f v = new Vector2f(point);
		
		Vector2f diag = getDiagonal();
		v.sub(corner1);
		
		if (v.length() > diag.length()) return false;
		if (v.angle(rotation) > Math.PI / 2.0) return false;
		if (Math.abs(v.length() * (float)Math.cos(v.angle(rotation))) > getWidth()) return false;
		if (Math.abs(v.length() * (float)Math.sin(v.angle(rotation))) > getHeight()) return false;
		
		return true;
	}
	
	@Override
	public Point2f getCenterPt(){
		Vector2f diag = getDiagonal();
		diag.scale(0.5f);
		Point2f pt = new Point2f(corner1);
		pt.add(diag);
		return pt;
	}
	
	public float getDiagonalLength(){
		return getDiagonal().length();
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		ArrayList<Point2f> nodes = new ArrayList<Point2f>();
		Point2f[] corners = getCorners();
		for (int i = 0; i < 4; i++)
			nodes.add(corners[i]);
		return nodes;
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		//TODO: implement me
	}
	
	@Override
	public Point2f getVertex(int i) {
		return getCorner(i);
	}
	
	
	
	@Override
	public Object clone(){
		return new Rect2D(corner1.x, corner1.y, corner2.x, corner2.y, rotation);
	}
}