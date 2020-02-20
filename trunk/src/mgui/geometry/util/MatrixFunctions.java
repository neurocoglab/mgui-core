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

package mgui.geometry.util;

import java.util.ArrayList;

import mgui.interfaces.Utility;
import Jama.Matrix;

/***************************************************************
 * Utility class providing functions on Jama matrices.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MatrixFunctions extends Utility {

	/***********************
	 * Returns {@code row} as an {@code ArrayList}.
	 * 
	 * @param M
	 * @param row
	 * @return
	 */
	public static ArrayList<Double> getRow(Matrix M, int row){
		ArrayList<Double> list = new ArrayList<Double>(M.getColumnDimension());
		for (int j = 0; j < M.getColumnDimension(); j++)
			list.add(M.get(row, j));
		return list;
	}
	
	/***********************
	 * Returns {@code col} as an {@code ArrayList}.
	 * 
	 * @param M
	 * @param row
	 * @return
	 */
	public static ArrayList<Double> getColumns(Matrix M, int col){
		ArrayList<Double> list = new ArrayList<Double>(M.getRowDimension());
		for (int i = 0; i < M.getRowDimension(); i++)
			list.add(M.get(i, col));
		return list;
	}
	
	
	/***********************
	 * Returns the maximum of {@code row}.
	 * 
	 * @param M 		A 2D matrix
	 * @param row
	 * @return
	 */
	public static double getMaxForRow(Matrix M, int row){
		double max = -Double.MAX_VALUE;
		for (int c = 0; c < M.getColumnDimension(); c++)
			max = Math.max(M.get(row, c), max);
		
		return max;
	}
	
	/***********************
	 * Returns the maximums of the rows of M.
	 * 
	 * @param M 		A 2D matrix
	 * @return
	 */
	public static ArrayList<Double> getRowMaxes(Matrix M){
		
		ArrayList<Double> maxes = new ArrayList<Double>(M.getRowDimension());
		
		for (int r = 0; r < M.getRowDimension(); r++)
			maxes.add(getMaxForRow(M, r));
		
		return maxes;
	}
	
	/***********************
	 * Returns the maximum of {@code col}.
	 * 
	 * @param M	 		A 2D matrix
	 * @param row
	 * @return
	 */
	public static double getMaxForColumn(Matrix M, int col){
		double max = -Double.MAX_VALUE;
		for (int r = 0; r < M.getRowDimension(); r++)
			max = Math.max(M.get(r, col), max);
		
		return max;
		
	}
	
	/***********************
	 * Returns the maximums of the columns of M.
	 * 
	 * @param M 		A 2D matrix
	 * @return
	 */
	public static ArrayList<Double> getColumnMaxes(Matrix M){
		
		ArrayList<Double> maxes = new ArrayList<Double>(M.getColumnDimension());
		
		for (int c = 0; c < M.getColumnDimension(); c++)
			maxes.add(getMaxForColumn(M, c));
		
		return maxes;
	}
	
	/***********************
	 * Returns the minimum of {@code row}.
	 * 
	 * @param M 		A 2D matrix
	 * @param row
	 * @return
	 */
	public static double getMinForRow(Matrix M, int row){
		double max = -Double.MAX_VALUE;
		for (int c = 0; c < M.getColumnDimension(); c++)
			max = Math.min(M.get(row, c), max);
		
		return max;
	}
	
	/***********************
	 * Returns the minimums of the rows of M.
	 * 
	 * @param M 		A 2D matrix
	 * @return
	 */
	public static ArrayList<Double> getRowMins(Matrix M){
		
		ArrayList<Double> mins = new ArrayList<Double>(M.getRowDimension());
		
		for (int r = 0; r < M.getRowDimension(); r++)
			mins.add(getMinForRow(M, r));
		
		return mins;
	}
	
	/***********************
	 * Returns the minimum of {@code col}.
	 * 
	 * @param M	 		A 2D matrix
	 * @param row
	 * @return
	 */
	public static double getMinForColumn(Matrix M, int col){
		double max = -Double.MAX_VALUE;
		for (int r = 0; r < M.getRowDimension(); r++)
			max = Math.min(M.get(r, col), max);
		
		return max;
		
	}
	
	/***********************
	 * Returns the minimums of the columns of M.
	 * 
	 * @param M 		A 2D matrix
	 * @return
	 */
	public static ArrayList<Double> getColumnMins(Matrix M){
		
		ArrayList<Double> mins = new ArrayList<Double>(M.getColumnDimension());
		
		for (int c = 0; c < M.getColumnDimension(); c++)
			mins.add(getMinForColumn(M, c));
		
		return mins;
	}
	
	/***********************
	 * Returns the mean of {@code row}.
	 * 
	 * @param M 		A 2D matrix
	 * @param row
	 * @return
	 */
	public static double getMeanForRow(Matrix M, int row){
		return getSumForRow(M, row) / (double)M.getColumnDimension();
	}
	
	/***********************
	 * Returns the means of the rows of M.
	 * 
	 * @param M 		A 2D matrix
	 * @return
	 */
	public static ArrayList<Double> getRowMeans(Matrix M){
		
		ArrayList<Double> means = new ArrayList<Double>(M.getRowDimension());
		
		for (int r = 0; r < M.getRowDimension(); r++)
			means.add(getMeanForRow(M, r));
		
		return means;
	}
	
	/***********************
	 * Returns the mean of {@code col}.
	 * 
	 * @param M	 		A 2D matrix
	 * @param row
	 * @return
	 */
	public static double getMeanForColumn(Matrix M, int col){
		return getSumForColumn(M, col) / (double)M.getRowDimension();
	}
	
	/***********************
	 * Returns the means of the columns of M.
	 * 
	 * @param M 		A 2D matrix
	 * @return
	 */
	public static ArrayList<Double> getColumnMeans(Matrix M){
		
		ArrayList<Double> means = new ArrayList<Double>(M.getColumnDimension());
		
		for (int c = 0; c < M.getColumnDimension(); c++)
			means.add(getMeanForColumn(M, c));
		
		return means;
	}
	
	/***********************
	 * Returns the sum of {@code row}.
	 * 
	 * @param M 		A 2D matrix
	 * @param row
	 * @return
	 */
	public static double getSumForRow(Matrix M, int row){
		double sum = 0;
		for (int c = 0; c < M.getColumnDimension(); c++)
			sum += M.get(row, c);
		
		return sum;
	}
	
	/***********************
	 * Returns the sums of the rows of M.
	 * 
	 * @param M 		A 2D matrix
	 * @return
	 */
	public static ArrayList<Double> getRowSums(Matrix M){
		
		ArrayList<Double> sums = new ArrayList<Double>(M.getRowDimension());
		
		for (int r = 0; r < M.getRowDimension(); r++)
			sums.add(getSumForRow(M, r));
		
		return sums;
	}
	
	/***********************
	 * Returns the sum of {@code col}.
	 * 
	 * @param M	 		A 2D matrix
	 * @param row
	 * @return
	 */
	public static double getSumForColumn(Matrix M, int col){
		double sum = 0;
		for (int r = 0; r < M.getRowDimension(); r++)
			sum += M.get(r, col);
		
		return sum;
		
	}
	
	/***********************
	 * Returns the sums of the columns of M.
	 * 
	 * @param M 		A 2D matrix
	 * @return
	 */
	public static ArrayList<Double> getColumnSums(Matrix M){
		
		ArrayList<Double> sums = new ArrayList<Double>(M.getColumnDimension());
		
		for (int c = 0; c < M.getColumnDimension(); c++)
			sums.add(getSumForColumn(M, c));
		
		return sums;
	}
	
}