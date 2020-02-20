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

package mgui.interfaces.math;

import java.util.ArrayList;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;

import org.cheffo.jeplite.JEP;
import org.cheffo.jeplite.ParseException;

/***********************************************
 * Class represents a mathematical expression, and methods to evaluate it. This is based upon the
 * <a href="http://www.singularsys.com/jep/doc/html/usage.html">Jep Java - Math Expression Parser v 4.2.1</a>, 
 * issued under the GNU Public License. JEP has since switched to a commercial license.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MathExpression {

	static double missing_value = 0;
	
	/***********************************************
	 * Evaluates a mathematical expression, returning an array of {@code double}s, if n < 3,
	 * otherwise returns a set of embedded {@code ArrayList<Double>}s. 
	 * 
	 * @param options
	 * @return
	 */
	public static Object evaluate(MathExpressionOptions options){
		return evaluate(options, null);
	}
	
	/***********************************************
	 * Evaluates a mathematical expression, returning an array of {@code double}s, if n < 3,
	 * otherwise returns a set of embedded {@code ArrayList<Double>}s. 
	 * 
	 * @param options
	 * @return
	 */
	public static Object evaluate(MathExpressionOptions options, ProgressUpdater updater){
		
		if (options.jep == null || options.variable == null) return false;
		int[] dims = options.variable.getDimensions();
		
			switch (dims.length){
			case 1: return evaluate1d(options.jep, options.variable, dims[0], options.used_variables, updater);
			case 2:	return evaluate2d(options.jep, options.variable, dims[0], dims[1], options.used_variables, updater);
			case 3:	return evaluate3d(options.jep, options.variable, dims[0], dims[1], dims[2], options.used_variables, updater);
			default: return evaluateNd(options.jep, options.variable, dims);
			}
	}
	
	/***********************************************
	 * Evaluates a conditional expression, returning an array of {@code boolean}s.
	 * 
	 * @param options
	 * @return
	 */
	public static Object evaluate_conditional(MathExpressionOptions options){
		return evaluate_conditional(options, null);
	}
	
	/***********************************************
	 * Evaluates a conditional expression, returning an array of {@code boolean}s.
	 * 
	 * @param options
	 * @return
	 */
	public static Object evaluate_conditional(MathExpressionOptions options, ProgressUpdater updater){
		
		if (options.jep == null || options.variable == null) return false;
		int[] dims = options.variable.getDimensions();
		
			switch (dims.length){
				case 1: return evaluate1b(options.jep, options.variable, dims[0], options.used_variables, updater);
				case 2:	return evaluate2b(options.jep, options.variable, dims[0], dims[1], options.used_variables, updater);
				case 3:	return evaluate3b(options.jep, options.variable, dims[0], dims[1], dims[2], options.used_variables, updater);
				default: return evaluateNb(options.jep, options.variable, dims);
			}
		
	}
		
	public static Object evaluate(JEP jep, VariableObject variable){
		int[] dims = variable.getDimensions();
		
		if (dims.length == 1)
			return evaluate1d(jep, variable, dims[0]);
		else
			return evaluate2d(jep, variable, dims[0], dims[1]);
	}
	
	public static double[] evaluate1d(JEP jep, VariableObject variable, int n){
		return evaluate1d(jep, variable, n, null, null);
	}
	
	public static double[] evaluate1d(JEP jep, VariableObject variable, int n, ArrayList<String> used_variables, ProgressUpdater updater){
		
		double[] result = new double[n];
		if (used_variables == null)
			used_variables = variable.getVariables();
		if (updater != null){
			updater.setMinimum(0);
			updater.setMaximum(n);
			updater.update(0);
			}
		
		for (int i = 0; i < n; i++){
			for (int v = 0; v < used_variables.size(); v++)
				jep.setVarValue(used_variables.get(v), variable.getVariableValue(used_variables.get(v), new int[]{i}));
			try{
				double d = jep.getValue();
				if (Double.isNaN(d)) d = missing_value;
				result[i] = d;
			}catch (ParseException ex){
				InterfaceSession.log("ParseException evaluating function: " + ex.getMessage(), LoggingType.Errors);
				return null;
				}
			if (updater != null){
				if (updater.isCancelled()){
					InterfaceSession.log("MathExpression evaluate cancelled by user.", LoggingType.Warnings);
					return null;
					}
				updater.update(i);
				}
			}
		
		return result;
	}
	
	public static double[][] evaluate2d(JEP jep, VariableObject variable, int m, int n){
		return evaluate2d(jep, variable, m, n, null, null);
	}
	
	public static double[][] evaluate2d(JEP jep, VariableObject variable, int m, int n, ArrayList<String> used_variables, ProgressUpdater updater){
		
		double[][] result = new double[m][n];
		if (used_variables == null)
			used_variables = variable.getVariables();
		if (updater != null){
			updater.setMinimum(0);
			updater.setMaximum(m);
			updater.update(0);
			}
		
		for (int i = 0; i < m; i++){
			for (int j = 0; j < n; j++){
				for (int v = 0; v < used_variables.size(); v++)
					jep.setVarValue(used_variables.get(v), variable.getVariableValue(used_variables.get(v), new int[]{i, j}));
				try{
					result[i][j] = jep.getValue();
				}catch (ParseException ex){
					InterfaceSession.log("ParseException evaluating function: " + ex.getMessage(), LoggingType.Errors);
					return null;
					}
				}
			if (updater != null){
				if (updater.isCancelled()){
					InterfaceSession.log("MathExpression evaluate cancelled by user.", LoggingType.Warnings);
					return null;
					}
				updater.update(i);
				}
			}
		
		return result;
	}
	
	public static double[][][] evaluate3d(JEP jep, VariableObject variable, int m, int n, int o){
		return evaluate3d(jep, variable, m, n, o, null, null);
	}
	
	public static double[][][] evaluate3d(JEP jep, VariableObject variable, int m, int n, int o, ArrayList<String> used_variables, ProgressUpdater updater){
		
		double[][][] result = new double[m][n][o];
		if (used_variables == null)
			used_variables = variable.getVariables();
		if (updater != null){
			updater.setMinimum(0);
			updater.setMaximum(m);
			updater.update(0);
			}
		
		for (int i = 0; i < m; i++){
			for (int j = 0; j < n; j++)
				for (int k = 0; k < n; k++){
					for (int v = 0; v < used_variables.size(); v++)
						jep.setVarValue(used_variables.get(v), variable.getVariableValue(used_variables.get(v), new int[]{i, j, k}));
					try{
						result[i][j][k] = jep.getValue();
					}catch (ParseException ex){
						InterfaceSession.log("ParseException evaluating function: " + ex.getMessage(), LoggingType.Errors);
						return null;
						}
					}
			if (updater != null){
				if (updater.isCancelled()){
					InterfaceSession.log("MathExpression evaluate cancelled by user.", LoggingType.Warnings);
					return null;
					}
				updater.update(i);
				}
			}
		
		return result;
	}
	
	public static Object evaluateNd(JEP jep, VariableObject variable, int[] dims){
		return evaluateNd(jep, variable, dims, (ArrayList<String>)null);
	}
	
	/**********************************************
	 * Evaluates an N-dimensional variable. Returns a set of embedded {@code ArrayList<Double>}s.
	 * 
	 * @param jep
	 * @param variable
	 * @param dims
	 * @return
	 */
	public static Object evaluateNd(JEP jep, VariableObject variable, int[] dims, ArrayList<String> used_variables){
		
		int n = dims[0];
		
		int[] new_dims = new int[dims.length - 1];
		for (int i = 1; i < dims.length; i++)
			new_dims[i-1] = dims[i];
		
		ArrayList<ArrayList<?>> data = new ArrayList<ArrayList<?>>();
		
		// Top-level list of data; add new dimensions recursively
		for (int i = 0; i < n; i++){
			int[] current_index = new int[]{i};
			data.add(evaluateNd(jep, variable, new_dims, current_index));
			}
		
		return data;
	}
	
	private static ArrayList<?> evaluateNd(JEP jep, VariableObject variable, int[] dims, int[] current_index){
		
		int len = dims.length;
		
		if (len == 1){
			// This is the lowest dimension
			// Cycle through current index + last index
			int n = dims[0];
			int cl = current_index.length;
			int fl = cl + 1;
			int[] final_index = new int[fl];
			System.arraycopy(current_index, 0, final_index, 0, cl);
			
			ArrayList<Double> result = new ArrayList<Double>();
			ArrayList<String> variables = variable.getVariables();
			
			for (int i = 0; i < n; i++){
				final_index[cl] = i;
				for (int v = 0; v < variables.size(); v++)
					jep.setVarValue(variables.get(v), variable.getVariableValue(variables.get(v), final_index));
				try{
					result.add(jep.getValue());
				}catch (ParseException ex){
					InterfaceSession.log("ParseException evaluating function: " + ex.getMessage(), LoggingType.Errors);
					return null;
					}
				}
			
			return result;
			}
		
		// Otherwise there are more dimensions; continue recursion
		int n = dims[0];
		
		int[] new_dims = new int[dims.length - 1];
		for (int i = 1; i < dims.length; i++)
			new_dims[i-1] = dims[i];
		
		ArrayList<ArrayList<?>> data = new ArrayList<ArrayList<?>>();
		int cl = current_index.length;
		int fl = cl + 1;
		int[] next_index = new int[fl];
		System.arraycopy(current_index, 0, next_index, 0, cl);
		
		for (int i = 0; i < n; i++){
			next_index[cl] = i;
			data.add(evaluateNd(jep, variable, new_dims, current_index));
			}
		
		return data;
	}

	static void _evaluateNd(JEP jep, VariableObject variable, int[] dims, Object result){
		
		
		
	}
	
	public static boolean[] evaluate1b(JEP jep, VariableObject variable, int n){
		return evaluate1b(jep, variable, n, null, null);
	}
	
	public static boolean[] evaluate1b(JEP jep, VariableObject variable, int n, ArrayList<String> used_variables, ProgressUpdater updater){
		
		boolean[] result = new boolean[n];
		if (used_variables == null)
			used_variables = variable.getVariables();
		int[] where = new int[1];
		
		if (updater != null){
			updater.setMinimum(0);
			updater.setMaximum(n);
			updater.update(0);
			}
		
		for (int i = 0; i < n; i++){
			where[0]=i;
			for (int v = 0; v < used_variables.size(); v++)
				jep.setVarValue(used_variables.get(v), variable.getVariableValue(used_variables.get(v), where));
			try{
				double d = jep.getValue();
				result[i] = d==1;
			}catch (ParseException ex){
				InterfaceSession.log("ParseException evaluating function: " + ex.getMessage(), LoggingType.Errors);
				return null;
				}
			
			if (updater != null){
				if (updater.isCancelled()){
					InterfaceSession.log("MathExpression evaluate cancelled by user.", LoggingType.Warnings);
					return null;
					}
				updater.update(i);
				}
			}
		
		return result;
	}
	
	public static boolean[][] evaluate2b(JEP jep, VariableObject variable, int m, int n){
		return evaluate2b(jep, variable, m, n, null, null);
	}
	
	public static boolean[][] evaluate2b(JEP jep, VariableObject variable, int m, int n, ArrayList<String> used_variables, ProgressUpdater updater){
	
		
		boolean[][] result = new boolean[m][n];
		if (used_variables == null)
			used_variables = variable.getVariables();
		int[] where = new int[2];
		
		if (updater != null){
			updater.setMinimum(0);
			updater.setMaximum(m);
			updater.update(0);
			}
		
		for (int i = 0; i < m; i++){
			where[0] = i;
			for (int j = 0; j < n; j++){
				where[1] = j;
				for (int v = 0; v < used_variables.size(); v++)
					jep.setVarValue(used_variables.get(v), variable.getVariableValue(used_variables.get(v), where));
				try{
					double d = jep.getValue();
					result[i][j] = d == 1;
				}catch (ParseException ex){
					InterfaceSession.log("ParseException evaluating function: " + ex.getMessage(), LoggingType.Errors);
					return null;
					}
				}
			if (updater != null){
				if (updater.isCancelled()){
					InterfaceSession.log("MathExpression evaluate cancelled by user.", LoggingType.Warnings);
					return null;
					}
				updater.update(i);
				}
			}
		
		return result;
	}
	
	public static boolean[][][] evaluate3b(JEP jep, VariableObject variable, int m, int n, int o){
		return evaluate3b(jep, variable, m, n, o, null, null);
	}
	
	public static boolean[][][] evaluate3b(JEP jep, VariableObject variable, int m, int n, int o, ArrayList<String> used_variables, ProgressUpdater updater){
	
		
		boolean[][][] result = new boolean[m][n][o];
		if (used_variables == null)
			used_variables = variable.getVariables();
		int[] where = new int[3];
		
		if (updater != null){
			updater.setMinimum(0);
			updater.setMaximum(m);
			updater.update(0);
			}
		
		for (int i = 0; i < m; i++){
			where[0] = i;
			for (int j = 0; j < n; j++){
				where[1] = j;
				for (int k = 0; k < n; k++){
					where[3] = k;
					for (int v = 0; v < used_variables.size(); v++)
						jep.setVarValue(used_variables.get(v), variable.getVariableValue(used_variables.get(v), where));
					try{
						double d = jep.getValue();
						result[i][j][k] = d==1;
					}catch (ParseException ex){
						InterfaceSession.log("ParseException evaluating function: " + ex.getMessage(), LoggingType.Errors);
						return null;
						}
					}
				}
			if (updater != null){
				if (updater.isCancelled()){
					InterfaceSession.log("MathExpression evaluate cancelled by user.", LoggingType.Warnings);
					return null;
					}
				updater.update(i);
				}
			}
		
		return result;
	}
	
	static Object evaluateNb(JEP jep, VariableObject variable, int[] dims){
		
		return null;
	}
	
}