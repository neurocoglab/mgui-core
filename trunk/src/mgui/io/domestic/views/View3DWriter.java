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

package mgui.io.domestic.views;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.View3D;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;

/*************************************************
 * Writes a set of {@link View3D} objects to file. The default format is XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class View3DWriter extends FileWriter {

	public View3DWriter(){
		
	}
	
	public View3DWriter(File file){
		setFile(file);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		View3DOutputOptions _options = (View3DOutputOptions)options;
		setFile(_options.getFiles()[0]);
		
		ArrayList<View3D> views = _options.views;
		
		try{
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		
			writer.write(InterfaceEnvironment.getXMLHeader());
			writer.write("\n<View3Ds>");
			
			for (int i = 0; i < views.size(); i++){
				views.get(i).writeXML(1, writer, progress_bar);
				}
			
			writer.write("\n</View3Ds>");
			writer.close();
			
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
		
		return true;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = View3DWriter.class.getResource("/mgui/resources/icons/view_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/view_3d_20.png");
		return null;
	}

}