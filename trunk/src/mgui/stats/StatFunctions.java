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

package mgui.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.Utility;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import Jama.Matrix;

/**********************************************************************
 * Utility class providing general statistical functions.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class StatFunctions extends Utility {

	
	/*************************
	 * Return statistics describing a set of normally distributed values ordered as follows:
	 * 
	 * <ol>
	 * <li>mean</li>
	 * <li>sum</li>
	 * <li>standard deviation</li>
	 * <li>sum of squares</li>
	 * <li>minimum</li>
	 * <li>maximum</li>
	 * </ol>
	 * 
	 * @param data
	 * @return
	 */
	
	public static double[] getBasicNormalStats(ArrayList<MguiNumber> data){
		double sum = 0;
		double max = -Double.MAX_VALUE;
		double min = Double.MAX_VALUE;
		double v;
		
		for (int i = 0; i < data.size(); i++){
			v = data.get(i).getValue();
			sum += v;
			min = Math.min(v, min);
			max = Math.max(v, max);
			}
			
		double mean = sum / data.size();
		double sum_sq = 0;
		
		for (int i = 0; i < data.size(); i++)
			sum_sq += Math.pow((data.get(i).getValue() - mean), 2);
		
		double st_dev = Math.sqrt(sum_sq / (data.size() - 1));
		
		return new double[] {mean, sum, st_dev, sum_sq, min, max};
	}
	
	/*************************
	 * Returns the mode of a list of values, where equality is determined as 
	 * {@code abs(values[i] - values[j]) < default_precision}.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMode(ArrayList<MguiNumber> values){
		double[] array = new double[values.size()];
		for (int i = 0; i < values.size(); i++)
			array[i] = values.get(i).getValue();
		return getMode(array, GeometryFunctions.error);
	}
	
	/*************************
	 * Returns the mode of a list of values, where equality is determined as 
	 * {@code abs(values[i] - values[j]) < default_precision}.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMode(double[] values){
		return getMode(values, GeometryFunctions.error);
	}
	
	/*************************
	 * Returns the mode of a list of values, where equality is determined as 
	 * {@code abs(values[i] - values[j]) < precision}.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMode(double[] values, double precision){
		
		if (values.length < 1) return Double.NaN;
		
		ArrayList<Double> indices = new ArrayList<Double>(values.length);
		ArrayList<MguiInteger> freq = new ArrayList<MguiInteger>(values.length);
		
		indices.add(values[0]);
		freq.add(new MguiInteger(1));
		boolean add;
		
		for (int i = 0; i < values.length; i++){
			add = false;
			for (int j = 0; j < indices.size(); j++){
				if (GeometryFunctions.compareDouble(values[i], indices.get(j), precision) == 0){
					freq.get(j).add(1);
				}else{
					add = true;
					}
				}
			if (add){
				indices.add(values[i]);
				freq.add(new MguiInteger(1));
				}
			}
		
		// Return max frequency
		int max_idx = -1;
		int max_freq = -1;
		for (int i = 0; i < freq.size(); i++){
			int f = freq.get(i).getInt();
			if (f > max_freq){
				max_idx = i;
				max_freq = f;
				}
			}
		
		if (max_idx > -1) return indices.get(max_idx);
		
		return Double.NaN;
	}
	
	/*************************
	 * Returns the median of a list of values. If the list has an odd number of elements, returns
	 * the lowest-index median value.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMedian(ArrayList<MguiNumber> values){
		ArrayList<MguiNumber> copy = new ArrayList<MguiNumber>(values);
		Collections.sort(copy);
		return copy.get((int)Math.floor(copy.size()/2.0)).getValue();
	}
	
	/*************************
	 * Returns the median of a list of values. If the list has an odd number of elements, returns
	 * the lowest-index median value.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMedian(double[] values){
		
		double[] copy = new double[values.length];
		System.arraycopy(values, 0, copy, 0, values.length);
		Arrays.sort(copy);
		
		return copy[(int)Math.floor(copy.length/2.0)];
		
	}
	
	/*************************
	 * Return statistics describing a set of weighted normally distributed values ordered as follows
	 * (see http://en.wikipedia.org/wiki/Weighted_mean):
	 * 
	 * <ol>
	 * <li>mean</li>
	 * <li>sum</li>
	 * <li>standard deviation</li>
	 * <li>sum of squares</li>
	 * </ol>
	 * 
	 * @param data
	 * @param weights
	 * @return
	 */
	
	public static double[] getWeightedBasicNormalStats(ArrayList<MguiNumber> data, ArrayList<MguiNumber> weights){
		
		
		double weight_sum = 0;
		for (MguiNumber nbr : weights)
			weight_sum += nbr.getValue();
		
		ArrayList<MguiNumber> norm_weights = new ArrayList<MguiNumber>(weights);
		for (MguiNumber nbr : norm_weights)
			nbr.divide(weight_sum);
		
		double mean = 0, sum = 0;
		for (int i = 0; i < data.size(); i++){
			mean += data.get(i).getValue() * norm_weights.get(i).getValue();
			sum += data.get(i).getValue() * weights.get(i).getValue();
			}
			
		double sum_sq = 0;
		double st_dev = 0;
		
		for (int i = 0; i < data.size(); i++){
			double a = Math.pow((data.get(i).getValue() - mean), 2);
			sum_sq += a;
			st_dev += norm_weights.get(i).getValue() * a;
			}
		
		st_dev = Math.sqrt(st_dev);
		return new double[] {mean, sum, st_dev, sum_sq};
	}
	
	/*************************
	 * Return statistics describing a set of normally distributed values ordered as follows:
	 * 
	 * <ol>
	 * <li>mean</li>
	 * <li>sum</li>
	 * <li>standard deviation</li>
	 * <li>sum of squares</li>
	 * </ol>
	 * 
	 * @param data
	 * @return
	 */
	
	public static double[] getBasicNormalStatsDouble(ArrayList<Double> data){
		double sum = 0;
		
		for (int i = 0; i < data.size(); i++)
			sum += data.get(i);
			
		double mean = sum / data.size();
		double sum_sq = 0;
		
		for (int i = 0; i < data.size(); i++)
			sum_sq += Math.pow((data.get(i) - mean), 2);
		
		double st_dev = Math.sqrt(sum_sq / (data.size() - 1));
		
		return new double[] {mean, sum, st_dev, sum_sq};
	}
	
	public static double getPearsonCoefficient(ArrayList<Double> list1, ArrayList<Double> list2) throws Exception{
		if (list1== null || list2 == null)
			throw new Exception("StatFunctions.getPearsonCoefficient: one or both lists are null.");
		if (list1.size() != list2.size())
			throw new Exception("StatFunctions.getPearsonCoefficient: list1 must be the same size as list2.");
		
		SimpleRegression sr = getRegression(list1, list2);
		
		return sr.getR();
	}
	
	/****************************************************************
	 * Returns a {@link SimpleRegression} object for the two lists of values.
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static SimpleRegression getRegression(ArrayList<Double> list1, ArrayList<Double> list2){
		
		SimpleRegression sr = new SimpleRegression();
		
		for (int i = 0; i < list1.size(); i++)
			sr.addData(list1.get(i), list2.get(i));
		
		return sr;
	}
	
	/**************************************
	 * Returns the normalized probability of <code>x</code> given the normal probability
	 * distribution defined by <code>mu</code> (mean) and <code>sigma</code> (st dev).
	 * 
	 * Formula for Gaussian:
	 * 1/ (sigma *sqrt(2*Pi)) * exp -((x - mu)^2 / (2 * sigma)^2) 
	 * 
	 * @param x value to evaluate
	 * @param mu mean of distribution
	 * @param sigma standard deviation of distribution
	 * @return
	 */
	public static double getGaussian(double x, double mu, double sigma){
		
		double a = 1.0 / (sigma * Math.sqrt(2 * Math.PI));
		double b = Math.pow(x - mu, 2);
		double c = 2 * Math.pow(sigma, 2);
		return a * Math.exp(-b / c);
		
		//return (1.0 / (sigma * Math.sqrt(2 * Math.PI))) * 
		//	   Math.exp(-(Math.pow(x - mu, 2) / (2 * Math.pow(sigma, 2))));
	
	}
	
	/**************************************
	 * Returns the normalized probability of <code>x</code> given the normal probability
	 * distribution defined by <code>mu</code> (mean) and <code>sigma</code> (st dev).
	 * Modified to ensure that the return value for x = mu is always 1.0.
	 * 
	 * Formula for normalized Gaussian:
	 * exp -((x - mu)^2 / (2 * sigma)^2) 
	 * 
	 * @param x value to evaluate
	 * @param mu mean of distribution
	 * @param sigma standard deviation of distribution
	 * @return
	 */
	public static double getGaussian2(double x, double mu, double sigma){
		
		//double a = 1.0 / (sigma * Math.sqrt(2 * Math.PI));
		double b = Math.pow(x - mu, 2);
		double c = 2 * Math.pow(sigma, 2);
		return Math.exp(-b / c);
		
		//return (1.0 / (sigma * Math.sqrt(2 * Math.PI))) * 
		//	   Math.exp(-(Math.pow(x - mu, 2) / (2 * Math.pow(sigma, 2))));
	
	}
	
	
	/*************************
	 * Constructs a histogram from the list <code>d</code>, comprised of <code>bins</code> bins. 
	 * 
	 * @status experimental
	 * @param d list of values
	 * @param bins number of bins in histogram
	 * @return list of size <code>bins</code>, representing the frequency of each interval
	 * occurring between the minimum and maximum values of d
	 */
	public static Histogram getHistogram(ArrayList<MguiNumber> d, int bins){
		Collections.sort(d);
		double min = d.get(0).getValue();
		double max = d.get(d.size() - 1).getValue();
		return getHistogram(d, bins, min, max, true);
	}
	
	/*************************
	 * Constructs a histogram from the list <code>d</code>, comprised of <code>bins</code> bins. The data array <code>d</code>
	 * will be sorted.
	 * 
	 * @status experimental
	 * @param d list of values
	 * @param bins number of bins in histogram
	 * @param min
	 * @param max
	 * @return list of size <code>bins</code>, representing the frequency of each interval
	 * occurring between the minimum and maximum values of d
	 */
	public static Histogram getHistogram(ArrayList<MguiNumber> d, int bins, double min, double max){
		return getHistogram(d, bins, min, max, false);
	}
	
	/*************************
	 * Constructs a histogram from the list <code>d</code>, comprised of <code>bins</code> bins.
	 * 
	 * @status experimental
	 * @param d list of values
	 * @param bins number of bins in histogram
	 * @param min
	 * @param max
	 * @param sorted 	Indicates whether the array <code>d</code> is presorted. If not, it will be sorted.
	 * @return list of size <code>bins</code>, representing the frequency of each interval
	 * occurring between the minimum and maximum values of d
	 */
	public static Histogram getHistogram(ArrayList<MguiNumber> values, int bins, double min, double max, boolean sorted){
		ArrayList<MguiNumber> d = new ArrayList<MguiNumber>(values);
		if (!sorted){
			Collections.sort(d);
			}
		
		double bin_size = (max - min) / bins;
		Histogram hist = new Histogram();
		
		int i = 1;
		int j = 0;
		for (i = 1; i <= bins && j < d.size(); i++){
			Histogram.Bin bin = hist.new Bin(min + (bin_size * i) - (bin_size / 2.0), 0);
			while (j < d.size() && d.get(j).getValue() < min + bin_size * i){
				bin.y++;
				j++;
				}
			hist.bins.add(bin);
			hist.dataN += bin.y;
			}
		
		for (; i <= bins; i++){
			Histogram.Bin bin = hist.new Bin(min + (bin_size * i) - (bin_size / 2.0), 0);
			hist.bins.add(bin);
			}
		
		hist.setLimits();
		return hist;
	}
	
	/*****************************************************
	 * Generates a bootstrap sample of <code>matrix</code> by randomly sampling from it
	 * <code>N</code> times, with the possibility of sampling the same observation multiple
	 * times.
	 * 
	 * @param matrix Matrix of observations X measurements
	 * @param N Number of samples in result
	 * @return Sampled matrix 
	 */
	public static Matrix getBootstrapSample(Matrix matrix, int N){
		
		int rows = matrix.getRowDimension();
		int cols = matrix.getColumnDimension();
		
		Matrix result = new Matrix(N, cols);
		
		for (int i = 0; i < N; i++){
			int s = (int)Math.round(Math.random() * rows);	//random sample
			for (int j = 0; j < cols; j++)
				result.set(i, j, matrix.get(s, j));			//fill row
			}
		
		return result;
	}
	
	
	/****************************************************
	 * Returns p-values corrected for family-wise error, using the Holm-Bonferroni method. This method
	 * sorts the values from lowest to highest, applies correction factor n to the first, and decrements n
	 * for each subsequent factor.
	 * 
	 * @param p_values
	 * @return
	 */
	public static double[] getHolmBonferroniCorrected(ArrayList<MguiNumber> p_values){
		
		HashMap<Integer, MguiNumber> map = new HashMap<Integer, MguiNumber>();
		ArrayList<MguiNumber> copy = new ArrayList<MguiNumber>();
		
		
		for (int i = 0; i < p_values.size(); i++){
			copy.add(p_values.get(i));
			map.put(i, p_values.get(i));
			}
		
		Collections.sort(copy);
		int n = copy.size();
		
		for (int i = 0; i < copy.size(); i++){
			copy.get(i).setValue(copy.get(i).getValue() * n);
			n--;
			}
		
		double[] result = new double[copy.size()];
		for (int i = 0; i < copy.size(); i++){
			result[i] = map.get(i).getValue();
			}
		
		return result;
		
	}
	
	/*********************************************
	 * Returns the root mean squared error (RMSE) for two lists of values. Calculated as:
	 * 
	 * <p><code>SQRT (sum( (v1_i - v2_i)^2 ) / n)</code> across all values i in list1 and list2 
	 * 
	 * </p>The lists must be of the same length.
	 * 
	 * @param list1
	 * @param list1
	 * @return
	 */
	public static double getRootMeanSquaredError(ArrayList<MguiNumber> list1, ArrayList<MguiNumber> list2){
		
		double sum_squares = 0;
		
		for (int i = 0; i < list1.size(); i++)
			sum_squares += Math.pow(list1.get(i).getValue() - list2.get(i).getValue(), 2.0);
		
		return Math.sqrt(sum_squares / list1.size());
		
	}
	
	class IndexedPValue implements Comparable<IndexedPValue> {
		
		public double p_value;
		public int index;
		
		public IndexedPValue(int index, double value){
			this.index = index;
			this.p_value = value;
			}
		
		@Override
		public int compareTo(IndexedPValue p) {
			if (p_value < p.p_value) return -1;
			if (p_value > p.p_value) return 1;
			return 0;
		}

		
	}
	
	protected static final double fwhm_factor = 2.0 * Math.sqrt(2.0 * Math.log(2));
	
	/****************************************************
	 * Converts full-width at half max to sigma (standard deviations)
	 * 
	 * @param fwhm
	 * @return
	 */
	public static double getFWHMtoSigma(double fwhm){
		return fwhm / fwhm_factor;
	}
	
	/****************************************************
	 * Converts sigma (standard deviations) to full-width at half max (FWHM)
	 * 
	 * @param fwhm
	 * @return
	 */
	public static double getSigmaToFWHM(double sigma){
		return fwhm_factor * sigma;
	}
	
	/***********************************
	 * Computes the frequency of each unique integer in the list. If the list is not comprised of integer values,
	 * values will be rounded. Returns a hash map of the integers to frequencies
	 * 
	 * @param values
	 * @return
	 */
	public static HashMap<Integer,Integer> getIntegerFrequencies(ArrayList<MguiNumber> values){
		
		HashMap<Integer,Integer> frequencies = new HashMap<Integer,Integer>();
		
		for (int i = 0; i < values.size(); i++){
			int value = (int)Math.round(values.get(i).getValue());
			if (!frequencies.containsKey(value))
				frequencies.put(value, 1);
			else
				frequencies.put(value, frequencies.get(value) + 1);
			}
		
		return frequencies;
		
	}
	
	
}