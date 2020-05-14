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

import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEnvironmentEvent;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;

/******************************************************
 * Simple data source which updates in response to changes in an environment's state variables.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since
 *
 */
public class SimpleEnvironmentObservableDataSource<T extends MguiNumber> extends AbstractEnvironmentDataSource<T> {

	@Override
	public boolean stimulate(DynamicModelEnvironmentEvent e) {
		
		DynamicModelEnvironment<MguiDouble> environment = e.getEnvironment();
		
		int size = environment.getObservableSize();
		double[] state = environment.getObservableState();
		clock.setValue(environment.getClock());
		int n = state.length / size;
		
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