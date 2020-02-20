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

package mgui.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import mgui.interfaces.InterfaceSession;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;

/*****************************************************
 * Histogram representation of a dataset.
 * 
 * @author typically
 * @version 1.0
 * @since 1.0
 *
 */

public class Histogram {

	public ArrayList<Bin> bins;
	public double dataMax, dataMin, dataN=0;
	public double maxY, maxX, minY, minX;
	public double bin_size;
	public float ignore_large_bin = 0.7f;  // Percentage above which to ignore a bin
											// when computing max Y
	
	public Histogram(){
		bins = new ArrayList<Bin>();
	}
	
	public Histogram(ArrayList<MguiNumber> x, ArrayList<MguiNumber> y, double dataMax, double dataMin){
		bins = new ArrayList<Bin>();
		this.dataMin = dataMin;
		this.dataMax = dataMax;
		setValues(x, y);
	}
	
	public Histogram(ArrayList<MguiNumber> d, int bins, double min, double max){
		this.bins = new ArrayList<Bin>(bins);
		this.set(d, bins, min, max);
	}
	
	public double getNormalized(double x){
		if (dataMax != dataMin)
			return (x - minX) / (maxX - minX);
		return x;
	}
	
	public void setLimits(){
		Collections.sort(bins);
		minX = bins.get(0).x;
		maxX = bins.get(bins.size() - 1).x;
		maxY = -Double.MAX_VALUE;
		minY = Double.MAX_VALUE;
		double temp=0;
		for (int i = 0; i < bins.size(); i++){
			temp = Math.max(bins.get(i).y, maxY);
			if (ignore_large_bin <= 0 || temp / dataN < ignore_large_bin){
				maxY = temp;
				}
			minY = Math.min(bins.get(i).y, minY);
			}
		if (maxY == -Double.MAX_VALUE)
			maxY=temp;
		bin_size = (maxX - minX) / bins.size(); 
			
	}
	
	/*********************************************
	 * Returns the value corresponding to the specified percentile
	 * 
	 * @param pct Value from 0 to 100
	 * @return
	 */
	public double getPercentileValue(double pct){
		
		pct /= 100;
		if (pct < 0) pct = 0;
		if (pct > 1) pct = 1;
		double total = 0;
		for (int i = 0; i < bins.size(); i++)
			total += bins.get(i).y;
		
		double cum = 0;
		for (int i = 0; i < bins.size(); i++){
			double p = cum / total;
			if (p >= pct) return bins.get(i).x;
			cum += bins.get(i).y;
		}
		
		return bins.get(bins.size() - 1).x;
		
	}
	
	public void toFile(File file){
		
		try{
			
			if (!file.exists() && !file.createNewFile()){
				InterfaceSession.log("Histogram: cannot create file '" + file.getAbsolutePath() + "'.");
				return;
				}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			bw.write("Bin\tX\tY\n");
			
			for (int i = 0; i < bins.size(); i++)
				bw.write(i + "\t" + bins.get(i).x + "\t" + bins.get(i).y + "\n");
				
			bw.close();
		
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public boolean setValues(ArrayList<MguiNumber> x, ArrayList<MguiNumber> y){
		if (x.size() != y.size()) return false;
		dataN = 0;
		for (int i = 0; i < x.size(); i++){
			bins.add(new Bin(x.get(i).getValue(), y.get(i).getValue()));
			dataN += y.get(i).getValue();
			}
		setLimits();
		return true;
	}
	
	public void set(int bins, double min, double max){
		this.bins = new ArrayList<Bin>();
		bin_size = (max - min) / bins;
		maxX = max;
		minX = min;
		
		for (int i = 0; i < bins + 1; i++){
			double x_mid = min + (bin_size * i) + (bin_size / 2.0);
			Bin bin = new Bin(x_mid, 0);
			this.bins.add(bin);
			}
		
		setLimits();
	}
	
	public void addValue(double val){
		
		if (val < minX){
			bins.get(0).y++;
			return;
			}
		if (val > maxX){
			bins.get(bins.size() - 1).y++;
			return;
			}
		//int b = (int) Math.round(((val - minX) / (maxX - minX)) * bin_size);
		int b = (int)Math.floor((val - minX) / (maxX - minX) * bins.size());
		b = Math.min(bins.size() - 1, b);
		b = Math.max(0, b);
		bins.get(b).y++;
		maxY = Math.max(bins.get(b).y, maxY);
		minY = Math.min(bins.get(b).y, maxY);
		dataN++;
	}
	
	public void set(ArrayList<MguiNumber> d, int bins, double min, double max){
		
		bin_size = (max - min) / bins;
		ArrayList<MguiNumber> sorted = new ArrayList<MguiNumber>(d);
		Collections.sort(sorted);
		dataMin = sorted.get(0).getValue();
		dataMax = sorted.get(d.size()-1).getValue();
		dataN = sorted.size();
		
		int j = 0;
		for (int i = 0; i < bins + 1; i++){
			double x_mid = min + (bin_size * i) + (bin_size / 2.0);
			Bin bin = new Bin(x_mid, 0);
			this.bins.add(bin);
			while (j < sorted.size() && sorted.get(j).getValue() < x_mid + (bin_size / 2.0)){
				bin.y ++;
				j++;
				}
			}
		
		setLimits();
	}
	
	@Override
	public String toString(){
		String b = "";
		if (bins == null)
			b = "null";
		else b = "" + bins.size();
		String s = "Histogram [bins=" + b + ", x_min=" + minX +  
											", x_max=" + maxX +
											", y_min=" + minY +
											", y_max=" + maxY + 
											", bin size=" + bin_size +
											"]";
		return s;
	}
	
	public class Bin implements Comparable<Bin>{
		
		public double x, y;
		
		public Bin(double x, double y){
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int compareTo(Bin bin) {
			
			return new MguiDouble(x).compareTo(bin.x);
			
		}

	}
	
}