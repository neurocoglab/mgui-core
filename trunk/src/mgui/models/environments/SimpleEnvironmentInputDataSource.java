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

package mgui.models.environments;

import java.util.List;

import mgui.interfaces.plots.XYData;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEnvironmentEvent;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;

/***********************************************************
 * Represents a simple input source for dynamic model environment.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T>
 */
public class SimpleEnvironmentInputDataSource<T extends MguiNumber> extends AbstractEnvironmentDataSource<T> {

	@Override
	public boolean stimulate(DynamicModelEnvironmentEvent e) {
		DynamicModelEnvironment<MguiDouble> environment = e.getEnvironment();
		
		int size = environment.getObservableSize();
		double[] state = environment.getObservableState();
		clock.setValue(environment.getClock());
		
		//Update signal with new environment state
		for (int i = 0; i < size; i++){
			signal.get(i).setValue(state[i]);
			}
		
		fireEmission();
		return true;
	}
	
	@Override
	public int getChannelCount() {
		return signal.size();
	}

	

}