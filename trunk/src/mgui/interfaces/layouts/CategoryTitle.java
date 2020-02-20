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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;

/******************************************************
 * A title for a category using the <code>CategoryLayout</code> layout. Overrides the look-and-feel.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.layouts.CategoryLayout
 *
 */
public class CategoryTitle extends JButton implements PopupMenuObject, 
													  MouseListener {

	public boolean isExpanded;
	public InterfacePanel parentObj;
	protected String category;
	
	BufferedImage right_arrow, down_arrow;
	
	public CategoryTitle(String cat){
		this(cat, cat);
	}
	
	public CategoryTitle(String cat, String label){
		this(cat, label, false);
	}
	
	public CategoryTitle(String cat, String label, boolean isExt){
		super(label);
		category = cat;
		isExpanded = isExt;
		this.setOpaque(false);
		init();
	}
	
	public String getCategory(){
		return category;
	}
	
	private void init(){
		//set up button appearance here
		this.setBackground(new Color(200, 200, 200));
		this.addMouseListener(this);
	}
	
	public void setParentObj(InterfacePanel c){
		parentObj = c; 
	}
	
	public void updateParentObj(){
		if (parentObj != null)
			//parentObj.update(parentObj.getGraphics());
			parentObj.updateUI();
	}
	
	public InterfacePopupMenu getPopupMenu() {
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Expand all"));
		menu.addMenuItem(new JMenuItem("Collapse all"));
		menu.addMenuItem(new JMenuItem("Collapse others"));
		
		return menu;
	}

	public void handlePopupEvent(ActionEvent e) {
		
		if (parentObj == null) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Collapse others")){
			parentObj.collapseOtherCategories(getText());
			return;
			}
		
		if (item.getText().equals("Collapse all")){
			parentObj.collapseAllCategories();
			return;
			}
		
		if (item.getText().equals("Expand all")){
			parentObj.expandAllCategories();
			return;
			}
		
	}
	
	@Override
	protected void paintBorder(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		Line2D.Double shape = new Line2D.Double(1, getHeight() - 2, getWidth() - 2, getHeight() - 2);
		g2d.setPaint(Color.black);
		g2d.setStroke(new BasicStroke(2f));
		g2d.draw(shape);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		
		Graphics2D g2d = (Graphics2D)g;
		
		//draw arrow and label
		Font font = new Font("Courier New", Font.BOLD, 15);
		TextLayout layout = new TextLayout(category, font, g2d.getFontRenderContext());
		
		//draw background
		Rectangle bounds = layout.getPixelBounds(g2d.getFontRenderContext(), 0, 0);
		
		if (this.isOpaque()){
			g2d.setColor(this.getBackground());
			g2d.fill(this.getBounds());
			}
		
		//center text
		int start_x = (int)((this.getWidth() / 2f) - bounds.getCenterX());
		int start_y = (int)((this.getHeight() / 2f) - bounds.getCenterY()-5);
		
		g2d.setColor(Color.BLUE);
		layout.draw(g2d, start_x, start_y);
		
		BufferedImage arrow = null;
		
		if (this.isExpanded)
			arrow = getDownArrow();
		else
			arrow = getRightArrow();
		
		if (arrow != null)
			g2d.drawImage(arrow, 1, 1, null);
		
	}

	BufferedImage getDownArrow(){
		if (down_arrow == null){
			java.net.URL imgURL = CategoryTitle.class.getResource("/mgui/resources/icons/down_arrow_18.png");
			try{
				if (imgURL != null){
					BufferedImage image = ImageIO.read(imgURL);
					//get some alpha
					down_arrow = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
					WritableRaster raster = image.getRaster();
					WritableRaster out_raster = down_arrow.getRaster();
					
					for (int i = 0; i < raster.getWidth(); i++)
						for (int j = 0; j < raster.getHeight(); j++){
							int[] pixel = raster.getPixel(i, j, (int[])null);
							int[] pixel2 = out_raster.getPixel(i, j, (int[])null);
							int min = Integer.MAX_VALUE;
							for (int k = 0; k < 3; k++){
								pixel2[k] = pixel[k];
								min = Math.min(pixel[k], min);
								}
							if (min > 150)
								pixel2[3] = 255 - min;
							else
								pixel2[3] = 255;
							out_raster.setPixel(i, j, pixel2);
							}
							
				}else{
					InterfaceSession.log("Cannot find resource: /mgui/resources/icons/down_arrow_18.png");
					}
			}catch (IOException e){
				InterfaceSession.log("Cannot load resource: /mgui/resources/icons/down_arrow_18.png");
				}
			}
		return down_arrow;
	}
	
	BufferedImage getRightArrow(){
		if (right_arrow == null){
			java.net.URL imgURL = CategoryTitle.class.getResource("/mgui/resources/icons/right_arrow_18.png");
			try{
				if (imgURL != null){
					BufferedImage image = ImageIO.read(imgURL);
					//get some alpha
					right_arrow = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
					WritableRaster raster = image.getRaster();
					WritableRaster out_raster = right_arrow.getRaster();
					
					for (int i = 0; i < raster.getWidth(); i++)
						for (int j = 0; j < raster.getHeight(); j++){
							int[] pixel = raster.getPixel(i, j, (int[])null);
							int[] pixel2 = out_raster.getPixel(i, j, (int[])null);
							int min = Integer.MAX_VALUE;
							for (int k = 0; k < 3; k++){
								pixel2[k] = pixel[k];
								min = Math.min(pixel[k], min);
								}
							if (min > 150)
								pixel2[3] = 255 - min;
							else
								pixel2[3] = 255;
							out_raster.setPixel(i, j, pixel2);
							}
							
				}else{
					InterfaceSession.log("Cannot find resource: /mgui/resources/icons/right_arrow_18.png");
					}
			}catch (IOException e){
				InterfaceSession.log("Cannot load resource: /mgui/resources/icons/right_arrow_18.png");
				}
			}
		return right_arrow;
	}
	
	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}
	
	public void mouseClicked(MouseEvent e) {
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}
	
	
}