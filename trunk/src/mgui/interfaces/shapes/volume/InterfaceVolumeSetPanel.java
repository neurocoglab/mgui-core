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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import mgui.geometry.Grid3D;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.VolumeSet3DInt;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.stats.HistogramPlot;
import mgui.numbers.MguiDouble;
import mgui.util.JMultiLineToolTip;

/**********************************************************************
 * Panel to provide an interface with a volume overlay set.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

@SuppressWarnings("serial")
public class InterfaceVolumeSetPanel extends InterfacePanel
									 implements DocumentListener,
									 			FocusListener,
									 			ActionListener,
									 			ShapeListener{

	
	CategoryTitle lblGrid = new CategoryTitle("OVERLAY SET");
	JLabel lblOverlaySet = new JLabel("Overlay set:");
	InterfaceComboBox cmbOverlaySet = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblParentSet = new JLabel("Parent Set:");
	InterfaceComboBox cmbParentSet = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	
	//Origin
	CategoryTitle lblSpecs = new CategoryTitle("GEOMETRY"); 
	JLabel lblOrigin = new JLabel("<html><b>ORIGIN</b></html>");
	JLabel lblOriginX = new JLabel(" X"); 
	JTextField txtOriginX = new JTextField("0");
	
	JLabel lblOriginY = new JLabel(" Y"); 
	JTextField txtOriginY = new JTextField("0");
	JLabel lblOriginZ = new JLabel(" Z"); 
	JTextField txtOriginZ = new JTextField("0");
	
	//Dimensions
	JLabel lblDimGeom = new JLabel("<html><b>DIMENSIONS</b></html>");
	JLabel lblGeomX = new JLabel(" X"); 
	JTextField txtGeomX = new JTextField("0");
	JLabel lblGeomY = new JLabel(" Y"); 
	JTextField txtGeomY = new JTextField("0");
	JLabel lblGeomZ = new JLabel(" Z"); 
	JTextField txtGeomZ = new JTextField("0");
	
	JCheckBox chkRatios = new JCheckBox("  Maintain ratios");
	JButton cmdDefGeometry = new JButton("Define 2D");
	
	//Axes
	JLabel lblAxisX = new JLabel("<html><b>X-AXIS</b></html>");
	JButton cmdDefX = new JButton("Def");
	JLabel lblAxisX_X = new JLabel(" X"); 
	JTextField txtAxisX_X = new JTextField("1");
	JLabel lblAxisX_Y = new JLabel(" Y"); 
	JTextField txtAxisX_Y = new JTextField("0");
	JLabel lblAxisX_Z = new JLabel(" Z"); 
	JTextField txtAxisX_Z = new JTextField("0");
	JLabel lblAxisY = new JLabel("<html><b>Y-AXIS</b></html>");
	JButton cmdDefY = new JButton("Def");
	JLabel lblAxisY_X = new JLabel(" X"); 
	JTextField txtAxisY_X = new JTextField("0");
	JLabel lblAxisY_Y = new JLabel(" Y"); 
	JTextField txtAxisY_Y = new JTextField("1");
	JLabel lblAxisY_Z = new JLabel(" Z"); 
	JTextField txtAxisY_Z = new JTextField("0");
	JLabel lblAxisZ = new JLabel("<html><b>Z-AXIS</b></html>");
	JButton cmdDefZ = new JButton("Def");
	JLabel lblAxisZ_X = new JLabel(" X"); 
	JTextField txtAxisZ_X = new JTextField("0");
	JLabel lblAxisZ_Y = new JLabel(" Y"); 
	JTextField txtAxisZ_Y = new JTextField("0");
	JLabel lblAxisZ_Z = new JLabel(" Z"); 
	JTextField txtAxisZ_Z = new JTextField("1");
	
	JButton cmdUpdateSpecs = new JButton("Apply Changes");
	
	
	CategoryTitle lblData = new CategoryTitle("DATA");
	
	JLabel lblDimData = new JLabel("<html><b>DIMENSIONS</b></html>");
	JLabel lblDataX = new JLabel(" X"); 
	JTextField txtDataX = new JTextField("0");
	JLabel lblDataY = new JLabel(" Y"); 
	JTextField txtDataY = new JTextField("0");
	JLabel lblDataZ = new JLabel(" Z"); 
	JTextField txtDataZ = new JTextField("0");
	
	JLabel lblDataType = new JLabel("Data type:");
	JComboBox cmbDataType = new JComboBox();
	
	private int current_width_max = 50000, default_width_max = 50000;
	private int current_mid_min = -20000, default_mid_min = -20000;
	private int current_mid_max = 20000;
	
	CategoryTitle lblVolumes = new CategoryTitle("VOLUMES");
	OverlayVolumeTableModel volume_table_model;
	JTable tblVolumes;
	JScrollPane scrVolumes;
	JButton cmdVolumesAdd = new JButton("Add..");
	JButton cmdVolumesLoad = new JButton("Load..");
	JButton cmdVolumesRemove = new JButton("Remove");
	
	CategoryTitle lblIntensity = new CategoryTitle("INTENSITY");
	ImageViewComponent imgIntensity = new ImageViewComponent(null, 0, null);
	JLabel lblIntScale = new JLabel(" Scale:");
	JTextField txtIntScale = new JTextField("1.0");
	JLabel lblIntIntercept = new JLabel(" Intercept:");
	JTextField txtIntIntercept = new JTextField("0.0");
	JLabel lblIntWidth = new JLabel(" Contrast:");
	JSlider sldIntWidth = new JSlider(SwingConstants.HORIZONTAL, 1, current_width_max, 1);
	JLabel lblIntMid = new JLabel(" Brightness:");
	JSlider sldIntMid = new JSlider(SwingConstants.HORIZONTAL, current_mid_min, current_mid_max, 1);
	JTextField txtIntWidth = new JTextField("0.0");
	JTextField txtIntMid = new JTextField("0.0");
	JLabel lblIntAlphaMin = new JLabel(" Alpha min:");
	JTextField txtIntAlphaMin = new JTextField("0.0");
	JSlider sldIntAlphaMin = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 1);
	JLabel lblIntAlphaMax = new JLabel(" Alpha max:");
	JTextField txtIntAlphaMax = new JTextField("1.0");
	JSlider sldIntAlphaMax = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 1);
	JLabel lblIntAlpha = new JLabel(" General alpha:");
	JTextField txtIntAlpha = new JTextField("1.0");
	JSlider sldIntAlpha = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 1);
	JCheckBox chkIntSetAlpha = new JCheckBox(" Has alpha");
	JLabel lblIntColourMap = new JLabel(" Colour map:");
	InterfaceComboBox cmbIntColourMap = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  												  true, 500);
	JLabel lblIntSlice = new JLabel(" Slice:");
	JTextField txtIntSlice = new JTextField("1");
	JSlider sldIntSlice = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 1);
	
	JButton cmdIntApply = new JButton("Apply");
	JButton cmdIntReset = new JButton("Reset");
	
	
	CategoryTitle lblHistogram = new CategoryTitle("HISTOGRAM");
	
	HistogramPlot pnlHistPanel = new HistogramPlot();
	JLabel lblHistBins = new JLabel("Bins:");
	JTextField txtHistBins = new JTextField("80");
	JLabel lblHistMin = new JLabel("Min:");
	JTextField txtHistMin = new JTextField("0");
	JLabel lblHistMax = new JLabel("Max:");
	JTextField txtHistMax = new JTextField("1");
	JLabel lblHistYScale = new JLabel("Y scale:");
	JTextField txtHistYScale = new JTextField("0.5");
	JSlider sldHistYScale = new JSlider(JSlider.HORIZONTAL, 1, 10000, 1);
	JButton cmdHistReset = new JButton("Reset");
	JButton cmdHistCalc = new JButton("Apply");
	
	
	static final String NEW_OVERLAY_SET = "<-New Overlay Set->"; 
	
	boolean updateGridCombo = true;
	boolean updateIntensity = true;
	
	protected VolumeSet3DInt current_volume_set;
	protected BufferedImage[] current_composites;
	
	public InterfaceVolumeSetPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	
	@Override
	protected void init(){
		_init();
		
		//set this sucker up
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		//listen to changes in text fields
		txtGeomX.getDocument().addDocumentListener(this);
		txtGeomY.getDocument().addDocumentListener(this);
		txtGeomZ.getDocument().addDocumentListener(this);
		txtGeomX.addFocusListener(this);
		txtGeomY.addFocusListener(this);
		txtGeomZ.addFocusListener(this);
		
		txtOriginX.getDocument().addDocumentListener(this);
		txtOriginY.getDocument().addDocumentListener(this);
		txtOriginZ.getDocument().addDocumentListener(this);
		txtOriginX.addFocusListener(this);
		txtOriginY.addFocusListener(this);
		txtOriginZ.addFocusListener(this);
		
		txtAxisX_X.getDocument().addDocumentListener(this);
		txtAxisX_Y.getDocument().addDocumentListener(this);
		txtAxisX_Z.getDocument().addDocumentListener(this);
		txtAxisX_X.addFocusListener(this);
		txtAxisX_Y.addFocusListener(this);
		txtAxisX_Z.addFocusListener(this);
		
		txtAxisY_X.getDocument().addDocumentListener(this);
		txtAxisY_Y.getDocument().addDocumentListener(this);
		txtAxisY_Z.getDocument().addDocumentListener(this);
		txtAxisY_X.addFocusListener(this);
		txtAxisY_Y.addFocusListener(this);
		txtAxisY_Z.addFocusListener(this);
		
		txtAxisZ_X.getDocument().addDocumentListener(this);
		txtAxisZ_Y.getDocument().addDocumentListener(this);
		txtAxisZ_Z.getDocument().addDocumentListener(this);
		txtAxisZ_X.addFocusListener(this);
		txtAxisZ_Y.addFocusListener(this);
		txtAxisZ_Z.addFocusListener(this);
		
		txtDataX.setActionCommand("Update Dim X");
		txtDataY.setActionCommand("Update Dim Y");
		txtDataZ.setActionCommand("Update Dim Z");
		
		updateGridList();
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblGrid, c);
		lblGrid.setParentObj(this);
		c = new CategoryLayoutConstraints("OVERLAY SET", 1, 1, 0.05, .25, 1);
		add(lblOverlaySet, c);
		c = new CategoryLayoutConstraints("OVERLAY SET", 1, 1, 0.3, .65, 1);
		add(cmbOverlaySet, c);
		c = new CategoryLayoutConstraints("OVERLAY SET", 2, 2, 0.05, .25, 1);
		add(lblParentSet, c);
		c = new CategoryLayoutConstraints("OVERLAY SET", 2, 2, 0.3, .65, 1);
		add(cmbParentSet, c);
		
		//Geometry
		c = new CategoryLayoutConstraints();
		add(lblSpecs, c);
		lblSpecs.setParentObj(this);
		c = new CategoryLayoutConstraints("GEOMETRY", 1, 1, 0.05, .9, 1);
		add(lblOrigin, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 2, 2, 0.1, .05, 1);
		add(lblOriginX, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 2, 2, 0.15, .2, 1);
		add(txtOriginX, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 2, 2, 0.4, .05, 1);
		add(lblOriginY, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 2, 2, 0.45, .2, 1);
		add(txtOriginY, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 2, 2, 0.7, .05, 1);
		add(lblOriginZ, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 2, 2, 0.75, .2, 1);
		add(txtOriginZ, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 3, 3, 0.05, .9, 1);
		add(lblDimGeom, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 4, 4, 0.1, .05, 1);
		add(lblGeomX, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 4, 4, 0.15, .2, 1);
		add(txtGeomX, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 4, 4, 0.4, .05, 1);
		add(lblGeomY, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 4, 4, 0.45, .2, 1);
		add(txtGeomY, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 4, 4, 0.7, .05, 1);
		add(lblGeomZ, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 4, 4, 0.75, .2, 1);
		add(txtGeomZ, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 5, 5, 0.05, 0.9, 1);
		add(lblAxisX, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 6, 6, 0.1, .05, 1);
		add(lblAxisX_X, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 6, 6, 0.15, .2, 1);
		add(txtAxisX_X, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 6, 6, 0.4, .05, 1);
		add(lblAxisX_Y, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 6, 6, 0.45, .2, 1);
		add(txtAxisX_Y, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 6, 6, 0.7, .05, 1);
		add(lblAxisX_Z, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 6, 6, 0.75, .2, 1);
		add(txtAxisX_Z, c);
		
		//Y-Axis
		c = new CategoryLayoutConstraints("GEOMETRY", 7, 7, 0.05, 0.9, 1);
		add(lblAxisY, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 8, 8, 0.1, .05, 1);
		add(lblAxisY_X, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 8, 8, 0.15, .2, 1);
		add(txtAxisY_X, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 8, 8, 0.4, .05, 1);
		add(lblAxisY_Y, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 8, 8, 0.45, .2, 1);
		add(txtAxisY_Y, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 8, 8, 0.7, .05, 1);
		add(lblAxisY_Z, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 8, 8, 0.75, .2, 1);
		add(txtAxisY_Z, c);
		
		//Z-Axis
		c = new CategoryLayoutConstraints("GEOMETRY", 9, 9, 0.05, 0.9, 1);
		add(lblAxisZ, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 10, 10, 0.1, .05, 1);
		add(lblAxisZ_X, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 10, 10, 0.15, .2, 1);
		add(txtAxisZ_X, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 10, 10, 0.4, .05, 1);
		add(lblAxisZ_Y, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 10, 10, 0.45, .2, 1);
		add(txtAxisZ_Y, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 10, 10, 0.7, .05, 1);
		add(lblAxisZ_Z, c);
		c = new CategoryLayoutConstraints("GEOMETRY", 10, 10, 0.75, .2, 1);
		add(txtAxisZ_Z, c);
		
		//Apply changes
		c = new CategoryLayoutConstraints("GEOMETRY", 11, 12, 0.2, .6, 1);
		add(cmdUpdateSpecs, c);
		
		//Data
		c = new CategoryLayoutConstraints();
		add(lblData, c);
		lblData.setParentObj(this);
		c = new CategoryLayoutConstraints("DATA", 1, 1, 0.05, 0.9, 1);
		add(lblDimData, c);
		c = new CategoryLayoutConstraints("DATA", 2, 2, 0.1, .05, 1);
		add(lblDataX, c);
		c = new CategoryLayoutConstraints("DATA", 2, 2, 0.15, .2, 1);
		add(txtDataX, c);
		c = new CategoryLayoutConstraints("DATA", 2, 2, 0.4, .05, 1);
		add(lblDataY, c);
		c = new CategoryLayoutConstraints("DATA", 2, 2, 0.45, .2, 1);
		add(txtDataY, c);
		c = new CategoryLayoutConstraints("DATA", 2, 2, 0.7, .05, 1);
		add(lblDataZ, c);
		c = new CategoryLayoutConstraints("DATA", 2, 2, 0.75, .2, 1);
		add(txtDataZ, c);
		
		c = new CategoryLayoutConstraints("DATA", 3, 3, 0.05, 0.3, 1);
		add(lblDataType, c);
		c = new CategoryLayoutConstraints("DATA", 3, 3, 0.35, 0.6, 1);
		add(cmbDataType, c);
		
		// Volumes
		c = new CategoryLayoutConstraints();
		add(lblVolumes, c);
		lblVolumes.setParentObj(this);
		c = new CategoryLayoutConstraints("VOLUMES", 1, 6, 0.05, 0.9, 1);
		add(scrVolumes, c);
		c = new CategoryLayoutConstraints("VOLUMES", 7, 7, 0.05, 0.3, 1);
		add(cmdVolumesAdd, c);
		c = new CategoryLayoutConstraints("VOLUMES", 7, 7, 0.37, 0.3, 1);
		add(cmdVolumesLoad, c);
		c = new CategoryLayoutConstraints("VOLUMES", 7, 7, 0.62, 0.3, 1);
		add(cmdVolumesRemove, c);
		
		// Intensity
		c = new CategoryLayoutConstraints();
		add(lblIntensity, c);
		lblIntensity.setParentObj(this);
		
		
		
		
	}
	
	protected void initVolumeSetTable(){
		this.volume_table_model = new OverlayVolumeTableModel();
		tblVolumes = new JTable(volume_table_model);
		TableColumn column = tblVolumes.getColumnModel().getColumn(2);
		column.setCellEditor(new AlphaSliderEditor());
		column.setCellRenderer(new AlphaSliderRenderer());
		column.setMinWidth(100);
		column.setPreferredWidth(200);
		column = tblVolumes.getColumnModel().getColumn(0);
		column.setMaxWidth(40);
	}
	
	protected void updateVolumeSetTable(){
		volume_table_model.setVolumeSet(current_volume_set);
		
	}
	
	protected void updateGridList(){
		updateGridCombo = false;
		boolean current_found = false;
		cmbOverlaySet.removeAllItems();
		cmbOverlaySet.addItem(NEW_OVERLAY_SET);
		
		List<Shape3DInt> volumes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new VolumeSet3DInt());
		
		for (Shape3DInt volume : volumes) {
			cmbOverlaySet.addItem(volume);
			if (current_volume_set != null && volume.equals(current_volume_set))
				current_found = true;
			}
		
		if (current_found){
			cmbOverlaySet.setSelectedItem(current_volume_set);
		}else{
			setCurrentVolumeSet(null);
			}
		
		//attrGrid3D.setAttributes(current_set.getAttributes());
		
		//update set list
		cmbParentSet.removeAllItems();
		ShapeSet3DInt current_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		cmbParentSet.addItem(current_set);
		//ShapeSet3DInt all_sets = current_set.getShapeType(current_set, true);
		List<Shape3DInt> all_sets = current_set.getShapeType(current_set);
		
		for (Shape3DInt set : all_sets) {
			cmbParentSet.addItem(set);
			}
		
		updateDisplay();
		updateGridCombo = true;
		
	}
	
	/*********************************************
	 * Set the current volume set; updates panel components to reflect the change
	 * 
	 * @param volume_set
	 */
	protected void setCurrentVolumeSet(VolumeSet3DInt volume_set){
	
		if (current_volume_set != null)
			current_volume_set.removeShapeListener(this);
		
		current_volume_set = volume_set;
		current_volume_set.addShapeListener(this);
		
		updateVolumeSetTable();
		updateComposites();
		
	}
	
	/****** ActionListener method ***********/
	
	public void actionPerformed(ActionEvent e){
		
		
		
	}
	
	/****** DocumentListener methods ********/
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		updateSpecBoxes();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {}

	@Override
	public void removeUpdate(DocumentEvent e) {}
	
	/****** FocusListener methods ********/
	
	public void focusGained(FocusEvent e) {}

	public void focusLost(FocusEvent e) {
		updateSpecBoxes();
	}
	
	private void updateSpecBoxes(){
		
		txtGeomX.setCaretPosition(0);
		txtGeomY.setCaretPosition(0);
		txtGeomZ.setCaretPosition(0);
		
		txtOriginX.setCaretPosition(0);
		txtOriginY.setCaretPosition(0);
		txtOriginZ.setCaretPosition(0);
		
		txtAxisX_X.setCaretPosition(0);
		txtAxisX_Y.setCaretPosition(0);
		txtAxisX_Z.setCaretPosition(0);
		
		txtAxisY_X.setCaretPosition(0);
		txtAxisY_Y.setCaretPosition(0);
		txtAxisY_Z.setCaretPosition(0);
		
		txtAxisZ_X.setCaretPosition(0);
		txtAxisZ_Y.setCaretPosition(0);
		txtAxisZ_Z.setCaretPosition(0);
		
	}
	
	protected void updateHistogram(){
		if (pnlHistPanel.histogram != null && pnlHistPanel.colour_model != null)
			pnlHistPanel.repaint();
	}
	
	public void shapeUpdated(ShapeEvent e){
		
		if (e.getShape() != this.current_volume_set) return;
		
		switch (e.eventType){
			case ShapeModified:
				updateComposites();
				return;
			}
		
	}


	protected void updateIntensity(){
		
		if (current_volume_set == null || !(current_volume_set.getGrid() instanceof Grid3D)){
			imgIntensity.setImage(null, 0);
			imgIntensity.setVolume(null);
			updateHistogram();
			return;
			}
		
		Grid3D grid = (Grid3D)current_volume_set.getGrid();
		
		imgIntensity.setVolume(current_volume_set);
		
		//set controls
		int n = grid.getSizeR();
		//WindowedColourModel model = (WindowedColourModel)((WindowedColourModel)grid.getColourModel()).clone();
		
		updateIntensity = false;
		sldIntSlice.setMaximum(n - 1);
		sldIntSlice.setValue(n / 2);
		
		updateIntensityValues();
		cmbIntColourMap.setSelectedItem(imgIntensity.model.getColourMap());
		updateIntensity = true;
		
		updateIntensitySlice();
		updateHistogram();
	}
	
	protected void updateComposites(){
		
		if (current_volume_set == null){
			current_composites = null;
			return;
			}
		
		current_composites = current_volume_set.generateComposites();
		
	}
	
	void updateIntensitySlice(){
		if (current_volume_set == null || !(current_volume_set.getGrid() instanceof Grid3D)){
			imgIntensity.setImage(null, 0);
			return;
			}
		
		Grid3D grid = (Grid3D)current_volume_set.getGrid();
		
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		
		if (current_composites == null)
			updateComposites();
		
		BufferedImage image =  current_composites[sldIntSlice.getValue()];
		WindowedColourModel model = imgIntensity.model;
		imgIntensity._bufferedImage = null;
		
		if (imgIntensity._bufferedImage == null){
			
			WritableRaster raster = (WritableRaster)image.getData(new Rectangle(0, 0, x_size, y_size));
			WritableRaster raster2 = model.createCompatibleWritableRaster(x_size, y_size);
			
			raster2.setDataElements(0, 0, raster);
			BufferedImage image2 = new BufferedImage(model, raster2, 
													 false, null);
			pnlHistPanel.colour_model = model;
			updateHistogram();
			//model.printToConsole();
			imgIntensity.setImage(image2, sldIntSlice.getValue());
		}else{
			
			WritableRaster raster = (WritableRaster)image.getData(new Rectangle(0, 0, x_size, y_size));
			WritableRaster raster2 = model.createCompatibleWritableRaster(x_size, y_size);
			raster2.setDataElements(0, 0, raster);
			
			imgIntensity._bufferedImage.setData(raster2);
			imgIntensity.z = sldIntSlice.getValue();
			imgIntensity.repaint();
			}
	}
	
	protected void updateIntensityValues(){
		if (imgIntensity != null && imgIntensity.model != null){
			sldIntWidth.setValue((int)(1.0 / imgIntensity.model.getWindowWidth() * 10000.0));
			sldIntMid.setValue((int)(imgIntensity.model.getWindowMid() * 10000.0));
			sldIntAlphaMax.setValue((int)(imgIntensity.model.getAlphaMax() * 10000.0));
			sldIntAlphaMin.setValue((int)(imgIntensity.model.getAlphaMin() * 10000.0));
			sldIntAlpha.setValue((int)(imgIntensity.model.getAlpha() * 10000.0));
			txtIntScale.setText(MguiDouble.getString(imgIntensity.model.getScale(), 8));
			txtIntIntercept.setText(MguiDouble.getString(imgIntensity.model.getIntercept(), 4));
			txtIntSlice.setText("" + sldIntSlice.getValue());
			chkIntSetAlpha.setSelected(imgIntensity.model.getHasAlpha());
			}
		
	}
	
	
	/** JComponent used to display a Buffered Image. */
	  private class ImageViewComponent extends JComponent implements MouseMotionListener, 
	  															 	 MouseListener {
		  
		public WindowedColourModel model;
		double last_mid, last_width, last_alpha;
		int z;
		Point click_pt;
		public Volume3DInt volume;
		
	    /** Buffered Image displayed in the JComponent. */
	    protected BufferedImage _bufferedImage;

	    /**
	     * Constructs an Image Component.
	     *
	     * @param bufferedImage Buffered Image displayed in the JComponent.
	     */
	    public ImageViewComponent(BufferedImage bufferedImage, int z, Volume3DInt volume)
	      {
	    	_bufferedImage = bufferedImage;
	    	this.volume = volume;
	    	
	    	this.z = z;
	    	this.addMouseListener(this);
	    	this.addMouseMotionListener(this);
	    	
	    	if (_bufferedImage == null) return;
	    	model = (WindowedColourModel)_bufferedImage.getColorModel();
	      }
	    
	    public JToolTip createToolTip(){
			return new JMultiLineToolTip();
		}
	    
	    public void setVolume(Volume3DInt volume){
	    	this.volume = volume;
	    	if (volume == null){
	    		this.model = null;
	    		this._bufferedImage = null;
	    		return;
	    		}
	    	this.model = (WindowedColourModel)volume.getColourModel().clone();
	    	this._bufferedImage = null;
	    }
	    
	    public void setImage(BufferedImage image, int z){
	    	_bufferedImage = image;
	    	this.z = z;
	    	this.repaint();
	    	if (_bufferedImage == null) return;
	    	//model = (WindowedColourModel)_bufferedImage.getColorModel();
	    }

		public void mouseDragged(MouseEvent e) {
			//InterfaceSession.log("Mouse dragged..");
			if (model == null) return;
			//InterfaceSession.log("Model not null..");
			
			if (SwingUtilities.isLeftMouseButton(e)){
				double diff = (double)(click_pt.getY() - e.getY()) / (double)this.getHeight();
				diff *= current_width_max / 10000;
				
				this.model.setWindowWidth(Math.min(last_width + diff, 1.0));
				this.model.setWindowWidth(Math.max(last_width + diff, 0.001));
				this.repaint();
				updateIntensityValues();
				return;
				}
			
			if (SwingUtilities.isRightMouseButton(e)){
				double diff = (double)(click_pt.getY() - e.getY()) / (double)this.getHeight();
				diff *= (current_mid_max - current_mid_min) / 10000;
				this.model.setWindowMid(last_mid + diff);
				this.model.setWindowMid(last_mid + diff);
				this.repaint();
				updateIntensityValues();
				return;
				}
			
			if (SwingUtilities.isMiddleMouseButton(e)){
				double diff = (double)(click_pt.getY() - e.getY()) / (double)this.getHeight();
				if (diff > 0)
					diff += 0;
				this.model.setAlphaMin(Math.min(last_alpha + diff, 1.0));
				this.model.setAlphaMin(Math.max(last_alpha + diff, 0));
				this.repaint();
				updateIntensityValues();
				return;
				}
			
		}

		public void mousePressed(MouseEvent e) {
			if (model == null) return;
			
			click_pt = e.getPoint();
			last_width = model.getWindowWidth();
			last_mid = model.getWindowMid();
			last_alpha = model.getAlphaMin();
			
		}
	    
	    /**
	     * Paints the image into the specified Graphics.
	     *
	     * @param g Graphics to paint into.
	     */
	    public void paint(Graphics g)
	      {
	    	Graphics2D g2d = (Graphics2D)g;
	    	g2d.setColor(Color.WHITE);
	    	g2d.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
	    	g2d.setColor(Color.BLUE);
	    	g2d.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
	    	if (_bufferedImage == null) return;
	    	g2d.drawImage(_bufferedImage, 1, 1, 
	    				  this.getWidth() - 2, this.getHeight() - 2, 
	    				  null);
	      }
	    
	    public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {
			
			if (volume == null) return;
			Grid3D grid = volume.getGrid();
			Point p = e.getPoint();
			
			double x_scale = (double)_bufferedImage.getWidth() / (double)this.getWidth();
			double y_scale = (double)_bufferedImage.getHeight() / (double)this.getHeight();
			
			int x_size = grid.getSizeS();
			int y_size = grid.getSizeT();
			
			int x = (int)Math.ceil(p.x * x_scale);
			if (x < 0) x = 0;
			if (x >= x_size) x = x_size - 1;
			int y = (int)Math.ceil((this.getHeight() - p.y) * y_scale);
			if (y < 0) y = 0;
			if (y >= y_size) y = y_size - 1;
			
			this.setToolTipText("[" + x + "," + y + "," + z + 
								"]\nVal: " + volume.getDatumAtVoxel(x, y, z));
			
		}
	    
	    
	  }
	
	
	  protected class OverlayVolumeTableModel extends DefaultTableModel{
		  
		  VolumeSet3DInt volume_set;
		  
		  public OverlayVolumeTableModel(){
			  
		  }
		  
		  public OverlayVolumeTableModel(VolumeSet3DInt volume_set){
			  this.volume_set = volume_set;
		  }
		  
		  public void setVolumeSet(VolumeSet3DInt volume_set){
			  this.fireTableDataChanged();
		  }
		  
		  @Override
		  public int getRowCount() {
			  if (volume_set == null) return 0;
			  return volume_set.getMemberCount();
		  }
		
		  @Override
		  public int getColumnCount() {
			  return 3;
		  }

		  @Override
		  public String getColumnName(int column) {
			  switch (column){
				  case 0:
					  return "Show";
				  case 1:
					  return "Volume";
				  case 3:
					  return "Alpha";
				  }
			  return "?";
		  }

		  @Override
		  public boolean isCellEditable(int row, int column) {
			  return column != 2;
		  }

		  @Override
		  public Object getValueAt(int row, int column) {
			  switch (column){
				  case 0:
					  return volume_set.getMember(column).isVisible();
				  case 1:
					  return volume_set.getMember(column);
				  case 2:
					  return volume_set.getAlpha(column);
				  }
			  return null;
		  }

		  @Override
		  public void setValueAt(Object value, int row, int column) {
			  switch (column){
				  case 0:
					  volume_set.getMember(row).setVisible((Boolean)value);
					  return;
					  
				  case 2:
					  volume_set.setAlpha(row, ((Float)value)/100f);
			  	}
		  }

		  @Override
		  public Class<?> getColumnClass(int column) {
			  switch (column){
			  case 0:
				  return Boolean.class;
			  case 1:
				  return VolumeSet3DInt.class;
			  case 2:
				  return Float.class;
			  }
		  return Object.class;
		  }

	  }
	  
	  protected class AlphaSliderRenderer extends JSlider implements TableCellRenderer {
		  
		  public AlphaSliderRenderer(){
			  super(SwingConstants.HORIZONTAL, 0, 100, 50);
			  setSize(100,15);
			  
		  }
		  
		  @Override
		  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				  										 boolean hasFocus, int row, int column) {
			  
			  if (value instanceof Float){
				  setValue((int)((Float)value * 100f));
				  }
			  
			  return this;
		  }
		  
		  
	  }
	
	  protected class AlphaSliderEditor extends AbstractCellEditor implements TableCellEditor {

		  private JSlider slider;
		  private boolean first_time = true;
		  
		  public AlphaSliderEditor(){
			  slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
			  slider.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseReleased(MouseEvent e) {
					stopCellEditing();
				}
			  });
		  }
		  
		  @Override
		  public Object getCellEditorValue() {
			  return (float)slider.getValue() / 100f;
		  }

		  @Override
		  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			  
			  //ensure proper behaviour on first use
			  if (first_time){
				  first_time = false;
				  slider.setBounds(table.getCellRect(row, column, false));
				  slider.updateUI();
			  	  }
			  
			  slider.setValue((int)(((Float)value) * 100f));
			  return slider;
		  }
		  
		  
		  
	  }
	  
	
}