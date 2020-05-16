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

package mgui.interfaces.shapes;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.datasources.DataTable;
import mgui.datasources.LinkedDataStream;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBoxRenderer;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourBar;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.math.MathExpression;
import mgui.interfaces.math.MathExpressionDialogBox;
import mgui.interfaces.math.MathExpressionOptions;
import mgui.interfaces.shapes.datasources.DataSourceLinkDialogBox;
import mgui.interfaces.shapes.datasources.DataSourceLinkOptions;
import mgui.interfaces.shapes.mesh.VertexSelection;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeEngine;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.stats.HistogramPlot;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;
import mgui.stats.Histogram;
import mgui.stats.StatFunctions;

import org.cheffo.jeplite.JEP;

/********************************************************
 * Interface panel providing a GUI for general shape-related functions.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceShapesPanel extends InterfacePanel implements ActionListener,
																	ChangeListener{

	CategoryTitle lblShapes = new CategoryTitle("SHAPES");
	JCheckBox chkCurrentAll = new JCheckBox("All shapes in model");
	//JCheckBox chkCurrentShape = new JCheckBox("Selected shape:");
	JLabel lblCurrentShape = new JLabel("Selected shape:");
	InterfaceComboBox cmbCurrentShape = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JCheckBox chkCurrentSet = new JCheckBox("Selection set:");
	JComboBox cmbCurrentSet = new JComboBox();
	
	CategoryTitle lblTransform = new CategoryTitle("TRANSFORMATION");
	JLabel lblTransformMethod = new JLabel("Transform method");
	JComboBox<String> cmbTransformMethod = new JComboBox<String>();
	JLabel lblTransformParameters = new JLabel("Parameters");
	InterfaceAttributePanel pnlTransform = new InterfaceAttributePanel();
	JButton cmdTransformApply = new JButton("Apply");
	
	CategoryTitle lblConvexHull = new CategoryTitle("CONVEX HULL");
	//JLabel lblConvexHullName = new JLabel("Surface name:");
	//JTextField txtConvexHullName = new JTextField("no-name");
	//JLabel lblConvexHullAlgorithm = new JLabel("Algorithm:");
	//JComboBox cmbConvexHullAlgorithm =new JComboBox();
	JLabel lblConvexHullMethod = new JLabel("Algorithm:");
	JComboBox<String> cmbConvexHullMethod = new JComboBox<String>();
	JLabel lblConvexHullParameters = new JLabel("Parameters");
	InterfaceAttributePanel pnlConvexHull = new InterfaceAttributePanel();
	JButton cmdConvexHullApply = new JButton("Apply");
	
	CategoryTitle lblNameMaps = new CategoryTitle("NAME MAPS");
	JLabel lblNameMapColumn = new JLabel("Column:");
	InterfaceComboBox cmbNameMapColumn = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblNameMap = new JLabel("Name map:");
	InterfaceComboBox cmbNameMap = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	
	CategoryTitle lblVertexSelection = new CategoryTitle("VERTEX SELECTION");
	JLabel lblVertexSelect = new JLabel("Select vertices where:");
	JScrollPane scrVertexSelection;
	JTable vertex_selection_table;
	VertexSelectionTableModel vertex_selection_model;
	JLabel lblVertexSelectionOp = new JLabel("Current selection:");
	JComboBox cmbVertexSelectionOp = new JComboBox();
	JLabel lblVertexSelectionConcat = new JLabel("Concatenator:");
	JComboBox cmbVertexSelectionConcat = new JComboBox();
	JButton cmdVertexSelectionAdd = new JButton("Add");
	JButton cmdVertexSelectionRemove = new JButton("Remove");
	JButton cmdVertexSelectionClear = new JButton("Clear");
	JButton cmdVertexSelectionApply = new JButton("Apply");
	
	CategoryTitle lblDataOperations = new CategoryTitle("DATA OPERATIONS");
	JCheckBox chkDataOperationsSelected = new JCheckBox(" Only selected nodes");
	JLabel lblDataOperationsTarget = new JLabel("Target column:");
	JButton cmdDataOperationsCreate = new JButton("Create..");
	InterfaceComboBox cmbDataOperationsTarget = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JButton cmdDataOperationsDefine = new JButton("Define..");
	JButton cmdDataOperationsApply = new JButton("Apply");
	
	CategoryTitle lblDataTransfer = new CategoryTitle("DATA TRANSFER");
	JCheckBox chkDataTransferSelected = new JCheckBox(" Only selected nodes");
	JLabel lblDataTransferType = new JLabel("Transfer type:");
	JComboBox cmbDataTransferType = new JComboBox();
	JLabel lblDataTransferInput = new JLabel("Input column:");
	JComboBox cmbDataTransferInput = new JComboBox();
	JLabel lblDataTransferShape = new JLabel("Target shape:");
	InterfaceComboBox cmbDataTransferShape = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblDataTransferData = new JLabel("Target column:");
	JButton cmdDataTransferCreate = new JButton("Create..");
	InterfaceComboBox cmbDataTransferTarget = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblDataTransferOperation = new JLabel("Operation:");
	JComboBox cmbDataTransferOperation = new JComboBox();
	JButton cmdDataTransferOptions = new JButton("Options");
	JButton cmdDataTransferApply = new JButton("Apply");
	
	CategoryTitle lblDataSources = new CategoryTitle("DATA SOURCES");
	JLabel lblDataSourcesLinkColumn = new JLabel("Link column:");
	InterfaceComboBox cmbDataSourcesLinkColumn = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	//JComboBox cmbDataSourcesLinkColumn = new JComboBox();
	JLabel lblDataSourcesLinks = new JLabel("Data Source Links:");
	JTable tblDataSourcesLinks;
	JScrollPane scrDataSourcesLinks;
	DataLinkTableModel data_sources_model;
	JButton cmdDataSourcesAddLink = new JButton("Add");
	JButton cmdDataSourcesRemoveLink = new JButton("Remove");
	JButton cmdDataSourcesEditLink = new JButton("Edit");
	
	CategoryTitle lblDataDisplay = new CategoryTitle("DATA DISPLAY");
	JLabel lblDataDisplayColumn = new JLabel("Column:");
	InterfaceComboBox cmbDataDisplayColumn = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblDataDisplayColourMap = new JLabel("Colour Map:");
	InterfaceComboBox cmbDataDisplayColourMap = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			   true, 500);
	JLabel lblDataMin = new JLabel("Min:");
	JTextField txtDataMin = new JTextField("0");
	JLabel lblDataMax = new JLabel("Max:");
	JTextField txtDataMax = new JTextField("1");
	
	JButton cmdApplyDataDisplay = new JButton("Apply");
	JButton cmdResetDataDisplay = new JButton("Reset");
	
	CategoryTitle lblHistogram = new CategoryTitle("HISTOGRAM");
	
	JCheckBox chkHistUseCurrent = new JCheckBox(" Use current data");
	JLabel lblHistColumn = new JLabel("Column:");
	//JComboBox cmbHistColumn = new JComboBox();
	InterfaceComboBox cmbHistColumn = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	HistogramPlot pnlHistPanel = new HistogramPlot();
	JLabel lblHistBins = new JLabel("Bins:");
	JTextField txtHistBins = new JTextField("80");
	JLabel lblHistMin = new JLabel("Min:");
	JTextField txtHistMin = new JTextField("0");
	JLabel lblHistMax = new JLabel("Max:");
	JTextField txtHistMax = new JTextField("1");
	JLabel lblHistYScale = new JLabel("Y scale:");
	JTextField txtHistYScale = new JTextField("0.5");
	JSlider sldHistYScale = new JSlider(SwingConstants.HORIZONTAL, 1, 10000, 1);
	JButton cmdHistReset = new JButton("Reset");
	JButton cmdHistCalc = new JButton("Apply");
	
	CategoryTitle lblVolume = new CategoryTitle("VOLUME");
	JLabel lblVolumeSource = new JLabel("Source Volume");
	//JComboBox cmbVolumeSource = new JComboBox();
	InterfaceComboBox cmbVolumeSource = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblVolumeName = new JLabel("Data column name:");
	JTextField txtVolumeName = new JTextField("no-name");
	JLabel lblVolumeMethod = new JLabel("Operation");
	JComboBox<?> cmbVolumeMethod = new JComboBox<Object>();
	JLabel lblVolumeParameters = new JLabel("Parameters");
	InterfaceAttributePanel volumeAttr;
	JButton cmdVolume = new JButton("Apply");
	
	CategoryTitle lblNormalize = new CategoryTitle("NORMALIZE");
	JLabel lblNormalizeMethod = new JLabel("Normalize method");
	JComboBox<String> cmbNormalizeMethod = new JComboBox<String>();
	JButton cmdNormalizeApply = new JButton("Apply");
	JLabel lblNormalizeParameters = new JLabel("Parameters");
	InterfaceAttributePanel pnlNormalize = new InterfaceAttributePanel();
	JButton cmdNormalize = new JButton("Apply");
	
	/*********** MISC STUFF **********************/
	
	ContinuousColourBar barScaledColourBar = new ContinuousColourBar();
	protected ShapeEngine shape_engine = new ShapeEngine();	
	boolean doUpdate = true;
	MathExpressionOptions math_expression_options, operation_expression_options;
	MathExpressionDialogBox mesh_expression_dialog;
	
	InterfaceShape current_shape, data_transfer_shape;
	ShapeSelectionSet current_selection;
	String current_column;
	protected DataSource currentDataSource;
	protected DataTable currentDataTable;
	protected DataField currentLinkField;
	
	public static final String CMB_NONE = "<---NONE--->";
	
	public InterfaceShapesPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	@Override
	protected void init() {
		_init();
		doUpdate = false;
		
		initComboBoxes();
		
		//set up panel categories and components
		setLayout(new CategoryLayout(20, 5, 200, 10));
		chkCurrentAll.setSelected(true);
		
		barScaledColourBar.showAnchors = false;
		barScaledColourBar.showDivisions = true;
		barScaledColourBar.noDivisions = 4;
		barScaledColourBar.divSize = 0.3;
		
		/********* INIT LISTS ******************/
		cmbDataTransferType.addItem("Vertex-wise");
		cmbDataTransferType.addItem("Nearest Neighbour");
		cmbDataTransferType.addItem("Interpolate Nearest Neighbour");
		cmbDataTransferType.setSelectedItem("Vertex-wise");
		
		cmbDataTransferOperation.addItem("Threshold");
		cmbDataTransferOperation.addItem("Mask with plane");
		cmbDataTransferOperation.addItem("Mask with data");
		cmbDataTransferOperation.addItem("Expression");
		
		/********* SET UP LISTENERS ************/
		
		pnlHistPanel.addChangeListener(this);
		
//		chkCurrentAll.addActionListener(this);
//		chkCurrentAll.setActionCommand("Shapes Changed All");
//		chkCurrentShape.addActionListener(this);
//		chkCurrentShape.setActionCommand("Shapes Changed Shape");
		cmbCurrentShape.addActionListener(this);
		cmbCurrentShape.setActionCommand("Shapes Current Shape");
//		chkCurrentSet.addActionListener(this);
//		chkCurrentSet.setActionCommand("Shapes Changed Set");
		
		cmdDataOperationsCreate.setActionCommand("Data Operations Create");
		cmdDataOperationsCreate.addActionListener(this);
		cmdDataOperationsDefine.setActionCommand("Data Operations Define");
		cmdDataOperationsDefine.addActionListener(this);
		cmdDataOperationsApply.setActionCommand("Data Operations Apply");
		cmdDataOperationsApply.addActionListener(this);
		
		cmdVertexSelectionAdd.setActionCommand("Vertex Selection Add");
		cmdVertexSelectionAdd.addActionListener(this);
		cmdVertexSelectionRemove.setActionCommand("Vertex Selection Remove");
		cmdVertexSelectionRemove.addActionListener(this);
		cmdVertexSelectionClear.setActionCommand("Vertex Selection Clear");
		cmdVertexSelectionClear.addActionListener(this);
		cmdVertexSelectionApply.setActionCommand("Vertex Selection Apply");
		cmdVertexSelectionApply.addActionListener(this);
		
		cmbDataTransferShape.setActionCommand("Data Transfer Shape");
		cmbDataTransferShape.addActionListener(this);
		cmdDataTransferCreate.setActionCommand("Data Transfer Create");
		cmdDataTransferCreate.addActionListener(this);
		cmdDataTransferOptions.setActionCommand("Data Transfer Options");
		cmdDataTransferOptions.addActionListener(this);
		cmdDataTransferApply.setActionCommand("Data Transfer Apply");
		cmdDataTransferApply.addActionListener(this);
		cmdHistCalc.setActionCommand("Histogram Apply");
		cmdHistCalc.addActionListener(this);
		cmdHistReset.setActionCommand("Histogram Reset");
		txtHistYScale.addActionListener(this);
		txtHistYScale.setActionCommand("Histogram Y Scale Changed");
		sldHistYScale.addChangeListener(this);
		
		cmdHistReset.addActionListener(this);
		cmbDataSourcesLinkColumn.setActionCommand("DataSources Change Column");
		cmbDataSourcesLinkColumn.addActionListener(this);
		cmdDataSourcesRemoveLink.setActionCommand("DataSources Remove");
		cmdDataSourcesRemoveLink.addActionListener(this);
		cmdDataSourcesAddLink.setActionCommand("DataSources Add");
		cmdDataSourcesAddLink.addActionListener(this);
		cmdDataSourcesEditLink.setActionCommand("DataSources Edit");
		cmdDataSourcesEditLink.addActionListener(this);
				
		cmdApplyDataDisplay.setActionCommand("Data Display Apply");
		cmdApplyDataDisplay.addActionListener(this);
		cmdResetDataDisplay.setActionCommand("Data Display Reset");
		cmdResetDataDisplay.addActionListener(this);
		cmbDataDisplayColumn.setActionCommand("Data Display Column Changed");
		cmbDataDisplayColumn.addActionListener(this);
		cmbDataDisplayColourMap.setActionCommand("Data Display ColourMap Changed");
		cmbDataDisplayColourMap.addActionListener(this);
		cmbTransformMethod.setActionCommand("Transform Shape Method");
		cmbTransformMethod.addActionListener(this);
		cmdTransformApply.setActionCommand("Transform Shape Apply");
		cmdTransformApply.addActionListener(this);
		cmdConvexHullApply.setActionCommand("Convex Hull Apply");
		cmdConvexHullApply.addActionListener(this);
		cmbConvexHullMethod.setActionCommand("Convex Hull Method");
		cmbConvexHullMethod.addActionListener(this);
		cmdNormalizeApply.setActionCommand("Normalize Apply");
		cmdNormalizeApply.addActionListener(this);
		cmbNormalizeMethod.setActionCommand("Normalize Method");
		cmbNormalizeMethod.addActionListener(this);
		
		cmbNameMap.setActionCommand("NameMap Map Changed");
		cmbNameMap.addActionListener(this);
		cmbNameMapColumn.setActionCommand("NameMap Column Changed");
		cmbNameMapColumn.addActionListener(this);
				
		chkHistUseCurrent.setActionCommand("Histogram Use Current");
		chkHistUseCurrent.addActionListener(this);
		cmbHistColumn.setActionCommand("Histogram Column Changed");
		cmbHistColumn.addActionListener(this);
		
		/************** SET UP PARAMETERS *********************/
		
		initOperations();
//		cmbTransformMethod.addItem("Translate");
//		cmbTransformMethod.addItem("Rotate YPR");		// Rotation defined with yaw, pitch, and roll
//		cmbTransformMethod.addItem("Rotate Axis"); 		// Rotation defined about an axis vector
//		cmbTransformMethod.addItem("Matrix Transform");
//		cmbConvexHullAlgorithm.addItem("QuickHull");
//		cmbConvexHullAlgorithm.addItem("GiftWrap");
//		cmbConvexHullAlgorithm.addItem("Divide & Conquer");
//		cmbConvexHullAlgorithm.addItem("Incremental");
//		cmbConvexHullAlgorithm.setSelectedItem("GiftWrap");
		
		cmbVertexSelectionOp.addItem("Overwrite");
		cmbVertexSelectionOp.addItem("Union");
		cmbVertexSelectionOp.addItem("Intersect");
		cmbVertexSelectionConcat.addItem("&&");
		cmbVertexSelectionConcat.addItem("||");
		cmbVertexSelectionConcat.setSelectedItem("&&");
		
		chkHistUseCurrent.setSelected(true);
		
		pnlTransform = new InterfaceAttributePanel(shape_engine.getAttributes("Translate"));
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblShapes, c);
		lblShapes.setParentObj(this);
		c = new CategoryLayoutConstraints("SHAPES", 1, 1, 0.05, 0.9, 1);
		add(lblCurrentShape, c);
		c = new CategoryLayoutConstraints("SHAPES", 2, 2, 0.1, 0.85, 1);
		add(cmbCurrentShape, c);
		
		c = new CategoryLayoutConstraints();
		lblTransform.isExpanded = false;
		add(lblTransform, c);
		lblTransform.setParentObj(this);
		c = new CategoryLayoutConstraints("TRANSFORMATION", 1, 1, 0.05, 0.9, 1);
		add(lblTransformMethod, c);
		c = new CategoryLayoutConstraints("TRANSFORMATION", 2, 2, 0.1, 0.85, 1);
		add(cmbTransformMethod, c);
		c = new CategoryLayoutConstraints("TRANSFORMATION", 3, 3, 0.05, 0.9, 1);
		add(lblTransformParameters, c);
		c = new CategoryLayoutConstraints("TRANSFORMATION", 4, 8, 0.05, 0.9, 1);
		add(pnlTransform, c);
		c = new CategoryLayoutConstraints("TRANSFORMATION", 9, 9, 0.1, 0.8, 1);
		add(cmdTransformApply, c);
		
		c = new CategoryLayoutConstraints();
		lblConvexHull.isExpanded = false;
		add(lblConvexHull, c);
		lblConvexHull.setParentObj(this);
		c = new CategoryLayoutConstraints("CONVEX HULL", 1, 1, 0.05, 0.9, 1);
		add(lblConvexHullMethod, c);
		c = new CategoryLayoutConstraints("CONVEX HULL", 2, 2, 0.1, 0.85, 1);
		add(cmbConvexHullMethod, c);
		c = new CategoryLayoutConstraints("CONVEX HULL", 3, 3, 0.05, 0.9, 1);
		add(lblConvexHullParameters, c);
		c = new CategoryLayoutConstraints("CONVEX HULL", 4, 8, 0.05, 0.9, 1);
		add(pnlConvexHull, c);
		c = new CategoryLayoutConstraints("CONVEX HULL", 9, 9, 0.1, 0.8, 1);
		add(cmdConvexHullApply, c);
		
		c = new CategoryLayoutConstraints();
		lblNormalize.isExpanded = false;
		add(lblNormalize, c);
		lblNormalize.setParentObj(this);
		c = new CategoryLayoutConstraints("NORMALIZE", 1, 1, 0.05, 0.9, 1);
		add(lblNormalizeMethod, c);
		c = new CategoryLayoutConstraints("NORMALIZE", 2, 2, 0.1, 0.85, 1);
		add(cmbNormalizeMethod, c);
		c = new CategoryLayoutConstraints("NORMALIZE", 3, 3, 0.05, 0.9, 1);
		add(lblNormalizeParameters, c);
		c = new CategoryLayoutConstraints("NORMALIZE", 4, 8, 0.05, 0.9, 1);
		add(pnlNormalize, c);
		c = new CategoryLayoutConstraints("NORMALIZE", 9, 9, 0.1, 0.8, 1);
		add(cmdNormalizeApply, c);
		
//		c = new CategoryLayoutConstraints();
//		lblConvexHull.isExpanded = false;
//		add(lblConvexHull, c);
//		lblConvexHull.setParentObj(this);
//		c = new CategoryLayoutConstraints("CONVEX HULL", 1, 1, 0.05, 0.9, 1);
//		add(lblConvexHullName, c);
//		c = new CategoryLayoutConstraints("CONVEX HULL", 2, 2, 0.1, 0.85, 1);
//		add(txtConvexHullName, c);
//		c = new CategoryLayoutConstraints("CONVEX HULL", 3, 3, 0.05, 0.9, 1);
//		add(lblConvexHullAlgorithm, c);
//		c = new CategoryLayoutConstraints("CONVEX HULL", 4, 4, 0.1, 0.85, 1);
//		add(cmbConvexHullAlgorithm, c);
//		c = new CategoryLayoutConstraints("CONVEX HULL", 5, 5, 0.1, 0.8, 1);
//		add(cmdConvexHull, c);
		
		c = new CategoryLayoutConstraints();
		lblNameMaps.isExpanded = false;
		lblNameMaps.setParentObj(this);
		add(lblNameMaps, c);
		c = new CategoryLayoutConstraints("NAME MAPS", 1, 1, 0.05, 0.9, 1);
		add(lblNameMapColumn, c);
		c = new CategoryLayoutConstraints("NAME MAPS", 2, 2, 0.1, 0.85, 1);
		add(cmbNameMapColumn, c);
		c = new CategoryLayoutConstraints("NAME MAPS", 3, 3, 0.05, 0.9, 1);
		add(lblNameMap, c);
		c = new CategoryLayoutConstraints("NAME MAPS", 4, 4, 0.1, 0.85, 1);
		add(cmbNameMap, c);
		
		vertex_selection_model = new VertexSelectionTableModel();
		vertex_selection_table = new JTable(vertex_selection_model);
		TableColumn column = vertex_selection_table.getColumnModel().getColumn(1);
		column.setCellEditor(new DefaultCellEditor(VertexSelectionTableModel.getComparisonCombo()));
				
		scrVertexSelection = new JScrollPane(vertex_selection_table);
		
		c = new CategoryLayoutConstraints();
		lblVertexSelection.isExpanded = false;
		add(lblVertexSelection, c);
		lblVertexSelection.setParentObj(this);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 1, 1, 0.05, 0.9, 1);
		add(lblVertexSelect, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 2, 6, 0.05, 0.9, 1);
		add(scrVertexSelection, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 7, 7, 0.05, 0.3, 1);
		add(lblVertexSelectionOp, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 7, 7, 0.35, 0.6, 1);
		add(cmbVertexSelectionOp, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 8, 8, 0.05, 0.3, 1);
		add(lblVertexSelectionConcat, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 8, 8, 0.35, 0.6, 1);
		add(cmbVertexSelectionConcat, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 9, 9, 0.05, 0.43, 1);
		add(cmdVertexSelectionAdd, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 9, 9, 0.52, 0.43, 1);
		add(cmdVertexSelectionRemove, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 10, 10, 0.05, 0.43, 1);
		add(cmdVertexSelectionClear, c);
		c = new CategoryLayoutConstraints("VERTEX SELECTION", 10, 10, 0.52, 0.43, 1);
		add(cmdVertexSelectionApply, c);
		
		c = new CategoryLayoutConstraints();
		lblDataOperations.isExpanded = false;
		add(lblDataOperations, c);
		lblDataOperations.setParentObj(this);
		c = new CategoryLayoutConstraints("DATA OPERATIONS", 1, 1, 0.05, 0.9, 1);
		add(chkDataOperationsSelected, c);
		c = new CategoryLayoutConstraints("DATA OPERATIONS", 2, 2, 0.05, 0.3, 1);
		add(lblDataOperationsTarget, c);
		c = new CategoryLayoutConstraints("DATA OPERATIONS", 2, 2, 0.35, 0.6, 1);
		add(cmbDataOperationsTarget, c);
		c = new CategoryLayoutConstraints("DATA OPERATIONS", 3, 3, 0.5, 0.45, 1);
		add(cmdDataOperationsCreate, c);
		c = new CategoryLayoutConstraints("DATA OPERATIONS", 5, 5, 0.1, 0.8, 1);
		add(cmdDataOperationsDefine, c);
		c = new CategoryLayoutConstraints("DATA OPERATIONS", 6, 6, 0.1, 0.8, 1);
		add(cmdDataOperationsApply, c);
		
		c = new CategoryLayoutConstraints();
		lblDataTransfer.isExpanded = false;
		add(lblDataTransfer, c);
		lblDataTransfer.setParentObj(this);
		
		c = new CategoryLayoutConstraints("DATA TRANSFER", 1, 1, 0.05, 0.9, 1);
		add(chkDataTransferSelected, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 2, 2, 0.05, 0.9, 1);
		add(lblDataTransferType, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 3, 3, 0.1, 0.85, 1);
		add(cmbDataTransferType, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 4, 4, 0.05, 0.9, 1);
		add(lblDataTransferInput, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 5, 5, 0.1, 0.85, 1);
		add(cmbDataTransferInput, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 6, 6, 0.05, 0.9, 1);
		add(lblDataTransferShape, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 7, 7, 0.1, 0.85, 1);
		add(cmbDataTransferShape, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 8, 8, 0.05, 0.4, 1);
		add(lblDataTransferData, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 8, 8, 0.5, 0.45, 1);
		add(cmdDataTransferCreate, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 9, 9, 0.05, 0.9, 1);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 9, 9, 0.1, 0.85, 1);
		add(cmbDataTransferTarget, c);
		
		c = new CategoryLayoutConstraints("DATA TRANSFER", 10, 10, 0.05, 0.3, 1);
		add(lblDataTransferOperation, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 10, 10, 0.4, 0.55, 1);
		add(cmbDataTransferOperation, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 11, 11, 0.2, 0.6, 1);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 11, 11, 0.1, 0.8, 1);
		add(cmdDataTransferOptions, c);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 12, 12, 0.2, 0.6, 1);
		c = new CategoryLayoutConstraints("DATA TRANSFER", 12, 12, 0.1, 0.8, 1);
		add(cmdDataTransferApply, c);
		
		c = new CategoryLayoutConstraints();
		lblDataSources.isExpanded = false;
		add(lblDataSources, c);
		lblDataSources.setParentObj(this);
		
		data_sources_model = new DataLinkTableModel();
		tblDataSourcesLinks = new JTable(data_sources_model);
		data_sources_model.setTable(tblDataSourcesLinks);
		scrDataSourcesLinks = new JScrollPane(tblDataSourcesLinks);
		
		c = new CategoryLayoutConstraints("DATA SOURCES", 1, 1, 0.05, 0.3, 1);
		add(lblDataSourcesLinkColumn, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 1, 1, 0.4, 0.55, 1);
		add(cmbDataSourcesLinkColumn, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 2, 2, 0.05, 0.9, 1);
		add(lblDataSourcesLinks, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 3, 8, 0.05, 0.9, 1);
		add(scrDataSourcesLinks, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 9, 9, 0.1, 0.8, 1);
		add(cmdDataSourcesAddLink, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 10, 10, 0.1, 0.8, 1);
		add(cmdDataSourcesEditLink, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 11, 11, 0.1, 0.8, 1);
		add(cmdDataSourcesRemoveLink, c);
		
		c = new CategoryLayoutConstraints();
		lblDataDisplay.isExpanded = false;
		add(lblDataDisplay, c);
		lblDataDisplay.setParentObj(this);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 1, 1, 0.05, 1, 1);
		add(lblDataDisplayColumn, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 2, 2, 0.1, 0.8, 1);
		add(cmbDataDisplayColumn, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 3, 3, 0.05, 1, 1);
		add(lblDataDisplayColourMap, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 4, 4, 0.1, 0.8, 1);
		add(cmbDataDisplayColourMap, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 5, 5, 0.05, 0.15, 1);
		add(lblDataMin, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 5, 5, 0.21, 0.28, 1);
		add(txtDataMin, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 5, 5, 0.51, 0.15, 1);
		add(lblDataMax, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 5, 5, 0.67, 0.28, 1);
		add(txtDataMax, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 6, 6, 0.1, .8, 1);
		add(cmdResetDataDisplay, c);
		c = new CategoryLayoutConstraints("DATA DISPLAY", 7, 7, 0.1, .8, 1);
		add(cmdApplyDataDisplay, c);
		
		c = new CategoryLayoutConstraints();
		add(lblHistogram, c);
		lblHistogram.setParentObj(this);
		
		c = new CategoryLayoutConstraints("HISTOGRAM", 1, 1, 0.05, 0.9, 1);
		add(chkHistUseCurrent, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 2, 2, 0.05, 0.3, 1);
		add(lblHistColumn, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 2, 2, 0.4, 0.55, 1);
		add(cmbHistColumn, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 3, 9, 0.05, 0.9, 1);
		add(pnlHistPanel, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 10, 10, 0.05, 0.3, 1);
		add(lblHistBins, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 10, 10, 0.35, 0.6, 1);
		add(txtHistBins, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 11, 11, 0.05, 0.3, 1);
		add(lblHistMin, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 11, 11, 0.35, 0.6, 1);
		add(txtHistMin, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 12, 12, 0.05, 0.3, 1);
		add(lblHistMax, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 12, 12, 0.35, 0.6, 1);
		add(txtHistMax, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 13, 13, 0.05, 0.3, 1);
		add(lblHistYScale, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 13, 13, 0.35, 0.6, 1);
		add(txtHistYScale, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 14, 14, 0.35, 0.6, 1);
		add(sldHistYScale, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 15, 15, 0.05, 0.43, 1);
		add(cmdHistReset, c);
		c = new CategoryLayoutConstraints("HISTOGRAM", 15, 15, 0.52, 0.43, 1);
		add(cmdHistCalc, c);
		
		
		
		doUpdate = true;
		updateControls();
		
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceShapesPanel.class.getResource("/mgui/resources/icons/shape_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/shape_3d_20.png");
		return null;
	}
	
	protected void initOperations(){
		
		doUpdate = false;
		
		// Transforms
		ArrayList<String> methods = shape_engine.getMethods("Transform Shape");
		cmbTransformMethod.removeAllItems();
		for (int i = 0; i < methods.size(); i++){
			cmbTransformMethod.addItem(methods.get(i));
			}
		cmbTransformMethod.setSelectedIndex(0);
		
		// Convex Hull
		methods = shape_engine.getMethods("Convex Hull");
		cmbConvexHullMethod.removeAllItems();
		for (int i = 0; i < methods.size(); i++){
			cmbConvexHullMethod.addItem(methods.get(i));
			}
		cmbConvexHullMethod.setSelectedIndex(0);
		
		// Normalize
		methods = shape_engine.getMethods("Normalize");
		cmbNormalizeMethod.removeAllItems();
		for (int i = 0; i < methods.size(); i++){
			cmbNormalizeMethod.addItem(methods.get(i));
			}
		cmbNormalizeMethod.setSelectedIndex(0);
		
		// Smoothing
		
		
		doUpdate = true;
		
		this.updateParameters();
	}
	
	private void initComboBoxes(){
		//set special renderer for these boxes
		
		//add an icon to vertex data column combos
		InterfaceComboBoxRenderer column_renderer = new InterfaceComboBoxRenderer(){
			
			public Component getListCellRendererComponent(JList list, 
														  Object value,
														  int index, 
														  boolean isSelected, 
														  boolean cellHasFocus) {
				
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (current_shape == null || value == null) return this;
				if (value.toString().contains(".{"))
					setIcon(LinkedDataStream.getIcon());
				else
					setIcon(VertexDataColumn.getIcon());
				return this;
				
			}
		};
		
		cmbDataTransferInput.setRenderer(column_renderer);
		cmbNameMapColumn.setRenderer(column_renderer);
		cmbDataSourcesLinkColumn.setRenderer(column_renderer);
		cmbDataDisplayColumn.setRenderer(column_renderer);
		cmbHistColumn.setRenderer(column_renderer);
		
	}
	
	
	
	public void cleanUpPanel(){
		//clear combo lists to avoid memory leaks (references to mesh objects which may be deleted)
		doUpdate = false;
		cmbCurrentShape.removeAllItems();
		doUpdate = true;
	}
	
	public void showPanel(){
		if (current_shape != null && current_shape.isDestroyed())
			current_shape = null;
		updateDisplay();
	}
	
	public void updateDisplay(){
		//System.out.println("Updating:");
		updateShapeLists();
		//System.out.print("Shapes, ");
		updateParameters();
		//System.out.print("Parameters, ");
		updateVolumes();
		//System.out.print("Volumes, ");
		updateControls();
		//System.out.print("Controls, ");
		updateColourBar();
		//System.out.print("Colour Bar, ");
		updateVertexSelection();
		//System.out.print("Vertex Selection, ");
		//updateDataDisplay();
		updateDataSources();
		//System.out.print("Data sources, ");
		updateUI();
		//System.out.println("UI.");
	}
	
	protected void updateParameters(){
		if (!doUpdate) return;
		String method = (String)cmbConvexHullMethod.getSelectedItem();
		if (method != null)
			updateParameters("Convex Hull", method);
		method = (String)cmbTransformMethod.getSelectedItem();
		if (method != null)
			updateParameters("Transform Shape", method);
		method = (String)cmbNormalizeMethod.getSelectedItem();
		if (method != null)
			updateParameters("Normalize", method);
	}
	
	protected void updateParameters(String operation, String method){
		if (!doUpdate) return;
		doUpdate = false;
		if (operation.equals("Transform Shape")){
			pnlTransform.setAttributes(shape_engine.getAttributes(operation, method));
		}else if (operation.equals("Convex Hull")){
			pnlConvexHull.setAttributes(shape_engine.getAttributes(operation, method));
		}else if (operation.equals("Normalize")){
			pnlNormalize.setAttributes(shape_engine.getAttributes(operation, method));
			}
		doUpdate = true;
		updateEngineLists(operation);
	}
	
	protected void updateHistogram(){
		setHistogram(true);
		if (pnlHistPanel.histogram != null && pnlHistPanel.colour_model != null)
			pnlHistPanel.repaint();
	}
	
	protected void setHistogram(boolean as_job){
		if (current_shape == null){
			pnlHistPanel.setHistogram(null);
			pnlHistPanel.repaint();
			return;
			}
		
		
		//run this as worker thread so as not to freeze UI
		SwingWorker<Boolean,String> worker = new SwingWorker<Boolean,String>(){

			@Override
			protected Boolean doInBackground() throws Exception {
				setHistogramBlocking();
				return true;
			}
			
			@Override
		    public void done() {
				pnlHistPanel.setBusy(false);
			}
			
		};
		
		pnlHistPanel.setBusy(true);
		worker.execute();
		
	}
	
	protected void setHistogramBlocking(){
		if (current_shape == null || cmbHistColumn.getSelectedItem() == null){
			pnlHistPanel.setHistogram(null);
			return;
			}
		
		String column = (String)cmbHistColumn.getSelectedItem();
		VertexDataColumn v_column = current_shape.getVertexDataColumn(column);
		ArrayList<MguiNumber> data = current_shape.getVertexData(column);
		double hist_min = 0, hist_max = 0;
		try{
			hist_min = Double.valueOf(txtHistMin.getText());
			hist_max = Double.valueOf(txtHistMax.getText());
		}catch (NumberFormatException ex){
			InterfaceSession.log("InterfaceShapesPanel: Bad min/max format for histogram..", 
								 LoggingType.Errors);
			return;
			}
		Histogram h = StatFunctions.getHistogram(data, 
											   	 Integer.valueOf(txtHistBins.getText()).intValue(),
											   	 hist_min,
											   	 hist_max,
											   	 false);

		//h.ignore_large_bin = Float.MAX_VALUE;
		pnlHistPanel.setHistogram(h);
		pnlHistPanel.y_scale = Float.valueOf(txtHistYScale.getText());
		ColourMap c_map = current_shape.getColourMap(column);
		
		if (c_map != null){
			
			double min = v_column.getColourMin();
			double max = v_column.getColourMax();
			
			if (max > min){
				
				double intercept = min;
				double scale = 1.0 / (max - min);
				double width = 1.0;
				double mid = 0.5;
				
				WindowedColourModel c_model = new WindowedColourModel(c_map,
																	  scale,
																	  intercept,
																	  mid,
																	  width,
																	  false, 
																	  DataBuffer.TYPE_DOUBLE);
				c_model.setLimits(min, max);
				pnlHistPanel.colour_model = c_model;							
				}
			}else{
				pnlHistPanel.colour_model = null;
				}
		
		pnlHistPanel.updateUI();
	}
	
	public void stateChanged(ChangeEvent e) {
		
		if (e.getSource().equals(this.pnlHistPanel)){
			double[] limits = pnlHistPanel.colour_model.getLimits();
			this.txtDataMin.setText(MguiDouble.getString(limits[0], 5));
			this.txtDataMax.setText(MguiDouble.getString(limits[1], 5));
			return;
			}
		
		if (e.getSource().equals(sldHistYScale)){
			float val = sldHistYScale.getValue() / 10000f;
			txtHistYScale.setText(MguiDouble.getString(val, "0.00#####"));
			pnlHistPanel.setYScale(val);
			return;
			}
	}
	
	protected void updateVertexSelection(){
		
		if (this.current_shape == null){
			vertex_selection_model.clear();
			return;
			}
			
		TableColumn column = vertex_selection_table.getColumnModel().getColumn(0);
		column.setCellEditor(new DefaultCellEditor(VertexSelectionTableModel.getVariableCombo(current_shape)));
		
		
	}
	
	protected void updateCurrentShape(){
		doUpdate = false;
		if (current_shape != null)
			cmbCurrentShape.setSelectedItem(current_shape);
		else{
			cmbCurrentShape.setSelectedIndex(0);
			current_shape = (InterfaceShape)cmbCurrentShape.getSelectedItem();
			}
		
		data_transfer_shape = null;
		doUpdate = true;
		
		updateShapeLists();
		updateDataSources();
		updateNameMaps();
		
		if (current_shape != null){
			String column = current_shape.getCurrentColumn();
			doUpdate = false;
			cmbDataDisplayColumn.setSelectedItem(column);
			doUpdate = true;
			}
		
		updateDataDisplay();
		updateMaxMinHistogram();
		updateHistogram();
		updateVertexSelection();
	}
	
	protected void updateShapeLists(){
		doUpdate = false;
		cmbCurrentShape.removeAllItems();
		cmbDataTransferShape.removeAllItems();
		
		//TODO: also add 2D shapes
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		ArrayList<Shape3DInt> shapes = new ArrayList<Shape3DInt>();
		
		for (int i = 0; i < models.size(); i++){
			ShapeSet3DInt shapes3D = models.get(i).getModelSet();
			shapes = shapes3D.get3DShapes(true);
			cmbCurrentShape.addItem(shapes3D);
			
			for (int j = 0; j < shapes.size(); j++){
				cmbCurrentShape.addItem(shapes.get(j));
				}
			}
		
		if (cmbCurrentShape.getItemCount() == 0){
			current_shape = null;
			data_transfer_shape = null;
			return;
			}
		
		if (current_shape != null)
			cmbCurrentShape.setSelectedItem(current_shape);
		else{
			cmbCurrentShape.setSelectedIndex(0);
			current_shape = (InterfaceShape)cmbCurrentShape.getSelectedItem();
			}
		
		ShapeSet3DInt shapes3D = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		if (current_shape == null) current_shape = shapes3D;
		
		//Data Ops list; lists only compatible variable objects
		
		if (current_shape != shapes3D && current_shape.supportsVariableType(shapes3D.getVariableType()))
			cmbDataTransferShape.addItem(shapes3D);
		
		for (int i = 0; i < shapes.size(); i++){
			if (current_shape != shapes.get(i) && current_shape.supportsVariableType(shapes.get(i).getVariableType()))
				cmbDataTransferShape.addItem(shapes.get(i));
			}
		
		if (data_transfer_shape != null)
			cmbDataTransferShape.setSelectedItem(data_transfer_shape);
		else{
			if (cmbDataTransferShape.getItemCount() > 0){
				cmbDataTransferShape.setSelectedIndex(0);
				data_transfer_shape = (InterfaceShape)cmbDataTransferShape.getSelectedItem();
			}else{
				data_transfer_shape = null;
				}
			}
		
		//selection sets
		cmbCurrentSet.removeAllItems();
		ArrayList<ShapeSelectionSet> list = InterfaceSession.getWorkspace().getSelectionSets();
		for (int i = 0 ; i < list.size(); i++)
			cmbCurrentSet.addItem(list.get(i));
		
		updateDataTransferShape();
		//System.out.print("[Transfer shape, ");
		updateDataColumns();
		//System.out.print("Data columns, ");
		updateDataDisplay();
		//System.out.print("Data display, ");
		
		updateNameMaps();
		//System.out.print("Name maps], ");
		updateEngineLists();
		doUpdate = true;
	}
	
	protected void updateEngineLists(){
		updateEngineLists(null);
	}
	
	protected void updateEngineLists(String operation){
		
		if (!doUpdate || current_shape == null) return;
		doUpdate = false;
		
		// Transforms
		
		// Convex Hull
			// Shape sets
		if (operation == null || operation.equals("Convex Hull")){
			ShapeSet3DInt model_set = current_shape.getModel().getModelSet();
			List<Shape3DInt> sets  = model_set.getShapeType(model_set, true);
			
			ArrayList<String> set_names = new ArrayList<String>();
			set_names.add(model_set.getFullName());
			for (Shape3DInt set : sets) {
				set_names.add(set.getFullName());
				}
			String method = (String)cmbConvexHullMethod.getSelectedItem();
			AttributeList list = shape_engine.getAttributes("Convex Hull", method);
			AttributeSelection<String> attr_sel = (AttributeSelection<String>)list.getAttribute("target_shape_set");
			String selected = attr_sel.getValue();
			attr_sel.setList(set_names);
			if (selected != null)
				attr_sel.select(selected);
			else if (set_names.size() > 0)
				attr_sel.select(set_names.get(0));
			}
		
		// Normalization
			// Columns
		if (operation == null || operation.equals("Normalize")){
			ArrayList<String> col_names = current_shape.getVertexDataColumnNames();
			String method = (String)cmbNormalizeMethod.getSelectedItem();
			AttributeList list = shape_engine.getAttributes("Normalize", method);
			AttributeSelection<String> attr_sel = (AttributeSelection<String>)list.getAttribute("source_column");
			String selected = attr_sel.getValue();
			attr_sel.setList(col_names);
			if (selected != null)
				attr_sel.select(selected);
			else if (col_names.size() > 0)
				attr_sel.select(col_names.get(0));
			attr_sel = (AttributeSelection<String>)list.getAttribute("target_column");
			selected = attr_sel.getValue();
			attr_sel.setList(col_names);
			if (selected != null)
				attr_sel.select(selected);
			else if (col_names.size() > 0)
				attr_sel.select(col_names.get(0));
			if (method.equals("Mask")){
				attr_sel = (AttributeSelection<String>)list.getAttribute("normalize_column");
				selected = attr_sel.getValue();
				attr_sel.setList(col_names);
				if (selected != null)
					attr_sel.select(selected);
				else if (col_names.size() > 0)
					attr_sel.select(col_names.get(0));
				attr_sel = (AttributeSelection<String>)list.getAttribute("mask_column");
				selected = attr_sel.getValue();
				attr_sel.setList(col_names);
				if (selected != null)
					attr_sel.select(selected);
				else if (col_names.size() > 0)
					attr_sel.select(col_names.get(0));
				}
			}
			
		// Smoothing
		
		doUpdate = true;
	}
	
	protected void updateDataDisplay(){
		if (!doUpdate) return;
		doUpdate = false;
		
		cmbDataDisplayColourMap.removeAllItems();
		ArrayList<ColourMap> maps = InterfaceEnvironment.getColourMaps();
		for (int i = 0; i < maps.size(); i++)
			cmbDataDisplayColourMap.addItem(maps.get(i));
		
		String column = (String)cmbDataDisplayColumn.getSelectedItem();
		
		if (current_shape == null){
			doUpdate = true;
			return;
			}
				
		if (column == null){
			doUpdate = true;
			return;
			}
		
		cmbDataDisplayColumn.setSelectedItem(column);
		
		doUpdate = true;
		
		updateDataDisplayColumn();
		
	}
	
	protected void updateDataDisplayColumn(){
		if (!doUpdate) return;
		doUpdate = false;
		
		if (current_shape == null){
			doUpdate = true;
			updateColourBar();
			return;
			}
		
		String column = (String)cmbDataDisplayColumn.getSelectedItem();
		if (column != null){
			ColourMap cmap = current_shape.getColourMap(column);
			cmbDataDisplayColourMap.setSelectedItem(cmap);
			VertexDataColumn v_column = current_shape.getVertexDataColumn(column);
			if (v_column != null){
				txtDataMin.setText("" + v_column.getColourMin());
				txtDataMax.setText("" + v_column.getColourMax());
				}
			}
		
		doUpdate = true;
		updateColourBar();
		updateHistogram();
		
	}
	
	protected void updateDataColumns(){
		//fill mesh columns
		String currentDataOpCol = null, currentSubmeshCol = null, currentNameMapCol = null, currentLinkColumn = null, currentDataDisplayCol = null;
		String currentHistCol = null, currentDataOpTarget = null;
		boolean dataOpColFound = false, submeshColFound = false, namemapColFound = false, linkcolumnFound = false, dataDisplayFound = false;;
		boolean histColFound = false, dataOpTargetFound = false;;
		
		if (cmbDataTransferInput.getSelectedItem() != null)
			currentDataOpCol = (String)cmbDataTransferInput.getSelectedItem();
	
		if (cmbNameMapColumn.getSelectedItem() != null)
			currentNameMapCol = (String)cmbNameMapColumn.getSelectedItem();
		
		if (cmbDataSourcesLinkColumn.getSelectedItem() != null)
			currentLinkColumn = (String)cmbDataSourcesLinkColumn.getSelectedItem();
		
		if (cmbDataDisplayColumn.getSelectedItem() != null)
			currentDataDisplayCol = (String)cmbDataDisplayColumn.getSelectedItem();
		
		if (cmbHistColumn.getSelectedItem() != null)
			currentHistCol = (String)cmbHistColumn.getSelectedItem();
		
		if (cmbDataOperationsTarget.getSelectedItem() != null)
			currentDataOpTarget = (String)cmbDataOperationsTarget.getSelectedItem();
		
		doUpdate = false;
		
		cmbDataTransferInput.removeAllItems();
		cmbNameMapColumn.removeAllItems();
		cmbDataSourcesLinkColumn.removeAllItems();
		cmbDataDisplayColumn.removeAllItems();
		cmbHistColumn.removeAllItems();
		cmbDataOperationsTarget.removeAllItems();
		
		if (current_shape == null || ! current_shape.hasData()){
			doUpdate = true;
			return;
			}
		
		ArrayList<String> list = current_shape.getVertexDataColumnNames();
		if (list == null){
			doUpdate = true;
			return;
			}
		
		for (int i = 0; i < list.size(); i++){
			cmbDataTransferInput.addItem(list.get(i));
			cmbDataOperationsTarget.addItem(list.get(i));
			if (!list.get(i).contains(".{")){
				cmbNameMapColumn.addItem(list.get(i));
				cmbDataSourcesLinkColumn.addItem(list.get(i));
				}
			cmbDataDisplayColumn.addItem(list.get(i));
			cmbHistColumn.addItem(list.get(i));
			if (currentDataOpCol != null && list.get(i).equals(currentDataOpCol))
				dataOpColFound = true;	
			if (currentSubmeshCol != null && list.get(i).equals(currentSubmeshCol))
				submeshColFound = true;	
			if (currentNameMapCol != null && list.get(i).equals(currentNameMapCol))
				namemapColFound = true;	
			if (currentLinkColumn != null && list.get(i).equals(currentLinkColumn))
				linkcolumnFound = true;	
			if (currentDataDisplayCol != null && list.get(i).equals(currentDataDisplayCol))
				dataDisplayFound = true;
			if (currentHistCol != null && list.get(i).equals(currentHistCol))
				histColFound = true;
			if (currentDataOpTarget != null && list.get(i).equals(currentDataOpTarget))
				dataOpTargetFound = true;
			}
		
		if (dataOpColFound) cmbDataTransferInput.setSelectedItem(currentDataOpCol);
		if (namemapColFound) cmbNameMapColumn.setSelectedItem(currentNameMapCol);
		if (linkcolumnFound) cmbDataSourcesLinkColumn.setSelectedItem(currentLinkColumn);
		if (dataDisplayFound) cmbDataDisplayColumn.setSelectedItem(currentDataDisplayCol);
		if (histColFound) cmbHistColumn.setSelectedItem(currentHistCol);
		if (dataOpTargetFound) cmbDataOperationsTarget.setSelectedItem(currentDataOpTarget);
		
		doUpdate = true;
		
	}
	
	protected void updateDataSources(){
		if (!doUpdate) return;
		doUpdate = false;
		
		//if (data_sources_model == null) return;
		
		String name = (String)cmbDataSourcesLinkColumn.getSelectedItem();
		if (name == null || current_shape == null){
			data_sources_model.clear();
			doUpdate = true;
			return;
			}
		
		VertexDataColumn column = current_shape.getVertexDataColumn(name);
		if (column == null){
			doUpdate = true;
			return;
			}
		
		data_sources_model.setFromColumn(column);
		doUpdate = true;
	}
	
	protected void updateNameMaps(){
		if (!doUpdate) return;
		doUpdate = false;
		String current_col = (String)cmbNameMapColumn.getSelectedItem();
		
		cmbNameMap.removeAllItems();
		cmbNameMap.addItem(CMB_NONE);
		
		if (current_col == null || current_shape == null){
			doUpdate = true;
			return;
			}
		
		ArrayList<NameMap> maps = InterfaceEnvironment.getNameMaps();
		for (NameMap map : maps) cmbNameMap.addItem(map);
		NameMap map = current_shape.getNameMap(current_col);
		
		if (map == null){
			cmbNameMap.setSelectedItem(CMB_NONE);
			doUpdate = true;
			return;
			}
		
		cmbNameMap.setSelectedItem(map);
		doUpdate = true;
	}
	
	protected void updateColourBar(){
		if (!doUpdate) return;
		doUpdate = false;
		
		if (cmbDataDisplayColourMap.getSelectedItem() == null ||
				!cmbDataDisplayColourMap.isEnabled()){
			remove(barScaledColourBar);
			repaint();
			doUpdate = true;
			return;
			}
		
		ColourMap map = (ColourMap)cmbDataDisplayColourMap.getSelectedItem();
		map = (ColourMap)map.clone();
		
		if (!(map instanceof ContinuousColourMap)){
			remove(barScaledColourBar);
			repaint();
			doUpdate = true;
			return;
			}
		
		barScaledColourBar.min = Double.valueOf(txtDataMin.getText()).doubleValue();
		barScaledColourBar.max = Double.valueOf(txtDataMax.getText()).doubleValue();
		
		if (barScaledColourBar.getParent() == null){
			CategoryLayoutConstraints c = new CategoryLayoutConstraints("DATA DISPLAY", 8, 9, 0.05, .9, 1);
			add(barScaledColourBar, c);
			}
		
		barScaledColourBar.setMap((ContinuousColourMap)map);
		
		//updateUI();
		//this.getParent().repaint();
		repaint();
		doUpdate = true;
		
	}
	
	protected void updateVolumes(){
		
		cmbVolumeSource.removeAllItems();
		List<Shape3DInt> volumes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Volume3DInt());
		
		for (Shape3DInt volume : volumes) {
			cmbVolumeSource.addItem(volume);
			}
		
	}
	
	protected void updateControls(){
		doUpdate = false;
		if (chkHistUseCurrent.isSelected()){
			cmbHistColumn.setEnabled(false);
		}else{
			cmbHistColumn.setEnabled(true);
			}
				
		doUpdate = true;
	}

	protected void updateDataTransferShape(){
		if (cmbDataTransferShape.getItemCount() == 0) return;
		
		if (data_transfer_shape != null)
			cmbDataTransferShape.setSelectedItem(data_transfer_shape);
		else{
			cmbDataTransferShape.setSelectedIndex(0);
			data_transfer_shape = (Shape3DInt)cmbDataTransferShape.getSelectedItem();
			}
		
		updateDataTransferColumns();
	}
	
	protected void updateDataTransferColumns(){
		//fill mesh columns
		String currentCol = null;
		boolean colFound = false;
		
		if (cmbDataTransferTarget.getSelectedItem() != null)
			currentCol = (String)cmbDataTransferTarget.getSelectedItem();
		
		cmbDataTransferTarget.removeAllItems();
		if (data_transfer_shape == null || ! data_transfer_shape.hasData()) return;
		
		doUpdate = false;
		
		ArrayList<String> list = data_transfer_shape.getNonLinkedDataColumns();
		if (list == null){
			doUpdate = true;
			return;
			}
		
		for (int i = 0; i < list.size(); i++){
			cmbDataTransferTarget.addItem(list.get(i));
			if (currentCol != null && list.get(i).equals(currentCol))
				colFound = true;
			}
		
		if (colFound) cmbDataTransferTarget.setSelectedItem(currentCol);
		
		doUpdate = true;
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
	
		//********** SHAPE SELECTION *********************************
		
		if (e.getActionCommand().startsWith("Shape")){
			
			if (e.getActionCommand().endsWith("Current Shape")){
				if (!doUpdate) return;
				current_shape = (InterfaceShape)cmbCurrentShape.getSelectedItem();
				vertex_selection_model.clear();
				updateCurrentShape();
				
				return;
				}
			
		}
		
		
		//*********** DATA SOURCES ***********************************
		
		if (e.getActionCommand().startsWith("DataSources")){
			if (current_shape == null) return;
			
			if (e.getActionCommand().endsWith("Change Column")){
				String column = (String)cmbDataSourcesLinkColumn.getSelectedItem();
				if (column == null) return;
				
				updateDataSources();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Add")){
				String column = (String)cmbDataSourcesLinkColumn.getSelectedItem();
				if (column == null) return;
				
				DataSourceLinkOptions options = new DataSourceLinkOptions(current_shape.getVertexDataColumn(column));
				DataSourceLinkDialogBox.showDialog(options);
				
				updateDataSources();
				updateCurrentShape();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Edit")){
				String column = (String)cmbDataSourcesLinkColumn.getSelectedItem();
				if (column == null) return;
				
				int selected = tblDataSourcesLinks.getSelectedRow();
				if (selected < 0) return;
				
				//LinkedDataStream stream = data_sources_model.getDataStream(selected);
				DataSourceLinkOptions options = new DataSourceLinkOptions(current_shape.getVertexDataColumn(column),
																		  data_sources_model.getName(selected));
				
				DataSourceLinkDialogBox.showDialog(options);
				
				updateDataSources();
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				String column = (String)cmbDataSourcesLinkColumn.getSelectedItem();
				if (column == null) return;
				
				int[] selected = tblDataSourcesLinks.getSelectedRows();
				if (selected.length == 0) return;
				
				if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
						"Really remove " + selected.length + " data link(s)?", 
						"Remove Data Link(s)", 
						JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) 
					return;
				
				VertexDataColumn v_column = current_shape.getVertexDataColumn(column);
				ArrayList<String> names = new ArrayList<String>();
				for (int i = 0; i < selected.length; i++){
					names.add(data_sources_model.getName(selected[i]));
					}
				
				for (int i = 0; i < names.size(); i++){
					v_column.removeDataLink(names.get(i));
					}
				
				updateDataSources();
				updateCurrentShape();
				return;
				}
				
			}
		
		//*************** NAME MAPS *************************************************
		
		if (e.getActionCommand().startsWith("NameMap")){
			if (!doUpdate || cmbNameMap.getSelectedItem() == null) return;
			
			if (e.getActionCommand().endsWith("Column Changed")){
				updateNameMaps();
				return;
				}
			
			if (e.getActionCommand().endsWith("Map Changed")){
				if (current_shape == null) return;
				String column = (String)cmbNameMapColumn.getSelectedItem();
				//Mesh3DInt selected_mesh = (Mesh3DInt)cmbCurrentMesh.getSelectedItem();
				if (cmbNameMap.getSelectedItem() == CMB_NONE){
					current_shape.removeNameMap(column);
				}else{
					InterfaceSession.log("Setting name map...");
					current_shape.setNameMap(column, (NameMap)cmbNameMap.getSelectedItem());
					
					}
				
				return;
				}
			
			}
		
		//***************** VERTEX SELECTION *****************************************
		
		if (e.getActionCommand().startsWith("Vertex Selection")){
			
			if (e.getActionCommand().endsWith("Add")){
				
				this.vertex_selection_model.addRow();
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				
				int row = vertex_selection_table.getSelectedRow();
				if (row < 0) return;
				this.vertex_selection_model.removeRow(row);
				return;
				}
			
			if (e.getActionCommand().endsWith("Clear")){
				
				vertex_selection_model.clear();
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				
				if (current_shape == null || vertex_selection_model.getRowCount() == 0) return;
				
				// Construct statement
				String statement = "";
				
				for (int i = 0; i < vertex_selection_model.variables.size(); i++){
					
					if (i > 0)
						statement = statement + " " + (String)cmbVertexSelectionConcat.getSelectedItem() + " ";
					statement = statement + vertex_selection_model.variables.get(i) + " " +
											vertex_selection_model.comparison.get(i) + " " +
											vertex_selection_model.values.get(i);
					
					}
				
				MathExpressionOptions options = new MathExpressionOptions(current_shape);
				options.jep = new JEP();
				ArrayList<String> vars = current_shape.getVariables();
				for (int i = 0; i < vars.size(); i++)
					options.jep.addVariable(vars.get(i), 0);
				
				options.jep.addStandardConstants();
				options.jep.addStandardFunctions();
				options.jep.parseExpression(statement);
				
				if (options.jep.hasError()){
					//error encountered, show user
					InterfaceSession.log("Errors parsing expression:\n" + options.jep.getErrorInfo());
					JOptionPane.showMessageDialog(null, "Error(s) encountered. See console.");
					return;
					}
				
				boolean[] result = (boolean[])MathExpression.evaluate_conditional(options);
				VertexSelection selection = new VertexSelection(result);
				VertexSelection current = current_shape.getVertexSelection();
				
				if (current != null){
					String op = (String)cmbVertexSelectionOp.getSelectedItem();
					if (op.equals("Intersect")){
						selection = selection.getIntersection(current);
					}else if (op.equals("Union")){
						selection = selection.getUnion(current);
						}
					}
				
				current_shape.setVertexSelection(selection);
				return;
				}
		
			return;
			}
		
		//***************** TRANSFORM SHAPE ******************************************
		
		if (e.getActionCommand().startsWith("Transform Shape")){
			
			if (e.getActionCommand().endsWith("Method")){
				String method = (String)cmbTransformMethod.getSelectedItem();
				updateParameters("Transform Shape", method);
				updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				InterfaceShape shape = getCurrentShapes();
				if (shape == null) return;
				
				if (shape instanceof Shape3DInt){
					InterfaceProgressBar progress_bar = new InterfaceProgressBar("Transforming shape '" + shape.getName() + "': ");
					
					if (!shape_engine.transformShape3D((Shape3DInt)shape, 
												  	   (String)cmbTransformMethod.getSelectedItem(),
												  	   progress_bar)){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Error transforming shape '" + shape.getName() + "'", 
													  "Transform Shape", 
													  JOptionPane.ERROR_MESSAGE);
						return;
						}
					
					shape.updateShape();
					((Shape3DInt)shape).setScene3DObject();
					shape.fireShapeModified();
					
				}else{
					//TODO: implement 2D transformation
					
					
					}
					
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Success transforming shape(s)", 
											  "Transform Shape", 
											  JOptionPane.INFORMATION_MESSAGE);
				return;
				}
			
			}
		
		
		//***************** CONVEX HULL **********************************************
		
		if (e.getActionCommand().startsWith("Convex Hull")){
			
			if (e.getActionCommand().endsWith("Method")){
				String method = (String)cmbConvexHullMethod.getSelectedItem();
				updateParameters("Convex Hull", method);
				updateUI();
				return;
			}
			
			if (e.getActionCommand().endsWith("Apply")){
				InterfaceShape shape = getCurrentShapes();
				if (shape == null) return;
				
				//3D hull
				if (shape instanceof Shape3DInt){
					Shape3DInt shape3D = (Shape3DInt)current_shape;
					InterfaceProgressBar bar = new InterfaceProgressBar("Computing convex hull: ");
					bar.register();
					
					String method = (String)cmbConvexHullMethod.getSelectedItem();
					ArrayList<Shape3DInt> params = new ArrayList<Shape3DInt>();
					params.add(shape3D);
					if (!shape_engine.callMethod("Compute Convex Hull", method, params, bar)){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Convex hull failed or was cancelled..", 
													  "Compute Convex Hull", 
													  JOptionPane.ERROR_MESSAGE);
					}else{
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Convex hull created", 
													  "Compute Convex Hull", 
													  JOptionPane.INFORMATION_MESSAGE);
						}
					
					bar.deregister();
					return;
				}else{
					
					//TODO: implement 2D hull
					
					
					}
				}
			return;
			}
	
		//***************** NORMALIZE **********************************************
		
		if (e.getActionCommand().startsWith("Normalize")){
			
			if (e.getActionCommand().endsWith("Method")){
				String method = (String)cmbNormalizeMethod.getSelectedItem();
				updateParameters("Normalize", method);
				updateUI();
				return;
			}
			
			if (e.getActionCommand().endsWith("Apply")){
				InterfaceShape shape = (InterfaceShape)current_shape;
				InterfaceProgressBar bar = new InterfaceProgressBar("Normalizing shape data: ");
				bar.register();
				
				String method = (String)cmbNormalizeMethod.getSelectedItem();
				ArrayList<InterfaceShape> params = new ArrayList<InterfaceShape>();
				params.add(shape);
				if (!shape_engine.callMethod("Normalize", method, params, bar)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Normalization failed or was cancelled..", 
												  "Normalize Shape Data", 
												  JOptionPane.ERROR_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Normalization finished", 
												  "Normalize Shape Data", 
												  JOptionPane.INFORMATION_MESSAGE);
					// Update panel to reflect changes
					updateDisplay();
					}
				bar.deregister();
				}
			
			return;
			}
	
		
		//********************** DATA OPERATIONS ********************************
		
		if (e.getActionCommand().startsWith("Data Operations")){
			
			if (this.current_shape == null) return;
			
			if (e.getActionCommand().endsWith("Define")){
				if (operation_expression_options == null || operation_expression_options.variable != current_shape)
					operation_expression_options = new MathExpressionOptions(current_shape);
				if (mesh_expression_dialog == null)
					mesh_expression_dialog = new MathExpressionDialogBox(InterfaceSession.getSessionFrame(), 
																		 operation_expression_options);
				else
					mesh_expression_dialog.updateDialog(operation_expression_options);
				
				mesh_expression_dialog.setVisible(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				if (operation_expression_options == null || operation_expression_options.jep == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No operation defined!", 
												  "Apply Data Operation",
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				String target = (String)cmbDataOperationsTarget.getSelectedItem();
				
				if (target == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No target column specified!", 
												  "Apply Data Operation",
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				VertexSelection selected = null;
				if (chkDataOperationsSelected.isSelected())
					selected = current_shape.getVertexSelection();
				
				if (current_shape.setVariableValues(target,
										  	 	  	MathExpression.evaluate(operation_expression_options),
										  	 	  	selected)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Success.", 
												  "Apply Data Operation",
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Failed to apply operation. See log.", 
												  "Apply Data Operation",
												  JOptionPane.ERROR_MESSAGE);
					}
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Create")){
				String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
														  "Name of data column:",
														  "Create data for '" + current_shape.getName() + "'", 
														  JOptionPane.PLAIN_MESSAGE);
				if (name != null && ! current_shape.hasColumn(name)){
					current_shape.addVertexData(name);
					updateDataDisplay();
					updateDataColumns();
					updateDataTransferColumns();
					updateDisplay();
					updateUI();
					return;
					}
				
				return;
				}
			
			
			return;
			}
		
		
		//*********************** DATA TRANSFER *********************************
		
		if (e.getActionCommand().startsWith("Data Transfer")){
			
			if (e.getActionCommand().endsWith("Shape") && doUpdate){
				data_transfer_shape = (Shape3DInt)cmbDataTransferShape.getSelectedItem();
				updateDataTransferShape();
				updateDataTransferColumns();
				updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Create")){
				if (data_transfer_shape == null) return;
				
				String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
														  "Name of data column:",
														  "Create data for " + data_transfer_shape.toString(), 
														  JOptionPane.PLAIN_MESSAGE);
				if (name != null){
					data_transfer_shape.addVertexData(name);
					updateDataDisplay();
					updateDataColumns();
					updateDataTransferColumns();
					updateDisplay();
					updateUI();
					return;
					}
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Options")){
				if (cmbDataTransferOperation.getSelectedItem().equals("Expression")){
					if (math_expression_options == null || math_expression_options.variable != current_shape)
						math_expression_options = new MathExpressionOptions(current_shape);
					if (mesh_expression_dialog == null)
						mesh_expression_dialog = new MathExpressionDialogBox(InterfaceSession.getSessionFrame(),
																			 math_expression_options);
					else
						mesh_expression_dialog.updateDialog(math_expression_options);
					
					mesh_expression_dialog.setVisible(true);
					return;
					}
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				if (current_shape == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No source shape set!", 
												  "Shape data transfer",
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				if (data_transfer_shape == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No target shape set!", 
												  "Shape data transfer",
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				VertexSelection selected = null;
				String type = (String)cmbDataTransferType.getSelectedItem();
				String target = (String)cmbDataTransferTarget.getSelectedItem();
				
				if (chkDataTransferSelected.isSelected())
					selected = current_shape.getVertexSelection();
				
				if (cmbDataTransferOperation.getSelectedItem().equals("Expression")){
					if (math_expression_options == null || math_expression_options.jep == null){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "No expression defined!", 
													  "Apply Expression to Shape",
													  JOptionPane.ERROR_MESSAGE);
						return;
						}
					
					if (type.equals("Vertex-wise")){
						data_transfer_shape.setVariableValues(target,
													  	 	  MathExpression.evaluate(math_expression_options),
													  	 	  selected);
						
					}else if (type.equals("Nearest Neighbour")){
						//Select nearest neighbours
						
						ArrayList<Integer> neighbours = ShapeFunctions.getNearestNeighbours(current_shape, data_transfer_shape);
						double[] values = (double[])MathExpression.evaluate(math_expression_options);
						
						double[] transfers = new double[neighbours.size()];
						for (int i = 0; i < transfers.length; i++)
							transfers[i] = values[neighbours.get(i)];
						
						data_transfer_shape.setVariableValues(target,
															  transfers,
													  	 	  selected);
						
					}else if (type.equals("Interpolate Nearest Neighbour")){
						
						}
					
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Expression applied to shape '" +
												  data_transfer_shape.getName() + "', column '" + (String)cmbDataTransferTarget.getSelectedItem() +"'.", 
												  "Shape data transfer: Success!",
												  JOptionPane.INFORMATION_MESSAGE);
					return;
					}
				
				
				}
			
			return;
			}
		
		
		
		
		//****************************** DATA DISPLAY ************************************
		
		if (e.getActionCommand().startsWith("Data Display")){
			
			if (e.getActionCommand().endsWith("Column Changed")){
				if (!doUpdate) return;
				String column = (String)cmbDataDisplayColumn.getSelectedItem();
				current_column = column;
				if (chkHistUseCurrent.isSelected()){
					cmbHistColumn.setSelectedItem(current_column);
					}
				updateDataDisplay();
				return;
				}
			
			if (e.getActionCommand().endsWith("ColourMap Changed")){
				updateColourBar();
				return;
				}
			
			//set a mesh's data set and show the data
			if (e.getActionCommand().endsWith("Apply")){
				if (current_shape == null) return;
				if (cmbDataDisplayColourMap.getSelectedItem() == null) return;
				if (cmbDataDisplayColumn.getSelectedItem() == null) return;
				
				ColourMap map = (ColourMap)cmbDataDisplayColourMap.getSelectedItem();
				String column = (String)cmbDataDisplayColumn.getSelectedItem();
				VertexDataColumn v_column = current_shape.getVertexDataColumn(column);
				if (v_column == null){
					this.showPanel();
					return;
					}
				
				String current_column = current_shape.getCurrentColumn();
				
				if (current_column == null || !current_column.equals(column)){
					v_column.setColourMap(map, false);
					v_column.setColourLimits(Double.valueOf(txtDataMin.getText()).doubleValue(), 
										     Double.valueOf(txtDataMax.getText()).doubleValue());
					current_shape.setCurrentColumn(column, true);
				}else{
					v_column.setColourMap(map, false);
					v_column.setColourLimits(Double.valueOf(txtDataMin.getText()).doubleValue(), 
							   			     Double.valueOf(txtDataMax.getText()).doubleValue());
					}
				
				if (!current_shape.showData())
					current_shape.showData(true);
								
				updateColourBar();
				updateHistogram();
				return;
				}
			
			if (e.getActionCommand().endsWith("Reset")){
				updateMaxMin();
				
				}
			
			}
		
		
		//********************************** HISTOGRAM ********************************************
		if (e.getActionCommand().startsWith("Histogram")){
			
			if (e.getActionCommand().endsWith("Column Changed")){
				updateMaxMinHistogram();
				updateHistogram();
				return;
			}
			
			if (e.getActionCommand().endsWith("Use Current")){
				updateControls();
				updateHistogram();
				return;
			}
			
			if (e.getActionCommand().endsWith("Apply")){
				updateHistogram();
				return;
				}
			
			if (e.getActionCommand().endsWith("Reset")){
				updateMaxMinHistogram();
				}
			
			}
		
	}
	
	protected void updateMaxMin(){
		if (current_shape == null) return;
		//if (cmbDataDisplayColourMap.getSelectedItem() == null) return;
		if (cmbDataDisplayColumn.getSelectedItem() == null) return;
		
		//get max/min
		String column = (String)cmbDataDisplayColumn.getSelectedItem();
		ArrayList<MguiNumber> list = current_shape.getVertexData(column);
		MguiDouble max = new MguiDouble(Double.MIN_VALUE);
		MguiDouble min = new MguiDouble(Double.MAX_VALUE);
		
		for (int i = 0; i < list.size(); i++){
			if (list.get(i).compareTo(max) > 0) max = new MguiDouble(list.get(i));
			if (list.get(i).compareTo(min) < 0) min = new MguiDouble(list.get(i));
			}
		
		txtDataMax.setText(max.toString("##0.0000000"));
		txtDataMax.updateUI();
		txtDataMin.setText(min.toString("##0.0000000"));
		txtDataMin.updateUI();
	}
	
	protected void updateMaxMinHistogram(){
		if (current_shape == null) return;
		//if (cmbDataDisplayColourMap.getSelectedItem() == null) return;
		if (cmbDataDisplayColumn.getSelectedItem() == null) return;
		
		//get max/min
		String column = (String)cmbHistColumn.getSelectedItem();
		ArrayList<MguiNumber> list = current_shape.getVertexData(column);
		MguiDouble max = new MguiDouble(Double.MIN_VALUE);
		MguiDouble min = new MguiDouble(Double.MAX_VALUE);
		
		for (int i = 0; i < list.size(); i++){
			if (list.get(i).compareTo(max) > 0) max = new MguiDouble(list.get(i));
			if (list.get(i).compareTo(min) < 0) min = new MguiDouble(list.get(i));
			}
		
		txtHistMax.setText(max.toString("##0.0000000"));
		txtHistMin.setText(min.toString("##0.0000000"));
	}
	
	/****************************************
	 * Determines which shapes are selected and returns them. If only one shape is selected, returns that shape;
	 * if all shapes are selected, returns the base shape set; if a selection set is selected, returns that set as
	 * a shape set.
	 * 
	 * @return
	 */
	public InterfaceShape getCurrentShapes(){
		
		return current_shape;
	}
	
	public String toString(){
		return "Shape Functions Panel";
	}
	
	static class DataLinkTableModel extends AbstractTableModel {

		ArrayList<LinkedDataStream> streams = new ArrayList<LinkedDataStream>();
		ArrayList<String> names = new ArrayList<String>();
		JTable table;
		
		public DataLinkTableModel(){
			
		}
		
		public DataLinkTableModel(JTable table){
			this.table = table;
		}
		
		public void setTable(JTable table){
			this.table = table;
		}
		
		public void clear(){
			streams.clear();
			names.clear();
			this.fireTableDataChanged();
		}
		
		public String getName(int row){
			return names.get(row);
		}
		
		public void setFromColumn(VertexDataColumn column){
			ArrayList<String> keys = column.getLinkedDataNames();
			streams.clear();
			names.clear();
			for (int i = 0; i < keys.size(); i++){
				streams.add(column.getLinkedData(keys.get(i)));
				names.add(keys.get(i));
				}
			this.fireTableDataChanged();
		}
		
		public ArrayList<LinkedDataStream> getDataStreams(){
			return streams;
		}
		
		public LinkedDataStream getDataStream(int i){
			return streams.get(i);
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		
		public void addStream(String name, LinkedDataStream stream){
			names.add(name);
			streams.add(stream);
			this.fireTableDataChanged();
		}
		
		public void removeStream(String name){
			for (int i = 0; i < names.size(); i++){
				if (names.get(i).equals(name)){
					names.remove(i);
					streams.remove(i);
					}
				}
			this.fireTableDataChanged();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex){
				case 0: return "Name";
				case 1: return "Source";
				case 2: return "Table";
				case 3: return "Link Field";
				}
		return "?";
		}

		@Override
		public int getRowCount() {
			return streams.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex){
				case 0: return names.get(rowIndex);
				case 1: return streams.get(rowIndex).getDataSource().getName();
				case 2: return streams.get(rowIndex).getLinkTable();
				case 3: return streams.get(rowIndex).getLinkField();
				}
			return "?";
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			
		}
		
	}

	
	public static class VertexSelectionTableModel extends AbstractTableModel {
		
		public ArrayList<String> variables = new ArrayList<String>();
		public ArrayList<String> comparison = new ArrayList<String>(); 
		public ArrayList<String> values = new ArrayList<String>();
		
		public VertexSelectionTableModel(){
			
		}
		
		public void clear(){
			variables.clear();
			comparison.clear();
			values.clear();
			this.fireTableDataChanged();
		}
		
		public void addRow(){
			variables.add("?");
			comparison.add("?");
			values.add("?");
			this.fireTableDataChanged();
		}
		
		public void removeRow(int row){
			variables.remove(row);
			comparison.remove(row);
			values.remove(row);
			this.fireTableDataChanged();
		}
		
		public static JComboBox getComparisonCombo(){
			JComboBox box = new JComboBox();
			box.addItem(">");
			box.addItem(">=");
			box.addItem("==");
			box.addItem("!=");
			box.addItem("<=");
			box.addItem("<");
			return box;
		}
		
		public static JComboBox getVariableCombo(InterfaceShape shape){
			JComboBox box = new JComboBox();
//			box.addItem("Coords.x");
//			box.addItem("Coords.y");
//			box.addItem("Coords.z");
			ArrayList<String> vars = shape.getVariables();
			for (int i = 0; i < vars.size(); i++)
				box.addItem(InterfaceShape.fromVariable(vars.get(i)));
			return box;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex){
				case 0: return "Variable";
				case 1: return "Comp";
				case 2: return "Value";
				}
		return "?";
		}

		@Override
		public int getRowCount() {
			return variables.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex){
				case 0: return InterfaceShape.fromVariable(variables.get(rowIndex));
				case 1: return comparison.get(rowIndex);
				case 2: return values.get(rowIndex);
				}
			return "?";
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (rowIndex > getRowCount()-1) return;
			switch (columnIndex){
				case 0: 
					variables.set(rowIndex, InterfaceShape.toVariable((String)aValue));
					return;
				case 1:
					comparison.set(rowIndex, (String)aValue);
					return;
				case 2: 
					values.set(rowIndex, (String)aValue);
					return;
			}
		
		}
		
	}
	
}