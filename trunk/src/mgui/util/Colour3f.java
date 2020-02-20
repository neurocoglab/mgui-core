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

import org.xml.sax.Attributes;

import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;


public class Colour3f extends Colour {

	Color3f colour;
	
	@Override
	public int getDims(){return 3;}
	
	public Colour3f(){
		super();
		colour = new Color3f();
	}
	
	public Colour3f(float red, float green, float blue){
		super();
		colour = new Color3f(red, green, blue);
	}
	
	public Colour3f(Color c){
		colour = new Color3f(c);
	}
	
	@Override
	public float getAlpha() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public float getBlue() {
		// TODO Auto-generated method stub
		return colour.z;
	}

	@Override
	public float getGreen() {
		// TODO Auto-generated method stub
		return colour.y;
	}

	/*
	public float getIntensity() {
		// TODO Auto-generated method stub
		return 0;
	}*/

	@Override
	public float getRed() {
		// TODO Auto-generated method stub
		return colour.x;
	}

	/*
	public void setAlpha(float a) {
		// TODO Auto-generated method stub
		
	}*/

	@Override
	public void setBlue(float b) {
		// TODO Auto-generated method stub
		colour.set(getRed(), getGreen(), b);
	}

	@Override
	public void setGreen(float g) {
		// TODO Auto-generated method stub
		colour.set(getRed(), g, getBlue());
	}

	@Override
	public void setIntensity(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRed(float r) {
		// TODO Auto-generated method stub
		colour.set(r, getGreen(), getBlue());
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
			}
	}
	
	@Override
	public String toString(){
		return "Colour3f: r" + getRed() + 
						" g" + getGreen() + 
						" b" + getBlue();
	}
	
	
	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<Colour3f";
		xml = xml + " red = '" + getRed() + "'";
		xml = xml + " green = '" + getGreen() + "'";
		xml = xml + " blue = '" + getBlue() + "'";
		xml = xml + " />";
		
		return xml;
	}
	
	public String getShortXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		return _tab + getRed() + " " + getGreen() + " " + getBlue();
	}
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
		if (localName.equals(getLocalName())){
			setRed(Float.valueOf(attributes.getValue("red")));
			setGreen(Float.valueOf(attributes.getValue("green")));
			setBlue(Float.valueOf(attributes.getValue("blue")));
			}
		
	}
	
	@Override
	public Color4f getColor4f(){
		return new Color4f(colour.getX(), colour.getY(), colour.getZ(), 1f);
	}
	
	@Override
	public Color3f getColor3f(){
		return new Color3f(colour.getX(), colour.getY(), colour.getZ());
	}
	
	@Override
	public String getLocalName(){
		return "Colour3f";
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new Colour3f(Float.valueOf(attributes.getValue("red")),
							Float.valueOf(attributes.getValue("green")),
							Float.valueOf(attributes.getValue("blue")));
	}
	
}