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

package mgui.io.domestic.shapes.xml;

import java.io.File;
import java.io.IOException;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.Utility;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.io.domestic.shapes.InterfaceShapeLoader;
import mgui.io.domestic.shapes.ShapeInputOptions;

/*********************************************************
 * Utility class for methods related specifically to XML representations of {@code Shape} or
 * {@code InterfaceShape} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeXMLFunctions extends Utility {

	
	/********************************************************
	 * Attempts to load a shape with the given {@code loader} and {@code url}. The loader
	 * must be a fully qualified reference to an class which is an instance of  
	 * 
	 * @param loader
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * 
	 */
	public static InterfaceShape loadShape(String loader, String url, ShapeInputOptions options, 
										   ProgressUpdater progress_bar) throws IOException{
		
		
		InterfaceShapeLoader shape_loader = getLoaderInstance(loader);
		
		shape_loader.setFile(new File(url));
		return shape_loader.loadShape(options, progress_bar);
		
	}
	
	/**********************************************************
	 * Attempts to return an instance of the loader from the qualified reference
	 * {@code loader}. Returns {@code null} if the attempt fails.
	 * 
	 * @param loader
	 * @return
	 */
	public static InterfaceShapeLoader getLoaderInstance(String loader){
		
		Object obj = null;
		try{
			Class<?> _class = Class.forName(loader);
			obj = _class.newInstance();
		}catch (Exception ex){
			InterfaceSession.log("ShapeXMLFunctions.loadShape: Loader must be of type " +
					InterfaceShapeLoader.class.getCanonicalName(), 
					LoggingType.Errors);
			return null;
			}
		
		if (!InterfaceShapeLoader.class.isInstance(obj)){
			InterfaceSession.log("ShapeXMLFunctions.loadShape: Loader must be of type " +
								InterfaceShapeLoader.class.getCanonicalName(), 
								LoggingType.Errors);
			return null;
			}
			
		InterfaceShapeLoader shape_loader = (InterfaceShapeLoader)obj;
		return shape_loader;
	}
	
	
}