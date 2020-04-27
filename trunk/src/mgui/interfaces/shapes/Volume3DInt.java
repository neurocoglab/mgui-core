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

package mgui.interfaces.shapes;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ImageComponent3D;
import org.jogamp.java3d.ImageComponent3D.Updater;
import javax.swing.ImageIcon;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3f;

import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Shape;
import mgui.geometry.util.GeometryFunctions;
import mgui.geometry.volume.VolumeFunctions;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Camera3DListener;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ColourMapListener;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.attributes.ShapeAttribute;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.shapes.volume.GridVertexDataColumn;
import mgui.interfaces.shapes.volume.Volume3DRenderer;
import mgui.interfaces.shapes.volume.Volume3DTexture;
import mgui.interfaces.shapes.volume.Volume3DTexture.UpdateTextureType;
import mgui.interfaces.shapes.volume.Volume3DUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.domestic.shapes.ShapeModel3DOutputOptions;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/****************************
 * Class acting as interface to a volume shape, as defined by the Grid3D geometry
 * class. Volume3D provides a Texture3D node and also defines the appearance of the
 * voxel set in terms of colour or intensity mapping. 
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class Volume3DInt extends Shape3DInt implements ColourMapListener,
													   ImageComponent3D.Updater,
													   ShapeListener {

	protected Volume3DRenderer renderer;
	protected boolean textureSet;
	protected Updater updater;
	protected File dataFile;
	protected VolumeFileLoader loader;
	protected ShapeSceneNode intRect3D = new ShapeSceneNode();
	protected HashMap<String,WindowedColourModel> colour_models = new HashMap<String,WindowedColourModel>();
	protected HashMap<String, boolean[][][]> masks = new HashMap<String, boolean[][][]>(); 
	protected HashMap<String, Boolean> apply_mask = new HashMap<String, Boolean>();
	protected HashMap<String, Double> composite_alphas = new HashMap<String, Double>();
	protected HashMap<String, Boolean> show_in_composite = new HashMap<String, Boolean>();
	
	
	protected ArrayList<String> composite_column_order = new ArrayList<String>();
	
	//debug flag
	boolean debug = false;
	
	protected BranchGroup group_node, render_node, box_node;
	
	public Volume3DInt(){
		this(new Grid3D());
	}
	
	public Volume3DInt(Grid3D g){
		init();
		setGrid(g);
	}
	
	public Volume3DInt(Grid3D g, ColourMap cmap){
		init();
		attributes.getAttribute("ColourMap").setValue(cmap, false);
		
	}

	@Override
	protected void updateDataColumns(){
		super.updateDataColumns();
		
		if (composite_column_order.size() == 0){
			composite_column_order.addAll(data_columns);
			return;
			}
		
		TreeSet<String> set = new TreeSet<String>(composite_column_order);
		
		ArrayList<String> new_ordered = new ArrayList<String> (composite_column_order); 
		
		for (int i = 0; i < data_columns.size(); i++){
			if (!set.contains(data_columns.get(i)))
				new_ordered.add(data_columns.get(i));
			}
		
		try{
			set = new TreeSet<String>(data_columns);
			for (int i = 0; i < composite_column_order.size(); i++){
				if (!set.contains(composite_column_order.get(i)))
					new_ordered.remove(composite_column_order.get(i));
				}
		}catch (NullPointerException ex){
			// Fail silently; should only happen if there is a concurrency issue
			}
		
		composite_column_order = new_ordered;
		
	}
	
	@Override
	public boolean renameVertexDataColumn(String old_name, String new_name){
		if (!hasColumn(old_name)) return false;
		if (hasColumn(new_name)) return false;
		VertexDataColumn column = vertexData.get(old_name);
		vertexData.remove(old_name);
		vertexData.put(new_name, column);
		column.setName(new_name);
		
		// Update all the hash maps
		colour_models.put(new_name, colour_models.get(old_name));
		colour_models.remove(old_name);
		masks.put(new_name, masks.get(old_name));
		masks.remove(old_name);
		apply_mask.put(new_name, apply_mask.get(old_name));
		apply_mask.remove(old_name);
		composite_alphas.put(new_name, composite_alphas.get(old_name));
		composite_alphas.remove(old_name);
		show_in_composite.put(new_name, show_in_composite.get(old_name));
		show_in_composite.remove(old_name);
		
		// Preserve composite order
		for (int i = 0; i < composite_column_order.size(); i++){
			if (composite_column_order.get(i).equals(old_name)){
				composite_column_order.remove(old_name);
				composite_column_order.add(i, new_name);
				}
			}
		
		updateDataColumns();
		if (getCurrentColumn() != null && getCurrentColumn().equals(old_name))
			this.setCurrentColumn(new_name, false);
		
		last_column_changed = column;
		fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnRenamed));
		return true;
		
	}
	
	/******************************************************
	 * Adjusts the order of column {@code column} in the composite rendering stack.
	 * 
	 * @param column
	 * @param index
	 */
	public void setCompositeIndex(String column, int index){
		setCompositeIndex(column, index, true);
	}
	
	/******************************************************
	 * Adjusts the order of column {@code column} in the composite rendering stack.
	 * 
	 * @param column
	 * @param index
	 */
	public void setCompositeIndex(String column, int index, boolean notify){
		updateDataColumns();
		
		if (index < 0 || index > composite_column_order.size()){
			InterfaceSession.log("Volume3DInt.setCompositeIndex: index " + index + " is invalid.", 
								 LoggingType.Errors);
			return;
			}
		
		composite_column_order.remove(column);
		composite_column_order.add(index, column);
		
		if (notify)
			this.fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnChanged));
		
	}
	
	public ArrayList<String> getCompositeOrderedColumns(){
		updateDataColumns();
		return composite_column_order;
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new Grid3D();
	}
	
	/********************************
	 * Overrides <code>InterfaceShape.setParentSet</code> to add itself as a listener to the parent
	 * 
	 */
	@Override
	public void setParentSet(ShapeSet set){
		if (this.parent_set != null) 
			((ShapeSet3DInt)parent_set).removeShapeListener(this);
		super.setParentSet(set);
		((ShapeSet3DInt)parent_set).addShapeListener(this);
		
	}
	
	
	@Override
	public int getClosestVertex(Point3f point){
		
		Grid3D grid = getGrid();
		int[] voxel = grid.getEnclosingVoxel(point);
		if (voxel == null) return -1;
		return grid.getAbsoluteIndex(voxel);
		
	}
	
	public void shapeUpdated(ShapeEvent e){
		
		switch (e.eventType){
			case SectionAdded:
			case SectionRemoved:
				setScene3DObject();
				return;
			}
		
		
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/volume_3d_17.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/volume_3d_17.png");
	}
	
	@Override
	protected void init(){
		super.init();
		//set up attributes here
		/**@todo implement ColourMap class **/
		attributes.add(new ShapeAttribute<MguiInteger>("TextureType", new MguiInteger(Volume3DTexture.TYPE_INTENSITY_CMAP_ALPHA)));
		attributes.add(new ShapeAttribute<ColourMap>("ColourMap", ColourMap.class));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowPolygon", new MguiBoolean(true)));
		attributes.add(new ShapeAttribute<Color>("2D.PolygonColour", Color.GREEN));
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.ShowPolygon", new MguiBoolean(true)));
		attributes.add(new ShapeAttribute<Color>("3D.PolygonColour", Color.GREEN));
		attributes.add(new ShapeAttribute<Color>("BackColour", Color.BLACK));
		attributes.add(new ShapeAttribute<MguiBoolean>("BackOpaque", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiFloat>("MapMax", new MguiFloat(255f)));
		attributes.add(new ShapeAttribute<MguiFloat>("MapMin", new MguiFloat(0f)));
		attributes.add(new ShapeAttribute<MguiFloat>("3D.SliceScale", new MguiFloat(2f)));
		attributes.add(new ShapeAttribute<HashMap<String,MguiBoolean>>("MaskSet", new HashMap<String,MguiBoolean>()));
		attributes.add(new AttributeSelection<String>("RenderMode", Volume3DRenderer.getRenderModes(), String.class, "As Volume"));
		attributes.add(new ShapeAttribute<MguiBoolean>("ApplyMasks", new MguiBoolean(true)));
		attributes.add(new ShapeAttribute<MguiBoolean>("IsByRef", new MguiBoolean(true)));
		attributes.add(new ShapeAttribute<MguiBoolean>("IsComposite", new MguiBoolean(false)));
		
		attributes.setValue("2D.LineColour", Color.BLUE);
		this.hasAlpha(true);
		hasCameraListener = true;
		
		isImageShape = true;
	}
	
	public HashMap<String, boolean[][][]> getMasks(){
		return masks;
	}
	
	
	
	@Override
	public boolean addVertexData(String column, ArrayList<MguiNumber> data, NameMap nmap, ColourMap cmap){
		if (data.size() != this.getVertexCount()){
			InterfaceSession.log("Value count " + data.size() + " not equal to vertex count " + getVertexCount() +
					" in shape '" + this.getName() + "'. Vertex column not set.",
					LoggingType.Errors);
			return false;
			}
		VertexDataColumn v_column = null;
		boolean is_new = !vertexData.containsKey(column);
		if (!is_new){
			v_column = vertexData.get(column);
			v_column.setValues(data, false);
		}else{
			v_column = new GridVertexDataColumn(column, this, data);
			v_column.resetDataLimits(false);
			vertexData.put(column, v_column);
			v_column.addListener(this);
			}
		
		if (nmap != null) v_column.setNameMap(nmap);
		if (cmap == null) cmap = this.getDefaultColourMap();
		v_column.setColourMap(cmap);
		
		updateDataColumns();
		
		notifyListeners = false;
		AttributeSelection<String> a = (AttributeSelection<String>)attributes.getAttribute("CurrentData");
		String currentData = getCurrentColumn();
		a.setValue(currentData, false);
		notifyListeners = true;
		
		if (is_new){
			last_column_added = v_column;
			composite_alphas.put(column, 0.0);
			show_in_composite.put(column, true);
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnAdded));
		}else{
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnChanged));
			String current = this.getCurrentColumn();
			if (current == null || !current.equals(column)) return true;
			// Data type may have changed, must reset the texture
			// TODO: check for this to reduce unnecessary overhead
			setTexture();
			}
		
		return true;
	}
	
	@Override
	public boolean addVertexData(VertexDataColumn column){
		boolean is_new = !hasColumn(column.getName());
		boolean success = super.addVertexData(column);
		if (!success) return false;
		GridVertexDataColumn g_column = (GridVertexDataColumn)column;
		g_column.setParent(this);
		g_column.updateColourModel();
		if (is_new){
			composite_alphas.put(column.getName(), 0.0);
			show_in_composite.put(column.getName(), true);
			}
		String current = getCurrentColumn();
		if (current == null || !current.equals(column)) return true;
		
		setTexture();
		return true;
	}
	
	@Override
	public boolean addVertexData(String column, int data_type){
		boolean is_new = !hasColumn(column);
		boolean success = super.addVertexData(column, data_type);
		if (!success) return false;
		if (is_new){
			composite_alphas.put(column, 0.0);
			show_in_composite.put(column, true);
			}
		String current = getCurrentColumn();
		if (current == null || !current.equals(column)) return true;
		// Data type may have changed, must reset the texture
		// TODO: check for this to reduce unnecessary overhead
		setTexture();
		return true;
	}
	
	@Override
	public void removeVertexData(String column){
		super.removeVertexData(column);
		composite_alphas.remove(column);
		show_in_composite.remove(column);
	}
	
	private boolean composite_changed = false;
	
	/*****************************************************
	 * Set the flag which determines whether this volume should render as a
	 * composite of its columns {@code show == true}, or as the current column
	 * only {@code show == false}. 
	 * 
	 * @param show
	 */
	public void isComposite(boolean show){
		isComposite(show, true);
	}
	
	/*****************************************************
	 * Set the flag which determines whether this volume should render as a
	 * composite of its columns {@code show == true}, or as the current column
	 * only {@code show == false}. 
	 * 
	 * @param show
	 * @param update 		Whether to notify attribute listeners
	 */
	public void isComposite(boolean show, boolean update){
		composite_changed = isComposite() != show;
		if (!composite_changed) return;
		//composite_changed = !update;
		attributes.setValue("IsComposite", new MguiBoolean(show), update);
	}
	
	/*****************************************************
	 * Determines whether this volume is current set to render as a
	 * composite of its columns {@code true}, or as the current column
	 * only {@code false}. 
	 * 
	 */
	public boolean isComposite(){
		return ((MguiBoolean)attributes.getValue("IsComposite")).getTrue();
	}
	
	/*****************************************************
	 * Sets the alpha value for {@code column}, which determines its transparency if
	 * composite mode is set. Also fires a {@code ShapeEvent}.
	 * 
	 * @param key
	 * @param value
	 */
	public void setCompositeAlpha(String column, double value){
		setCompositeAlpha(column, value, true);
	}
		
	/*****************************************************
	 * Sets the alpha value for {@code column}, which determines its transparency if
	 * composite mode is set.
	 * 
	 * @param key
	 * @param value
	 * @param update 		Whether to notify listeners of the change
	 */
	public void setCompositeAlpha(String column, double value, boolean update){
		if (!hasColumn(column)) return;
		composite_alphas.put(column, value);
		if (update)
			fireShapeModified();
	}
	
	public double getCompositeAlpha(String column){
		if (!hasColumn(column)) return Double.NaN;
		Double result = composite_alphas.get(column);
		if (result == null) 
			return Double.NaN;
		return result;
	}
	
	/*********************************************************
	 * Sets whether to include {@code column}'s data in the composite image
	 * 
	 * @param column
	 * @param show
	 */
	public void showInComposite(String column, boolean show){
		showInComposite(column, show, true);
	}
	
	/*********************************************************
	 * Sets whether to include {@code column}'s data in the composite image.
	 * 
	 * @param column
	 * @param show
	 * @param update 			Whether to notify this volume's listeners
	 */
	public void showInComposite(String column, boolean show, boolean update){
		if (!hasColumn(column)) return;
		show_in_composite.put(column, show);
		if (update){
			last_column_changed = getVertexDataColumn(column);
			fireShapeModified();
			}
	}
	
	/******************************************************
	 * Returns whether {@code column}'s data is to be included in the composite image.
	 * 
	 * @param column
	 * @return
	 */
	public boolean showInComposite(String column){
		if (column == null || !hasColumn(column)) return false;
		Boolean alpha = show_in_composite.get(column);
		if (alpha == null) return false;
		return alpha;
	}
	
	/*****************************************************
	 * Adds a boolean mask to this volume. Set = true by default.
	 * 
	 * @param name
	 * @param mask
	 */
	public void addMask(String name, boolean[][][] mask){
		addMask(name, mask, true);
	}
	
	/*****************************************************
	 * Adds a boolean mask to this volume.
	 * 
	 * @param name
	 * @param mask
	 * @param set
	 */
	public void addMask(String name, boolean[][][] mask, boolean apply){
		//HashMap<String, boolean[][][]> masks = getMasks();
		masks.put(name, mask);
		apply_mask.put(name, apply);
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));
	}
	
	public void clearMask(String name){
		if (!getMasks().containsKey(name)) return;
		
		boolean[][][] mask = getMask(name);
		Grid3D grid = getGrid(); 
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		
		for (int i = 0; i < x_size; i++)
			for (int j = 0; j < y_size; j++)
				for (int k = 0; k < z_size; k++)
					mask[i][j][k] = false;
		
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));
	}
	
	public void invertMask(String name){
		if (!getMasks().containsKey(name)) return;
		boolean[][][] mask = getMask(name);
		Grid3D grid = getGrid(); 
		
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		
		for (int i = 0; i < x_size; i++)
			for (int j = 0; j < y_size; j++)
				for (int k = 0; k < z_size; k++)
					mask[i][j][k] = !mask[i][j][k];
		
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));
		
	}
	
	/*************************************************
	 * Determines whether the voxel enclosing {@code point} is masked.
	 * 
	 * @param p
	 * @return
	 */
	public boolean isMaskedAtPoint(Point3f point){
		Grid3D grid = this.getGrid();
		int[] voxel = grid.getEnclosingVoxel(point);
		if (voxel == null) return false;
		return isMaskedAtVoxel(voxel);
	}
	
	/*********************************************************
	 * Determines whether {@code voxel} is masked by any active mask
	 * 
	 * @param voxel
	 * @return
	 */
	public boolean isMaskedAtVoxel(int[] voxel){
		ArrayList<String> names = this.getMaskNames();
		String name;
		for (int i = 0; i < names.size(); i++){
			name = names.get(i);
			if (apply_mask.get(name) && masks.get(name)[voxel[0]][voxel[1]][voxel[2]])
				return true;
			}
		return false;
	}
	
	/*********************************************************
	 * Determines whether {@code voxel} is masked by any active mask
	 * 
	 * @param voxel
	 * @return
	 */
	public boolean isMaskedAtVoxel(int i, int j, int k){
		ArrayList<String> names = this.getMaskNames();
		String name;
		for (int a = 0; a < names.size(); a++){
			name = names.get(a);
			if (apply_mask.get(name) && masks.get(name)[i][j][k])
				return true;
			}
		return false;
	}
	
	public Point3f getCenterOfVoxel(int i, int j, int k){
		Grid3D grid = getGrid();
		return grid.getVoxelMidPoint(i, j, k);
	}
	
	public boolean isMaskApplied(String name){
		return apply_mask.get(name);
	}
	
//	public HashMap<String, MguiBoolean> getMaskSet(){
//		return (HashMap<String, MguiBoolean>)attributes.getValue("MaskSet");
//	}
	
	/**********************************************
	 * Returns an ordered list of mask names for this volume.
	 * 
	 * @return
	 */
	public ArrayList<String> getMaskNames(){
		ArrayList<String> names = new ArrayList<String>(masks.keySet());
		Collections.sort(names);
		return names;
	}
	
//	public boolean isMaskSet(String name){
//		if (getMaskSet().containsKey(name))
//			return getMaskSet().get(name).getTrue();
//		return false;
//	}
	
	/**********************************************
	 * Returns the union of all masks which are currently set.
	 * 
	 * @return
	 */
	public boolean[][][] getEffectiveMask(){
		//HashMap<String,boolean[][][]> all_masks = this.getMasks();
		ArrayList<String> names = new ArrayList<String>(masks.keySet());
		Grid3D grid = this.getGrid();
		boolean[][][] effective_mask = new boolean[grid.getSizeS()][grid.getSizeT()][grid.getSizeR()];
		for (int m = 0; m < names.size(); m++){
			if (apply_mask.get(names.get(m))){
				boolean[][][] mask = masks.get(names.get(m));
				for (int i = 0; i < grid.getSizeS(); i++)
					for (int j = 0; j < grid.getSizeT(); j++)
						for (int k = 0; k < grid.getSizeR(); k++)
							effective_mask[i][j][k] |= mask[i][j][k];
				}
			}
		return effective_mask;
	}
	
	public void setMaskApplied(String name, boolean b){
		if (!masks.containsKey(name)) return;
		apply_mask.put(name, b);
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));
	}
	
	public void removeMask(String name){
		//HashMap<String, boolean[][][]> masks = getMasks();
		if (!masks.containsKey(name)) return;
		masks.remove(name);
		//HashMap<String,MguiBoolean> apply_masks = (HashMap<String,MguiBoolean>)attributes.getValue("MaskSet");
		apply_mask.remove(name);
		attributeUpdated(new AttributeEvent(attributes.getAttribute("Masks")));
	}
	
	public boolean[][][] getMask(String name){
		//HashMap<String, boolean[][][]> masks = getMasks();
		return masks.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public void setMask(String name, boolean show){
		HashMap<String,MguiBoolean> apply_masks = (HashMap<String,MguiBoolean>)attributes.getValue("MaskSet");
		if (apply_masks.get(name) != null)
			apply_masks.get(name).setTrue(show);
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));
	}
	
	public void setMask(String name, boolean[][][] mask){
		getMasks().put(name, mask);
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));
	}
	
	/*****************************************************
	 * Sets mask <code>name</code> to a new mask which is a union between its current state and the
	 * mask specified by <code>volume</code>, where all zero-valued voxels are masked.
	 * 
	 * @param name
	 * @param volume
	 * @param column 		The data column of {@code volume} containing the mask
	 * @return
	 */
	public boolean unionMaskWithVolume(String name, Volume3DInt volume){
		String current = volume.getCurrentColumn();
		if (current == null) return false;
		return unionMaskWithVolume(name, volume, current);
	}
	
	/*****************************************************
	 * Sets mask <code>name</code> to a new mask which is a union between its current state and the
	 * mask specified by <code>volume</code>, where all zero-valued voxels are masked.
	 * 
	 * @param name
	 * @param volume
	 * @param column 		The data column of {@code volume} containing the mask
	 * @return
	 */
	public boolean unionMaskWithVolume(String name, Volume3DInt volume, String column){
		
		boolean[][][] mask = getMask(name);
		if (mask == null) return false;
		Grid3D grid = volume.getGrid();
		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(column);
		
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		
		if (mask.length != x_size ||
			mask[0].length != y_size ||
			mask[0][0].length != z_size)
			return false;
		
		long in_count = 0;
		
		for (int i = 0; i < x_size; i++)
			for (int j = 0; j < y_size; j++)
				for (int k = 0; k < z_size; k++){
					mask[i][j][k] |= GeometryFunctions.compareDouble(v_column.getDoubleValueAtVoxel(i,j,k), 0.0) == 0;
					if (mask[i][j][k]) in_count++;
					}
		
		long out_count = x_size * y_size * z_size - in_count;
		InterfaceSession.log("Mask merged with volume: " + in_count + " masked; " + out_count + " unmasked.");
		
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));
		return true;
		
	}
	
	public boolean unionMask(String name, boolean[][][] to_merge){
		
		boolean[][][] mask = getMask(name);
		if (mask == null) return false;
		if (mask.length != to_merge.length ||
			mask[0].length != to_merge[0].length ||
			mask[0][0].length != to_merge[0][0].length)
			return false;
		
		for (int i = 0; i < mask.length; i++)
			for (int j = 0; j < mask[0].length; j++)
				for (int k = 0; k < mask[0][0].length; k++)
					mask[i][j][k] &= to_merge[i][j][k];
		
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));
		
		return true;
		
	}
	
	@SuppressWarnings("unchecked")
	public void setAllMasks(boolean show){
		HashMap<String,MguiBoolean> apply_masks = (HashMap<String,MguiBoolean>)attributes.getValue("MaskSet");
		Iterator<MguiBoolean> itr = apply_masks.values().iterator();
		
		while (itr.hasNext())
			itr.next().setValue(show);
		
		if (getApplyMasks())
			attributeUpdated(new AttributeEvent(attributes.getAttribute("ApplyMasks")));

	}
	
	/****************************
	 * Applies the mask specified in params to the data in this volume.
	 * NOTE: temporary until I can get the updater to work.
	 * This function reloads data from the file filter and applies the given mask to it,
	 * whereas I'd prefer to update referenced data directly, since this is much quicker
	 * and more seamless.
	 * @param params
	 */
	public void applyMask(){
		
		
	}

	/*************************
	 * Sets an updater class, descended from Volume3DUpdater, with which to update
	 * the data in this volume's grid. All updates to Volume3DInt data for objects with
	 * live scene nodes must be performed using such an updater. Grid data must be set
	 * by reference (isByRef() = true) in order to set an updater.
	 * @param u Volume3DUpdater with which to update this volume's data
	 */
	public void setUpdater(Volume3DUpdater u){
		if (!isByRef())	return;
		updater = u;
	}
	
	/*************************
	 * Updates this volume's grid data with the current Volume3DUpdater object. Does
	 * nothing if this Volume3DInt is not set by reference (isByRef() = true), or if no
	 * updater is currently set. Updaters can be set using the setUpdater method.
	 * 
	 * @param type The type of update to perform: Values only, Colour map only, or both
	 */
	public void update(UpdateTextureType type){
		////deactivateClips();
		Volume3DTexture texture = renderer.getTexture();
		if (!isByRef() || texture == null) return;
		if (getApplyMasks())
			texture.setCurrentMask(getEffectiveMask());
		else
			texture.setCurrentMask(null);
		texture.setCurrentUpdateType(type);
		texture.setCurrentColumn((GridVertexDataColumn)getCurrentDataColumn());
		if (isComposite()) 
			texture.setCurrentComposite(this);
		else
			texture.setCurrentComposite(null);
		Grid3D grid = getGrid();
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		for (int k = 0; k < z_size; k++)
			renderer.getImageComponent().updateData(texture, k, 0, 0, 
													x_size, 
													y_size);
		////reactivateClips();
		InterfaceSession.log("Updated volume - x:" + x_size + " y:" + y_size, LoggingType.Debug);
	}
		
	public boolean isByRef(){
		return ((MguiBoolean)attributes.getValue("IsByRef")).getTrue();
	}
	
	/********************************
	 * Returns the {@linkplain Grid3D} associated with this volume.
	 * 
	 * @return
	 */
	public Grid3D getGrid(){
		return (Grid3D)shape3d;
	}
	
	/*******************************************
	 * Obtains an absolute (vertex) index for the specified voxel indices
	 * 
	 * @param voxel
	 * @return
	 */
	public int getVertexIndex(int[] voxel){
		return this.getGrid().getAbsoluteIndex(voxel);
	}
	
	/*******************************************
	 * Obtains an absolute (vertex) index for the specified voxel indices
	 * 
	 * @param voxel
	 * @return
	 */
	public int getVertexIndex(int i, int j, int k){
		return this.getGrid().getAbsoluteIndex(i,j,k);
	}
	
	/**********************************************
	 * Returns the datum from the current column at <code>index</code>.
	 * 
	 * @param index
	 * @return
	 */
	public MguiNumber getDatumAtVoxel(int[] voxel){
		String column = this.getCurrentColumn();
		if (column == null) return null;
		return getDatumAtVoxel(column, voxel);
	}
	
	/**********************************************
	 * Returns the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	public MguiNumber getDatumAtVoxel(String column, int i, int j, int k){
		return super.getDatumAtVertex(column, getVertexIndex(i, j, k));
	}
	
	/**********************************************
	 * Returns the datum from the current column at <code>index</code>.
	 * 
	 * @param index
	 * @return
	 */
	public MguiNumber getDatumAtVoxel(int i, int j, int k){
		String column = this.getCurrentColumn();
		if (column == null) return null;
		return getDatumAtVoxel(column, i, j, k);
	}
	
	/**********************************************
	 * Returns the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public MguiNumber getDatumAtVoxel(String column, int[] voxel){
		return super.getDatumAtVertex(column, getVertexIndex(voxel));
	}
	
	/**********************************************
	 * Sets the datum from the current column at <code>index</code>.
	 * 
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVoxel(int[] voxel, MguiNumber datum){
		String column = this.getCurrentColumn();
		if (column == null) return false;
		return setDatumAtVoxel(column, voxel, datum);
	}
	
	/**********************************************
	 * Sets the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVoxel(String column, int[] voxel, MguiNumber datum){
		return super.setDatumAtVertex(column, getVertexIndex(voxel), datum);
	}
	
	/**********************************************
	 * Sets the datum from the current column at <code>index</code>.
	 * 
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVoxel(int[] voxel, double datum){
		String column = this.getCurrentColumn();
		if (column == null) return false;
		return setDatumAtVoxel(column, voxel, datum);
	}
	
	/**********************************************
	 * Sets the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVoxel(String column, int[] voxel, double datum){
		return super.setDatumAtVertex(column, getVertexIndex(voxel), datum);
	}
	
	/**********************************************
	 * Sets the datum from the current column at <code>index</code>.
	 * 
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVoxel(int i, int j, int k, MguiNumber datum){
		String column = this.getCurrentColumn();
		if (column == null) return false;
		return setDatumAtVoxel(column, i, j, k, datum);
	}
	
	/**********************************************
	 * Sets the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVoxel(String column, int i, int j, int k, MguiNumber datum){
		return super.setDatumAtVertex(column, getVertexIndex(i, j, k), datum);
	}
	
	/**********************************************
	 * Sets the datum from the current column at <code>index</code>.
	 * 
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVoxel(int i, int j, int k, double datum){
		String column = this.getCurrentColumn();
		if (column == null) return false;
		return setDatumAtVoxel(column, i, j, k, datum);
	}
	
	/**********************************************
	 * Sets the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVoxel(String column, int i, int j, int k, double datum){
		return super.setDatumAtVertex(column, getVertexIndex(i, j, k), datum);
	}
	
	/**********************************************
	 * Returns the datum from the current column for the voxel enclosing {@code point}.
	 * 
	 * @param point
	 * @return The value, or {@code Double.NaN} is {@code point} is not enclosed by this volume
	 */
	public MguiNumber getDatumAtPoint(Point3f point){
		String column = this.getCurrentColumn();
		if (column == null) return null;
		return getDatumAtPoint(column, point);
	}
	
	/**********************************************
	 * Returns the datum from the {@code column} for the voxel enclosing {@code point}.
	 * 
	 * @param column
	 * @param point
	 * @return The value, or {@code null} if {@code point} is not enclosed by this volume
	 */
	public MguiNumber getDatumAtPoint(String column, Point3f point){
		Grid3D grid = this.getGrid();
		int[] voxel = grid.getEnclosingVoxel(point);
		if (voxel == null)
			return null;
			
		return getDatumAtVoxel(column, voxel);
	}
	
	/**********************************************
	 * Returns a value which is interpolated from the voxel enclosing <code>p</code> and its nearest neighbours.
	 * Interpolation is an average weighted by the inverse distance of the center points of these neighbouring
	 * voxels to <code>p</code>. Uses the current data column.
	 * 
	 * @param p The geometric point at which to sample
	 * @param t The time, if this is a stack of time points
	 * @return The sampled value, or <code>Double.NaN</code> if this point is not within the bounds of this grid
	 */
	public double getInterpolatedValueAtPoint(Point3f p, int t){
		String column = this.getCurrentColumn();
		if (column == null) return Double.NaN;
		return getInterpolatedValueAtPoint(column, p, t);
	}
	
	/**********************************************
	 * Returns a value which is interpolated from the voxel enclosing <code>p</code> and its nearest neighbours.
	 * Interpolation is an average weighted by the inverse distance of the center points of these neighbouring
	 * voxels to <code>p</code>. Time point is assumed to be 1. Uses the current data column.
	 * 
	 * @param p The geometric point at which to sample
	 * @return The sampled value, or <code>Double.NaN</code> if this point is not within the bounds of this grid
	 */
	public double getInterpolatedValueAtPoint(Point3f p){
		String column = this.getCurrentColumn();
		if (column == null) return Double.NaN;
		return getInterpolatedValueAtPoint(column, p, 1);
	}
	
	/**********************************************
	 * Returns a value which is interpolated from the voxel enclosing <code>p</code> and its nearest neighbours.
	 * Interpolation is an average weighted by the inverse distance of the center points of these neighbouring
	 * voxels to <code>p</code>. Time point is assumed to be 1. Uses the current data column.
	 * 
	 * @param column The vertex data column
	 * @param p The geometric point at which to sample
	 * @return The sampled value, or <code>Double.NaN</code> if this point is not within the bounds of this grid
	 */
	public double getInterpolatedValueAtPoint(String column, Point3f p){
		return getInterpolatedValueAtPoint(column, p, 1);
	}
	
	/**********************************************
	 * Returns a value which is interpolated from the voxel enclosing <code>p</code> and its nearest neighbours.
	 * Interpolation is an average weighted by the inverse distance of the center points of these neighbouring
	 * voxels to <code>p</code>.
	 * 
	 * @param column The vertex data column
	 * @param p The geometric point at which to sample
	 * @param t The time, if this is a stack of time points
	 * @return The sampled value, or <code>Double.NaN</code> if this point is not within the bounds of this grid
	 */
	public double getInterpolatedValueAtPoint(String column, Point3f p, int t){
		
		Grid3D grid = this.getGrid();
		if (!grid.contains(p)) return Double.NaN;
		
		int[] coords = grid.getEnclosingVoxel(p);
		float x_geom = grid.getSDim();
		float y_geom = grid.getTDim();
		float z_geom = grid.getRDim();
		int s_size = grid.getSizeS();
		int t_size = grid.getSizeT();
		int r_size = grid.getSizeR();
		
		float voxel_delta = Math.max(x_geom / s_size, y_geom / t_size);
		voxel_delta = Math.max(voxel_delta, z_geom / r_size);
		
		double w_sum = 0;
		double val = 0;
		
		//for enclosing voxel and its neighbours, if p.distance(voxel.midpoint) <= voxel.delta
		//add to weighted average
		for (int i = Math.max(0, coords[0] - 1); i < Math.min(s_size, coords[0] + 1); i++)
			for (int j = Math.max(0, coords[1] - 1); j < Math.min(t_size, coords[1] + 1); j++)
				for (int k = Math.max(0, coords[2] - 1); k < Math.min(r_size, coords[2] + 1); k++){
					Point3f p2 = grid.getVoxelMidPoint(i, j, k);
					if (i == coords[0] && j == coords[1] && k == coords[2])
						p2.scale(1f);
					if (p.distance(p2) < voxel_delta){
						double w = (voxel_delta - p.distance(p2)) / voxel_delta;
						w_sum += w;
						double value = this.getDatumAtVoxel(column, new int[]{i, j, k, t}).getValue();
						if (Double.isInfinite(value) || Double.isNaN(value)) value = getDataMin();
						val += w * value;
						}
					}
		
		return val / w_sum;
	}
	
	/***************************************
	 * Samples <code>sample_volume</code> where its geometric bounds cross the bounds of this volume. All non-crossing
	 * voxels will be assigned a value of <code>data_min</code>. Source and target columns are the current columns of 
	 * either volume.
	 * 
	 * @param sample_grid
	 * @return
	 */
	public boolean sampleFromVolume(Volume3DInt sample_volume){
		return sampleFromVolume(sample_volume, getDataMin());
	}
	
	/***************************************
	 * Samples <code>sample_volume</code> where its geometric bounds cross the bounds of this volume. All non-crossing
	 * voxels will be assigned a value of <code>nan_value</code>. Source and target columns are the current columns of 
	 * either volume.
	 * 
	 * @param sample_grid
	 * @param nan_value Value to assign if voxel is outside {@code sample_volume}
	 * @return
	 */
	public boolean sampleFromVolume(Volume3DInt sample_volume, double nan_value){
		String current_source = getCurrentColumn();
		String current_target = sample_volume.getCurrentColumn();
		if (current_source == null){
			InterfaceSession.log("Volume3DInt.sampleFromVolume: No current source column set." , LoggingType.Errors);
			return false;
			}
		if (current_target == null){
			InterfaceSession.log("Volume3DInt.sampleFromVolume: No current target column set." , LoggingType.Errors);
			return false;
			}
		return sampleFromVolume(sample_volume, current_source, current_target, nan_value);
	}
	
	/***************************************
	 * Samples <code>sample_volume</code> where its geometric bounds cross the bounds of this volume. All non-crossing
	 * voxels will be assigned a value of <code>data_min</code>.
	 * 
	 * @param sample_grid
	 * @param source_column The column in {@code sample_volume} from which to sample.
	 * @param target_column The target column in this volume. Will be created if it does not exist; otherwise, 
	 * 						will be overwritten
	 * @return
	 */
	public boolean sampleFromVolume(Volume3DInt sample_volume, String source_column, String target_column){
		return sampleFromVolume(sample_volume, source_column, target_column, getDataMin());
	}
	
	/***************************************
	 * Samples <code>sample_volume</code> where its geometric bounds cross the bounds of this volume. All non-crossing
	 * voxels will be assigned a value of <code>nan_value</code>.
	 * 
	 * @param sample_grid
	 * @param source_column The column in {@code sample_volume} from which to sample.
	 * @param target_column The target column in this volume. Will be created if it does not exist; otherwise, 
	 * 						will be overwritten
	 * @param nan_value Value to assign if voxel is outside {@code sample_volume}
	 * @return
	 */
	public boolean sampleFromVolume(Volume3DInt sample_volume, String source_column, String target_column, double nan_value){
		
		Grid3D grid = this.getGrid();
		int s_size = grid.getSizeS();
		int t_size = grid.getSizeT();
		int r_size = grid.getSizeR();
		
		if (!hasColumn(target_column)){
			addVertexData(target_column, sample_volume.getDataType(source_column));
			}
		
		for (int i = 0; i < s_size; i++)
			for (int j = 0; j < t_size; j++)
				for (int k = 0; k < r_size; k++){
					Point3f mid_pt = grid.getVoxelMidPoint(i, j, k);
					double sample = sample_volume.getInterpolatedValueAtPoint(mid_pt);
					if (Double.isNaN(sample)){
						setDatumAtVoxel(target_column, i, j, k, nan_value);
					}else{
						setDatumAtVoxel(target_column, i, j, k, sample);
						}
					}
		return true;
		
	}
	
	/**********************************************
	 * Returns the data type of the current column. Returns -1 if no current column is set.
	 * 
	 * @return
	 */
	public int getDataType(){
		String current = this.getCurrentColumn();
		if (current == null) return -1;
		return getDataType(current);
	}
	
	/**********************************************
	 * Returns the data type of {@code column}. Returns -1 if there is no column by that name.
	 * 
	 * @return
	 */
	public int getDataType(String column){
		GridVertexDataColumn v_column = (GridVertexDataColumn)this.getVertexDataColumn(column);
		if (v_column == null) return -1;
		return v_column.getDataTransferType();
	}

	public void vertexDataColumnColourMapChanged(VertexDataColumnEvent event){
		//colour map changed, must regenerate
		GridVertexDataColumn column = (GridVertexDataColumn)event.getSource();
		
		if (!column.getName().equals(this.getCurrentColumn()))
			return; 	
		
		updateTextureColourMap();
		fireShapeModified();
		fireChildren2DModified();
	}
	
	public void vertexDataColumnChanged(VertexDataColumnEvent event){
		
		VertexDataColumn column = (VertexDataColumn)event.getSource();
		
		switch (event.type){
			case ColumnIsCurrent:
				this.setCurrentColumn(column.getName());
				if (!showData())
					showData(true);
				break;
				
			case ColumnChanged:
				updateDataColumns();
				last_column_changed = (VertexDataColumn)event.getSource();
				if (column.getName().equals(this.getCurrentColumn()))
					this.updateTextureValues();
				fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnChanged));
				//fireChildren2DModified();
				break;
			}
	}
	
	public void setMapMax(float m){
		if (m == getMapMax()) return;
		textureSet = false;
		attributes.setValue("MapMax", new MguiFloat(m));
	}
	
	public float getMapMax(){
		return ((MguiFloat)attributes.getValue("MapMax")).getFloat();
	}
	
	public void setMapMin(float m){
		if (m == getMapMin()) return;
		textureSet = false;
		attributes.setValue("MapMin", new MguiFloat(m));
	}
	
	public float getMapMin(){
		return ((MguiFloat)attributes.getValue("MapMin")).getFloat();
	}
	
	public void setGrid(Grid3D grid){
		setGrid(grid, true);
	}
	
	public void setGrid(Grid3D grid, boolean fire){
		if (grid == null) return;
		shape3d = grid;
		if (renderer == null)
			renderer = new Volume3DRenderer(this);
		
		updateShape();
		
		if (fire){
			setScene3DObject();
			fireShapeModified();
			}
	}
	
	//renderer updates based upon camera angle
	public void registerCameraListener(Camera3D c){
		if (renderer != null)
			c.addListener(renderer);
	}
	
	public void deregisterCameraListener(Camera3D c){
		if (renderer != null)
			c.removeListener(renderer);
	}
	
	public Camera3DListener getCameraListener(){
		return renderer;
	}
	
	/*******************************************
	 * Resets the texture for this volume's renderer.
	 * 
	 */
	public void setTexture(){
		setTexture(false);
	}
	
	/*******************************************
	 * Resets the texture for this volume's renderer. 
	 * 
	 * @param setModel - Also resets the colour model
	 */
	public void setTexture(boolean setModel){
		
		if (!is_auxiliary && (getModel() == null || !getModel().isLive3D()))
			return;
		
		//TODO: throw exception if colour map is null
		ColourMap map = getColourMap();
		String column = this.getCurrentColumn();
		if (setModel && map != null && column != null){
			renderer.setTexture(map);
			textureSet = true;
			return;
			}
		renderer.setTexture();
		textureSet = true;
		
	}
	
	/*******************************************
	 * Texture values have changed, so the texture must be updated. Does not reset the colour model; use
	 * {@linkplain updateTextureColourMap} to do this.
	 * 
	 */
	public void updateTextureValues(){
		
		if (getCurrentColumn() == null) return;
		
		if (this.isByRef() && !composite_changed){
			update(UpdateTextureType.Values);
			return;
			}
		
		// If the composite status has changed, we need to regenerate the ImageComponent entirely
		if (composite_changed && renderer.getTexture() != null){
			renderer.getTexture().setFromVolume(this);
			composite_changed = false;
			return;
			}
		
		composite_changed = false;
		
		if (renderer.getTexture() == null)
			this.setTexture();
		
		renderer.getTexture().updateFromVolume(this);
		
	}
	
	/*******************************************
	 * Texture values have changed, so the texture must be updated. Does not reset the colour model; use
	 * {@linkplain updateTextureColourMap} to do this.
	 * 
	 */
	public void updateTextureColourMap(){
		
		if (getCurrentColumn() == null) return;
		
		if (this.isByRef() && !composite_changed){
			update(UpdateTextureType.ColourMap);
			return;
			}
		
		// If the composite status has changed, we need to regenerate the ImageComponent entirely
		if (composite_changed && renderer.getTexture() != null){
			renderer.getTexture().setFromVolume(this);
			composite_changed = false;
			return;
			}
		
		composite_changed = false;
		
		//deactivateClips();
		if (renderer.getTexture() == null)
			this.setTexture();
		
		renderer.getTexture().updateFromVolume(this);
		//reactivateClips();
		
	}
	
	/*******************************************
	 * Texture has changed, so the texture must be updated. Also resets the colour model.
	 * 
	 */
	public void updateTexture(){
		
		if (!is_auxiliary && (getModel() == null || !getModel().isLive3D()))
			return;
		
		if (getCurrentColumn() == null) return;
		
		// Deactivate clip nodes to avoid yet another Java3D bug
		//deactivateClips();
		
		if (this.isByRef() && !composite_changed){
			update(UpdateTextureType.All);
			//reactivateClips();
			fireChildren2DModified();
			return;
			}
		
		// If the composite status has changed, we need to regenerate the ImageComponent entirely
		if (composite_changed && renderer.getTexture() != null){
			textureSet = false;
			setScene3DObject(true);
			this.update(UpdateTextureType.All);
			//reactivateClips();
			fireChildren2DModified();
			composite_changed = false;
			return;
			}
		
		composite_changed = false;
		
		if (renderer.getTexture() == null)
			this.setTexture();
		
		renderer.getTexture().updateFromVolume(this);
		//reactivateClips();
		
		fireChildren2DModified();
	}
	
	/************************************************
	 * Returns the colour model for the current column, or {@code null} if there is no current
	 * column.
	 * 
	 * @return
	 */
	public WindowedColourModel getColourModel(){
		String column = this.getCurrentColumn();
		if (column == null) return null;
		return getColourModel(column);
	}
	
	public WindowedColourModel getColourModel(String column){
		GridVertexDataColumn v_column = (GridVertexDataColumn)getVertexDataColumn(column);
		if (v_column == null){
			InterfaceSession.log("Volume3DInt.getColourModel: No column named '" + column + "'.", LoggingType.Errors);
			return null;
			}
		WindowedColourModel model = v_column.getColourModel();
		if (model == null){
			v_column.setColourMap(getDefaultColourMap(), true);
			model = v_column.getColourModel();
			if (model == null){
				InterfaceSession.log("Volume3DInt.getColourModel: Model is still null; WTF?.", LoggingType.Errors);
				return null;
				}
			}
		return model;
	}
	
	public void attributeUpdated(AttributeEvent e){
		if (!notifyListeners) return;
		
		if (e.getAttribute().getName().equals("IsComposite")){
			composite_changed = true;
			this.updateTexture();
			return;
			}
		
		if (e.getAttribute().getName().equals("CurrentData")){
			updateTextureValues();
			updateChildren2D(e.getAttribute());
			return;
			}
		
		if (e.getAttribute().getName().equals("RenderMode")){
			String new_mode = e.getAttribute().getValueStr();
			renderer.setRenderMode(new_mode);
			setScene3DObject();
			return;
			}
		
		if (e.getAttribute().getName().equals("ApplyMasks")){
			//textureSet = false;
			updateTextureValues();
			//setScene3DObject();
			fireShapeModified();
			return;
			}
		if (e.getAttribute().getName().equals("3D.HasAlpha")){
			// Update columns to reflect change
			for (int i = 0; i < data_columns.size(); i++){
				GridVertexDataColumn v_column = (GridVertexDataColumn)getVertexDataColumn(data_columns.get(i));
				v_column.updateColourModel();
				}
			updateTextureColourMap();
			return;
			}
		if (e.getAttribute().getName().equals("3D.Alpha")){
			if (!hasAlpha()) return;
			InterfaceSession.log(renderer.toString());
			InterfaceSession.log(e.getAttribute().getValue().toString());
			renderer.setTransparency(((MguiFloat)e.getAttribute().getValue()).getFloat());
			fireShapeModified();
			return;
			}
		if (e.getAttribute().getName().equals("ColourMap")){
			updateTextureColourMap();
			//this.updateSceneNode();
			updateChildren2D(e.getAttribute());
			return;
			}
		super.attributeUpdated(e);
	}
	
	/*****************************************************
	 * Sets whether currently set masks are to be applied or not.
	 * 
	 * @return
	 */
	public void setApplyMasks(boolean b){
		textureSet = false;
		attributes.setValue("ApplyMasks", new MguiBoolean(b));
	}
	
	/*****************************************************
	 * Whether currently set masks are to be applied or not.
	 * 
	 * @return
	 */
	public boolean getApplyMasks(){
		return ((MguiBoolean)attributes.getValue("ApplyMasks")).getTrue();
	}
	
	public Volume3DTexture getTexture(){
		if (!this.textureSet) return null;
		return renderer.getTexture();
	}
	
	boolean setScene3DObjectDebug(boolean make_live){
		if (!debug) return false;
		
		super.setScene3DObject(false);
		if (scene3DObject == null) return false;
		if (shape3d == null || !this.isVisible()){
			if (make_live) setShapeSceneNode();
			return true;
			}
		
		return true;
	}
	
	public void setBounds(Box3D box){
		
		Grid3D grid = getGrid();
		grid.setBounds(box);
		updateShape();
		((ShapeSet3DInt)getParentSet()).updateShape();
		setScene3DObject();
		fireShapeModified();
		
	}
	
	@Override
	public void updateShape() {
		super.updateShape();
		
		// Update colour models to ensure alpha is set correctly
		ArrayList<String> names = getVertexDataColumnNames();
		for (int i = 0; i < names.size(); i++){
			((GridVertexDataColumn)getVertexDataColumn(names.get(i))).updateColourModel();
			}
			
		
		if (getCurrentColumn() == null) return;
		
		// Re-establish texture...
		if (!isByRef()){
			setTexture(false);
		}else{
			update(UpdateTextureType.All);
			}
		
	}
	
	//updates the scene node by calling this object as an updater
	protected void updateSceneNode(){
		if (this.scene3DObject == null) return;
		
		Grid3D grid = getGrid();
		if (grid == null) return;
		
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		
		//call updater on all images
		for (int index = 0; index < z_size; index++)
			this.renderer.getImageComponent().updateData(this, index, 0, 0, x_size, y_size);
		
	}
	
	/*******************************************
	 * Updates the underlying images for by-reference textures 
	 * 
	 */
	@Override
	public void updateData(ImageComponent3D imageComponent,
					       int index,
					       int x, int y,
					       int width, int height){
		
		Volume3DTexture texture = renderer.getTexture();
		if (texture == null) return;
		
		texture.setFromVolume(this, true);
		
	}
	
	@Override
	public void setScene3DObject(boolean make_live){
		
		// Don't build node unless it's necessary
		if (!is_auxiliary && (getModel() == null || !getModel().isLive3D()))
			return;
		
		if (setScene3DObjectDebug(make_live)) return;
		
		//set the texture3D object and its appearance, etc.
		super.setScene3DObject(false);
		if (scene3DObject == null) return;
		if (shape3d == null || getCurrentColumn() == null || !this.isVisible() || !this.show3D()){
			if (make_live) setShapeSceneNode();
			return;
			}
		
		//deactivateClips();
		if (render_node != null) render_node.detach();
		if (box_node != null) box_node.detach();
		
		if (group_node != null){
			group_node.removeAllChildren();
		}else{
			group_node = new BranchGroup();
			group_node.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			group_node.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			group_node.setCapability(BranchGroup.ALLOW_DETACH);
			}
		
		if (!ShapeFunctions.nodeHasChild(scene3DObject, group_node))
			scene3DObject.addChild(group_node);
		
		//get node from renderer
		if (!textureSet)
			setTexture();
		
		renderer.setAlpha = hasAlpha();
		renderer.alpha = this.getAlpha();
		renderer.setRenderMode(getRenderMode());
		render_node = renderer.getNode();
		
		group_node.addChild(render_node);
		
		if (((MguiBoolean)attributes.getValue("3D.ShowPolygon")).getTrue()){
			Color clr = (Color)attributes.getValue("3D.PolygonColour");
			Box3DInt box = new Box3DInt(getBox());
			box.setAttribute("3D.LineColour", clr);
			box.isAuxiliaryShape(true);
			box_node = box.getScene3DObject();
			box_node.detach();
			group_node.addChild(box_node);
			}
		
		if (make_live) setShapeSceneNode();
		
	}
	
//	@Override
//	public float getAlpha(){
//		
//		if (this.isComposite()) return super.getAlpha();
//		
//		WindowedColourModel c_model = this.getColourModel();
//		if (c_model == null) return super.getAlpha();
//		
//		return (float)c_model.getAlpha();
//		
//	}
//	
//	@Override 
//	public void setAlpha(float alpha){
//		if (this.isComposite()) super.setAlpha(alpha);
//		
//		WindowedColourModel c_model = this.getColourModel();
//		if (c_model == null) super.setAlpha(alpha);
//		
//		c_model.setAlpha(alpha);
//		updateTextureColourMap();
//	}
	
	
	/*************************************************
	 * Returns the current render mode of this volume
	 * 
	 * @return
	 */
	public String getRenderMode(){
		if (renderer == null) return "?";
		return renderer.getRenderModeStr();
	}
	
	/*************************************************
	 * Sets the current render mode of this volume
	 * 
	 * @param mode
	 */
	public void setRenderMode(String mode){
		attributes.getAttribute("RenderMode").setValue(mode);
	}
	
	public Box3D getBox(){
		//return ((Grid3D)thisShape).bounds;
		return (Box3D)getShape();
	}
	
	@Override
	public boolean hasAlpha(){
		return super.hasAlpha();
//		GridVertexDataColumn column = (GridVertexDataColumn)getCurrentDataColumn();
//		if (column == null) return super.hasAlpha();
//		WindowedColourModel model = column.getColourModel();
//		if (model == null) return super.hasAlpha();
//		return model.hasAlpha();
	}
	
	@Override
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist, Matrix4d transform){
		//System.out.print("Volume3DInt: create 2D image..");
		boolean setAlpha = ((MguiBoolean)attributes.getValue("2D.HasAlpha")).getTrue();
		
		Shape2DInt shape2D = VolumeFunctions.getIntersectionImage(this, plane, setAlpha);
		
		if (shape2D == null){
			return null;
			}
		
		ShapeFunctions.setAttributesFrom3DParent(shape2D, this, inheritAttributesFromParent());
		
		//show intersection polygon in 3D
		if (intRect3D != null)
			intRect3D.detach();
		
		return shape2D;
	}
	
	@Override
	public void colourMapChanged(ColourMap map){
		
	}
	
	@Override
	public void hasAlpha(boolean b){
		if (b == this.hasAlpha()) return;
		super.hasAlpha(b);
		//Update colour models
		
	}
	
	public void hasAlpha(String column, boolean b){
		GridVertexDataColumn v_column = (GridVertexDataColumn)getVertexDataColumn(column);
		if (v_column == null || v_column.getColourModel() == null) return;
		v_column.getColourModel().setHasAlpha(b);
	}
	
	public String toString(){
		return "Volume3D [" + getName() + "][" + String.valueOf(ID) + "]"; 
	}
	
		
	/********************** VARIABLE STUFF ********************************/
	
	/***************************
	 * Returns the value of a variable in this object, at the specified element location. If <code>element</code>
	 * is not of length 3 or 4, a value of <code>Double.NaN</code> is returned: {i, j, k, t}. If <code>element</code> 
	 * is of length 3, the value of <code>t</code> is set to zero. 
	 * 
	 * @param variable Name of the variable
	 * @param element 
	 * @return
	 */
//	public double getVariableValue(String variable, int[] element) {
//		// TODO: Modify for time element... 
//		GridVertexDataColumn column = (GridVertexDataColumn)getVertexDataColumn(variable);
//		MguiNumber v = column.getValueAtVertex(element[0]);
//		if (v == null) return Double.NaN; 
//		return v.getValue();
//		//return column.getDoubleValueAtVoxel(element[0], element[1], element[2]);
//	}

	/****************************
	 * Sets the variable's values with the <code>values</code> object, which must be one of the following types:
	 * 
	 * <ul>
	 * <li><code>ArrayList&lt;BufferedImage[]></code>
	 * <li><code>DataBufferDouble</code>
	 * </ul>
	 * 
	 * If not, a value of <code>false</code> is returned.
	 * 
	 * @param variable		The variable to update
	 * @param values		An <code>Object</code> containing the new data
	 */
//	public boolean setVariableValues(String variable, Object values){
//		return setVariableValues(variable, values, null);
//	}
	
	/****************************
	 * Sets the variable's values with the <code>values</code> object, which must be one of the following types:
	 * 
	 * <ul>
	 * <li><code>ArrayList&lt;BufferedImage[]></code>
	 * <li><code>DataBufferDouble</code>
	 * </ul>
	 * 
	 * If not, a value of <code>false</code> is returned.
	 * 
	 * @param variable		The variable to update
	 * @param values		An <code>Object</code> containing the new data
	 * @param selection		Not implemented
	 */
//	public boolean setVariableValues(String variable, Object values, VertexSelection selection){
//		try{
//			if (values instanceof double[])
//				return super.setVariableValues(variable, values, selection);
//			
//			//TODO: use selection to apply masks
//			if (values instanceof ArrayList<?>){
//				VertexDataColumn column = getVertexDataColumn(variable);
//				column.setValues((ArrayList<MguiNumber>)values);
//				return true;
//				}
//			
//		}catch (ClassCastException ex){
//			InterfaceSession.log("Invalid value array passed to Mesh3DInt.setVariableValues()");
//			ex.printStackTrace();
//			}
//		return false;
//		
//	}
	
//	@Override
//	public Class<?> getVariableType(){
//		ArrayList<BufferedImage[]> images = new ArrayList<BufferedImage[]>();
//		return images.getClass();
//	}
	
//	public boolean supportsVariableType(Class<?> type){
//		//currently only supports an ArrayList of image stacks
//		if (getVariableType().isAssignableFrom(type)) return true;
//		
//		return false;
//	}

//	public void setCurrentColumn(String key){
//		
//	}
//	
//	public void setCurrentColumn(String key, boolean update){
//	}
//	
//	public void setCurrentColumn(String key, double min, double max, boolean update){
//	}
	
	protected boolean xml_is_reading_header = false;
	protected boolean xml_is_reading_composite_order = false;
	protected ArrayList<String> xml_composite_column_order;
	protected HashMap<String,Double> xml_composite_alphas;
	protected HashMap<String,Boolean> xml_show_in_composite;
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException{
	
		xml_current_type = type;
		
		if (xml_is_reading_composite_order){
			if (localName.equals("Column")){
				String name = attributes.getValue("name");
				boolean show = Boolean.valueOf(attributes.getValue("show"));
				double alpha = Double.valueOf(attributes.getValue("alpha"));
				xml_composite_column_order.add(name);
				xml_show_in_composite.put(name, show);
				xml_composite_alphas.put(name, alpha);
				return;
				}
			throw new SAXException("Volume3DInt.handleXMLElementEnd: Only elements of type 'Column' are expected in a " +
								   "CompositeOrder block; found '" + localName + "'.");
			}
		
		if (localName.equals("CompositeOrder")){
			xml_composite_column_order = new ArrayList<String>();
			xml_composite_alphas = new HashMap<String,Double>();
			xml_show_in_composite = new HashMap<String,Boolean>();
			xml_is_reading_composite_order = true;
			return;
			}
		
		if (localName.equals("GridVertexDataColumn")){
			if (!xml_is_vertex_data)
				throw new SAXException("InterfaceShape.handleXMLElementStart: Vertex data column must occur " +
									   "within a VertexData block..");
			
			xml_current_column = new GridVertexDataColumn(attributes.getValue("name"), this);
			((GridVertexDataColumn)xml_current_column).setXMLRoot(xml_root_dir);
			xml_current_column.handleXMLElementStart(localName, attributes, type);
			return;
			}
	
		
		// Special handling for volumes
		if (localName.equals("InterfaceShape")){
			this.setName(attributes.getValue("name"));
			xml_composite_column_order = null;
			xml_composite_alphas = null;
			xml_show_in_composite = null;
			
			switch (xml_current_type){
			
				case Full:
					// Shape is fully encoded in the XML source, prepare for accepting the data, which
					// must be handled by this object's shape's handleXMLString function.
					//xml_current_encoding = XMLFunctions.getEncodingForStr(attributes.getValue("encoding"));
					
					return;
					
				case Reference:
					// Read header information only; vertex data will read elsewhere
					xml_is_reading_header = true;
					return;
					
				default:
					return;
				}
			}
		
		if (xml_is_reading_header){
			// Read header information from XML
			this.getGrid().handleXMLElementStart(localName, attributes, type);
			return;
			}
		
		super.handleXMLElementStart(localName, attributes, type);
		
	}
	
	
	@Override
	public void handleXMLElementEnd(String localName) throws SAXException{
		
		if (xml_is_reading_header){
			// Reading header information from XML
			this.getGrid().handleXMLElementEnd(localName);
			if (localName.equals(getGrid().getLocalName()))
				xml_is_reading_header = false;
			return;
			}
		
		if (xml_current_column != null){
			if (localName.equals("GridVertexDataColumn")){
				xml_current_column.handleXMLElementEnd(localName);
				// Clone the current colour model to preserve its pre-set range
				WindowedColourModel c_model = (WindowedColourModel)((GridVertexDataColumn)xml_current_column).getColourModel().clone();
				addVertexData(xml_current_column);
				WindowedColourModel c_model2 = ((GridVertexDataColumn)xml_current_column).getColourModel();
				c_model2.setLimits(c_model.getLimits());
				xml_current_column = null;
				return;
				}
			xml_current_column.handleXMLElementEnd(localName);
			return;
			}
		
		if (localName.equals("CompositeOrder")){
			if (!xml_is_reading_composite_order)
				throw new SAXException("Volume3DInt.handleXMLElementEnd: CompositeOrder ended without being started.");
			xml_is_reading_composite_order = false;
			return;
			}
		
		if (localName.equals("InterfaceShape")){
			if (xml_composite_column_order != null){
				// Set composite ordering
				composite_column_order = xml_composite_column_order;
				composite_alphas = xml_composite_alphas;
				show_in_composite = xml_show_in_composite;
				}
			super.handleXMLElementEnd(localName);
			return;
			}
		
		super.handleXMLElementEnd(localName);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
	
		XMLOutputOptions shape_options = null;
		ShapeModel3DOutputOptions model_options = null;
		if (options instanceof ShapeModel3DOutputOptions){
			model_options = (ShapeModel3DOutputOptions)options;
			shape_options = model_options.shape_xml_options.get(this);
			if (shape_options == null)
				throw new IOException("InterfaceShape: no XML options defined for shape " + this.getFullName() + ".");
		}else{
			shape_options = options;
			}
		
		XMLType type = shape_options.type;
		
		//if "normal" type, check whether this shape has a source URL
		//if so, write only the reference to XML
		//otherwise, write fully
		if (type.equals(XMLType.Normal)){
			String url = getSourceURL();
			if (url != null && url.length() > 0 && !url.equals("n/a"))
				type = XMLType.Reference;
			else
				type = XMLType.Full;
			}
		
		if (type != XMLType.Reference){
			writeFullXML(tab, writer, options, progress_bar);
			return;
			}

		// By-reference writing is special for this shape; i.e., reference files
		// for each data column rather than the shape itself
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		String _tab3 = XMLFunctions.getTab(tab + 2);
		
		// Shape
		writer.write(_tab + "<InterfaceShape \n" + 
					 _tab2 + "class = '" + getClass().getCanonicalName() + "'\n" +
					 _tab2 + "name = '" + getName() + "'\n" +
					 _tab2 + "type = '" + XMLFunctions.getXMLStrForType(XMLType.Reference) + "'\n" + 
					 _tab + ">\n");
		
		// Write geometry
		getGeometry().writeXML(tab + 1, writer, shape_options, progress_bar);
		writer.write("\n");
		
		// Write columns
		writer.write("\n" + _tab2 + "<VertexData>\n");
		
		ArrayList<String> names = getVertexDataColumnNames();
		for (int i = 0; i < names.size(); i++){
			GridVertexDataColumn column = (GridVertexDataColumn)getVertexDataColumn(names.get(i));
			column.writeXML(tab + 2, writer, options, progress_bar);
			}
		
		writer.write("\n" + _tab2 + "</VertexData>\n");
		
		// Composite column order & alphas, if they exist
		writer.write(_tab2 + "<CompositeOrder>\n");
		if (composite_column_order.size() > 0){
			for (int i = 0; i < composite_column_order.size(); i++)
				writer.write(_tab3 + "<Column name='" + composite_column_order.get(i) +
										   "' show='" + show_in_composite.get(composite_column_order.get(i)) +
										   "' alpha='" + composite_alphas.get(composite_column_order.get(i)) + "' />\n");
			}
		writer.write(_tab2 + "</CompositeOrder>\n");
		
		// Masks
		
		
		// Write attributes
		if (attributes != null){
			attributes.writeXML(tab + 1, writer, progress_bar);
			}
		
		//close up
		writer.write("\n" + _tab + "</InterfaceShape>");
		
	}
	
	// Same as InterfaceShape, but adds a composite columns block
	protected void writeFullXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
			
			XMLOutputOptions shape_options = null;
			ShapeModel3DOutputOptions model_options = null;
			if (options instanceof ShapeModel3DOutputOptions){
				model_options = (ShapeModel3DOutputOptions)options;
				shape_options = model_options.shape_xml_options.get(this);
				if (shape_options == null)
					throw new IOException("InterfaceShape: no XML options defined for shape " + this.getFullName() + ".");
			}else{
				shape_options = options;
				}
			
			XMLType type = shape_options.type;
			
			String _tab = XMLFunctions.getTab(tab);
			String _tab2 = XMLFunctions.getTab(tab + 1);
			String _tab3 = XMLFunctions.getTab(tab + 2);
			String _type = XMLFunctions.getXMLStrForType(type);

			writer.write(_tab + "<InterfaceShape \n" + 
					_tab2 + "class = '" + getClass().getCanonicalName() + "'\n" +
					_tab2 + "name = '" + getName() + "'\n" +
					_tab2 + "type = '" + _type + "'\n" + 
					_tab + ">\n");
				
			//Shape writes its XML here if necessary
			switch (type){
				case Full:
					//writer.write("\n");
					getGeometry().writeXML(tab + 1, writer, shape_options, progress_bar);
					writer.write("\n");
					break;
				case Short:
					//writer.write("\n");
					writer.write(getGeometry().getShortXML(tab + 1));
					writer.write("\n");
					break;
				default:
					break;
				}
			
			
			// Masks
			
			// Vertex data here
			if (type == XMLType.Full && this.vertexData.size() > 0){
				
				writer.write(_tab2 + "<VertexData>\n");
				
				ArrayList<String> names = getVertexDataColumnNames();
				for (int i = 0; i < names.size(); i++){
					VertexDataColumn column = getVertexDataColumn(names.get(i));
					column.writeXML(tab + 2, writer, shape_options, progress_bar);
					}
				
				writer.write(_tab2 + "</VertexData>\n");
				}
			
			// Composite column order & alphas, if they exist
			writer.write(_tab2 + "<CompositeOrder>\n");
			if (composite_column_order.size() > 0){
				for (int i = 0; i < composite_column_order.size(); i++)
					writer.write(_tab3 + "<Column name='" + composite_column_order.get(i) +
											   "' show='" + show_in_composite.get(composite_column_order.get(i)) +
											   "' alpha='" + composite_alphas.get(composite_column_order.get(i)) + "' />\n");
				}
			writer.write(_tab2 + "</CompositeOrder>\n");
			
			// Attributes are last because they may need to load last
			if (attributes != null){
				attributes.writeXML(tab + 1, writer, progress_bar);
				}
			
			//close up
			writer.write("\n" + _tab + "</InterfaceShape>");
			
	}
	
}