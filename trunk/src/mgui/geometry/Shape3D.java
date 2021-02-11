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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiDouble;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import Jama.Matrix;

import java.util.Base64;

/****************************************
 * Base class to be extended by all 3D shapes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class Shape3D implements Shape, Cloneable {

	/*******************************************************
	 * Determines the proximity of {@code point} to this shape.
	 * 
	 * @param thisPoint
	 * @return the proximity, or {@code Double.NaN} if the distance cannot be determined, or
	 * 		   the subclass has not implemented this method. 
	 */
	public double getProximity(Point3f point) {
		return Double.NaN;
	}
	
	/*******************************************************
	 * Returns the point on this shape which is closest to {@code point}. The returned
	 * point need not be a vertex of this shape; its determination is particular to the
	 * subclass.
	 * 
	 * @param point
	 * @return The closest point, or {@code null} if this cannot be determined, or
	 * 		   the subclass has not implemented this method. 
	 */
	public Point3d getProximityPoint(Point3f point) {
		
		return null;
	}
	
	/********************************************************
	 * Determines whether this shape contains {@code point}. Whether this shape can contain
	 * a point is particular to the subclass.
	 * 
	 * @param thisPoint
	 * @return {@code true} if this shape contains {@code point}; {@code false} otherwise.
	 */
	public boolean contains(Point3f point){
		return false;
	}
	
	/****************************
	 * Returns a list of this shape's nodes as <code>Point3f</code>'s. This list is a copy, so operations 
	 * performed on these nodes will not affect this shape.
	 * 
	 * @return
	 */
	public abstract ArrayList<Point3f> getVertices();
	
	/***************************
	 * Returns the vertex at the specified index as a <code>Point3f</code>. This vertex is a copy, so operations
	 * performed on it will not affect this shape.
	 * 
	 * <p>Note: subclasses may want to provide a more efficient implementation of this method.
	 * 
	 * @param index
	 * @return
	 */
	public abstract Point3f getVertex(int index);
	
	/******************************
	 * Returns the number of vertices in this shape. Subclasses can provide more efficient implementations. 
	 * 
	 * @return the number of vertices
	 */
	public int getSize(){
		ArrayList<Point3f> nodes = getVertices();
		if (nodes == null) return 0;
		return nodes.size();
	}
	
	@Override
	public Object clone(){
		return null;
	}
	
	public void setVertices(Point3f[] n){ }
	public abstract void setVertices(ArrayList<Point3f> n);
	
	public abstract float[] getCoords();
	
	public abstract void setCoords(float[] f);
	
	public Box3D getBoundBox(){
		ArrayList<Point3f> nodes = getVertices();
		return GeometryFunctions.getBoundingBox(nodes);
	}
	
	public Point3f getCenter(){
		return getBoundBox().getCenter();
	}
	
	
	public boolean transform(Matrix T){
		//InterfaceSession.log("Transform not implemented for class " + this.getClass().getCanonicalName());
		Matrix4d m = GeometryFunctions.getJamaMatrixAsMatrix4d(T);
		return transform(m);
	}
	
	
	public String getDTD() {
		
		return null;
	}
	
	
	public boolean transform(Matrix4d M){
		return false;
	}

	public String getLocalName() {
		return "Shape3D";
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

	protected XMLEncoding xml_encoding;
	protected String xml_current_block;
	protected ArrayList<Point3f> xml_vertices;
	protected int xml_count, xml_itr;
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException{
		
		if (localName.equals(this.getLocalName())){
			xml_encoding = XMLFunctions.getEncodingForStr(attributes.getValue("encoding"));
			return;
			}
		
		if (xml_current_block != null && this.xml_encoding == XMLEncoding.XML){
			// Read coords as XML
			if (xml_current_block.equals("Vertices")){
				if (xml_vertices == null)
					xml_vertices = new ArrayList<Point3f>(xml_count);
				xml_itr++;
				if (xml_itr > xml_count)
					throw new SAXException("Shape3D.handleXMLElementStart: More vertices than expected (" + xml_count + ").");
				loadXMLCoord(attributes);
				}
			return;
			}

		xml_current_block = localName;
		if (xml_current_block.equals("Vertices")){
			xml_count = Integer.valueOf(attributes.getValue("count"));
			xml_itr = 0;
			}
		
	}
	
	@Override
	public void handleXMLElementEnd(String localName) throws SAXException{
		
		if (xml_current_block != null && localName.equals(xml_current_block)){
			if (xml_encoding == XMLEncoding.XML && localName.equals("Vertices")){
				// They're all loaded, so set them
				//this.setVertices(xml_vertices);
				xml_vertices = null;
				}
			xml_current_block = null;
			return;
			}
		
		if (localName.equals(this.getLocalName())){
			xml_encoding = null;
			xml_current_block = null;
			return;
			}
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException{
		
		if (xml_current_block != null && xml_current_block.equals("Vertices")){
			if (xml_encoding == null)
				throw new SAXException("Shape3D.handleXMLString: Vertex data received but no encoding set.");
			
			switch(xml_encoding){
				case Ascii:
					loadAsciiCoords(s);
					break;
				case Base64Binary:
					loadBinaryCoords(s, 0);
					break;
				case Base64BinaryZipped:
				case Base64BinaryGZipped:
					loadBinaryCoords(s, 1);
					break;
//				case Base64BinaryGZipped:
//					loadBinaryCoords(s, 2);
//					break;
				}
			
			return;
		}
		
	}
	
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<" + getLocalName() + " encoding='" + XMLFunctions.getEncodingStr(options.encoding) + "' >\n");
		writeCoords(tab + 1, writer, options, progress_bar);
		writer.write(_tab + "</" + getLocalName() + ">\n");
		
	}
	
	public String getShortXML(int tab) {
		return XMLFunctions.getTab(tab) + "<" + getLocalName() + " />";
	}
	
	/*******************************************
	 * Write mesh coordinates as Base64 encoded binary data to an XML writer, in row major order.
	 * 
	 * @param mesh
	 * @param encoding
	 * @param compress Compression; 0 for none, 1 for zip, 2 for gzip
	 */
	protected void writeBinaryCoords(Writer writer, int compress) throws IOException{
		
		ByteBuffer data_out = ByteBuffer.allocate(getSize() * 3 * 4);
		
		// First encode as raw bytes
		for (int i = 0; i < getSize(); i++){
			Point3f p = getVertex(i);
			data_out.putFloat(p.x);
			data_out.putFloat(p.y);
			data_out.putFloat(p.z);
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
		String out = Base64.getEncoder().encodeToString(data_out.array());
		writer.write(out);
		
	}
	
	/*******************************************
	 * Write coordinates as ASCII data to an XML writer; formats to {@code decimals} decimal places. Wraps
	 * line when it exceeds 76 characters.
	 * 
	 * @param mesh
	 * @param encoding
	 */
	protected void writeAsciiCoords(Writer writer, String tab, int decimals) throws IOException{
		
		String line;
		int length = 0;
		
		for (int i = 0; i < getSize(); i++){
			Point3f p = getVertex(i);
			line = MguiDouble.getString(p.x, decimals) + " " + 
				   MguiDouble.getString(p.y, decimals) + " " + 
				   MguiDouble.getString(p.z, decimals);
			length += line.length();
			if (length > 76 && i < getSize() - 1){
				line = line + "\n" + tab;
				length = 0;
			}else{
				line = line + " ";
				}
			writer.write(line);
			}
		
	}
	
	/************************************************
	 * Writes this shape's coordinates, according the parameters in {@code options}
	 * 
	 * @param tab
	 * @param writer
	 * @param options
	 * @param progress_bar
	 * @throws IOException
	 */
	protected void writeCoords(int tab, Writer writer, XMLOutputOptions options, 
							   ProgressUpdater progress_bar) throws IOException{
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		writer.write(_tab + "<Vertices count = '" + getSize() + "'>\n");
		// Write data in specified format
		switch (options.encoding){
			case Base64BinaryGZipped:
//				writeBinaryCoords(writer, 2);
//				break;
			case Base64BinaryZipped:
				writeBinaryCoords(writer, 1);
				break;
			case Base64Binary:
				writeBinaryCoords(writer, 0);
				break;
			case Ascii:
				writer.write(_tab2);
				writeAsciiCoords(writer, _tab2, options.sig_digits);
				break;
			}
		writer.write("\n" + _tab + "</Vertices>\n");
	}
	
	/**************************************
	 * Decodes {@code data} and sets this shape's coordinates.
	 * 
	 * @param data
	 * @param encoding
	 */
	protected void loadCoords(String data, XMLEncoding encoding){
		
		switch (encoding){
			case Base64BinaryGZipped:
				loadBinaryCoords(data, 2);
				break;
			case Base64BinaryZipped:
				loadBinaryCoords(data, 1);
				break;
			case Base64Binary:
				loadBinaryCoords(data, 0);
				break;
			case Ascii:
				loadAsciiCoords(data);
				break;
			}
		
	}
	
	/********************************************
	 * Load vertices from Base64 binary encoded data.
	 * 
	 * @param data
	 * @param compression Compression; 0 for none, 1 for zip, 2 for gzip
	 */
	protected void loadBinaryCoords(String data, int compression){
		
		try{
			// Decode
			Charset charset = Charset.forName("UTF-8");
			byte[] utf8_bytes = data.getBytes(charset);
			byte[] b_data = Base64.getDecoder().decode(utf8_bytes);
			
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
			ArrayList<Point3f> vertices = new ArrayList<Point3f>();
			ByteBuffer buffer = ByteBuffer.wrap(b_data);
			while (buffer.hasRemaining()){
				Point3f v = new Point3f(buffer.getFloat(),
										buffer.getFloat(),
										buffer.getFloat());
				vertices.add(v);
				}
			this.setVertices(vertices);
			
		}catch (Exception ex){
			InterfaceSession.handleException(ex);
			}
		
	}
	
	/******************************************************
	 * Load vertices from Ascii encoded data
	 * 
	 * @param data
	 */
	protected void loadAsciiCoords(String data){
		
		ArrayList<Point3f> vertices = new ArrayList<Point3f>();
		StringTokenizer tokens = new StringTokenizer(data);
		while (tokens.hasMoreTokens()){
			Point3f v = new Point3f(Float.valueOf(tokens.nextToken()),
									Float.valueOf(tokens.nextToken()),
									Float.valueOf(tokens.nextToken()));
			vertices.add(v);
			}
		this.setVertices(vertices);
		
	}
	
	/****************************************************
	 * Load a single vertex from XML format, and add to this shape
	 * 
	 * @param attributes
	 */
	protected void loadXMLCoord(Attributes attributes){
		
		
		
		String position = attributes.getValue("position");
		String[] parts = position.split(" ");
		Point3f p = new Point3f(Float.valueOf(parts[0]),
								Float.valueOf(parts[1]),
								Float.valueOf(parts[2]));
		
		
	}
	
}