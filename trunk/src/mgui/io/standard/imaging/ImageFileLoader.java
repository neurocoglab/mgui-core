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

import java.awt.image.BufferedImage;
import java.io.IOException;

import mgui.interfaces.ProgressUpdater;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;


public abstract class ImageFileLoader extends FileLoader {

	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadImage(getImageWidth(), getImageHeight(), getImageDataType());
	}
	
	public abstract BufferedImage loadImage(int x, int y, int data_type) throws IOException;
	
	@Override
	public boolean load(InterfaceIOOptions options,
						ProgressUpdater progress_bar) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public int getImageWidth(){
		return -1;
	}
	
	public int getImageHeight(){
		return -1;
	}
	
	public int getImageDataType(){
		return -1;
	}

}