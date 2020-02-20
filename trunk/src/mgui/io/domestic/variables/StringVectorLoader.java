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

package mgui.io.domestic.variables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.variables.StringVectorInt;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;

/****************************************************
 * Loader class to input an array of {@code String} objects into an {@link StringVectorInt} instance.
 * Reads list from a delimited text file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class StringVectorLoader extends FileLoader {

	protected StringVectorInOptions options;
	
	public StringVectorLoader(){
		
	}
	
	public StringVectorLoader(File file){
		setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions _options, ProgressUpdater progress_bar) {
		if (_options == null || !(_options instanceof StringVectorInOptions)) return false;
		
		options = (StringVectorInOptions)_options;
		
		if (options.files == null || options.files.length == 0) return false;
		String name;
		boolean success = true;
		
		try{
			ArrayList<StringVectorInt> variables = new ArrayList<StringVectorInt>();
			for (int i = 0; i < options.files.length; i++){
				setFile(options.files[i]);
				if (options.names != null)
					name = options.names[i];
				else{
					name = options.files[i].getName();
					if (name.lastIndexOf(".") > 0) name = name.substring(0, name.lastIndexOf("."));
					}
				
				ArrayList<String> array = loadStringArray(progress_bar);
				if (array != null)
					variables.add(new StringVectorInt(name, array));
					
				success &= array != null;
				}
			
			for (int i = 0; i < variables.size(); i++)
				InterfaceSession.getWorkspace().addVariable(variables.get(i));
			
		}catch (Exception e){
			e.printStackTrace();
			return false;
			}
		
		return success;
		
	}

	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException {
		
		return null;
	}
	
	/*********************************************
	 * Loads an array of Strings from a delimited text file.
	 * 
	 * @param progress_bar
	 * @return
	 * @throws IOException
	 */
	protected ArrayList<String> loadStringArray(ProgressUpdater progress_bar) throws IOException{
		
		if (this.dataFile == null)
			throw new IOException("StringVectorLoader: No input file specified!");
		
		String delimiter = " \t\n\r\f";
		if (options != null)
			delimiter = options.delimiter;
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = reader.readLine();
		
		ArrayList<String> array = new ArrayList<String>();
		
		while (line != null){
			StringTokenizer tokens = new StringTokenizer(line, delimiter);
			
			while (tokens.hasMoreTokens())
				array.add(tokens.nextToken());
			
			line = reader.readLine();
			}
		
		reader.close();
		
		return array;
	}

}