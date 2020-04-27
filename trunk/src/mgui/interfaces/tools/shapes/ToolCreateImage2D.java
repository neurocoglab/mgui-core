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

package mgui.interfaces.tools.shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jogamp.vecmath.Point2f;

import mgui.geometry.Rect2D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.Image2DInt;
import mgui.interfaces.shapes.util.ImageFilter;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool2D;


/*******************************************
 * Tool allows user to select a file or url pointing to an image, and specify
 * rectangular bounds for that image in model space.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class ToolCreateImage2D extends Tool2D {

	public Point startPt, lastPt;
	int deltaX, deltaY;
	
	public ToolCreateImage2D(){
		super();
		init();
	}
	
	private void init(){
		name = "Create Image 2D: First corner ";
		
		
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		Map2D map = (Map2D)targetPanel.getMap();
		switch(e.getEventType()){
		
		case ToolConstants.TOOL_MOUSE_MOVED:
			if (toolPhase > 0){
				deltaX = Math.abs(e.getPoint().x - startPt.x);
				deltaY = Math.abs(e.getPoint().y - startPt.y);
				
				Point2f p = new Point2f(map.getMapDist(deltaX),
										map.getMapDist(deltaY));			
				name = "Zoom Window 2D: " + mgui.numbers.NumberFunctions.getPoint2fStr(p, "##0.00");
				Graphics2D g = (Graphics2D)targetPanel.getGraphics();
				g.setXORMode(Color.RED);
				if (lastPt != null)
					g.drawRect(Math.min(startPt.x, lastPt.x), Math.min(startPt.y, lastPt.y),
							   Math.abs(lastPt.x - startPt.x), Math.abs(lastPt.y - startPt.y));
				g.drawRect(Math.min(startPt.x, e.getPoint().x), Math.min(startPt.y, e.getPoint().y),
						   deltaX, deltaY);
			
				lastPt = e.getPoint();
				}
			
			break;
				
			case ToolConstants.TOOL_MOUSE_CLICKED:
				switch (toolPhase){
					case 0:
						if (targetPanel.getToolLock())
							break;
						targetPanel.setToolLock(true);
						//set start pt
						startPt = e.getPoint();
						lastPt = null;
						name = "Create Image 2D: Second corner ";
						toolPhase = 1;
						break;
					
					case 1:
						//load and draw image
						JFileChooser fc = new JFileChooser("Select image file");
						fc.setFileFilter(new ImageFilter());
						int returnVal = fc.showOpenDialog(InterfaceSession.getSessionFrame());
						
						if (returnVal == JFileChooser.APPROVE_OPTION){
							//load and draw image
							try{
								BufferedImage img = ImageIO.read(fc.getSelectedFile());
								//todo set img dimensions to power of two
								
								//temp: assigns an alpha channel based upon pixel intensity
								img = mgui.util.Colours.getRGBtoRGBA(img, false, 0, 1);
								
								int w = img.getWidth(null);
					            int h = img.getHeight(null);
					            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					            
					            Graphics g = bi.getGraphics();
					            g.drawImage(img, 0, 0, null);
					            
								Rect2D rect = new Rect2D(map.getMapPoint(startPt),
														 map.getMapPoint(e.getPoint()));
								Image2DInt image = new Image2DInt(rect, img, true);
								image.updateShape();
								targetPanel.addShape2D(image);
								}
							catch (IOException ex){
								ex.printStackTrace();
								JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(),
															  "Error loading image.");
								}
							}
							
						toolPhase = 0;
						name = "Create Image 2D: first corner";
						targetPanel.updateDisplays();
						targetPanel.setToolLock(false);
						break;

					}
				break;
				}
		}
	
	@Override
	public Object clone(){
		return new ToolCreateImage2D();
	}
	
	
}