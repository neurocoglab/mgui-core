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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import foxtrot.Job;
import foxtrot.Worker;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.io.InterfaceIOOptions;
import mgui.io.standard.xml.XMLWriter;
import mgui.io.util.IoFunctions;

/************************************************
 * Writes a {@link ShapeModel3D} object to XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModel3DWriter extends XMLWriter {

	public ShapeModel3DWriter(){
		
	}
	
	public ShapeModel3DWriter(File file){
		this.setFile(file);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options,
						 ProgressUpdater progress_bar) {
		
		if (!(options instanceof ShapeModel3DOutputOptions))
			return false;
		
		ShapeModel3DOutputOptions _options = (ShapeModel3DOutputOptions)options;
		boolean success = true;
		
		try{
			setFile(_options.file);
			if (_options.getModel() != null){
				success &= writeModel(_options, progress_bar);
			}else{
				InterfaceSession.log("ShapeModel3DWriter: No model set!", LoggingType.Errors);
				failure_message = "No model set to write...";
				return false;
				}
		}catch (ShapeIOException e){
			InterfaceSession.handleException(e);
			failure_message = e.getMessage();
			return false;
			}
		
		return success;
	}
	
	public boolean writeModel(final ShapeModel3DOutputOptions options, final ProgressUpdater progress_bar) throws ShapeIOException {
		
		if (dataFile == null) 
			throw new ShapeIOException("ShapeModel3DWriter: no output file set!");
		
		if (dataFile.exists() && !dataFile.delete())
			throw new ShapeIOException("ShapeModel3DWriter: cannot delete existing file '" + dataFile.getAbsolutePath() + "'");
		
		boolean success = false;
		
		try{
			File write_file = dataFile;
			File archive_file = null;
			if (options.gzip_xml){
				String unzip = write_file.getAbsolutePath(); 
				if (unzip.endsWith(".gz")){
					unzip = unzip.substring(0, unzip.lastIndexOf(".gz"));
				} else if (unzip.endsWith(".smodz")){
					archive_file = new File(write_file.getAbsolutePath());
					unzip = unzip.substring(0, unzip.length()-1);
				} else {
					archive_file = new File(write_file.getAbsolutePath() + "z");
					}
				
				write_file = new File(unzip);
				}
			
			if (!write_file.getName().contains(".")) {
				write_file = new File(write_file.getAbsolutePath() + ".smod");
				if (options.gzip_xml) {
					archive_file = new File(write_file.getAbsolutePath() + "z");
					}
				}
			
			if (archive_file != null) {
				if (archive_file.exists() && !archive_file.delete())
					throw new ShapeIOException("ShapeModel3DWriter: cannot delete existing file '" + archive_file.getAbsolutePath() + "'");
				}
			
			if (write_file.exists() && !write_file.delete())
				throw new ShapeIOException("ShapeModel3DWriter: cannot delete existing file '" + write_file.getAbsolutePath() + "'");
			
			if (!write_file.createNewFile())
				throw new ShapeIOException("ShapeModel3DWriter: cannot create output file '" + write_file.getAbsolutePath() + "'");
			
			File root_dir = write_file.getParentFile();
			File ref_dir = null;
			String shapes_dir = "resources";
			File orig_file = dataFile;
			dataFile = write_file;
			
			if (options.containsByReferenceShapes() && 
					options.shapes_folder != null && 
					options.shapes_folder.length() > 0){
				
				shapes_dir = options.shapes_folder;
				
				ref_dir = IoFunctions.fullFile(root_dir, options.shapes_folder);
				if (ref_dir.exists() && !ref_dir.isDirectory()){
					throw new ShapeIOException("ShapeModel3DWriter: " + ref_dir.getAbsolutePath() + 
											   File.separator + options.shapes_folder + " is not a valid directory...");
					}
				if (!ref_dir.exists() && !IoFunctions.createDirs(ref_dir)){
					throw new ShapeIOException("ShapeModel3DWriter: " + ref_dir.getAbsolutePath() + 
											   File.separator + options.shapes_folder + " could not be created...");
					}
				
				}
			
			if (progress_bar == null){
				writeXMLObject(options, null);
				success = true;
				
			}else{
			
				progress_bar.setMessage("Writing '" + dataFile.getName() + "':");
				progress_bar.setIndeterminate(true);
				success = (Boolean)Worker.post(new Job(){
					@Override
					public Boolean run(){
						try{
							writeXMLObject(options, progress_bar);
							return true;
						}catch (Exception e){
							InterfaceSession.handleException(e);
							return false;
							}
						}
				});
			}
			
			// Reset original dataFile
			dataFile = orig_file;
			
			// Compress?
			if (success && archive_file != null){
				
				ArrayList<File> zip_files = new ArrayList<File>();
				zip_files.add(write_file);
				
				if (options.containsByReferenceShapes()) {
					// Get list of files to add to archive
					
					List<String> urls = options.getModel().getModelSet().getByReferenceUrls();
					
					for ( String url : urls) {
						url = url.replace("{root}", root_dir.getAbsolutePath());
						zip_files.add(new File(url));
						}
					
					}
				
				IoFunctions.gzipAndTarFiles(zip_files, archive_file, root_dir.getAbsolutePath());
				
				// Remove zipped files
				for (File file : zip_files) {
					Files.delete(Paths.get(file.toURI()));
					}
				
				}
		
		}catch (IOException e){
			//e.printStackTrace();
			InterfaceSession.handleException(e);
			throw new ShapeIOException("ShapeModel3DWriter: IOException encountered..\nDetails: " + e.getMessage());
			}
		
		return success;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ShapeModel3DWriter.class.getResource("/mgui/resources/icons/shape_model_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/shape_model_20.png");
		return null;
	}

}