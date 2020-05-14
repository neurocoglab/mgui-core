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

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.graphs.Graph3DInt;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.maps.NameMapLoader;
import mgui.numbers.MguiNumber;

/*********************************************************************
 * Loads vertex-wise data into an {@linkplain InterfaceShape} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class SurfaceDataFileLoader extends FileLoader {

	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		return false;
	}
	
	public ArrayList<MguiNumber> loadValues(){
		return loadValues(null);
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadValues(progress_bar);
	}
	
	public abstract ArrayList<MguiNumber> loadValues(ProgressUpdater progress_bar);

	@Override
	public String getSuccessMessage(){
		return "Surface data loaded successfully.";
	}
	
	@Override
	public String getFailureMessage(){
		return "Failed to fully load surface data.";
	}
	
	@Override
	public String getTitle(){
		return "Load surface data file";
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = SurfaceDataFileLoader.class.getResource("/mgui/resources/icons/vertex_data_set_20.png");
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