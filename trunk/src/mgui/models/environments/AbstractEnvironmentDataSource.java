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

package mgui.models.environments;

import java.util.ArrayList;
import java.util.List;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.io.DataSourceEvent;
import mgui.interfaces.io.DataSourceListener;
import mgui.interfaces.io.InterfaceDataSourceTimeSeries;
import mgui.interfaces.io.NamedDataSource;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.plots.XYData;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEnvironmentSensor;
import mgui.numbers.MguiNumber;

/*******************************************************
 * Serves as a data source updated by a {@link DynamicModelEnvironment}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T> Number type for this data source
 */
public abstract class AbstractEnvironmentDataSource<T extends MguiNumber> extends AbstractInterfaceObject 
																	  implements InterfaceDataSourceTimeSeries<T>,
																				 DynamicModelEnvironmentSensor,
																				 NamedDataSource{
	
	public NameMap names;
	protected ArrayList<T> signal;
	protected T clock;
	protected int index = -1;
	protected ArrayList<DataSourceListener> sourceListeners = new ArrayList<DataSourceListener>();
	protected boolean isDestroyed = false;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int i) {
		index = i;
	}

	//for data source functionality
	@Override
	public List<T> getSourceSignal() {
		return signal;
	}
	
	@Override
	public byte[] getSourceSignalAsBytes() {
		// TODO generate byte array from values
		return null;
	}

	@Override
	public ArrayList<String> getChannelNames(){
		return new ArrayList<String>(names.getNames());
	}
	
	@Override
	public void setChannelNames(List<String> names){
		this.names = new NameMap();
		for (int i = 0; i < names.size(); i++)
			this.names.add(i, names.get(i));
	}
	
	@Override
	public void addDataSourceListener(DataSourceListener l){
		sourceListeners.add(l);
	}
	
	@Override
	public void removeDataSourceListener(DataSourceListener l){
		sourceListeners.remove(l);
	}
	
	protected void fireEmission(){
		//fire data source listeners
		DataSourceEvent event = new DataSourceEvent(this);
		for (int i = 0; i < sourceListeners.size(); i++)
			sourceListeners.get(i).dataSourceEmission(event);
	}
	
	protected void fireReset(){
		DataSourceEvent event = new DataSourceEvent(this);
		for (int i = 0; i < sourceListeners.size(); i++)
			sourceListeners.get(i).dataSourceReset(event);
	}
	
	public void reset(){
		for (int i = 0; i < signal.size(); i++)
			signal.get(i).setValue(0);
		clock.setValue(0);
	}
	
	@Override
	public List<T> getSourceSignalX() {
		ArrayList<T> x = new ArrayList<T>(1);
		x.add(clock);
		return x;
		//return clock;
	}

	@Override
	public List<T> getSourceSignalY(int i) {
		ArrayList<T> y = new ArrayList<T>(1);
		y.add(signal.get(i));
		return y;
	}
	
	@Override
	public List<List<T>> getSourceSignalY() {
		ArrayList<List<T>> y = new ArrayList<List<T>>();
		y.add(new ArrayList<T>(getSourceSignal()));
		return y;
		//return getSourceSignal();
	}
	
	@Override
	public List<XYData<T>> getSourceSignalXY(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> getSourceSignalX(int i) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getName(){
		return "";
	}

	@Override
	public void destroy(){
		isDestroyed = true;
	}
	
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	
}