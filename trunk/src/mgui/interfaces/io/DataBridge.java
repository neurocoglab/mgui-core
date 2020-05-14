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

package mgui.interfaces.io;

import java.io.IOException;
import java.util.ArrayList;

import mgui.interfaces.InterfaceSession;
import mgui.numbers.MguiNumber;

/*******************************************************
 * Represents a data bridge, combining an input stream and an output stream.
 * 
 * <p>TODO: Make this object and all I/O objects thread safe. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T>
 */
public abstract class DataBridge<T extends MguiNumber> implements DataInputStream<T>, 
															  	  DataOutputStream<T>{

	public boolean closed = false;
	public ArrayList<DataInputStreamListener<T>> inputListeners = new ArrayList<DataInputStreamListener<T>>();
	
	public void addInputStreamListener(DataInputStreamListener<T> l) {
		inputListeners.add(l);
	}

	public void removeInputStreamListener(DataInputStreamListener<T> l) {
		inputListeners.remove(l);
	}

	@Override
	public void dataSourceEmission(DataSourceEvent event) {
		
		//TODO: handle possible cast exception?
		InterfaceDataSource<T> source = (InterfaceDataSource<T>)event.getDataSource();
		
		//clean up if closed
		if (closed){ 
			source.removeDataSourceListener(this);
			return;
			}
		try{
			setData(source.getSourceSignal());
		}catch (IOException e){
			InterfaceSession.log("DataBridge: IOException: " + e.getLocalizedMessage());
			if (InterfaceSession.showErrorsOnConsole())
				e.printStackTrace();
			}
		fireListeners();
	}

	protected void fireListeners(){
		for (int i = 0; i < inputListeners.size(); i++)
			inputListeners.get(i).dataInputEvent(this);
	}
	
	public void close(){
		closed = true;
	}

}