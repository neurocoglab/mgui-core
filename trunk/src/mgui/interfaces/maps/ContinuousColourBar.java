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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.ColourBarOutDialog;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.util.Colour;


/********************************
 * Component displays a continuous colour map as a horizontal bar portraying
 * the colour gradient between anchors. Displays the anchors themselves as solid
 * vertical lines 
 *  
 * @author Andrew Reid
 *
 */
public class ContinuousColourBar extends InterfacePanel implements MouseListener,
														   MouseMotionListener,
														   KeyListener{

	//TODO: make this an InterfaceObject
	public ContinuousColourMap map;
	public int selectedAnchor;
	protected boolean updateBar;
	protected BufferedImage bar;
	protected BasicStroke selectedLine, normalLine;
	protected ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	protected boolean isDragging = false;
	
	//todo make these attributes
	public boolean showAnchors = true;
	public boolean showDivisions = true;
	public int noDivisions = 4;
	public double divSize = 0.2;
	public double max = 1, min = 0;
	public int decimals = -1;
	//public Color divColour = Color.BLACK;
	//public Color backgroundColour = Color.WHITE;
	//public Color labelColour = Color.BLACK;
	public String divFont = "Courier New";
	public double fontScale = 1.0;
	public double lineWeightScale = 1.0;
	public int padding = 0;
	
	protected Font label_font = new Font("Courier New", Font.PLAIN, 16);
	
	public ContinuousColourBar(){
		init();
	}
	
	public ContinuousColourBar(ContinuousColourMap cm){
		init();
		map = cm;
//		cm.mapMin = 0;
//		cm.mapMax = 1;
		selectAnchor(0);
	}
	
	@Override
	protected void init(){
		_init();
		updateBar = true;
		this.setBackground(Color.WHITE);
		normalLine = new BasicStroke();
		selectedLine = new BasicStroke(3f);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	@Override
	public Object clone(){
		ContinuousColourBar bar = new ContinuousColourBar(this.map);
		bar.showAnchors = showAnchors;
		bar.showDivisions = showDivisions;
		bar.noDivisions = noDivisions;
		bar.divSize = divSize;
		//bar.divColour = divColour;
		bar.setForeground(getForeground());
		bar.setBackground(getBackground());
		bar.divFont = divFont;
		bar.padding = padding;
		bar.label_font = label_font;
		bar.min = min;
		bar.max = max;
		bar.fontScale = fontScale;
		bar.lineWeightScale = lineWeightScale;
		bar.decimals = decimals;
		return bar;
	}
	
	@Override
	protected void paintComponent(Graphics g){
		
		int inset_left = getInsets().left + padding;
		int inset_right = getInsets().right + padding;
		int inset_top = getInsets().top + padding;
		int inset_bottom = getInsets().bottom + padding;
		
		int width = getWidth() - inset_left - inset_right;
		int height = getHeight() - inset_bottom - inset_top;
		int div_size = 0;
		
		if (showDivisions){
			if (width > height){
				div_size = (int)(divSize * height);
				height -= div_size;
			}else{
				div_size = (int)(divSize * width * 2f);
				width -= div_size;
				}
			}
			
		Graphics2D g2 = (Graphics2D)g.create();
		
	
		
		if (isOpaque()) { //paint background
            g2.setPaint(getBackground());
			//g2.setPaint(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setPaint(getBackground());
            int gap = (int)(getHeight() / 6.0f);
            int mid = (int)(getHeight() / 2.0f);
            //wtf?
            g2.fillRect(0, mid, getWidth(), mid);
        }
		
		if (map == null) return;
		
		//fill graphics with colour bar image
		bar = getColourBarImage(width, height);
		
		g2.drawImage(bar, inset_left, inset_top, width, height, null);
		g2.setPaint(getForeground());
		
		g2.setStroke(new BasicStroke((float)lineWeightScale));
		
		//draw border
		g2.drawRect(inset_left, inset_top, width - 1, height - 1);
		
		//draw anchors; if anchor is selected, draw differently
		if (showAnchors){
			for (int i = 0; i < map.anchors.size(); i++){
				if (width > height){
					//draw horizontal bar
					int pos = inset_left;
					pos += map.anchors.get(i).value.getValue() * width;
					if (i == selectedAnchor){
						g2.setStroke(selectedLine);
						g2.setPaint(Color.BLACK);
					}else{
						g2.setStroke(normalLine);
						g2.setPaint(Color.WHITE);
						}
					g2.drawLine(pos, inset_top, pos, inset_top + height );
					g2.setStroke(normalLine);
					
					//triangle
					drawAnchorUp(g2, pos, width, inset_top + height, i == selectedAnchor);
				}else{
					//draw vertical bar
					int pos = inset_bottom;
					pos += map.anchors.get(i).value.getValue() * height;
					if (i == selectedAnchor){
						g2.setStroke(selectedLine);
						g2.setPaint(Color.BLACK);
					}else{
						g2.setStroke(normalLine);
						g2.setPaint(Color.WHITE);
						}
					g2.drawLine(inset_left, pos, inset_left + width, pos);
					g2.setStroke(normalLine);
					
					//triangle
					drawAnchorLeft(g2, pos, inset_left + width, height, i == selectedAnchor);
					
					}
				}
			}
		
		//draw divisions
		if (showDivisions && div_size > 1){
			//lines
			
			g2.setPaint(getForeground());
			//g2.setStroke(normalLine);
			g2.setStroke(new BasicStroke((float)lineWeightScale));
			//get point size to fit this bar with n divisions, such that 20% of the space
			//is text, the remaining 80% is white space
			int textSize = getTextPointSize(g2, 10, div_size + 5);
			if (width < height) textSize /= 2;
			
			if (textSize < 0){
				InterfaceSession.log("Error computing font for continuous colour bar...");
				textSize = 10;
				}
			
			textSize = (int)(textSize * fontScale);
			
			double[] number = new double[noDivisions + 1];
			double max_no = 0;
			for (int i = 0; i < noDivisions + 1; i++){
				number[i] = min + i * (max - min) / noDivisions;
				max_no = Math.max(Math.abs(number[i]), max);
				}
			String formatStr = "0";
			
			if (Math.abs(max) > 10E4 || Math.abs(min) > 10E4){
				// Use scientific notation if numbers are too big
				formatStr = "0.00E00";
			}else if (decimals > 0){
				formatStr = "0.";
				for (int j = 0; j < decimals; j++)
					formatStr = formatStr + "0";
			}else if (decimals < 0){
				if (max_no <= 0)
					formatStr = "0.0000";
				else if (max_no < 1)
					formatStr = "0.000";
				else if (max_no < 10)
					formatStr = "0.00";
				else if (max_no < 100)
					formatStr = "0.0";
				else
					formatStr = "0";
				}
			
			Font font = new Font(label_font.getFontName(), label_font.getStyle(), textSize);
			
			FontMetrics metrics = g2.getFontMetrics(font);
			g2.setFont(font);
			int str_height = metrics.getAscent();
			
			for (int i = 0; i < noDivisions + 1; i++){
				if (width > height){
					//draw horizontal
					int pos = inset_left + (int)((float)i * (float)width / noDivisions);
					pos = Math.min(getWidth() - inset_right - 1, pos);
					
					//divider
					g2.drawLine(pos, inset_bottom, pos, getHeight() - inset_top - div_size + 1);
					
					//number
					String str = MguiDouble.getString(number[i], formatStr);
					int str_width = metrics.stringWidth(str);
					pos = Math.max(inset_left, pos - (str_width / 2));
					pos = Math.min(getWidth() - inset_right - str_width, pos);
					
					g2.drawString(str, pos, getHeight() - inset_top );
				}else{
					//draw vertical
					//number
					String str = MguiDouble.getString(number[noDivisions - i], formatStr);
					
					int pos = inset_top + (int)((float)i * (float)height / noDivisions);
					//pos = Math.min(width - getInsets().right - 1, pos);
					
					//divider
					g2.drawLine(inset_left, pos, getWidth() - inset_right - div_size + 1, pos);
										
					pos += str_height / 2f;
					pos = Math.min(inset_top + height, pos);
					pos = Math.max(str_height + inset_top, pos);
					
					g2.drawString(str, getWidth() - inset_right - div_size + 2, pos);
					
					}
				}
			
			}
		
		
	}
	
	private int getTextPointSize(Graphics g, int points, int div_height){
		//div_height -= 1;
		//String testStr = arDouble.getString(max, "0.00");
		//res = acceptable error
		int res = 1;
		//int width = (int)(ratio * (double)getWidth() / noDivisions);
		int count = 0;
		
		while (count < 200){
			FontMetrics metrics = g.getFontMetrics(new Font(divFont, Font.PLAIN, points));
			//int w = metrics.stringWidth(testStr);
			int h = metrics.getHeight();
			if (h >= div_height - 1 && h <= div_height + 1)
			//if (h == div_height)
				return points;
			if (h < div_height){
				//if (metrics.getHeight() == div_height) return points;
				points ++; //= res;
			}else{
				points --; //= res;
				}
			count++;
			}
		return -1;
	}
	
	protected void drawAnchorUp(Graphics2D g2, int pos, int width, int height, boolean selected){
		g2.setPaint(Color.BLACK);
		GeneralPath tri = new GeneralPath(Path2D.WIND_EVEN_ODD, 3);
		int start = Math.max(pos - 5, 0);
		int end = Math.min(pos + 5, getWidth() - getInsets().left - padding);
		//height -= getInsets().bottom + padding;
		
		tri.moveTo(start, height);
		tri.lineTo(end, height);
		tri.lineTo(pos, height - 11);
		tri.closePath();
		
		if (!selected)
			g2.setPaint(Color.WHITE);
		
		g2.fill(tri);
		g2.setPaint(Color.BLACK);
		g2.draw(tri);
		
	}
	
	protected void drawAnchorLeft(Graphics2D g2, int pos, int width, int height, boolean selected){
		g2.setPaint(Color.BLACK);
		GeneralPath tri = new GeneralPath(Path2D.WIND_EVEN_ODD, 3);
		int start = Math.max(pos - 5, 0);
		int end = Math.min(pos + 5, getHeight() - getInsets().right - padding);
		//width -= getInsets().right + padding;
		
		tri.moveTo(width, start);
		tri.lineTo(width, end);
		tri.lineTo(width - 10, pos);
		tri.closePath();
		
		if (!selected)
			g2.setPaint(Color.WHITE);
		
		g2.fill(tri);
		g2.setPaint(Color.BLACK);
		g2.draw(tri);
		
	}
	
	protected BufferedImage getColourBarImage(int x, int y){
		if (map == null) return null;
		//TODO: update ColourMap to specify colour model
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ComponentColorModel model = new ComponentColorModel(cs, null, true, false, 
															Transparency.TRANSLUCENT, 0);
		//get raster
		WritableRaster raster = model.createCompatibleWritableRaster(x, y); 
        
		//get BufferedImage
		BufferedImage bImage = new BufferedImage(model, raster, false, null);
		
		int[] pixel = null;
		Colour thisColour;
		
		int pos = 0, opp = 0;
		if (getWidth() > getHeight()){
			pos = x;
			opp = y;
		}else{
			opp = x;
			pos = y;
			}
		
		//fill x pixel columns from map
		for (int i = 0; i < pos; i++){
			thisColour = map.getColourAtValue((float)i / (float)pos, 0, 1);
			pixel = new int[4];
			pixel[0] = (int)(thisColour.getRed() * 255f);
			pixel[1] = (int)(thisColour.getGreen() * 255f);
			pixel[2] = (int)(thisColour.getBlue() * 255f);
			pixel[3] = (int)(thisColour.getAlpha() * 255f);
			for (int j = 0; j < opp; j++)
				if (getWidth() > getHeight())
					bImage.getRaster().setPixel(i, j, pixel);
				else
					bImage.getRaster().setPixel(j, pos - 1 - i, pixel);
			}
		
		return bImage;
	}
	
	public void setMap(ContinuousColourMap m){
		map = m;
//		m.mapMin = 0;
//		m.mapMax = 1;
		selectAnchor(0);
		update();
	}
	
	public ContinuousColourMap.Anchor getSelectedAnchor(){
		return map.anchors.get(selectedAnchor);
	}
	
	public void addActionListener(ActionListener l){
		actionListeners.add(l);
	}
	
	public void removeActionListener(ActionListener l){
		actionListeners.remove(l);
	}
	
	protected void fireActionListeners(){
		for (int i = 0; i < actionListeners.size(); i++)
			actionListeners.get(i).actionPerformed(new ActionEvent(this, 0, "Colour Map Changed"));
	}
	
	public void selectAnchor(int anchor){
		if (anchor < 0 || anchor >= map.anchors.size()) return;
		selectedAnchor = anchor;
		this.updateUI();
		fireActionListeners();
	}
	
	public void removeAnchor(int anchor){
		if (anchor < 0 || anchor >= map.anchors.size()) return;
		if (selectedAnchor >= anchor && selectedAnchor > 0) selectedAnchor--;
		map.removeAnchor(anchor);
		this.updateUI();
		fireActionListeners();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//anchors can only be editable if they are visible
		if (!showAnchors) return;
		int pos = 0;
		if (getWidth() > getHeight())
			pos = e.getPoint().x;
		else
			pos = getHeight() - e.getPoint().y;
		pos -= this.getInsets().left + padding;
		int width = getWidth() - this.getInsets().left - this.getInsets().right - (padding * 2);
		float searchRadius = 5f / width; //(float)getWidth();
		
		float mapVal = (float)pos / width; //(float)getWidth();
		
		//if double-clicked, add new anchor
		if (e.getClickCount() > 1){

			selectAnchor(map.addAnchor(new MguiFloat(mapVal), map.getColourAtValue(mapVal)));
			return;
			}
		
		
		//if on or near an anchor, select that anchor
		double a, b, min = searchRadius;
		int anchor = -1;
		
		for (int i = 0; i < map.anchors.size(); i++){
			a = map.anchors.get(i).value.getValue();
			b = Math.abs(a - mapVal);
			if (b < min){
				anchor = i;
				min = b;
				}
			}
		
		//if right-clicked, delete
		if (e.isControlDown()){
			//can't delete first and last anchors
			if (anchor <= 0 || anchor == map.anchors.size() - 1) return;
			
			map.anchors.remove(anchor);
			if (anchor == selectedAnchor) selectAnchor(0);
			update();
			return;
		}
		
		if (anchor >= 0)
			selectAnchor(anchor);
		
	}
	
	public void update(){
		updateBar = true;
		//updateUI();
		repaint();
		fireActionListeners();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		isDragging = false; 
		if (e.isPopupTrigger()) showPopupMenu(e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isDragging){
			if (getWidth() > getHeight()){
				int pos = e.getPoint().x;
				double searchRadius = 5f / (double)getWidth();
				double mapVal = (double)pos / (double)getWidth();
				
				double a, b, min = searchRadius;
				int anchor = -1;
				
				for (int i = 0; i < map.anchors.size(); i++){
					a = map.anchors.get(i).value.getValue();
					b = Math.abs(a - mapVal);
					if (b < min){
						anchor = i;
						min = b;
						}
					}
				
				//if an anchor is selected and is not 0 or last, move it
				if (anchor > 0 && anchor < map.anchors.size() - 1)
					selectAnchor(anchor);
			}else{
				int pos = e.getPoint().y;
				double searchRadius = 5f / (double)getHeight();
				double mapVal = (double)pos / (double)getHeight();
				
				double a, b, min = searchRadius;
				int anchor = -1;
				
				for (int i = 0; i < map.anchors.size(); i++){
					a = map.anchors.get(i).value.getValue();
					b = Math.abs(a - mapVal);
					if (b < min){
						anchor = i;
						min = b;
						}
					}
				
				//if an anchor is selected and is not 0 or last, move it
				if (anchor > 0 && anchor < map.anchors.size() - 1)
					selectAnchor(anchor);
				}
			isDragging = true;
		}else{
			if (selectedAnchor == 0 || selectedAnchor == map.anchors.size() - 1){
				isDragging = false;
				return;
			}
			//is anchor between its neighbours?
			double min = map.anchors.get(selectedAnchor - 1).value.getValue();
			double max = map.anchors.get(selectedAnchor + 1).value.getValue();
			
			double mapVal = 0;
			
			if (getWidth() > getHeight()){
				int pos = e.getPoint().x;
				mapVal = (float)pos / (float)getWidth();
			}else{
				int pos = e.getPoint().y;
				mapVal = 1.0 / ((float)pos / (float)getHeight());
				}
			
			if (mapVal > min && mapVal < max){
				map.anchors.get(selectedAnchor).value.setValue(mapVal);
				updateBar = true;
				fireActionListeners();
				updateUI();
				}
			}
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int pos = 0;
		if (getWidth() > getHeight())
			pos = e.getPoint().x;
		else
			pos = getHeight() - e.getPoint().y;
		
		double mapVal = (double)pos / (double)getWidth();
		mapVal = min + (mapVal * (max - min));
		this.setToolTipText(MguiDouble.getString(mapVal, "0.00#####"));
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
	
		
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Save as image.."));
		
		return menu;
	}

	@Override
	public void handlePopupEvent(ActionEvent e) {
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Save as image..")){
			ColourBarOutDialog.showDialog(InterfaceSession.getSessionFrame(), this.map);
			return;
			}
		
	}
	
	/************************************************
	 * Writes this colour bar to an image file. 
	 * 
	 * @param file
	 */
	public void writeToImage(File file) throws IOException{
		writeToImage(file, getWidth(), getHeight());
	}

	/************************************************
	 * Writes this colour bar to an image file. 
	 * 
	 * @param file
	 */
	public void writeToImage(File file, int width, int height) throws IOException{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ContinuousColourBar bar = (ContinuousColourBar)clone();
		bar.setSize(width, height);
		bar.paint(image.getGraphics());
		ImageIO.write(image, "png", file);
		
	}
	
}