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
import java.util.Comparator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jogamp.vecmath.Color4f;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;
import mgui.util.Colour;
import mgui.util.Colour4f;
import mgui.util.Colours;

import org.xml.sax.Attributes;


/******************************
 * Maps colours to values based upon a set of anchors (expressed on the range [0:1]) and an interpolator.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * TODO: allow specific colour models; currently is 4-channel RGBA only
 */
public class ContinuousColourMap extends ColourMap {
	
	public static ContinuousColourMap DEFAULT_2 = getDefault2();
	public static ContinuousColourMap DEFAULT_3 = getDefault3();
	public static ContinuousColourMap GREY_SCALE = getGreyScale();
	
	//public Class valueClass;
	public ArrayList<Anchor> anchors = new ArrayList<Anchor>();
	//public String name;
	Comparator comp = new Comparator(){
		public int compare(Object o1, Object o2){
			return ((Anchor)o1).value.compareTo(((Anchor)o2).value);
			}};
	
	public ContinuousColourMap(){ 
		
	}
	
	public ContinuousColourMap(String name){ 
		this.name = name;
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/continuous_cmap_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/continuous_cmap_20.png");
		return null;
	}
	
	/*******************************
	 * Set this map from <code>map</code>.
	 * 
	 * @param map
	 */
	public void setFromMap(ContinuousColourMap map){
		
		this.anchors = new ArrayList<Anchor>();
		
		for (int i = 0; i < map.anchors.size(); i++)
			addAnchor(new MguiDouble(map.anchors.get(i).value), new Colour4f(map.anchors.get(i).colour.getColor()));
//		mapMin = map.mapMin;
//		mapMax = map.mapMax;
		name = map.name;
		nullColour = map.nullColour;
		
	}
	
	public static ContinuousColourMap getDefault2(){
		ContinuousColourMap d = null;
		d = new ContinuousColourMap();
		d.name = "Default 2-Anchor";
		d.addAnchor(new MguiFloat(0), new Colour4f(0, 0, 1, 1));
		d.addAnchor(new MguiFloat(1), new Colour4f(1, 0, 0, 1));
		return d;
	}
	
	public static ContinuousColourMap getDefault3(){
		ContinuousColourMap d = null;
		d = new ContinuousColourMap();
		d.name = "Default 3-Anchor";
		d.addAnchor(new MguiFloat(0), new Colour4f(0, 0, 1, 1));
		d.addAnchor(new MguiFloat(1), new Colour4f(1, 0, 0, 1));
		d.addAnchor(new MguiFloat(0.5f), new Colour4f(0, 1, 0, 1));
		d.addAnchor(new MguiFloat(0.75f), new Colour4f(0.5f, 0.5f, 0.5f, 1));
		return d;
	}
	
	public static ContinuousColourMap getGreyScale(){
		ContinuousColourMap d = null;
		//try{
			d = new ContinuousColourMap();
			d.name = "Greyscale";
			d.addAnchor(new MguiFloat(0), new Colour4f(0, 0, 0, 1));
			d.addAnchor(new MguiFloat(1), new Colour4f(1, 1, 1, 1));
		return d;
	}
	
	@Override
	public Colour getColour(int i){
		return getColour(new MguiInteger(i));
	}
	
	@Override
	public Colour getColour(Comparable o){
		if (MguiNumber.class.isInstance(o))
			return getColourAtValue((MguiNumber)o);
		return null;
	}
	
	@Override
	public Colour getColour(double d, double min, double max){
		return getColourAtValue(d, min, max);
	}
	
	public int addAnchor(double value, Colour colour){
		return addAnchor(new MguiDouble(value), colour);
	}
	
	public int addAnchor(MguiNumber value, Colour colour){
		//TODO: ensure value doesn't already exist
		//anchors.add(new Anchor(value, colour));
		Anchor anchor = new Anchor(value, colour);
		int index = Collections.binarySearch(anchors, anchor, comp);
		if (index >= 0) return -1;
		//add sorted
		index = -index - 1;
		anchors.add(index, new Anchor(value, colour));
		return index;
	}
	
	/*********************
	 * Remove all anchor points between min and max
	 * @param min
	 * @param max
	 */
	public void removeAnchors(MguiNumber min, MguiNumber max){
		for (int i = 0; i < anchors.size(); i++)
			if (anchors.get(i).value.compareTo(min) >= 0 && anchors.get(i).value.compareTo(max) <= 0){
				anchors.remove(i);
				i--;
				}
	}
	
	public void removeAnchor(int anchor){
		if (anchors.size() < 3 || anchor >= anchors.size() || anchor < 0) return;
		anchors.remove(anchor);
	}
	
	public int getAnchor(double val, float radius){
		double min = Float.MAX_VALUE;
		int anchor = -1;
		for (int i = 0; i < anchors.size(); i++)
			if (anchors.get(i).value.compareTo(val - radius) >= 0 && 
				anchors.get(i).value.compareTo(val + radius) <= 0){
				if (Math.abs(val - anchors.get(i).value.getValue()) < min){
					min = Math.abs(val - anchors.get(i).value.getValue());
					anchor = i;
					}
				}
		return anchor;
	}
	
	public void resort(){
		Collections.sort(anchors, comp);
	}
	
	@Override
	public Color4f[] getColor4fArray(ArrayList<MguiNumber> list, double min, double max){
		Colour thisColour;
		Color4f[] colours = new Color4f[list.size()];
		for (int i = 0; i < list.size(); i++){
			thisColour = getColourAtValue(list.get(i), min, max);
			if (thisColour == null) 
				thisColour = nullColour;
			colours[i] = thisColour.getColor4f();
			}
		return colours;
	}
	
	protected Colour getColourAtValue(double n){
		return getColourAtValue(n, 0, 1);
	}
	
	protected Colour getColourAtValue(double n, double min, double max){
		return getColourAtValue(new MguiDouble(n), min, max);
	}
	
	protected Colour getColourAtValue(MguiNumber n){
		return getColourAtValue(n, 0, 1);
	}
	
	protected Colour getColourAtValue(MguiNumber n, double min, double max){
		if (anchors.size() < 2) return null;
		int i = 0;
		
		if (max > min)
			n = normalize(n, min, max);
		
		//find enclosing anchors
		if (anchors.get(0).value.compareTo(n) == 0) return anchors.get(0).colour;
		for (i = 0; i < anchors.size() && anchors.get(i).value.compareTo(n) < 0; i++);
		//i -= 1;
		if (i > anchors.size() - 1) return anchors.get(anchors.size() - 1).colour;
		if (i == 0) return anchors.get(0).colour;
		
		//interpolate colour values
		double nInt = getInterpolation(anchors.get(i - 1).value, anchors.get(i).value, n);
		
		return Colours.interpolate(anchors.get(i - 1).colour, anchors.get(i).colour, nInt);
	}
	
	protected MguiNumber normalize(MguiNumber n, double mn, double mx){
		MguiDouble min = new MguiDouble(mn);
		MguiDouble max = new MguiDouble(mx);
		if (n.compareTo(min) < 0) return new MguiDouble(0);
		if (n.compareTo(max) > 0) return new MguiDouble(1);
		MguiDouble d = NumberFunctions.getDifference(n, min);
		MguiDouble len = NumberFunctions.getDifference(max, min);
		d.divide(len);
		return d;
	}
	
	protected double getInterpolation(MguiNumber n1, MguiNumber n2, MguiNumber a3){
		MguiDouble d = NumberFunctions.getDifference(a3, n1);
		MguiDouble len = NumberFunctions.getDifference(n2, n1);
		//d.value = d.value / len.value;
		d.divide(len);
		return d.getValue();
	}
	
	public class Anchor {
		public MguiNumber value;
		public Colour colour;
		
		public Anchor(MguiNumber v, Colour c){
			value = v;
			colour = c;
		}
		
	}
	
	@Override
	public byte[] getDiscreteMap(int size, int channels, boolean setAlphaFromPos){
		
		byte[] map = new byte[size * channels];
		byte[] sample;
		Colour colour;
		Colour dcolour = Colours.getColourNf(new Colour4f(Color.gray), channels);
		
		for (int i = 0; i < size; i++){
			sample = new byte[channels];
			colour = getColourAtValue(i, 0, size - 1);
			if (colour != null)
				colour = Colours.getColourNf(colour, channels);
			else
				colour = dcolour;
			if (channels > 1 && setAlphaFromPos)
				colour.setAlpha((float)i/(float)size);
			Colours.toBytes(colour, sample);
			System.arraycopy(sample, 0, map, i * channels, channels);
			}
		return map;
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public Object clone(){
		ContinuousColourMap map = new ContinuousColourMap();
		//copy anchors
		for (int i = 0; i < anchors.size(); i++)
			map.addAnchor(new MguiDouble(anchors.get(i).value), new Colour4f(anchors.get(i).colour.getColor()));
//		map.mapMin = mapMin;
//		map.mapMax = mapMax;
		map.name = name;
		map.nullColour = nullColour;
		return map;
	}
	
	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		String _tab3 = XMLFunctions.getTab(tab + 2);
		
		String xml = super.getXML(tab);
		xml = xml + " type = 'continuous' >\n";
		
		xml = xml + _tab2 + "<Anchors>\n"; 
		
		for (int i = 0; i < anchors.size(); i++){
			Anchor anchor = anchors.get(i);
			xml = xml + _tab3 + "<Anchor value = '" + anchor.value.toString() +
					  			"' colour = '" + anchor.colour.getShortXML(0) + "' />\n";
			}
		
		xml = xml + _tab2 + "</Anchors>\n"; 
			
		xml = xml + _tab + "</ColourMap>";
		
		return xml;
	}
	
	public String getShortXML(int tab){
		return XMLFunctions.getTab(tab) + "<ColourMap name = '" + getName() + "' type = 'continuous' />";
	}
	
	public XMLObject getXMLInstance(Attributes attributes){
		return new ContinuousColourMap(attributes.getValue("name"));
	}
	
	protected boolean xml_in_anchors = false;
	protected boolean xml_anchors_failed = false;
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
		if (localName.equals("Anchors")){
			xml_in_anchors = true;
			return;
			}
		
		if (localName.equals("Anchor")){
			if (xml_anchors_failed) return;
			if (!xml_in_anchors){
				InterfaceSession.log("ContinuousColourMap: Anchors must occur inside an Anchors block..", 
									 LoggingType.Errors);
				return;
				}
			Colour clr = Colours.parse(attributes.getValue("colour"));
			if (clr == null){
				xml_anchors_failed = true;
				return;	// Will have already given an error message
				}
			this.addAnchor(Double.valueOf(attributes.getValue("value")), clr);
			
		}
		
	}
	
	@Override
	public void handleXMLElementEnd(String localName){
		
		if (localName.equals("Anchors")){
			xml_anchors_failed = false;
			return;
			}
		
		
	}
	
}