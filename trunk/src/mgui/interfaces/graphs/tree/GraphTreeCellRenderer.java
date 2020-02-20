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

package mgui.interfaces.graphs.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import mgui.interfaces.trees.TreeObject;
import mgui.resources.icons.IconObject;


public class GraphTreeCellRenderer extends JPanel implements TreeCellRenderer {

	protected JTree tree;
	protected boolean selected;
	protected boolean hasFocus;
	
	protected JLabel label;
	
	protected Color selectedBG;
	
	public GraphTreeCellRenderer(){
		super();
		init();
	}
	
	protected void init(){
		this.setLayout(new BorderLayout());
		label = new JLabel();
		label.setOpaque(true);
		add(label, BorderLayout.CENTER);
		
		selectedBG = new Color(212, 212, 255);
	}
	
	public Color getSelectedBackground(){
		return selectedBG;
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		String s = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof TreeObject)
			s = ((TreeObject)value).getTreeLabel();
		
		this.tree = tree;
		this.hasFocus = hasFocus;
		this.selected = selected;
		label.setText(s);
		
		if (selected)
			label.setBackground(getSelectedBackground());
		else
			label.setBackground(tree.getBackground());
		
		label.setFont(tree.getFont());
		Icon icon = null;
		if (value instanceof IconObject)
			icon = ((IconObject)value).getObjectIcon();
		if (icon != null){
			label.setIcon(icon);
			}
		
		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		
		//custom stuff here
		
		
		super.paint(g);
	}
	
	
}