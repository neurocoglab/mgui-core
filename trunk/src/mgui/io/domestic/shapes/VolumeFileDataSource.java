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

package mgui.io.domestic.shapes;

import java.util.ArrayList;
import java.util.List;

import mgui.geometry.Grid3D;
import mgui.geometry.volume.VolumeFunctions;
import mgui.interfaces.io.DataSourceEvent;
import mgui.interfaces.io.DataSourceListener;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.numbers.MguiDouble;


/**********************************
 * Acts as a data source for volume files. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class VolumeFileDataSource implements InterfaceDataSource<MguiDouble> {

	protected VolumeFileLoader loader;
	protected ArrayList<DataSourceListener> sourceListeners = new ArrayList<DataSourceListener>();
	
	public VolumeFileDataSource(VolumeFileLoader loader){
		this.loader = loader;
	}
	
	@Override
	public void addDataSourceListener(DataSourceListener l){
		sourceListeners.add(l);
	}
	
	@Override
	public void removeDataSourceListener(DataSourceListener l){
		sourceListeners.remove(l);
	}

	public byte[] getSourceSignalAsBytes() {
		return getVolumeAsBytes(loader.getVolume3D());
	}
	
	//return grid as a 1-D byte array, changing in order of x, y, z
	//(x changes fastest)
	
	
	protected byte[] getVolumeAsBytes(Volume3DInt volume){
		Grid3D grid = volume.getGrid();
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		byte[] data = new byte[x_size * y_size * z_size];
		for (int k = 0; k < z_size; k++)
			for (int j = 0; j < x_size; j++)
				for (int i = 0; i < x_size; i++){
					byte b = VolumeFunctions.getMappedValueByte(volume, i, j, k);
					data[k * z_size + i * x_size + j] = b;
					}
		return data;
	}
	
	@Override
	public List<MguiDouble> getSourceSignal() {
		return getSourceSignalAsDouble(loader.getVolume3D());
	}
	
	protected List<MguiDouble> getSourceSignalAsDouble(Volume3DInt volume) {
		Grid3D grid = volume.getGrid();
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		ArrayList<MguiDouble> data = new ArrayList<MguiDouble>(x_size * y_size * z_size);
		for (int k = 0; k < z_size; k++)
			for (int j = 0; j < x_size; j++)
				for (int i = 0; i < x_size; i++){
					double d = VolumeFunctions.getMappedValueDouble(volume, i, j, k);
					data.add(new MguiDouble(d));
					}
		return data;
	}
	
	protected void fireListeners(){
		//fire data source listeners
		DataSourceEvent event = new DataSourceEvent(this);
		for (int i = 0; i < sourceListeners.size(); i++)
			sourceListeners.get(i).dataSourceEmission(event);
	}

}