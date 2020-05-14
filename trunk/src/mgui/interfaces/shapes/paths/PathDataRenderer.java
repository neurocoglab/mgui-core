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

package mgui.interfaces.shapes.paths;

import java.util.ArrayList;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;

/*******************************************************
 * Specifies how data attached to a path is to be rendered.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class PathDataRenderer extends AbstractInterfaceObject implements AttributeObject {
	
	AttributeList attributes = new AttributeList();
	
	static ArrayList<String> render_types;
	
	protected void init(){
		attributes.add(new Attribute<String>("Name", "No-name"));
		attributes.add(new Attribute<MguiBoolean>("IsVisible", new MguiBoolean(true)));
		
		//rendering attributes
		attributes.add(new Attribute<MguiFloat>("Offset", new MguiFloat(1f)));
		attributes.add(new Attribute<MguiBoolean>("IsRelative", new MguiBoolean(false)));
		ArrayList<String> render_types = getRenderTypes();
		AttributeSelection<String> attr_sel = new AttributeSelection<String>("RenderType", render_types, String.class, render_types.get(0));
		attributes.add(attr_sel);
		
		updateRenderAttributes();
		
	}
	
	public abstract Object getValueAtDistance(float depth);
	
	protected void updateRenderAttributes(){
		//TODO: replace current render category with one that matches current render type 
		attributes.removeCategory("Render2D");
		
	}
	
	public static ArrayList<String> getRenderTypes(){
		if (render_types == null){
			render_types = new ArrayList<String>();
			render_types.add("Line Graph");
			render_types.add("Bar Graph");
			render_types.add("Filled Shapes");
			render_types.add("Tick Marks");
			}
		return render_types;
	}
	
	@Override
	public Attribute getAttribute(String attrName) {
		return attributes.getAttribute(attrName);
	}

	@Override
	public AttributeList getAttributes() {
		return attributes;
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		this.attributes = thisList;
	}


}