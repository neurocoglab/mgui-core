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

package mgui.interfaces.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import mgui.geometry.Ellipse2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiBoolean;

/************************************************
 * Interface for a {@link Ellipse2D} shape.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Ellipse2DInt extends Shape2DInt {

	public Ellipse2DInt(){
		this(new Ellipse2D());
	}
	
	public Ellipse2DInt(Ellipse2D ellipse){
		super();
		init();
		this.setShape(ellipse);
	}
	
	private void init(){
		attributes.add(new Attribute("HasFill", new MguiBoolean(false)));
		attributes.add(new Attribute("FillColour", Color.blue));
	}
	
	public Ellipse2D getEllipse(){
		return (Ellipse2D)getShape();
	}
	
	@Override
	public boolean needsRedraw(Attribute a){
		if (a.getName().equals("LineColour") ||
			a.getName().equals("LineStyle") ||
			a.getName().equals("HasTransparency") ||
				a.getName().equals("Alpha")) 
			return true;
		
		return super.needsRedraw(a);
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine de){
		if (!isVisible()) return;
		
		de.setAttributes(attributes);
		
		de.drawEllipse2D(g, (Ellipse2D)this.getShape());
		
		
	}
	
}