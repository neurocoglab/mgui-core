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

package mgui.io.domestic.videos;

import java.awt.Dimension;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.GraphicsConfigTemplate3D;
import org.jogamp.java3d.GraphicsContext3D;
import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.Raster;
import org.jogamp.java3d.Screen3D;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.InterfaceCanvas3D;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.graphics.video.Video;
import mgui.interfaces.graphics.video.VideoEvent;
import mgui.interfaces.graphics.video.VideoTask;
import mgui.interfaces.logs.LoggingType;
import mgui.numbers.MguiFloat;
import mgui.util.ImageFunctions;
import foxtrot.Job;
import foxtrot.Worker;

/**********************************************************
 * Outputs a <code>Video</code> to a stack of images which can be used to compile a video using a utility
 * such as VirtualDub.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.graphics.video.Video
 *
 */
public class ImageStackVideoWriter extends VideoWriter {

	public boolean use_offscreen_buffer = true;
	
	public ImageStackVideoWriter(){
		
	}
	
	public ImageStackVideoWriter(File file){
		setFile(file);
	}
	
	@Override
	public boolean writeVideo(VideoOutputOptions options,
							  ProgressUpdater progress_bar) {
		
		//we must run through the video from start to stop 
		//and output n frames per "second"
		
		if (options.window instanceof InterfaceGraphic3D)
			return writeVideo3D((ImageStackVideoOptions)options, progress_bar);
		
		return false;
	}
	
	public boolean writeVideo3D(final ImageStackVideoOptions options,
			 					final ProgressUpdater progress_bar) {
		
		if (progress_bar == null){
//			if (options.use_offscreen_buffer)
//				return writeFromOffscreenBufferBlocking(options, progress_bar);
//			else
				return writeFromScreenshotsBlocking(options, progress_bar);
			}
		
		return (Boolean)Worker.post(new Job(){
			@Override
			public Boolean run(){
				progress_bar.setMinimum((int)(options.start_time / 1000));
				progress_bar.setMaximum((int)(options.stop_time / 1000));
				progress_bar.reset();
				if (options.use_offscreen_buffer)
					return writeFromOffscreenBufferBlocking(options, progress_bar);
				else
					return writeFromScreenshotsBlocking(options, progress_bar);
				}
			});
		
	}
	
	protected boolean writeFromOffscreenBufferBlocking(ImageStackVideoOptions options,
														ProgressUpdater progress_bar){
		
		InterfaceGraphic3D window = (InterfaceGraphic3D)options.window; 
		
		ArrayList<VideoTask> tasks = options.video.tasks;
		long refresh = 1000 / options.images_per_second; 
		long duration = options.stop_time - options.start_time;
		long total_count = duration / refresh;
		int digits = String.valueOf(total_count).length();
		
		VideoEvent ev = new VideoEvent(options.video);
		Video video = options.video;
		
		InterfaceCanvas3D int_canvas = window.getInterfaceCanvas3D();
		Canvas3D canvas = int_canvas.getCanvas();
		Dimension size = canvas.getSize();
		//int_canvas.setOffScreenCanvas(size.width, size.height);
		
		InterfaceSession.log("Video '" + video.getName() + "' resumed at " + video.clock + " (duration = " + duration + ")");
		
		try{
			//loop through tasks until stop
			video.start();
			
			int count = 1;
			while (!video.isStopped()) {
				
				for (int i = 0; i < tasks.size(); i++)
					tasks.get(i).perform(window, video.clock);
			
				BufferedImage buffer_image = int_canvas.getScreenShot(1f);
				
				if (video.clock >= options.start_time){
//					File output_file = new File(dataFile.getAbsolutePath() + 
//												System.getProperty("file.separator") + 
//												video.getName() +
//												" (" + (count++) + ").png");
					File output_file = new File(dataFile.getAbsolutePath() + 
												System.getProperty("file.separator") + 
												video.getName() +
												"_" + getCountStr(count, digits) + ".png");
					
					video.clock += refresh;
					video.fireClockChanged(ev);
					
					try{
						//wait for 
						if (options.wait > 0)
							Thread.sleep(options.wait);
						ImageIO.write(buffer_image, "png", output_file);
					}catch (IOException e){
						//unset buffer
						InterfaceSession.log("Error outputting to '" + output_file.getAbsolutePath() + "'");
						
						//e.printStackTrace();
						return false;
					}catch (InterruptedException e){
						InterfaceSession.handleException(e, LoggingType.Errors);
						//e.printStackTrace();
						}
					}
				
				if (progress_bar != null){
					if (progress_bar.isCancelled()) return false;
					progress_bar.update((int)(video.clock / 1000));
					}
				
				if (video.clock > options.stop_time) video.stop();
				}
			
		}catch (Exception e){
			InterfaceSession.handleException(e, LoggingType.Errors);
			//e.printStackTrace();
			return false;
			}
		
		return true;
	}
	
	private String getCountStr(int count, int digits){
		
		String str = "";
		int size = String.valueOf(count).length();
		for (int i = 0; i < digits-size; i++)
			str = str + "0";
		str = str + count;
		return str;
	}
	
	protected boolean writeFromScreenshotsBlocking(ImageStackVideoOptions options,
													ProgressUpdater progress_bar){
		
		InterfaceGraphic3D window = (InterfaceGraphic3D)options.window; 
		
		ArrayList<VideoTask> tasks = options.video.tasks;
		long refresh = 1000 / options.images_per_second; 
		long duration = options.stop_time - options.start_time;
		long total_count = duration / refresh;
		int digits = String.valueOf(total_count).length();
		
		VideoEvent ev = new VideoEvent(options.video);
		Video video = options.video;
		
		InterfaceCanvas3D int_canvas = window.getInterfaceCanvas3D();
		
		InterfaceSession.log("Video '" + video.getName() + "' resumed at " + video.clock + " (duration = " + duration + ")");
		
		try{
			//loop through tasks until stop
			video.start();
			
			int count = 1;
			while (!video.isStopped()) {
				
				for (int i = 0; i < tasks.size(); i++)
					tasks.get(i).perform(window, video.clock);
			
				BufferedImage buffer_image = int_canvas.getScreenShot(1f);
				
				// Resample if necessary
				if (options.resample != null){
					buffer_image = ImageFunctions.getResampledImage(buffer_image, options.resample.width, options.resample.height);
					}
				
				if (video.clock >= options.start_time){
					File output_file = new File(dataFile.getAbsolutePath() + 
												System.getProperty("file.separator") + 
												video.getName() + //"_" +
												"_" + getCountStr(count++, digits) + ".png");
					
					video.clock += refresh;
					video.fireClockChanged(ev);
					
					try{
						//wait for 
						if (options.wait > 0)
							Thread.sleep(options.wait);
						ImageIO.write(buffer_image, "png", output_file);
					}catch (IOException e){
						//unset buffer
						InterfaceSession.log("Error outputting to '" + output_file.getAbsolutePath() + "'");
						e.printStackTrace();
						return false;
					}catch (InterruptedException e){
						e.printStackTrace();
						}
					}
				
				if (progress_bar != null){
					if (progress_bar.isCancelled()) return false;
					progress_bar.update((int)(video.clock / 1000));
					}
				
				if (video.clock > options.stop_time) video.stop();
				}
		}catch (Exception e){
			e.printStackTrace();
			return false;
			}
		
		return true;
		
	}
	
	
//	protected Canvas3D setOffscreenBuffer(InterfaceGraphic3D window, Canvas3D canvas, BufferedImage image){
//		GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
//        template.setDoubleBuffer(GraphicsConfigTemplate.UNNECESSARY);
//        GraphicsConfiguration gc = GraphicsEnvironment
//                .getLocalGraphicsEnvironment().getDefaultScreenDevice()
//                .getBestConfiguration(template);
//		Canvas3D c = new Canvas3D(gc, true);
//		ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGB, image);
//		buffer.setCapability(ImageComponent.ALLOW_IMAGE_READ);
//		buffer.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
//		//c.setOffScreenBuffer(buffer);
//		Screen3D screen = canvas.getScreen3D();
//		c.getScreen3D().setSize(screen.getSize());
//		c.getScreen3D().setPhysicalScreenHeight(screen.getPhysicalScreenHeight());
//		c.getScreen3D().setPhysicalScreenWidth(screen.getPhysicalScreenWidth());
//		c.setOffScreenLocation(c.getLocationOnScreen());
//		c.startRenderer();
//		canvas.getView().stopView();
//		window.getView().addCanvas3D(c);
//		canvas.getView().startView();
//		return c;
//	}
//	
//	protected void unsetOffscreenBuffer(InterfaceGraphic3D window, Canvas3D shot){
//		window.getView().removeCanvas3D(shot);
//	}
	
	protected BufferedImage getBufferImage(InterfaceGraphic3D window){
		Dimension size = window.canvas3D.getCanvas().getSize();
		return new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		
	}
	
	protected BufferedImage getScreenShot_bak3(InterfaceGraphic window) {
		Dimension size = window.getSize();
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        window.paint(image.getGraphics());
		return image;
	}
	
	protected BufferedImage getScreenShot3D_bak4(InterfaceGraphic3D window){
		
		Canvas3D c = window.canvas3D.getCanvas();
		Screen3D on = c.getScreen3D();
        Canvas3D shot=new Canvas3D(c.getGraphicsConfiguration(), true);
        c.getView().stopView();
        c.getView().addCanvas3D(shot);
        c.getView().startView();
        Screen3D off = shot.getScreen3D();
        off.setSize(on.getSize());
        off.setPhysicalScreenHeight(on.getPhysicalScreenHeight());
        off.setPhysicalScreenWidth(on.getPhysicalScreenWidth());
        shot.setOffScreenLocation(c.getLocationOnScreen());

        BufferedImage bi=new BufferedImage(c.getWidth(),c.getHeight(),BufferedImage.TYPE_INT_ARGB);
        ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGBA, bi);
        shot.setOffScreenBuffer(buffer);
        shot.renderOffScreenBuffer();
        shot.waitForOffScreenRendering();
        BufferedImage res = shot.getOffScreenBuffer().getImage();
        c.getView().removeCanvas3D(shot);
        return res;
		
	}
	
	//takes actual screen shot rather than using offscreen buffer, which has some kind of bug
	//with some graphics hardware... see
	protected BufferedImage getScreenShot3D(InterfaceGraphic3D window) {
		Canvas3D canvas = window.canvas3D.getCanvas();
		BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		Point p = new Point();
		p = canvas.getLocationOnScreen();
		Rectangle bounds = new Rectangle(p.x, p.y, canvas.getWidth(), canvas.getHeight());
		
		try{
			Robot robot = new Robot(window.getGraphicsConfiguration().getDevice());
			return robot.createScreenCapture(bounds);
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}
	}
	
	protected BufferedImage getScreenShot(Canvas3D canvas3d) {

		GraphicsContext3D ctx = canvas3d.getGraphicsContext3D();
		java.awt.Dimension scrDim = canvas3d.getSize();

		ImageComponent2D image = new ImageComponent2D(ImageComponent.FORMAT_RGB,
				  									  scrDim.width, 
				  									  scrDim.height);
		
		Raster ras = new Raster();
        ras.setType(Raster.RASTER_COLOR);
        ras.setCapability(Raster.ALLOW_IMAGE_READ); 
		ras.setCapability(Raster.ALLOW_IMAGE_WRITE);
		
		ras.setSize(scrDim);
		ras.setImage(image);
				
		ctx.readRaster(ras);
		BufferedImage img = ras.getImage().getImage();

		return img;
		}

}