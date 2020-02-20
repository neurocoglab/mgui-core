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

package mgui.interfaces.shapes;

import java.awt.Color;

import mgui.geometry.Grid3D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.maps.ColourMap;
import mgui.io.domestic.maps.Vector3ColourMap;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.util.Colour3f;


public class TensorVolume3DInt extends Volume3DInt {

	protected Vector3ColourMap cmap;
	
	
	public TensorVolume3DInt(){
		super();
		init2();
	}
	
	public TensorVolume3DInt(Grid3D g,  ColourMap cmap){
		
		super(g, cmap);
		init2();
		
	}
	
	private void init2(){
	
		attributes.add(new Attribute<Color>("X Colour", Color.red));
		attributes.add(new Attribute<Color>("Y Colour", Color.green));
		attributes.add(new Attribute<Color>("Z Colour", Color.blue));
		
		setColourMap();
		
	}
	
	public Colour3f getXColour(){
		Color c = (Color)attributes.getValue("X Colour");
		if (c == null) return null;
		return new Colour3f(c);
	}
	
	public Colour3f getYColour(){
		Color c = (Color)attributes.getValue("Y Colour");
		if (c == null) return null;
		return new Colour3f(c);
	}
	
	public Colour3f getZColour(){
		Color c = (Color)attributes.getValue("Z Colour");
		if (c == null) return null;
		return new Colour3f(c);
	}
	
	public void setXColour(Color c){
		attributes.setValue("X Colour", new Colour3f(c));
	}
	
	public void setYColour(Color c){
		attributes.setValue("Y Colour", new Colour3f(c));
	}
	
	public void setZColour(Color c){
		attributes.setValue("Z Colour", new Colour3f(c));
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		if (e.getAttribute().getName().endsWith(" Colour")){
			setColourMap();
			fireShapeModified();
			return;
			}
		super.attributeUpdated(e);
	}
	
	protected void setColourMap(){
		if (cmap == null){
			cmap = new Vector3ColourMap(getXColour(), getYColour(), getZColour());
		}else{
			cmap.setColours(getXColour(), getYColour(), getZColour());
			}
	}
	
}