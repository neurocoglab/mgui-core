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

import mgui.interfaces.xml.XMLObject;

import org.xml.sax.Attributes;


public class Colour2f extends Colour {

	@Override
	public int getDims(){return 2;}
	
	public Colour2f(){
		
	}
	
	public Colour2f(float red, float green){
		setRed(red);
		setGreen(green);
	}
	
	@Override
	public float getAlpha() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getBlue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGreen() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getIntensity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getRed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(int a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBlue(int g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGreen(int b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIntensity(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRed(int r) {
		// TODO Auto-generated method stub
		
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new Colour2f(Float.valueOf(attributes.getValue("red")),
							Float.valueOf(attributes.getValue("green")));
	}

}