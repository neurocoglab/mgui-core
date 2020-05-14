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

package mgui.io;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import mgui.interfaces.io.InterfaceIOType;
import mgui.resources.icons.IconObject;

/*********************************
 * Interface for all classes performing I/O operations. The emphasis of this class is:
 * 
 * <ol>
 * <li>Provide a standard interface for input and output operations
 * <li>Allow I/O interfaces to be loaded at runtime in the the {@link InterfaceEnvironment}
 * <li>Provide complementarity for input and output operations; interfaces should specify their
 * 	   complement (writers have a complementary loader, and vice versa), and should also be
 * 	   specifiable by this complement 
 * </ol>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface InterfaceIO extends IconObject{

	public String getProgressMessage();
	public String getSuccessMessage();
	public String getTitle();
	public void setFile(File file);
	public void setURL(URL url);
	
	/***********************************************
	 * Sets the {@code InterfaceIOOptions} specifying this I/O operation
	 * 
	 * @param options
	 */
	public void setOptions(InterfaceIOOptions options);
	
	/***********************************************
	 * Gets the {@code InterfaceIOOptions} specifying this I/O operation, if options
	 * have been set; otherwise returns {@code null}.
	 * 
	 * @param options
	 */
	public InterfaceIOOptions getOptions();
	

	/***********************************************
	 * Returns the registered IO type for this object, if one exists
	 * 
	 * @return the registered IO type, or {@code null} if none exists
	 */
	public InterfaceIOType getIOType();
	
	/********************************************
	 * Returns a list of classes for objects which are supported by this I/O interface
	 * 
	 * @return
	 */
	public ArrayList<Class<?>> getSupportedObjects();
	
	/********************************************
	 * Returns an {@code InterfaceIOType} specifying the {@code InterfaceIO} which performs the complementary
	 * operation to this one. I.e., loaders should specify their complementary writers.
	 * 
	 * @return The complementary class, or {@code null} if none is defined
	 */
	public InterfaceIOType getComplementIOType();
	
}