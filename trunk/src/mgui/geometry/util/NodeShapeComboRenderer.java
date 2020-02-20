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

package mgui.geometry.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import mgui.interfaces.gui.InterfaceComboBoxRenderer;

/************************************************
 * Extends {@link InterfaceComboBoxRenderer} to allow rendering of node shapes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NodeShapeComboRenderer extends InterfaceComboBoxRenderer {
	
	Color colour = Color.blue;
	
	public NodeShapeComboRenderer(){
		this(false, Color.blue);
	}
	
	/*
	public NodeShapeComboRenderer(HashMap<String, NodeShape> shapes){
		this(shapes, false, Color.blue);
	}
	*/
	
	public NodeShapeComboRenderer(boolean show_icon, Color colour){
		super(show_icon);
		this.colour = colour;
		this.setPreferredSize(new Dimension(30,30));
	}
	
	/*
	public NodeShapeComboRenderer(boolean show_icon, Color colour){
		super(show_icon);
		this.colour = colour;
		this.setPreferredSize(new Dimension(30,30));
		
	}
	*/
	
	@Override
	public Component getListCellRendererComponent(JList list, 
												  Object value,
												  int index, 
												  boolean isSelected, 
												  boolean cellHasFocus) {
		
		if (!(value instanceof NodeShape)) 
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		NodeShape shape = (NodeShape)value;
		JPanel panel = new JPanel();
		panel.setPreferredSize(this.getPreferredSize());
		DrawShapePanel draw_panel = new DrawShapePanel(shape.getShape());
		draw_panel.setPreferredSize(this.getPreferredSize());
		draw_panel.setOpaque(false);
		
		JLabel label = new JLabel(shape.getName());
		panel.setLayout(new BorderLayout());
		panel.add(draw_panel, BorderLayout.WEST);
		panel.add(label, BorderLayout.CENTER);
		panel.setOpaque(false);
		
		setBackground(list.getBackground());
        setForeground(list.getForeground());
		
		return panel;
		
	}
	
	
	
	class DrawShapePanel extends JPanel{
		
		Shape shape;
		
		public DrawShapePanel(Shape shape){
			this.shape = shape;
			this.setOpaque(true);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D)g;
			
			Rectangle bounds = this.getBounds();
			Rectangle shape_bounds = shape.getBounds();
			
			//scale shape to bounds
			float scale_x = (float)bounds.width / (float)shape_bounds.width;
			float scale_y = (float)bounds.height / (float)shape_bounds.height;
			
			float scale = Math.min(scale_x, scale_y) * 0.6f;
			AffineTransform T = AffineTransform.getScaleInstance(scale, scale);
			Shape t_shape = T.createTransformedShape(shape);
			T.setToTranslation((float)bounds.width / 2f,
							   (float)bounds.height / 2f); //+ (float)shape_bounds.height / 2f);
			t_shape = T.createTransformedShape(t_shape);
			g2d.setColor(colour);
			g2d.fill(t_shape);
			g2d.setColor(Color.black);
			g2d.draw(t_shape);
			
		}
		
	}
}