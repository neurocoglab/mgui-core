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

package mgui.interfaces.io;

import java.io.IOException;
import java.net.URL;

import mgui.io.FileLoader;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;

/******************************************************
 * Interface for objects which can be made persistent (loaded from and written to persistent memory). 
 * Functions specify file references and file loaders/writers.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface PersistentObject {

	/*******************************************
	 * Returns the URL reference for this persistent object; i.e., the location where its data were loaded and where it should
	 * be written.
	 * 
	 * @return
	 */
	public URL getUrlReference();
	
	/*******************************************
	 * Sets the URL reference for this persistent object; i.e., the location where its data were loaded and where it should
	 * be written.
	 * 
	 * @param url
	 */
	public void setUrlReference(URL url);
	
	/******************************************
	 * Returns an instance of the file loader associated with this persistent object.
	 * 
	 * @return
	 */
	public FileLoader getFileLoader() throws IOException;
	
	/******************************************
	 * Returns the options used to last load this object, if available
	 * 
	 * @return The options, or {@code null} if none exist
	 */
	public InterfaceIOOptions getLoaderOptions();
	
	/******************************************
	 * Sets the {@link InterfaceIOType} associated with this persistent object's loader.
	 * 
	 * @return
	 */
	public boolean setFileLoader(InterfaceIOType io_type);
	
	/******************************************
	 * Sets the options used to last load this object; can be {@code null}.
	 * 
	 */
	public void setLoaderOptions(InterfaceIOOptions options);
	
	/******************************************
	 * Returns an instance of the file writer associated with this persistent object.
	 * 
	 * @return
	 */
	public FileWriter getFileWriter() throws IOException;
	
	/******************************************
	 * Returns the options used to last write this object, if available
	 * 
	 * @return The options, or {@code null} if none exist
	 */
	public InterfaceIOOptions getWriterOptions();
	
	/******************************************
	 * Sets the {@link InterfaceIOType} associated with this persistent object's writer.
	 * 
	 * @return
	 */
	public boolean setFileWriter(InterfaceIOType io_type);
	
	/******************************************
	 * Sets the options used to last write this object; can be {@code null}.
	 * 
	 */
	public void setWriterOptions(InterfaceIOOptions options);
	
}