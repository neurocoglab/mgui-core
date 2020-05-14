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

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.xml.XMLObject;
import mgui.io.FileWriter;

/************************************************************
 * General writer for an {@code InterfaceShape} object. The default implementation writes
 * shapes using their domestic XML representations (these must be implemented). See also
 * {@linkplain XMLObject}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfaceShapeWriter extends FileWriter {

	

	/*******************************************************
	 * Writes this shape to file using its domestic XML representation.
	 * 
	 * @param graph
	 * @return
	 */
	public abstract boolean writeShape(InterfaceShape shape, ProgressUpdater progress_bar) throws IOException;

	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(InterfaceShape.class);
		return objs;
	}
	
}