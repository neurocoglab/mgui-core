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

import java.io.File;

import mgui.interfaces.ProgressUpdater;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;


public abstract class VideoWriter extends FileWriter {

	
	@Override
	public boolean write(InterfaceIOOptions options,
						 ProgressUpdater progress_bar) {
		
		VideoOutputOptions v_options = (VideoOutputOptions)options;
		
		File[] files = options.getFiles();
		boolean success = true;
		
		for (int i = 0; i < files.length; i++){
			setFile(files[i]);
			success &= writeVideo(v_options, progress_bar);
			}
		
		return success;
	}
	
	public abstract boolean writeVideo(VideoOutputOptions options, ProgressUpdater progress_bar);
	
}