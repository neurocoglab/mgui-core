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

package mgui.interfaces.graphics;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.GraphicsConfigTemplate3D;
import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.J3DGraphics2D;
import org.jogamp.java3d.Screen3D;
import org.jogamp.java3d.View;
import javax.swing.JOptionPane;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.shapes.Shape2DInt;

/***************************************************
 * Extends {@link Canvas3D} to provide post-rendering capabilities. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class PostRenderingCanvas3D extends Canvas3D {

	protected InterfaceCanvas3D parent;
	protected ArrayList<Shape2DInt> post_render_shapes = new ArrayList<Shape2DInt>();
	public boolean post_render = true;
	public OffScreenCanvas3D offscreen_canvas;
	
	public PostRenderingCanvas3D(InterfaceCanvas3D parent, GraphicsConfiguration graphicsConfiguration) {
		super(graphicsConfiguration);
		this.parent = parent;
	}

	public PostRenderingCanvas3D(InterfaceCanvas3D parent, GraphicsConfiguration graphicsConfiguration, boolean offScreen) {
	    super(graphicsConfiguration, offScreen);
	    this.parent = parent;
	}

	public void addPostRenderShape(Shape2DInt shape){
		post_render_shapes.add(shape);
	}
	
	public void removePostRenderShape(Shape2DInt shape){
		post_render_shapes.remove(shape);
	}
	
	public void clearPostRenderShapes(){
		post_render_shapes = new ArrayList<Shape2DInt>();
	}
	
	@Override
	public void postRender() {
		if (!post_render) return;
		J3DGraphics2D g2d = this.getGraphics2D(); 
		
		if (post_render_shapes.size() > 0){ 
			DrawingEngine de = new DrawingEngine();
			for (int i = 0; i < post_render_shapes.size(); i++)
				post_render_shapes.get(i).drawShape2D(g2d, de);
			}
		
		parent.postRender();
		g2d.flush(true);
	}
	
	@Override
	public void postSwap() {
		if (parent != null)
			parent.postRender();
	}
	
	public void setOffScreenCanvas(OffScreenCanvas3D c) {
		offscreen_canvas = c;
	}
	
	public void unsetOffScreenCanvas() {
		offscreen_canvas = null;
	}
	
	
	/***************************************************
	 * Returns a screen shot created from an offscreen buffer for this canvas. An offscreen canvas must
	 * have already been set via the {@linkplain setOffScreenCanvas} method (otherwise the method returns
	 * {@code null}). 
	 * 
	 * <p>Note that post rendering is (for some reason) not captured in this screen shot. TODO: fix this
	 * 
	 * @return
	 */
	public BufferedImage getScreenShot(InterfaceGraphic3D graphic3D, float scale){

		GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
		template.setDoubleBuffer(GraphicsConfigTemplate3D.UNNECESSARY);
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getBestConfiguration(template);
		OffScreenCanvas3D offCanvas = new OffScreenCanvas3D(gc,true);
		
		Screen3D sOn = this.getScreen3D();
	    Screen3D sOff = offCanvas.getScreen3D();
	    sOff.setSize(sOn.getSize());
	    sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth());
	    sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight());
	    
	    View view = graphic3D.getView();
	    view.addCanvas3D(offCanvas);
		
		Dimension dim = this.getSize();
		dim.width *= scale;
		dim.height *= scale;
		BufferedImage bImage=offCanvas.doRender(dim.width, dim.height);
		offCanvas.waitForOffScreenRendering();

        return bImage;
	}
	
	/**********************************
	 * Taken from:
	 @(#)OffScreenTest.java 1.13 02/10/21 13:46:49
	 *
	 * Copyright (c) 1996-2002 Sun Microsystems, Inc. All Rights Reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions
	 * are met:
	 *
	 * - Redistributions of source code must retain the above copyright
	 *   notice, this list of conditions and the following disclaimer.
	 *
	 * - Redistribution in binary form must reproduce the above copyright
	 *   notice, this list of conditions and the following disclaimer in
	 *   the documentation and/or other materials provided with the
	 *   distribution.
	 *
	 * Neither the name of Sun Microsystems, Inc. or the names of
	 * contributors may be used to endorse or promote products derived
	 * from this software without specific prior written permission.
	 *
	 * This software is provided "AS IS," without a warranty of any
	 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
	 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
	 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
	 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
	 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
	 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
	 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
	 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
	 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
	 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
	 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
	 *
	 * You acknowledge that Software is not designed,licensed or intended
	 * for use in the design, construction, operation or maintenance of
	 * any nuclear facility.
	 */
	class OffScreenCanvas3D extends Canvas3D {

		public OffScreenCanvas3D(GraphicsConfiguration gc){
			super(gc, true);
		}
		
		public OffScreenCanvas3D(GraphicsConfiguration gc, boolean offScreen) {
            super (gc, offScreen);
        }

        BufferedImage doRender(int width, int height) {

            BufferedImage bImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);

            ImageComponent2D buffer = new ImageComponent2D(
                    ImageComponent.FORMAT_RGBA, bImage);

            setOffScreenBuffer(buffer);
            renderOffScreenBuffer();
            waitForOffScreenRendering();
            bImage = getOffScreenBuffer().getImage();

            // To release the reference of buffer inside Java 3D.
            setOffScreenBuffer(null);

            return bImage;
        }

        @Override
		public void postSwap() {
            // No-op since we always wait for off-screen rendering to complete
        }
	}
	
}