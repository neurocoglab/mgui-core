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

package mgui.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.io.PersistentObject;
import mgui.interfaces.logs.LoggingType;

/*****************************************
 * Base class for all file loader classes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public abstract class FileLoader implements InterfaceIO {

	protected File dataFile;
	protected URL dataURL;
	protected InterfaceIOOptions options;
	
	/**********************************************
	 * Sets the current File for this loader. Sets the URL to {@code null}.
	 * 
	 */
	public void setFile(File file) {
		dataFile = file;
		dataURL = null;
	}

	/**********************************************
	 * Sets the current URL for this loader. Sets the File to {@code null}.
	 * 
	 */
	public void setURL(URL url) {
		dataFile = null;
		dataURL = url;
	}
	
	/***********************************************
	 * Returns the current data file for this loader.
	 * 
	 * @return
	 */
	public File getFile(){
		return dataFile;
	}
	
	@Override
	public void setOptions(InterfaceIOOptions options){
		this.options = options;
	}
	
	@Override
	public InterfaceIOOptions getOptions(){
		return options;
	}
	
	/***********************************************
	 * Loads data from the set of files or URLs specified by {@code options}. Loading is controlled
	 * by the specifications of {@code options}. 
	 * 
	 * <p>This method should also set the URL reference and writer and loader attributes to match
	 * this one and its complement.
	 * 
	 * @return
	 */
	public boolean load(){
		return load(options, null);
	}
	
	/***********************************************
	 * Loads data from the set of files or URLs specified by {@code options}. Loading is controlled
	 * by the specifications of {@code options}. 
	 * 
	 * <p>This method should also set the URL reference and writer and loader attributes to match
	 * this one and its complement.
	 * 
	 * @param progress_bar
	 * @return
	 */
	public boolean load(ProgressUpdater progress_bar){
		return load(options, null);
	}
	
	/***********************************************
	 * Loads data from the set of files or URLs specified by {@code options}. Loading is controlled
	 * by the specifications of {@code options}. If loaded object is of type {@link PersistentObject},
	 * this method should also set the file loader and reference URL for the object.
	 * 
	 * <p>This method should also set the URL reference and writer and loader attributes to match
	 * this one and its complement.
	 * 
	 * @param options
	 * @param progress_bar 		An optional progress updater. Can be {@code null}.
	 * @return
	 */
	public abstract boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar);
	
	/***********************************************
	 * Loads data into an instance of the object corresponding to this loader.
	 * If loaded object is of type {@link PersistentObject},
	 * this method should also set the file loader and reference URL for the object.
	 * 
	 * TODO: make generic?
	 * 
	 * @return
	 * @throws IOException
	 */
	public Object loadObject() throws IOException{
		return loadObject(null, null);
	}
	
	/***********************************************
	 * Loads data into an instance of the object corresponding to this loader.
	 * If loaded object is of type {@link PersistentObject},
	 * this method should also set the file loader and reference URL for the object.
	 * 
	 * TODO: make generic?
	 * 
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public Object loadObject(InterfaceIOOptions options) throws IOException{
		return loadObject(null, options);
	}
	
	@Override
	public InterfaceIOType getIOType(){
		return InterfaceEnvironment.getIOTypeForInstance(this);
	}
	
	/***********************************************
	 * Loads data into an instance of the object corresponding to this loader.
	 * If loaded object is of type {@link PersistentObject},
	 * this method should also set the file loader and reference URL for the object.
	 * 
	 * TODO: make generic?
	 * 
	 * @param progress_bar 		An optional progress updater. Can be {@code null}.
	 * @param options 			Options defining the loading process
	 * @return
	 * @throws IOException
	 */
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		InterfaceSession.log(this.getClass().getCanonicalName() + ": Method 'loadObject(InterfaceIOOptions options, ProgressUpdater progress_bar)'" +
																  " is not implemented!",  
																  LoggingType.Warnings);
		return null;
	}
	
	/***********************************************
	 * Loads data into an instance of the object corresponding to this loader.
	 * If loaded object is of type {@link PersistentObject},
	 * this method should also set the file loader and reference URL for the object.
	 * 
	 * TODO: make generic?
	 * 
	 * @param progress_bar 		An optional progress updater. Can be {@code null}.
	 * @return
	 * @throws IOException
	 */
	public Object loadObject(ProgressUpdater progress_bar) throws IOException{
		return loadObject(progress_bar, null);
	}
	
	/************************************************
	 * Returns a message for the progress updater, while loading is in progress.
	 * 
	 * @return Progress message
	 */
	public String getProgressMessage(){
		if (dataFile == null) return "";
		return "Loading '" + dataFile.getName() + "': ";
	}
	
	/************************************************
	 * Returns a message to indicate that loading was successful.
	 * 
	 * @return Progress message
	 */
	public String getSuccessMessage(){
		return "Data loaded successfully.";
	}
	
	/************************************************
	 * Returns a message to indicate that loading failed.
	 * 
	 * @return Progress message
	 */
	public String getFailureMessage(){
		return "Failed to fully load data.";
	}
	
	public String getTitle(){
		return "Load data file";
	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}
	
	/**************************************************
	 * Returns the {@code InterfaceIOType} of a {@code FileWriter} which is the complement of this
	 * loader; i.e., writes what this loader reads with identical encoding. Transfer between loader and
	 * writer should be lossless. 
	 * 
	 * <p>Returns {@code null} if no complement is defined.
	 * 
	 * @return
	 */
	public InterfaceIOType getWriterComplement(){
		return null;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = FileLoader.class.getResource("/mgui/resources/icons/file_loader_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/file_loader_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		//objs.add(InterfaceObject.class);
		return objs;
	}
	
	@Override
	public InterfaceIOType getComplementIOType(){
		return getWriterComplement();
	}
	
}