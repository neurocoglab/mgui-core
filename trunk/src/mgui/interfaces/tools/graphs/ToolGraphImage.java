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

package mgui.interfaces.tools.graphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jogamp.vecmath.Point2f;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphs.shapes.GraphImage;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.util.ImageFilter;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;

/***********************************************
 * Tool which inserts or appends an image to a graph window.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolGraphImage extends ToolGraph {

	int toolPhase = 0;
	Point startPt, lastPt;
	int deltaX, deltaY;
	Type type = Type.Append;
	
	public enum Type{
		Insert,
		Append;
	}
	
	public ToolGraphImage(){
		this(Type.Append);
	}
	
	public ToolGraphImage(Type type){
		this.type = type;
		name = getTypeStr() + " image: First corner:";
	}
	
	String getTypeStr(){
		switch (type){
			case Append:
				return "Append";
			case Insert:
				return "Insert";
			}
		return "?";
	}
	
	public void handleToolEvent(ToolInputEvent e){
		
		switch(e.getEventType()){
		
			case ToolConstants.TOOL_MOUSE_MOVED:
				if (toolPhase > 0){
					deltaX = Math.abs(e.getPoint().x - startPt.x);
					deltaY = Math.abs(e.getPoint().y - startPt.y);
							
					name = getTypeStr() + " image: " + e.getPoint().x + ", " + e.getPoint().y;
					Graphics2D g = (Graphics2D)target_panel.getGraphics();
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
						if (target_panel.getToolLock())
							break;
						target_panel.setToolLock(true);
						//set start pt
						startPt = e.getPoint();
						lastPt = null;
						name = getTypeStr() + " image: " + e.getPoint().x + ", " + e.getPoint().y;
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
								
					            Point2f p1 = target_panel.getMouseCoords(startPt);
					            Point2f p2 = target_panel.getMouseCoords(e.getPoint());
					            
					            GraphImage image = new GraphImage(img, p1, p2);
					            
					            switch (type){
						            case Append:
							            target_panel.appendPaintable(image);
							            break;
						            case Insert:
							            target_panel.insertPaintable(image);
							            break;
						            }
								}
							catch (IOException ex){
								ex.printStackTrace();
								JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(),
															  "Error loading image.");
								}
							}
							
						toolPhase = 0;
						name = "Append Image: first corner:";
						target_panel.setToolLock(false);
						target_panel.finishTool();
						break;

					}
				break;
			}
		
		
		
	}
	
	@Override
	public Object clone() {
		return new ToolGraphImage(type);
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isExclusive() {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showPopupMenu(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}