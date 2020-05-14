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

package mgui.geometry.volume;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;

import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolator;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.util.MathArrays;

/**************************************************************
 * Extends {@linkplain TricubicSplineInterpolator} to allow a {@code ProgressUpdater}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class TricubicSplineInterpolaterUpdater extends TricubicSplineInterpolator {

	public TricubicSplineInterpolatingFunction interpolate(final double[] xval,
												            final double[] yval,
												            final double[] zval,
												            final double[][][] fval,
												            final ProgressUpdater progress)
            						throws NoDataException, NumberIsTooSmallException,
            							   DimensionMismatchException, NonMonotonicSequenceException {
	
		if (xval.length == 0 || yval.length == 0 || zval.length == 0 || fval.length == 0) {
		throw new NoDataException();
		}
	if (xval.length != fval.length) {
		throw new DimensionMismatchException(xval.length, fval.length);
		}
	
	
	
	MathArrays.checkOrder(xval);
	MathArrays.checkOrder(yval);
	MathArrays.checkOrder(zval);
	
	final int xLen = xval.length;
	final int yLen = yval.length;
	final int zLen = zval.length;
	
	if (progress != null){
		progress.setMinimum(0);
		progress.setMaximum(4 * xLen + 2 * yLen + 2 * zLen);
		progress.setMessage("Computing bicubic splines:");
		}
	
	int itr = 0;
	
	// Samples, re-ordered as (z, x, y) and (y, z, x) tuplets
	// fvalXY[k][i][j] = f(xval[i], yval[j], zval[k])
	// fvalZX[j][k][i] = f(xval[i], yval[j], zval[k])
	final double[][][] fvalXY = new double[zLen][xLen][yLen];
	final double[][][] fvalZX = new double[yLen][zLen][xLen];
	for (int i = 0; i < xLen; i++) {
		if (fval[i].length != yLen) {
			throw new DimensionMismatchException(fval[i].length, yLen);
		}
	
		for (int j = 0; j < yLen; j++) {
			if (fval[i][j].length != zLen) {
				throw new DimensionMismatchException(fval[i][j].length, zLen);
				}
			
			for (int k = 0; k < zLen; k++) {
				final double v = fval[i][j][k];
				fvalXY[k][i][j] = v;
				fvalZX[j][k][i] = v;
				
				}
			}
		if (progress != null){
			if (progress.isCancelled()){
				InterfaceSession.log("TricubicSpline interpolation cancelled by user.");
				return null;
				}
			progress.update(itr++);
			}
		}
	
	final BicubicSplineInterpolator bsi = new BicubicSplineInterpolator();
	
	// For each line x[i] (0 <= i < xLen), construct a 2D spline in y and z
	final BicubicSplineInterpolatingFunction[] xSplineYZ
					= new BicubicSplineInterpolatingFunction[xLen];
	for (int i = 0; i < xLen; i++) {
		xSplineYZ[i] = bsi.interpolate(yval, zval, fval[i]);
		if (progress != null){
			if (progress.isCancelled()){
				InterfaceSession.log("TricubicSpline interpolation cancelled by user.");
				return null;
				}
			progress.update(itr++);
			}
		}
	
	// For each line y[j] (0 <= j < yLen), construct a 2D spline in z and x
	final BicubicSplineInterpolatingFunction[] ySplineZX
					= new BicubicSplineInterpolatingFunction[yLen];
	for (int j = 0; j < yLen; j++) {
		ySplineZX[j] = bsi.interpolate(zval, xval, fvalZX[j]);
		if (progress != null){
			if (progress.isCancelled()){
				InterfaceSession.log("TricubicSpline interpolation cancelled by user.");
				return null;
				}
			progress.update(itr++);
			}
		}
	
	// For each line z[k] (0 <= k < zLen), construct a 2D spline in x and y
	final BicubicSplineInterpolatingFunction[] zSplineXY
					= new BicubicSplineInterpolatingFunction[zLen];
	for (int k = 0; k < zLen; k++) {
		zSplineXY[k] = bsi.interpolate(xval, yval, fvalXY[k]);
		if (progress != null){
			if (progress.isCancelled()){
				InterfaceSession.log("TricubicSpline interpolation cancelled by user.");
				return null;
				}
			progress.update(itr++);
			}
		}
	
	if (progress != null){
		progress.setMessage("Computing derivatives:");
		}
	
	// Partial derivatives wrt x and wrt y
	final double[][][] dFdX = new double[xLen][yLen][zLen];
	final double[][][] dFdY = new double[xLen][yLen][zLen];
	final double[][][] d2FdXdY = new double[xLen][yLen][zLen];
	for (int k = 0; k < zLen; k++) {
		final BicubicSplineInterpolatingFunction f = zSplineXY[k];
		for (int i = 0; i < xLen; i++) {
			final double x = xval[i];
			for (int j = 0; j < yLen; j++) {
				final double y = yval[j];
				dFdX[i][j][k] = f.partialDerivativeX(x, y);
				dFdY[i][j][k] = f.partialDerivativeY(x, y);
				d2FdXdY[i][j][k] = f.partialDerivativeXY(x, y);
				}
			}
		if (progress != null){
			if (progress.isCancelled()){
				InterfaceSession.log("TricubicSpline interpolation cancelled by user.");
				return null;
				}
			progress.update(itr++);
			}
		}
	
	// Partial derivatives wrt y and wrt z
	final double[][][] dFdZ = new double[xLen][yLen][zLen];
	final double[][][] d2FdYdZ = new double[xLen][yLen][zLen];
	for (int i = 0; i < xLen; i++) {
		final BicubicSplineInterpolatingFunction f = xSplineYZ[i];
		for (int j = 0; j < yLen; j++) {
			final double y = yval[j];
			for (int k = 0; k < zLen; k++) {
				final double z = zval[k];
				dFdZ[i][j][k] = f.partialDerivativeY(y, z);
				d2FdYdZ[i][j][k] = f.partialDerivativeXY(y, z);
				}
			}
		if (progress != null){
			if (progress.isCancelled()){
				InterfaceSession.log("TricubicSpline interpolation cancelled by user.");
				return null;
				}
			progress.update(itr++);
			}
		}
	
	// Partial derivatives wrt x and wrt z
	final double[][][] d2FdZdX = new double[xLen][yLen][zLen];
	for (int j = 0; j < yLen; j++) {
		final BicubicSplineInterpolatingFunction f = ySplineZX[j];
			for (int k = 0; k < zLen; k++) {
			final double z = zval[k];
			for (int i = 0; i < xLen; i++) {
				final double x = xval[i];
				d2FdZdX[i][j][k] = f.partialDerivativeXY(z, x);
				}
			}
		if (progress != null){
			if (progress.isCancelled()){
				InterfaceSession.log("TricubicSpline interpolation cancelled by user.");
				return null;
				}
			progress.update(itr++);
			}
		}
	
	// Third partial cross-derivatives
	final double[][][] d3FdXdYdZ = new double[xLen][yLen][zLen];
	for (int i = 0; i < xLen ; i++) {
		final int nI = nextIndex(i, xLen);
		final int pI = previousIndex(i);
		for (int j = 0; j < yLen; j++) {
			final int nJ = nextIndex(j, yLen);
			final int pJ = previousIndex(j);
			for (int k = 0; k < zLen; k++) {
				final int nK = nextIndex(k, zLen);
				final int pK = previousIndex(k);
				
				// XXX Not sure about this formula
				d3FdXdYdZ[i][j][k] = (fval[nI][nJ][nK] - fval[nI][pJ][nK] -
				fval[pI][nJ][nK] + fval[pI][pJ][nK] -
				fval[nI][nJ][pK] + fval[nI][pJ][pK] +
				fval[pI][nJ][pK] - fval[pI][pJ][pK]) /
				((xval[nI] - xval[pI]) * (yval[nJ] - yval[pJ]) * (zval[nK] - zval[pK])) ;
				}
			}
		if (progress != null){
			if (progress.isCancelled()){
				InterfaceSession.log("TricubicSpline interpolation cancelled by user.");
				return null;
				}
			progress.update(itr++);
			}
		}
	
	if (progress != null){
		progress.setMessage("Creating splines:");
		}
	
	// Create the interpolating splines
	return new TricubicSplineInterpolatingFunction(xval, yval, zval, fval,
											        dFdX, dFdY, dFdZ,
											        d2FdXdY, d2FdZdX, d2FdYdZ,
											        d3FdXdYdZ);
	}
	
	 /**
     * Compute the next index of an array, clipping if necessary.
     * It is assumed (but not checked) that {@code i} is larger than or equal to 0}.
     *
     * @param i Index
     * @param max Upper limit of the array
     * @return the next index
     */
    protected int nextIndex(int i, int max) {
        final int index = i + 1;
        return index < max ? index : index - 1;
    }
    /**
     * Compute the previous index of an array, clipping if necessary.
     * It is assumed (but not checked) that {@code i} is smaller than the size of the array.
     *
     * @param i Index
     * @return the previous index
     */
    protected int previousIndex(int i) {
        final int index = i - 1;
        return index >= 0 ? index : 0;
    }
	
}