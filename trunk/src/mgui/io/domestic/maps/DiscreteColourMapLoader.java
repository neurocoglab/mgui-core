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
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileWriter;
import mgui.util.Colour3f;
import mgui.util.Colour4f;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/****************************************************************
 * Loads a discrete colour map from either delimited ASCII or XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DiscreteColourMapLoader extends ColourMapLoader {

	protected ColourMap map;
	
	public DiscreteColourMapLoader(){
		
	}
	
	public DiscreteColourMapLoader(File file){
		this.setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		DiscreteColourMapInOptions opts = (DiscreteColourMapInOptions)options;
		File[] files = opts.getFiles();
		if (opts.names == null) opts.setNamesFromFiles();
		
		boolean success = true;
		try{
			
			for (int i = 0; i < files.length; i++){
				dataFile = files[i];
				if (opts.as_discrete)
					map = loadMap(opts.format, opts.normalized);
				else
					map = loadAsContinuousMap(opts.format, opts.no_anchors, opts.normalized);
				map.setName(opts.names[i]);
				InterfaceEnvironment.addColourMap(map);
				}
		
		}catch (IOException e){
			//e.printStackTrace();
			InterfaceSession.log("Error loading colour map.\nDetails: " + e.getLocalizedMessage(), 
								 LoggingType.Errors);
			success = false;
			}
		
		return success;
	}

	@Override
	public DiscreteColourMap loadMap() throws IOException{
		return loadMap(DiscreteColourMapInOptions.Format.XML, true);
	}
	
	/****************************************
	 * Loads a discrete colour map from the specified format.
	 * 
	 * @param format
	 * @return the map
	 * @throws IOException
	 */
	public DiscreteColourMap loadMap(DiscreteColourMapInOptions.Format format, boolean normalized) throws IOException{
		
		switch (format){
		
			case XML:
				try{
					XMLReader reader = XMLReaderFactory.createXMLReader();
					ColourMapXMLHandler handler = new ColourMapXMLHandler();
					reader.setContentHandler(handler);
					reader.setErrorHandler(handler);
					reader.parse(new InputSource(new FileReader(dataFile)));
					return (DiscreteColourMap)handler.getMap();
					
				}catch (SAXParseException e){
					InterfaceSession.log("Exception at line " + e.getLineNumber() + " col " + e.getColumnNumber() + ":");
					InterfaceSession.handleException(e);
					//e.printStackTrace();	
					}
				catch (Exception e){
					InterfaceSession.handleException(e);
					//e.printStackTrace();	
					}
				return null;
			
			case Ascii:
				String name = dataFile.getName();
				if (name.contains("."))
					name = name.substring(0, name.indexOf("."));
				DiscreteColourMap map = new DiscreteColourMap(name);
				
				BufferedReader reader = new BufferedReader(new FileReader(dataFile));
				String line = reader.readLine();
				int i = 1;
				
				while (line != null){
					StringTokenizer tokens = new StringTokenizer(line," ,\t\n\r\f");
					int count = tokens.countTokens();
					if (count < 4) throw new IOException("DiscreteColourMapLoader: Bad input at line " + i + ": " + line);
					int index = Integer.valueOf(tokens.nextToken());
					float r = Float.valueOf(tokens.nextToken());
					if (!normalized)
						r /= 256f;
					float g = Float.valueOf(tokens.nextToken());
					if (!normalized)
						g /= 256f;
					float b = Float.valueOf(tokens.nextToken());
					if (!normalized)
						b /= 256f;
					if (tokens.countTokens() == 5){
						float a = Float.valueOf(tokens.nextToken());
						if (!normalized)
							a /= 256f;
						map.setColour(index, new Colour4f(r, g, b, a));
					}else{
						map.setColour(index, new Colour3f(r, g, b));
						}
					line = reader.readLine();
					}
				
				reader.close();
				return map;
			
		}
		
		return null;
	}
	
	/********************************************
	 * Loads this discrete map as a continuous maps with <code>no_anchors</code> anchors. This is useful for
	 * converting discrete look-up tables (LUTs) which are meant to be continuous.
	 * 
	 * @param no_anchors
	 * @return the continuous version of this discrete map
	 * @throws IOException
	 */
	public ContinuousColourMap loadAsContinuousMap(DiscreteColourMapInOptions.Format format,
												   int no_anchors, 
												   boolean normalized) throws IOException{
		
		//load map, then select anchors
		DiscreteColourMap d_map = loadMap(format, normalized);
		return d_map.getAsContinuousMap(no_anchors);
		
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = DiscreteColourMapLoader.class.getResource("/mgui/resources/icons/discrete_cmap_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/discrete_cmap_20.png");
		return null;
	}
	
}