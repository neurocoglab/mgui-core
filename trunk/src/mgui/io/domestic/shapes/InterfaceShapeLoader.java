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

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;

/*************************************************************
 * Abstract loader class for all loaders which load {@linkplain InterfaceShape} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfaceShapeLoader extends FileLoader {

	
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException {
		return loadShape((ShapeInputOptions)options, progress_bar);
	}

	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		ShapeInputOptions _options = (ShapeInputOptions)options;
		
		if (_options.shape_set == null){
			InterfaceSession.log("InterfaceShapeLoader: No target shape set specified!", LoggingType.Errors);
			return false;
			}
		
		File[] input_files = _options.getFiles();
		boolean success = true;
		for (int i = 0; i < input_files.length; i++){
			setFile(input_files[i]);
			try{
				InterfaceShape shape = loadShape(_options, progress_bar);
				if (shape == null){
					InterfaceSession.log("InterfaceShapeLoader: No target shape set specified!", LoggingType.Errors);
				}else{
					if (!_options.shape_set.addShape(shape)){
						InterfaceSession.log("InterfaceShapeLoader: Problem loading '" + 
											input_files[i].getAbsolutePath() + "'. See log for details.", 
											LoggingType.Errors);
						success = false;
					}else{
						InterfaceSession.log("Loaded '" + input_files[i].getName() + "'.", LoggingType.Verbose);
						
						}
					}
			}catch (IOException ex){
				InterfaceSession.log("InterfaceShapeLoader: Problem loading '" + 
											input_files[i].getAbsolutePath() + "'. See log for details.", 
											LoggingType.Errors);
				InterfaceSession.handleException(ex);
				success = false;
				}
			}
		
		return success;
	}
	
	
	/************************************************
	 * Loads a shape with the given {@code options}.
	 * 
	 * @param options
	 * @param progress_bar
	 * @return
	 */
	public abstract InterfaceShape loadShape(ShapeInputOptions options, ProgressUpdater progress_bar) throws IOException;

}