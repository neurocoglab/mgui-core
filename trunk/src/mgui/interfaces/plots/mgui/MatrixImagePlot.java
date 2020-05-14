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

package mgui.interfaces.plots.mgui;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;

import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.io.DataSourceEvent;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.plots.MatrixPlotDataSource;
import mgui.interfaces.plots.PlotException;
import mgui.interfaces.variables.MatrixInt;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import Jama.Matrix;

/************************************************************
 * Plots a matrix as an image, with a specified colour map.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MatrixImagePlot extends InterfacePlotMgui {

	protected MatrixPlotDataSource data_source;
	BufferedImage image;
	
	public enum ColourType{
		ColourMapped,
		RGBA,
		Greyscale;
	}
	
	public MatrixImagePlot(){
		init2();
	}
	
	private void init2(){
		// TODO: Ugg, fix this.
		_init();
		init();
		
		attributes.add(new AttributeSelection<ColourMap>("ColourMap", InterfaceEnvironment.getColourMaps(), ColourMap.class, ContinuousColourMap.DEFAULT_2));
		
		attributes.add(new Attribute<MguiDouble>("ColourMapMin",new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("ColourMapMax",new MguiDouble(1)));
		
		ArrayList<String> colour_types = new ArrayList<String>();
		colour_types.add("Colour mapped");
		colour_types.add("RGBA");
		colour_types.add("Greyscale");
		attributes.add(new AttributeSelection<String>("ColourType", colour_types, String.class, "Colour mapped"));
		
		attributes.add(new Attribute<String>("ColourChannels",""));
	}
	
	@Override
	public void setDataSource(InterfaceDataSource<?> source) throws PlotException {
		if (!(source instanceof MatrixPlotDataSource))
			throw new PlotException("MatrixImagePlot: Data source must be an instance of MatrixPlotDataSource.");
		
		
		if (data_source != null){
			data_source.removeDataSourceListener(this);
			}
		
		data_source = (MatrixPlotDataSource)source;
		data_source.addDataSourceListener(this);
		try{
			data_source.reset();
		}catch (IOException e){
			InterfaceSession.log("MatrixImagePlot: Could not set data source; with exception:\n" + e.getMessage(),
								 LoggingType.Errors);
			}
	}

	@Override
	public void dataSourceEmission(DataSourceEvent event) {
		MatrixPlotDataSource source = (MatrixPlotDataSource)event.getDataSource();
		String s = (String)attributes.getValue("ColourChannels");
		String[] channels = s.split(" ");
				
		switch (getColourType()){
			case ColourMapped:
				if (channels.length == 0){
					InterfaceSession.log("MatriximagePlot: Invalid number of channels: '" + s + "'.", LoggingType.Errors);
					return;
					}
				MatrixInt matrix_int = source.getMatrix(channels[0]);
				if (matrix_int == null){
					InterfaceSession.log("MatriximagePlot: Invalid channel: '" + channels[0] + "'.", LoggingType.Errors);
					return;
					}
				
				setMatrix(matrix_int.getJamaMatrix());
				
				
				break;
			case RGBA:
				
				break;
			case Greyscale:
				
				break;
			}
	}

	@Override
	public void dataSourceReset(DataSourceEvent event) {
		
	}
	
	protected void setMatrix(Matrix M){
		
		int x = M.getRowDimension();
		int y = M.getColumnDimension();
		ColorModel cm = getColourModel();
		if (image == null || image.getHeight() != y || image.getWidth() != x){
			// Instantiate new image
			WritableRaster raster = cm.createCompatibleWritableRaster(x, y);
			image = new BufferedImage(cm, raster, false, null);
			}
		
		// Set values
		WritableRaster raster = image.getRaster();
		for (int i = 0; i < x; i++)
			for (int j = 0; j < y; j++){
				raster.setPixel(x, y, new double[]{M.get(i, j)});
				}
		
		// Notify of change
		dataChanged(new MguiPlotEvent(this));
		
	}
	
	@Override
	public void dataChanged(MguiPlotEvent event){
		
		this.repaint();
		
	}
	
	protected ColorModel getColourModel(){
		
		switch (getColourType()){
			case ColourMapped:
				WindowedColourModel cm = new WindowedColourModel(getColourMap(), 
																 getColourMapMin(),
																 getColourMapMax(),
																 getHasAlpha(),
																 DataBuffer.TYPE_DOUBLE);
				return cm;
			case RGBA:
				
			case Greyscale:
				
			}
		return null;
	}
	
	/*************************************************
	 * Return the current colour map
	 * 
	 * @return
	 */
	public ColourMap getColourMap(){
		return (ColourMap)attributes.getValue("ColourMap");
	}
	
	/*************************************************
	 * Return the current colour map minimum value.
	 * 
	 * @return
	 */
	public double getColourMapMin(){
		return ((MguiDouble)attributes.getValue("ColourMapMin")).getValue();
	}
	
	/*************************************************
	 * Return the current colour map maximum value.
	 * 
	 * @return
	 */
	public double getColourMapMax(){
		return ((MguiDouble)attributes.getValue("ColourMapMax")).getValue();
	}
	
	/*************************************************
	 * Query the current alpha state
	 * 
	 * @return
	 */
	public boolean getHasAlpha(){
		return ((MguiBoolean)attributes.getValue("HasAlpha")).getTrue();
	}
	
	/*************************************************
	 * Return the current colour type.
	 * 
	 * @return
	 */
	public ColourType getColourType(){
		String type = (String)attributes.getValue("ColourType");
		if (type.equals("Colour mapped")) return ColourType.ColourMapped;
		if (type.equals("RGBA")) return ColourType.ColourMapped;
		if (type.equals("Greyscale")) return ColourType.ColourMapped;
		return ColourType.ColourMapped;
	}

}