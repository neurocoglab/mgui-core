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

package mgui.io.domestic.variables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.jogamp.vecmath.Matrix4d;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;


public class MatrixTransformLoader extends FileLoader {

	public MatrixTransformLoader(File file){
		setFile(file);
	}
	
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		return false;
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadMatrix();
	}
	
	/***********************************************
	 * Returns the matrix stored in the current data file.
	 * 
	 * @return
	 */
	public Matrix4d loadMatrix(){
		
		if (!dataFile.exists()){
			InterfaceSession.log("Input tranform file '" + dataFile.getAbsolutePath() + "' not found..");
			return null;
			}
		
		try{
			
			String filename = dataFile.getAbsolutePath();
			String ext = "";
			
			if (filename.contains("."))
				ext = filename.substring(filename.indexOf("."));
			
			if (ext.equals(".xfm")){
				return loadMniMatrix();
				}
			
			return loadAsciiMatrix();
			
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}
		
	}
	
	protected Matrix4d loadAsciiMatrix() throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = reader.readLine();
		
		Matrix4d matrix = new Matrix4d();
		
		for (int i = 0; i < 4; i++){
			StringTokenizer tokens = new StringTokenizer(line);
			
			try{
				for (int j = 0; j < 4; j++)
					matrix.setElement(i, j, Double.valueOf(tokens.nextToken()));
			}catch (Exception e){
				throw new IOException("MatrixTransformLoader: Error reading 4 x 4 Ascii matrix..");
				}
			
			line = reader.readLine();
			}
		
		reader.close();
		return matrix;
	}
	
	protected Matrix4d loadMniMatrix() throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = reader.readLine();
		if (!line.equals("MNI Transform File")){
			InterfaceSession.log("File '" + dataFile.getAbsolutePath() + "' is not an MNI transform file..");
			reader.close();
			return null;
			}
		
		while (line != null && !line.startsWith("Linear_Transform"))
			line = reader.readLine();
		
		if (line == null){
			InterfaceSession.log("Unexpected EOF for transform file '" + dataFile.getAbsolutePath() + "'..");
			reader.close();
			return null;
			}
		
		line = reader.readLine();
		if (line == null){
			InterfaceSession.log("Unexpected EOF for transform file '" + dataFile.getAbsolutePath() + "'..");
			reader.close();
			return null;
			}
		
		StringTokenizer tokens = new StringTokenizer(line);
		Matrix4d matrix = new Matrix4d();
		
		boolean end = false;
		int i = 0;
		
		for (; i < 4 && !end; i++){
			for (int j = 0; j < 4 && !end; j++){
				String t = tokens.nextToken();
				if (t.endsWith(";")){
					t = t.substring(0, t.indexOf(";"));
					end = true;
					}
				matrix.setElement(i, j, Double.valueOf(t));
				}
			line = reader.readLine();
			if (line == null) 
				end = true;
			else
				tokens = new StringTokenizer(line);
			}
		
		//if only three lines, set 4, 4 to 1
		if (i < 4)
			matrix.setElement(3, 3, 1);
		
		reader.close();
		return matrix;
		
	}

}