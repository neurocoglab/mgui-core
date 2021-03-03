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

package mgui.io.domestic.shapes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiNumber;


public abstract class SurfaceDataFileWriter extends FileWriter {

	public abstract boolean writeValues(ArrayList<MguiNumber> values, ProgressUpdater progress_bar) throws IOException;
	public abstract void setFormat(String format);
	
	/*****************************************************
	 * Writes the set of values to the data file.
	 * 
	 * @param values
	 * @return
	 */
	public boolean writeValues(ArrayList<MguiNumber> values) throws IOException{
		return writeValues(values, null);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar){
		if (options == null || !(options instanceof SurfaceDataOutputOptions)) return false;
		
		SurfaceDataOutputOptions opts = (SurfaceDataOutputOptions)options;
		if (opts.mesh == null) return false;
		setFile(opts.files[0]);
		File dir = new File(dataFile.getAbsolutePath());
		
		boolean success = true;
		for (int j = 0; j < opts.columns.size(); j++){
			setFile(new File(dir.getAbsolutePath() + File.separator + opts.prefix + opts.filenames.get(j)));
			setFormat(opts.formats.get(j));
			try{
				if (!writeValues(opts.mesh.getVertexData(opts.columns.get(j)), progress_bar))
					success = false;
			}catch (IOException ex){
				InterfaceSession.handleException(ex);
				return false;
				}
			}
		
		dataFile = dir;
		return success;
	}
	
	@Override
	public String getSuccessMessage(){
		return "Surface data written successfully.";
	}
	
	@Override
	public String getFailureMessage(){
		return "Failed to fully write surface data.";
	}
	
	@Override
	public String getTitle(){
		return "Write surface data to file";
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = SurfaceDataFileWriter.class.getResource("/mgui/resources/icons/vertex_data_set_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/name_map_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(ArrayList.class);
		return objs;
	}
	
}