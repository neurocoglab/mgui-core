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

package mgui.interfaces.graphics.video;

import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.xml.XMLObject;

import org.xml.sax.Attributes;

/*********************************************************
 * A {@link Video} operating on an {@link InterfaceGraphic3D} window or its
 * associated objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Video3D extends Video implements Comparable<Video3D> {

		//default duration = 1 min
	
	public Video3D(){
		init();
	}
	
	public Video3D(String name){
		init();
		setName(name);
	}
	
	public Video3D(InterfaceGraphic3D g3d){
		init();
		setWindow(g3d);
	}
	
	public Video3D(String name, InterfaceGraphic3D g3d){
		init();
		setName(name);
		setWindow(g3d);
	}
	
	public void setWindow(InterfaceGraphic3D g3d){
		window = g3d;
	}
	
	@Override
	public String getType(){
		return "3D";
	}
	
	@Override
	public String toString(){
		return "Video3D: " + getName();
	}
	
	public int compareTo(Video3D o) {
		return getName().compareTo(o.getName());
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new Video3D(attributes.getValue("name"));
	}
	
}