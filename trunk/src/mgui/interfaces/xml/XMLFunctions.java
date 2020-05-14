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

package mgui.interfaces.xml;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Tuple2d;
import org.jogamp.vecmath.Tuple2f;
import org.jogamp.vecmath.Tuple3d;
import org.jogamp.vecmath.Tuple3f;
import org.jogamp.vecmath.Vector2d;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.Utility;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.xml.XMLObject.XMLEncoding;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.util.Colour3f;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/***********************************************************
 * Utility class for XML-related functions.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class XMLFunctions extends Utility {

	
	/******************************************************
	 * Returns a string which is an XML-friendly version of {@code string}, for insertion into a tag value. 
	 * That is, replaces special characters in tags
	 * such as '&', '<', or '>' with their XML equivalents. See <a href="http://www.dvteclipse.com/documentation/svlinter/How_to_use_special_characters_in_XML.3F.html">
	 * this page</a> for a list of codes.
	 * 
	 * <p> TODO: Either (1) complete this list; or (2) set policy such that tags cannot contain special characters
	 * 
	 * @param string
	 * @return
	 */
	public static String getXMLFriendlyString(String string){
		
		string = string.replaceAll("&", "&amp;");
		string = string.replaceAll("<", "&gt;");
		string = string.replaceAll(">", "&lt;");
		
		
		return string;
		
	}
	
	/******************************************************
	 * Returns the XML header from the current environment
	 * 
	 * @return
	 */
	public static String getXMLHeader(){
		return InterfaceEnvironment.getXMLHeader();
	}
	
	/******************************************************
	 * Returns the {@linkplain XMLType} corresponding to {@code string}.
	 * 
	 * @param string
	 * @return
	 */
	public static XMLType getXMLTypeForStr(String string){
		if (string.equalsIgnoreCase("reference")) return XMLType.Reference;
		if (string.equalsIgnoreCase("full")) return XMLType.Full;
		if (string.equalsIgnoreCase("short")) return XMLType.Short;
		if (string.equalsIgnoreCase("normal")) return XMLType.Normal;
		return null;
	}
	
	/******************************************************
	 * Returns a String corresponding to the {@linkplain XMLType}.
	 * 
	 * @param string
	 * @return
	 */
	public static String getXMLStrForType(XMLType type){
		switch (type){
			case Reference: return "Reference";
			case Full: return "Full";
			case Short: return "Short";
			case Normal: return "Normal";
			}
		return null;
	}
	
	/*****************************************************
	 * Returns the string corresponding to {@code encoding}.
	 * 
	 * @param format
	 * @return
	 */
	public static String getEncodingStr(XMLEncoding encoding){
		switch (encoding){
			case Ascii: return "Ascii";
			case Base64Binary: return "Base64Binary";
			case Base64BinaryZipped: return "Base64BinaryZipped";
			case Base64BinaryGZipped: return "Base64BinaryGZipped";
			case XML: return "XML";
			}
		return "";
	}
	
	/*****************************************************
	 * Returns the {@code XMLFormat} corresponding to {@code format}.
	 * 
	 * @param format
	 * @return
	 */
	public static XMLEncoding getEncodingForStr(String encoding){
		if (encoding.equals("Ascii")) return XMLEncoding.Ascii;
		if (encoding.equals("Base64Binary")) return XMLEncoding.Base64Binary;
		if (encoding.equals("Base64BinaryZipped")) return XMLEncoding.Base64BinaryZipped;
		if (encoding.equals("Base64BinaryGZipped")) return XMLEncoding.Base64BinaryGZipped;
		if (encoding.equals("XML")) return XMLEncoding.XML;
		return null;
	}
	
	/*****************************
	 * Returns a string with the specified number of tabs
	 * 
	 * @param tab
	 * @return
	 */
	public static String getTab(int tab){
		String _tab = "";
		for (int i = 0; i < tab; i++)
			_tab = _tab + "\t";
		return _tab;
	}
	
	public static String getTupleStr(Tuple3d t, int precision){
		return MguiDouble.getString(t.x, precision) + " " + 
			   MguiDouble.getString(t.y, precision) + " " + 
			   MguiDouble.getString(t.z, precision);
	}
	
	public static String getTupleStr(Tuple3f t, int precision){
		return MguiDouble.getString(t.x, precision) + " " + 
			   MguiDouble.getString(t.y, precision) + " " + 
			   MguiDouble.getString(t.z, precision);
	}
	
	public static String getVectorStr(Tuple2d t, int precision){
		return MguiDouble.getString(t.x, precision) + " " + 
			   MguiDouble.getString(t.y, precision);
	}
	
	public static String getVectorStr(Tuple2f t, int precision){
		return MguiDouble.getString(t.x, precision) + " " + 
			   MguiDouble.getString(t.y, precision);
	}
	
	public static Vector3d getVector3d(String s){
		StringTokenizer tokens = new StringTokenizer(s, " ");
		return new Vector3d(Double.valueOf(tokens.nextToken()),
							Double.valueOf(tokens.nextToken()),
							Double.valueOf(tokens.nextToken()));
			
	}
	
	public static Vector2d getVector2d(String s){
		StringTokenizer tokens = new StringTokenizer(s, " ");
		return new Vector2d(Double.valueOf(tokens.nextToken()),
							Double.valueOf(tokens.nextToken()));
			
	}
	
	public static Vector3f getVector3f(String s){
		StringTokenizer tokens = new StringTokenizer(s, " ");
		return new Vector3f(Float.valueOf(tokens.nextToken()),
							Float.valueOf(tokens.nextToken()),
							Float.valueOf(tokens.nextToken()));
			
	}
	
	/*********************************************************
	 * Creates a new {@code Attribute} with a new instance of the given class. Class must define an
	 * empty constructor.
	 * 
	 * @param c
	 * @param name
	 * @return
	 */
	public static <V> Attribute<V> createAttribute(Class<V> c, String name, Object value) throws SAXException{
		
		try{
			if (value != null)
				return new Attribute<V>(name, (V)value) {};
		}catch (ClassCastException ex){
			throw new SAXException("XMLFunctions.createAttribute: Value '" + value.toString() + "' is not of class " +
									c.getCanonicalName());
			}
			
		return new Attribute<V>(name, c) {};
		
	}
	
	/**********************************************************
	 * Encodes a string into an XML-friendly line by replacing angle brackets with their escape
	 * characters:
	 * 
	 * <ul>
	 * <li>< --> &lt; 
	 * <li>> --> &gt;
	 * </ul>
	 * 
	 * @param string
	 * @return
	 */
	public static String getXMLCodedString(String string){
		
		string = string.replace("<", "&lt;");
		return string.replace(">", "&gt;");
		
	}
	
	/**********************************************************
	 * Decodes a string from an XML-friendly line by replacing angle brackets escape
	 * characters with their characters.
	 * 
	 * <ul>
	 * <li>< --> &lt; 
	 * <li>> --> &gt;
	 * </ul>
	 * 
	 * @param string
	 * @return
	 */
	public static String getXMLDecodedString(String string){
		
		string = string.replace("&lt;", "<");
		return string.replace("&gt;", ">");
		
	}
	
	/***********************
	 * Prepends all newline characters with the appropriate tab character, for
	 * friendlier XML output
	 * 
	 * @param string
	 * @return
	 */
	public static String getXMLTabbedString(String string, int tab){
		String ch = "\n" + getTab(tab);
		return string.replace("\n", ch);
	}
	
	/***********************
	 * Removes all new-line tabs from an XML input string
	 * 
	 * @param string
	 * @param tab
	 */
	public static String getXMLUntabbedString(String string, int tab){
		String ch = getTab(tab);
		return string.replace(ch, "");
	}
	
	/**********************
	 * Parses <code>s</code> for '\t' and '\n' characters into a list of 2D points
	 * 
	 * @param s
	 * @return
	 */
	public static ArrayList<Point2f> getPoint2fList(String s){
		
		StringTokenizer tokens = new StringTokenizer(s, "\t\n");
		ArrayList<Point2f> pts = new ArrayList<Point2f>();
		
		while (tokens.hasMoreTokens())
			pts.add(getPoint2f(tokens.nextToken()));
			
		return pts;
	}
	
	public static Point2f getPoint2f(String s){
		StringTokenizer tokens = new StringTokenizer(s, " ");
		return new Point2f(Float.valueOf(tokens.nextToken()),
							Float.valueOf(tokens.nextToken()));
			
	}
	
	public static Point3f getPoint3f(String s){
		StringTokenizer tokens = new StringTokenizer(s, " ");
		return new Point3f(Float.valueOf(tokens.nextToken()),
							Float.valueOf(tokens.nextToken()),
							Float.valueOf(tokens.nextToken()));
			
	}
	
	public static Point3d getPoint3d(String s){
		StringTokenizer tokens = new StringTokenizer(s, " ");
		return new Point3d(Float.valueOf(tokens.nextToken()),
							Float.valueOf(tokens.nextToken()),
							Float.valueOf(tokens.nextToken()));
			
	}
	
	public static FileFilter getXMLFileFilter(){
		return new FileNameExtensionFilter("XML files (*.xml)", "xml");
	}
	
	public static String getXMLForPoint2f(Point2f p, int precision){
		return MguiFloat.getString(p.x, precision) + " " +
			   MguiFloat.getString(p.y, precision);
	}
	
	public static String getXMLForObject(Object obj, int tab){
		
		if (obj == null) 
			return XMLFunctions.getTab(tab) + "null\n";
		
		if (obj instanceof XMLObject)
			return ((XMLObject)obj).getXML(tab);
		
		if (obj instanceof Color)
			return (new Colour3f((Color)obj)).getXML(tab);
		
		//otherwise return toString()
		return XMLFunctions.getTab(tab) + obj.toString() + "\n";
		
	}
	
	/*****************************************************
	 * Returns a string representing {@code obj} in an {@code Attribute}.
	 * 
	 * @param obj
	 * @param tab
	 * @return
	 */
	public static String getXMLForAttributeObject(Object obj, int tab){
		
		if (obj == null) 
			return XMLFunctions.getTab(tab) + "null";
		
		if (obj instanceof Class)
			return ((Class)obj).getCanonicalName();
		
		if (obj instanceof ColourMap)
			return ((ColourMap)obj).getName();
			
		if (obj instanceof XMLObject)
			return ((XMLObject)obj).getShortXML(tab);
		
		if (obj instanceof Color)
			return (new Colour3f((Color)obj)).getShortXML(tab);
		
		if (obj instanceof BasicStroke){
			BasicStroke stroke = (BasicStroke)obj;
			String xml = "width='" + stroke.getLineWidth() + "' " +
						 "join='" + getStrokeJoin(stroke) + "' " +
						 "endcap='" + getStrokeEndCap(stroke) + "' " +
						 "miterlimit='"+ stroke.getMiterLimit() + "'";
						 
			if (stroke.getDashArray() != null){
				 xml = xml + " dashphase='" + stroke.getDashPhase() + "' " + 
						 	 "dasharray='" + getArrayString(stroke.getDashArray()) + "'";
				}
			return xml;
		}
			
		
		//otherwise return toString()
		return XMLFunctions.getTab(tab) + obj.toString();
		
	}
	
	private static String getStrokeJoin(BasicStroke stroke){
		int join = stroke.getLineJoin();
		switch(join){
			case BasicStroke.JOIN_BEVEL:
				return "BEVEL";
			case BasicStroke.JOIN_MITER:
				return "MITER";
			case BasicStroke.JOIN_ROUND:
				return "ROUND";
			}
		return "NONE";
	}
	
	private static int getStrokeJoin(String name){
		if (name.equals("BEVEL"))
			return BasicStroke.JOIN_BEVEL;
		if (name.equals("ROUND"))
			return BasicStroke.JOIN_ROUND;
		
		return BasicStroke.JOIN_MITER;
	}
	
	private static String getStrokeEndCap(BasicStroke stroke){
		int cap = stroke.getEndCap();
		switch(cap){
			case BasicStroke.CAP_BUTT:
				return "BUTT";
			case BasicStroke.CAP_ROUND:
				return "ROUND";
			case BasicStroke.CAP_SQUARE:
				return "SQUARE";
			}
		return "NONE";
	}
	
	private static int getStrokeEndCap(String name){
		if (name.equals("BUTT"))
			return BasicStroke.CAP_BUTT;
		if (name.equals("ROUND"))
			return BasicStroke.CAP_ROUND;
		
		return BasicStroke.CAP_SQUARE;
	}
	
	private static String getArrayString(float[] array){
		String str = "[";
		for (int i = 0; i < array.length; i++){
			if (i > 0) str = str + ",";
			str = str + Float.toString(array[i]);
			}
		return str + "]";
	}
	
	public static Color getColourForXML(Attributes attributes){
		return new Color(Float.valueOf(attributes.getValue("red")),
						 Float.valueOf(attributes.getValue("green")),
						 Float.valueOf(attributes.getValue("blue")));
	}
	
	/************************************************
	 * Returns a new instantiated Object corresponding to the "object_class" field of 
	 * <code>attributes</code>.
	 * 
	 * @param localName
	 * @param attributes
	 * @return New object instance, or {@code null} if one could not be created
	 * 
	 *  TODO: Allow user-defined handlers in the {@code InterfaceEnvironment}
	 */
	public static Object getObjectForXML(String localName, Attributes attributes, String class_name){
		
		Class<?> obj_class = null;
		try{
			obj_class = Class.forName(class_name);
		}catch (ClassNotFoundException e){
			InterfaceSession.log("XMLFunctions: Class '" + class_name + "' not found.", 
								 LoggingType.Errors);
			return null;
			}
		if (obj_class.getCanonicalName().equals("java.awt.Color") && localName.startsWith("Colour")){
			return getColourForXML(attributes);
			}
		try{
					
			try{
				// Try empty constructor
				return obj_class.newInstance();
			}catch (InstantiationException ex){
			}catch (IllegalAccessException ex){}
			
			// Handle special cases here
			
		}catch (Exception e){
			//InterfaceSession.log("XMLFunctions: Exception loading attribute. \nDetails: " + e.getMessage(), LoggingType.Errors);
			}
				
		return null;
		
	}
	
	/**************************************************
	 * Attempts to detect the data type of <code>s</code> and return a newly instantiated Object
	 * of the correct Class.
	 * 
	 * @param string String with which to instantiate object
	 * @return The instantiated object
	 * @throws XMLException If object could not be instantiated
	 */
	public static Object getObjectForXMLString(Class<?> obj_class, String string) throws XMLException{
		
		// Colour maps are pre-loaded, fetch with name
		if (ColourMap.class.isAssignableFrom(obj_class))
			return InterfaceEnvironment.getColourMap(string);
		
		// Class objects set with Class.forName
		if (Class.class == obj_class){
			try{
				return Class.forName(string);
			}catch (Exception e){
				InterfaceSession.handleException(e, LoggingType.Errors);
				throw new XMLException("XMLFunctions: No class found for '" + string + "'.");
				}
			}
		
		// Is it a String attribute?
		if (obj_class.isInstance(string)) 
			return string;
		
		if (Double.class.isAssignableFrom(obj_class)) 
			return Double.valueOf(string);
		if (Float.class.isAssignableFrom(obj_class)) 
			return Float.valueOf(string);
		if (Long.class.isAssignableFrom(obj_class)) 
			return Long.valueOf(string);
		if (Integer.class.isAssignableFrom(obj_class)) 
			return Integer.valueOf(string);
		if (Boolean.class.isAssignableFrom(obj_class)) 
			return Boolean.valueOf(string);
		if (Color.class.isAssignableFrom(obj_class)){
			try{
				return parseColor(string);
			}catch(Exception e){
				InterfaceSession.log("XMLFunctions: Could not parse colour '" + string + "'.", LoggingType.Warnings);
				return Color.black;
				}
			}
		if (obj_class.getCanonicalName().equals("java.awt.Font")){
			try{
				return parseFont(string);
			}catch(Exception e){
				InterfaceSession.log("XMLFunctions: Could not parse font '" + string + "'.", LoggingType.Warnings);
				return new Font("Arial", Font.PLAIN, 10);
				}
			}
		if (obj_class.getCanonicalName().equals("java.awt.BasicStroke")){
			try{
				return parseStroke(string);
			}catch(Exception e){
				InterfaceSession.log("XMLFunctions: Could not parse stroke '" + string + "'.", LoggingType.Warnings);
				return new BasicStroke();
				}
			}
		if (obj_class.getCanonicalName().equals("mgui.geometry.util.SpatialUnit")){
			return InterfaceEnvironment.getSpatialUnit(string);
			}
		if (obj_class.getCanonicalName().equals("mgui.geometry.util.NodeShape")){
			return InterfaceEnvironment.getVertexShapes().get(string);
			}
	
		//try to construct class with string as argument
		try{
			Constructor<?> c = obj_class.getConstructor(String.class);
			return c.newInstance(string);
		}catch (NoSuchMethodException e){
			try{
				//try to construct class with empty constructor 
				return obj_class.newInstance();
			}catch (Exception e2){ }
		}catch (Exception e){ }
		
		throw new XMLException("XMLFunctions: Error instantiating class '" + obj_class.getCanonicalName() + "'.");
		
	}
	
	/**********************************************
	 * Parses a font string (returned by Font.toString()) and returns the corresponding font
	 * 
	 * @param fstr
	 * @return
	 */
	public static Font parseFont(String fstr) throws Exception{
		String line = fstr.substring(fstr.indexOf("[") + 1, fstr.indexOf("]"));
		String[] parts = line.split(",");
		String name = parts[1].substring(parts[1].indexOf("=")+1);
		String style = parts[2].substring(parts[2].indexOf("=")+1);
		int size = Integer.valueOf(parts[3].substring(parts[3].indexOf("=")+1));
		int _style = Font.PLAIN;
		if (style.equalsIgnoreCase("bold"))
			_style = Font.BOLD;
		if (style.equalsIgnoreCase("italic"))
			_style = Font.ITALIC;
		return new Font(name, _style, size);
	}
	
	/**********************************************
	 * Parses a color string (three space-delimited values) and returns the corresponding font
	 * 
	 * @param fstr
	 * @return
	 */
	public static Color parseColor(String fstr) throws Exception{
		String[] vals = fstr.split(" ");
		if (vals.length != 3) return Color.black;
		return new Color(Float.valueOf(vals[0]), 
						 Float.valueOf(vals[1]), 
						 Float.valueOf(vals[2]));
	}
	
	/**********************************************
	 * Parses the string for a {@linkplain BasicStroke}.
	 * 
	 * @param fstr
	 * @return
	 * @throws Exception
	 */
	public static BasicStroke parseStroke(String fstr) throws Exception{
		
		float width=1.0f, miterlimit=10.0f, dashphase=0;
		int endcap=1, join=-1;
		float[] dasharray=null;
		
		String[] parts = fstr.split(" ");
		for (int i = 0; i < parts.length; i++){
			String[] subparts = parts[i].split("=");
			if (subparts.length != 2){
				throw new SAXException("XMLFunctions.parseStroke: error parsing stroke '" + fstr + "'");
				}
			String attr = subparts[0].trim();
			String value = subparts[1];
			if (value.contains("'"))
				value = value.replaceAll("'", "");
			if (attr.equals("width")){
				width = Float.valueOf(value);
			}else if (attr.equals("join")){
				join = getStrokeJoin(value);
			}else if (attr.equals("endcap")){
				endcap = getStrokeEndCap(value);
			}else if (attr.equals("miterlimit")){
				miterlimit = Float.valueOf(value);
			}else if (attr.equals("dashphase")){
				dashphase = Float.valueOf(value);
			}else if (attr.equals("dasharray")){
				dasharray = parseFloatArray(value);
				}
			}
		
		return new BasicStroke(width, endcap, join, miterlimit, dasharray, dashphase);
	}
	
	private static float[] parseFloatArray(String array_str){
		array_str = array_str.substring(1,array_str.length()-2);
		String[] parts = array_str.split(",");
		float[] array = new float[parts.length];
		for (int i = 0; i < parts.length; i++)
			array[i] = Float.valueOf(parts[i]);
		return array;
	}
	
	/************************************************
	 * Writes an XML object to file. Overwrites an existing file. To append to an existing file,
	 * use appendToXML.
	 * 
	 * @param file
	 * @param xml
	 */
	public static void writeToXML(File file, XMLObject xml) throws XMLException{
		
		try{
			if (file.exists())
				file.delete();
		
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			writer.write(getXMLHeader() + "\n\n");
			writer.write(xml.getXML());
			writer.close();
		
		}catch (IOException e){
			throw new XMLException("XMLException: " + e.getMessage());
			}
		
	}
	
}