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



import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
import org.jogamp.vecmath.Vector4d;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;

/********
 * Represents a plane in 3 dimensions, using a Point3d and a Vector3d (normal)
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Plane3D extends Shape3D {

	

	public Point3f origin;
	public Vector3f xAxis, yAxis;
	public boolean flip_normal = false;
	
	public Plane3D(){
		origin = new Point3f();
		xAxis = new Vector3f(1, 0, 0);
		yAxis = new Vector3f(0, 1, 0);
	}
	
	public Plane3D(Point3f pt, Vector3f x, Vector3f y){
		this(pt, x, y, false);
	}
	
	//construct from a point and a normal vector
	public Plane3D(Point3f pt, Vector3f x, Vector3f y, boolean flip_normal){
		origin = pt;
		xAxis = x;
		yAxis = y;
		xAxis.normalize();
		yAxis.normalize();
		this.flip_normal = flip_normal;
	}
	
	public Plane3D(Plane3D plane){
		origin = new Point3f(plane.origin);
		xAxis = new Vector3f(plane.xAxis);
		yAxis = new Vector3f(plane.yAxis);
		flip_normal = plane.flip_normal;
	}
	
	public Point3f getOrigin(){
		return new Point3f(origin);
	}
	
	public void setOrigin(Point3f p){
		this.origin = new Point3f(p);
	}
	
	public Vector3f getAxisX(){
		return new Vector3f(xAxis);
	}
	
	public Vector3f getAxisY(){
		return new Vector3f(yAxis);
	}
	
	public void setAxisX(Vector3f v){
		this.xAxis = new Vector3f(v);
	}
	
	public void setAxisY(Vector3f v){
		this.yAxis = new Vector3f(v);
	}
	
	//construct from three points
	public Plane3D(Point3f pt1, Point3f pt2, Point3f pt3){
		origin = pt1;
		xAxis = new Vector3f(pt2.x - pt1.x, pt2.y - pt1.y, pt2.z - pt1.z);
		yAxis = new Vector3f(pt3.x - pt1.x, pt3.y - pt1.y, pt3.z - pt1.z);
		//normal.normalize(GeometryFunctions.getCrossProduct(v1, v2));
		xAxis.normalize();
		yAxis.normalize();
	}
	
	/***************************
	 * Return a plane containing point <code>x0</code>, having normal <code>normal</code>,
	 * and having a Y axis which is (0, 1, 0) projected onto the plane.
	 *  
	 * @param x0
	 * @param normal
	 * @return
	 */
	public static Plane3D getPlaneFromNormalAndY(Point3f x0, Vector3f normal){
		return getPlaneFromNormalAndY(x0, normal, new Vector3f(0, 1, 0));
	}
	
	/***************************
	 * Return a plane containing point <code>x0</code>, having normal <code>normal</code>,
	 * and having a Y axis which is <code>Y</code> projected onto the plane.
	 *  
	 * @param x0
	 * @param normal
	 * @param Y
	 * @return
	 */
	public static Plane3D getPlaneFromNormalAndY(Point3f x0, Vector3f normal, Vector3f Y){
		normal.normalize();
		//1. solve for d in plane equation
		float d = -normal.x * x0.x - normal.y * x0.y - normal.z * x0.z;
		
		//2. Py from Y
		Point3f Py = new Point3f(x0);
		Py.add(Y);
		
		//3. solve for z'
		float z_p = -normal.x * Py.x - normal.y * Py.y - d;
		
		//4. Y-axis is Y' minus x0
		Vector3f yAxis = new Vector3f(Py.x, Py.y, z_p);
		yAxis.sub(x0);
		
		//5. X-axis is cross-product of normal and Y-axis
		Vector3f xAxis = new Vector3f();
		xAxis.cross(normal, Y);
		
		return new Plane3D(x0, xAxis,yAxis);
	}
	
	/******************************
	 * Returns the normal vector, as a unit vector, for this plane.
	 * 
	 * @return
	 */
	public Vector3f getNormal(){
		Vector3f normal = new Vector3f();
		normal.cross(xAxis, yAxis);
		if (flip_normal) normal.scale(-1);
		normal.normalize();
		return normal;
	}
	
	/************************
	 * Return plane as a Vector4d defining the plane equation:
	 * 
	 * <p>Ax + By + Cz + D = 0
	 * 
	 * @return
	 */
	public Vector4d getAsVector4d(){
		
		Vector3f normal = getNormal();
		double A = normal.x;
		double B = normal.y;
		double C = normal.z;
		double D = -(A * origin.x + B * origin.y + C * origin.z);
		
		return new Vector4d(A, B, C, D);
	}
	
	@Override
	public Object clone(){
		Point3f p = new Point3f(origin);
		Vector3f x = new Vector3f(xAxis);
		Vector3f y = new Vector3f(yAxis);
		return new Plane3D(p, x, y, flip_normal);
	}
	
	@Override
	public String toString(){
		String s = "Plane3D:\nBase pt: (" + origin.x + ", " + origin.y + ", " + origin.z +")";
		s += "\nX-axis: (" + xAxis.x + ", " + xAxis.y + ", " + xAxis.z +")";
		s += "\nY-axis: (" + yAxis.x + ", " + yAxis.y + ", " + yAxis.z +")";
		return s;
	}
	
	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		String _tab3 = XMLFunctions.getTab(tab + 2);
		
		String xml = _tab + "<Plane3D>\n";
		
		xml = xml + _tab2 + "<Origin>\n";
		xml = xml + _tab3 + XMLFunctions.getTupleStr(origin, 8);
		xml = xml + _tab2 + "\n</Origin>\n";
		xml = xml + _tab2 + "<X_Axis>\n";
		xml = xml + _tab3 + XMLFunctions.getTupleStr(xAxis, 8);
		xml = xml + _tab2 + "\n</X_Axis>\n";
		xml = xml + _tab2 + "<Y_Axis>\n";
		xml = xml + _tab3 + XMLFunctions.getTupleStr(yAxis, 8);
		xml = xml + _tab2 + "\n</Y_axis>\n";
		
		xml = xml + _tab + "</Plane3D>\n";
		
		return xml;
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<" + getLocalName() + " encoding='" + XMLFunctions.getEncodingStr(options.encoding) + "'\n");
		writeCoords(tab + 1, writer, options, progress_bar);
		writer.write(_tab + "/>");
		
	}
	
	@Override
	protected void writeCoords(int tab, Writer writer, XMLOutputOptions options,  ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		writer.write(_tab + "origin = '" + XMLFunctions.getTupleStr(origin, 8) + "'\n");
		writer.write(_tab + "x_axis = '" + XMLFunctions.getTupleStr(xAxis, 8) + "'\n");
		writer.write(_tab + "y_axis = '" + XMLFunctions.getTupleStr(yAxis, 8) + "'\n");
		writer.write(_tab + "z_axis = '" + XMLFunctions.getTupleStr(getNormal(), 8) + "'\n");
		
	}
	
	@Override
	public void handleXMLElementEnd(String localName) {
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
		if (localName.equals(getLocalName())){
			origin = XMLFunctions.getPoint3f(attributes.getValue("origin"));
			xAxis = XMLFunctions.getVector3f(attributes.getValue("x_axis"));
			yAxis = XMLFunctions.getVector3f(attributes.getValue("y_axis"));
			Vector3f normal = XMLFunctions.getVector3f(attributes.getValue("z_axis"));
			if (!normal.equals(getNormal())) this.flip_normal = true;
			}
		
	}

	@Override
	public void handleXMLString(String s) {
		
		
	}
	
	//not implemented since Plane3D is not a finite shape
	@Override
	public String getLocalName(){
		return "Plane3D";
	}
	
	@Override
	public float[] getCoords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point3f getVertex(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Point3f> getVertices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCoords(float[] f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVertices(ArrayList<Point3f> n) {
		// TODO Auto-generated method stub
		
	}
	
	
}