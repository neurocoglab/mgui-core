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

package mgui.util;

import java.awt.Color;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;

import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;

import org.xml.sax.Attributes;


public class Colour4f extends Colour {

	Color4f colour;
	
	public Colour4f(){
		
	}
	
	public Colour4f(float r, float g, float b, float a){
		colour = new Color4f(r, g, b, a);
	}
	
	public Colour4f(Color c){
		colour = new Color4f(c);
		//setAlpha(1f);
	}
	
	
	@Override
	public int getDims(){return 4;}
	
	public Color4f getColour(){
		return colour;
	}
	
	public void setColour(Color4f c){
		colour = c;
	}
	
	public void set(Color c){
		setRed(c.getRed());
		setGreen(c.getGreen());
		setBlue(c.getBlue());
		setAlpha(c.getAlpha());
		}
	public Color getColor(){
		return new Color(getRed(), getGreen(), getBlue(), getAlpha());
	}
	
	@Override
	public float getAlpha() {
		if (colour == null) return 0;
		return colour.get().getAlpha() / 255f;
	}

	@Override
	public float getBlue() {
		if (colour == null) return 0;
		return colour.z;
	}

	@Override
	public float getGreen() {
		if (colour == null) return 0;
		return colour.y;
	}

	@Override
	public float getIntensity() {
		if (colour == null) return 0;
		return Math.max(Math.max(getRed(), getBlue()), getGreen());
	}

	@Override
	public float getRed() {
		if (colour == null) return 0;
		return colour.x;
	}

	@Override
	public void setAlpha(float a) {
		colour = new Color4f(getRed(), getGreen(), getBlue(), a);
	}

	@Override
	public void setBlue(float b) {
		colour = new Color4f(getRed(), getGreen(), b, getAlpha());
	}

	@Override
	public void setGreen(float g) {
		colour = new Color4f(getRed(), g, getBlue(), getAlpha());
	}

	@Override
	public void setIntensity(float i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRed(float r) {
		colour = new Color4f(r, getGreen(), getBlue(), getAlpha());
	}
	
	@Override
	public float getDim(int i){
		switch (i){
			case 0:
				return getRed();
			case 1:
				return getGreen();
			case 2:
				return getBlue();
			case 3:
				return getAlpha();
			}
		return -1;
	}

	@Override
	public void setDim(int i, float val){
		switch (i){
			case 0:
				setRed(val);
				return;
			case 1:
				setGreen(val);
				return;
			case 2:
				setBlue(val);
				return;
			case 3:
				setAlpha(val);
				return;
			}
	}
	
	@Override
	public Color4f getColor4f(){
		return new Color4f(colour);
	}
	
	@Override
	public Color3f getColor3f(){
		return new Color3f(colour.getX(), colour.getY(), colour.getZ());
	}
	
	@Override
	public String toString(){
		return "Colour4f: r" + getRed() + 
						" g" + getGreen() + 
						" b" + getBlue() + 
						" a" + getAlpha();
	}
	
	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<Colour4f";
		xml = xml + " red = '" + getRed() + "'";
		xml = xml + " green = '" + getGreen() + "'";
		xml = xml + " blue = '" + getBlue() + "'";
		xml = xml + " alpha = '" + getAlpha() + "'";
		xml = xml + " />";
		
		return xml;
	}
	
	public String getShortXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		return _tab + getRed() + " " + getGreen() + " " + getBlue() + " " + getAlpha();
	}
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
		if (localName.equals(getLocalName())){
			setRed(Float.valueOf(attributes.getValue("red")));
			setGreen(Float.valueOf(attributes.getValue("green")));
			setBlue(Float.valueOf(attributes.getValue("blue")));
			setAlpha(Float.valueOf(attributes.getValue("alpha")));
			}
		
	}
	
	@Override
	public String getLocalName(){
		return "Colour4f";
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new Colour4f(Float.valueOf(attributes.getValue("red")),
							Float.valueOf(attributes.getValue("green")),
							Float.valueOf(attributes.getValue("blue")),
							Float.valueOf(attributes.getValue("alpha")));
	}
	
}