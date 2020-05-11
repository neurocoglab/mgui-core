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
import java.net.URL;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.shapes.ShapeSet3DInt;

/*****************************************
 * Base class for all file writer classes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public abstract class FileWriter implements InterfaceIO {

	protected File dataFile;
	protected URL dataURL;
	protected InterfaceIOOptions options;
	protected String failure_message = "Failed to fully write data.";
	protected String success_message = "Data written successfully.";
	
	public void setFile(File file) {
		dataFile = file;
		dataURL = null;
	}

	public void setURL(URL url) {
		dataFile = null;
		dataURL = url;
	}
	
	public File getFile() {
		return dataFile;
	}
	
	public URL getURL() {
		return dataURL;
	}
	
	@Override
	public void setOptions(InterfaceIOOptions options){
		this.options = options;
	}
	
	@Override
	public InterfaceIOOptions getOptions(){
		return options;
	}
	
	/*************************************************
	 * Write the object to file subject to the given set of options.
	 * 
	 * @return
	 */
	public boolean write(){
		return write(options, null);
	}
	
	public boolean write(ProgressUpdater progress_bar){
		return write(options, progress_bar);
	}
	
	@Override
	public InterfaceIOType getIOType(){
		return InterfaceEnvironment.getIOTypeForInstance(this);
	}
	
	/***************************************************************
	 * Writes this object according to the given {@code options}.
	 * If loaded object is of type {@link PersistentObject},
	 * this method should also set the file writer and reference URL for the object.
	 * 
	 * @param options
	 * @param progress_bar
	 * @return
	 */
	public abstract boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar);
	
	public String getProgressMessage(){
		if (dataFile == null) return "";
		return "Writing '" + dataFile.getName() + "'";
	}
	
	public String getSuccessMessage(){
		return success_message;
	}
	
	public String getFailureMessage(){
		return failure_message;
	}
	
	public String getTitle(){
		return "Write data to file";
	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}
	
	/**************************************************
	 * Returns the {@code InterfaceIOType} of a {@code FileLoader} which is the complement of this
	 * writer; i.e., reads what this writer writes with identical encoding. Transfer between loader and
	 * writer should be lossless. 
	 * 
	 * <p>Returns {@code null} if no complement is defined.
	 * 
	 * @return
	 */
	public InterfaceIOType getLoaderComplement(){
		return null;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = FileWriter.class.getResource("/mgui/resources/icons/disk_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/disk_20.png");
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
		return getLoaderComplement();
	}
	
}