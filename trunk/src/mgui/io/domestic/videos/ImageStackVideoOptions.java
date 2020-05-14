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
import java.io.File;

import javax.swing.JFileChooser;


/******************************************************************
 * Options for a video stack output operation; i.e., vis {@linkplain ImageStackVideoWriter}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ImageStackVideoOptions extends VideoOutputOptions {

	public int images_per_second;
	public long start_time, stop_time;
	public File output_folder;
	public long wait;
	public boolean use_offscreen_buffer = false;
	public Dimension resample = null;
	
	public ImageStackVideoOptions(){
		this(30, 0, 1000, 20, null, null);
	}
	
	public ImageStackVideoOptions(int images_per_second, long start, long stop, long wait, File output_folder){
		this.images_per_second = images_per_second;
		this.start_time = start;
		this.stop_time = stop;
		this.output_folder = output_folder;
		this.wait = wait;
	}
	
	public ImageStackVideoOptions(int images_per_second, long start, long stop, long wait, File output_folder, Dimension resample){
		this.images_per_second = images_per_second;
		this.start_time = start;
		this.stop_time = stop;
		this.output_folder = output_folder;
		this.wait = wait;
		this.resample = resample;
	}
	
	public JFileChooser getFileChooser(){
		File f = output_folder;
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Select directory for image stack video output");
		return fc;
	}
	
	public void setFiles(File[] files){
		output_folder = files[0];
	}
	
	public File[] getFiles(){
		return new File[]{output_folder}; 
	}
	
	@Override
	public JFileChooser getFileChooser(File f) {
		// TODO Auto-generated method stub
		return null;
	}
	
}