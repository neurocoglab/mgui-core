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

package mgui.interfaces.maps;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Color4f;

import org.xml.sax.Attributes;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.layouts.InterfaceLayoutPanel;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.resources.icons.IconObject;
import mgui.util.Colour;
import mgui.util.Colour4f;


/*************************************
 * Abstract class for mapping values (instances of {@link Comparable}) to colours.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public abstract class ColourMap extends AbstractInterfaceObject 
								implements IconObject,
										   Cloneable, 
										   XMLObject {
	
	public String name = "";
	public Colour nullColour = new Colour4f(0.1f, 0.1f, 0.1f, 1.0f);
	//public double mapMin = 0, mapMax = 255;
	
	protected ColourMap(){
		
	}
	
	//return a colour corresponding to Object o
	//to be overridden
	public Colour getColour(Comparable<?> o)
	{return new Colour4f(1f, 0.1f, 0.1f, 1.0f);}	
	@Override
	public String getName(){return name;}
	public Colour getColour(int i){
		return getColour(new MguiInteger(i));
	}
	
	/************************************************************
	 * Get an array of colours for the given list of values, given the specified limits.
	 * 
	 * @param list
	 * @param min
	 * @param max
	 * @return
	 */
	public Color4f[] getColor4fArray(ArrayList<MguiNumber> list, double min, double max){
		return getColor4fArray(list);
	}
	
	/************************************************************
	 * Get an array of colours for the given list of values, given the specified limits.
	 * 
	 * @param list
	 * @param min
	 * @param max
	 * @return
	 */
	public Color3f[] getColor3fArray(ArrayList<MguiNumber> list, double min, double max){
		return getColor3fArray(list);
	}
	
	/************************************************************
	 * Get colour for the specified value, for the current limits.
	 * 
	 * @param d
	 * @return
	 */
	public Colour getColour(double d){
		return getColour(new MguiDouble(d));
	}
	
	/************************************************************
	 * Get colour for the specified value, given the specified limits.
	 * 
	 * @param d
	 * @param min
	 * @param max
	 * @return
	 */
	public Colour getColour(double d, double min, double max){
		return getColour(new MguiDouble(d));
	}
	
	/****************************
	 * Returns an array of Color4f colours mapped from the values in list
	 * @param list {@code ArrayList} of type {@code MguiNumber} to be mapped to colours
	 * @return an array of type Color4f
	 */
	
	public Color4f[] getColor4fArray(ArrayList<MguiNumber> list){
		Colour thisColour;
		Color4f[] colours = new Color4f[list.size()];
		for (int i = 0; i < list.size(); i++){
			thisColour = getColour(list.get(i));
			if (thisColour == null) 
				thisColour = nullColour;
			colours[i] = thisColour.getColor4f();
			}
		return colours;
	}
	
	
	/****************************
	 * Returns an array of Color3f colours mapped from the values in list
	 * @param list {@code ArrayList} of type {@code MguiNumber} to be mapped to colours
	 * @return an array of type Color3f
	 */
	
	public Color3f[] getColor3fArray(ArrayList<MguiNumber> list){
		Colour thisColour;
		Color3f[] colours = new Color3f[list.size()];
		for (int i = 0; i < list.size(); i++){
			thisColour = getColour(list.get(i));
			if (thisColour == null) 
				thisColour = nullColour;
			colours[i] = thisColour.getColor3f();
			}
		return colours;
	}
	
	@Override
	public void setName(String name){
		this.name = name;
	}
	
	/******************************************
	 * Compile and return a discrete colour map of size <code>size</code> with <code>channels</code>
	 * interleaved data channels. For a given colour model (e.g., RGBA), each colour component <code>c</code> 
	 * will be represented by the index <code>(i * 4) + c</code>.
	 * 
	 * @param size
	 * @param channels
	 * @param setAlphaFromPos sets the alpha value from position rather than colour map
	 * @return
	 */
	public abstract byte[] getDiscreteMap(int size, int channels, boolean setAlphaFromPos);
	
	/******************************************
	 * Compile and return a discrete colour map of size <code>size</code> with <code>channels</code>
	 * interleaved data channels. For a given colour model (e.g., RGBA), each colour component <code>c</code> 
	 * will be represented by the index <code>(i * 4) + c</code>.
	 * 
	 * @param size
	 * @param channels
	 * @return
	 */
	public byte[] getDiscreteMap(int size, int channels){
		return getDiscreteMap(size, channels, false);
	}
	
	public void printToConsole(){
		
	}
	
	public String getDTD() {
		return "";
	}

	public String getXML() {
		return getXML(0);
	}

	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<ColourMap";
		xml = xml + " name = '" + name + "'";
		
		return xml;
	}

	public String getXMLSchema() {
		
		
		return null;
	}
	
	public String getLocalName(){
		return "ColourMap";
	}

	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
		
	}
	
	public void handleXMLElementEnd(String localName) {
		
		
	}
	
	public void handleXMLString(String s){
		
	}
	
	public InterfaceLayoutPanel getLayoutPanel(){
		return null;
	}
	
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		//XML string should be small, so get it directly
		writer.write(getXML(tab));
	}
	
	@Override
	public abstract Object clone();
	
	
}