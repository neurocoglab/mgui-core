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
import java.util.ArrayList;
import java.util.TreeSet;

import mgui.geometry.LineSegment2D;
import mgui.geometry.Plane3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Shape;
import mgui.geometry.Vector2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.GraphicEvent;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.graphics.InterfaceGraphicListener;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiBoolean;

/**************************************************
 * Provides a 2D representation of a 3D section set, where it intersects a particular plane.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SectionSet2DInt extends Shape2DInt implements InterfaceGraphicListener {

	/*
	 *  The 3D section set is represented in 2D by a 2D origin point (the point where the section set normal
	 *  intersections the cutting plane), and a 2D normal vector (the 3D normal projected onto the cutting
	 *  plane). The normal will be scaled by the spacing value.
	 * 
	 *  TODO: set current sections somehow...
	 * 
	 */
	Plane3D cutting_plane;
	SectionSet3DInt parent;
	ArrayList<Integer> current_sections = new ArrayList<Integer>();
	ArrayList<InterfaceGraphic2D> current_windows = new ArrayList<InterfaceGraphic2D>(); 
	
	public SectionSet2DInt(){
		super();
		init();
	}
	
	public SectionSet2DInt(SectionSet3DInt parent, Plane3D plane){
		super();
		init();
		this.parent = parent;
		this.cutting_plane = plane;
	}
	
	private void init(){
		
//		attributes.add(new Attribute<MguiBoolean>("DrawOnlyCurrent2D", new MguiBoolean(false)));
//		attributes.add(new Attribute<MguiBoolean>("HighlightCurrent2D", new MguiBoolean(false)));
//		attributes.add(new Attribute<Color>("BackgroundColour", Color.LIGHT_GRAY));
		
	}
	
	@Override
	public Shape getGeometryInstance(){
		return null;
	}
	
	public boolean getDrawOnlyCurrent2D(){
		return ((MguiBoolean)attributes.getValue("DrawOnlyCurrent2D")).getTrue();
	}
	
	public boolean getHighlightCurrent2D(){
		return ((MguiBoolean)attributes.getValue("HighlightCurrent2D")).getTrue();
	}
	
	public Color getBackgroundColour(){
		return (Color)attributes.getValue("BackgroundColour");
	}
	
	public boolean isCurrent(int section){
		for (int i = 0; i < current_sections.size(); i++)
			if (current_sections.get(i).intValue() == section)
				return true;
		return false;
	}
	
	//override this method to draw the object
	@Override
	protected void draw(Graphics2D g, DrawingEngine d){
		
		if (parent == null || cutting_plane == null){
			InterfaceSession.log("SectionSet2DInt.draw: parent or cutting plane not set...");
			return;
			}
		
		updateCurrentSections();
		
		for (int i = 0; i < current_sections.size(); i++)
			drawSection(g, d, current_sections.get(i));
		return;
		
	}
	
	protected void updateCurrentSections(){
		
		for (int i = 0; i < current_windows.size(); i++){
			current_windows.get(i).removeGraphicListener(this);
			}
		
		current_windows.clear();
		current_sections.clear();
		
		InterfaceDisplayPanel display_panel = InterfaceSession.getWorkspace().getDisplayPanel();
		if (display_panel == null) return;
		ArrayList<InterfaceGraphicWindow> windows = display_panel.getAllWindows();
		
		TreeSet<Integer> sections = new TreeSet<Integer>();
		
		for (int i = 0; i < windows.size(); i++){
			InterfaceGraphicWindow window = windows.get(i);
			if (window.getPanel() instanceof InterfaceGraphic2D){
				SectionSet3DInt section_set = (SectionSet3DInt)window.getPanel().getSource();
				if (section_set == this.getParentShape()){
					// Passes, get polygon
					InterfaceGraphic2D window2d = (InterfaceGraphic2D)window.getPanel();
					int section = window2d.getCurrentSection();
					sections.add(section);
					current_windows.add(window2d);
					window2d.addGraphicListener(this);
					}
				}
			}
		
		current_sections.addAll(sections);
	}
	
	protected void drawSection(Graphics2D g, DrawingEngine d, int section){
		
		Rect2D bounds = d.getMap().getMapBounds();
		Plane3D section_plane = parent.getPlaneAt(section);
		
		//need to find segment where intersection line crosses bounds
		Vector2D int_line = GeometryFunctions.getIntersectionLine(section_plane, cutting_plane);
		if (int_line == null) return;
		
		LineSegment2D segment = GeometryFunctions.getIntersectionSegment(int_line, bounds);
		
		if (segment != null){
			d.setAttribute("LineColour", getLineColour());
			d.drawLine2D(g, segment);
			}
		
	}
	
	@Override
	public void updateShape(){
		
	}

	@Override
	public void graphicUpdated(GraphicEvent e) {
		this.fireShapeModified();
	}

	@Override
	public void graphicSourceChanged(GraphicEvent e) {
		this.fireShapeModified();
	}
	
	
}