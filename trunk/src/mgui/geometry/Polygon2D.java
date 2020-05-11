/*
* Copyright (C) 2014 Andrew Reid and the ModelGUI Project <http://mgui.wikidot.com>
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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Tuple2f;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.xml.XMLFunctions;

import org.xml.sax.Attributes;

/********************************************
 * 
 * Represents a series of connected 2D points, defining either a polyline
 * or a closed polygon.
 * 
 * @author Andrew Reid
 * @since 1.0
 * @version 1.0
 *
 */
public class Polygon2D extends Shape2D {
	//variables for: nodes, as ArrayList

	public ArrayList<Point2f> vertices;
	public ArrayList<Boolean> segments;
	public ArrayList<Boolean> render_vertex;
	
	public Polygon2D(){
		vertices = new ArrayList<Point2f>();
		segments = new ArrayList<Boolean>();
		render_vertex = new ArrayList<Boolean>();
	}
	
	public Polygon2D(ArrayList<Point2f> nodeList){
		vertices = nodeList;
		segments = new ArrayList<Boolean>();
		render_vertex = new ArrayList<Boolean>();
		for (int i = 0; i < vertices.size(); i++){
			segments.add(true);
			render_vertex.add(true);
			}
	}
	
	public Polygon2D(ArrayList<Point2f> vertices, ArrayList<Boolean> segments){
		this.vertices = vertices;
		this.segments = segments;
		render_vertex = new ArrayList<Boolean>();
		for (int i = 0; i < vertices.size(); i++){
			render_vertex.add(true);
			}
	}
	
	public void setRenderVertices(ArrayList<Boolean> render){
		this.render_vertex = new ArrayList<Boolean>(render);
	}
	
	@Override
	public float[] getCoords(){
		float[] coords = new float[vertices.size()*3];
		Point2f p = new Point2f();
		for (int i = 0; i < vertices.size(); i++){
			p.set(vertices.get(i));;
			coords[i*2] = p.getX();
			coords[(i+1)*2] = p.getY();
			}
		return coords;
	}
	
	@Override
	public void setCoords(float[] coords){
		vertices = new ArrayList<Point2f>();
		segments = new ArrayList<Boolean>();
		for (int i = 0; i < coords.length; i+=2){
			this.addVertex(new Point2f(coords[i], coords[i+1]));
			}
	}
	
	@Override
	public Point2f getCenterPt(){
		return Polygon2D.getCenterOfGravity(this);
	}
	
	@Override
	public Point2f getVertex(int i){
		return vertices.get(i);
	}
	
	public Point2f getNextVertex(int i){
		if (i == vertices.size() - 1)
			return vertices.get(0);
		return vertices.get(i + 1);
	}
	
	@Override
	public int getSize(){
		return vertices.size();
	}
	
	public Point2f getPrevVertex(int i){
		if (i == 0)
			return vertices.get(vertices.size() - 1);
		return vertices.get(i - 1);
	}
	
	public Point2f getFirstVertex(){
		return vertices.get(0);
	}
	
	public Point2f getLastVertex(){
		return vertices.get(vertices.size() - 1);
	}
	
	/************************
	 * Adds a vertex.
	 * 
	 * @param p
	 * @param include
	 */
	public void addVertex(Point2f p){
		addVertex(p,true);
	}
	
	/************************
	 * Adds a vertex. {@code include} determines whether the segment i-1, i is included in this polygon
	 * 
	 * @param p
	 * @param include_segment Whether to include the previous segment
	 */
	public void addVertex(Point2f p, boolean include_segment){
		addVertex(p, include_segment, true);
	}
	
	/************************
	 * Adds a vertex. {@code include} determines whether the segment i-1, i is included in this polygon
	 * 
	 * @param p
	 * @param include_segment Whether to include the previous segment
	 * @param render_vertex   Whether this vertex should be rendered
	 */
	public void addVertex(Point2f p, boolean include_segment, boolean render_vertex){
		vertices.add(new Point2f(p));
		segments.add(include_segment);
		this.render_vertex.add(render_vertex);
	}
	
	/************************
	 * Removes the vertex at idx
	 * 
	 * @param idx
	 */
	public void removeVertex(int idx){
		vertices.remove(idx);
		segments.remove(idx);
		render_vertex.remove(idx);
	}
	
	@Override
	public boolean contains(Point2f pt){
		return GeometryFunctions.isInternalConcave(this, pt);
	}
	
	/**
	 * @param thisPoly
	 * @return a Point2f object that is the center of gravity for this polygon
	 */
	public static Point2f getCenterOfGravity(Polygon2D thisPoly){
		//sum x's and y's
		float sumX = 0, sumY = 0;
		int polySize = thisPoly.vertices.size();
		for (int i = 0; i < polySize; i++){
			sumX += thisPoly.vertices.get(i).x;
			sumY += thisPoly.vertices.get(i).y;
		}
		//divide by no. nodes
		return new Point2f(sumX / polySize, sumY / polySize);
	}
	
	@Override
	public Object clone(){
		ArrayList<Point2f> newNodes = new ArrayList<Point2f>(vertices.size());
		for (int i = 0; i < vertices.size(); i++)
			newNodes.add(new Point2f((Tuple2f)vertices.get(i).clone()));
		if (newNodes.get(0).x == Float.NaN)
			return null;
		return new Polygon2D(newNodes);
	}
	
	@Override
	public Point2f getProximityPoint(Point2f thisPt){
		int j;
		float nearest = Float.MAX_VALUE;
		float thisDist;
		LineSegment2D thisLine;
		Point2f retPoint = null;
		
		for (int i = 0; i < vertices.size(); i++){
		j = i + 1;
		if (j == vertices.size()) j = 0;
		thisLine = new LineSegment2D(vertices.get(i), vertices.get(j));
			thisDist = thisLine.getProximity(thisPt);
			if (nearest > thisDist && thisDist > 0){
				nearest = thisDist;
				retPoint = thisLine.getProximityPoint(thisPt);
				}
			}
		return retPoint;
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		return vertices;
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		vertices = new ArrayList<Point2f>(vertices.size());
		for (int i = 0; i < vertices.size(); i++)
			vertices.add(new Point2f(vertices.get(i)));
	}
	
	@Override
	public float getProximity(Point2f thisPt){
		//need to determine perpendicular distance from each edge to this point
		int j;
		float nearest = Float.MAX_VALUE;
		float thisDist;
		
		for (int i = 0; i < vertices.size(); i++){
		j = i + 1;
		if (j == vertices.size()) j = 0;
			thisDist = new LineSegment2D(vertices.get(i), vertices.get(j)).getProximity(thisPt);
			if (nearest > thisDist && thisDist > 0)
				nearest = thisDist;
			}
		return nearest;
	}
	
	public void writeXML(int tab, Writer writer, InterfaceProgressBar progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		writer.write(_tab + "<Nodes n='" + vertices.size() + "'>\n");
		
		for (int i = 0; i < vertices.size(); i++)
			writer.write(_tab2 + XMLFunctions.getXMLForPoint2f(vertices.get(i), 8) + "\n");
		
		writer.write(_tab + "</Nodes>\n");
		
	}
	
	boolean start_xml = false;
	
	@Override
	public void handleXMLElementEnd(String localName) {
		if (localName.equals(getLocalName())){
			start_xml = false;
			}
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
		if (localName.equals(getLocalName())){
			start_xml = true;
			}
		
	}

	@Override
	public void handleXMLString(String s) {
		if (!start_xml) return;
		
		addVertex(XMLFunctions.getPoint2f(s));
		
	}
	
}