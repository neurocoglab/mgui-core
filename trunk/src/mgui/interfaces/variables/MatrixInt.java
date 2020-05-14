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

package mgui.interfaces.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;
import Jama.Matrix;

/***********************************************
 * Interface for a 2D matrix, represented internally as a Jama matrix of doubles. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MatrixInt extends VariableInt<MguiDouble> {

	protected Matrix matrix;
	
	public MatrixInt(String name, int m, int n){
		matrix = new Matrix(m, n);
		init();
		setName(name);
	}
	
	public MatrixInt(String name, Matrix m){
		this.matrix = m;
		//type = DataType.Double;
		init();
		setName(name);
	}
	
	public MatrixInt(ArrayList<ArrayList<MguiNumber>> values){
		int m = values.size();
		int n = values.get(0).size();
		matrix = new Matrix(m, n);
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				matrix.set(i, j, values.get(i).get(j).getValue());	
	}
	
	@Override
	public boolean isNumeric(){
		return true;
	}
	
	@Override
	public ArrayList<MguiDouble> getAsList(){
		ArrayList<MguiDouble> list = new ArrayList<MguiDouble>();
		for (int i = 0; i < getM(); i++)
			for (int j = 0; j < getN(); j++)
				list.add(new MguiDouble(matrix.get(i, j)));
		return list;
	}
	
	@Override
	public int getSize(){
		return getN() * getM();
	}
	
	@Override
	public Class<MguiDouble> getType(){
		return MguiDouble.class;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = MatrixInt.class.getResource("/mgui/resources/icons/matrix_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/matrix_20.png");
		return null;
	}
	
	@Override
	public boolean setValue(List<Integer> indices, MguiDouble value) throws VariableException{
		if (!isEditable()) return false;
		
		if (indices.size() < 1 || indices.get(0) < 0 || indices.get(0) >= getN())
			throw new VariableException("MatrixInt: indices are out of bounds.");
		
		matrix.set(indices.get(0), indices.get(1), value.getValue());
		
		updated = true;
		if (!batch)
			fireListeners();
		
		return true;
	}
	
	@Override
	public boolean setStringValue(List<Integer> indices, String value) throws VariableException{
		if (!isEditable()) return false;
		
		if (indices.size() < 2 || indices.get(0) < 0 || indices.get(0) >= getM()
							   || indices.get(1) < 0 || indices.get(1) >= getN())
			throw new VariableException("MatrixInt: indices are out of bounds.");
		
		matrix.set(indices.get(0), indices.get(1), MguiDouble.getValue(value));
		
		updated = true;
		if (!batch)
			fireListeners();
		
		return true;
	}
	
	public MguiDouble getValue(int i, int j) throws VariableException{
		if (i >= 0 && i < getM() &&
			j >= 0 && j < getN())
			return new MguiDouble(matrix.get(i, j));
			
		throw new VariableException("Matrix element out of bounds: " + i + ", " + j);
			
	}
	
	@Override
	public ArrayList<Integer> getDimensions(){
		ArrayList<Integer> dims = new ArrayList<Integer>(2);
		dims.add(getM());
		dims.add(getN());
		return dims;
	}
	
	@Override
	protected MguiDouble getValue(List<Integer> indices) throws VariableException{
		//handle lower dimensionality
		if (indices.size() == 1){
			//return "Double[" + getN() + "]";
			}
		return getValue(indices.get(0), indices.get(1));
	}
	
	/*************************************************
	 * Returns a sub-matrix of this matrix, defined by start and end indices.
	 * 
	 * @param row_start
	 * @param col_start
	 * @param row_end
	 * @param col_end
	 * @return
	 */
	public MatrixInt getPart(int row_start, int row_end, int col_start, int col_end){
		return getPart(this.getName() + ".sub", row_start, row_end, col_start, col_end);
	}
	
	/*************************************************
	 * Returns a sub-matrix of this matrix, defined by start and end indices, with the given name.
	 * 
	 * @param name
	 * @param row_start
	 * @param col_start
	 * @param row_end
	 * @param col_end
	 * @return
	 */
	public MatrixInt getPart(String name, int row_start, int col_start, int row_end, int col_end){
		
		Matrix sub = this.matrix.getMatrix(row_start, row_end, col_start, col_end);
		return new MatrixInt(name, sub);
		
	}
	
	
	/*****************************************************
	 * Parses <code>part</code> to return a sub-matrix. 
	 * Syntax is: "<code>row_start,col_start:row_end,col_end</code>".
	 * E.g., "<code>1,2:20,2</code>". Use an asterisk "*" to indicate the last row and column,
	 * respectively (actually replaces the value with M-1, N-1).
	 * E.g,, "<code>0,0:*,*</code>" is the entire matrix.
	 * 
	 * @param name
	 * @param part
	 * @return
	 */
	@Override
	public MatrixInt getPart(String name, String part){
		
		if (!part.contains(":")) return null;
		Pattern p = Pattern.compile(":");
		String[] parts = p.split(part);
		p = Pattern.compile(",");
		String[] n1 = p.split(parts[0]);
		String[] n2 = p.split(parts[1]);
		
		if (n2[0].equals("*"))
			n2[0] = "" + (getM() - 1);
		if (n2[1].equals("*"))
			n2[1] = "" + (getN() - 1);
		
		return getPart(name, Integer.valueOf(n1[0]), Integer.valueOf(n1[0]),
							 Integer.valueOf(n2[0]), Integer.valueOf(n2[0]));
	}
	
	/*************************************************
	 * Returns row <code>row</code> as a {@link VectorInt} object.
	 * 
	 * @param row
	 * @return
	 */
	public VectorInt<MguiDouble> getRow(int row){
		return getRow("No-name", row);
	}
	
	/*************************************************
	 * Returns row <code>row</code> as a {@link VectorInt} object.
	 * 
	 * @param row
	 * @return
	 */
	public VectorInt<MguiDouble> getRow(String name, int row){
		VectorInt<MguiDouble> vector = new VectorInt<MguiDouble>(name, getN());
		Matrix mat = matrix.getMatrix(new int[]{row}, 0, getN() - 1);
		ArrayList<MguiDouble> array = new ArrayList<MguiDouble>(getN());
		for (int i = 0; i < getN(); i++)
			array.add(new MguiDouble(mat.get(i,0)));
		vector.setValues(array);
		return vector;
	}
	
	/*************************************************
	 * Returns column <code>col</code> as a {@link VectorInt} object.
	 * 
	 * @param col
	 * @return
	 */
	public VectorInt<MguiDouble> getColumn(int col){
		return getColumn("No-name", col);
	}
	
	/*************************************************
	 * Returns column <code>col</code> as a {@link VectorInt} object.
	 * 
	 * @param row
	 * @return
	 */
	public VectorInt<MguiDouble> getColumn(String name, int col){
		VectorInt<MguiDouble> vector = new VectorInt<MguiDouble>(name, getM());
		Matrix m = matrix.getMatrix(0, getM() - 1, new int[]{col});
		ArrayList<MguiDouble> array = new ArrayList<MguiDouble>(getM());
		for (int i = 0; i < getM(); i++)
			array.add(new MguiDouble(m.get(0,i)));
		vector.setValues(array);
		return vector;
	}
	
	/***********************************
	 * Returns the row dimension, M
	 * 
	 * @return
	 */
	public int getM(){
		return matrix.getRowDimension();
	}
	
	/************************************
	 * Returns the column dimension, N
	 * 
	 * @return
	 */
	public int getN(){
		return matrix.getColumnDimension();
	}
	
	/**************************************************
	 * Returns the Jama matrix for this object.
	 * 
	 * @return
	 */
	public Matrix getJamaMatrix(){
		return matrix;
	}
	
	@Override
	public String toString(){
		return super.toString() + " [" + getM() + ", " + getN() + "]";
	}
	
}