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

package mgui.io.domestic.shapes;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.util.Point2DShape;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;


public class Point2DShapeLoader extends FileLoader {

	public Point2DShapeLoader(File file){
		setFile(file);
	}
	
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		return false;
	}

	public Object[] loadShape2D() throws IOException{
		
		if (dataFile == null || !dataFile.exists()) return null;
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		
		
		
		return null;
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		 return loadShape2D();
	 }
	
	class Point2DShapeParser {
		
		public Point2DShapeParser(){
			
		}
		
		public Point2DShape parseLine(String line){
			
			StringTokenizer tokens = new StringTokenizer(line);
			String token = tokens.nextToken().toLowerCase();
			
			boolean filled = token.startsWith("f");
			if (filled) token = token.substring(1);
			
			if (token.equals("c")){
				//circle
				Ellipse2D.Double circle = new Ellipse2D.Double(Double.valueOf(tokens.nextToken()),
															   Double.valueOf(tokens.nextToken()),
															   1,
															   1);
				
				ArrayList<Shape> shapes = new ArrayList<Shape>();
				shapes.add(circle);
				ArrayList<Boolean> fills = new ArrayList<Boolean>();
				fills.add(filled);
				return new Point2DShape(shapes, fills);
				}
			
			return null;
			
		}
		
		
		
	}
	
}