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

package mgui.interfaces.shapes.dynamic;

import java.io.IOException;

import mgui.datasources.DataType;
import mgui.geometry.Grid3D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


/********************************************
 * Volume with 3 space and 1 time dimension.
 * 
 * @author Andrew Reid
 *
 */
public class Volume4DInt extends Volume3DInt implements Shape4DInt {

	//public InterfaceInputStream inputStream;	//input for time samples
	public Grid3D baseGrid;						//static base grid
	public DataType sampleDataType;				//data type of the input stream
	public int dynX, dynY, dynZ;				//dimensions of dynamic input
	
	public Volume4DInt(){
		super();
		init();
		
	}
	
	public Volume4DInt(Grid3D dyn_grid, Grid3D base_grid){
		super(dyn_grid);
		setBaseGrid(base_grid);
		init();
		
	}
	
	public Volume4DInt(Grid3D dyn_grid, Grid3D base_grid, ColourMap cmap){
		super(dyn_grid, cmap);
		setBaseGrid(base_grid);
		init();
	}
	
	@Override
	protected void init(){
		attributes.add(new Attribute("TimeStep", new MguiDouble(0.0)));
		attributes.add(new Attribute("IntegrateMethod", "Default"));
		attributes.add(new Attribute("ShowBaseGrid", new MguiBoolean(true)));
		attributes.add(new Attribute("MinValue", new MguiInteger(1)));
		attributes.add(new Attribute("MaxValue", new MguiInteger(-1)));
	}
	
	//public void setInputStream(InterfaceInputStream stream, DataType type){
		//inputStream = stream;
	//	sampleDataType = type;
	//}
	
	public void setBaseGrid(Grid3D grid){
		baseGrid = (Grid3D)grid.clone();
	}
	
	public Grid3D getBaseGrid(){
		return baseGrid;
	}
	
	public boolean showBaseGrid(){
		return ((MguiBoolean)attributes.getValue("ShowBaseGrid")).getTrue();
	}
	
	public void showBaseGrid(boolean show){
		attributes.setValue("ShowBaseGrid", new MguiBoolean(show));
	}
	
	public int getMinValue(){
		return ((MguiInteger)attributes.getValue("MinValue")).getInt();
	}
	
	public int getMaxValue(){
		return ((MguiInteger)attributes.getValue("MaxValue")).getInt();
	}
	
	/***************************************
	 * Sets the display grid with sample <code>i</code> from this volume's input stream,
	 * using the integration method specified in the IntegrateMethod attribute. Sets a
	 * new updater which must be called using the <code>update()</code> method in order
	 * for the changes to be applied.
	 * @param i sample with which to set grid
	 * @throws IOException if <code>inputStream</code> is null or 
	 * <code>inputStream.sample(i)</code> throws an exception
	 */
	public void setSample(int i) throws IOException{
		
		
	}
	
	/*
	protected void integrateSample(byte[] sample){
		//integrate sample with baseGrid according to the IntegrateMethod attribute
		//TODO: set method
		//temp: default method is window sample to fit grid
		//parameters:
		int min = getMinValue();
		int max = getMaxValue();
		if (max < 0) max = Integer.MAX_VALUE;
		
		//DataType baseDataType = baseGrid.dataType;
		int baseSize = baseGrid.xSize * baseGrid.ySize * baseGrid.zSize;
		int sampleDataSize = DataTypes.getSize(sampleDataType);
		int sampleSize = sample.length / sampleDataSize;
		int sampleMax = Math.min(baseGrid.dataSize, sampleDataSize);
		double ratio = (double)sampleSize / (double)baseSize;
		byte[] b, s;
		double v;
		//ByteOrder bo = inputStream.getByteOrder();
		Grid3D tempGrid = (Grid3D)baseGrid.clone();
		
		//iterate through every element in sample
		//sample must be organized (by input stream) as: x(changes fastest), y, z:
		try{
			int p = 0, q = 0;
			for (int k = 0; k < baseGrid.x_size; k++)
				for (int j = 0; j < baseGrid.x_size; j++)
					for (int i = 0; i < baseGrid.z_size; i++){
						//b = new byte[baseGrid.dataSize];
						//if (showBaseGrid())
							baseGrid.getValue(i, j, k, b);
						//else
						//	b = new byte[baseGrid.dataSize];
						s = new byte[sampleMax];
						//get sample data
						p = (int)Math.round((double)q * (double)sampleDataSize * ratio);
						for (int m = 0; m < sampleMax; m++)
							s[m] = sample[p + m];
						//v = ioFunctions.getValue(s, sampleDataType, bo);
						//s = ioFunctions.getBytes(v, baseDataType, bo);
						
						//if value is above threshold, set this pixel
						//if (v >= min && v <= max)
							//integrate sample with base grid
						//	for (int l = 0; l < sampleMax; l++)
						//		b[l] = s[l];
						tempGrid.setValue(i, j, k, b);
						q++;
						}
			if (isByRef())
				//set updater
				setUpdater(new Volume4DUpdater(this, tempGrid));
			else
				//set grid now
				super.setGrid(tempGrid);
		}catch (Exception e){
			e.printStackTrace();
			return;
		}
		
	}
	
	
	public class Volume4DUpdater extends Volume3DUpdater{
		
		public Volume4DInt volume;
		public Grid3D grid;
		
		public Volume4DUpdater(Volume4DInt volume, Grid3D grid){
			this.volume = volume;
			this.grid = grid;
			}
		
		public void updateData(ImageComponent3D imageComponent, int index, int x, int y, int width, int height) {
			//copy grid
			//int k = index;
			for (int i = x; i < x + width; i++)
				for (int j = y; j < y + height; j++){
					double[] val = new double[grid.dataSize];
					grid.getValue(x, y, index, val);
					volume.getGrid().setValue(x, y, index, val);
					}
		}
		
	}
	*/
	
}