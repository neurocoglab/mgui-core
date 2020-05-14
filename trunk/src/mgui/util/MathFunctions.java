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

package mgui.util;

import org.jogamp.vecmath.Point3d;

import Jama.Matrix;

public class MathFunctions {

	//interpolate
	public static double interpolate(double v1, double v2, double val){
		if (val < 0 || val > 1) return Double.NaN;
		
		double delta = v2 - v1;
		double c = v1 + val * delta;
		
		return c;
	}
	
	/**************************
	 * Normalize <code>val</code> to the range [<code>min</code>:<code>max</code>].
	 * If val is outside the range [min:max], it is set to the nearest limit (0 or 1).
	 * @param min
	 * @param max
	 * @param val
	 * @return normalized value
	 */
	public static double normalize(double min, double max, double val){
		//if (min > max || val < min || val > max) return Double.NaN;
		double delta1 = max - min;
		double delta2 = val - min;
		delta1 = delta2 / delta1;
		delta1 = Math.min(delta1, 1.0);
		delta1 = Math.max(delta1, 0.0);
		return delta1;
	}
	
	/**************************
	 * Unnormalize <code>val</code> to the range [<code>min</code>:<code>max</code>].
	 * @param min
	 * @param max
	 * @param val
	 * @return normalized value
	 */  
	public static double unnormalize(double min, double max, double val){
		//if (min > max || val < min || val > max) return Double.NaN;
		double delta1 = max - min;
		//double delta2 = val - min;
		//double delta2 = val * delta1;
		return min + (val * delta1);
	}
	
	/*************************
	 * Rounds m up to the next multiple of n
	 * @param m
	 * @param n
	 * @return
	 */
	public static double ceil(double m, double n){
		double d = m / n;
		d = Math.ceil(d);
		return n * d;
	}
	
	public static Point3d getPoint3dFromMatrix(Matrix m){
		
		double[] vals = m.getRowPackedCopy();
		Point3d p = new Point3d(vals[0], vals[1], vals[2]);
		return p;
	}
	
	public static Matrix getMatrixFromPoint3d(Point3d p){
		
		return new Matrix(new double[]{p.x, p.y, p.z, 1}, 4);
	}
	
	/**********************************************
	 * Computes log to base {@code n} of {@code a}.
	 * 
	 * @param a
	 * @param n
	 * @return
	 */
	public static double logn( double a, double n ){
		return Math.log(a) / Math.log(n);
	}
	
}