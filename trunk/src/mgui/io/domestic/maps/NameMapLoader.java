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

package mgui.io.domestic.maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.NameMap;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/*******************************
 * Reads a name map from file. One of three format options are possible:
 * 
 * <ol>
 * <li>Ascii - An Ascii file having two delimited columns, ordered as
 * index (integer), then name (String)
 * <li>XML - An XML file comprised of entries
 * <li>Auto [default] - Searches for the leading xml tag; if found, reads as XML, otherwise as Ascii 
 * </ol>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class NameMapLoader extends FileLoader {

	protected Format format = Format.Auto;
	
	public enum Format{
		Auto,
		Ascii,
		XML;
	}
	
	public NameMapLoader(){
		
	}
	
	public NameMapLoader(File file){
		setFile(file);
	}
	
	public NameMapLoader(File file, Format format){
		setFile(file);
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		NameMapInOptions opts = (NameMapInOptions)options;
		//NameMap map = new NameMap(opts.names[0]);
		format = opts.format;
		NameMap map = loadNameMap();
		return map;
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		if (options == null || !(options instanceof NameMapInOptions)) return false;
		NameMapInOptions opts = (NameMapInOptions)options;
		format = opts.format;
		
		if (opts.files == null || opts.files.length == 0) return false;
		String name;
		boolean success = true;
		
		for (int i = 0; i < opts.files.length; i++){
			setFile(opts.files[i]);
			if (opts.names != null)
				name = opts.names[i];
			else{
				name = opts.files[i].getName();
				if (name.lastIndexOf(".") > 0) name = name.substring(0, name.lastIndexOf("."));
				}
			//NameMap map = new NameMap(name);
			NameMap map = loadNameMap();
			if (map == null){
				success = false;
			}else{
				map.setName(name);
				InterfaceEnvironment.addNameMap(map);
				}
			}
		
		return success;
	}
	
	/********************************************
	 * Load data into {@code name_map}, with the current data format and data file
	 * 
	 * @param name_map
	 * @return
	 */
	public NameMap loadNameMap(){
		
		Format _format = Format.Ascii;
		
		try{
			if (format == Format.Auto){
				BufferedReader in = new BufferedReader(new FileReader(dataFile));
				String line = in.readLine();
				if(line.startsWith("<?xml"))
					_format = Format.XML;
				in.close();
				}
			
			switch(_format){
				case Ascii:
					return loadNameMapAscii(new String[]{" ",",","\t"});
				case XML:
				default:
					return loadNameMapXML();
				}
		}catch (IOException ex){
			InterfaceSession.log("NameMapLoader.loadMap: Could not determine file format.");
			return null;
			}
		}
	
	protected NameMap loadNameMapAscii(String[] delim){
		
		try{
			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			String line = in.readLine();
			
			int index = 0;
			NameMap map = new NameMap();
			
			while (line != null){
				int sep = getSep(line, delim);
				if (sep > 0 && sep < line.length()){
					index = Integer.valueOf(line.substring(0, sep));
					map.add(index, line.substring(sep + 1));
					}
				line = in.readLine();
				}
			
			in.close();
			String name = dataFile.getName();
			if (name.contains(".")) name = name.substring(0,name.lastIndexOf("."));
			map.setName(name);
			return map;
			
		}catch (IOException e){
			//e.printStackTrace();
			InterfaceSession.log("NameMapLoader: Exception reading file '" + dataFile.getAbsolutePath() + "'", 
								 LoggingType.Errors);
			return null;
			}
		
	}

	/******************************
	 * Loads a name map from XML format.
	 * 
	 * @return
	 */
	protected NameMap loadNameMapXML(){
		
		try{
			NameMapXMLHandler handler = new NameMapXMLHandler();
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
		
			return handler.getMap();
			
		}catch (Exception ex){
			InterfaceSession.log("Error loading name map '" + dataFile.getAbsolutePath() + "': " + ex.getMessage(),
								 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			}
		
		return null;
	}

	
	int getSep(String str, String[] delim){
		int idx = Integer.MAX_VALUE;
		for (int i = 0; i < delim.length; i++){
			String d = delim[i];
			if (d.equals("\\t")) d = "\t";
			int a = str.indexOf(d);
			if (a > 0 && a < idx) idx = a;
			}
		return idx;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = NameMapLoader.class.getResource("/mgui/resources/icons/name_map_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/name_map_20.png");
		return null;
	}

}