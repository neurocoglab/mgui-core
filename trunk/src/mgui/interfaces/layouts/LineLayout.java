/*
* Copyright (C) 2011 Andrew Reid and the modelGUI Project <http://mgui.wikidot.com>
* 
* This file is part of modelGUI[core] (mgui-core).
* 
* modelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* modelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with modelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

package mgui.interfaces.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.ArrayList;

/***************************************
 * Layout which provides a basic functionality for rendering components on horizontal lines. Uses
 * {@link LineLayoutConstraints} to specify line height, gap height, and preferred width.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class LineLayout implements LayoutManager2,
								   Serializable {

	public int lineHeight;
	public int lineGap;
	public int lineWidth;
	public Dimension preferredSize = new Dimension (200, 900);
	public Dimension minimumSize = new Dimension (100, 100);
	public Dimension maximumSize = new Dimension (1000, 5000);
	public ArrayList<LineLayoutConstraints> constraints = new ArrayList<LineLayoutConstraints>();
	public ArrayList<Component> components = new ArrayList<Component>();
	protected Component flexible_component = null;
	
	public LineLayout(){
		
	}
	
	/***********************************************************
	 * Construct a new {@code LineLayout} with the given dimensions 
	 * 
	 * @param height
	 * @param gap
	 * @param width
	 */
	public LineLayout(int height, int gap, int width){
		lineHeight = height;
		lineGap = gap;
		lineWidth = width;
	}
	
	public void addLayoutComponent(String str, Component comp) {

	}
	
	/********************************************
	 * Sets the flexible component for this layout; this component is stretched vertically when the layout
	 * is larger than is total line + gap height, to fill the extra space. A value of {@code null} unsets
	 * the flexible component.
	 * 
	 * @param c
	 */
	public void setFlexibleComponent(Component c){
		this.flexible_component = c;
	}

	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int width = parent.getWidth() - (insets.left + insets.right);
			int height = parent.getHeight() - (insets.top + insets.bottom);
			int flexible_height = -1;
			boolean flexible_passed = false;
			
			if (flexible_component != null){
				// We need to get the total height, and determine whether
				// there is space remaining.
				
				int total_height = 0;
				int flex_pos = -1;
				for (int i = 0; i < components.size(); i++){
					LineLayoutConstraints constraint = constraints.get(i);
					Component component = components.get(i);
					if (component == flexible_component)
						flex_pos = i;
					Dimension dim = new Dimension();
					if (component.isVisible()){
						int x = insets.left + (int)(constraint.hPos * width);
						int y = insets.top + (constraint.lineFrom * lineHeight) + 
											 (constraint.lineFrom * lineGap);
						dim = new Dimension((int)(width * constraint.hWeight), 
													 (constraint.lineTo - constraint.lineFrom + 1) * lineHeight +
													 (constraint.lineTo - constraint.lineFrom ) * lineGap);
						if (constraint.lineTo < 0)
							dim.height = height - y;
						total_height = y + dim.height;
						}
					}
				if (flex_pos > -1 && total_height < height){
					// add the difference, in increments of lineHeight
					flexible_height = 0;
					while(total_height + flexible_height < height - lineHeight - lineGap){
						flexible_height += lineHeight;
						}
					}
				}
			
			//set each object
			LineLayoutConstraints con;
			Component comp;
			Dimension thisDim;
			int x, y;
			for (int i = 0; i < components.size(); i++){
				con = constraints.get(i);
				comp = components.get(i);
				boolean is_flexible = false;
				if (!flexible_passed && flexible_component != null && comp == flexible_component){
					is_flexible = true;
					}
				thisDim = new Dimension();
				if (comp.isVisible()){
					x = insets.left + (int)(con.hPos * width);
					y = insets.top + 
						(con.lineFrom * lineHeight) + 
						(con.lineFrom * lineGap);
					thisDim = new Dimension((int)(width * con.hWeight), 
												 (con.lineTo - con.lineFrom + 1) * lineHeight +
												 (con.lineTo - con.lineFrom ) * lineGap);
					if (con.lineTo < 0)
						thisDim.height = height - y;
					if (is_flexible)
						thisDim.height += flexible_height;
					if (flexible_passed)
						y += flexible_height;
					comp.setBounds(x, y, thisDim.width, thisDim.height);
					}
				if (is_flexible)
					flexible_passed = true;
				}
			}
	}
	
	public Dimension minimumLayoutSize(Container arg0) {
		return minimumSize;
	}

	public void setPreferredSize(Dimension size){
		preferredSize = size;
	}
	
	public Dimension preferredLayoutSize(Container parent) {	
		return preferredSize;
	}

	public void removeLayoutComponent(Component comp) {
		int i = components.indexOf(comp);
		if (i < 0) return;
		components.remove(i);
		constraints.remove(i);
	}
	
	public void addLayoutComponent(Component comp, Object cons) {
		if (comp == null || cons == null) return;
		components.add(comp);
		constraints.add((LineLayoutConstraints)cons);
	}

	
	public float getLayoutAlignmentX(Container parent) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getLayoutAlignmentY(Container parent) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void invalidateLayout(Container parent) {
		// TODO Auto-generated method stub
		
	}

	public Dimension maximumLayoutSize(Container parent) {
		// TODO Auto-generated method stub
		return maximumSize;
	}
 
}