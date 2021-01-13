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
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import foxtrot.Job;
import foxtrot.Worker;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.xml.ShapeModel3DXMLHandler;
import mgui.io.util.IoFunctions;

/*********************************************************
 * Loader for a {@linkplain ShapeModel3D}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModel3DLoader extends FileLoader {
	
	ShapeModel3DInputOptions options;
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		if (!(options instanceof ShapeModel3DInputOptions)){
			InterfaceSession.log("ShapeModel3DLoader: wrong options type '" + options.getClass().getCanonicalName() + "'..");
			return false;
			}
		
		if (progress_bar != null){
			progress_bar.setIndeterminate(true);
			}
		
		ShapeModel3DInputOptions _options = (ShapeModel3DInputOptions)options;
		boolean success = true;
		
		for (int i = 0; i < _options.files.length; i++){
			setFile(_options.files[i]);
			if (progress_bar != null){
				progress_bar.setMessage("Loading '" + dataFile.getName() + "':");
				}
			ShapeModel3D model = loadModel(_options, progress_bar);
			if (model == null)
				success = false;
			else{
				if (_options.merge_with_model != null ){
					ShapeModel3D merge_model = _options.merge_with_model; //.getModelSet().addShape(model.getModelSet());
					ShapeSet3DInt model_set = merge_model.getModelSet();
					// If they have the same name or options.merge_model_set, merge
					if (_options.merge_model_set || model_set.getName().equals(model.getModelSet().getName())){
						model_set.mergeWithSet(model.getModelSet(), _options.existing_shapes, true);
					// Otherwise simply add
					}else{
						model_set.addShape(model.getModelSet());
						}
					
				}else{
					InterfaceSession.getWorkspace().addShapeModel(model, true);
					}
				}
			}
			
		return success;
	}

	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadModel((ShapeModel3DInputOptions)options, progress_bar);
	}
	
	public ShapeModel3D loadModel(final ShapeModel3DInputOptions options,
			  					  final ProgressUpdater progress_bar) {
		
		if (progress_bar == null)
			return loadModelBlocking(options, null);
		
		return (ShapeModel3D)Worker.post(new Job(){
			
			public ShapeModel3D run(){
				return loadModelBlocking(options, progress_bar);
			}
			
		});
		
	}
	
	protected ShapeModel3D loadModelBlocking(ShapeModel3DInputOptions options,
								  			 ProgressUpdater progress_bar) {

		if (options == null){
			InterfaceSession.log("ShapeModel3DLoader: Options not set..");
			return null;
			}
		
		if (dataFile == null){
			InterfaceSession.log("ShapeModel3DLoader: Input file not set..");
			return null;
			}
		
		if (!dataFile.exists()){
			InterfaceSession.log("ShapeModel3DLoader: Input file '" + dataFile.getAbsolutePath() + "' not found.");
			return null;
			}
		
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			ShapeModel3DXMLHandler handler = new ShapeModel3DXMLHandler();
			
			File write_file = dataFile;
			
			File temp_dir = null;
			
			// Is it compressed? If so, decompress to a temporary file
			if (dataFile.getAbsolutePath().endsWith(".gz")){
				String temp = dataFile.getAbsolutePath();
				temp = temp + ".~";
				write_file = IoFunctions.unzipFile(dataFile, new File(temp));
			} else if (dataFile.getAbsolutePath().endsWith(".smodz")){
				String tdir = IoFunctions.getTempDir().getAbsolutePath() + File.separator + "mgui" +
																		InterfaceEnvironment.getNow("yyyyMMdd");
				
				temp_dir = new File(tdir);
				if (temp_dir.exists())
					FileUtils.deleteDirectory(temp_dir);
				Files.createDirectory(Paths.get(tdir));
				
				IoFunctions.gunzipAndTarFiles(dataFile, temp_dir);
				String fn = dataFile.getName();
				fn = fn.replace(".smodz", ".smod");
				
				write_file = new File(temp_dir.getAbsolutePath() + File.separator + fn);

				}
			
			handler.setRootDir(write_file.getParent());
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(write_file)));
			
			// Clean up if necessary
			if (dataFile.getAbsolutePath().endsWith(".gz")){
				write_file.delete();
			} else if (dataFile.getAbsolutePath().endsWith(".smodz")){
				FileUtils.deleteDirectory(temp_dir);
				}
			
			return handler.getShapeModel();
			
		}catch (Exception e){
			InterfaceSession.handleException(e);
			return null;
			}
	
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ShapeModel3DLoader.class.getResource("/mgui/resources/icons/shape_model_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/shape_model_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(ShapeModel3D.class);
		return objs;
	}
}