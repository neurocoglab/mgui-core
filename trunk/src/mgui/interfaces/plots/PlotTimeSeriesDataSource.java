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

package mgui.interfaces.plots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mgui.interfaces.io.DataSourceEvent;
import mgui.interfaces.io.DataSourceListener;
import mgui.interfaces.io.InterfaceDataSourceTimeSeries;
import mgui.interfaces.io.NamedDataSource;
import mgui.interfaces.maps.NameMap;
import mgui.numbers.MguiNumber;

/************************************************************
 * A plot data source for XY data, with multiple named Y channels. Can be used as an
 * updatable time series data source.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T> Number type for this data source
 */
public class PlotTimeSeriesDataSource<T extends MguiNumber> extends PlotXYDataSource<T>
															implements InterfaceDataSourceTimeSeries<T>,
											   			   	   		   NamedDataSource {

	
	protected HashMap<String, List<T>> y = new HashMap<String, List<T>>();
	protected List<T> x;
	protected HashMap<String,List<T>> y_values = new HashMap<String,List<T>>();
	protected ArrayList<T> x_value;
	
	//protected NameMap names = new NameMap();
		
	public PlotTimeSeriesDataSource(){
		
	}
	
	public PlotTimeSeriesDataSource(int size, int var_count){
		//this.size = size;
		this.var_count = var_count;
	}
	
	/*******************************************
	 * Copies the X data series to this data source.
	 * 
	 * @param x
	 */
	public void setX(ArrayList<T> x){
		//deep copy
		x_value = new ArrayList<T>(x.size());
		for (int i = 0; i < x.size(); i++)
			if (i < x.size())
				x_value.add((T)x.get(i).clone());
		fireReset();
		fireEmission(x.size());
	}
	
	/*******************************************
	 * Copies the complete Y data series to this data source.
	 * 
	 * @param y
	 */
	public void setY(List<String> names, ArrayList<ArrayList<T>> y){
		//deep copy
		if (this.x_value == null) return;
		//y_values = new ArrayList<ArrayList<T>>(y.size());
		y_values.clear();
		for (int v = 0; v < var_count; v++){
			ArrayList<T> data = new ArrayList<T>();
			for (int i = 0; i < x_value.size(); i++){
				if (v < y.size())
					data.add((T)y.get(v).get(i).clone());
				}
			y_values.put(names.get(v), data);
			}
		fireReset();
		fireEmission(x_value.size());
	}
	
	/*****************************************
	 * Sets the full set of Y data for the channel named <code>name</code>.
	 * 
	 * @param name
	 * @param data
	 * @throws IOException
	 */
	public void setY(String name, ArrayList<T> data) throws IOException{
		if (!y_values.containsKey(name))
			throw new IOException("Invalid name.");
		y_values.put(name, data);
	}
	
	@Override
	public List<List<T>> getSourceSignalY() {
		ArrayList<String> names = getChannelNames();
		List<List<T>> signals = new ArrayList<List<T>>();
		
		for (int i = 0; i < names.size(); i++){
			signals.add((ArrayList<T>)getSourceSignalY(i));
			}
		
		return signals;
	}
	
	@Override
	public List<T> getSourceSignalX(int i) {
		return getSourceSignalX();
	}
	
	@Override
	public List<T> getSourceSignalY(int i) {
		return getSourceSignalY(this.getChannelNames().get(i));
	}
	
	public List<T> getSourceSignalY(String channel) {
		return new ArrayList<T>(y.get(channel));
	}

	@Override
	public byte[] getSourceSignalAsBytes() {
		return null;
	}
	
	@Override
	public void setChannelNames(List<String> names){
		y_values.clear();
		for (int i = 0; i < names.size(); i++)
			y_values.put(names.get(i), new ArrayList<T>());
	}
	
	@Override
	public int getChannelCount() {
		return y_values.size();
	}

	@Override
	public List<T> getSourceSignalX() {
		return new ArrayList<T>(x);
	}

	@Override
	public List<T> getSourceSignal() {
		
		return null;
	}

	@Override
	public ArrayList<String> getChannelNames(){
		return new ArrayList<String>(y_values.keySet());
	}
	
	/*******************************************
	 * Returns the full set of Y data for the channel named <code>name</code>.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public List<T> getY(String name) throws IOException{
		//Integer i = names.get(name);
		if (!y_values.containsKey(name))
			throw new IOException("Invalid name.");
		return y_values.get(name);
	}
	
	@Override
	protected void fireEmission(int size){
		if (batch){
			emission = true;
			return;
			}
		
		int x_size = x_value.size() - cursor;
		size = Math.min(x_size, size);
		
		if (size <= 0) return;			//beyond size of data; reset required
		
		DataSourceEvent event = new DataSourceEvent(this, size);
		ArrayList<String> names = this.getChannelNames();
		//fire data source listeners for all variables
		
			x = new ArrayList<T>(x_value.subList(cursor, size));
			
			y.clear();
			//for (int k = 0; k < y_values.size(); k++)
				for (int n = 0; n < names.size(); n++){
					String channel = names.get(n);
					List<T> list = y_values.get(channel);
					y.put(channel, new ArrayList<T>(list.subList(cursor, size)));	
					}
			
		for (int i = 0; i < sourceListeners.size(); i++){
			sourceListeners.get(i).dataSourceEmission(event);
			}
		
		cursor += size;
		emission = false;
	}
	
	
}