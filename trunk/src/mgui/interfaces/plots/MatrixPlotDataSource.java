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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import mgui.interfaces.io.DataSourceEvent;
import mgui.interfaces.io.DataSourceListener;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.io.NamedDataSource;
import mgui.interfaces.variables.MatrixInt;
import mgui.interfaces.variables.VariableEvent;
import mgui.interfaces.variables.VariableListener;
import mgui.numbers.MguiDouble;

/*************************************************
 * Data source representing a {@linkplain MatrixInt} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MatrixPlotDataSource implements InterfaceDataSource<MguiDouble>, 
											 VariableListener,
											 NamedDataSource{

	protected HashMap<String, MatrixInt> matrix_map = new HashMap<String, MatrixInt>();
	protected ArrayList<DataSourceListener> sourceListeners = new ArrayList<DataSourceListener>();
	protected String last_channel_changed = null;
	protected String last_channel_removed = null;
	protected String last_channel_added = null;
	protected boolean batch = false;
	protected boolean emission = false, reset = false;
	protected int cursor = 0;
	
	/********************************************
	 * Return the matrix corresponding to {@code channel}.
	 * 
	 * @param channel
	 * @return
	 */
	public MatrixInt getMatrix(String channel){
		return matrix_map.get(channel);
	}
	
	
	
	@Override
	public List<MguiDouble> getSourceSignal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getSourceSignalAsBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDataSourceListener(DataSourceListener l){
		sourceListeners.add(l);
	}
	
	@Override
	public void removeDataSourceListener(DataSourceListener l){
		sourceListeners.remove(l);
	}

	@Override
	public List<String> getChannelNames() {
		ArrayList<String> channels = new ArrayList<String>(matrix_map.keySet());
		Collections.sort(channels);
		return channels;
	}

	@Override
	public void setChannelNames(List<String> names) {
		// Does nothing..
	}

	@Override
	public void variableValuesUpdated(VariableEvent e) {
		MatrixInt variable = (MatrixInt)e.getSource();
		if (matrix_map.containsKey(variable.getName())){
			fireReset();
			fireEmission(Integer.MAX_VALUE);
			}
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
		
		DataSourceEvent event = new DataSourceEvent(this);
		
		//fire data source listeners for all channels
		for (int i = 0; i < sourceListeners.size(); i++){
			sourceListeners.get(i).dataSourceEmission(event);
			}
		
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
	
	/**********************************************
	 * Sets a channel for this data source; uses the object's name as the channel name.
	 * If a channel by this name doesn't already exist, creates it; otherwise overwrites it.
	 * Finally, fires an emission event to alert listeners of this change.
	 * 
	 * @param matrix
	 */
	public void setChannel(MatrixInt matrix){
		
		matrix_map.put(matrix.getName(), matrix);
		last_channel_removed = matrix.getName();
		fireReset();
		fireEmission(Integer.MAX_VALUE);
		
	}
	
	/**********************************************
	 * Removes a channel for this data source.
	 * 
	 * @param channel
	 */
	public void removeChannel(String channel){
		matrix_map.remove(channel);
		last_channel_removed = channel;
		fireReset();
		fireEmission(Integer.MAX_VALUE);
		
	}
	
}