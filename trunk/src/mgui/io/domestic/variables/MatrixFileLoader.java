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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.variables.MatrixInt;
import mgui.interfaces.variables.VariableInt;
import mgui.interfaces.variables.VectorInt;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOException;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceDataFileWriter;
import mgui.io.domestic.variables.MatrixInOptions.Format;
import mgui.io.domestic.variables.MatrixInOptions.Variable;
import mgui.numbers.MguiDouble;
import Jama.Matrix;

public abstract class MatrixFileLoader extends FileLoader {

	protected MatrixInOptions options;
	public boolean has_header = true;
	
	public MatrixFileLoader(){
		
	}
	
	/**************************************
	 * Constructs this loader with default options:
	 * 
	 * <p>Format = AsciiFull
	 * <p>As type = Matrix
	 * 
	 * @param file
	 */
	public MatrixFileLoader(File file){
		this(file, Format.AsciiFull, Variable.Matrix);
	}
	
	/**************************************
	 * Constructs this loader with the given format, and default type:
	 * 
	 * <p>As type = Matrix
	 * 
	 * @param file
	 */
	public MatrixFileLoader(File file, Format format){
		this(file, format, Variable.Matrix);
	}
	
	/**************************************
	 * Constructs this loader with the specified format and type.
	 * 
	 * @param file
	 */
	public MatrixFileLoader(File file, Format format, Variable as_type){
		setFile(file);
		options = new MatrixInOptions();
		options.setFiles(new File[]{file});
		options.format = format;
		options.as_type = as_type;
	}
	
	@Override
	public boolean load(InterfaceIOOptions _options, ProgressUpdater progress_bar) {
		if (_options == null || !(_options instanceof MatrixInOptions)) return false;
		
		options = (MatrixInOptions)_options;
		
		if (options.files == null || options.files.length == 0) return false;
		String name;
		boolean success = true;
		has_header = options.has_header;
		
		try{
			ArrayList<VariableInt<?>> variables = new ArrayList<VariableInt<?>>();
			for (int i = 0; i < options.files.length; i++){
				setFile(options.files[i]);
				if (options.names != null)
					name = options.names[i];
				else{
					name = options.files[i].getName();
					if (name.lastIndexOf(".") > 0) name = name.substring(0, name.lastIndexOf("."));
					}
				Matrix matrix = loadMatrix(progress_bar);
				success &= matrix != null;
				String this_name = options.names[i];
				double[][] array;
				ArrayList<MguiDouble> list;
				switch (options.as_type){
					case Matrix:
						variables.add(new MatrixInt(this_name, matrix));
						break;
					case ColumnsAsVectors:
						array = matrix.getArray();
						for (int row = 0; row < array.length; row++){
							double[] col_array = array[row];
							list = new ArrayList<MguiDouble>(col_array.length);
							for (int j = 0; j < col_array.length; j++)
								list.add(new MguiDouble(col_array[j]));
							String n = this_name + "." + row;
							VectorInt<MguiDouble> vector = new VectorInt<MguiDouble>(n, list);
							variables.add(vector);
							}
						break;
					case RowsAsVectors:
						array = matrix.getArray();
						for (int col = 0; col < array[0].length; col++){
							list = new ArrayList<MguiDouble>(array.length);
							for (int j = 0; j < array.length; j++)
								list.add(new MguiDouble(array[j][col]));
							String n = this_name + "." + col;
							VectorInt<MguiDouble> vector = new VectorInt<MguiDouble>(n, list);
							variables.add(vector);
							}
						break;
					case VectorPackedRow:
						double[] row_array = matrix.getRowPackedCopy();
						list = new ArrayList<MguiDouble>(row_array.length);
						for (int j = 0; j < row_array.length; j++)
							list.add(new MguiDouble(row_array[j]));
						variables.add(new VectorInt<MguiDouble>(this_name, list));
						break;
					case VectorPackedColumn:
						double[] col_array = matrix.getColumnPackedCopy();
						list = new ArrayList<MguiDouble>(col_array.length);
						for (int j = 0; j < col_array.length; j++)
							list.add(new MguiDouble(col_array[j]));
						variables.add(new VectorInt<MguiDouble>(this_name, list));
						break;
					}
				}
			// Add all variables
			for (int i = 0; i < variables.size(); i++)
				InterfaceSession.getWorkspace().addVariable(variables.get(i));
			
		}catch (Exception e){
			e.printStackTrace();
			return false;
			}
		
		return success;
	}

	public abstract Matrix loadMatrix(final ProgressUpdater progress_bar) throws IOException, InterfaceIOException;
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = MatrixFileLoader.class.getResource("/mgui/resources/icons/matrix_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/matrix_20.png");
		return null;
	}
	
}