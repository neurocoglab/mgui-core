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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import org.xml.sax.SAXException;

import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiDouble;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/*******************
 * Geometry class to represent a 3D triangular mesh. This is implemented as an array
 * of nodes and an array of MeshFace3D objects, which hold the indices of the three nodes
 * of the triangular face. Normals are generated here, as in Java3D, by the policy that
 * clockwise triangles have up-facing normals.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */

public class Mesh3D extends Shape3D {

	public float[] nodes = new float[30];
	public int[] faces = new int[10];
	public int n = 0, f = 0;
	
	public Mesh3D(){
		
	}
	
	public Mesh3D(Mesh3D mesh_to_copy){
		setFromMesh(mesh_to_copy);
	}
	
	public boolean addVertex(Point3f node){
		
		try{
			resizeNodesArray();
			nodes[(n * 3)] = node.x;
			nodes[(n * 3) + 1] = node.y;
			nodes[(n * 3) + 2] = node.z;
	
			n++;
		}catch (Exception ex){
			return false;
			}
		
		return true;
		//return n - 1;
	}
	
	/*******************************************
	 * Returns the number of faces in this mesh.
	 * 
	 * @return
	 */
	public int getFaceCount(){
		return f;
	}
	
	public int getVertexCount(){
		return n;
	}
	
	public void clear(){
		nodes = new float[30];
		faces = new int[10];
		n = 0;
		f = 0;
	}
	
	@Override
	public int getSize(){
		return n;
	}
	
	private void resizeNodesArray(){
		if (nodes == null) nodes = new float[30];
		
		if (nodes.length == 0){
			nodes = new float[30];
			return;
			}
		if (n >= (nodes.length / 3) - 1){
			float[] buffer = new float[(n + 1) * 6];
			if (nodes.length > 0)
			System.arraycopy(nodes, 0, buffer, 0, nodes.length);
			nodes = buffer;
			}
	}
	
	private void resizeFacesArray(){
		if (faces.length == 0){
			faces = new int[30];
			return;
			}
		if (f >= (faces.length / 3) - 1){
			int[] buffer = new int[(f + 1) * 6];
			System.arraycopy(faces, 0, buffer, 0, faces.length);
			faces = buffer;
			}
	}
	
	/****************************************
	 * Set this mesh from another mesh.
	 * 
	 * @param mesh
	 */
	public void setFromMesh(Mesh3D mesh){
		n = mesh.n;
		f = mesh.f;
		nodes = new float[n * 3];
		faces = new int[f * 3];
		System.arraycopy(mesh.nodes, 0, nodes, 0, n * 3);
		System.arraycopy(mesh.faces, 0, faces, 0, f * 3);
	}
	
	/****************************************
	 * Add a triangular face to this mesh.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public boolean addFace(int a, int b, int c) {
		
		try {
			MeshFace3D face = new MeshFace3D();
			face.setIndices(a, b, c);
		}catch (Exception e){
			return false;
			}
		
		resizeFacesArray();
		faces[(f * 3)] = a;
		faces[(f * 3) + 1] = b;
		faces[(f * 3) + 2] = c;
		
		f++;
		
		return true;
	}
	
	/**********************************************
	 * Remove a vertex from this mesh, and any faces including it.
	 * 
	 * @param i
	 */
	public void removeVertex(int i){
		removeVertex(i, true);
	}
	
	/**********************************************
	 * Remove a vertex from this mesh. 
	 * 
	 * @param i
	 * @param remove_faces 		Also remove faces including this vertex?
	 */
	public void removeVertex(int i, boolean remove_faces){
		//complication is we need to find and remove all faces with i as nodes
		//plus decrement all indices >= i
		//nodes.remove(i);
		for (int a = i; a < n - 1; a++){
			nodes[a * 3] = nodes[(a * 3) + 3];
			nodes[(a * 3) + 1] = nodes[(a * 3) + 4];
			nodes[(a * 3) + 2] = nodes[(a * 3) + 5];
			}
		n--;
		for (int a = 0; a < f; a++){
			if (remove_faces && 
				(faces[a * 3] == i || faces[(a * 3) + 1] == i || faces[(a * 3) + 2] == i)){ 
				removeFace(a);
				a--;
			}else{
				if (faces[a * 3] > i) faces[a * 3]--;
				if (faces[(a * 3) + 1] > i) faces[(a * 3) + 1]--;
				if (faces[(a * 3) + 2] > i) faces[(a * 3) + 2]--;
				}
			}
	}
	
	/********************************
	 * Removes all vertices in this mesh. 
	 * 
	 * @param remove_faces  - Also removes the faces
	 */
	public void removeAllVertices(boolean remove_faces){
		nodes = new float[30];
		n = 0;
		
		if (remove_faces){
			faces = new int[10];
			f = 0;
			}
	}
	
	/********************************
	 * Removes all vertices in this mesh. Also removes the faces.
	 * 
	 */
	public void removeAllVertices(){
		removeAllVertices(true);
	}
	
	/********************************
	 * Remove a list of vertices. <code>removed</code> must be sorted ascending.
	 * @param removed
	 */
	public HashMap<Integer, Integer> removeVertices(TreeSet<Integer> removed){
		if (removed.size() == 0) return null;
		Mesh3D mesh_new = new Mesh3D();
		
		HashMap<Integer, Integer> index_map = new HashMap<Integer, Integer>(n);
		HashMap<Integer, Integer> reverse_map = new HashMap<Integer, Integer>(n);
		
		int k = 0;
		for (int i = 0; i < n; i++){
			if (removed.contains(i)){
				
			}else{
				mesh_new.addVertex(getVertex(i));
				index_map.put(i, k);
				reverse_map.put(k++, i);
				}
			}
		
		//for each face, add if each of its vertices is non zero
		for (int i = 0; i < f; i++){
			Mesh3D.MeshFace3D face = getFace(i);
			
			if (!(removed.contains(face.A) || 
				  removed.contains(face.B) ||
				  removed.contains(face.C))){
				//add extra vertices if necessary 
				
				mesh_new.addFace(index_map.get(face.A),
								 index_map.get(face.B),
								 index_map.get(face.C));
				
				}
			}
		
		setFromMesh(mesh_new);
		
		return reverse_map;
	}
	
	/********************************
	 * Remove a list of faces. <code>removed</code> must be sorted ascending.
	 * @param removed
	 */
	public void removeFaces(TreeSet<Integer> removed){
		
		if (removed.size() == 0) return;
		ArrayList<MeshFace3D> new_faces = new ArrayList<MeshFace3D> (f);
		for (int i = 0; i < f; i++)
			if (!removed.contains(i))
				new_faces.add(getFace(i));
				
		
		f = 0;
		faces = new int[new_faces.size() * 3];
		
		for (int i = 0; i < new_faces.size(); i++)
			addFace(new_faces.get(i));
		
	}
	
	public void removeFace(int i){
		//faces.remove(i);
		for (int a = i; a < f - 1; a++){
			faces[a * 3] = faces[(a * 3) + 3];
			faces[(a * 3) + 1] = faces[(a * 3) + 4];
			faces[(a * 3) + 2] = faces[(a * 3) + 5];
			}
		f--;
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
		float[] coords = new float[n*3];
		System.arraycopy(nodes, 0, coords, 0, n*3);
		return coords;
	}
	
	/************************************************
	 * Returns this face's vertices as {@link Point3f} objects.
	 * 
	 * @param n
	 * @return
	 */
	public ArrayList<Point3f> getFaceNodes(int i){
		MeshFace3D face = getFace(i);
		ArrayList<Point3f> retNodes = new ArrayList<Point3f>(3);
		for (int j = 0; j < 3; j++)
			retNodes.add(getVertex(face.getNode(j)));
		return retNodes;
	}
	
	/*******************************************************************
	 * Returns a 3D triangle for face {@code i}.
	 * 
	 * @param i
	 * @return
	 */
	public Triangle3D getFaceTriangle(int i){
		MeshFace3D face = getFace(i);
		Triangle3D tri = new Triangle3D();
		tri.A = getVertex(face.A);
		tri.B = getVertex(face.B);
		tri.C = getVertex(face.C);
		return tri;
	}
	
	@Override
	public void setVertices(ArrayList<Point3f> list){
		//nodes = n;
		n = list.size();
		nodes = new float[n * 3];
		for (int i = 0; i < n; i++){
			nodes[i * 3] = list.get(i).x;
			nodes[(i * 3) + 1] = list.get(i).y;
			nodes[(i * 3) + 2] = list.get(i).z;
			}
		
	}
	
	@Override
	public void setCoords(float[] coords) {
		nodes = new float[coords.length];
		System.arraycopy(coords, 0, nodes, 0, coords.length);
		n = coords.length / 3;
	}
	
	public void setFaces(ArrayList<MeshFace3D> list){
		
		f = list.size();
		faces = new int[f * 3];
		for (int i = 0; i < f; i++){
			faces[i * 3] = list.get(i).A;
			faces[(i * 3) + 1] = list.get(i).B;
			faces[(i * 3) + 2] = list.get(i).C;
			}
		
	}
	
	public int[] getFaceIndexArray(){
		return faces;
	}
	
	public ArrayList<MeshFace3D> getFaces(){
		if (f <= 0)
			return null;
		ArrayList<MeshFace3D> list = new ArrayList<MeshFace3D> (f);
		for (int i = 0; i < f; i++){
			list.add(new MeshFace3D(faces[i * 3],
									faces[i * 3 + 1],
									faces[i * 3 + 2]));
			}
		return list;
	}
	
	public void removeAllFaces(){
		faces = new int[0];
		f = 0;
	}
	
	@Override
	public void finalize(){
		float[] nBuffer = new float[(n * 3)];
		System.arraycopy(nodes, 0, nBuffer, 0, n * 3);
		nodes = nBuffer;
		int[] fBuffer = new int[(f * 3)];
		System.arraycopy(faces, 0, fBuffer, 0, f * 3);
		faces = fBuffer;
		
	}
	
	//is this face clockwise?
	public boolean isClockwiseFace(int i){
		return GeometryFunctions.isClockwise(getVertex(faces[i * 3]),
											 getVertex(faces[(i * 3) + 1]), 
											 getVertex(faces[(i * 3) + 2]));
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
	
	/**************************
	 * Flips face i
	 * @param i
	 */
	public void flipFace(int i){
		int t = faces[i * 3];
		faces[i * 3] = faces[i * 3 + 1];
		faces[i * 3 + 1] = t;
	}
	
	/**************************************************
	 * Returns the face at index {@code i}.
	 * 
	 * @param i
	 * @return
	 */
	public MeshFace3D getFace(int i){
		return new MeshFace3D(faces[i * 3], faces[(i * 3) + 1], faces[(i * 3) + 2]);
	}
	
	/****************************
	 * Returns the angle in face {@code face} at vertex {@code i}. Returns {@code Double.NaN}
	 * if the vertex or face indices are invalid, or if the vertex is not in this face
	 * 
	 * @param vertex
	 * @param face
	 * @return
	 */
	public double getFaceAngle(int i, int face){
		if (i < 0 || i >= n) return Double.NaN;
		MeshFace3D this_face = getFace(face);
		int idx = this_face.whichNode(i);
		if (idx < 0) return Double.NaN;
		Triangle3D tri = this.getFaceTriangle(face);
		float[] angles = tri.getAngles();
		return angles[idx];
	}
	
	/*****************************
	 * Adds a face to this mesh.
	 * 
	 * @param face
	 */
	public void addFace(MeshFace3D face){
		int m = f;
		f++;
		resizeFacesArray();
		faces[m * 3] = face.A;
		faces[(m * 3) + 1] = face.B;
		faces[(m * 3) + 2] = face.C;
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
	
	public void addFaces(ArrayList<MeshFace3D> list){
		int m = f;
		f += list.size(); 
		resizeFacesArray();
		for (int i = 0; i < list.size(); i++){
			MeshFace3D face = list.get(i);
			faces[(m + i) * 3] = face.A;
			faces[((m + i) * 3) + 1] = face.B;
			faces[((m + i) * 3) + 2] = face.C;
			}
	}
	
	public ArrayList<Vector3f> getNormals(){
		finalize();
		
		return MeshFunctions.getSurfaceNormals(this);
	
	}
	
	public Vector3f getNormalAtFace(int i){
		/**@TODO implement GeometryFunctions method to get normal from nodes **/
		
		MeshFace3D face = getFace(i);
		Vector3f v1 = new Vector3f(getVertex(face.B));
		v1.sub(getVertex(face.A));
		Vector3f v2 = new Vector3f(getVertex(face.C));
		v2.sub(getVertex(face.A));
		Vector3f normal = new Vector3f();
		normal.cross(v1, v2);
		//normal.normalize();
		
		return normal;
	}
	
	/***********************************
	 * T must be 4 x 4 transformation matrix
	 *
	public void transform(Matrix T){
		
		for (int i = 0; i < n; i++){
			
			Matrix node = arMath.getMatrixFromPoint3d(new Point3d(getNode(i)));
			node = node.transpose().times(T);
			Point3f p = new Point3f(arMath.getPoint3dFromMatrix(node));
			this.setNode(i, p);
			
			}
		
	}
	*/
	
	/***********************************************
	 * Inner class which defines a face in a mesh
	 * 
	 * @author Andrew Reid
	 *
	 */
	public class MeshFace3D{
		//indices of this face's nodes, in clockwise order of A, B, C
		public int A, B, C;
		
		public MeshFace3D(){
			
		}
		
		public MeshFace3D(int a, int b, int c){
			try{
				setIndices(a, b, c);
			}catch (Exception e){
				InterfaceSession.handleException(e, LoggingType.Errors);
				//e.printStackTrace();
				}
		}
		
		public MeshFace3D(MeshFace3D f){
			this(f.A, f.B, f.C);
		}
		
		public void setIndices(int a, int b, int c) throws Exception {
			if (a < 0 || b < 0 || c < 0) throw new Exception("MeshFace3D: bad indices [" + a + ", " + b + ", " + c + "]");
			A = a;
			B = b;
			C = c;
		}
		
		public int getNode(int i){
			switch(i){
				case 0: return A;
				case 1: return B;
				case 2: return C;
				}
			return -1;
		}
		
		/******************
		 * Returns the index in this face of the vertex at {@code index} (i.e., in the
		 * mesh). Returns {@code -1} if the vertex is not in this face.
		 * 
		 * @param index
		 * @return
		 */
		public int whichNode(int index){
			if (index == A) return 0;
			if (index == B) return 1;
			if (index == C) return 2;
			return -1;
		}
		
		public void setNode(int i, int index){
			switch(i){
				case 0: 
					A = index;
					return;
				case 1: 
					B = index;
					return;
				case 2: 
					C = index;
					return;
				}
		}
		
		public boolean hasNode(int i){
			return A == i || B == i || C == i;
		}
		
		//returns false if nodes are non-distinct
		public boolean isValid(){
			return !(A == B || A == C || B == C);
		}
		
		//reverse clockwise direction of this triangle
		public void reverse(){
			int T = A;
			A = B;
			B = T;
		}
		
		/****************************************************
		 * Determines whether {@code face} shares at least one edge (two vertices) with this face.
		 * 
		 * @param face
		 * @return
		 */
		public boolean isAdjacent(MeshFace3D face){
			int count = 0;
			
			for (int i = 0; i < 3; i++){
				int a = this.getNode(i);
				for (int j = 0; j < 3; j++){
					if (a == face.getNode(j)) count++;
					if (count == 2) 
						return true;
					}
				}
			
			return false;
		}
		
	}
	
	@Override
	public Object clone(){
		return new Mesh3D(this);
	}
	
	@Override
	public String getLocalName() {
		return "Mesh3D";
	}
	
	@Override
	public String getXML(int tab) {
		
		return "<Mesh3D> Exception: use writeXML() to write Mesh3D to XML </Mesh3D>";
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		String _tab3 = XMLFunctions.getTab(tab + 2);
		
		writer.write(_tab + "<" + getLocalName() + " encoding='" + XMLFunctions.getEncodingStr(options.encoding) + "' >\n");
		
		writeCoords(tab+1, writer, options, progress_bar);
		
		writer.write("\n" + _tab2 + "<Faces count = '" + f + "'>\n");
		
		// Write data in specified format
		switch (options.encoding){
			case Base64BinaryGZipped:
//				writeBinaryFaces(writer, 2);
//				break;
			case Base64BinaryZipped:
				writeBinaryFaces(writer, 1);
				break;
			case Base64Binary:
				writeBinaryFaces(writer, 0);
				break;
			case Ascii:
				writeAsciiFaces(writer, _tab3);
				break;
			}
		writer.write("\n" + _tab2 + "</Faces>\n");
		
		writer.write(_tab + "</" + getLocalName() + ">\n");
		
	}
	
	@Override
	public void handleXMLString(String s) throws SAXException{
		
		if (xml_current_block != null && xml_current_block.equals("Faces")){
			if (xml_encoding == null)
				throw new SAXException("Shape3D.handleXMLElementStart: Faces data received but no encoding set.");
			
			switch(xml_encoding){
				case Ascii:
					loadAsciiFaces(s);
					break;
				case Base64Binary:
					loadBinaryFaces(s, 0);
					break;
				case Base64BinaryZipped:
				case Base64BinaryGZipped:
					loadBinaryFaces(s, 1);
					break;
//				case Base64BinaryGZipped:
//					loadBinaryFaces(s, 2);
//					break;
				}
			
			return;
			}
		
		super.handleXMLString(s);
		
	}
	
	/*******************************************
	 * Write mesh faces as Base64 encoded binary data to an XML writer, in row major order.
	 * 
	 * @param mesh
	 * @param encoding
	 * @param compress Compression; 0 for none, 1 for zip, 2 for gzip
	 */
	protected void writeBinaryFaces(Writer writer, int compress) throws IOException{
		
		ByteBuffer data_out = ByteBuffer.allocate(getFaceCount() * 3 * 4);
		
		// First encode as raw bytes
		for (int i = 0; i < getFaceCount(); i++){
			MeshFace3D face = getFace(i);
			data_out.putInt(face.A);
			data_out.putInt(face.B);
			data_out.putInt(face.C);
			}
		
		// Now compress if necessary
		if (compress == 1){
			byte[] b = IoFunctions.compressZipped(data_out.array());
			data_out = ByteBuffer.wrap(b);
		}else if (compress == 2){
			byte[] b = IoFunctions.compressGZipped(data_out.array());
			data_out = ByteBuffer.wrap(b);
			}
		
		// Now encode as Base64
		// Finally, write string to file
		writer.write(Base64.encode(data_out.array()));
		
	}
	
	/*******************************************
	 * Write faces as ASCII data to an XML writer; formats to {@code decimals} decimal places. Wraps
	 * line when it exceeds 76 characters.
	 * 
	 * @param mesh
	 * @param encoding
	 */
	protected void writeAsciiFaces(Writer writer, String tab) throws IOException{
		
		String line;
		int length = 0;
		writer.write(tab);
		
		for (int i = 0; i < getFaceCount(); i++){
			MeshFace3D face = getFace(i);
			line = face.A + " " + face.B + " " + face.C;
			length += line.length();
			if (length > 76 && i < getFaceCount() - 1){
				line = line + "\n" + tab;
				length = 0;
			}else{
				line = line + " ";
				}
			writer.write(line);
			}
		
	}
	
	/********************************************
	 * Load vertices from Base64 binary encoded data.
	 * 
	 * @param data
	 * @param compression Compression; 0 for none, 1 for zip, 2 for gzip
	 */
	protected void loadBinaryFaces(String data, int compression){
		
		try{
			// Decode
			Charset charset = Charset.forName("UTF-8");
			byte[] utf8_bytes = data.getBytes(charset);
			byte[] b_data = Base64.decode(utf8_bytes);
			
			// Decompress
			switch (compression){
				case 1: 
					b_data = IoFunctions.decompressZipped(b_data);
					break;
				case 2: 
					b_data = IoFunctions.decompressGZipped(b_data);
					break;
				}
			
			// Convert to vertices
			ArrayList<MeshFace3D> faces = new ArrayList<MeshFace3D>();
			ByteBuffer buffer = ByteBuffer.wrap(b_data);
			while (buffer.hasRemaining()){
				MeshFace3D face = new MeshFace3D(buffer.getInt(),
												 buffer.getInt(),
												 buffer.getInt());
				faces.add(face);
				}
			this.setFaces(faces);
			
		}catch (Exception ex){
			InterfaceSession.handleException(ex);
			}
		
	}
	
	/******************************************************
	 * Load vertices from Ascii encoded data
	 * 
	 * @param data
	 */
	protected void loadAsciiFaces(String data){
		
		ArrayList<MeshFace3D> faces = new ArrayList<MeshFace3D>();
		StringTokenizer tokens = new StringTokenizer(data);
		while (tokens.hasMoreTokens()){
			MeshFace3D face = new MeshFace3D(Integer.valueOf(tokens.nextToken()),
											 Integer.valueOf(tokens.nextToken()),
											 Integer.valueOf(tokens.nextToken()));
			faces.add(face);
			}
		this.setFaces(faces);
		
	}
	
}