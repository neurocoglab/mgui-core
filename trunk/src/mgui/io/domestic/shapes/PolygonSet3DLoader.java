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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import foxtrot.Job;
import foxtrot.Worker;
import mgui.datasources.DataTypes;
import mgui.geometry.Polygon3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.Polygon3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.numbers.MguiShort;

/**************************************
 * Loads a set of 3D polygon objects (*.poly3d) from text.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PolygonSet3DLoader extends FileLoader {

	public boolean skip_lines = false;
	public int skip = 1;
	public boolean skip_min_nodes = false;
	public int min_nodes = 1;
	public boolean embed_single_polygon = false; 		// Whether to embed a single polygon in a shape set
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		PolygonSet3DInputOptions _options = (PolygonSet3DInputOptions)options;
		boolean success = true;
		ShapeSet3DInt parent_set = null;
		
		if (_options.new_shape_set) {
			parent_set = new ShapeSet3DInt(_options.new_shape_set_name);
			parent_set.setAttribute("IsOverriding", new MguiBoolean(true));
			ShapeSet3DInt grandparent_set = (ShapeSet3DInt)_options.shape_set;
			grandparent_set.addShape(parent_set);
			
		}else{
			parent_set = (ShapeSet3DInt)_options.shape_set;
			if (parent_set == null) {
				failure_msg = "No shape set specified!";
				return false;
				}
			}
		
		if (_options.getFiles() == null) {
			failure_msg = "No input files specified!";
			return false;
			}
		
		parent_set.setLive(false);
		
		for (int i = 0; i < _options.getFiles().length; i++){
			setFile(_options.getFiles()[i]);
			try{
				skip_lines = _options.skip_lines;
				skip = _options.skip;
				skip_min_nodes = _options.skip_min_nodes;
				min_nodes = _options.min_nodes;
				Shape3DInt polygon_int = (Shape3DInt)loadObject(progress_bar,_options);
				if (polygon_int == null)
					success = false;
				else{
					polygon_int.setName(_options.names[i]);
					if (_options.new_shape_set) {
						polygon_int.setAttribute("InheritFromParent", new MguiBoolean(true));
						}
					System.out.print("Adding Polygon3D object..");
					parent_set.addShape(polygon_int, false);
					InterfaceSession.log("done.");
					}
			}catch (IOException e){
				InterfaceSession.log("PolygonSet3DLoader: I/O Error reading data from " + this.dataFile.getAbsolutePath());
				failure_msg = "Couldn't read data from " + dataFile.getAbsolutePath();
				success = false;
			}catch (Exception e){
				InterfaceSession.log("PolygonSet3DLoader: Error reading data from " + this.dataFile.getAbsolutePath());
				failure_msg = "Couldn't read data from " + dataFile.getAbsolutePath();
				InterfaceSession.handleException(e);
				success = false;
				}
			}
		
		parent_set.setLive(true);
		
		return success;
	}
	
	String failure_msg = null;
	
	@Override
	public String getFailureMessage() {
		
		if (failure_msg == null || failure_msg.length() == 0) {
			return super.getFailureMessage();
			}
		
		String msg = failure_msg;
		failure_msg = null;
		
		return msg;
		
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		ShapeSet3DInt shape_set = loadPolygonSet(progress_bar);
		if (shape_set == null || embed_single_polygon || shape_set.getSize() > 1) 
			return shape_set;
		
		// This is a single, polygon, return as a Polygon3DInt object
		Polygon3DInt polygon = (Polygon3DInt)shape_set.members.get(0);
		polygon.setName(shape_set.getName());
		return polygon;
		
	}
	
	public ShapeSet3DInt loadPolygonSet(final ProgressUpdater progress_bar) throws IOException{
		
		if (dataFile == null)
			throw new IOException(getClass().getSimpleName() + ": No input file set.");
		
		ShapeSet3DInt polygon_set = null;
		
		if (progress_bar != null){
			polygon_set = (ShapeSet3DInt)Worker.post(new Job(){
				@Override
				public ShapeSet3DInt run(){
					try{
						return loadPolygonSetBlocking(progress_bar);
					}catch (IOException e){
						e.printStackTrace();
						return null;
						}
				}
			});
		}else{
			polygon_set = loadPolygonSetBlocking(progress_bar);
			}
		
		return polygon_set;
	}
	
	protected ShapeSet3DInt loadPolygonSetBlocking(ProgressUpdater progress_bar) throws IOException{
		
		try {
		
		//load in domestic format
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = reader.readLine();
		if (line == null) {
			reader.close();
			throw new IOException("PolygonSet3DLoader: unexpected end of file in '" + dataFile.getAbsolutePath() + "'.");
			}
		
		int n_polys = Integer.valueOf(line);
		
		if (progress_bar != null){
			progress_bar.setMessage("Loading polygons from '" + dataFile.getName() + "': ");
			progress_bar.setMinimum(0);
			progress_bar.setMaximum(n_polys);
			progress_bar.reset();
			}
		
		ShapeSet3DInt polygons = new ShapeSet3DInt();
		
		for (int i = 0; i < n_polys; i++){
			line = reader.readLine();
			while (line != null && line.length() == 0)
				line = reader.readLine();
			if (line == null) {
				reader.close();
				throw new IOException("PolygonSet3DLoader: unexpected end of file in '" + dataFile.getAbsolutePath() + "'.");
				}
			String name = "Polygon_" + (i+1);
			if (line.contains("\"")) line = line.replaceAll("\"", "");
			String[] parts = line.split(" ");
			int n = Integer.valueOf(parts[0]);
			boolean is_closed = false;
			
			if (parts.length > 1){
				is_closed = parts[1].equals("1");
				}
			
			if (parts.length > 2){
				name = parts[2];
				}
			
			String[] hdr = null;
			int[] type = null;
			int n_vals = 0;
			NameMap[] name_maps = null;
			Integer[] indices = null;
 			ArrayList<ArrayList<MguiNumber>> values = null;
			if (parts.length > 3) {
				hdr = new String[parts.length - 3];
				type = new int[parts.length - 3];
				name_maps = new NameMap[parts.length - 3];
				indices = new Integer[parts.length - 3];
				for (int j = 3; j < parts.length; j++) {
					String str = parts[j];
					if (str.contains("%")) {
						String t = str.substring(str.lastIndexOf("%s")+1);
						switch(t) {
							case "f":
								type[j-3] = DataTypes.FLOAT;
								break;
							case "i":
								type[j-3] = DataTypes.INTEGER;
								break;
							case "d":
								type[j-3] = DataTypes.DOUBLE;
								break;
							case "s":
								type[j-3] = DataTypes.SHORT;
								break;
							case "q":
								type[j-3] = DataTypes.STRING;
								name_maps[j-3] = new NameMap(name);
								indices[j-3] = 0;
								break;
							default:
								throw new IOException("PolygonSet3DLoader: invalid data type '" + t + "'.");
							}
					} else {
						type[j-3] = DataTypes.FLOAT;
						}
					hdr[j-3] = parts[j];
					}
				n_vals = hdr.length;
				values = new ArrayList<ArrayList<MguiNumber>>(n_vals);
				for (int j = 0; j < n_vals; j++) {
					values.add(new ArrayList<MguiNumber>(n));
					}
				}
			
			Polygon3D polygon =  new Polygon3D();
			float[] nodes = new float[n * 3];
			for (int j = 0; j < n; j++){
				line = reader.readLine();
				if (line == null) {
					reader.close();
					throw new IOException("PolygonSet3DLoader: unexpected end of file in '" + dataFile.getAbsolutePath() + "'.");
					}
				StringTokenizer tokens = new StringTokenizer(line);
				nodes[j * 3] = Float.valueOf(tokens.nextToken());
				nodes[(j * 3) + 1] = Float.valueOf(tokens.nextToken());
				nodes[(j * 3) + 2] = Float.valueOf(tokens.nextToken());
				
				for (int k = 0; k < n_vals; k++) {
					String str = tokens.nextToken();
					MguiNumber num = null;
					if (type[k] == DataTypes.STRING) {
						Integer index = name_maps[k].get(str);
						if (index == -Integer.MAX_VALUE) {
							indices[k]++;
							num = new MguiInteger(indices[k]);
						} else {
							num = new MguiInteger(index);
							}
					}else {
						num = getNumber(str, type[k]);
						if (num == null) {
							// Shouldn't happen
							reader.close();
							throw new IOException("PolygonSet3DLoader: Bad data type: '" + type[k] + "'.");
							}
						}
					values.get(k).add(num);
					}
				}
			
			polygon.nodes = nodes;
			polygon.n = n;
			
			if (progress_bar != null)
				progress_bar.update(i);
			
			Polygon3DInt poly_int = new Polygon3DInt(polygon, name);
			poly_int.setClosed(is_closed);
			
			for (int k = 0; k < n_vals; k++) {
				poly_int.addVertexData(hdr[k], values.get(k));
				}
			
			polygons.addShape(poly_int);
			}

		reader.close();
		return polygons;
		
		} catch (NumberFormatException ex) {
			throw new IOException("PolygonSet3DLoader: Bad number format: " + ex.getMessage());
			}
		
	}
	
	private MguiNumber getNumber( String str, int type) {
		
		switch(type) {
			case DataTypes.FLOAT:
				return new MguiFloat(str);
				
			case DataTypes.INTEGER:
				return new MguiInteger(str);
				
			case DataTypes.DOUBLE:
				return new MguiDouble(str);
				
			case DataTypes.SHORT:
				return new MguiShort(str);
				
			default:
					return null;
			}
		
		
	}
	
}