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

package mgui.interfaces.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JColorChooser;

/**************************************************
 * Overrides renderer to colour component regardless of look and feel.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ColourButton extends JButton {

	Color colour = Color.gray;
	boolean show_label = false;
	float border_width = 2f;
	boolean show_dialog = false;
	
	public ColourButton(){
		super();
	}
	
	public ColourButton(Color colour){
		super();
		this.colour = colour;
	}
	
	public ColourButton(String text){
		this(text, Color.gray);
	}
	
	public ColourButton(String text, Color colour){
		super(text);
		this.colour = colour;
	}
	
	public ColourButton(String text, Color colour, boolean show_label){
		super(text);
		this.colour = colour;
		this.show_label = show_label;
	}
	
	public ColourButton(String text, Color colour, boolean show_label, boolean show_dialog){
		super(text);
		this.colour = colour;
		this.show_label = show_label;
		
		
	}
	
	ActionListener current_listener = null;
	
	public void showDialog(final boolean show_dialog, final String title, final Component parent){
		this.show_dialog = show_dialog;
		if (show_dialog){
			if (current_listener != null)
				this.removeActionListener(current_listener);
			current_listener = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					Color new_colour = JColorChooser.showDialog(parent, title, getColour());
					if (new_colour != null)
						setColour(new_colour);
				}
			};
			this.addActionListener(current_listener);
		}else{
			if (current_listener != null)
				this.removeActionListener(current_listener);
			}
	}
	
	public void showLabel(boolean b){
		this.show_label = b;
		this.repaint();
	}
		
	public void setColour(Color colour){
		this.colour = colour;
		repaint();
	}
	
	public Color getColour(){
		return colour;
	}
	
	public void setBorderWidth(float w){
		this.border_width = w;
		repaint();
	}
	
	@Override
	protected void paintBorder(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		Rectangle2D.Double drawRect = new Rectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2);
		g2d.setPaint(Color.black);
		g2d.setStroke(new BasicStroke(this.border_width));
		g2d.draw(drawRect);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		Rectangle2D.Double drawRect = new Rectangle2D.Double(border_width, 
															 border_width, 
															 getWidth() - border_width * 2, 
															 getHeight() - border_width * 2);
		g2d.setPaint(colour);
		g2d.fill(drawRect);
		
		if (this.show_label == false) return;
		String text = getText();
		
		if (text == null || text.length() == 0) return;
		
		FontRenderContext frc = g2d.getFontRenderContext();
		TextLayout layout = new TextLayout(text, getFont(), frc);
		
		float advance = layout.getAdvance();
		float width = this.getWidth() - this.getInsets().left - this.getInsets().right;
		float delta_w = width - advance;
		
		while (delta_w < 2 && text.length() > 0){
			text = text.substring(0, text.length() - 1);
			layout = new TextLayout(text + "..", getFont(), frc);
			advance = layout.getAdvance();
			delta_w = width - advance;
			}
		
		float ascent = layout.getAscent();
		float height = this.getHeight() - this.getInsets().top - this.getInsets().bottom;
		float delta_h = (height - ascent) / 2f;
		float start_x = (float)getInsets().left + delta_w / 2f;
		float start_y = (float)getInsets().top + delta_h + ascent;
		
		g2d.setPaint(this.getForeground());
		layout.draw(g2d, start_x, start_y);
		
	}
	
	
}