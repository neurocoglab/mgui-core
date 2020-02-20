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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

/****************************************************
 * Extends {@link LineLayout} to provide collapsible categories.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class CategoryLayout extends LineLayout implements ActionListener,
														  CategoryObject{

	

	public ArrayList<catObj> categories = new ArrayList<catObj>();
	public ArrayList<CategoryLayoutConstraints> constraints = new ArrayList<CategoryLayoutConstraints>();
	public int categorySpacing;
	protected int height;
	protected int categoryHeight;
	
	public CategoryLayout(){
		
	}
	
	public CategoryLayout(int height, int gap, int width, int spc){
		this(height, gap, width, spc, height);
	}
	
	public CategoryLayout(int height, int gap, int width, int spc, int cat_height){
		lineHeight = height;
		lineGap = gap;
		lineWidth = width;
		categorySpacing = spc;
		categoryHeight = cat_height;
	}
	
	private void addCategory(String cat, boolean isExp){
		catObj thisCat = new catObj(cat, isExp);
		for (int i = 0; i < categories.size(); i++)
			if (categories.get(i).compareTo(thisCat) == 0)
				return;
		categories.add(thisCat);
	}
	
	@Override
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int thisWidth = parent.getWidth() - (insets.left + insets.right);
		//int lWidth = lineWidth;
		int currentLine = 0;

		//if (lineWidth <= 0)
		int	lWidth = thisWidth;
		
		//set each object
		CategoryLayoutConstraints con;
		Component comp;
		Dimension thisDim;
		int x, y, a, thisLine = 0;
		
		//layout each category
		for (int c = 0; c < categories.size(); c++){
			//find title component 
			a = -1;
			for (int i = 0; i < components.size(); i++){
				if (components.get(i) instanceof CategoryTitle)
					if (((CategoryTitle)components.get(i)).getCategory().compareTo(categories.get(c).category) == 0)
						a = i;
				}
			//if found... draw it
			if (a >= 0){
				comp = components.get(a);
				x = insets.left + (int)(0.05 * lWidth);
				int y_pos = (currentLine * lineHeight) + (currentLine * lineGap) + (c * categorySpacing);
				y_pos += (c * categoryHeight) - (c * lineHeight);
				y = insets.top + y_pos; 
				//thisDim = new Dimension((int)(lWidth * 0.9), lineHeight);
				thisDim = new Dimension((int)(lWidth * 0.9), categoryHeight);
				comp.setBounds(x, y, thisDim.width, thisDim.height);
				thisLine = 0;
				//layout each component in category relative to title
				for (int i = 0; i < components.size(); i++){
					con = constraints.get(i);
					if (con.category.compareTo(categories.get(c).category) == 0){
						comp = components.get(i);
						if (!categories.get(c).isExpanded){
							comp.setVisible(false);
						}else{
							comp.setVisible(true);
							thisDim = new Dimension();
							
							x = insets.left + (int)(con.hPos * lWidth);
							y_pos = ((currentLine + con.lineFrom) * lineHeight)
								   + ((currentLine + con.lineFrom) * lineGap)
								   + (c * categorySpacing);
							y_pos += ((c + 1) * categoryHeight) - ((c + 1) * lineHeight);
							//y = insets.top + ((currentLine + con.lineFrom) * lineHeight)
							//			   + ((currentLine + con.lineFrom) * lineGap)
							//			   + (c * categorySpacing);
							y = insets.top + y_pos;
							thisDim = new Dimension((int)(lWidth * con.hWeight), 
														 (con.lineTo - con.lineFrom + 1) * lineHeight +
														 (con.lineTo - con.lineFrom ) * lineGap);
							comp.setBounds(x, y, thisDim.width, thisDim.height);
							thisLine = Math.max(con.lineTo, thisLine);
							}
						}
					}
				//add space at end of category
				currentLine += thisLine + 1;
				}
			}
	}
	
	protected void setHeight(){
		int currentLine = 0;
		
		//set each object
		CategoryLayoutConstraints con;
		int a, thisLine = 0;
		
		for (int c = 0; c < categories.size(); c++){
			//find title component 
			a = -1;
			for (int i = 0; i < components.size(); i++){
				if (components.get(i) instanceof CategoryTitle)
					if (((CategoryTitle)components.get(i)).getCategory().compareTo(categories.get(c).category) == 0)
						a = i;
				}
			//if found... draw it
			if (a >= 0){
				thisLine = 0;
				//layout each component in category relative to title
				for (int i = 0; i < components.size(); i++){
					con = constraints.get(i);
					if (con.category.compareTo(categories.get(c).category) == 0)
						if (categories.get(c).isExpanded)
							thisLine = Math.max(con.lineTo, thisLine);
					}
				//add space at end of category
				currentLine += thisLine + 1;
				}
			}
		height = (currentLine) * (lineHeight + lineGap) + (categories.size() * categorySpacing);
	}
	
	protected int getHeight(){
		return height;
	}
	
	@Override
	public void removeLayoutComponent(Component comp) {
		int i = components.indexOf(comp);
		if (i < 0) return;
		/**@TODO remove category if this is a CategoryTitle component **/
		components.remove(i);
		constraints.remove(i);
	}
	
	@Override
	public void addLayoutComponent(Component comp, Object cons) {
		if (comp == null || cons == null) return;
		//components.add(comp);
		//if this is a CategoryTitle, add it as such
		if (comp instanceof CategoryTitle){
			CategoryTitle c = (CategoryTitle)comp;
			addCategory(c.getCategory(), c.isExpanded);
			//action listener to handle title click events
			c.setActionCommand(c.getCategory());
			c.addActionListener(this);
			}
		
		components.add(comp);
		constraints.add((CategoryLayoutConstraints)cons);
		setHeight();
		setPreferredSize(new Dimension(200, getHeight()));
	}
	
	protected CategoryTitle getCategoryComponent(String category){
		for (int i = 0; i <  components.size(); i++)
			if (components.get(i) instanceof CategoryTitle){
				CategoryTitle title = (CategoryTitle)components.get(i);
				if (title.category.equals(category))
						return title;
			}
		return null;
					
	}
	
	//handle title click events by switching isExtended value
	public void actionPerformed(ActionEvent e){
		
		for (int i = 0; i < categories.size(); i++)
			if (categories.get(i).category.compareTo(e.getActionCommand()) == 0){
				//toggle isExtended switch
				setExpanded(categories.get(i), !categories.get(i).isExpanded);
				
				//redraw parent
				updateParent((CategoryTitle)e.getSource());
				break;
				}
	}
	
	void setExpanded(catObj category, boolean b){
		category.isExpanded = b;
		CategoryTitle title = getCategoryComponent(category.category);
		if (title != null){
			title.isExpanded = b;
			title.repaint();
			}
	}
	
	public void updateParent(CategoryTitle c){
		setHeight();
		setPreferredSize(new Dimension(200, getHeight()));
		c.updateParentObj();
	}
	
	private class catObj implements Serializable{
		String category;
		boolean isExpanded;
		
		public catObj(String cat, boolean isExp){
			category = cat;
			isExpanded = isExp;
		}
		
		public int compareTo(catObj obj){
			return category.compareTo(obj.category);
		}
		
	}
	
	public void collapseAllCategories() {
		for (int i = 0; i < categories.size(); i++)
			setExpanded(categories.get(i), false);
		
		setHeight();
		setPreferredSize(new Dimension(200, getHeight()));
	}

	public void collapseCategory(String cat) {
		// TODO Auto-generated method stub
		
	}

	public void collapseOtherCategories(String cat) {
		for (int i = 0; i < categories.size(); i++)
			if (categories.get(i).category.equals(cat))
				setExpanded(categories.get(i), true);
			else
				setExpanded(categories.get(i), false);
		
		setHeight();
		setPreferredSize(new Dimension(200, getHeight()));
	}

	public void expandAllCategories() {
		for (int i = 0; i < categories.size(); i++)
			setExpanded(categories.get(i), true);
		
		setHeight();
		setPreferredSize(new Dimension(200, getHeight()));
	}

	public void expandCategory(String cat) {
		// TODO Auto-generated method stub
		
	}
	
}