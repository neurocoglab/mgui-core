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

import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point2f;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;

import Jama.Matrix;

/***********************************************
 * Base class to be extended by all 2D shapes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class Shape2D implements Shape, Cloneable {

	public float getProximity(Point2f thisPoint) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public Point2f getProximityPoint(Point2f thisPoint) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean transform(Matrix4d M){
		return false;
	}
	
	public boolean contains(Point2f thisPoint){
		return false;
	}
	
	public Point2f getCenterPt(){
		return null;
	}
	
	public abstract Point2f getVertex(int i);
	public abstract ArrayList<Point2f> getVertices();
	public void setVertices(Point2f[] n){ }
	public abstract void setVertices(ArrayList<Point2f> n);
	
	public void getVertices(ArrayList<Point2f> theNodes){
	}
	
	/******************************
	 * Returns the number of vertices in this shape. Subclasses can provide more efficient implementations. 
	 * 
	 * @return the number of vertices
	 */
	public int getSize(){
		ArrayList<Point2f> nodes = getVertices();
		if (nodes == null) return 0;
		return nodes.size();
	}
	
	/******************************************
	 * Returns a rectangle which bounds this 2D shape
	 * 
	 * @return
	 */
	public Rect2D getBounds(){
		ArrayList<Point2f> nodes = getVertices();
		Point2f p1, p2;
		float xMin = Float.MAX_VALUE, yMin = Float.MAX_VALUE;
		float xMax = -Float.MAX_VALUE, yMax = -Float.MAX_VALUE;
		for (int i = 0; i < nodes.size(); i++){
			p1 = nodes.get(i);
			if (p1.x < xMin) xMin = p1.x;
			if (p1.y < yMin) yMin = p1.y;
			if (p1.x > xMax) xMax = p1.x;
			if (p1.y > yMax) yMax = p1.y;
			}
		p1 = new Point2f(xMin, yMin);
		p2 = new Point2f(xMax, yMax);
		return new Rect2D(p1, p2);
	}
	
	/**************************
	 * Returns the coordinates of this shape as an array of length {@code 2*n}. Every two sequential
	 * elements in the array represents a coordinate. 
	 * 
	 * <p>All shape classes which implement coordinates must implement this method.
	 * 
	 */
	public float[] getCoords(){return null;}
	
	/**************************
	 * Sets the coordinates of this shape from {@code coords}. Every two sequential
	 * elements in the array represents a coordinate. 
	 * 
	 * <p>All shape classes which implement coordinates must implement this method.
	 * 
	 * @param coords An array of length {@code 2*n}
	 */
	public void setCoords(float[] coords){}
	
	@Override
	public Object clone(){
		return null;
	}

	public boolean transform(Matrix T){
		InterfaceSession.log("Transform not implemented for class " + this.getClass().getCanonicalName());
		return false;
	}
	
	public String getDTD() {
		
		return null;
	}

	public String getLocalName() {
		return "Shape2D";
	}

	public String getXML() {
		return getXML(0);
	}

	public String getXML(int tab) {
		return "";
	}

	public String getXMLSchema() {
		
		return null;
	}

	public void handleXMLElementEnd(String localName) {
		
	}

	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
	}

	public void handleXMLString(String s) {
		
	}

	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
	}
	
	public String getShortXML(int tab) {
		return XMLFunctions.getTab(tab) + "<" + getLocalName() + " />";
	}
	
}