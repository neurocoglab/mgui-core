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

package mgui.io.domestic.graphs;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.graphs.Graph3DInt;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;


/*********************************************************
 * Abstract loader class for loading graphs.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 ****/


public abstract class GraphFileLoader extends FileLoader {

	@Override
	public String getSuccessMessage(){
		return "Graph(s) loaded successfully.";
	}
	
	@Override
	public String getFailureMessage(){
		return "Failed to fully load graph(s).";
	}
	
	@Override
	public String getTitle(){
		return "Load surface file";
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		 return loadGraphs();
	 }
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {

		if (!(options instanceof GraphInputOptions)){
			InterfaceSession.log("GraphFileLoader: options must be instance of GraphInputOptions..");
			return false;
			}
		
		GraphInputOptions _options = (GraphInputOptions)options;
		boolean success = true;
		
		for (int i = 0; i < _options.files.length; i++){
			setFile(_options.files[i]);
			switch (_options.type){
				case InterfaceGraph:
					InterfaceAbstractGraph graph = loadGraph(_options.unique_labels, _options.get_locations);
					if (graph == null)
						success = false;
					else{
						graph.setName(_options.names[i]);
						InterfaceSession.getWorkspace().addGraph(graph);
						}
					break;
					
				case Graph3DInt:
					Graph3DInt graph3d = loadGraph3DInt(_options.unique_labels);
					if (graph3d == null)
						success = false;
					else
						_options.shape_set.addShape(graph3d);
					break;
				}
			}
		
		return success;
	}
	
	public abstract InterfaceAbstractGraph loadGraph(boolean unique_labels, boolean get_locations);
	
	public Graph3DInt loadGraph3DInt(boolean unique_labels){
		InterfaceSession.log("Loader for Graph3DInt is not implement for class " + this.getClass().getCanonicalName());
		return null;
	}
	
	public Graph3DInt loadGraph2DInt(boolean unique_labels){
		InterfaceSession.log("Loader for Graph2DInt is not implement for class " + this.getClass().getCanonicalName());
		return null;
	}

	public ArrayList<InterfaceAbstractGraph> loadGraphs(){
		return null;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = GraphFileLoader.class.getResource("/mgui/resources/icons/graph_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/volume_3d_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(Graph3DInt.class);
		return objs;
	}
	
}