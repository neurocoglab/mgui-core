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

package mgui.interfaces.shapes;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.TreeSet;

import org.jogamp.vecmath.Point2f;

import mgui.geometry.Mesh2D;
import mgui.geometry.PointSet2D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Rect2D;
import mgui.geometry.util.NodeShape;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.logs.LoggingType;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.util.Colour;

/*******************************************************
 * A 2D representation of a {@link Mesh3DInt} object. Allows references to vertices of the parent mesh. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Mesh2DInt extends Shape2DInt {

	public ArrayList<Colour> colours;		//TODO: replace with reference to parent shape
	public ArrayList<Integer[]> indices;
	
	public Mesh2DInt(){
		super();
		init();
	}
	
	/****************************************
	 * Constructor for this <code>Mesh2DInt</code>.
	 * 
	 * @param edges		list of edges obtained from a parent mesh
	 */
	public Mesh2DInt(ArrayList<Point2f[]> edges){
		this(edges, null);
	}
	
	/****************************************
	 * Constructor for this <code>Mesh2DInt</code>.
	 * 
	 * @param edges		list of edges obtained from a parent mesh
	 * @param indices	list of indices corresponding to the vertices of the parent mesh (can be <code>null</code>)
	 */
	public Mesh2DInt(ArrayList<Point2f[]> edges, ArrayList<Integer[]> indices){
		super();
		
		setShape(new Mesh2D(edges));
		this.indices = indices;
		
		init();
	}
	
	private void init(){
		//add attributes here
		
	}
	
	public Mesh2D getMesh() {
		return (Mesh2D)thisShape;
	}
	
	public ArrayList<Point2f> getVertices(){
		Mesh2D mesh = getMesh();
		if (mesh == null) return null;
		return mesh.getVertices();
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		if (!isVisible()) return;
		
		Mesh2D mesh = getMesh();
		
		if (mesh == null){
			InterfaceSession.log("No edges to draw!", LoggingType.Debug);
			return;
			}
		
		float alpha = -1;
		if (((MguiBoolean)attributes.getValue("2D.HasAlpha")).getTrue())
			alpha = ((MguiFloat)attributes.getValue("2D.Alpha")).getFloat();
		
		if (((MguiBoolean)attributes.getValue("2D.HasFill")).getTrue()) {
			ArrayList<Polygon2D> polygons = mesh.getPolygons();
			
			if (polygons.size() > 0) {
				for (Polygon2D polygon : polygons) {
					d.drawPolygon2D(g, polygon);
					}
				//return;
				}
			}
		
		if (colours == null) {
			d.drawMesh2D(g, mesh, alpha);
		} else {
			d.drawMesh2D(g, mesh, colours, alpha);
			}
		
		if (((MguiBoolean)attributes.getValue("2D.ShowVertices")).getTrue()){
			PointSet2D points = new PointSet2D(mesh.getCoords());
			float scale = (((MguiFloat)attributes.getValue("2D.VertexScale")).getFloat());
			NodeShape shape = this.getVertexShape();
			d.drawPointSet2D(g, points, scale, shape, null, alpha);
		}
	
	}
	
	
	@Override
	public boolean needsRedraw(Attribute<?> a){
		//is it visible?
		
		if (a.getName().equals("2D.LineColour") ||
			a.getName().equals("2D.LineStyle") ||
			a.getName().equals("2D.HasAlpha") ||
			a.getName().equals("2D.Alpha")) 
			return true;
		
		return super.needsRedraw(a);
	}
	
	public void setColours(ArrayList<Colour> c){
		colours = c;
	}
	
//	public void merge(Mesh2DInt mesh_to_merge){
//		if (mesh_to_merge == null) return;
//		Mesh2D mesh = mesh_to_merge.getMesh();
//		if (mesh == null) {
//			setMesh(merge_to_merge);
//			edges = new_edges;
//		} else {
//			edges.addAll(new_edges);
//			}
//	}
//	
//	public void addEdge(Point2f[] edge){
//		edges.add(edge);
//	}
	
	@Override
	public void updateShape(){
		float maxX = Float.MIN_VALUE, minX = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE, minY = Float.MAX_VALUE;
		float xSum = 0, ySum = 0;
		
		Mesh2D mesh = getMesh();
		ArrayList<Point2f[]> edges = mesh.getEdges();
		
		for (int i = 0; i < edges.size(); i++)
			for (int j = 0; j < 2; j++){
				maxX = Math.max(edges.get(i)[j].x, maxX);
				maxY = Math.max(edges.get(i)[j].y, maxY);
				minX = Math.min(edges.get(i)[j].x, minX);
				minY = Math.min(edges.get(i)[j].y, minY);
				xSum += edges.get(i)[j].x;
				ySum += edges.get(i)[j].y;
				}
		
		if (centerPt == null) centerPt = new Point2f();
		centerPt.x = xSum / (edges.size() * 2);
		centerPt.y = ySum / (edges.size() * 2);
		
		if (bounds == null )
			bounds = new Rect2D(minX, minY, maxX, maxY);
		else{
			bounds.corner1.x = minX;
			bounds.corner1.y = minY;
			bounds.corner2.x = maxX;
			bounds.corner2.y = maxY;
		}
	}
}