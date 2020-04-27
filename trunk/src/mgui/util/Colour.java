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
import java.io.IOException;
import java.io.Writer;

import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Color4f;

import org.xml.sax.Attributes;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;


public abstract class Colour implements XMLObject{
	
	public float getRed(){return 0;}
	public float getBlue(){return 0;}
	public float getGreen(){return 0;}
	public float getAlpha(){return 0;}
	public float getIntensity(){return 0;}
	public void setRed(int r){setRed(r / 255f);}
	public void setBlue(int b){setBlue(b / 255f);}
	public void setGreen(int g){setGreen(g / 255f);}
	public void setAlpha(int a){setAlpha(a / 255f);}
	public void setIntensity(int i){setIntensity(i / 255f);}
	public void setRed(float r){}
	public void setBlue(float b){}
	public void setGreen(float g){}
	public void setAlpha(float a){}
	public void setIntensity(float i){}
	public int getDims(){return 0;}
	public float getDim(int i){return 0;}
	public void setDim(int i, float val){}
	public Color4f getColor4f(){return null;}
	public Color3f getColor3f(){return null;}
	public Colour getColour(Object o){return null;}
	public void set(Color c){
		setRed(c.getRed());
		setGreen(c.getGreen());
		setBlue(c.getBlue());
		//setAlpha(c.getAlpha());
		}
	public Color getColor(){
		return new Color(getRed(), getGreen(), getBlue(), getAlpha());
	}
	
	public String getDTD() {
		return "";
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

	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
		
		
	}
	
	public void handleXMLElementEnd(String localName) {
		
		
		
	}
	
	public void handleXMLString(String s){
		
	}
	
	public String getLocalName(){
		return "Colour";
	}
	
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		//XML string should be small, so get it directly
		writer.write(getXML(tab));
	}
	
	public String getShortXML(int tab){
		return getXML(tab);
	}
	
}