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

package mgui.io.domestic.maps;

import java.awt.Color;

import javax.vecmath.Vector3f;

import mgui.interfaces.maps.ContinuousColourMap;
import mgui.util.Colour;
import mgui.util.Colour3f;


/***************************************************
 * Provides a colour mapping for a vector of size three, such that each element of the
 * vector has a colour assignment, and the resulting colour is a weighting of these
 * colours by x, y, z components:<p>
 * 
 * colour.red = x_red * x_component + y_red * y_component + z_red * z_component<br>
 * colour.green = x_green * x_component + y_green * y_component + z_green * z_component<br>
 * colour.blue = x_blue * x_component + y_blue * y_component + z_blue * z_component
 * 
 * @author Andrew Reid
 *
 */

public class Vector3ColourMap extends ContinuousColourMap {

	public Colour3f x_colour = new Colour3f(Color.red);
	public Colour3f y_colour = new Colour3f(Color.green);
	public Colour3f z_colour = new Colour3f(Color.blue);
	
	public Vector3ColourMap(){
		
	}
	
	public Vector3ColourMap(Color x, Color y, Color z){
		x_colour = new Colour3f(x);
		y_colour = new Colour3f(y);
		z_colour = new Colour3f(z);
	}
	
	public Vector3ColourMap(Colour3f x, Colour3f y, Colour3f z){
		setColours(x, y, z);
	}
	
	public void setColours(Colour3f x, Colour3f y, Colour3f z){
		x_colour = x;
		y_colour = y;
		z_colour = z;
	}
	
	public Colour getColourForVector(Vector3f v){
		
		float red = x_colour.getRed() * v.x +  
					y_colour.getRed() * v.y +
					z_colour.getRed() * v.z;
		
		if (red > 1f) red = 1f;
		if (red < 0) red = 0;
		
		float green = x_colour.getGreen() * v.x +  
					  y_colour.getGreen() * v.y +
					  z_colour.getGreen() * v.z;
		
		if (red > 1f) green = 1f;
		if (red < 0) green = 0;
		
		float blue = x_colour.getBlue() * v.x +  
					 y_colour.getBlue() * v.y +
					 z_colour.getBlue() * v.z;
		
		if (red > 1f) blue = 1f;
		if (red < 0) blue = 0;
		
		return new Colour3f(red, green, blue);
		
	}
	
	
}