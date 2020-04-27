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

import org.jogamp.vecmath.Point3f;

import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.xml.XMLFunctions;

/******************************************
 * Represents a set of 3D points.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PointSet3D extends Shape3D {

	public float[] nodes;
	public int n = 0;
	
	public PointSet3D(){
		
	}
	
	public PointSet3D(float[] nodes){
		this.nodes = nodes;
		n = nodes.length / 3;
	}
	
	public PointSet3D(PointSet3D set_to_copy){
		setFromPointSet(set_to_copy);
	}
	
	public int addVertex(Point3f vertex){
		
		resizeNodesArray();
		nodes[(n * 3)] = vertex.x;
		nodes[(n * 3) + 1] = vertex.y;
		nodes[(n * 3) + 2] = vertex.z;

		n++;
		
		return n - 1;
	}
	
	public void clear(){
		nodes = new float[30];
		n = 0;
	}
	
	private void resizeNodesArray(){
		if (nodes == null || nodes.length == 0){
			nodes = new float[30];
			return;
			}
			
			
		if (n >= (nodes.length / 3) - 1){
			//new array needed
			//Point3f[] buffer = new Point3f[n * 2];
			//int factor = n;
			//if (factor == 0) factor = 5;
			float[] buffer = new float[(n + 1) * 6];
			if (nodes.length > 0)
			System.arraycopy(nodes, 0, buffer, 0, nodes.length);
			nodes = buffer;
			}
	}
	
	public void removeNode(int i){
		for (int a = i; a < n - 1; a++){
			nodes[a * 3] = nodes[(a * 3) + 3];
			nodes[(a * 3) + 1] = nodes[(a * 3) + 4];
			nodes[(a * 3) + 2] = nodes[(a * 3) + 5];
			}
		n--;
	}
	
	@Override
	public ArrayList<Point3f> getVertices(){
		ArrayList<Point3f> retNodes = new ArrayList<Point3f>(n);
		for (int i = 0; i < n; i++)
			retNodes.add(new Point3f(nodes[i * 3], nodes[(i * 3) + 1], nodes[(i * 3) + 2]));
		return retNodes;
	}
	
	@Override
	public float[] getCoords(){
		return nodes;
	}
	
	@Override
	public void finalize(){
		float[] nBuffer = new float[(n * 3)];
		System.arraycopy(nodes, 0, nBuffer, 0, n * 3);
		nodes = nBuffer;
		
	}
	
	@Override
	public Point3f getVertex(int i){
		return new Point3f(nodes[i * 3], nodes[(i * 3) + 1], nodes[(i * 3) + 2]);
	}
	
	public void setVertex(int i, Point3f p){
		nodes[i * 3] = p.x;
		nodes[(i * 3) + 1] = p.y;
		nodes[(i * 3) + 2] = p.z;
	}
	
	public void addVertices(ArrayList<Point3f> list){
		int m = n;
		n += list.size(); // * 3;
		
		resizeNodesArray();
		for (int i = 0; i < list.size(); i++){
			nodes[(m + i) * 3] = list.get(i).x;
			nodes[((m + i) * 3) + 1] = list.get(i).y;
			nodes[((m + i) * 3) + 2] = list.get(i).z;
			}
	}
	
	/***********************************
	 * T must be 4 x 4 transformation matrix
	 *
	public boolean transform(Matrix T){
		
		for (int i = 0; i < n; i++){
			
			Matrix node = arMath.getMatrixFromPoint3d(new Point3d(getNode(i)));
			node = node.transpose().times(T);
			Point3f p = new Point3f(arMath.getPoint3dFromMatrix(node));
			this.setNode(i, p);
			
			}
		
	}
	*/
	
	@Override
	public Object clone(){
		return new PointSet3D(this);
	}

	public void setFromPointSet(PointSet3D set){
		n = set.n;
		nodes = new float[n * 3];
		System.arraycopy(set.nodes, 0, nodes, 0, n * 3);
	}
	
	public void writeXML(int tab, Writer writer, InterfaceProgressBar progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		String _tab3 = XMLFunctions.getTab(tab + 2);
		
		writer.write(_tab + "<PointSet3D>\n");
		
		writer.write(_tab2 + "<Nodes count = '" + n + "'>\n");
		for (int i = 0; i < nodes.length; i += 3)
			writer.write(_tab3 + nodes[i] + "," + nodes[i + 1] + "," + nodes[i + 2] + "\n");
		writer.write(_tab2 + "</Nodes>\n");
			
		writer.write(_tab + "</PointSet3D>\n");
		
	}
	
	@Override
	public void setCoords(float[] coords) {
		nodes = new float[coords.length];
		System.arraycopy(coords, 0, nodes, 0, coords.length);
		n = coords.length / 3;
	}

	@Override
	public void setVertices(ArrayList<Point3f> list) {
		n = list.size();
		nodes = new float[n * 3];
		for (int i = 0; i < n; i++){
			nodes[i * 3] = list.get(i).x;
			nodes[(i * 3) + 1] = list.get(i).y;
			nodes[(i * 3) + 2] = list.get(i).z;
			}
	}
	
	
}