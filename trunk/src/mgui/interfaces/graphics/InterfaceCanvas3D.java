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

package mgui.interfaces.graphics;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Screen3D;
import javax.media.j3d.View;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.Shape2DInt;

import com.sun.j3d.utils.universe.SimpleUniverse;


/*********************************
 * Acts as an InterfaceGraphic wrapper for the Canvas3D object, to enable mouse
 * handling which passes the correct source panel (set here as <graphic3D>). Also
 * allows for handling of mouse events from Canvas3D; AWT events on the parent panel
 * do not get handled until the cursor exits the Canvas3D area.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */

public class InterfaceCanvas3D extends JPanel {

	protected Canvas3D canvas3D;
	private CanvasMouseListener cListener = new CanvasMouseListener();
	private ArrayList<MouseListener> mouseListeners = new ArrayList<MouseListener>();
	private ArrayList<MouseMotionListener> mouseMotionListeners = new ArrayList<MouseMotionListener>();
	private ArrayList<MouseWheelListener> mouseWheelListeners = new ArrayList<MouseWheelListener>();
	public InterfaceGraphic3D graphic3D;
	
	boolean has_been_dragged;
	
	public InterfaceCanvas3D(GraphicsConfiguration config){
		setCanvas(new PostRenderingCanvas3D(this, config));
	}
	
	public InterfaceCanvas3D(Canvas3D c){
		setCanvas(c);
	}
	
	public Canvas3D getCanvas(){
		return canvas3D;
	}
	
	public void setCanvas(Canvas3D c){
		canvas3D = c;
		canvas3D.addMouseListener(cListener);
		canvas3D.addMouseMotionListener(cListener);
		canvas3D.addMouseWheelListener(cListener);
		
		//if (((Boolean)canvas3D.queryProperties().get("texture3DAvailable")).booleanValue())
		//	return;
	}
	
	public void setGraphic3D(InterfaceGraphic3D g){
		graphic3D = g;
	}
	
	//override mouse listeners to add to canvas3D instead
	@Override
	public void addMouseListener(MouseListener m){
		if (m != null)
			mouseListeners.add(m);
	}
	
	@Override
	public void addMouseMotionListener(MouseMotionListener m){
		if (m != null)
			mouseMotionListeners.add(m);
	}
	
	@Override
	public void addMouseWheelListener(MouseWheelListener m){
		if (m != null)
			mouseWheelListeners.add(m);
	}
	
	class CanvasMouseListener extends MouseInputAdapter implements MouseWheelListener {
		
		
		
		@Override
		public void mouseMoved(MouseEvent e) {
			has_been_dragged = false;
			e.setSource(graphic3D);
			for (int i = 0; i < mouseMotionListeners.size(); i++)
				mouseMotionListeners.get(i).mouseMoved(e);
			}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			has_been_dragged = true;
			e.setSource(graphic3D);
			for (int i = 0; i < mouseMotionListeners.size(); i++)
				mouseMotionListeners.get(i).mouseDragged(e);
			}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			has_been_dragged = false;
			e.setSource(graphic3D);
			for (int i = 0; i < mouseWheelListeners.size(); i++)
				mouseWheelListeners.get(i).mouseWheelMoved(e);
			}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			has_been_dragged = false;
			e.setSource(graphic3D);
			for (int i = 0; i < mouseListeners.size(); i++)
				mouseListeners.get(i).mouseClicked(e);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (!has_been_dragged && e.isPopupTrigger()){
				graphic3D.showPopupMenu(e);
				return;
				}
			has_been_dragged = false;
			e.setSource(graphic3D);
			for (int i = 0; i < mouseListeners.size(); i++)
				mouseListeners.get(i).mousePressed(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (!has_been_dragged && e.isPopupTrigger()){
				graphic3D.showPopupMenu(e);
				return;
				}
			has_been_dragged = false;
			e.setSource(graphic3D);
			for (int i = 0; i < mouseListeners.size(); i++)
				mouseListeners.get(i).mouseReleased(e);
		}
	
	}
	
	public void addPostRenderShape(Shape2DInt shape){
		if (canvas3D instanceof PostRenderingCanvas3D)
			((PostRenderingCanvas3D)canvas3D).addPostRenderShape(shape);
	}
	
	public void removePostRenderShape(Shape2DInt shape){
		if (canvas3D instanceof PostRenderingCanvas3D)
			((PostRenderingCanvas3D)canvas3D).removePostRenderShape(shape);
	}
	
	public void clearPostRenderShapes(){
		if (canvas3D instanceof PostRenderingCanvas3D)
			((PostRenderingCanvas3D)canvas3D).clearPostRenderShapes();
	}
	
	public void postRender(){
		if (graphic3D != null)
			graphic3D.postRender();
	}
	
	public BufferedImage getScreenShot(float scale){
		
		try{
			BufferedImage image = null;
			//Canvas3D canvas = canvas3D.getCanvas();
			Point p = new Point();
			p = canvas3D.getLocationOnScreen();
			Rectangle bounds = new Rectangle(p.x, p.y, canvas3D.getWidth(), canvas3D.getHeight());
			Robot robot = new Robot(graphic3D.getGraphicsConfiguration().getDevice());
			image = robot.createScreenCapture(bounds);
			return image;
		}catch(Exception ex){
			return null;
			}
		
		
		// Mtf...
//		if (!(canvas3D instanceof PostRenderingCanvas3D)) return null;
//		return ((PostRenderingCanvas3D)canvas3D).getScreenShot(graphic3D, scale);
	}
	
}