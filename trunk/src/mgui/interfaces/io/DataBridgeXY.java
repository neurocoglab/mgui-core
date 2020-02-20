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

package mgui.interfaces.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mgui.interfaces.InterfaceSession;
import mgui.numbers.MguiNumber;

/*************************************************************
 * Acts as a data bridge for XY data, where X holds a single value and each Y
 * channel has an array of values corresponding to that X.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T>
 */
public class DataBridgeXY<T extends MguiNumber> extends DataBridge<T>
										 implements DataInputStreamXY<T>, 
										 			DataOutputStreamXY<T> {

	//public double x;
	protected List<T> x;
	protected List<List<T>> y;
	private boolean is_init = false; 
	
	public DataBridgeXY(int size){
		this(size, null);
	}
	
	public DataBridgeXY(int size, T initial_value){
		y = new ArrayList<List<T>>();
	
		if (initial_value != null){
			for (int i = 0; i < size; i++){
				ArrayList<T> list = new ArrayList<T>();
				Collections.fill(list, initial_value);
				y.add(list);
				}
			is_init = true;
			
			}
				
		x = new ArrayList<T>();
		Collections.fill(x, initial_value);
	}
	
	@Override
	public List<T> getYData(int i) throws IOException {
		if (y == null || i >= y.size() || i < 0) 
			throw new IOException("Y data not set or index is invalid.");
		return y.get(i); // sampleInputStream();
	}
	
	@Override
	public List<List<T>> getYData() throws IOException {
		if (y == null) 
			throw new IOException("Y data not set.");
		return y;
	}

	public void setXData(List<T> x) throws IOException {
		this.x = x;
	}
	
	@Override
	public void setYData(List<List<T>> data) throws IOException{
		if (y == null) 
			throw new IOException("Y data not set.");
		
		y.clear();
		
		for (int i = 0; i < data.size(); i++){
			y.add(new ArrayList<T>(data.get(i)));
			}
		
		is_init = true;
	}
	
	@Override
	public void setYData(int i, List<T> data) throws IOException{
		if (y == null || i >= y.size() || i < 0) 
			throw new IOException("Y data not set or index is invalid.");
		
		y.set(i, data);
	}
	
	@Override
	public void dataSourceEmission(DataSourceEvent event) {
		if (!(event.getDataSource() instanceof InterfaceDataSourceTimeSeries)) super.dataSourceEmission(event);
		
		InterfaceDataSourceTimeSeries<T> source_xy = (InterfaceDataSourceTimeSeries<T>)event.getDataSource();
		
		try{
			setYData(source_xy.getSourceSignalY());
			setXData(source_xy.getSourceSignalX());
		}catch (IOException e){
			InterfaceSession.log("DataBridgeXY: IOException: " + e.getLocalizedMessage());
			if (InterfaceSession.showErrorsOnConsole())
				e.printStackTrace();
			}
		
		fireListeners();
	}
	
	@Override
	public void dataSourceReset(DataSourceEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<T> getData() throws IOException{
		if (!is_init)
			throw new IOException("Y data not set.");
		
		ArrayList<T> data = new ArrayList<T>();
		int size = this.getChannelCount();
		for (int i = 0; i < size; i++)
			data.addAll(this.getYData(i));
		
		return data;
	}

	@Override
	public byte[] getDataAsBytes() throws IOException {
		// TODO implement
		return null;
	}

	@Override
	public void setData(List<T> data) throws IOException {
		if (y == null)
			throw new IOException("Y array not set.");
		
		int c_count = this.getChannelCount();
		int size = (int)((float)data.size() / (float)c_count);
		
		// Set y data from concatenated array
		//if (data instanceof ArrayList)
		//	y = (ArrayList<T>)data;
		//else
		//	y = new ArrayList<T>(data);
		
		y.clear();
		for (int i = 0; i < c_count; i++){
			int start = i * size;
			ArrayList<T> list = new ArrayList<T>();
				list.addAll(data.subList(start, start + size));
			}
			
		is_init = true;
	}

	@Override
	public void setDataAsBytes(byte[] d) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getChannelCount() {
		if (y == null) return -1;
		return y.size();
	}

	@Override
	public List<T> getXData() throws IOException {
		return x;
	}

}