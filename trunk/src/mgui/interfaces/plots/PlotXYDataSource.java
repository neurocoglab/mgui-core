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

package mgui.interfaces.plots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import mgui.interfaces.io.DataSourceEvent;
import mgui.interfaces.io.DataSourceListener;
import mgui.interfaces.io.InterfaceDataSourceXY;
import mgui.interfaces.io.NamedDataSource;
import mgui.numbers.MguiNumber;

/*****************************************************************
 * Plot data source for X-Y data sets.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PlotXYDataSource<T extends MguiNumber> implements InterfaceDataSourceXY<T>,
															   NamedDataSource{

	protected boolean batch = false;
	protected boolean emission = false, reset = false;
	protected ArrayList<DataSourceListener> sourceListeners = new ArrayList<DataSourceListener>();
	protected HashMap<String, List<XYData<T>>> xy_data = new HashMap<String, List<XYData<T>>>();
	protected HashMap<String, List<XYData<T>>> xy = new HashMap<String, List<XYData<T>>>();
	protected int cursor = 0;
	protected int var_count = 0;
	
	public PlotXYDataSource(){
		
	}
	
	public PlotXYDataSource(int var_count){
		//this.size = size;
		this.var_count = var_count;
	}
	
	//public int getSize(){
	//	return size;
	//}
	
	@Override
	public List<T> getSourceSignal() {
		// TODO implement (return concatenated list of all values)
		return null;
	}

	@Override
	public byte[] getSourceSignalAsBytes() {
		// TODO implement
		return null;
	}

	public int getChannelSize(String channel){
		List<XYData<T>> list = xy_data.get(channel);
		if (list == null) return -1;
		return list.size();
	}
	
	@Override
	public List<XYData<T>> getSourceSignalXY(int i) {
		ArrayList<String> names = (ArrayList<String>)getChannelNames();
		return xy_data.get(names.get(i));
	}
	
	@Override
	public List<T> getSourceSignalX(int i){
		ArrayList<XYData<T>> _xy = (ArrayList<XYData<T>>)getSourceSignalXY(i);
		ArrayList<T> x = new ArrayList<T>();
		for (int j = 0; j < _xy.size(); j++)
			x.add(_xy.get(j).getX());
		return x;
	}
	
	@Override
	public List<T> getSourceSignalY(int i){
		ArrayList<XYData<T>> _xy = (ArrayList<XYData<T>>)getSourceSignalXY(i);
		ArrayList<T> y = new ArrayList<T>();
		for (int j = 0; j < _xy.size(); j++)
			y.add(_xy.get(j).getY());
		return y;
	}
	
	public List<XYData<T>> getSourceSignalXY(String channel) {
		
		return new ArrayList<XYData<T>>(xy.get(channel));
			
	}
	
	public List<T> getSourceSignalX(String channel){
		ArrayList<XYData<T>> _xy = (ArrayList<XYData<T>>)xy_data.get(channel);
		ArrayList<T> x = new ArrayList<T>();
		for (int j = 0; j < _xy.size(); j++)
			x.add(_xy.get(j).getX());
		return x;
	}
	
	public List<T> getSourceSignalY(String channel){
		ArrayList<XYData<T>> _xy = (ArrayList<XYData<T>>)xy_data.get(channel);
		ArrayList<T> y = new ArrayList<T>();
		for (int j = 0; j < _xy.size(); j++)
			y.add(_xy.get(j).getY());
		return y;
	}
	
	@Override
	public void setChannelNames(List<String> names){
		
		//TODO: implement
	}
	
	@Override
	public List<String> getChannelNames() {
		ArrayList<String> names = new ArrayList<String>(xy_data.keySet());
		Collections.sort(names);
		return names;
	}

	@Override
	public int getChannelCount() {
		return xy_data.size();
	}

	@Override
	public void addDataSourceListener(DataSourceListener l){
		sourceListeners.add(l);
	}
	
	@Override
	public void removeDataSourceListener(DataSourceListener l){
		sourceListeners.remove(l);
	}
	
	public void setX(String channel, List<T> x){
		List<XYData<T>> _xy = xy_data.get(channel);
		if (_xy == null)
			_xy = appendChannel(channel, x.size());
		for (int i = 0; i < _xy.size(); i++)
			_xy.get(i).setX(x.get(i));
		fireReset();
		fireEmission(_xy.size());
	}
	
	public void setY(String channel, List<T> y){
		List<XYData<T>> _xy = xy_data.get(channel);
		if (_xy == null)
			_xy = appendChannel(channel, y.size());
		for (int i = 0; i < _xy.size(); i++)
			_xy.get(i).setY(y.get(i));
		fireReset();
		fireEmission(_xy.size());
	}
	
	protected List<XYData<T>> appendChannel(String channel, int size){
		List<XYData<T>> list = new ArrayList<XYData<T>>(size);
		xy_data.put(channel, list);
		for (int i = 0; i < size; i++)
			list.add(new XYData<T>());
		return list;
	}
	
	public void setXY(String channel, List<XYData<T>> new_xy){
		List<XYData<T>> _xy = xy_data.get(channel);
		for (int i = 0; i < _xy.size(); i++)
			_xy.get(i).set(new_xy.get(i));
		fireReset();
		fireEmission(_xy.size());
	}
	
	/******************************************
	 * Informs this data source to treat all subsequent operations as a batch job;
	 * i.e., do not fire any events. Events will be fired, if necessary, when this
	 * method is called with <code>b == false</code>.
	 * 
	 * @param b
	 */
	public void setBatch(boolean b){
		if (!b){
			batch = false;
			if (reset) fireReset();
			if (emission) fireEmission(Integer.MAX_VALUE);
			return;
			}
		batch = true;
	}
	
	/*********************************
	 * Resets this data source.
	 * 
	 * @throws IOException
	 */
	public void reset() throws IOException{
		cursor = 0;
		fireReset();
		fireEmission(Integer.MAX_VALUE);
	}
	
	/*******************************************
	 * Fires a source emission of size <code>size</code>, and updates the cursor.
	 * 
	 * @param size
	 */
	protected void fireEmission(int size){
		if (batch){
			emission = true;
			return;
			}
	
		//if (size < cursor) return;			//beyond size of data; reset required
		
		DataSourceEvent event = new DataSourceEvent(this, size);
		List<String> names = this.getChannelNames();
		
		//fire data source listeners for all variables
		xy.clear();
		
		for (int i = 0; i < names.size(); i++){
			String channel = names.get(i);
			List<XYData<T>> list = xy_data.get(channel);
			int _size = list.size() - cursor;
			_size = Math.min(_size, size);
			xy.put(channel, new ArrayList<XYData<T>>(list.subList(cursor, _size)));	
			}
			
		for (int i = 0; i < sourceListeners.size(); i++){
			sourceListeners.get(i).dataSourceEmission(event);
			}
		
		cursor += size;
		emission = false;
		
	}
	
	/*******************************************
	 * Informs listeners that this data source has been reset (i.e., the XY signal history should
	 * be destroyed.
	 * 
	 */
	protected void fireReset(){
		if (batch){
			reset = true;
			return;
			}
		
		DataSourceEvent event = new DataSourceEvent(this);
		
		for (int i = 0; i < sourceListeners.size(); i++)
			sourceListeners.get(i).dataSourceReset(event);
		
		reset = false;
	}
	
}