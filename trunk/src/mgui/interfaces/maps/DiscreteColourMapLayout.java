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

package mgui.interfaces.maps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import mgui.interfaces.gui.ColourButton;
import mgui.interfaces.layouts.InterfaceLayoutPanel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.util.Colour;

/********************************************************************
 * Layout which displays a discrete colour map as a list of colour boxes, names, and optionally descriptions and
 * indexes.
 * 
 * @author Andrew Reid
 * @since 1.0
 * @version 1.0
 *
 */
public class DiscreteColourMapLayout extends InterfaceLayoutPanel {

	public DiscreteColourMap map;
	public int box_size;
	public Font font;
	LineLayout lineLayout;
	public int preferred_y;
	
	//TODO set with attributes
	
	public DiscreteColourMapLayout(DiscreteColourMap map){
		this(map, 20, new Font("Arial", Font.PLAIN, 12), Color.white);
	}
	
	public DiscreteColourMapLayout(DiscreteColourMap map, Color background){
		this(map, 20, new Font("Arial", Font.PLAIN, 12), background);
	}
	
	public DiscreteColourMapLayout(DiscreteColourMap map, int box_size, Font font, Color background){
		this.map = map;
		this.box_size = box_size;
		this.font = font;
		this.setBackground(background);
		init2();
	}
	
	void init2(){
		
		super.init();
		if (map == null) return;
		
		
		lineLayout = new LineLayout((int)(box_size * 1.2), (int)(box_size * 0.5), 100);
		setLayout(lineLayout);
		
		//Iterator<Integer> itr = map.colours.keySet().iterator();
		ArrayList<Integer> keys = new ArrayList<Integer>(map.colours.keySet());
		Collections.sort(keys);
		
		//title
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		JLabel label = new JLabel(map.getName());
		label.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize() + 2));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		add(label, c);
		
		int i = 3;
		
		//while (itr.hasNext()){
		for (int j = 0; j < keys.size(); j++){
			//int index = itr.next();
			int index = keys.get(j);
			String str = "" + index;
			boolean skip = false;
			if (map.hasNameMap()){
				str = map.nameMap.get(index);
				if (str == null) skip = true; // str = "N/V";
				}
			if (!skip){
				Colour colour = map.getColour(index);
				c = new LineLayoutConstraints(i, i, 0.05, 0.2, 1);
				ColourButton button = new ColourButton("" + index, colour.getColor(), true);
				button.setBackground(colour.getColor());
				button.setForeground(Color.black);
				add(button, c);
				c = new LineLayoutConstraints(i, i, 0.3, 0.65, 1);
				
				
				label = new JLabel(str);
				label.setHorizontalAlignment(SwingConstants.LEFT);
				label.setFont(font);
				add(label, c);
				i++;
				}
			}
		
		this.setPreferredSize(new Dimension(100, (i + 1) * (int)(box_size * 1.2 + box_size * 0.5)));
		
		
	}
	
	
}