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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import foxtrot.Job;
import foxtrot.Worker;
import mgui.datasources.DataTypes;
import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Rect3D;
import mgui.geometry.volume.VolumeEngine;
import mgui.geometry.volume.VolumeFunctions;
import mgui.geometry.volume.VolumeFunctions.VolumeAxis;
import mgui.geometry.volume.VolumeFunctions.VolumeRotationAngle;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.io.VolumeFileWriteDialog;
import mgui.interfaces.io.VolumeFileWriteOptions;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.stats.HistogramPlot;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.interfaces.tools.graphics.ToolDefine3DGrid2D;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.ShapeIOException;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.domestic.shapes.VolumeInputDialogBox;
import mgui.io.domestic.shapes.VolumeInputOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.NumberFunctions;
import mgui.stats.Histogram;
import mgui.util.JMultiLineToolTip;

/****************************************************************
 * Interface panel which allows the user to interact with {@linkplain Volume3DInt} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceVolumePanel extends InterfacePanel implements InterfaceIOPanel,
																	ActionListener,
																	KeyListener,
																	FocusListener,
																	ChangeListener,
																	DocumentListener,
																	ListSelectionListener{

	protected VolumeEngine volume_engine = new VolumeEngine();
	
	protected Volume3DInt currentVolume = null;
	Tool2D lastTool;
	
	static final String NEW_GRID = "<-New Volume->"; 
	static final String NEW_COLUMN = "<-New Column->"; 
	static final String NEW_MASK = "<-New Mask->"; 
	
	//Grid
	CategoryTitle lblGrid = new CategoryTitle("GRID");
	JLabel lblCurrentGrid = new JLabel("Shape:");
	InterfaceComboBox cmbGrid = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  true, 500);
	JLabel lblParentSet = new JLabel("Set:");
	InterfaceComboBox cmbParentSet = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  true, 500);
	
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
	
	//Data
	CategoryTitle lblData = new CategoryTitle("DATA");
	
	JLabel lblDimData = new JLabel("<html><b>DIMENSIONS</b></html>");
	JLabel lblDataX = new JLabel(" X"); 
	JTextField txtDataX = new JTextField("0");
	JLabel lblDataY = new JLabel(" Y"); 
	JTextField txtDataY = new JTextField("0");
	JLabel lblDataZ = new JLabel(" Z"); 
	JTextField txtDataZ = new JTextField("0");
	
	VertexDataTableModel vertex_data_model;
	JTable vertex_data_table;
	JScrollPane scrDataColumns;
	JButton cmdMoveColumnUp = new JButton("Move up");
	JButton cmdMoveColumnDown = new JButton("Move down");
	
	JLabel lblDataType = new JLabel("Data type:");
	JComboBox cmbDataType = new JComboBox();
	//JLabel lblSetData = new JLabel("Set from:");
	JCheckBox chkAddNewColumn = new JCheckBox("Add new:");
	JCheckBox chkUpdateCurrentColumn = new JCheckBox("Update current:");
	JCheckBox chkSetNone = new JCheckBox();
	JButton cmdZeroes = new JButton("Zeroes");
	JCheckBox chkSetVolFile = new JCheckBox();
	JButton cmdVolumeFile = new JButton("Volume File");
	JCheckBox chkSetImgStack = new JCheckBox();
	JButton cmdImgStack = new JButton("Image Stack");
	
	private int current_width_max = 50000, default_width_max = 50000;
	private int current_mid_min = -20000, default_mid_min = -20000;
	private int current_mid_max = 20000;
	
	// Preview
	CategoryTitle lblPreview = new CategoryTitle("PREVIEW");
	ImageViewComponent imgIntensity = new ImageViewComponent();
	
	//Intensity
	CategoryTitle lblIntensity = new CategoryTitle("INTENSITY");
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
	JCheckBox chkLowIsTransparent = new JCheckBox(" Low transp.");
	JLabel lblIntMin = new JLabel("Min:");
	JTextField txtIntMin = new JTextField("0.0");
	JLabel lblIntMax = new JLabel("Max:");
	JTextField txtIntMax = new JTextField("1.0");
	JLabel lblIntColourMap = new JLabel(" Colour map:");
	JCheckBox chkIsComposite = new JCheckBox(" Render as composite");
	InterfaceComboBox cmbIntColourMap = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  												  true, 500);
	JLabel lblIntSlice = new JLabel(" Slice:");
	JTextField txtIntSlice = new JTextField("1");
	JSlider sldIntSlice = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 1);
	
	JButton cmdIntApply = new JButton("Apply");
	JButton cmdIntReset = new JButton("Reset");
	
	//Attributes
	CategoryTitle lblAttributes = new CategoryTitle("ATTRIBUTES");
	InterfaceAttributePanel attrGrid3D = new InterfaceAttributePanel();
	AttributeList gridAttributes;
	
	//Buttons
	CategoryTitle lblApply = new CategoryTitle("APPLY");
	JButton cmdDelete = new JButton("Delete");
	//Create or Update dep. on cmbGrid
	JButton cmdApply = new JButton("Create");
	JButton cmdAddColumn = new JButton("Add Column");
	
	//Dynamic Input
//	CategoryTitle lblDynamic = new CategoryTitle("DYNAMIC");
//	JCheckBox chkDynamicNone = new JCheckBox("  No dynamic data");
//	JCheckBox chkDynamicVolume = new JCheckBox("  From volume file");
//	JCheckBox chkDynamicFiles = new JCheckBox();
//	JButton cmdDynamicFiles = new JButton("From files..");
//	JLabel lblDynamicInt = new JLabel("Intgr. Method");
//	JComboBox cmbDynamicInt = new JComboBox();
//	JLabel lblDynamicColourMap = new JLabel("Colour map");
//	JComboBox cmbDynamicColourMap = new JComboBox();
//	JLabel lblDynamicMin = new JLabel("Min");
//	JTextField txtDynamicMin = new JTextField("0");
//	JLabel lblDynamicMax = new JLabel("Max");
//	JTextField txtDynamicMax = new JTextField("256");
//	JLabel lblDynamicStep = new JLabel("Time Step (ms)");
//	JTextField txtDynamicStep = new JTextField("1000.0");
//	//Navigation stuff here
//	JButton cmdDynamicShowFirst = new JButton("|<");
//	JButton cmdDynamicShowPrev = new JButton("<");
//	JButton cmdDynamicShowNext = new JButton(">");
//	JButton cmdDynamicShowLast = new JButton(">|");
//	JLabel lblDynamicSample = new JLabel("Sample");
//	JTextField txtDynamicSample = new JTextField("0");
//	JLabel lblDynamicTime = new JLabel("Time");
//	JTextField txtDynamicTime = new JTextField("0.0");
//	JCheckBox chkDynamicAutoApply = new JCheckBox("  Auto apply");
//	JButton cmdDynamicApply = new JButton("Apply");
	
	//Overlays
	
	//Colour Maps
	
	//Masks
	CategoryTitle lblMasking = new CategoryTitle("MASKING");
	JTextField txtMaskVolume = new JTextField();
	JLabel lblMask = new JLabel("Mask:");
	JComboBox cmbMask = new JComboBox();
	JLabel lblMaskName = new JLabel("Name:");
	JTextField txtMaskName = new JTextField();
	JCheckBox chkMaskActive = new JCheckBox(" Active");
	
	//JCheckBox chkInvert = new JCheckBox(" Invert");
	JLabel lblMaskMergeFile = new JLabel("<html><u>Union with file</u></html>");
	JButton cmdMaskMergeFileBrowse = new JButton("Browse..");
	//JButton cmdMaskMergeFileApply = new JButton("Apply");
	JLabel lblMaskMergeOther = new JLabel("<html><u>Union with volume</u></html>");
	JLabel lblMaskMergeOtherVolume = new JLabel("Volume: ");
	JButton cmdMaskMergeOtherApply = new JButton("Apply");
	JComboBox cmbMaskMergeOtherVolume = new JComboBox();
	JCheckBox chkMaskMergeOtherMask = new JCheckBox("Mask:");
	JComboBox cmbMaskMergeOtherMask = new JComboBox();
	JLabel lblMaskMergeShape = new JLabel("<html><u>Union with shape</u></html>");
	JLabel lblMaskMergeShape2 = new JLabel("Shape: ");
	JComboBox cmbMaskMergeShape = new JComboBox();
	JPanel pnlMaskShape = new JPanel();
	JComboBox cmbMaskShapeCombo1;
	JTextField txtMaskShapeText1;
	JButton cmdMaskMergeShapeApply = new JButton("Apply");
	
	JButton cmdMaskClear = new JButton("Clear mask");
	JButton cmdMaskInvert = new JButton("Invert mask");
	JButton cmdMaskRemove = new JButton("Remove mask");
	JButton cmdMaskAddUpdate = new JButton("Add mask");
	
	//Operations
	CategoryTitle lblOperations = new CategoryTitle("OPERATIONS");
	JLabel lblOpRotate = new JLabel("<html><b>ROTATE</b></html>");
	JLabel lblOpRotateAxis = new JLabel("Axis:");
	JComboBox cmbOpRotateAxis = new JComboBox();
	JLabel lblOpRotateAngle = new JLabel("Angle:");
	JComboBox cmbOpRotateAngle = new JComboBox();
	JButton cmdOpRotate = new JButton("Apply");
	
	JLabel lblOpFlipDim = new JLabel("<html><b>FLIP DIM</b></html>");
	JLabel lblOpFlipWhichDim = new JLabel("Dim:");
	JComboBox cmbOpFlipWhichDim = new JComboBox();
	JButton cmdOpFlipDim = new JButton("Apply");
	
	JButton cmdOpInvertImage = new JButton("Invert values");
	
	// Histogram
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
	
	// Volume To Volume
	CategoryTitle lblV2V = new CategoryTitle("VOLUME TO VOLUME");
	JLabel lblV2VOperation = new JLabel("Operation:");
	InterfaceComboBox cmbV2VOperation = new InterfaceComboBox(RenderMode.LongestItem, false, 250);
	JLabel lblV2VTarget = new JLabel("Target volume:");
	InterfaceComboBox cmbV2VTarget = new InterfaceComboBox(RenderMode.LongestItem, true, 250);
	InterfaceAttributePanel lstV2VParameters = new InterfaceAttributePanel();
	JButton cmdV2V = new JButton("Execute");
	
	// Smoothing
	CategoryTitle lblSmoothing = new CategoryTitle("SMOOTHING");
	JLabel lblSmoothingOperation = new JLabel("Operation:");
	InterfaceComboBox cmbSmoothingOperation = new InterfaceComboBox(RenderMode.LongestItem, false, 250);
	InterfaceAttributePanel lstSmoothingParameters = new InterfaceAttributePanel();
	JButton cmdSmoothing = new JButton("Execute");
	
	// Smoothing
	CategoryTitle lblBlobs = new CategoryTitle("BLOBS");
	JLabel lblBlobOperation = new JLabel("Operation:");
	InterfaceComboBox cmbBlobOperation = new InterfaceComboBox(RenderMode.LongestItem, false, 250);
	InterfaceAttributePanel lstBlobParameters = new InterfaceAttributePanel();
	JButton cmdBlob = new JButton("Execute");
	
	// Isosurface
	CategoryTitle lblIsosurf = new CategoryTitle("ISOSURFACE");
	JLabel lblIsosurfColumn = new JLabel("Column:");
	InterfaceComboBox cmbIsosurfColumn = new InterfaceComboBox(RenderMode.LongestItem, true, 250);
	JLabel lblIsosurfLevel = new JLabel("Level:");
	JCheckBox chkIsosurfFixed = new JCheckBox(" Fixed:");
	JTextField txtIsosurfLevel = new JTextField("0");
	JCheckBox chkIsosurfCmapMin = new JCheckBox(" Colour map max");
	JCheckBox chkIsosurfCmapMax = new JCheckBox(" Colour map min");
	JLabel lblIsosurfShapeSet = new JLabel(" Shape set:");
	InterfaceComboBox cmbIsosurfShapeSet = new InterfaceComboBox(RenderMode.LongestItem, true, 250);
	JLabel lblIsosurfName = new JLabel("Name:");
	JTextField txtIsosurfName = new JTextField("no-name");
	JCheckBox chkIsosurfAuto = new JCheckBox(" Auto update");
	InterfaceAttributePanel lstIsosurfAttributes = new InterfaceAttributePanel();
	JButton cmdIsosurfApply = new JButton("Apply");
	
	public VolumeMaskOptions_old maskOptions = new VolumeMaskOptions_old();
	VolumeMaskDialog maskDialog;
	
	//Dialogs
	//VolFileReadDialog volFileReadDialog;
	VolumeFileWriteDialog volumeFileWriteDialog;
	
	//constants
	//final String CMD_VOL_FILE = "Data Vol File";
	
	//Input settings
	VolumeInputOptions volOptions;
	VolumeMaskOptions volumeMaskOptions = new VolumeMaskOptions();
	
	boolean updateGridCombo = true;
	boolean updateIntColourMap = true;
	boolean updateIntensity = true;
	boolean updateAnything = true;
	boolean updateSliders = true;
	boolean slidersJustChanged = false;
	
	int sliderLastMaxMid = -1, sliderLastMaxWidth = -1;
	
	
	boolean[][][] current_mask;
	
	public InterfaceVolumePanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	protected void init(){
		_init();
		
		//set this sucker up
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		//data formats
		cmbDataType.addItem("TYPE_BYTE");
		cmbDataType.addItem("TYPE_USHORT");
		cmbDataType.addItem("TYPE_SHORT");
		cmbDataType.addItem("TYPE_INT");
		cmbDataType.addItem("TYPE_FLOAT");
		cmbDataType.addItem("TYPE_DOUBLE");
		cmbDataType.setSelectedIndex(0);
		
		// Table
		initVertexDataTable();
		
		//combos
		updateGridList();
		updateOpCombos();
		
		//buttons
		cmdApply.addActionListener(this);
		cmdDelete.addActionListener(this);
		cmdZeroes.addActionListener(this);
		cmdVolumeFile.addActionListener(this);
		cmdImgStack.addActionListener(this);
		cmdDefX.addActionListener(this);
		cmdDefY.addActionListener(this);
		cmdDefZ.addActionListener(this);
		cmdDefGeometry.addActionListener(this);
		cmdOpRotate.addActionListener(this);
		cmdOpFlipDim.addActionListener(this);
		//cmdDynamicFiles.addActionListener(this);
		cmdHistCalc.addActionListener(this);
		cmdHistReset.addActionListener(this);
		cmdOpInvertImage.addActionListener(this);
		cmdOpInvertImage.setActionCommand("Invert Image");
		
		cmdMoveColumnUp.addActionListener(this);
		cmdMoveColumnUp.setActionCommand("Move Column Up");
		cmdMoveColumnDown.addActionListener(this);
		cmdMoveColumnDown.setActionCommand("Move Column Down");
		
		cmdUpdateSpecs.addActionListener(this);
		cmdUpdateSpecs.setActionCommand("Specifications Apply");
		
		cmdApply.setActionCommand("Create");
		cmdDelete.setActionCommand("Preview");
		cmdVolumeFile.setActionCommand("Data Volume File");
		cmdZeroes.setActionCommand("Data Zeroes");
		cmdImgStack.setActionCommand("ImgStack");
		cmdDefX.setActionCommand("DefX");
		cmdDefY.setActionCommand("DefY");
		cmdDefZ.setActionCommand("DefZ");
		cmdDefGeometry.setActionCommand("Define Geometry 2D");
		cmdOpRotate.setActionCommand("Apply Rotation");
		cmdOpFlipDim.setActionCommand("Apply Flip Dim");
		//cmdDynamicFiles.setActionCommand("Dynamic File");
		cmdHistCalc.setActionCommand("Histogram Apply");
		cmdHistReset.setActionCommand("Histogram Reset");
		cmdIntApply.setActionCommand("Apply Intensity");
		cmdIntApply.addActionListener(this);
		cmdIntReset.setActionCommand("Reset Intensity");
		cmdIntReset.addActionListener(this);
		
		cmdMaskAddUpdate.addActionListener(this);
		cmdMaskAddUpdate.setActionCommand("Mask Add");
		cmdMaskMergeFileBrowse.addActionListener(this);
		cmdMaskMergeFileBrowse.setActionCommand("Mask File Browse");
		cmdMaskInvert.addActionListener(this);
		cmdMaskInvert.setActionCommand("Mask Invert");
		cmdMaskMergeShapeApply.addActionListener(this);
		cmdMaskMergeShapeApply.setActionCommand("Mask Apply Shape");
		cmdMaskClear.addActionListener(this);
		cmdMaskClear.setActionCommand("Mask Clear");
		
		cmdAddColumn.addActionListener(this);
		cmdAddColumn.setActionCommand("Add Column");
				
		//combo box
		cmbGrid.addActionListener(this);
		cmbGrid.setActionCommand("Grid Changed");
		//cmbGrid.setActionCommand("Grid Changed");
		cmbIntColourMap.addActionListener(this);
		cmbIntColourMap.setActionCommand("Intensity Colour Map Changed");
		cmbMask.addActionListener(this);
		cmbMask.setActionCommand("Mask Changed");
		cmbMaskMergeShape.addActionListener(this);
		cmbMaskMergeShape.setActionCommand("Mask Shape Changed");
		
		//disable stuff
		updateMasking();
		enableOps(false);
		
		//check boxes
		chkSetNone.setSelected(true);
		//chkDynamicNone.setSelected(true);
		chkSetNone.addActionListener(this);
		chkSetVolFile.addActionListener(this);
		chkSetImgStack.addActionListener(this);
//		chkDynamicNone.addActionListener(this);
//		chkDynamicVolume.addActionListener(this);
//		chkDynamicFiles.addActionListener(this);
		chkSetNone.setActionCommand("Set None");
		chkSetVolFile.setActionCommand("Set Volume File");
		chkSetImgStack.setActionCommand("Set Image Stack");
//		chkDynamicNone.setActionCommand("Set Dynamic None");
//		chkDynamicVolume.setActionCommand("Set Dynamic Volume");
//		chkDynamicFiles.setActionCommand("Set Dynamic Files");
		chkIntSetAlpha.addActionListener(this);
		chkIntSetAlpha.setActionCommand("Intensity Set Alpha");
		chkLowIsTransparent.addActionListener(this);
		chkLowIsTransparent.setActionCommand("Intensity Set LowIsTransparent");
		chkMaskActive.addActionListener(this);
		chkMaskActive.setActionCommand("Mask Set Active");
		chkAddNewColumn.setSelected(true);
		chkAddNewColumn.addActionListener(this);
		chkAddNewColumn.setActionCommand("Is Add New Column");
		chkUpdateCurrentColumn.addActionListener(this);
		chkUpdateCurrentColumn.setActionCommand("Is Update Current Column");
		chkIsComposite.addActionListener(this);
		chkIsComposite.setActionCommand("Is Composite");
		
		//intensity
		txtIntIntercept.setActionCommand("Intensity Changed Intercept");
		txtIntIntercept.addActionListener(this);
		txtIntScale.setActionCommand("Intensity Changed Scale");
		txtIntScale.addActionListener(this);
		txtIntWidth.setActionCommand("Intensity Changed Width");
		txtIntWidth.addActionListener(this);
		sldIntWidth.addChangeListener(this);
		txtIntMid.setActionCommand("Intensity Changed Mid");
		txtIntMid.addActionListener(this);
		sldIntMid.addChangeListener(this);
		
		txtIntAlphaMin.setActionCommand("Intensity Changed Alpha Min");
		txtIntAlphaMin.addActionListener(this);
		sldIntAlphaMin.addChangeListener(this);
		txtIntAlphaMax.setActionCommand("Intensity Changed Alpha Max");
		txtIntAlphaMax.addActionListener(this);
		sldIntAlphaMax.addChangeListener(this);
		txtIntAlpha.setActionCommand("Intensity Changed Alpha");
		txtIntAlpha.addActionListener(this);
		sldIntAlpha.addChangeListener(this);
		txtIntSlice.setActionCommand("Intensity Changed Slice");
		txtIntSlice.addActionListener(this);
		sldIntSlice.addChangeListener(this);
		txtIntMin.addActionListener(this);
		txtIntMin.setActionCommand("Intensity Changed Min");
		txtIntMax.addActionListener(this);
		txtIntMax.setActionCommand("Intensity Changed Max");
		
		//text fields (listeners update text box from user input)
		txtDataZ.addActionListener(this);
		txtDataX.addKeyListener(this);
		txtDataY.addKeyListener(this);
		txtDataZ.addKeyListener(this);
		txtDataX.addFocusListener(this);
		txtDataY.addFocusListener(this);
		txtDataZ.addFocusListener(this);
		
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
		
		cmbV2VOperation.addActionListener(this);
		cmbV2VOperation.setActionCommand("V2V Change Operation");
		cmbV2VTarget.addActionListener(this);
		cmbV2VTarget.setActionCommand("V2V Change Target");
		cmdV2V.addActionListener(this);
		cmdV2V.setActionCommand("V2V Execute");
		
		cmbSmoothingOperation.addActionListener(this);
		cmbSmoothingOperation.setActionCommand("Smoothing Change Operation");
		cmdSmoothing.addActionListener(this);
		cmdSmoothing.setActionCommand("Smoothing Execute");
		
		cmbBlobOperation.addActionListener(this);
		cmbBlobOperation.setActionCommand("Blob Change Operation");
		cmdBlob.addActionListener(this);
		cmdBlob.setActionCommand("Blob Execute");
		
		cmdIsosurfApply.addActionListener(this);
		cmdIsosurfApply.setActionCommand("Isosurf Execute");
		
		txtHistYScale.addActionListener(this);
		txtHistYScale.setActionCommand("Histogram Y Scale Changed");
		sldHistYScale.addChangeListener(this);
		
		txtMaskName.addActionListener(this);
		txtMaskName.setActionCommand("Mask Changed Name");
		
		pnlHistPanel.addChangeListener(this);
		
		pnlMaskShape.setLayout(new LineLayout(20, 5, 0));
		
		//update volume options from current values
		updateVolOptions();
		
		//controls
		//Grid
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblGrid, c);
		lblGrid.setParentObj(this);
		c = new CategoryLayoutConstraints("GRID", 1, 1, 0.05, .25, 1);
		add(lblCurrentGrid, c);
		c = new CategoryLayoutConstraints("GRID", 1, 1, 0.3, .65, 1);
		add(cmbGrid, c);
		c = new CategoryLayoutConstraints("GRID", 2, 2, 0.05, .25, 1);
		add(lblParentSet, c);
		c = new CategoryLayoutConstraints("GRID", 2, 2, 0.3, .65, 1);
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
		
		c = new CategoryLayoutConstraints("DATA", 4, 4, 0.05, 0.43, 1);
		add(chkAddNewColumn, c);
		c = new CategoryLayoutConstraints("DATA", 4, 4, 0.52, 0.43, 1);
		add(chkUpdateCurrentColumn, c);
		c = new CategoryLayoutConstraints("DATA", 5, 5, 0.11, 0.1, 1);
		add(chkSetNone, c);
		c = new CategoryLayoutConstraints("DATA", 5, 5, 0.2, 0.75, 1);
		add(cmdZeroes, c);
		c = new CategoryLayoutConstraints("DATA", 6, 6, 0.11, 0.1, 1);
		add(chkSetVolFile, c);
		c = new CategoryLayoutConstraints("DATA", 6, 6, 0.2, 0.75, 1);
		add(cmdVolumeFile, c);
		c = new CategoryLayoutConstraints("DATA", 7, 7, 0.11, 0.1, 1);
		add(chkSetImgStack, c);
		c = new CategoryLayoutConstraints("DATA", 7, 7, 0.2, 0.75, 1);
		add(cmdImgStack, c);
		c = new CategoryLayoutConstraints("DATA", 8, 9, 0.05, 0.43, 1);
		add(cmdDelete, c);
		c = new CategoryLayoutConstraints("DATA", 8, 9, 0.52, 0.43, 1);
		add(cmdApply, c);		
		
		c = new CategoryLayoutConstraints("DATA", 10, 15, 0.05, .9, 1);
		add(scrDataColumns, c);
		c = new CategoryLayoutConstraints("DATA", 16, 16, 0.05, .43, 1);
		add(cmdMoveColumnUp, c);
		c = new CategoryLayoutConstraints("DATA", 16, 16, 0.52, .43, 1);
		add(cmdMoveColumnDown, c);
		
		
		
		// Preview
		c = new CategoryLayoutConstraints();
		add(lblPreview, c);
		lblPreview.setParentObj(this);
		c = new CategoryLayoutConstraints("PREVIEW", 1, 10, 0.05, 0.9, 1);
		add(imgIntensity, c);
		c = new CategoryLayoutConstraints("PREVIEW", 11, 11, 0.05, 0.3, 1);
		add(lblIntSlice, c);
		c = new CategoryLayoutConstraints("PREVIEW", 11, 11, 0.75, 0.2, 1);
		add(txtIntSlice, c);
		c = new CategoryLayoutConstraints("PREVIEW", 11, 11, 0.35, 0.4, 1);
		add(sldIntSlice, c);
		c = new CategoryLayoutConstraints("PREVIEW", 12, 12, 0.05, 0.3, 1);
		add(lblIntColourMap, c);
		c = new CategoryLayoutConstraints("PREVIEW", 12, 12, 0.35, 0.6, 1);
		add(cmbIntColourMap, c);
		
		//Intensity
		c = new CategoryLayoutConstraints();
		add(lblIntensity, c);
		lblIntensity.setParentObj(this);
		c = new CategoryLayoutConstraints("INTENSITY", 1, 1, 0.05, 0.25, 1);
		add(lblIntScale, c);
		c = new CategoryLayoutConstraints("INTENSITY", 1, 1, 0.3, 0.19, 1);
		add(txtIntScale, c);
		c = new CategoryLayoutConstraints("INTENSITY", 1, 1, 0.51, 0.19, 1);
		add(lblIntIntercept, c);
		c = new CategoryLayoutConstraints("INTENSITY", 1, 1, 0.7, 0.25, 1);
		add(txtIntIntercept, c);
		c = new CategoryLayoutConstraints("INTENSITY", 2, 2, 0.05, 0.3, 1);
		add(lblIntWidth, c);
		c = new CategoryLayoutConstraints("INTENSITY", 2, 2, 0.75, 0.2, 1);
		add(txtIntWidth, c);
		c = new CategoryLayoutConstraints("INTENSITY", 2, 2, 0.35, 0.4, 1);
		add(sldIntWidth, c);
		c = new CategoryLayoutConstraints("INTENSITY", 3, 3, 0.05, 0.3, 1);
		add(lblIntMid, c);
		c = new CategoryLayoutConstraints("INTENSITY", 3, 3, 0.75, 0.2, 1);
		add(txtIntMid, c);
		c = new CategoryLayoutConstraints("INTENSITY", 3, 3, 0.35, 0.4, 1);
		add(sldIntMid, c);
		c = new CategoryLayoutConstraints("INTENSITY", 4, 4, 0.05, 0.3, 1);
		add(lblIntAlphaMin, c);
		c = new CategoryLayoutConstraints("INTENSITY", 4, 4, 0.75, 0.2, 1);
		add(txtIntAlphaMin, c);
		c = new CategoryLayoutConstraints("INTENSITY", 4, 4, 0.35, 0.4, 1);
		add(sldIntAlphaMin, c);
		c = new CategoryLayoutConstraints("INTENSITY", 5, 5, 0.05, 0.3, 1);
		add(lblIntAlphaMax, c);
		c = new CategoryLayoutConstraints("INTENSITY", 5, 5, 0.75, 0.2, 1);
		add(txtIntAlphaMax, c);
		c = new CategoryLayoutConstraints("INTENSITY", 5, 5, 0.35, 0.4, 1);
		add(sldIntAlphaMax, c);
		c = new CategoryLayoutConstraints("INTENSITY", 6, 6, 0.05, 0.3, 1);
		add(lblIntAlpha, c);
		c = new CategoryLayoutConstraints("INTENSITY", 6, 6, 0.75, 0.2, 1);
		add(txtIntAlpha, c);
		c = new CategoryLayoutConstraints("INTENSITY", 6, 6, 0.35, 0.4, 1);
		add(sldIntAlpha, c);
		c = new CategoryLayoutConstraints("INTENSITY", 7, 7, 0.05, 0.44, 1);
		add(chkIntSetAlpha, c);
		c = new CategoryLayoutConstraints("INTENSITY", 7, 7, 0.5, 0.43, 1);
		add(chkLowIsTransparent, c);
		c = new CategoryLayoutConstraints("INTENSITY", 8, 8, 0.05, 0.15, 1);
		add(lblIntMin, c);
		lblIntMin.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("INTENSITY", 8, 8, 0.21, 0.28, 1);
		add(txtIntMin, c);
		c = new CategoryLayoutConstraints("INTENSITY", 8, 8, 0.51, 0.15, 1);
		add(lblIntMax, c);
		lblIntMax.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("INTENSITY", 8, 8, 0.67, 0.28, 1);
		add(txtIntMax, c);
		
		c = new CategoryLayoutConstraints("INTENSITY", 9, 9, 0.05, 0.9, 1);
		add(chkIsComposite, c);
		c = new CategoryLayoutConstraints("INTENSITY", 10, 10, 0.05, 0.9, 1);
		add(cmdIntApply, c);
		c = new CategoryLayoutConstraints("INTENSITY", 11, 11, 0.05, 0.9, 1);
		add(cmdIntReset, c);
		
		//Histogram
		c = new CategoryLayoutConstraints();
		add(lblHistogram, c);
		lblHistogram.setParentObj(this);
		
		c = new CategoryLayoutConstraints("HISTOGRAM", 1, 7, 0.05, 0.9, 1);
		add(pnlHistPanel, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 8, 8, 0.05, 0.3, 1);
		add(lblHistBins, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 8, 8, 0.35, 0.6, 1);
		add(txtHistBins, c);
		
		c = new CategoryLayoutConstraints("HISTOGRAM", 9, 9, 0.05, 0.15, 1);
		add(lblHistMin, c);
		lblHistMin.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("HISTOGRAM", 9, 9, 0.21, 0.28, 1);
		add(txtHistMin, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 9, 9, 0.51, 0.15, 1);
		add(lblHistMax, c);
		lblHistMax.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("HISTOGRAM", 9, 9, 0.67, 0.28, 1);
		add(txtHistMax, c);
		
//		c = new CategoryLayoutConstraints("HISTOGRAM", 9, 9, 0.05, 0.3, 1);
//		add(lblHistMin, c);
//		c = new CategoryLayoutConstraints("HISTOGRAM", 9, 9, 0.35, 0.6, 1);
//		add(txtHistMin, c);
//		c = new CategoryLayoutConstraints("HISTOGRAM", 10, 10, 0.05, 0.3, 1);
//		add(lblHistMax, c);
//		c = new CategoryLayoutConstraints("HISTOGRAM", 10, 10, 0.35, 0.6, 1);
//		add(txtHistMax, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 10, 10, 0.05, 0.3, 1);
		add(lblHistYScale, c);
//		c = new CategoryLayoutConstraints("HISTOGRAM", 11, 11, 0.35, 0.6, 1);
//		add(txtHistYScale, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 10, 10, 0.35, 0.6, 1);
		add(sldHistYScale, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 11, 11, 0.05, 0.43, 1);
		add(cmdHistReset, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 11, 11, 0.52, 0.43, 1);
		add(cmdHistCalc, c);
		
		c = new CategoryLayoutConstraints();
		add(lblV2V, c);
		lblV2V.setParentObj(this);
		c = new CategoryLayoutConstraints("VOLUME TO VOLUME", 1, 1, 0.05, 0.3, 1);
		add(lblV2VOperation, c);
		c = new CategoryLayoutConstraints("VOLUME TO VOLUME", 1, 1, 0.35, 0.6, 1);
		add(cmbV2VOperation, c);
		c = new CategoryLayoutConstraints("VOLUME TO VOLUME", 2, 2, 0.05, 0.3, 1);
		add(lblV2VTarget, c);
		c = new CategoryLayoutConstraints("VOLUME TO VOLUME", 2, 2, 0.35, 0.6, 1);
		add(cmbV2VTarget, c);
		c = new CategoryLayoutConstraints("VOLUME TO VOLUME", 3, 7, 0.05, 0.9, 1);
		add(lstV2VParameters, c);
		c = new CategoryLayoutConstraints("VOLUME TO VOLUME", 8, 8, 0.15, 0.7, 1);
		add(cmdV2V, c);
		
		c = new CategoryLayoutConstraints();
		add(lblSmoothing, c);
		lblSmoothing.setParentObj(this);
		c = new CategoryLayoutConstraints("SMOOTHING", 1, 1, 0.05, 0.3, 1);
		add(lblSmoothingOperation, c);
		c = new CategoryLayoutConstraints("SMOOTHING", 1, 1, 0.35, 0.6, 1);
		add(cmbSmoothingOperation, c);
		c = new CategoryLayoutConstraints("SMOOTHING", 2, 6, 0.05, 0.9, 1);
		add(lstSmoothingParameters, c);
		c = new CategoryLayoutConstraints("SMOOTHING", 7, 7, 0.15, 0.7, 1);
		add(cmdSmoothing, c);
		
		c = new CategoryLayoutConstraints();
		add(lblBlobs, c);
		lblBlobs.setParentObj(this);
		c = new CategoryLayoutConstraints("BLOBS", 1, 5, 0.05, 0.9, 1);
		add(lstBlobParameters, c);
		c = new CategoryLayoutConstraints("BLOBS", 6, 6, 0.15, 0.7, 1);
		add(cmdBlob, c);
		
		//Attributes
		c = new CategoryLayoutConstraints();
		add(lblAttributes, c);
		lblAttributes.setParentObj(this);
		c = new CategoryLayoutConstraints("ATTRIBUTES", 1, 5, 0.05, 0.9, 1);
		add(attrGrid3D, c);
		
		// Isosurface
		c = new CategoryLayoutConstraints();
		add(lblIsosurf, c);
		lblIsosurf.setParentObj(this);
		c = new CategoryLayoutConstraints("ISOSURFACE", 1, 1, 0.05, 0.3, 1);
		add(lblIsosurfLevel, c);
		c = new CategoryLayoutConstraints("ISOSURFACE", 1, 1, 0.35, 0.6, 1);
		add(txtIsosurfLevel, c);
		c = new CategoryLayoutConstraints("ISOSURFACE", 2, 2, 0.05, 0.3, 1);
		add(lblIsosurfName, c);
		c = new CategoryLayoutConstraints("ISOSURFACE", 2, 2, 0.35, 0.6, 1);
		add(txtIsosurfName, c);
		c = new CategoryLayoutConstraints("ISOSURFACE", 3, 3, 0.05, 0.3, 1);
		add(lblIsosurfShapeSet, c);
		c = new CategoryLayoutConstraints("ISOSURFACE", 3, 3, 0.35, 0.6, 1);
		add(cmbIsosurfShapeSet, c);
		c = new CategoryLayoutConstraints("ISOSURFACE", 4, 4, 0.05, 0.3, 1);
		add(lblIsosurfColumn, c);
		c = new CategoryLayoutConstraints("ISOSURFACE", 4, 4, 0.35, 0.6, 1);
		add(cmbIsosurfColumn, c);
		c = new CategoryLayoutConstraints("ISOSURFACE", 5, 5, 0.15, 0.7, 1);
		add(cmdIsosurfApply, c);
		
		
		//Dynamic
//		c = new CategoryLayoutConstraints();
//		add(lblDynamic, c);
//		lblDynamic.setParentObj(this);
//		c = new CategoryLayoutConstraints("DYNAMIC", 1, 1, 0.05, 0.9, 1);
//		add(chkDynamicNone, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 2, 2, 0.05, 0.9, 1);
//		add(chkDynamicVolume, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 3, 3, 0.05, 0.1, 1);
//		add(chkDynamicFiles, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 3, 3, 0.2, 0.75, 1);
//		add(cmdDynamicFiles, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 4, 4, 0.05, 0.3, 1);
//		add(lblDynamicInt, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 4, 4, 0.35, 0.6, 1);
//		add(cmbDynamicInt, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 5, 5, 0.05, 0.3, 1);
//		add(lblDynamicColourMap, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 5, 5, 0.35, 0.6, 1);
//		add(cmbDynamicColourMap, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 6, 6, 0.05, 0.15, 1);
//		add(lblDynamicMin, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 6, 6, 0.20, 0.29, 1);
//		add(txtDynamicMin, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 6, 6, 0.51, 0.15, 1);
//		add(lblDynamicMax, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 6, 6, 0.66, 0.29, 1);
//		add(txtDynamicMax, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 7, 7, 0.05, 0.15, 1);
//		add(lblDynamicSample, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 7, 7, 0.20, 0.29, 1);
//		add(txtDynamicSample, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 7, 7, 0.51, 0.15, 1);
//		add(lblDynamicTime, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 7, 7, 0.66, 0.29, 1);
//		add(txtDynamicTime, c);
//		
//		c = new CategoryLayoutConstraints("DYNAMIC", 8, 8, 0.06, 0.22, 1);
//		cmdDynamicShowFirst.setToolTipText("First");
//		add(cmdDynamicShowFirst, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 8, 8, 0.28, 0.22, 1);
//		cmdDynamicShowPrev.setToolTipText("Prev");
//		add(cmdDynamicShowPrev, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 8, 8, 0.50, 0.22, 1);
//		cmdDynamicShowNext.setToolTipText("Next");
//		add(cmdDynamicShowNext, c);
//		c = new CategoryLayoutConstraints("DYNAMIC", 8, 8, 0.72, 0.22, 1);
//		cmdDynamicShowLast.setToolTipText("Last");
//		add(cmdDynamicShowLast, c);
		
		
		//Masking
		c = new CategoryLayoutConstraints();
		add(lblMasking, c);
		lblMasking.setParentObj(this);
		
		c = new CategoryLayoutConstraints("MASKING", 1, 1, 0.05, 0.24, 1);
		add(lblMask, c);
		c = new CategoryLayoutConstraints("MASKING", 1, 1, 0.3, 0.65, 1);
		add(cmbMask, c);
		c = new CategoryLayoutConstraints("MASKING", 2, 2, 0.05, 0.24, 1);
		add(lblMaskName, c);
		c = new CategoryLayoutConstraints("MASKING", 2, 2, 0.3, 0.65, 1);
		add(txtMaskName, c);
		c = new CategoryLayoutConstraints("MASKING", 3, 3, 0.3, 0.65, 1);
		add(chkMaskActive, c);
		
		c = new CategoryLayoutConstraints("MASKING", 4, 4, 0.05, 0.9, 1);
		add(lblMaskMergeFile, c);
		c = new CategoryLayoutConstraints("MASKING", 5, 5, 0.51, 0.44, 1);
		add(cmdMaskMergeFileBrowse, c);
		
		c = new CategoryLayoutConstraints("MASKING", 6, 6, 0.05, 0.9, 1);
		add(lblMaskMergeOther, c);
		c = new CategoryLayoutConstraints("MASKING", 7, 7, 0.1, 0.2, 1);
		add(lblMaskMergeOtherVolume, c);
		lblMaskMergeOtherVolume.setHorizontalAlignment(JLabel.RIGHT);
		c = new CategoryLayoutConstraints("MASKING", 7, 7, 0.35, 0.6, 1);
		add(cmbMaskMergeOtherVolume, c);
		c = new CategoryLayoutConstraints("MASKING", 8, 8, 0.1, 0.2, 1);
		add(chkMaskMergeOtherMask, c);
		chkMaskMergeOtherMask.setHorizontalAlignment(JLabel.RIGHT);
		c = new CategoryLayoutConstraints("MASKING", 8, 8, 0.35, 0.6, 1);
		add(cmbMaskMergeOtherMask, c);
		c = new CategoryLayoutConstraints("MASKING", 9, 9, 0.51, 0.44, 1);
		add(cmdMaskMergeOtherApply, c);
		
		c = new CategoryLayoutConstraints("MASKING", 10, 10, 0.05, 0.9, 1);
		add(lblMaskMergeShape, c);
		c = new CategoryLayoutConstraints("MASKING", 11, 11, 0.1, 0.2, 1);
		add(lblMaskMergeShape2, c);
		lblMaskMergeShape2.setHorizontalAlignment(JLabel.RIGHT);
		c = new CategoryLayoutConstraints("MASKING", 11, 11, 0.35, 0.6, 1);
		add(cmbMaskMergeShape, c);
		c = new CategoryLayoutConstraints("MASKING", 12, 14, 0.1, 0.85, 1);
		add(pnlMaskShape, c);
		c = new CategoryLayoutConstraints("MASKING", 15, 15, 0.51, 0.44, 1);
		add(cmdMaskMergeShapeApply, c);
		
		//room for shape parameters
		
		c = new CategoryLayoutConstraints("MASKING", 16, 16, 0.05, 0.44, 1);
		add(cmdMaskClear, c);
		c = new CategoryLayoutConstraints("MASKING", 16, 16, 0.51, 0.44, 1);
		add(cmdMaskInvert, c);
		c = new CategoryLayoutConstraints("MASKING", 17, 17, 0.05, 0.44, 1);
		add(cmdMaskRemove, c);
		c = new CategoryLayoutConstraints("MASKING", 17, 17, 0.51, 0.44, 1);
		add(cmdMaskAddUpdate, c);
		
		//enableMasking(false);
		
		//Operations
		c = new CategoryLayoutConstraints();
		add(lblOperations, c);
		lblOperations.setParentObj(this);
		c = new CategoryLayoutConstraints("OPERATIONS", 1, 1, 0.05, 0.4, 1);
		add(lblOpRotate, c);
		c = new CategoryLayoutConstraints("OPERATIONS", 2, 2, 0.3, 0.2, 1);
		add(lblOpRotateAxis, c);
		c = new CategoryLayoutConstraints("OPERATIONS", 2, 2, 0.5, 0.45, 1);
		add(cmbOpRotateAxis, c);
		c = new CategoryLayoutConstraints("OPERATIONS", 3, 3, 0.3, 0.2, 1);
		add(lblOpRotateAngle, c);
		c = new CategoryLayoutConstraints("OPERATIONS", 3, 3, 0.5, 0.45, 1);
		add(cmbOpRotateAngle, c);
		c = new CategoryLayoutConstraints("OPERATIONS", 4, 4, 0.5, 0.45, 1);
		add(cmdOpRotate, c);
		
		c = new CategoryLayoutConstraints("OPERATIONS", 5, 5, 0.05, 0.4, 1);
		add(lblOpFlipDim, c);
		c = new CategoryLayoutConstraints("OPERATIONS", 6, 6, 0.3, 0.2, 1);
		add(lblOpFlipWhichDim, c);
		c = new CategoryLayoutConstraints("OPERATIONS", 6, 6, 0.5, 0.45, 1);
		add(cmbOpFlipWhichDim, c);
		c = new CategoryLayoutConstraints("OPERATIONS", 7, 7, 0.5, 0.45, 1);
		add(cmdOpFlipDim, c);
		
		initOperations();
		
	}
	
	private void initVertexDataTable(){
		
		vertex_data_model = new VertexDataTableModel();
		vertex_data_table = new JTable(vertex_data_model);
		vertex_data_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		vertex_data_table.getColumnModel().getColumn(3).setCellRenderer(new SliderTableColumn(0,1000,1000));
		vertex_data_table.getColumnModel().getColumn(3).setCellEditor(new SliderTableColumn(0,1000,1000));
		vertex_data_table.getColumnModel().getColumn(0).setPreferredWidth(50);
		vertex_data_table.getColumnModel().getColumn(1).setPreferredWidth(50);
//		vertex_data_table.getColumnModel().getColumn(0).setMinWidth(30);
//		vertex_data_table.getColumnModel().getColumn(1).setMinWidth(30);
		vertex_data_table.getColumnModel().getColumn(2).setPreferredWidth(300);
		vertex_data_table.getColumnModel().getColumn(3).setPreferredWidth(400);
		vertex_data_table.getSelectionModel().addListSelectionListener(this);
		
		scrDataColumns = new JScrollPane(vertex_data_table);
		
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
	}


	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceVolumePanel.class.getResource("/mgui/resources/icons/volume_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/volume_3d_20.png");
		return null;
	}
	
	public void cleanUpPanel(){
		//clear combo lists to avoid memory leaks (references to volume objects which may be deleted)
		//for whatever reason this fires action events
		updateAnything = false;
		//cmbDynamicColourMap.removeAllItems();
		cmbIntColourMap.removeAllItems();
		cmbGrid.removeAllItems();
		cmbParentSet.removeAllItems();
		updateAnything = true;
	}
	
	public void shapeUpdated(ShapeEvent e){
		
		switch (e.eventType){
		
			case ShapeModified:
			case VertexColumnChanged:
			case VertexColumnAdded:
			case VertexColumnRemoved:
				setCurrentVolume(currentVolume);
				return;
		
			case ShapeRemoved:
			case ShapeDestroyed:
				
				if (e.getShape().equals(currentVolume)){
					updateGridList();
					setCurrentVolume(null);
					if (this.isVisible()) showPanel();
					}
				
				return;
		
		}
		
		
	}
	
	protected void enableMasking(){
		boolean b1 = cmbGrid.getSelectedItem() != null && 
					!cmbGrid.getSelectedItem().equals(NEW_GRID);
		boolean b2 = b1 &&
					 cmbMask.getSelectedItem() != null &&
					!cmbMask.getSelectedItem().equals(NEW_MASK);
		
		txtMaskVolume.setEnabled(b1);
		cmbMask.setEnabled(b1);
		txtMaskName.setEnabled(b1);
		chkMaskActive.setEnabled(b2);
		
		cmdMaskMergeFileBrowse.setEnabled(b2);
		//cmdMaskMergeFileApply.setEnabled(b2);
		cmdMaskMergeOtherApply.setEnabled(b2);
		cmbMaskMergeOtherVolume.setEnabled(b2);
		chkMaskMergeOtherMask.setEnabled(b2);
		cmbMaskMergeOtherMask.setEnabled(b2);
		cmbMaskMergeShape.setEnabled(b2);
		cmdMaskMergeShapeApply.setEnabled(b2);
		
		cmdMaskClear.setEnabled(b2);
		cmdMaskInvert.setEnabled(b2);
		cmdMaskRemove.setEnabled(b2);
		cmdMaskAddUpdate.setEnabled(b1 && txtMaskName.getText().length() > 0);
		
	}
	
	protected void enableOps(boolean b){
		cmbOpRotateAxis.setEnabled(b);
		cmbOpRotateAngle.setEnabled(b);
		cmdOpRotate.setEnabled(b);
		cmdOpFlipDim.setEnabled(b);
		cmbOpFlipWhichDim.setEnabled(b);
	}
	
	protected void enableIntensity(boolean b){
		txtIntWidth.setEnabled(b);
		sldIntWidth.setEnabled(b);
		txtIntMid.setEnabled(b);
		sldIntMid.setEnabled(b);
		txtIntAlphaMin.setEnabled(b);
		sldIntAlphaMin.setEnabled(b);
		txtIntAlphaMax.setEnabled(b);
		sldIntAlphaMax.setEnabled(b);
		txtIntAlpha.setEnabled(b);
		sldIntAlpha.setEnabled(b);
		txtIntSlice.setEnabled(b);
		sldIntSlice.setEnabled(b);
		imgIntensity.setEnabled(b);
		chkIntSetAlpha.setEnabled(b);
		chkLowIsTransparent.setEnabled(b);
		cmbIntColourMap.setEnabled(b);
		txtIntMin.setEnabled(b);
		txtIntMax.setEnabled(b);
		
		txtIntScale.setEnabled(b);
		txtIntIntercept.setEnabled(b);
		
		cmdIntApply.setEnabled(b);
		cmdIntReset.setEnabled(b);
	}
	
	private void updateOpCombos(){
		cmbOpRotateAxis.removeAllItems();
		cmbOpRotateAxis.addItem("S Axis");
		cmbOpRotateAxis.addItem("R Axis");
		cmbOpRotateAxis.addItem("T Axis");
		cmbOpRotateAngle.removeAllItems();
		cmbOpRotateAngle.addItem("90 CW");
		cmbOpRotateAngle.addItem("180");
		cmbOpRotateAngle.addItem("270 CW");
		
		cmbOpFlipWhichDim.removeAllItems();
		cmbOpFlipWhichDim.addItem("S");
		cmbOpFlipWhichDim.addItem("T");
		cmbOpFlipWhichDim.addItem("R");
	}
	
	//fill grid combo with list of Grid3D objects in this model
	//TODO make this a tree?
	private void updateGridList(){
		updateGridCombo = false;
		//boolean isNew = isNewGrid();
		boolean current_found = false;
		cmbGrid.removeAllItems();
		cmbGrid.addItem(NEW_GRID);
		//cmbGrid.addItem(NEW_OVERLAY_SET);
		
		List<Shape3DInt> volumes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Volume3DInt());
		for (Shape3DInt volume : volumes) {
			cmbGrid.addItem(volume);
			if (currentVolume != null && volume.equals(currentVolume))
				current_found = true;
			}
		
		if (current_found){
			cmbGrid.setSelectedItem(currentVolume);
		}else{
			setCurrentVolume(null);
			}
		
		attrGrid3D.setAttributes(currentVolume.getAttributes());
		
		//update set list
		cmbParentSet.removeAllItems();
		cmbIsosurfShapeSet.removeAllItems();
		ShapeSet3DInt current_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		cmbParentSet.addItem(current_set);
		cmbIsosurfShapeSet.addItem(current_set);
		//ShapeSet3DInt all_sets = current_set.getShapeType(current_set, true);
		
		List<Shape3DInt> all_sets = current_set.getShapeType(current_set, true);
		for (Shape3DInt set : all_sets) {
			cmbParentSet.addItem(set);
			cmbIsosurfShapeSet.addItem(set);
			}
		
		updateDisplay();
		updateGridCombo = true;
		
	}
	
	public void updateDisplay(){
		boolean b = !isNewGrid();
		enableOps(b);
		enableIntensity(b);
		updateButtons();
		updateOperations();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		
		if (!this.updateIntensity) return;
		updateIntensity = false;
		
		if (e.getSource().equals(pnlHistPanel)){
			updateIntensityValues();
			updateIntensitySlice();
			imgIntensity.repaint();
			updateIntensity = true;
			return;
			}
		
		if (e.getSource().equals(sldIntSlice)){
			int val = sldIntSlice.getValue();
			txtIntSlice.setText("" + val);
			updateIntensitySlice();
			updateIntensity = true;
			return;
		}
		
		if (e.getSource().equals(sldIntWidth)){
			if (!updateSliders){
				updateIntensity = true;
				return;
				}
			int sld_val = sldIntWidth.getValue();
			
			if (slidersJustChanged && sld_val >= current_width_max){
				updateIntensity = true;
				return;
				}
			slidersJustChanged = false;
			
			updateSliders = false;
			
			double val = (double)sld_val / 10000.0;
			txtIntWidth.setText(MguiDouble.getString(val, "0.00#####"));
			if (imgIntensity != null && imgIntensity.model != null){
				if (chkIsComposite.isSelected()){
					WindowedColourModel model = vertex_data_model.getColourModel(vertex_data_model.getCurrentColumn());
					model.setWindowWidth(1.0 / val);
					updateIntensitySlice();
				}else{
					imgIntensity.model.setWindowWidth(1.0 / val);
					imgIntensity.repaint();
					}
				updateHistogram();
				}
			
			else if (sld_val == sldIntWidth.getMinimum()){
				current_width_max = (int)((double)current_width_max / 2.0);
				if (current_width_max < 2000) current_width_max = 2000;
				sld_val = (int)((double)current_width_max / 2.0);
				sldIntWidth.setMaximum(current_width_max);
				sldIntWidth.setValue(sld_val);
				sldIntWidth.updateUI();
				slidersJustChanged = true;
				}
			updateSliders = true;
			updateIntensity = true;
			return;
			}
		
		if (e.getSource().equals(sldIntMid)){
			
			if (!updateSliders){
				updateIntensity = true;
				return;
				}
			int sld_val = sldIntMid.getValue();
			
			if (slidersJustChanged && (sld_val >= current_mid_max || sld_val <= current_mid_min)) return;
			slidersJustChanged = false;
			
			updateSliders = false;
			
			double val = (double)sld_val / 10000.0;
			txtIntMid.setText(MguiDouble.getString(val, "0.00#####"));
			if (imgIntensity != null && imgIntensity.model != null){
				if (chkIsComposite.isSelected()){
					WindowedColourModel model = vertex_data_model.getColourModel(vertex_data_model.getCurrentColumn());
					model.setWindowMid(val);
					updateIntensitySlice();
				}else{
					imgIntensity.model.setWindowMid(val);
					imgIntensity.repaint();
					}
				updateHistogram();
				}
					
			updateIntensity = true;
			updateSliders = true;
			return;
			}
		
		if (e.getSource().equals(sldIntAlphaMin)){
			double val = (double)sldIntAlphaMin.getValue() / 10000.0;
			txtIntAlphaMin.setText(MguiDouble.getString(val, "0.00#####"));
			if (imgIntensity != null && imgIntensity.model != null){
				if (chkIsComposite.isSelected()){
					WindowedColourModel model = vertex_data_model.getColourModel(vertex_data_model.getCurrentColumn());
					model.setAlphaMin(val);
					updateIntensitySlice();
				}else{
					imgIntensity.model.setAlphaMin(val);
					}
				if (imgIntensity.model.getAlphaMin() > imgIntensity.model.getAlphaMax()){
					if (chkIsComposite.isSelected()){
						WindowedColourModel model = vertex_data_model.getColourModel(vertex_data_model.getCurrentColumn());
						model.setAlphaMax(imgIntensity.model.getAlphaMin());
						updateIntensitySlice();
					}else{
						imgIntensity.model.setAlphaMax(imgIntensity.model.getAlphaMin());
						}
					sldIntAlphaMax.setValue((int)(imgIntensity.model.getAlphaMax() * 10000f));
					updateIntensity = true;
					return;
					}
				
				imgIntensity.repaint();
				updateHistogram();
				}
			
			updateIntensity = true;
			return;
			}
		
		if (e.getSource().equals(sldIntAlphaMax)){
			double val = (double)sldIntAlphaMax.getValue() / 10000.0;
			txtIntAlphaMax.setText(MguiDouble.getString(val, "0.00#####"));
			if (imgIntensity != null && imgIntensity.model != null){
				if (chkIsComposite.isSelected()){
					WindowedColourModel model = vertex_data_model.getColourModel(vertex_data_model.getCurrentColumn());
					model.setAlphaMax(val);
					updateIntensitySlice();
				}else{
					imgIntensity.model.setAlphaMax(val);
					}
				if (imgIntensity.model.getAlphaMin() > imgIntensity.model.getAlphaMax()){
					if (chkIsComposite.isSelected()){
						WindowedColourModel model = vertex_data_model.getColourModel(vertex_data_model.getCurrentColumn());
						model.setAlphaMin(imgIntensity.model.getAlphaMax());
						updateIntensitySlice();
					}else{
						imgIntensity.model.setAlphaMin(imgIntensity.model.getAlphaMax());
						}
					sldIntAlphaMin.setValue((int)(imgIntensity.model.getAlphaMin() * 10000f));
					updateIntensity = true;
					return;
					}
				imgIntensity.repaint();
				updateHistogram();
				}
			updateIntensity = true;
			return;
			}
		
		if (e.getSource().equals(sldIntAlpha)){
			double val = (double)sldIntAlpha.getValue() / 10000.0;
			txtIntAlpha.setText(MguiDouble.getString(val, "0.00#####"));
			if (imgIntensity != null && imgIntensity.model != null){
				if (chkIsComposite.isSelected()){
					WindowedColourModel model = vertex_data_model.getColourModel(vertex_data_model.getCurrentColumn());
					model.setAlpha(val);
					updateIntensitySlice();
				}else{
					imgIntensity.model.setAlpha(val);
					imgIntensity.revalidate();
					imgIntensity.repaint();
					}
				updateHistogram();
				}
			updateIntensity = true;
			return;
			}
		
		if (e.getSource().equals(sldHistYScale)){
			//if (!updateIntensity) return;
			float val = sldHistYScale.getValue() / 10000f;
			txtHistYScale.setText(MguiDouble.getString(val, "0.00#####"));
			pnlHistPanel.setYScale(val);
			//updateHistogram();
			updateIntensity = true;
			return;
			}
		
	}
	
	protected boolean isNewGrid(){
		if (cmbGrid.getSelectedItem() == null) return true;
		return cmbGrid.getSelectedItem().equals(NEW_GRID); 
	}
	
	private void initOperations(){
		updateAnything = false;
		cmbV2VOperation.removeAllItems();
		ArrayList<String> methods = volume_engine.getMethods("Map Volume to Volume");
		for (int i = 0; i < methods.size(); i++){
			cmbV2VOperation.addItem(methods.get(i));
			}
		cmbSmoothingOperation.removeAllItems();
		methods = volume_engine.getMethods("Smooth Volume");
		for (int i = 0; i < methods.size(); i++){
			cmbSmoothingOperation.addItem(methods.get(i));
			}
		
		updateAnything = true;
	}
	
	protected void updateOperations(){
		if (!updateAnything) return;
		
		// V2V operation list
		Object sel = cmbV2VTarget.getSelectedItem();
		cmbV2VTarget.removeAllItems();
		List<Shape3DInt> volumes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Volume3DInt());
		for (Shape3DInt volume : volumes){
			if (currentVolume == null || volume != currentVolume)
				cmbV2VTarget.addItem(volume);
			}
		if (sel != null)
			cmbV2VTarget.setSelectedItem(sel);
		
		String op = (String)cmbV2VOperation.getSelectedItem();
		if (op == null){
			lstV2VParameters.setAttributes(null);
		}else{
			lstV2VParameters.setAttributes(volume_engine.getAttributes("Map Volume to Volume (" + op + ")"));
			}
		
		// Smoothing operation list
		op = (String)cmbSmoothingOperation.getSelectedItem();
		if (op == null){
			lstSmoothingParameters.setAttributes(null);
		}else{
			lstSmoothingParameters.setAttributes(volume_engine.getAttributes("Smooth Volume (" + op + ")"));
			}
		
		lstBlobParameters.setAttributes(volume_engine.getAttributes("Get Blobs from Volume"));
		
		
		updateOperationColumns();
	}
	
	protected void updateOperationColumns(){
		if (!updateAnything) return;
		if (currentVolume == null) return;
		
		String op = (String)cmbV2VOperation.getSelectedItem();
		if (op != null) {
			
			AttributeList attr_list = lstV2VParameters.getAttributes();
			AttributeSelection<String> attr = (AttributeSelection<String>)attr_list.getAttribute("source_column");
			if (attr != null){
				String value = attr.getValue();
				attr.setList(currentVolume.getVertexDataColumnNames());
				if (value != null) attr.setValue(value);
				Volume3DInt target_vol = (Volume3DInt)cmbV2VTarget.getSelectedItem();
				if (target_vol != null){
					attr = (AttributeSelection<String>)attr_list.getAttribute("target_column");
					value = attr.getValue();
					attr.setList(target_vol.getVertexDataColumnNames());
					if (value != null) attr.setValue(value);
					}
				}
			}
		
		op = (String)cmbSmoothingOperation.getSelectedItem();
		if (op != null) {
			AttributeList attr_list = lstSmoothingParameters.getAttributes();
			AttributeSelection<String> attr = (AttributeSelection<String>)attr_list.getAttribute("source_column");
			if (attr != null){
				String value = attr.getValue();
				attr.setList(currentVolume.getVertexDataColumnNames());
				if (value != null) attr.setValue(value);
				
				attr = (AttributeSelection<String>)attr_list.getAttribute("target_column");
				value = attr.getValue();
				attr.setList(currentVolume.getVertexDataColumnNames());
				if (value != null) attr.setValue(value);
				}
			}
		
		AttributeList attr_list = lstBlobParameters.getAttributes();
		AttributeSelection<String> attr = (AttributeSelection<String>)attr_list.getAttribute("source_column");
		if (attr != null){
			String value = attr.getValue();
			attr.setList(currentVolume.getVertexDataColumnNames());
			if (value != null) attr.setValue(value);
			
			attr = (AttributeSelection<String>)attr_list.getAttribute("target_column");
			value = attr.getValue();
			attr.setList(currentVolume.getVertexDataColumnNames());
			if (value != null) attr.setValue(value);
			}
		
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
	
	//update values based upon current volume, if one exists
	private void updateValues(){
		Grid3D grid = null;
		Volume3DInt volume = currentVolume;
		if (isNewGrid()){
			volume = getDefaultVolume();
			cmdUpdateSpecs.setEnabled(false);
		}else{
			volume = currentVolume;
			cmdUpdateSpecs.setEnabled(true);
			}
		
		updateColumns();
		
		grid = volume.getGrid();
		float geom[] = grid.getGeomDims();
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
	
		Point3f origin = grid.getOrigin();
		txtOriginX.setText(MguiDouble.getString(origin.getX(), 6));
		txtOriginY.setText(MguiDouble.getString(origin.getY(), 6));
		txtOriginZ.setText(MguiDouble.getString(origin.getZ(), 6));
		
		txtGeomX.setText(MguiDouble.getString(geom[0], 6));
		txtGeomY.setText(MguiDouble.getString(geom[1], 6));
		txtGeomZ.setText(MguiDouble.getString(geom[2], 6));
		
		txtDataX.setText(new MguiInteger(x_size).toString());
		txtDataY.setText(new MguiInteger(y_size).toString());
		txtDataZ.setText(new MguiInteger(z_size).toString());
		
		Vector3f x_axis = grid.getSAxis();
		x_axis.normalize();
		Vector3f y_axis = grid.getTAxis();
		y_axis.normalize();
		Vector3f z_axis = grid.getRAxis();
		z_axis.normalize();
		
		txtAxisX_X.setText(new MguiFloat(x_axis.getX()).toString("#0.00000"));
		txtAxisX_Y.setText(new MguiFloat(x_axis.getY()).toString("#0.00000"));
		txtAxisX_Z.setText(new MguiFloat(x_axis.getZ()).toString("#0.00000"));
		
		txtAxisY_X.setText(new MguiFloat(y_axis.getX()).toString("#0.00000"));
		txtAxisY_Y.setText(new MguiFloat(y_axis.getY()).toString("#0.00000"));
		txtAxisY_Z.setText(new MguiFloat(y_axis.getZ()).toString("#0.00000"));
		
		txtAxisZ_X.setText(new MguiFloat(z_axis.getX()).toString("#0.00000"));
		txtAxisZ_Y.setText(new MguiFloat(z_axis.getY()).toString("#0.00000"));
		txtAxisZ_Z.setText(new MguiFloat(z_axis.getZ()).toString("#0.00000"));
		
		chkIsComposite.setSelected(volume.isComposite());
		
		updateAnything = false;
		
		GridVertexDataColumn current_column = getSelectedColumn();
		if (current_column == null){
			String column_name = volume.getCurrentColumn();
			if (column_name == null){
				updateAnything = true;
				updateIntensity();
				updateSpecBoxes();
				return;
				}
			//cmbDataColumn.setSelectedItem(volume.getVertexDataColumn(column_name));
			setSelectedColumn(column_name);
			}
		
		current_column = getSelectedColumn();
		if (current_column == null) return;
		cmbDataType.setSelectedItem(DataTypes.getDataBufferTypeStr(current_column.getDataTransferType()));
		
		updateAnything = true;
		
		updateIntensity();
		updateSpecBoxes();
		
	}
	
	public static Volume3DInt getDefaultVolume(){
		Box3D box = new Box3D();
		
		//base point
		box.setBasePt(new Point3f(0,0,0));
		
		//basis vectors
		Vector3f v = new Vector3f(1, 0, 0);
		box.setSAxis(v);
		v = new Vector3f(0, 1, 0);
		box.setTAxis(v);
		v = new Vector3f(0, 0 , 1);
		box.setRAxis(v);
		
		ColourMap cmap = ContinuousColourMap.getGreyScale();
		
		WindowedColourModel model = (WindowedColourModel)VolumeFunctions.getColourModel(DataBuffer.TYPE_DOUBLE,
																						cmap,
																						true);
		Grid3D grid = new Grid3D(10,10,10, box);
		Volume3DInt volume = new Volume3DInt(grid);
	
		int n = grid.getSize();
		ArrayList<MguiDouble> values = new ArrayList<MguiDouble>(n);
		for (int i = 0; i < n; i++)
			values.add(new MguiDouble(0));
		
		volume.addVertexData("Default", DataBuffer.TYPE_DOUBLE);
		volume.setCurrentColumn("Default");
		
		return volume;
	}
	
	protected void updateMaskShapePanel(){
		
		if (!updateAnything) return;
		
		pnlMaskShape.removeAll();
		String s = (String)cmbMaskMergeShape.getSelectedItem();
		
		if (s == null) return;
		
		if (s.endsWith("plane")){
			LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.0, 0.3, 1);
			pnlMaskShape.add(new JLabel("Section set:"), c);
			
			cmbMaskShapeCombo1 = new JComboBox();
			SectionSet3DInt section_set = new SectionSet3DInt();
			List<Shape3DInt> shape_sets = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(section_set);
			
			for (Shape3DInt set : shape_sets) {
				cmbMaskShapeCombo1.addItem(set);
				}
			
			pnlMaskShape.add(cmbMaskShapeCombo1, c);
			txtMaskShapeText1 = new JTextField("0");
			c = new LineLayoutConstraints(1, 1, 0.3, 0.7, 1);
			pnlMaskShape.add(cmbMaskShapeCombo1, c);
			c = new LineLayoutConstraints(2, 2, 0.0, 0.3, 1);
			pnlMaskShape.add(new JLabel("Section:"), c);
			c = new LineLayoutConstraints(2, 2, 0.3, 0.7, 1);
			pnlMaskShape.add(txtMaskShapeText1, c);
			pnlMaskShape.updateUI();
			return;
			}
		
	}
	
	protected void updateMasking(){

		updateAnything = false;
		Object current = cmbMask.getSelectedItem();
		
		//mask combo
		cmbMask.removeAllItems();
		cmbMask.addItem(NEW_MASK);
		
		if (cmbGrid.getSelectedItem() == null){
			updateAnything = true;
			return;
			}
		
		if (isNewGrid()){
			enableMasking();
			updateAnything = true;
			return;		
			}
		
		HashMap<String, boolean[][][]> masks = currentVolume.getMasks();
		
		Iterator<String> itr = masks.keySet().iterator();
		while (itr.hasNext()) cmbMask.addItem(itr.next());
		
		if (current == null)
			cmbMask.setSelectedItem(NEW_MASK);
		else
			cmbMask.setSelectedItem(current);
		
		enableMasking();
		
		cmbMaskMergeShape.removeAllItems();
		cmbMaskMergeShape.addItem("Above plane");
		cmbMaskMergeShape.addItem("Below plane");
		
		updateAnything = true;
		
		updateMaskShapePanel();
		
	}
	
	private boolean reset_histogram = false;
	
	protected void setHistogram(boolean as_job){
		if (currentVolume == null){
			pnlHistPanel.setHistogram(null);
			pnlHistPanel.repaint();
			return;
			}
		
		// If the panel is already busy responding to a previous call, set
		// the flag to indicate that it should recall this method once it
		// finishes.
		if (as_job && pnlHistPanel.isBusy()){
			reset_histogram = true;
			return;
			}
		
		final int t = 0;
		final int bins = Integer.valueOf(txtHistBins.getText()).intValue();
		final double min = Double.valueOf(txtHistMin.getText()).doubleValue();
		final double max = Double.valueOf(txtHistMax.getText()).doubleValue();
		
		Histogram histogram = null;
		
		//as_job=false;
		if (!as_job){
			histogram = getHistogramBlocking(currentVolume, t, bins, min, max);
		}else{
			//run this as worker thread so as not to freeze UI
			
			pnlHistPanel.setBusy(true);
			histogram = (Histogram)Worker.post(new Job(){
			
				public Histogram run(){
					return getHistogramBlocking(currentVolume, t, bins, min, max);
					}
			
				});
			pnlHistPanel.setBusy(false);
			// If set histogram has been called while this one was updating,
			// call this method again.
			if (reset_histogram){
				reset_histogram = false;
				setHistogram(true);
				return;
				}
			}
		
		reset_histogram = false;
		if (histogram == null) return;
		pnlHistPanel.setHistogram(histogram);
		pnlHistPanel.y_scale = Float.valueOf(txtHistYScale.getText());
		pnlHistPanel.colour_model = imgIntensity.model;
		pnlHistPanel.revalidate();
		pnlHistPanel.repaint();
		
	}
	
	protected void resetHistogram(){
		if (currentVolume == null || currentVolume.getGrid() == null) return;
		
		GridVertexDataColumn column = getSelectedColumn();
		if (column == null) return;
		txtHistMin.setText("" + column.getDataMin());
		txtHistMax.setText("" + column.getDataMax());
	}
	
	protected GridVertexDataColumn getSelectedColumn(){
		if (currentVolume == null) return null;
		int row = vertex_data_model.is_current;
		if (row < 0) return null;
		return (GridVertexDataColumn)currentVolume.getVertexDataColumn(vertex_data_model.columns.get(row));
	}
	
	protected void setSelectedColumn(String column){
		if (currentVolume == null) return;
		
		int row = vertex_data_model.findVertexColumn(column);
		if (row < 0) return;
		
		vertex_data_table.setRowSelectionInterval(row, row);
		
	}
	
	/**************************************
	 * 
	 * 
	 * @param grid
	 * @param t
	 * @param bins
	 * @param min
	 * @param max
	 */
	protected Histogram getHistogramBlocking(Volume3DInt volume, int t, int bins, double min, double max){
		if (volume == null){
			return null;
			}
		
		GridVertexDataColumn column = getSelectedColumn();
		if (column == null) return null;
		
		return VolumeFunctions.getHistogram(volume, column.getName(), t, bins, min, max);

	}
	
	protected void updateHistogram(){
		if (pnlHistPanel.histogram != null && pnlHistPanel.colour_model != null)
			pnlHistPanel.repaint();
	}
	
	protected void updateIntensity(){
		
		GridVertexDataColumn v_column = getSelectedColumn();
		if (v_column == null || currentVolume == null){ // || !(currentVolume.getGrid() instanceof Grid3D)){
			imgIntensity.setImage(null, 0);
			imgIntensity.setVolume(null, null);
			updateHistogram();
			return;
			}
		
		Grid3D grid = (Grid3D)currentVolume.getGrid();
		
		imgIntensity.setVolume(currentVolume, 
							   v_column.getName(), 
							   vertex_data_model.getColourModel(v_column.getName()));
		
		//set controls
		int n = grid.getSizeR();
		
		updateIntensity = false;
		int v = sldIntSlice.getValue();
		sldIntSlice.setMaximum(n - 1);
		if (v > n - 1) sldIntSlice.setValue(n / 2);
		
		updateIntensityValues();
		ColourMap cmap = (ColourMap)cmbIntColourMap.getSelectedItem();
		if (cmap == null || cmap != imgIntensity.model.getColourMap())
			cmbIntColourMap.setSelectedItem(imgIntensity.model.getColourMap());
		updateIntensity = true;
		
		updateIntensitySlice();
	}
	
	void updateIntensitySlice(){
		if (currentVolume == null || !(currentVolume.getGrid() instanceof Grid3D)){
			imgIntensity.setImage(null, 0);
			return;
			}
		
		Grid3D grid = (Grid3D)currentVolume.getGrid();
		GridVertexDataColumn column = getSelectedColumn(); 
		
		if (column == null){
			imgIntensity.setImage(null, 0);
			return;
			}
		
		if (!(column.getColourModel() instanceof WindowedColourModel)){
			imgIntensity.setImage(null, 0);
			imgIntensity.setVolume(null, null);
			return;
			}
		
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		
		// TODO: Implement other section orientations
		
		BufferedImage image = null;
		
		if (!chkIsComposite.isSelected()){
			image = column.getRSliceImage(sldIntSlice.getValue());
		}else{
			image = VolumeFunctions.getCompositeRSliceImage(currentVolume,
															sldIntSlice.getValue(),
															vertex_data_model.columns,
															vertex_data_model.colour_models,
															vertex_data_model.alphas,
															vertex_data_model.is_composite);
			imgIntensity.setImage(image, sldIntSlice.getValue());
			pnlHistPanel.colour_model = imgIntensity.model;
			imgIntensity.z = sldIntSlice.getValue();
			imgIntensity.repaint();
			updateHistogram();
			return;
			}
			
			WindowedColourModel model = imgIntensity.model;
			
			WritableRaster raster = (WritableRaster)image.getData(new Rectangle(0, 0, x_size, y_size));
			WritableRaster raster2 = model.createCompatibleWritableRaster(x_size, y_size);
			
			raster2.setDataElements(0, 0, raster);
			BufferedImage image2 = new BufferedImage(model, raster2, 
													 false, null);
			pnlHistPanel.colour_model = model;
			imgIntensity.setImage(image2, sldIntSlice.getValue());
			updateHistogram();

	}
	
	protected void updateIntensityValues(){
		
		if (imgIntensity != null && imgIntensity.model != null){
			sldIntWidth.setValue((int)(1.0 / imgIntensity.model.getWindowWidth() * 10000.0));
			sldIntMid.setValue((int)(imgIntensity.model.getWindowMid() * 10000.0));
			sldIntAlphaMax.setValue((int)(imgIntensity.model.getAlphaMax() * 10000.0));
			sldIntAlphaMin.setValue((int)(imgIntensity.model.getAlphaMin() * 10000.0));
			sldIntAlpha.setValue((int)(imgIntensity.model.getAlpha() * 10000.0));
			txtIntScale.setText(MguiDouble.getString(imgIntensity.model.getScale(), 4));
			txtIntIntercept.setText(MguiDouble.getString(imgIntensity.model.getIntercept(), 4));
			txtIntSlice.setText("" + sldIntSlice.getValue());
			chkIntSetAlpha.setSelected(imgIntensity.model.getHasAlpha());
			chkLowIsTransparent.setSelected(imgIntensity.model.getLowIsTransparent());
			double[] limits = imgIntensity.model.getLimits();
			txtIntMin.setText(MguiDouble.getString(limits[0], 4));
			txtIntMax.setText(MguiDouble.getString(limits[1], 4));
			
			txtIntWidth.setText(NumberFunctions.getReasonableString(1.0 / imgIntensity.model.getWindowWidth()));
			txtIntMid.setText(NumberFunctions.getReasonableString(imgIntensity.model.getWindowMid()));
			txtIntAlphaMax.setText(NumberFunctions.getReasonableString(imgIntensity.model.getAlphaMax()));
			txtIntAlphaMin.setText(NumberFunctions.getReasonableString(imgIntensity.model.getAlphaMin()));
			txtIntAlpha.setText(NumberFunctions.getReasonableString(imgIntensity.model.getAlpha()));
			
			updateHistogram();
			}
		
	}
	
	protected void updateVolOptions(){
		if (volOptions == null)
			volOptions = new VolumeInputOptions();
		
		try{
			volOptions.dim_x = new Integer(txtDataX.getText()).intValue();
			volOptions.dim_y = new Integer(txtDataY.getText()).intValue();
			volOptions.dim_z = new Integer(txtDataZ.getText()).intValue();
			volOptions.geom_x = new Float(txtGeomX.getText()).floatValue();
			volOptions.geom_y = new Float(txtGeomY.getText()).floatValue();
			volOptions.geom_z = new Float(txtGeomZ.getText()).floatValue();
			volOptions.origin_x = new Float(txtOriginX.getText()).floatValue();
			volOptions.origin_y = new Float(txtOriginY.getText()).floatValue();
			volOptions.origin_z = new Float(txtOriginZ.getText()).floatValue();
			volOptions.transfer_type = DataTypes.getDataBufferType((String)cmbDataType.getSelectedItem());
			
		}catch (NumberFormatException e){
			InterfaceSession.log("InterfaceVolumePanel: NumberFormatException: " + txtOriginX.getText());
		}
		
	}
	
	@Override
	public void showPopupMenu(MouseEvent e){
		Component component = this.getComponentAt(e.getPoint());
		if (component == lblIntMid){
			last_click_point = e.getPoint();
			InterfacePopupMenu menu = new InterfacePopupMenu(this);
			JMenuItem item = new JMenuItem("Set range");
			menu.addMenuItem(item);
			item.setActionCommand("sldIntMid.Range");
			menu.show(e);
		}else{
			super.showPopupMenu(e);
			}
	}
	
	public void handlePopupEvent(ActionEvent e){
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		String test = item.getActionCommand();
		
		if (item.getActionCommand().startsWith("sldIntMid")){
			String range = ((double)current_mid_min / 10000.0) + " " + ((double)current_mid_max / 10000.0);
			String new_range = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
															"Set range for intensity brightness", 
															range);
			if (new_range == null || !new_range.contains(" ")) return;
			String[] vals = new_range.split(" ");
			current_mid_min = (int)(Double.valueOf(vals[0]) * 10000);
			current_mid_max = (int)(Double.valueOf(vals[1]) * 10000);
			int sld_val = (int)((double)current_mid_min + (double)(current_mid_max - current_mid_min) / 2.0);
			sldIntMid.setMinimum(current_mid_min);
			sldIntMid.setMaximum(current_mid_max);
			sldIntMid.setValue(sld_val);
			sldIntMid.updateUI();
			slidersJustChanged = true;
			}
		
	}
	
	public void updateFromDialog(InterfaceDialogBox box){
	
		if (box instanceof VolumeInputDialogBox){
			volOptions = ((VolumeInputDialogBox)box).getOptions();
			setParameters(volOptions);
			return;
			}
		
	}
	
	//update volume from parameters
	protected boolean setParameters(InterfaceOptions p) {
		
		if (p == null) return false;
		
		if (p instanceof VolumeInputOptions){
			VolumeInputOptions options = (VolumeInputOptions)p;
			
			if (options.set_dims)
				this.setDataDims(options.dim_x, options.dim_y, options.dim_z);
			if (options.set_geom)
				this.setGeomDims(options.geom_x, options.geom_y, options.geom_z);
			if (options.set_origin)
				this.setImgOrigin(options.origin_x, options.origin_y, options.origin_z);
			if (options.axis_x != null)
				setAxes(options.axis_x, options.axis_y, options.axis_z);
			if (options.set_type)
				cmbDataType.setSelectedItem(DataTypes.getDataBufferTypeStr(options.transfer_type));
			else
				cmbDataType.setSelectedItem("TYPE_FLOAT");
			
			if (volOptions != p){
				volOptions = new VolumeInputOptions();
				updateVolOptions();
				volOptions.setFrom(options);
				}
			
			return true;
			}
		
		return false;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		
		updateSpecBoxes();
		
		
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void updateButtons(){
		String apply = "Create";
		String delete = "Preview";
		
		if (isNewGrid()){
			updateAnything = false;
			chkUpdateCurrentColumn.setEnabled(false);
			chkAddNewColumn.setText("Create volume:");
			chkAddNewColumn.setSelected(true);
			updateAnything = true;
			cmdApply.setActionCommand("Create");
		}else{
			chkAddNewColumn.setText("Add column:");
			chkUpdateCurrentColumn.setEnabled(true);
			if (chkUpdateCurrentColumn.isSelected()){
				apply = "Update";
			}else{
				apply = "Add Column";
				}
			delete = "Delete";
			}
		
		cmdApply.setText(apply);
		cmdDelete.setText(delete);
		cmdApply.setActionCommand(apply);
		cmdDelete.setActionCommand(delete);
		
		cmdZeroes.setEnabled(chkSetNone.isSelected());
		cmdVolumeFile.setEnabled(chkSetVolFile.isSelected());
		cmdImgStack.setEnabled(chkSetImgStack.isSelected());
		
		//cmdDynamicFiles.setEnabled(chkDynamicFiles.isSelected());
		
		cmdAddColumn.setEnabled(!isNewGrid());
		
	}
	
	private void updateSetBoxes(String chk){
		if (chk.equals("Set None")){
			chkSetNone.setSelected(true);
			chkSetVolFile.setSelected(false);
			chkSetImgStack.setSelected(false);
			}
		if (chk.equals("Set Volume File")){
			chkSetVolFile.setSelected(true);
			chkSetImgStack.setSelected(false);
			chkSetNone.setSelected(false);
			}
		if (chk.equals("Set Image Stack")){
			chkSetImgStack.setSelected(true);
			chkSetNone.setSelected(false);
			chkSetVolFile.setSelected(false);
			}
//		if (chk.equals("Set Dynamic None")){
//			chkDynamicNone.setSelected(true);
//			chkDynamicFiles.setSelected(false);
//			chkDynamicVolume.setSelected(false);
//			}
//		if (chk.equals("Set Dynamic Volume")){
//			chkDynamicVolume.setSelected(true);
//			chkDynamicFiles.setSelected(false);
//			chkDynamicNone.setSelected(false);
//			}
//		if (chk.equals("Set Dynamic Files")){
//			chkDynamicFiles.setSelected(true);
//			chkDynamicNone.setSelected(false);
//			chkDynamicVolume.setSelected(false);
//			}
		updateButtons();
	}
	
	void applyShapeMask(){
		
		String s = (String)cmbMaskMergeShape.getSelectedItem();
		if (s == null) return;
		
		if (s.endsWith("plane")){
			
			SectionSet3DInt section_set = (SectionSet3DInt)cmbMaskShapeCombo1.getSelectedItem();
			if (section_set == null) return;
			boolean is_above = s.substring(0, s.indexOf(" plane")).equals("Above");
			Plane3D plane = section_set.getPlaneAt(Integer.valueOf(txtMaskShapeText1.getText()));
			
			ShapeFunctions.unionMaskVolumeWithPlane(current_mask, currentVolume, plane, is_above);
			currentVolume.setApplyMasks(currentVolume.getApplyMasks());
			
			return;
			}
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		// ADD new column check box changed
		if (e.getActionCommand().equals("Is Add New Column")){
			if (!updateAnything) return;
			updateAnything = false;
			if (isNewGrid()){
				chkAddNewColumn.setSelected(true);
			}else{
				chkUpdateCurrentColumn.setSelected(!chkAddNewColumn.isSelected());
				}
			updateAnything = true;
			updateButtons();
			return;
			}
		
		// UPDATE existing column check box changed
		if (e.getActionCommand().equals("Is Update Current Column")){
			if (!updateAnything) return;
			updateAnything = false;
			if (isNewGrid()){
				chkAddNewColumn.setSelected(true);
			}else{
				chkAddNewColumn.setSelected(!chkUpdateCurrentColumn.isSelected());
				}
			updateAnything = true;
			updateButtons();
			return;
			}
		
		// The current column changed
		if (e.getActionCommand().equals("Current Column Changed")){
			if (!updateAnything) return;
			
			updateValues();
			updateVolOptions();
			updateButtons();
			
			resetHistogram();
			setHistogram(SwingUtilities.isEventDispatchThread());
			
			return;
			}
		
		// SPECIFICATIONS for the current volume (Geometry, dimensions) have changed
		if (e.getActionCommand().startsWith("Specifications")){
			
			//apply specification changes to volume
			if (e.getActionCommand().endsWith("Apply")){
				
				if (currentVolume == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No current volume!", 
												  "Update Specifications", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				Grid3D grid = getGridFromSpecs();
				currentVolume.setGrid(grid, true);
				return;
				}
			}
		
		if (e.getActionCommand().startsWith("Set")){
			updateSetBoxes(e.getActionCommand());
			return;
			}
		
		if (e.getActionCommand().startsWith("Move Column")){
			if (currentVolume == null || currentVolume.getVertexDataColumns().size() < 2)
				return;
			int idx = vertex_data_table.getSelectedRow();
			if (idx < 0) return;
			
			int new_idx = idx - 1;
			if (e.getActionCommand().endsWith("Down"))
				new_idx = idx + 1;
			if (new_idx < 0 || new_idx >= vertex_data_model.columns.size())
				return;
			vertex_data_model.swapIndex(idx, new_idx);
			updateIntensity();
			
			vertex_data_table.getSelectionModel().setSelectionInterval(new_idx, new_idx);
			
			return;
			}
		
		if (e.getActionCommand().equals("Define Geometry 2D")){
			lastTool = InterfaceSession.getDisplayPanel().getCurrentTool2D();
			InterfaceSession.getDisplayPanel().setCurrentTool(new ToolDefine3DGrid2D(this));
			return;
			}
		
		// MASKING  new column check box changed
		if (e.getActionCommand().startsWith("Mask")){
			
			if (e.getActionCommand().endsWith("Options")){
				volumeMaskOptions.setVolume(currentVolume);
				maskDialog = new VolumeMaskDialog(InterfaceSession.getSessionFrame(), volumeMaskOptions);
				maskDialog.setVisible(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Clear")){
				if (current_mask == null) return;
				String name = txtMaskName.getText();
				currentVolume.clearMask(name);
				return;
				}
			
			if (e.getActionCommand().endsWith("Changed Name")){
				enableMasking();
				return;
				}
			
			if (e.getActionCommand().endsWith("Set Active")){
				if (cmbMask.getSelectedItem().equals(NEW_MASK)) return;
				
				currentVolume.setMaskApplied((String)cmbMask.getSelectedItem(),
										 chkMaskActive.isSelected());
				return;
				
			}
			
			if (e.getActionCommand().endsWith("Apply Shape")){
				
				applyShapeMask();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Shape Changed")){
				updateMaskShapePanel();
				return;
				}
			
			if (e.getActionCommand().endsWith("Changed")){
				if (!updateAnything) return;
				if (cmbMask.getSelectedItem().equals(NEW_MASK)){
					cmdMaskAddUpdate.setText("Add");
					current_mask = null;
					enableMasking();
				}else{
					cmdMaskAddUpdate.setText("Update");
					String name = (String)cmbMask.getSelectedItem();
					current_mask = currentVolume.getMask(name);
					if (current_mask != null)
						chkMaskActive.setSelected(currentVolume.isMaskApplied(name));
					enableMasking();
					return;
					}
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Invert")){
				
				if (current_mask == null) return;
				String name = txtMaskName.getText();
				currentVolume.invertMask(name);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Add")){
				String name = txtMaskName.getText();
				
				if (name == null || name.length() == 0){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Invalid name for mask", 
												  "Create volume mask", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				Grid3D grid = currentVolume.getGrid();
				current_mask = new boolean[grid.getSizeS()][grid.getSizeT()][grid.getSizeR()];
				currentVolume.addMask(name, current_mask);
				updateMasking();
				cmbMask.setSelectedItem(name);
				return;
				}
			
			if (e.getActionCommand().endsWith("File Browse")){
				
				VolumeInputOptions options = new VolumeInputOptions();
				Volume3DInt volume = VolumeInputDialogBox.showDialog(this, options, false);
				if (volume == null) 
					return;
				if (!currentVolume.unionMaskWithVolume((String)cmbMask.getSelectedItem(), volume)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error setting mask from volume file..", 
												  "Set mask from file", JOptionPane.ERROR_MESSAGE);
					return;
					}
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Set mask from volume file.", 
											  "Set mask from file", JOptionPane.INFORMATION_MESSAGE);
				}
			
			return;
			}
		
		// HISTOGRAM options
		if (e.getActionCommand().startsWith("Histogram")){
			
			if (e.getActionCommand().endsWith("Apply")){
				setHistogram(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Reset")){
				resetHistogram();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Y Scale Changed")){
				float scale = Float.valueOf(txtHistYScale.getText());
				sldHistYScale.setValue((int)(scale * 10000));
				return;
				}
			
			}
		
		// VOLUME to volume mapping
		if (e.getActionCommand().startsWith("V2V")){
			
			if (e.getActionCommand().endsWith("Change Operation")){
				updateOperations();
				return;
				}
			
			if (e.getActionCommand().endsWith("Change Target")){
				updateOperationColumns();
				return;
				}
			
			if (e.getActionCommand().endsWith("Execute")){
				
				String op = (String)cmbV2VOperation.getSelectedItem();
				Volume3DInt target = (Volume3DInt)cmbV2VTarget.getSelectedItem();
				if (target == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No target volume selected.", 
												  "Map volume to volume", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
					
				InterfaceProgressBar progress = new InterfaceProgressBar("Mapping volume to volume:");
				
				progress.register();
				if (volume_engine.mapVolumeToVolume(currentVolume, target, op, progress)){
					JOptionPane.showMessageDialog(InterfaceSession.getDisplayPanel(), 
												  "Mapping successful.", 
												  "Map volume to volume", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getDisplayPanel(), 
												  "Error mapping between volumes. See log.", 
												  "Map volume to volume", 
												  JOptionPane.ERROR_MESSAGE);
					}
				progress.deregister();
				
				return;
				}
			
			return;
			}
		
		// SMOOTHING
		if (e.getActionCommand().startsWith("Smoothing")){
			
			if (e.getActionCommand().endsWith("Change Operation")){
				updateOperations();
				return;
				}
			
			if (e.getActionCommand().endsWith("Change Target")){
				updateOperationColumns();
				return;
				}
			
			if (e.getActionCommand().endsWith("Execute")){
				
				String op = (String)cmbSmoothingOperation.getSelectedItem();
				
				InterfaceProgressBar progress = new InterfaceProgressBar("Smoothing volume:");
				
				progress.register();
				if (volume_engine.smoothVolume(currentVolume, op, progress)){
					JOptionPane.showMessageDialog(InterfaceSession.getDisplayPanel(), 
												  "Mapping successful.", 
												  "Map volume to volume", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getDisplayPanel(), 
												  "Error mapping between volumes. See log.", 
												  "Map volume to volume", 
												  JOptionPane.ERROR_MESSAGE);
					}
				progress.deregister();
				
				return;
				}
			
			return;
			}
		
		if (e.getActionCommand().startsWith("Blob")){
			if (currentVolume == null) return;
			if (e.getActionCommand().endsWith("Execute")){
				if (volume_engine.getBlobsFromVolume(currentVolume)){
					JOptionPane.showMessageDialog(InterfaceSession.getDisplayPanel(), 
												  "Got some blobs successfully.", 
												  "Get blobs from volume", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getDisplayPanel(), 
												  "Error getting blobs. See log.", 
												  "Get blobs from volume", 
												  JOptionPane.ERROR_MESSAGE);
					}
				}
			return;
			}
		
		if (e.getActionCommand().startsWith("Isosurf")){
			if (currentVolume == null) return;
			if (e.getActionCommand().endsWith("Execute")){
				String column = (String)cmbIsosurfColumn.getSelectedItem();
				if (column == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No column selected!", 
												  "Get Isosurface from Volume", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				double isovalue = 0;
				try{
					isovalue = Double.valueOf(txtIsosurfLevel.getText());
				}catch (NumberFormatException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Invalid value for level (must be numeric)!", 
							  "Get Isosurface from Volume", 
							  JOptionPane.ERROR_MESSAGE);
					return;
					}
				String name = txtIsosurfName.getText();
				if (name.length() == 0){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Surface must have a valid name!", 
												  "Get Isosurface from Volume", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				ShapeSet3DInt shape_set = (ShapeSet3DInt)cmbIsosurfShapeSet.getSelectedItem();
				if (shape_set == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No shape set selected!", 
												  "Get Isosurface from Volume", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				AttributeList attributes = volume_engine.getAttributes("Get Isosurface from Volume");
				attributes.setValue("isovalue", new MguiDouble(isovalue));
				attributes.setValue("source_column", column);
				attributes.setValue("surface_name", name);
				attributes.setValue("shape_set", shape_set);
				
				if (volume_engine.getIsosurfaceFromVolume(currentVolume)){
					JOptionPane.showMessageDialog(InterfaceSession.getDisplayPanel(), 
												  "Created isosurface.", 
												  "Get Isosurface from Volume", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getDisplayPanel(), 
											  "Error creating isosurface. See log for details.", 
											  "Get Isosurface from Volume", 
											  JOptionPane.ERROR_MESSAGE);
					}
				
				}
			
			
			}
		
		// COMPOSITE check box changed
		if (e.getActionCommand().equals("Is Composite")){
			this.updateIntensity();
			return;
			}
		
		// APPLY the current intensity options (colour map, alpha, etc.)
		if (e.getActionCommand().equals("Apply Intensity")){
			if (currentVolume == null) return;
		
			String current_column = vertex_data_model.getCurrentColumn();
			if (current_column == null) return;
			
			// Set alpha mode
			currentVolume.hasAlpha(chkIntSetAlpha.isSelected());
			currentVolume.getColourModel().setLowIsTransparent(chkLowIsTransparent.isSelected());
			
			// Set all colour models
			for (int i = 0; i < vertex_data_model.columns.size(); i++){
				String column = vertex_data_model.columns.get(i);
				GridVertexDataColumn v_column = (GridVertexDataColumn)currentVolume.getVertexDataColumn(column);
				WindowedColourModel live_model = v_column.getColourModel();
				live_model.setFromColourModel(vertex_data_model.getColourModel(column));
				}
			
			// Set alphas and visibility
			for (int i = 0; i < vertex_data_model.alphas.size(); i++){
				String column = vertex_data_model.columns.get(i);
				currentVolume.setCompositeAlpha(column, vertex_data_model.getAlpha(column), false);
				currentVolume.showInComposite(column,vertex_data_model.isInComposite(column),false);
				currentVolume.setCompositeIndex(column, i, false);
				}
			
			// Set composite status
			currentVolume.isComposite(chkIsComposite.isSelected(), false);
			
			// Update columns
			for (int i = 0; i < vertex_data_model.columns.size(); i++){
				String column = vertex_data_model.columns.get(i);
				GridVertexDataColumn v_column = (GridVertexDataColumn)currentVolume.getVertexDataColumn(column);
				v_column.colourModelChanged(false);
				}
			
			// Update texture
			currentVolume.setCurrentColumn(current_column, false);
			currentVolume.updateTexture();
			currentVolume.fireShapeModified();
			InterfaceSession.getDisplayPanel().updateDisplays();
			int slice = sldIntSlice.getValue();
			updateIntensity();
			sldIntSlice.setValue(slice);
			return;
			}
		
		// RESET the current intensity options from the current volume
		if (e.getActionCommand().equals("Reset Intensity")){
			updateIntensity();
			return;
		}
		
		// ALPHA check box has changed
		if (e.getActionCommand().equals("Intensity Set Alpha")){
			WindowedColourModel model = imgIntensity.model;
			if (model == null) return;
			model.setHasAlpha(chkIntSetAlpha.isSelected());
			model.setLowIsTransparent(chkLowIsTransparent.isSelected());
			model.setColourMap(model.getColourMap()); //, chkIntSetAlpha.isSelected());
			updateIntensitySlice();
			updateHistogram();
			return;
			}
		
		// LOWISTRANSPARENT check box has changed
		if (e.getActionCommand().equals("Intensity Set LowIsTransparent")){
			WindowedColourModel model = imgIntensity.model;
			if (model == null) return;
			model.setHasAlpha(chkIntSetAlpha.isSelected());
			model.setLowIsTransparent(chkLowIsTransparent.isSelected());
			model.setColourMap(model.getColourMap()); //, chkIntSetAlpha.isSelected());
			updateIntensitySlice();
			updateHistogram();
			return;
			}
		
		// INVERT image command
		if (e.getActionCommand().equals("Invert Image")){
			
			if (currentVolume == null) return;
			
			Volume3DInt i_volume = VolumeFunctions.getInvertedVolume(currentVolume);
			if (i_volume == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						  "Failed to inverted volume.", 
						  "Invert volume data", 
						  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			currentVolume.setVertexData(i_volume.getVertexData());
			
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Inverted volume.", 
										  "Invert volume data", 
										  JOptionPane.INFORMATION_MESSAGE);
			
			return;
			
		}
		
		// INTENSITY has changed somehow; update the controls and histogram
		if (e.getActionCommand().startsWith("Intensity Changed")){
			
			if (e.getActionCommand().endsWith("Scale")){
				double val = Double.valueOf(txtIntScale.getText());
				this.imgIntensity.model.setScale(val);
				}
			
			if (e.getActionCommand().endsWith("Intercept")){
				double val = Double.valueOf(txtIntIntercept.getText());
				this.imgIntensity.model.setIntercept(val);
				}
			
			if (e.getActionCommand().endsWith("Slice")){
				int val = Integer.valueOf(txtIntSlice.getText()).intValue();
				sldIntSlice.setValue(val);
				}
			
			if (e.getActionCommand().endsWith("Width")){
				double val = Double.valueOf(txtIntWidth.getText()).doubleValue();
				sldIntWidth.setValue((int)(val * 10000.0));
				}
			
			if (e.getActionCommand().endsWith("Mid")){
				double val = Double.valueOf(txtIntMid.getText()).doubleValue();
				sldIntMid.setValue((int)(val * 10000.0));
				}
			
			if (e.getActionCommand().endsWith("Alpha Max")){
				double val = Double.valueOf(txtIntAlphaMax.getText()).doubleValue();
				sldIntAlphaMax.setValue((int)(val * 10000.0));
				}
			
			if (e.getActionCommand().endsWith("Alpha Min")){
				double val = Double.valueOf(txtIntAlphaMin.getText()).doubleValue();
				sldIntAlphaMin.setValue((int)(val * 10000.0));
				}
			
			if (e.getActionCommand().endsWith("Alpha")){
				double val = Double.valueOf(txtIntAlpha.getText()).doubleValue();
				sldIntAlpha.setValue((int)(val * 10000.0));
				}
			
			if (e.getActionCommand().equals("Intensity Changed Min") ||
					e.getActionCommand().equals("Intensity Changed Max")){
				imgIntensity.model.setLimits(Double.valueOf(txtIntMin.getText()), 
											 Double.valueOf(txtIntMax.getText()));
				updateIntensity();
				}
			
			updateHistogram();
			}
		
		// COLOUR MAP for the current intensity has changed; update stuff 
		if (e.getActionCommand().equals("Intensity Colour Map Changed")){
			if (!updateIntColourMap || !updateAnything){
				return;
				}
			ColourMap map = (ColourMap)cmbIntColourMap.getSelectedItem();
			if (map == null || imgIntensity.model == null){
				return;
				}
			if (chkIsComposite.isSelected()){
				vertex_data_model.getColourModel(vertex_data_model.getCurrentColumn()).setColourMap(map);
				updateIntensitySlice();
				return;
				}
			
			
			if (!imgIntensity.model.setColourMap(map)){ //, chkIntSetAlpha.isSelected())){
				InterfaceSession.log("Set map failed..");
				return;
				}
			this.updateIntensitySlice();
			updateHistogram();
			return;
			}
		
		// APPLY the current mask to the current volume
		if (e.getActionCommand().equals("Apply Mask")){
			if (currentVolume == null) return;
			currentVolume.addMask("mask", VolumeFunctions.getMask(currentVolume.getGrid(), maskOptions));
			currentVolume.setApplyMasks(true);
			return;
		}
		
		// GRID changed, update buttons
		if (e.getActionCommand().equals("Grid Changed")){
			if (!updateGridCombo || !updateAnything) return;
			if (cmbGrid.getSelectedItem() == null || isNewGrid())
				setCurrentVolume(null);
			else
				setCurrentVolume((Volume3DInt)cmbGrid.getSelectedItem());
		
			return;
		}
				
		// WRITE this volume to disk
		if (e.getActionCommand().equals("Write")){
			if (currentVolume == null) return;
			VolumeFileWriteOptions writeOptions = new VolumeFileWriteOptions();
			writeOptions.volume = this.currentVolume.getGrid();
			
			volumeFileWriteDialog.updateDialog(writeOptions);
			volumeFileWriteDialog.setVisible(true);
		}
		
		// CREATE button has been pressed; create new volume and make it current
		if (e.getActionCommand().equals("Create") && chkSetNone.isSelected()){
			
			updateVolOptions();
			
			if (volOptions.colour_map == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "You must specify a colour map", 
											  "Create/Update Volume",
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			// Name for column
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), "Name for new column?");
			if (name == null) return;
			
			Volume3DInt volume = getVolume(name);
			volume.setName(volOptions.name);
			ShapeSet3DInt set = (ShapeSet3DInt)cmbParentSet.getSelectedItem();
			if (set == null)
				set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
			
			set.addShape(volume);
			
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Volume '" + volume.getName() + "' created.", 
										  "Create/Update Volume",
										  JOptionPane.INFORMATION_MESSAGE);
			
			this.showPanel();
			setCurrentVolume(volume);
			
			return;
			}
		
		// ADD a new column to current volume
		if (e.getActionCommand().equals("Add Column")){
			
			if (chkSetNone.isSelected()){
				if (currentVolume == null) return;
				
				updateVolOptions();
				
				// Name for column
				//String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), "Name for new column?");
				//if (name == null) return;
				String name = volOptions.name;
				if (name == null) return;
				
				if (currentVolume.hasColumn(name) && 
						JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
													  "Overwrite existing column '" + name + "'?", 
													  "Add data column", 
													  JOptionPane.YES_NO_OPTION, 
													  JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
					return;
				
				//int transfer_type = DataTypes.getDataBufferType((String)cmbDataType.getSelectedItem());
				currentVolume.addVertexData(name, volOptions.transfer_type, volOptions.colour_map);
				currentVolume.setCurrentColumn(name);
				updateValues();
				resetHistogram();
				setHistogram(true);
				return;
				}
		
			if (currentVolume == null) return;
			String current_column = null;
			
			VolumeFileLoader loader = null;
			
			//set data from volume file
			if (chkSetVolFile.isSelected()){
				
				updateVolOptions();
				File file = volOptions.files[0];
				loader = volOptions.getLoader();
				loader.setFile(file);
				current_column = file.getName();
				if (current_column.contains("."))
					current_column = current_column.substring(0, current_column.lastIndexOf("."));
				
				if (currentVolume.hasColumn(current_column) && 
						JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
													  "Overwrite existing column '" + current_column + "'?", 
													  "Add data column", 
													  JOptionPane.YES_NO_OPTION, 
													  JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
					return;
				
				InterfaceProgressBar progress_bar = new InterfaceProgressBar();
				progress_bar.register();
				if (!loadVolume(currentVolume, current_column, loader, volOptions, progress_bar)){
					InterfaceSession.log("InterfaceVolumePanel: Problem loading data from file '"
										 + file.getAbsolutePath() + "'. Check log for details.", 
										 LoggingType.Errors);
					progress_bar.deregister();
					return;
					}
				progress_bar.deregister();
				currentVolume.setFileLoader(loader.getIOType());
				try{
					currentVolume.setUrlReference(file.toURI().toURL());
				}catch (MalformedURLException ex){
					InterfaceSession.log("InterfaceVolumePanel: Could not set URL reference for file '"
										 + file.getAbsolutePath() + "'", 
										 LoggingType.Warnings);
					}
				vertex_data_model.setVolume(currentVolume);
				updateColumns();
				setSelectedColumn(current_column);
				}
			
			return;
			}
			
		
		// CREATE or UPDATE grid from settings
		if (e.getActionCommand().equals("Create") ||
			e.getActionCommand().equals("Update")){
			
			updateVolOptions();
			
			if (volOptions.colour_map == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "You must specify a colour map", 
											  "Create/Update Volume",
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			String current_column = null;
			Volume3DInt volume = null;
			
			if (isNewGrid()){
				current_column = "Default";
				if (chkSetVolFile.isSelected() && volOptions != null && volOptions.files != null){
					File file = volOptions.files[0];
					current_column = file.getName();
					if (current_column.contains("."))
						current_column = current_column.substring(0, current_column.lastIndexOf("."));
					volume = getVolume(null);
				}else{
					current_column = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), "Name for new column?", current_column);
					if (current_column == null) return;
					volume = getVolume(current_column);
					}
				
			}else if (getSelectedColumn() == null){
				return;
			}else{
				current_column = getSelectedColumn().getName();
				volume = currentVolume;
				}
			
			VolumeFileLoader loader = null;
			
			//set data from volume file
			if (chkSetVolFile.isSelected() && volOptions != null && volOptions.files != null){
				
				File file = volOptions.files[0];
				loader = volOptions.getLoader();
				loader.setFile(file);
					
				InterfaceProgressBar progress_bar = new InterfaceProgressBar();
				progress_bar.setMessage("Reading volume file '" + file.getName() + "': ");
				progress_bar.register();
				
				//create new or update existing
				if (e.getActionCommand().equals("Create")){
					
					if (!loadVolume(volume, current_column, loader, volOptions, progress_bar)){
						progress_bar.deregister();
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
								"InterfaceVolumePanel: Error loading volume from '" + file.getAbsolutePath() + "' (see log).",
								"Load volume from file",
								JOptionPane.ERROR_MESSAGE);
						return;
						}
					volume.setFileLoader(loader.getIOType());
					try{
						volume.setUrlReference(file.toURI().toURL());
					}catch (MalformedURLException ex){
						InterfaceSession.log("InterfaceVolumePanel: Could not set URL reference for file '"
											 + file.getAbsolutePath() + "'", 
											 LoggingType.Warnings);
						}
					if (isNewGrid()){
						String name = file.getName();
						if (name.contains("."))
							name = name.substring(0, name.indexOf("."));
						volume.setName(name);
						volume.setScene3DObject();
						ShapeSet3DInt parent_set = (ShapeSet3DInt)cmbParentSet.getSelectedItem();
						setCurrentVolume(volume);
						parent_set.addShape(volume);
						updateGridList();
						}
					
				}else{
					if (chkSetNone.isSelected()){
						
					}else{
						try{
							currentVolume.setUrlReference(file.toURI().toURL());
						}catch (MalformedURLException ex){
							InterfaceSession.log("InterfaceVolumePanel: Could not set URL reference for file '"
												 + file.getAbsolutePath() + "'", 
												 LoggingType.Warnings);
							}
						try{
							updateVolOptions();
							if (!loader.setVolume3D(currentVolume, 0, volOptions, progress_bar)){
								InterfaceSession.log("InterfaceVolumePanel: Problem loading data from file '"
										 + file.getAbsolutePath() + "'. Check log for details.", 
										 LoggingType.Errors);
								}
						}catch (ShapeIOException ex){
							InterfaceSession.log("InterfaceVolumePanel: Problem loading data from file '"
									 + file.getAbsolutePath() + "'. Check log for details.", 
									 LoggingType.Errors);
							}
						}
					}
				
				progress_bar.deregister();
				
				InterfaceSession.getDisplayPanel().updateDisplays();
				if (cmbGrid.getSelectedItem().equals(NEW_GRID)){
					setCurrentVolume(null);
					}
				else{
					setCurrentVolume((Volume3DInt)cmbGrid.getSelectedItem());
					}
				}	//end of load volume file
			
			
		}
		
		// SPECIFY the data for the new column/volume
		if (e.getActionCommand().startsWith("Data")){
			if (e.getActionCommand().endsWith("Volume File")){
				if (isNewGrid()){
					volOptions.allow_dim_change = true;
					volOptions.allow_geom_change = true;
				}else{
					// Is an update of an existing column
					if (chkUpdateCurrentColumn.isSelected()){
						volOptions.allow_dim_change = currentVolume.getVertexDataColumns().size() == 1;
						volOptions.allow_geom_change = true;
					}else{
						volOptions.allow_geom_change = false;
						volOptions.allow_dim_change = false;
						}
					}
				VolumeInputDialogBox dialog = new VolumeInputDialogBox(this, volOptions, true);
				dialog.setVisible(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Zeroes")){
				NewVolumeDialog.showDialog(InterfaceSession.getSessionFrame(), volOptions);
				return;
				}
		}
		
		//***************** OPERATIONS ********************************
		
		if (e.getActionCommand().equals("Apply Flip Dim")){
			
			
			return;
			}
		
		if (e.getActionCommand().equals("Apply Rotation")){
			
			if (currentVolume == null) return;
			
			String angle = (String)this.cmbOpRotateAngle.getSelectedItem();
			String axis = (String)this.cmbOpRotateAxis.getSelectedItem();
			
			VolumeAxis v_axis = VolumeAxis.S;
			if (axis.equals("T Axis"))
				v_axis = VolumeAxis.T;
			if (axis.equals("R Axis"))
				v_axis = VolumeAxis.R;

			VolumeRotationAngle v_angle = VolumeRotationAngle.ROT90;			
			if (angle.equals("180"))
				v_angle = VolumeRotationAngle.ROT180;
			if (angle.equals("270 CW"))
				v_angle = VolumeRotationAngle.ROT270;
			
			InterfaceProgressBar progress_bar = new InterfaceProgressBar();
			progress_bar.setMessage("Rotating volume '" + currentVolume.getName() + "'.");
			progress_bar.register();
			
			Volume3DInt new_volume = VolumeFunctions.applyRotation(v_axis, v_angle, currentVolume, progress_bar);
			
			progress_bar.deregister();
			
			if (new_volume == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(),
												"Rotation Operation Failed", 
												"Rotate Grid3D", 
												JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			// TODO: this only rotates grid; want to replace values...
			currentVolume.setGrid(new_volume.getGrid());
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(),
											"Rotation Operation Successful", 
											"Rotate Grid3D", 
											JOptionPane.INFORMATION_MESSAGE);
			currentVolume.setScene3DObject();
			currentVolume.fireShapeModified();
			this.setCurrentVolume(currentVolume);
			return;
			}
		
	}
	
	public boolean loadVolume(Volume3DInt volume, String column, VolumeFileLoader loader, VolumeInputOptions options, ProgressUpdater progress_bar){
		if (loader == null) return false;
		try{
			return loader.setVolume3D(volume, column, 0, options, progress_bar);
		}catch (ShapeIOException e){
			InterfaceSession.log("InterfaceVolumePanel: Could not load volume.\nDetails: " + e.getMessage(), 
								 LoggingType.Errors);
			}
		return false;
	}
	
	protected Grid3D getGridFromSpecs(){
		Box3D box = new Box3D();
		
		//base point
		Point3f origin = new Point3f(Float.valueOf(txtOriginX.getText()).floatValue(),
								  	 Float.valueOf(txtOriginY.getText()).floatValue(),
								  	 Float.valueOf(txtOriginZ.getText()).floatValue());
		
		float[] geom_dims = new float[]{Float.valueOf(txtGeomX.getText()).floatValue(),
										Float.valueOf(txtGeomY.getText()).floatValue(),
										Float.valueOf(txtGeomZ.getText()).floatValue()};
		
		int[] data_dims = getDataDims();
		float s_size = geom_dims[0] / (float)data_dims[0];
		float t_size = geom_dims[1] / (float)data_dims[1];
		float r_size = geom_dims[2] / (float)data_dims[2];
		//origin.sub(new Vector3f(x_size*0.5f, y_size*0.5f, z_size*0.5f));
		
		Vector3f v_s = new Vector3f(Float.valueOf(txtAxisX_X.getText()).floatValue(),
								  Float.valueOf(txtAxisX_Y.getText()).floatValue(),
								  Float.valueOf(txtAxisX_Z.getText()).floatValue());
		v_s.normalize();
		v_s.scale(geom_dims[0] );
		box.setSAxis(new Vector3f(v_s));
		Vector3f v_t = new Vector3f(Float.valueOf(txtAxisY_X.getText()).floatValue(),
								   Float.valueOf(txtAxisY_Y.getText()).floatValue(),
								   Float.valueOf(txtAxisY_Z.getText()).floatValue());
		
		v_t.normalize();
		v_t.scale(geom_dims[1] );
		box.setTAxis(new Vector3f(v_t));
		Vector3f v_r = new Vector3f(Float.valueOf(txtAxisZ_X.getText()).floatValue(),
									 Float.valueOf(txtAxisZ_Y.getText()).floatValue(),
									 Float.valueOf(txtAxisZ_Z.getText()).floatValue());
		v_r.normalize();
		v_r.scale(geom_dims[2]);
		box.setRAxis(new Vector3f(v_r));
		
		v_s.normalize();
		v_s.scale(s_size * 0.5f);
		origin.sub(v_s);
		v_t.normalize();
		v_t.scale(t_size * 0.5f);
		origin.sub(v_t);
		v_r.normalize();
		v_r.scale(r_size * 0.5f);
		origin.sub(v_r);
		box.setBasePt(origin);
		
		return new Grid3D(Integer.valueOf(txtDataX.getText()).intValue(),
			 		     Integer.valueOf(txtDataY.getText()).intValue(),
			 		     Integer.valueOf(txtDataZ.getText()).intValue(),
			 		     box);
	}
	
	protected Volume3DInt getVolume(String column){
		
		Grid3D grid = getGridFromSpecs();
		
		Volume3DInt volume = new Volume3DInt(grid);
		
		if (column != null){
			cmbDataType.getSelectedItem();
			int transfer_type = DataTypes.getDataBufferType((String)cmbDataType.getSelectedItem());
			volume.addVertexData(column, transfer_type);
			volume.setColourMap(column, volOptions.colour_map);
			volume.setCurrentColumn(column);
			}
		
		volume.hasAlpha(volOptions.has_alpha);
		
		return volume;
	}
	
	public int[] getDataDims(){
		int[] ret = new int[3];
		ret[0] = Integer.valueOf(txtDataX.getText()).intValue();
		ret[1] = Integer.valueOf(txtDataY.getText()).intValue();
		ret[2] = Integer.valueOf(txtDataZ.getText()).intValue();
		return ret;
	}
		
	protected int getDataSize(String type){
		if (type.equals("intensity")) return 1;
		if (type.equals("lum+alpha")) return 2;
		if (type.equals("RGB")) return 3;
		if (type.equals("RGBA")) return 4;
		if (type.equals("colour map")) return 3;
		if (type.equals("cmap+alpha")) return 4;
		return 1;
	}
	
	protected int getTextureType(String type){
		return Volume3DTexture.TYPE_INTENSITY_CMAP_ALPHA;
		
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			if (e.getSource().equals(txtDataX)){
				txtDataX.setText(new MguiInteger(powerOfTwo(new MguiInteger(txtDataX.getText()).getInt())).toString("#0"));
				return;
				}
			if (e.getSource().equals(txtDataY)){
				txtDataY.setText(new MguiInteger(powerOfTwo(new MguiInteger(txtDataY.getText()).getInt())).toString("#0"));
				return;
				}
			if (e.getSource().equals(txtDataZ)){
				txtDataZ.setText(new MguiInteger(powerOfTwo(new MguiInteger(txtDataZ.getText()).getInt())).toString("#0"));
				return;
				}
			}
		
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void focusGained(FocusEvent e) {
		
		
	}

	public void focusLost(FocusEvent e) {
		
		updateSpecBoxes();
		
	}
	
	protected void setCurrentVolume(Volume3DInt volume){
		
		updateGridCombo = false;
		boolean has_changed = currentVolume != null && currentVolume != volume;
		if (has_changed)
			currentVolume.removeShapeListener(this);
		
		if (volume == null){
			cmbGrid.setSelectedItem(NEW_GRID);
			currentVolume = new Volume3DInt();
			volOptions = new VolumeInputOptions();
			cmbParentSet.setSelectedItem(InterfaceSession.getDisplayPanel().getCurrentShapeSet());
		}else{
			currentVolume = volume;
			if (cmbGrid.getSelectedItem() == null || !cmbGrid.getSelectedItem().equals(volume))
				cmbGrid.setSelectedItem(volume);
			cmbParentSet.setSelectedItem(currentVolume.getParentSet());
			}
		
		if (has_changed)
			currentVolume.addShapeListener(this);
		attrGrid3D.setAttributes(currentVolume.getAttributes());
		
		current_width_max = default_width_max;
		current_mid_max = default_width_max;
		current_mid_min = default_mid_min;
		sldIntWidth.setMaximum(current_width_max);
		sldIntMid.setMinimum(current_mid_min);
		sldIntMid.setMaximum(current_mid_max);
		
		updateColumns(true);
		updateValues();
		updateVolOptions();
		
		if (currentVolume != null){
			volOptions.setInputType(currentVolume.getFileLoader());
			URL url = currentVolume.getUrlReference();
			if (url != null){
				String file = url.getFile();
				if (file != null){
					volOptions.setFiles(new File[]{new File(file)});
					}
				}
			if (currentVolume.getColourMap() != null){
				volOptions.colour_map = currentVolume.getColourMap();
				volOptions.has_alpha = currentVolume.hasAlpha();
				}
			
			}
		
		updateButtons();
		updateMasking();
		enableOps(!isNewGrid());
		enableIntensity(!isNewGrid());
		
		updateIntensity();
		updateOperations();
		
		//reset the histogram with this volume's data limits
		resetHistogram();
		setHistogram(SwingUtilities.isEventDispatchThread());
		
		
		if (currentVolume.getFileLoader() != null){
			FileLoader loader = currentVolume.getFileLoader();
			volOptions.setInputType(loader);
		}else{
			volOptions.input_type = null;
			}
		
		if (currentVolume.getCurrentColumn() != null)
			setSelectedColumn(currentVolume.getCurrentColumn());
		
		updateGridCombo = true;
	}
	
	protected void updateColumns(){
		updateColumns(false);
	}
	
	protected void updateColumns(boolean force){
		updateAnything = false;
		
		if (vertex_data_table == null) return;
		if (vertex_data_model.volume != currentVolume || force)
			vertex_data_model.setVolume(currentVolume);
		
		cmbIsosurfColumn.removeAllItems();
		if (currentVolume != null){
			ArrayList<String> columns = currentVolume.getVertexDataColumnNames();
			for (int i = 0; i < columns.size(); i++)
				cmbIsosurfColumn.addItem(columns.get(i));
			}
		
		
		updateAnything = true;
	}
	
	public void showPanel(){	
		updateAnything = true;
		updateGridList();
		updateColumns();
		updateColourMaps();
		updateIntensity();
		updateHistogram();
		updateMasking();
	}
	
	public void updateColourMaps(){
		updateIntColourMap = false;
		cmbIntColourMap.removeAllItems();
		
		ArrayList<ColourMap> maps = InterfaceEnvironment.getColourMaps();
		
		for (int i = 0; i < maps.size(); i++)
			cmbIntColourMap.addItem(maps.get(i));
		
		if (cmbGrid.getSelectedItem().equals(NEW_GRID)){
			updateIntColourMap = true;
			return;
		}
		
		ColourMap map =  null;
		if (imgIntensity != null)
			map = imgIntensity.model.getColourMap();
		
		if (map != null)
			cmbIntColourMap.setSelectedItem(map);
		
		updateIntColourMap = true;
	}
	
	//TODO accommodate non-power-of-two input dimensions
	public void setDataDims(int x, int y, int z){
		txtDataX.setText(x + "");
		txtDataY.setText(y + "");
		txtDataZ.setText(z + "");
	}
	
	public void setImgOrigin(float x, float y, float z){
		txtOriginX.setText(MguiFloat.getString(x, "#0.000000"));
		txtOriginY.setText(MguiFloat.getString(y, "#0.000000"));
		txtOriginZ.setText(MguiFloat.getString(z, "#0.000000"));
	}
	
	public void setGeomDims(float x, float y, float z){
		txtGeomX.setText(MguiFloat.getString(x, "#0.000000"));
		txtGeomY.setText(MguiFloat.getString(y, "#0.000000"));
		txtGeomZ.setText(MguiFloat.getString(z, "#0.000000"));
	}
	
	public void setDataMax(double max, double min){
		//txtDataMax.setText(arDouble.getString(max, "#0.000000"));
		//txtDataMin.setText(arDouble.getString(min, "#0.000000"));
	}
	
	public void setAxes(Vector3f xAxis, Vector3f yAxis, Vector3f zAxis){
		txtAxisX_X.setText(MguiFloat.getString(xAxis.x, "#0.000000"));
		txtAxisX_Y.setText(MguiFloat.getString(xAxis.y, "#0.000000"));
		txtAxisX_Z.setText(MguiFloat.getString(xAxis.z, "#0.000000"));
		txtAxisY_X.setText(MguiFloat.getString(yAxis.x, "#0.000000"));
		txtAxisY_Y.setText(MguiFloat.getString(yAxis.y, "#0.000000"));
		txtAxisY_Z.setText(MguiFloat.getString(yAxis.z, "#0.000000"));
		txtAxisZ_X.setText(MguiFloat.getString(zAxis.x, "#0.000000"));
		txtAxisZ_Y.setText(MguiFloat.getString(zAxis.y, "#0.000000"));
		txtAxisZ_Z.setText(MguiFloat.getString(zAxis.z, "#0.000000"));
	}
	
	public void setGeometryFromRect(Rect3D r){
		if (r == null) return;
		Point3f origin = r.getOrigin();
		txtOriginX.setText(new MguiFloat(origin.x).toString("#0.00"));
		txtOriginY.setText(new MguiFloat(origin.y).toString("#0.00"));
		txtOriginZ.setText(new MguiFloat(origin.z).toString("#0.00"));
		txtGeomX.setText(new MguiFloat(r.getWidth()).toString("#0.00"));
		txtGeomY.setText(new MguiFloat(r.getHeight()).toString("#0.00"));
		txtGeomZ.setText("0.00");
		InterfaceSession.getDisplayPanel().setCurrentTool(lastTool);
	}
	
	public String toString(){
		return "Volume 3D Panel";
	}
	
	private int powerOfTwo(int value) {
		int retval = 2;
		while (retval < value)
		    retval *= 2;
		return retval;
	}
	
	static class NewVolumeDialog extends InterfaceOptionsDialogBox {
		
		JLabel lblColourMap = new JLabel("Colour Map:");
		JComboBox cmbColourMap = new JComboBox();
		JLabel lblName = new JLabel("Name:");
		JTextField txtName = new JTextField("No name");
		
		public boolean cancelled = false;
		
		public NewVolumeDialog(JFrame frame, VolumeInputOptions options){
			super(frame, options);
			_init();
		}
		
		void _init(){
			super.init();
			
			this.setDialogSize(400,200);
			this.setTitle("Create New Volume");
			
			LineLayout lineLayout = new LineLayout(20, 5, 0);
			this.setMainLayout(lineLayout);
			
			updateControls();
			updateDialog();
			
			LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.2, 1);
			mainPanel.add(lblName, c);
			c = new LineLayoutConstraints(1, 1, 0.25, 0.7, 1);
			mainPanel.add(txtName, c);
			c = new LineLayoutConstraints(2, 2, 0.05, 0.2, 1);
			mainPanel.add(lblColourMap, c);
			c = new LineLayoutConstraints(2, 2, 0.25, 0.7, 1);
			mainPanel.add(cmbColourMap, c);
			
			
			
		}
		
		private void updateControls(){
			
			cmbColourMap.removeAllItems();
			ArrayList<ColourMap> maps = InterfaceEnvironment.getColourMaps();
			for (int i = 0; i < maps.size(); i++)
				cmbColourMap.addItem(maps.get(i));
			
		}
		
		public boolean updateDialog(){
			
			VolumeInputOptions _options = (VolumeInputOptions)options;
			
			if (_options.colour_map != null)
				cmbColourMap.setSelectedItem(_options.colour_map);
			
			txtName.setText(_options.name);
			
			return true;
		}
		
		public static boolean showDialog(JFrame frame, VolumeInputOptions options){
			NewVolumeDialog dialog = new NewVolumeDialog(frame, options);
			dialog.setVisible(true);
			return !dialog.cancelled;
		}
		
		public void actionPerformed(ActionEvent e){
			
			if (e.getActionCommand().equals(DLG_CMD_OK)){
				
				VolumeInputOptions _options = (VolumeInputOptions)options;
				_options.colour_map = (ColourMap)cmbColourMap.getSelectedItem();
				_options.name = txtName.getText();
				
				this.setVisible(false);
				return;
				}
			
			if (e.getActionCommand().equals(DLG_CMD_CANCEL)){
				this.setVisible(false);
				cancelled = true;
				}
		}
		
		
	}
	
	/** JComponent used to display a Buffered Image. */
	  private class ImageViewComponent extends JComponent implements MouseMotionListener, 
	  															 	 MouseListener
	  {
		  
		public WindowedColourModel model;
		double last_mid, last_width, last_alpha;
		int z;
		Point click_pt;
		public Volume3DInt volume;
		public String column;
		//public GridVertexDataColumn column;
		
	    /** Buffered Image displayed in the JComponent. */
	    protected BufferedImage _bufferedImage;

	    public ImageViewComponent(){
	    	this(null, 0, null, null);
	    }
	    
	    /**
	     * Constructs an Image Component.
	     *
	     * @param bufferedImage Buffered Image displayed in the JComponent.
	     */
	    public ImageViewComponent(BufferedImage bufferedImage, int z, Volume3DInt volume, String column)
	      {
	    	_bufferedImage = bufferedImage;
	    	//this.volume = volume;
	    	this.volume = volume;
	    	this.column = column;
	    	
	    	this.z = z;
	    	this.addMouseListener(this);
	    	this.addMouseMotionListener(this);
	    	
	    	if (_bufferedImage == null) return;
	    	model = (WindowedColourModel)_bufferedImage.getColorModel();
	      }
	    
	    public JToolTip createToolTip(){
			return new JMultiLineToolTip();
		}
	    
	    public void setVolume(Volume3DInt volume, String column){
	    	WindowedColourModel model = null;
	    	if (column != null){
	    		model = volume.getColourModel(column);
	    		setVolume(volume, column, (WindowedColourModel)model.clone());
	    	}else{
	    		setVolume(volume, null, null);
	    		}
	    }
	    
	    public void setVolume(Volume3DInt volume, String column, WindowedColourModel colour_model){
	    	this.volume = volume;
	    	this.column = column;
	    	if (column == null){
	    		this.model = null;
	    		this._bufferedImage = null;
	    		return;
	    		}
	    	this.model = colour_model;
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
//	    public void paint(Graphics g){
//	    	
//	    }
	    
	    @Override
	    public void paintComponent(Graphics g) {
	    	super.paintComponent(g);
	    	Graphics2D g2d = (Graphics2D)g;
	    	g2d.setColor(Color.WHITE);
	    	g2d.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
	    	g2d.setColor(Color.BLUE);
	    	g2d.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
	    	if (_bufferedImage == null) return;
	    	_bufferedImage.flush();
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
			
			Point p = e.getPoint();
			
			double x_scale = (double)_bufferedImage.getWidth() / (double)this.getWidth();
			double y_scale = (double)_bufferedImage.getHeight() / (double)this.getHeight();
			
			Grid3D grid = volume.getGrid();
			int x_size = grid.getSizeS();
			int y_size = grid.getSizeT();
			
			int x = (int)Math.ceil(p.x * x_scale);
			if (x < 0) x = 0;
			if (x >= x_size) x = x_size - 1;
			int y = (int)Math.ceil((this.getHeight() - p.y) * y_scale);
			if (y < 0) y = 0;
			if (y >= y_size) y = y_size - 1;
			
			this.setToolTipText("[" + x + "," + y + "," + z + 
								"]\nVal: " + volume.getDatumAtVoxel(column, new int[]{x, y, z})); 
			
		}
	    
	    
	  }
	  
	  // Table model for display vertex data columns and their status
	  class VertexDataTableModel extends AbstractTableModel{
		  
			public ArrayList<String> columns;
		  	public Volume3DInt volume;
		  	public int is_current = -1;
		  	public ArrayList<Boolean> is_composite;
		  	public ArrayList<Double> alphas;
		  	public ArrayList<WindowedColourModel> colour_models;
		
		  	public VertexDataTableModel(){
		  		columns = new ArrayList<String>();
		  		is_composite = new ArrayList<Boolean>();
		  		alphas = new ArrayList<Double>();
		  	}
		  	
			public VertexDataTableModel(Volume3DInt volume){
				setVolume(volume);
			}
			  
			public void setVolume(Volume3DInt volume){
				this.volume = volume;
				columns = volume.getCompositeOrderedColumns();
				//Collections.sort(columns);
				String current = volume.getCurrentColumn();
				is_composite = new ArrayList<Boolean>(columns.size());
				alphas = new ArrayList<Double>(columns.size());
				colour_models = new ArrayList<WindowedColourModel>();
				is_current = -1;
				for (int i = 0; i < columns.size(); i++){
					String column = columns.get(i);
					if (column.equals(current)) 
						is_current = i;
					is_composite.add(volume.showInComposite(column));
					alphas.add(volume.getCompositeAlpha(column));
					colour_models.add((WindowedColourModel)volume.getColourModel(column).clone());
					}
				
				fireTableDataChanged();
			}
			
			public int getIndexOf(String column){
				for (int i = 0; i < columns.size(); i++)
					if (columns.get(i).equals(column)) 
						return i;
				return -1;
			}
			
			public WindowedColourModel getColourModel(String column){
				int i = getIndexOf(column);
				if (i < 0)
					return null;
				return colour_models.get(i);
			}
			
			public boolean swapIndex(int idx, int new_idx){
				
				if (idx < 0 || idx >= columns.size()) return false;
				if (new_idx < 0 || new_idx >= columns.size()) return false;
				
				String current = null;
				if (is_current > -1)
					current = columns.get(is_current);
				
				String column = columns.remove(idx);
				columns.add(new_idx, column);
				boolean show = is_composite.remove(idx);
				is_composite.add(new_idx, show);
				double alpha = alphas.remove(idx);
				alphas.add(new_idx, alpha);
				WindowedColourModel model = colour_models.remove(idx);
				colour_models.add(new_idx, model);
				
				if (current != null)
					is_current = this.getIndexOf(current);
				
				fireTableDataChanged();
				
				return true;
			}
			
			public double getAlpha(String column){
				int i = getIndexOf(column);
				if (i < 0)
					return Double.NaN;
				return alphas.get(i);
			}
			
			public boolean isInComposite(String column){
				int i = getIndexOf(column);
				if (i < 0)
					return false;
				return is_composite.get(i);
			}
			
			public String getCurrentColumn(){
				if (is_current < 0) return null;
				return columns.get(is_current);
			}
			
			@Override
			public int getRowCount() {
				return columns.size();
			}
			
			@Override
			public int getColumnCount() {
				return 4;
			}
			
			@Override
			public Object getValueAt(int row, int column) {
				switch (column){
					case 0:
						return row == is_current;
					case 1:
						return is_composite.get(row);
					case 2:
						return columns.get(row);
					case 3:
						double val = alphas.get(row);
						if (Double.isNaN(val)) return 0;
						return (int)(val * 1000.0);
					default:
						return null;	
					}
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return column != 2;
			}
			
			@Override
			public String getColumnName(int column) {
				switch (column){
					case 0: return "Current";
					case 1: return "Show";
					case 2: return "Column";
					case 3: return "Opacity";
					default:
						return "?";
					}
			}
			
			@Override
			public Class<?> getColumnClass(int column) {
				switch (column){
					case 0: return Boolean.class;
					case 1: return Boolean.class;
					case 2: return String.class;
					case 3: return Integer.class;
					default:
						return String.class;
					}
			}
			
			@Override
			public void setValueAt(Object value, int row, int column) {
				switch (column){
					case 0:
						is_current = row;
						break;
					case 1:
						is_composite.set(row, (Boolean)value);
						break;
					case 3:
						int val = (Integer)value;
						alphas.set(row, (double)val / 1000.0);
						break;
					default:
						return;
					}
				if (column == 0){
					fireTableDataChanged();
					updateIntensity();
					resetHistogram();
					setHistogram(true);
				}else{
					fireTableCellUpdated(row, column);
					updateIntensity();
					}
				
			}
		  
			public int findVertexColumn(String name){
				for (int i = 0; i < columns.size(); i++)
					if (columns.get(i).equals(name)) return i;
				return -1;
			}
			
			
			
			
	  }
	
	  public class SliderTableColumn extends AbstractCellEditor implements TableCellRenderer,
	  																	   TableCellEditor,
	  																	   ChangeListener{
			

			private final JSlider slRenderer;
			private final JSlider slEditor;
			
			private final int INITAL_VALUE;
			private final int MIN_VLAUE;
			private final int MAX_VALUE;
			
			public SliderTableColumn(int min, int max, int initial){
				INITAL_VALUE = initial;
				MIN_VLAUE    = min;
				MAX_VALUE    = max;
			
				slRenderer = new JSlider(MIN_VLAUE, MAX_VALUE);
				slEditor   = new JSlider(MIN_VLAUE, MAX_VALUE);
				
				slEditor.addChangeListener(this);
								
				slRenderer.setValue(INITAL_VALUE);
				slEditor.setValue(INITAL_VALUE);
			}
			
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if (!source.getValueIsAdjusting()){
					this.fireEditingStopped();
					//InterfaceSession.log("Editor stopped.", LoggingType.Debug);
					}
			}
			
			@Override
			public Object getCellEditorValue(){
				return slEditor.getValue();
			}
			
			@Override
			public Component getTableCellRendererComponent(JTable table,
															Object value,
															boolean isSelected,
															boolean hasFocus,
															int row,
															int column){
				if(value != null){
					slRenderer.setValue((Integer) value);
					}
				slRenderer.updateUI();
				return slRenderer;
			}
			
			@Override
			public Component getTableCellEditorComponent(JTable table,
														Object value,
														boolean isSelected,
														int row,
														int column){
				//System.out.println("Editor started.");
			if(value != null){
				slEditor.setValue((Integer) value);
				}
			return slEditor;
			}
			
		}
	  

	
}
