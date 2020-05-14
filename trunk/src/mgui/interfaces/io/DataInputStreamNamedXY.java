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
import java.util.List;

import mgui.numbers.MguiNumber;

public interface DataInputStreamNamedXY<T extends MguiNumber> extends DataInputStreamXY<T> {

	/******************************************
	 * Returns the Y data for the channel named <code>channel</code>, as a list of type
	 * <code>T</code>.
	 * 
	 * @param channel
	 * @return
	 */
	public List<T> getYData(String channel) throws IOException;
	
	/******************************************
	 * Returns a list of the channels in this stream. The list's indices correspond
	 * to the channel index.
	 * 
	 * @return
	 */
	public List<String> getVariableNames();
	
}