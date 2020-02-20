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

package mgui.io.imaging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import mgui.io.standard.imaging.GreyFileLoader;
import mgui.io.standard.imaging.RGBFileLoader;

public class ImagingIOFunctions {

	public static boolean convertRawRGBtoPNG(String input_file, String output_file, int width, int height, int data_type) throws IOException{
		  
		  File input = new File(input_file);
		  if (!input.exists()) throw new IOException("File '" + input_file + "' not found..");
		  File output = new File(output_file);
		  if (output.exists() && !output.delete())
			  throw new IOException ("Cannot delete existing file '" + output_file + "'");
		  if (!output.createNewFile())
			  throw new IOException ("Cannot create file '" + output_file + "'");
		  
		  try{
			  BufferedImage image = (new RGBFileLoader(input)).loadImage(width, height, data_type);
			  ImageIO.write(image, "png", output);
			  
			  return true;
		  
		  }catch (IOException e){
			  e.printStackTrace();
			  return false;
		  	}
		  
	  }
	  
	  public static boolean convertRawGreytoPNG(String input_file, String output_file, int width, int height, int data_type) throws IOException{
		  
		  File input = new File(input_file);
		  if (!input.exists()) throw new IOException("File '" + input_file + "' not found..");
		  File output = new File(output_file);
		  if (output.exists() && !output.delete())
			  throw new IOException ("Cannot delete existing file '" + output_file + "'");
		  if (!output.createNewFile())
			  throw new IOException ("Cannot create file '" + output_file + "'");
		  
		  try{
			  BufferedImage image = (new GreyFileLoader(input)).loadImage(width, height, data_type);
			  ImageIO.write(image, "png", output);
			  
			  return true;
		  
		  }catch (IOException e){
			  e.printStackTrace();
			  return false;
		  	}
		  
	  }
	  
	  /******************************************************
	   * Writes the given image to a png-format image file.
	   * 
	   * @param image
	   * @param file
	   * @return
	   */
	  public static boolean writeImageToPng(BufferedImage image, File file){
			try{
				ImageIO.write(image, "png", file);
				return true;
			}catch (IOException e){
				e.printStackTrace();
				return false;
				}
		}
	
	
}