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

package mgui.io.standard.imaging;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JPanel;

public class RGBFileLoader extends ImageFileLoader {

	public RGBFileLoader(File file){
		setFile(file);
	}
	
	//TODO: this could probably be more efficient
	@Override
	public BufferedImage loadImage(int width, int height, int data_type) throws IOException {
		
		DataInputStream dis = new DataInputStream(new FileInputStream(dataFile));
		ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 
												false, 
												false, 
												Transparency.OPAQUE, 
												data_type);
		
		WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
	
		// Fill the raster with the pixel values (which will be read one by one from the file)
		for(int h=0; h < height; h++)
			for(int w = 0; w < width; w++)
				for (int i = 0; i < 3; i++){
					double x = Double.NaN;
					switch (data_type){
						case DataBuffer.TYPE_BYTE:
							x = dis.read();
							break;
						case DataBuffer.TYPE_SHORT:
							x = dis.readShort();
							break;
						case DataBuffer.TYPE_INT:
							x = dis.readInt();
							break;
						case DataBuffer.TYPE_FLOAT:
							x = dis.readFloat();
							break;
						case DataBuffer.TYPE_DOUBLE:
							x = dis.readDouble();
							break;
						}
					raster.setSample(w, h, i, x);
					}
				
		dis.close();
		BufferedImage image = new BufferedImage(cm, raster, false, null);
		
		return image;
		
	}
	
	static class ImagePanel extends JPanel {

		BufferedImage image;
		
		public ImagePanel(BufferedImage image){
			this.image = image;
		}
		
		@Override
		public void paintComponent(Graphics g){
			
			Graphics2D g2 = (Graphics2D)g;
			g2.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
			
		}
		
		
	}

	
	
}