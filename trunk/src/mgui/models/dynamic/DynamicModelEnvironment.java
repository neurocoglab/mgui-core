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

package mgui.models.dynamic;

import java.util.List;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.maps.NameMap;
import mgui.numbers.MguiNumber;

/***********************************
 * Interface for a dynamic model's environment. An environment controls the external input to
 * model components, and specifies state variables which respond to events from output components.
 * Also specifies getters and setters for 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 * @param <T> Number type for this environment
 */

public interface DynamicModelEnvironment<T extends MguiNumber> extends TimeStepListener,
																	 Updateable,
																	 Cloneable,
																	 InterfaceObject{

	public void handleOutputEvent(DynamicModelOutputEvent e);
	public void addSensor(DynamicModelEnvironmentSensor s);
	public void removeSensor(DynamicModelEnvironmentSensor s);
	public void addListener(DynamicModelEnvironmentListener l);
	public void removeListener(DynamicModelEnvironmentListener l);
	public double[] getInputState();
	public double getInputState(int i);
	public void setInputName(int index, String name);
	public void setInputNames(List<String> names);
	public void setInputSize(int s);
	public void setObservableState(double[] values);
	public void setObservableState(int index, double value);
	public double[] getObservableState();
	public double getObservableState(int i);
	public double getClock();
	public void setObservableName(int index, String name);
	public void setObservableNames(NameMap names);
	public void setObservableSize(int s);
	public void removeObservableName(String name);
	public void removeObservableName(int index);
	public List<String> getObservableNames();
	public List<String> getInputNames();
	public int getObservableSize();
	public InterfaceDataSource<T> getObservableDataSource();
	public InterfaceDataSource<T> getInputDataSource();
	public void setUpdater(DynamicModelUpdater updater) throws DynamicModelException;
	public void reset();
	public List<InterfaceDataSource<T>> getDataSources();
	
}