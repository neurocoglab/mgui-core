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

package mgui.interfaces.maps;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.InterfaceLayoutPanel;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.numbers.MguiNumber;
import mgui.util.Colour;
import mgui.util.Colour3f;
import mgui.util.Colour4f;
import mgui.util.Colours;

import org.xml.sax.Attributes;

/***********************************************************
 * Colour map for discrete 1-to-1 mappings (integers to colours).
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DiscreteColourMap extends ColourMap {

	public static DiscreteColourMap RANDOM_600 = getRandom600();
	
	public HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
	//public String name = "";
	public NameMap nameMap;
	
	public DiscreteColourMap(){
		
	}
	
	public DiscreteColourMap(String name){
		this.name = name;
	}
	
	public DiscreteColourMap(NameMap map){
		nameMap = map;
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/discrete_cmap_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/discrete_cmap_20.png");
		return null;
	}
	
	public ArrayList<Integer> getIndices(){
		return new ArrayList<Integer>(colours.keySet());
	}
	
	@Override
	public Colour getColour(Comparable<?> o){
		if (o instanceof String)
			return getColour((String)o);
		if (o instanceof MguiNumber)
			return getColour((int)(Math.round(((MguiNumber)o).getValue())));
		if (o instanceof Integer)
			return getColour(((Integer)o).intValue());
		return new Colour4f(Color.WHITE);
	}
	
	@Override
	public Colour getColour(int i) {
		Colour c = colours.get(i);
		if (c == null) c = new Colour4f(Color.WHITE);
		return c;
	}
	
	public void setColour(String name, Colour c){
		if (nameMap == null) return;
		int i = nameMap.get(name);
		if (i >= 0) setColour(i, c);
	}
	
	public void setColour(int i, Colour c){
		//arInteger index = new arInteger(i);
		Colour colour = colours.get(i);
		if (colour != null) colours.remove(i);
		colours.put(i, c);
	}
	
	public void setColour(int i, String n, Colour c){
		setColour(i, c);
		if (nameMap != null)
			nameMap.set(i, n);
	}
	
	/*****************************************************
	 * Converts this discrete map to a continuous map by sampling at 0, N and <code>no_anchors</code> - 2 evenly-spaced
	 * indices. The discrete map should be a discretized continuous map for this to make sense.
	 * 
	 * @param no_anchors
	 * @return
	 */
	public ContinuousColourMap getAsContinuousMap(int no_anchors){
		
		ContinuousColourMap c_map = new ContinuousColourMap();
		//c_map.addAnchor(0.0, this.getColour(0));
		int n = this.getSize();
		double unit = (double)n / (double)no_anchors; 
		
		for (int i = 0; i < no_anchors; i++){
			double val = (double)(i) / (double)(no_anchors - 1);
			int index = (int)Math.round(i * unit);
			index = Math.min(n - 1, index);
			c_map.addAnchor(val, getColour(index));
			}
		
		//c_map.addAnchor(1.0, getColour(n - 1));
		return c_map;
		
	}
	
	public boolean hasNameMap(){
		return nameMap != null;
	}
	
	public void setNameMap(NameMap map){
		nameMap = map;
	}
	
	public void setNameMap(boolean b){
		if (!b)
			nameMap = null;
		else if (nameMap == null)
			nameMap = new NameMap();
	}
	
	public NameMap getNameMap(){
		return nameMap;
	}
	
	public Colour getColour(String value){
		if (!hasNameMap()) return null;
		int i = nameMap.get(value);
		if (i < 0) return null;
		return getColour(i);
	}
	
	//default 6-colour map
	public static DiscreteColourMap getRandom600(){
		DiscreteColourMap map = new DiscreteColourMap();
		map.name = "Random 2200-colour";
		//float r = 0, g = 0, b = 0;
		//add five colours
		for (int i = 0; i < 2200; i++){
			//b = (float)Math.random();
			//g = (float)Math.random();
			//r = (float)Math.random();
			map.setColour(i, Colours.getRandom()); // new Colour4f(r, g, b, 1));
			}
		return map;
	}
	
	/**********************************
	 * Returns the maximum value in this map's indices
	 * 
	 * @return
	 */
	public int getMax(){
		ArrayList<Integer> indices = getIndices();
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < indices.size(); i++)
			if (indices.get(i) > max)
				max = indices.get(i);
		
		return max;
	}
	
	/**********************************
	 * Returns the minimum value in this map's indices
	 * 
	 * @return
	 */
	public int getMin(){
		ArrayList<Integer> indices = getIndices();
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < indices.size(); i++)
			if (indices.get(i) < min)
				min = indices.get(i);
		
		return min;
	}
	
	public int getSize(){
		return colours.size();
	}
	
	/****************************
	 * Returns a mapping from the index values of this discrete colour map to the 
	 * byte map indices return by {@linkplain getDiscreteMap}.
	 * 
	 * @return
	 */
	public HashMap<Integer,Integer> getIndexMap(){
		HashMap<Integer,Integer> idx_map = new HashMap<Integer,Integer>();
		ArrayList<Integer> indices = new ArrayList<Integer>(colours.keySet());
		Collections.sort(indices);
		for (int i = 0; i < indices.size(); i++) {
			idx_map.put(indices.get(i),i);
			}
		return idx_map;
	}
	
	@Override
	public byte[] getDiscreteMap(int max_size, int channels, boolean setAlphaFromMax){
		
		//return the first "size" values from list; if value is not mapped,
		//set to default colour
		
		byte[] map = new byte[max_size * channels];
		byte[] sample;
		Colour colour;
		Colour dcolour = Colours.getColourNf(new Colour4f(Color.gray), channels);
		
		ArrayList<Integer> indices = new ArrayList<Integer>(colours.keySet());
		Collections.sort(indices);
//		int start = 0; // getMin();
//		int end = getMax() + 1;
//		int size = end - start;
//		if (max_size < size)
//			end = start + max_size;
		
		int j = 0;
		
		for (int i = 0; i < indices.size(); i++){
			sample = new byte[channels];
			int idx = indices.get(i);
			colour = getColour(idx);
			if (colour != null)
				colour = Colours.getColourNf(colour, channels);
			else
				colour = dcolour;
			if (channels > 1 && setAlphaFromMax)
				colour.setAlpha(colour.getIntensity());
			Colours.toBytes(colour, sample);
			System.arraycopy(sample, 0, map, j * channels, channels);
			j++;
			}
		return map;
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = super.getXML(tab);
		xml = xml + " type = 'discrete' >\n";
		//xml = xml + _tab + ">\n";
		
		//Iterator<Integer> itr = colours.keySet().iterator(); 
		ArrayList<Integer> indexes = new ArrayList<Integer>(colours.keySet());
		Collections.sort(indexes);
		
		if (nameMap != null)
			xml = xml + nameMap.getXML(tab + 1);
		
		for (int i = 0; i < indexes.size(); i++){
			//int index = itr.next();
			int index = indexes.get(i);
			xml = xml + _tab2 + "<Item index = '" + index + "'>\n";
			xml = xml + colours.get(index).getXML(tab + 2) + "\n";
			xml = xml + _tab2 + "</Item>\n";
			}
		
		xml = xml + _tab + "</ColourMap>";
		
		return xml;
	}
	
	public String getShortXML(int tab){
		return XMLFunctions.getTab(tab) + "<ColourMap name = '" + getName() + "' type = 'discrete' />\n";
	}

	Integer current_item;
	NameMap current_name_map;
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
	
		if (localName.equals("NameMap")){
			current_name_map = new NameMap(attributes.getValue("name"));
			return;
			}
		
		if (current_name_map != null){
			current_name_map.handleXMLElementStart(localName, attributes, null);
			return;
			}
		
		if (localName.equals("Item")){
			current_item = Integer.valueOf(attributes.getValue("index"));
			return;
			}
		
		if (localName.startsWith("Colour") && current_item != null){
			Colour c = null;
			if (localName.endsWith("4f"))
				c = new Colour4f();
				
			if (localName.endsWith("3f"))
				c = new Colour3f();
			
			if (c == null) return;	//throw exception?
			
			c.handleXMLElementStart(localName, attributes, null);
			colours.put(current_item, c);
			}
		
		
	}
	
	@Override
	public void handleXMLElementEnd(String localName){
		
		if (localName.equals("NameMap")){
			nameMap = current_name_map;
			current_name_map = null;
			return;
			}
			
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new DiscreteColourMap(attributes.getValue("name"));
	}
	
	@Override
	public InterfaceLayoutPanel getLayoutPanel(){
	
		
		
		return null;
	}
	
	@Override
	public Object clone(){
		
		DiscreteColourMap map = new DiscreteColourMap(this.getName());
		map.colours = new HashMap<Integer, Colour>(colours);
		if (this.nameMap != null) map.setNameMap(this.nameMap);
		return map;
		
	}
	
}