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

package mgui.io.domestic.maps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.NameMap;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;


/***************************
 * Writes a name map to an delimited ASCII file.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class NameMapWriter extends FileWriter {

	public NameMapWriter(){
		
	}
	
	public NameMapWriter(File dir){
		setFile(dir);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		if (options == null || !(options instanceof NameMapOutOptions)) return false;
		NameMapOutOptions opts = (NameMapOutOptions)options;
		if (opts.names == null || opts.maps == null) return false;
		
		File dir = opts.getDir();
		if (dir == null || !dir.exists() || !dir.isDirectory()) return false;
		
		boolean success = true;
		
		for (int i = 0; i < opts.names.length; i++){
			try{
				File file = new File(dir.getAbsolutePath() + File.separator + opts.names[i]);
				file.createNewFile();
				dataFile = file;
				success &= writeNameMap(opts.maps[i], opts.delim);
			}catch (IOException e){
				success = false;
				}
			}
		
		return success;
	}
	
	public boolean writeNameMap(NameMap map, String delim){
		if (map == null || dataFile == null) return false;
		
		try{
			
			if (!dataFile.exists() && !dataFile.createNewFile()){
				InterfaceSession.log("Couldn't create output file '" + dataFile.getAbsolutePath() + "'");
				return false;
				}
			
			if (delim.equals("\\t")) delim = "\t";
			
			BufferedWriter out = new BufferedWriter(new java.io.FileWriter(dataFile));
			//Iterator<Integer> itr = map.getIndices().iterator();
			ArrayList<Integer> indices = map.getIndices();
			Collections.sort(indices);
			
			for (int i = 0; i < indices.size(); i++){
				out.write(indices.get(i) + delim + map.get(indices.get(i)) + "\n");
				}
			
			out.close();
			
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
		
		return true;
	}
	
	@Override
	public String getSuccessMessage(){
		return "Name map(s) written successfully.";
	}
	
	@Override
	public String getFailureMessage(){
		return "Failed to fully write name map(s).";
	}
	
	@Override
	public String getTitle(){
		return "Write name map to file";
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = NameMapWriter.class.getResource("/mgui/resources/icons/name_map_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/name_map_20.png");
		return null;
	}
	

}