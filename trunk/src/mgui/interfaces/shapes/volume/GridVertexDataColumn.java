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

package mgui.interfaces.shapes.volume;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.geometry.Grid3D;
import mgui.geometry.volume.VolumeFunctions;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.VertexDataColumnEvent;
import mgui.interfaces.shapes.VertexDataColumnEvent.EventType;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.FileLoader;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.InterfaceShapeLoader;
import mgui.io.domestic.shapes.ShapeInputOptions;
import mgui.io.domestic.shapes.ShapeModel3DOutputOptions;
import mgui.io.domestic.shapes.VolumeInputOptions;
import mgui.io.domestic.shapes.VolumeOutputOptions;
import mgui.io.domestic.shapes.xml.ShapeXMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;

/********************************************************
 * Vertex data column defined for a {@link Volume3DInt}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0 
 *
 */
public class GridVertexDataColumn extends VertexDataColumn {

	protected Volume3DInt volume;
	private WindowedColourModel colour_model;
	
	public GridVertexDataColumn(String name, Volume3DInt volume){
		super(name);
		this.volume = volume;
		init();
	}
	
	public GridVertexDataColumn(String name, Volume3DInt volume, ArrayList<MguiNumber> data){
		super(name, data);
		this.volume = volume;
		init();
		
	}
	
	private void init(){
		
		attributes.add(new Attribute<MguiBoolean>("IsSolidColour", new MguiBoolean(false)));
		attributes.add(new Attribute<Color>("SolidColour", Color.BLUE));
		
	}
	
	/************************************
	 * Fills this column with {@code value}.
	 * 
	 * @param data_type  The {@linkplain DataBuffer} type
	 */
	public void fillWithValues(int data_type, double value){
		int size = this.volume.getVertexCount();
		this.data = new ArrayList<MguiNumber>(size);
		DataType type = DataTypes.getFromDataBufferType(data_type);
		for (int i = 0; i < size; i++){
			data.add(NumberFunctions.getInstance(type, value));
			}
	}
	
	/**********************************
	 * Whether this column should be rendered as a solid colour (for values > 0). Otherwise,
	 * renders as a colour map.
	 * 
	 * @return
	 */
	public boolean isSolidColour(){
		return ((MguiBoolean)attributes.getValue("IsSolidColour")).getTrue();
	}
	
	/**********************************
	 * Sets whether this column should be rendered as a solid colour (for values > 0). If {@code false},
	 * renders as a colour map.
	 * 
	 * @return
	 */
	public void setIsSolidColour(boolean b){
		attributes.setValue("IsSolidColour", new MguiBoolean(b));
	}
	
	/*********************************
	 * Returns the colour for solid colour rendering. Requires "IsSolid" to be true.
	 * 
	 * @return
	 */
	public Color getSolidColour(){
		return (Color)attributes.getValue("SolidColour");
	}
	
	/*********************************
	 * Sets the colour for solid colour rendering. Requires "IsSolid" to be true.
	 * 
	 * @return
	 */
	public void setSolidColour(Color clr){
		attributes.setValue("SolidColour", clr);
	}
	
	/*************************************************
	 * Returns the value at voxel index [i, j, k]
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	public double getDoubleValueAtVoxel(int i, int j, int k){
		
		Grid3D grid = volume.getGrid();
		int idx = grid.getAbsoluteIndex(i, j, k);
		double d = this.getDoubleValueAtVertex(idx);
		return d;
		
	}
	
	/*****************************************************
	 * Returns the voxel-wise data as a 3D array of type {@code double}
	 * 
	 * @return
	 */
	public double[][][] getVoxelsAsDouble(){
		if (data == null || volume == null) return null;
		Grid3D grid = volume.getGrid();
		double[][][] values = new double[grid.getSizeS()][grid.getSizeT()][grid.getSizeR()];
		
		for (int i = 0; i < grid.getSizeS(); i++)
			for (int j = 0; j < grid.getSizeT(); j++)
				for (int k = 0; k < grid.getSizeR(); k++){
					values[i][j][k] = this.getDoubleValueAtVoxel(i, j, k);
					}
		
		return values;
	}
	
	/******************************************************
	 * Returns the value in this column at voxel index [i, j, k]
	 * 
	 * @param index
	 * @return
	 */
	public MguiNumber getValueAtVoxel(int i, int j, int k){
		
		Grid3D grid = volume.getGrid();
		int idx = grid.getAbsoluteIndex(i, j, k);
		return this.getValueAtVertex(idx);
	}
	
	/***************************************************
	 * Sets the value at voxel index [i, j, k]
	 * 
	 * @param i
	 * @param j
	 * @param k
	 */
	public void setValueAtVoxel(int i, int j, int k, MguiNumber value){
		
		Grid3D grid = volume.getGrid();
		int idx = grid.getAbsoluteIndex(i, j, k);
		this.setValueAtVertex(idx, value);
	}
	
	/*************************************
	 * Sets the parent volume for this vertex data
	 * 
	 * @param parent
	 */
	public void setParent(Volume3DInt parent){
		this.volume = parent;
	}
	
	@Override
	public void setColourMap(ColourMap map, double min, double max, boolean update){
		AttributeSelection<ColourMap> selection = (AttributeSelection<ColourMap>)attributes.getAttribute("ColourMap");
		selection.setValue(map, false);
		if (map != null){
			setColourMin(min, false);
			setColourMax(max, false);
			}
		updateColourModel();
		if (update)
			fireDataColumnColourMapChanged(new VertexDataColumnEvent(this, VertexDataColumnEvent.EventType.ColourMapChanged));
	}

	@Override
	public void setColourMap(ColourMap map, boolean update){
		AttributeSelection<ColourMap> selection = (AttributeSelection<ColourMap>)attributes.getAttribute("ColourMap");
		selection.setValue(map, false);
//		if (map != null){
//			setColourMin((float)map.mapMin, false);
//			setColourMax((float)map.mapMax, false);
//			}
		updateColourModel();
		if (update)
			fireDataColumnColourMapChanged(new VertexDataColumnEvent(this, VertexDataColumnEvent.EventType.ColourMapChanged));
	}
	
	@Override
	public void setDataLimits(double min, double max, boolean update){
		super.setDataLimits(min, max, update);
		if (this.colour_model != null){
			double range = max-min;
			if (range > 0){
				colour_model.setScale(1/range);
				colour_model.setIntercept(min);
				}
			}
			
	}
	
	/**********************************
	 * Returns the current colour model
	 * 
	 * @return
	 */
	public WindowedColourModel getColourModel(){
		if (colour_model == null)
			updateColourModel();
		return this.colour_model;
	}

	@Override
	public void attributeUpdated(AttributeEvent e){
		
		Attribute<?> attribute = e.getAttribute();
		
		if (attribute.getName().equals("ColourMap") ||
				attribute.getName().startsWith("ColourM") ||
				attribute.getName().contains("SolidColour")){
			updateColourModel();
			fireDataColumnColourMapChanged(new VertexDataColumnEvent(this, EventType.ColumnChanged));
			return;
			}
		
		super.attributeUpdated(e);
	}
	
	/**************************************************
	 * Called when some process has altered the colour model. Updates the max and min accordingly
	 * 
	 * @param update Whether to notify listeners after change is applied
	 */
	public void colourModelChanged(boolean update){
		
		if (colour_model == null) return;
		
		super.setColourMap(colour_model.getColourMap(), false);
		
		double mid = colour_model.getWindowMid();
		double width = colour_model.getWindowWidth();
		double intercept = colour_model.getIntercept();
		double scale = colour_model.getScale();
		mid = mid / scale + intercept;
		width = width / scale + intercept;
	
		double min = mid - width / 2.0;
		double max = min + width;
		
		this.setColourLimits(min, max, false);
		//this.setColourMax(max, false);
		
	}
	
	@Override
	public void setFromVertexDataColumn(VertexDataColumn column){
		this.setValues(column.getData(), false);
		this.setColourMap(column.getColourMap(), false);
		
		if (!(column instanceof GridVertexDataColumn)){
			setDataLimits(column.getDataMin(), column.getDataMax(), true);
			setColourLimits(column.getColourMin(), column.getColourMax(), true);
			return;
			}
		
		setDataLimits(column.getDataMin(), column.getDataMax(), false);
		setColourLimits(column.getColourMin(), column.getColourMax(), false);
		GridVertexDataColumn g_column = (GridVertexDataColumn)column;
		colour_model.setFromColourModel(g_column.getColourModel());
		
		fireDataColumnChanged(new VertexDataColumnEvent(this, EventType.ColumnChanged));
	}
	
	@Override
	public void setColourMin(double min, boolean update){
		super.setColourMin(min, update);
		if (update)
			updateColourModel();
	}
	
	@Override
	public void setColourMax(double max, boolean update){
		super.setColourMax(max, update);
		if (update)
			updateColourModel();
	}
	
	@Override
	public void setColourLimits(double min, double max, boolean update){
		super.setColourLimits(min, max, update);
		if (update)
			updateColourModel();
	}
	
	/*****************************************************
	 * Updates this data column's colour model from its current colour map. This requires that this
	 * column currently has data set (required to determine the data type for the colour model).
	 * 
	 */
	public void updateColourModel(){
		ColourMap map = (ColourMap)attributes.getValue("ColourMap");
		if (map == null || volume == null) return;
		if (colour_model == null){
			int data_type = getDataTransferType();
			if (data_type < 0) return; 		// Unknown data type means no data is currently set
			colour_model = VolumeFunctions.getColourModel(data_type, map, volume.hasAlpha());
			double min = this.getColourMin();
			double max = this.getColourMax();
			colour_model.setLimits(min, max);
		}else{
			colour_model.setColourMap(map);
			
//			double intercept = colour_model.getIntercept();
//			double scale = colour_model.getScale();
//			
//			double min = this.getColourMin();
//			double max = this.getColourMax();
//			
//			double width = max - min;
//			double mid = min + width/2.0;
//			
//			width = (width - intercept) * scale;
//			mid = (mid - intercept) * scale;
//		
//			min = mid - width / 2.0;
//			max = min + width;
//			
//			colour_model.setLimits(min, max);
			colour_model.setHasAlpha(volume.hasAlpha());
			}
		
		
		super.setColourMap(colour_model.getColourMap(), false);
		
		double mid = colour_model.getWindowMid();
		double width = colour_model.getWindowWidth();
		double intercept = colour_model.getIntercept();
		double scale = colour_model.getScale();
		mid = mid / scale + intercept;
		width = width / scale + intercept;
	
		double min = mid - width / 2.0;
		double max = min + width;
		
		this.setColourLimits(min, max, false);
		
		
		
		
		colour_model.is_solid = this.isSolidColour();
		colour_model.setSolidColour(this.getSolidColour());
		
	}
	
	/***************************************************
	 * Returns a slice image at the plane R={@code r}.
	 * 
	 * @param r
	 * @return
	 */
	public BufferedImage getRSliceImage(int r){
		return getRSliceImage(r, colour_model);
	}
	
	/***************************************************
	 * Returns a slice image at the plane R={@code r}.
	 * 
	 * @param r
	 * @return
	 */
	public BufferedImage getRSliceImage(int r, WindowedColourModel colour_model){
		
		Grid3D grid = volume.getGrid();
		if (r < 0 || r > grid.getSizeR()) return null;
		int s_size = grid.getSizeS();
		int t_size = grid.getSizeT();
		
		WritableRaster raster = colour_model.createCompatibleWritableRaster(s_size, t_size);
		BufferedImage image = new BufferedImage(colour_model, raster, false, null);
		raster = image.getRaster();
		for (int i = 0; i < s_size; i++)
			for (int j = 0; j < t_size; j++){
				raster.setPixel(i, t_size - j - 1, new double[]{getDoubleValueAtVoxel(i, j, r)});
				}
		
		return image;
	}
	
	/***************************************************
	 * Returns a slice image at the plane S={@code s}.
	 * 
	 * @param s
	 * @return
	 */
	public BufferedImage getSSliceImage(int s){
		return getSSliceImage(s, colour_model);
	}
	
	/***************************************************
	 * Returns a slice image at the plane S={@code s}.
	 * 
	 * @param z
	 * @return
	 */
	public BufferedImage getSSliceImage(int s, WindowedColourModel colour_model){
		
		Grid3D grid = volume.getGrid();
		if (s < 0 || s > grid.getSizeS()) return null;
		int r_size = grid.getSizeR();
		int t_size = grid.getSizeT();
		
		WritableRaster raster = colour_model.createCompatibleWritableRaster(t_size, r_size);
		BufferedImage image = new BufferedImage(colour_model, raster, false, null);
		raster = image.getRaster();
		for (int j = 0; j < t_size; j++)
			for (int k = 0; k < r_size; k++){
				raster.setPixel(j, r_size - k - 1, new double[]{getDoubleValueAtVoxel(s, j, k)});
				}
		
		return image;
	}
	
	/***************************************************
	 * Returns a slice image at the plane T={@code t}.
	 * 
	 * @param z
	 * @return
	 */
	public BufferedImage getTSliceImage(int t){
		return getTSliceImage(t, colour_model);
	}
	
	/***************************************************
	 * Returns a slice image at the plane T={@code t}.
	 * 
	 * @param z
	 * @return
	 */
	public BufferedImage getTSliceImage(int t, WindowedColourModel colour_model){
		
		Grid3D grid = volume.getGrid();
		if (t < 0 || t > grid.getSizeT()) return null;
		int r_size = grid.getSizeR();
		int s_size = grid.getSizeS();
		
		WritableRaster raster = colour_model.createCompatibleWritableRaster(s_size, r_size);
		BufferedImage image = new BufferedImage(colour_model, raster, false, null);
		raster = image.getRaster();
		for (int i = 0; i < s_size; i++)
			for (int k = 0; k < r_size; k++){
				raster.setPixel(i, r_size - k - 1, new double[]{getDoubleValueAtVoxel(i, t, k)});
				}
		
		return image;
	}
	
	// ****************************  XML STUFF *****************************************
	
	double 					xml_data_min, xml_data_max;
	WindowedColourModel 	xml_colour_model;
	String 					xml_root_dir;
	String 					xml_current_url;
	InterfaceShapeLoader 	xml_current_loader;
	VolumeInputOptions 		xml_current_shape_options;
	
	public void setXMLRoot(String root_dir){
		xml_root_dir = root_dir;
	}
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type)throws SAXException {
		
		if (localName.equals("WindowedColourModel")){
			xml_colour_model = this.getColourModel();
			if (xml_colour_model == null)
				xml_colour_model = new WindowedColourModel();
			xml_colour_model.handleXMLElementStart(localName, attributes, type);
			
			return;
			}
		
		if (localName.equals("Data") && type.equals(XMLType.Reference)){
			
			// By reference load required
			
			xml_data_type = attributes.getValue("type");
			xml_data_size = Integer.valueOf(attributes.getValue("size"));
			
			xml_current_url = attributes.getValue("url");
			if (xml_root_dir != null)
				xml_current_url = xml_current_url.replace("{root}", xml_root_dir);
			
			String _loader = attributes.getValue("loader");
			
			// Get loader instance
			xml_current_loader = ShapeXMLFunctions.getLoaderInstance(_loader);
			if (xml_current_loader == null){
				xml_current_url = null;
				throw new SAXException("GridVertexDataColumn.handleXMLElementStart: Could not instantiate " +
						 "a loader for the reference '" + _loader + "'.");
				}
			
			return;
			}
		
		if (localName.equals("InterfaceIOOptions")){
			// Set I/O options here
			if (xml_current_loader == null)
				throw new SAXException("InterfaceShape.handleXMLElementStart: IOOptions started, but " + 
									   "no loader has been set..");
			
			InterfaceIOType complement = xml_current_loader.getWriterComplement();
			if (complement == null){
				InterfaceSession.log("GridVertexDataColumn.handleXMLElementStart: No writer complement for loader" +
									 xml_current_loader.getClass().getCanonicalName() +"'; using defaults.", 
									 LoggingType.Warnings);
				return;
				}
			
			InterfaceIOOptions c_options = complement.getOptionsInstance();
			InterfaceIOOptions options = xml_current_loader.getIOType().getOptionsInstance();
			if (c_options == null || options == null || 
					!(options instanceof ShapeInputOptions)){
				xml_current_shape_options = null;
				xml_current_url = null;
				String _loader = xml_current_loader.getClass().getCanonicalName();
				xml_current_loader = null;
				throw new SAXException("GridVertexDataColumn.handleXMLElementStart: Could not instantiate " +
						 			   "options for the loader '" + _loader + "'.");
				}
			
			xml_current_shape_options = (VolumeInputOptions)options;
			c_options.handleXMLElementStart(localName, 
											attributes, 
											null);
			xml_current_shape_options.setFromComplementaryOptions(c_options);
			
			return;
			}
		
		
		super.handleXMLElementStart(localName, attributes, type);
		
	}
	
	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		
		if (localName.equals("InterfaceIOOptions")){
			if (xml_current_shape_options == null)
				throw new SAXException("GridVertexDataColumn.handleXMLElementEnd: InterfaceIOOptions block ended, but " + 
						   			   "no I/O options have been set..");
			if (xml_current_loader == null)
				throw new SAXException("GridVertexDataColumn.handleXMLElementEnd: InterfaceIOOptions block ended, but " + 
						   			   "no loader has been set..");
			try{
				Volume3DInt shape = (Volume3DInt)xml_current_loader.loadShape(xml_current_shape_options, null);
				if (shape == null)
					throw new SAXException("GridVertexDataColumn.handleXMLElementEnd: Could not load referenced " + 
							   "shape..");
				
				ArrayList<MguiNumber> data = shape.getCurrentVertexData();
				setValues(data);
				
				xml_current_shape_options = null;
				xml_current_loader = null;
			}catch (IOException ex){
				throw new SAXException("GridVertexDataColumn.handleXMLElementEnd: IOException loading referenced " + 
									   "shape..\nDetails: " + ex.getMessage());
				}
			}
		
		if (localName.equals(this.getLocalName())){
			WindowedColourModel cm = this.getColourModel();
			if (cm == null)
				throw new SAXException("GridVertexDataColumn ending but no colour model set...");
			
			if (xml_colour_model == null){
				cm.setIntercept(xml_data_min);
				if (xml_data_max > xml_data_min)
					cm.setScale(1.0 / (xml_data_max - xml_data_min));
				else
					cm.setScale(1.0);
				cm.setWindowMid(0.5);
				cm.setWindowWidth(1.0);
			}else{
				cm.setFromColourModel(xml_colour_model);
				}
			setDataMin(xml_data_min, false);
			setDataMax(xml_data_max, false);
			
			InterfaceSession.log("Scale: " + cm.getScale() + " Intercept: " + cm.getIntercept(), LoggingType.Debug);
			InterfaceSession.log("Min: " + xml_data_min + " Max: " + xml_data_max, LoggingType.Debug);
			
			return;
			}
		
		super.handleXMLElementEnd(localName);
	}
	
	String by_reference_url = null;
	
	/****************************
	 * 
	 * Returns a URL string for the latest call to {@linkplain writeXML}. Is {@code null} if no call has yet
	 * been made, or the latest write was not by reference.
	 * 
	 * @return
	 */
	public String getByReferenceUrl() {
		return by_reference_url;
	}
	
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		by_reference_url = null;
		
		if (data == null || data.size() == 0)
			throw new IOException("GridVertexDataColumn '" + this.getName() + "': no data to write!");
		
		XMLOutputOptions shape_options = null;
		ShapeModel3DOutputOptions model_options = null;
		if (options instanceof ShapeModel3DOutputOptions){
			model_options = (ShapeModel3DOutputOptions)options;
			shape_options = model_options.shape_xml_options.get(volume);
			if (shape_options == null)
				throw new IOException("InterfaceShape: no XML options defined for shape.");
		}else{
			shape_options = options;
			}
		
		if (shape_options.type != XMLType.Reference){
			writeXMLFull(tab, writer, options, progress_bar);
			return;
			}
		
		
		
		// Write column as reference file
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		String _tab3 = XMLFunctions.getTab(tab + 2);
		
		String data_type = "?";
		if (data.size() > 0){
			data_type = data.get(0).getLocalName();
			}
		
		writer.write(_tab + "<" + getLocalName() + " name='" + getName() + "' >\n");
		
		// Attributes
		attributes.writeXML(tab + 1, writer, shape_options, progress_bar);
		
		// Loader/writer
		
		String url_ref = null, url_ref2 = null;
		InterfaceIOOptions io_options = null;
		FileWriter shape_writer = null;
		FileLoader shape_loader = null;
		if (model_options != null){
			// Set up from model
			String filename = shape_options.filename;
			File root_dir = options.getFiles()[0].getParentFile();
			File shapes_dir = IoFunctions.fullFile(root_dir, model_options.shapes_folder);
			File file = IoFunctions.fullFile(shapes_dir, filename);
			if (file.exists() && !model_options.overwrite_existing){
				InterfaceSession.log("InterfaceShape.writeXML: Skipping existing file '" + file.getAbsolutePath() +"'.", 
									 LoggingType.Warnings);
				return;
				}
			
			//File parent_dir = file.getParentFile();
			if (!shapes_dir.exists() && !IoFunctions.createDirs(shapes_dir))
				throw new IOException("Could not create the path to '" + file.getAbsolutePath() + "'.");
			
			url_ref = "{root}" + File.separator + model_options.shapes_folder + File.separator + filename;
			
			url_ref2 = file.getAbsolutePath();
			io_options = shape_options.io_options;
			//io_options = model_options.shape_io_options.get(this);
			shape_writer = shape_options.writer;
			if (shape_writer == null)
				throw new IOException("No shape writer assigned for shape '" + volume.getFullName() + "'.");
			
			if (io_options == null){
				// Get default options
				io_options = shape_writer.getIOType().getOptionsInstance();
				}
			if (shape_writer.getLoaderComplement() == null)
				throw new IOException("Shape writer '" + shape_writer.getClass().getCanonicalName() + 
									  "' has no complementary loader.");
			shape_loader = (FileLoader)shape_writer.getLoaderComplement().getIOInstance();
		}else{
			// Get from shape itself
			url_ref = (String)attributes.getValue("UrlReference");
			by_reference_url = url_ref;
			if (url_ref == null || url_ref == ""){
				throw new IOException("Shape [" + this.getClass().getCanonicalName() + ": " + 
									  this.getName() + "]: URL reference not set for XML write of type 'reference'.");
				}
			shape_writer = volume.getFileWriter();
			if (shape_writer == null){
				throw new IOException("Shape [" + this.getClass().getCanonicalName() + ": " + 
									  this.getName() + "]: No file writer set for XML write of type 'reference'.");
				}
			shape_loader = (FileLoader)shape_writer.getLoaderComplement().getIOInstance();
			if (shape_loader == null)
				throw new IOException("Shape writer '" + shape_writer.getClass().getCanonicalName() + 
									  "' has no complementary loader.");
			io_options = shape_writer.getIOType().getOptionsInstance();
			// Set up the URL, replace the full path with the root tag, if applicable
			if (volume.getModel() != null){
				String root_dir = (String)volume.getModel().getModelSet().getAttribute("RootURL").getValue();
				if (root_dir != null && root_dir.length() > 0){
					if (!root_dir.endsWith(File.separator))
						root_dir = root_dir + File.separator;
					url_ref2 = url_ref.replace(root_dir, "{root}" + File.separator);
					}
				}
			}
		
		
		// Add extension if not defined
		String extension = "";
		List<String> exts = shape_writer.getIOType().getExtensions();
		if (exts.size() > 0) {
			boolean has_ext = false;
			for (String ext : exts) {
				if (url_ref.endsWith("." + ext)) {
					has_ext = true;
					extension = ext;
					break;
					}
				}
			if (!has_ext) {
				extension = exts.get(0);
				}
			}
		
		if (extension.length() > 0) {
			if (url_ref.endsWith(extension)) {
				url_ref = url_ref.substring(0,url_ref.lastIndexOf("." + extension));
				}
			if (url_ref2.endsWith(extension)) {
				url_ref2 = url_ref2.substring(0,url_ref2.lastIndexOf("." + extension));
				}
			}
		
		// Modify URL for this column
		url_ref = url_ref + "." + this.getName() + "." + extension;
		url_ref2 = url_ref2 + "." + this.getName() + "." + extension;;
		File output_file = new File(url_ref2);
		
		by_reference_url = url_ref;
		
		// Data
		writer.write("\n" + _tab2 + "<Data\n" + 
					 _tab3 + "type='" + data_type + "'\n" + 
					 _tab3 + "size='" + data.size() + "'\n" +
					 _tab3 + "writer='" + shape_writer.getClass().getCanonicalName() +"'\n" +
					 _tab3 + "loader='" + shape_loader.getClass().getCanonicalName() +"'\n" +
					 _tab3 + "url='" + url_ref + "'\n" + 
					 _tab2 + "/>\n");
		
		// I/O Options
		writer.write("\n");
		io_options.writeXML(tab+1, writer, model_options, progress_bar);
		
		// Write the volume file
		VolumeOutputOptions _options = (VolumeOutputOptions)io_options;
		VolumeOutputOptions volume_options = new VolumeOutputOptions();
		volume_options.datatype = this.getDataBufferType();
		volume_options.flipX = _options.flipX;
		volume_options.flipY = _options.flipY;
		volume_options.flipZ = _options.flipZ;
		volume_options.use_column = this.getName();
		volume_options.setFiles(new File[]{new File(url_ref2)});
		volume_options.volumes = new Volume3DInt[]{this.volume};
		volume_options.apply_masks = _options.apply_masks;
		
		// Does parent directory exist?
		File parent_dir = output_file.getParentFile();
		if (!parent_dir.exists()){
			if (!IoFunctions.createDirs(parent_dir)){
				throw new IOException("Column '" + getName() + "' for shape '" + volume.getFullName() + "': " +  
  			  			  "parent directory '" + parent_dir.getAbsolutePath() + " cannot be created.");
				}
			}
		
		if (!shape_writer.write(volume_options, progress_bar)){
			throw new IOException("Column '" + getName() + "' for shape '" + volume.getFullName() + "': " +  
		  			  			  "writer failed.");
			}
		
		if (shape_writer.getFile() != null) {
			by_reference_url = "{root}" + File.separator + model_options.shapes_folder + File.separator +
					shape_writer.getFile().getName();
			}
		
		// Colour map
		ColourMap cmap = this.getColourMap();
		if (cmap != null){
			cmap.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		// Colour model
		WindowedColourModel cmodel = this.getColourModel();
		if (cmodel != null){
			cmodel.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		// Name map
		NameMap nmap = this.getNameMap();
		if (nmap != null){
			nmap.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		writer.write(_tab + "</" + getLocalName() + ">");
		
		
	}
	
	protected void writeXMLFull(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String data_type = "?";
		if (data.size() > 0){
			data_type = data.get(0).getLocalName();
			}
		
		writer.write(_tab + "<" + getLocalName() + " name='" + getName() + "' >\n");
		
		// Attributes
		attributes.writeXML(tab + 1, writer, options, progress_bar);
		
		// Data
		writer.write("\n" + _tab2 + "<Data " + 
					 "type='" + data_type + "' " + 
					 "size='" + data.size() + "' " +
					 "encoding='" + XMLFunctions.getEncodingStr(options.encoding) + "' >\n");
		
		if (data.size() > 0){
		
			switch (options.encoding){
				case Base64Binary:
					writeBinaryData(writer, tab+2, 0);
					break;
				case Base64BinaryZipped:
				case Base64BinaryGZipped:
					writeBinaryData(writer, tab+2, 1);
					break;
				case Ascii:
					writeAsciiData(writer, tab+2, options.max_line_size, options.sig_digits);
					break;
				}
			}
		
		writer.write("\n" + _tab2 + "</Data>\n");
		
		// Colour map
		ColourMap cmap = this.getColourMap();
		if (cmap != null){
			cmap.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		// Colour model
		WindowedColourModel cmodel = this.getColourModel();
		if (cmodel != null){
			cmodel.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		// Name Map
		NameMap nmap = this.getNameMap();
		if (nmap != null){
			nmap.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		// TODO: Linked data
		
		
		writer.write(_tab + "</" + getLocalName() + ">\n");
	}
	
	protected int getDataBufferType(){
		
		String data_type = data.get(0).getLocalName();
		if (data_type.equals((new MguiDouble()).getClass().getCanonicalName()))
			return DataBuffer.TYPE_DOUBLE; 
		if (data_type.equals((new MguiFloat()).getClass().getCanonicalName()))
			return DataBuffer.TYPE_FLOAT; 
		if (data_type.equals((new MguiInteger()).getClass().getCanonicalName()))
			return DataBuffer.TYPE_INT;
		if (data_type.equals((new MguiBoolean()).getClass().getCanonicalName()))
			return DataBuffer.TYPE_BYTE;
		return DataBuffer.TYPE_DOUBLE; 
			
	}
	
	public String getLocalName(){
		return "GridVertexDataColumn";
	}
	
	@Override
	protected void loadBinaryData(String string_data, int compression){
		
		try{
			// Decode
			Charset charset = Charset.forName("UTF-8");
			byte[] utf8_bytes = string_data.getBytes(charset);
			byte[] b_data = java.util.Base64.getMimeDecoder().decode(utf8_bytes);
			
			xml_data_min = Double.MAX_VALUE;
			xml_data_max = -Double.MAX_VALUE;
			
			DataType type = DataTypes.getType(DataTypes.DOUBLE);
			if (xml_data_type.equals("MguiInteger"))
				type = DataTypes.getType(DataTypes.INTEGER);
			if (xml_data_type.equals("MguiLong"))
				type = DataTypes.getType(DataTypes.LONG);
			if (xml_data_type.equals("MguiFloat"))
				type = DataTypes.getType(DataTypes.FLOAT);
			
			// Decompress
			switch (compression){
				case 1: 
					b_data = IoFunctions.decompressZipped(b_data);
					break;
				case 2: 
					b_data = IoFunctions.decompressGZipped(b_data);
					break;
				}
			
			// Convert to data
			ByteBuffer buffer = ByteBuffer.wrap(b_data);
			while (buffer.hasRemaining()){
				double value = Double.NaN;
				switch (type.val){
					case DataTypes.INTEGER:
						value = buffer.getInt();
						break;
					case DataTypes.FLOAT:
						value = buffer.getFloat();
						break;
					case DataTypes.LONG:
						value = buffer.getLong();
						break;
					case DataTypes.DOUBLE:
						value = buffer.getDouble();
						break;
					}
				data.add(NumberFunctions.getInstance(type, value));
				if (value < xml_data_min) xml_data_min = value;
				if (value > xml_data_max) xml_data_max = value;
				
				}
			
		}catch (Exception ex){
			InterfaceSession.handleException(ex);
			}
		
	}
	
	@Override
	protected void loadAsciiData(String string_data){
		
		StringTokenizer tokens = new StringTokenizer(string_data);
		
		xml_data_min = Double.MAX_VALUE;
		xml_data_max = -Double.MAX_VALUE;
		
		DataType type = DataTypes.getType(DataTypes.DOUBLE);
		if (xml_data_type.equals("MguiInteger"))
			type = DataTypes.getType(DataTypes.INTEGER);
		if (xml_data_type.equals("MguiLong"))
			type = DataTypes.getType(DataTypes.LONG);
		if (xml_data_type.equals("MguiFloat"))
			type = DataTypes.getType(DataTypes.FLOAT);
		
		while (tokens.hasMoreTokens()){
			double value = Double.NaN;
			switch (type.val){
				case DataTypes.INTEGER:
					value = Integer.valueOf(tokens.nextToken());
					break;
				case DataTypes.FLOAT:
					value = Float.valueOf(tokens.nextToken());
					break;
				case DataTypes.LONG:
					value = Long.valueOf(tokens.nextToken());
					break;
				case DataTypes.DOUBLE:
					value = Double.valueOf(tokens.nextToken());
					break;
				}
			data.add(NumberFunctions.getInstance(type, value));
			if (value < xml_data_min) xml_data_min = value;
			if (value > xml_data_max) xml_data_max = value;
			
			}
		
	}
	
}