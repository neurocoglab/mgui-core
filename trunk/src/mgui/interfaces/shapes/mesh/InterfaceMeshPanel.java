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

package mgui.interfaces.shapes.mesh;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.datasources.DataTable;
import mgui.geometry.Grid3D;
import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.geometry.mesh.MeshEngine;
import mgui.geometry.mesh.MeshFunctionException;
import mgui.geometry.mesh.MeshFunctions;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.ContinuousColourBar;
import mgui.interfaces.math.MathExpressionDialogBox;
import mgui.interfaces.math.MathExpressionOptions;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.numbers.MguiNumber;

/**************************************************************************
 * Provides an interface to {@code Mesh3DInt} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceMeshPanel extends InterfacePanel implements ActionListener, ItemListener {

	//public InterfaceDisplayPanel displayPanel;
	private MeshEngine meshEngine = new MeshEngine();
	protected Mesh3DInt currentMesh;
	protected Mesh3DInt dataOpsMesh;
	protected DataSource currentDataSource;
	protected DataTable currentDataTable;
	protected DataField currentLinkField;
	
	//categories and components
	CategoryTitle lblMeshes = new CategoryTitle("SHAPES");
	JCheckBox chkAll = new JCheckBox("All meshes in model");
	JCheckBox chkSel = new JCheckBox("Selected mesh:");
	InterfaceComboBox cmbCurrentMesh = new InterfaceComboBox(RenderMode.LongestItem, true, 200);
	JCheckBox chkSelSet = new JCheckBox("Selection set:");
	JComboBox cmbCurrentSet = new JComboBox();
	
	CategoryTitle lblSubdivision = new CategoryTitle("SUBDIVISION");
	JLabel lblSubdivideMethod = new JLabel("Subdivision method");
	JComboBox cmbSubdivideMethod = new JComboBox();
	JLabel lblSubdivideParameters = new JLabel("Parameters");
	InterfaceAttributePanel subdivideAttr;
	JButton cmdSubdivide = new JButton("Apply");
	
	CategoryTitle lblDecimation = new CategoryTitle("DECIMATION");
	JLabel lblDecimateMethod = new JLabel("Decimation method");
	JComboBox cmbDecimateMethod = new JComboBox();
	JLabel lblDecimateParameters = new JLabel("Parameters");
	InterfaceAttributePanel decimateAttr;
	JButton cmdDecimate = new JButton("Apply");
	
	CategoryTitle lblInflation = new CategoryTitle("INFLATION");
	JLabel lblInflateMethod = new JLabel("Inflation method");
	JComboBox cmbInflateMethod = new JComboBox();
	JLabel lblInflateParameters = new JLabel("Parameters");
	InterfaceAttributePanel inflateAttr;
	JButton cmdInflate = new JButton("Apply");
	
	CategoryTitle lblRois = new CategoryTitle("ROIS");
	JLabel lblRoisMethod = new JLabel("ROI method");
	JComboBox cmbRoisMethod = new JComboBox();
	JLabel lblRoisParameters = new JLabel("Parameters");
	InterfaceAttributePanel roisAttr;
	JButton cmdRois = new JButton("Apply");
	
	CategoryTitle lblCurvature = new CategoryTitle("CURVATURE");
	protected JLabel lblCurvatureMethod = new JLabel("Curvature method");
	protected JComboBox cmbCurvatureMethod = new JComboBox();
//	protected JLabel lblCurvatureName = new JLabel("Variable name:");
//	protected JTextField txtCurvatureName = new JTextField("curvature");
	InterfaceAttributePanel curvatureAttr;
	protected JButton cmdCurvature = new JButton("Apply");
	
	CategoryTitle lblSmoothing = new CategoryTitle("SMOOTHING");
	JLabel lblSmoothingMethod = new JLabel("Smoothing method");
	JComboBox cmbSmoothingMethod = new JComboBox();
	JLabel lblSmoothParameters = new JLabel("Parameters");
	InterfaceAttributePanel smoothAttr;
	JButton cmdSmooth = new JButton("Apply");
	
	CategoryTitle lblSubmesh = new CategoryTitle("SUBMESH");
	JCheckBox chkSubmeshSelected = new JCheckBox(" Selected vertices");
	JCheckBox chkSubmeshMask = new JCheckBox(" Mask column:");
	JComboBox cmbSubmeshMask = new JComboBox();
	JCheckBox chkSubmeshRetain = new JCheckBox("Retain vertices");
	JCheckBox chkSubmeshRemove = new JCheckBox("Remove vertices");
	JLabel lblSubmeshValue = new JLabel("Mask value:");
	JTextField txtSubmeshValue = new JTextField("0");
	
	JCheckBox chkSubmeshCreate = new JCheckBox("Create new mesh:");
	JTextField txtSubmeshCreate = new JTextField("-");
	JCheckBox chkSubmeshData = new JCheckBox("Copy vertex data");
	JButton cmdSubmesh = new JButton("Apply");
	
	CategoryTitle lblMeshParts = new CategoryTitle("MESH PARTS");
	
	JCheckBox chkMeshPartsShapeSet = new JCheckBox("Create new shape set:");
	JTextField txtMeshPartsShapeSet = new JTextField("-");
	JCheckBox chkMeshPartsData = new JCheckBox("Copy vertex data");
	JButton cmdMeshParts = new JButton("Apply");
	
	CategoryTitle lblVolume = new CategoryTitle("VOLUME TO MESH");
	JLabel lblVolumeSource = new JLabel("Source Volume");
	InterfaceComboBox cmbVolumeSource = new InterfaceComboBox(RenderMode.LongestItem, true, 200);
	JLabel lblVolumeName = new JLabel("Data column name:");
	JTextField txtVolumeName = new JTextField("no-name");
	JLabel lblVolumeMethod = new JLabel("Operation");
	JComboBox cmbVolumeMethod = new JComboBox();
	JLabel lblVolumeParameters = new JLabel("Parameters");
	InterfaceAttributePanel volumeAttr;
	JButton cmdVolume = new JButton("Apply");
	
	CategoryTitle lblMesh2Volume = new CategoryTitle("MESH TO VOLUME");
	JLabel lblMesh2VolumeSource = new JLabel("Volume");
	InterfaceComboBox cmbMesh2VolumeSource = new InterfaceComboBox(RenderMode.LongestItem, true, 200);
	JLabel lblMesh2VolumeMethod = new JLabel("Operation");
	JComboBox cmbMesh2VolumeMethod = new JComboBox();
	JLabel lblMesh2VolumeName = new JLabel("Data column:");
	JComboBox cmbMesh2VolumeName = new JComboBox();
	JLabel lblMesh2VolumeParameters = new JLabel("Parameters");
	InterfaceAttributePanel mesh2VolumeAttr;
	JButton cmdMesh2Volume = new JButton("Apply");
	
	ContinuousColourBar barScaledColourBar = new ContinuousColourBar();
	
	CategoryTitle lblValidate = new CategoryTitle("VALIDATE");
	
	JLabel lblValidateOp = new JLabel("Operation:");
	JComboBox cmbValidateOp = new JComboBox();
	HashMap<String,SubPanel> validate_panels = new HashMap<String,SubPanel>();
	SubPanel current_validate_panel;
	JButton cmdValidate = new JButton("Apply");
	InterfaceComboBox cmbValidateMesh2 = new InterfaceComboBox(RenderMode.LongestItem, true, 200);
	
	CategoryTitle lblMultiFile = new CategoryTitle("MULTI-FILE");
	
	JLabel lblMultiFileFormat = new JLabel("File format:");
	JComboBox cmbMultiFileFormat = new JComboBox();
	
	JLabel lblMultiFileInputDir = new JLabel("Main input folder:");
	JButton cmdMultiFileInputDir = new JButton("Browse..");
	JTextField txtMultiFileInputDir = new JTextField("");
	JCheckBox chkMultiFileInputEmDir = new JCheckBox(" Embedded input folder:");
	JButton cmdMultiFileInputEmDir = new JButton("Browse..");
	JTextField txtMultiFileInputEmDir = new JTextField("");
	JLabel lblMultiFileInputMask = new JLabel("Input filename mask:");
	JTextField txtMultiFileInputMask = new JTextField("");
	
	JCheckBox chkMultiFileOutputInput = new JCheckBox(" Output=input folders");
	JLabel lblMultiFileOutputDir = new JLabel("Main output folder:");
	JButton cmdMultiFileOutputDir = new JButton("Browse..");
	JTextField txtMultiFileOutputDir = new JTextField("");
	JCheckBox chkMultiFileOutputEmDir = new JCheckBox(" Embedded output folder:");
	JButton cmdMultiFileOutputEmDir = new JButton("Browse..");
	JTextField txtMultiFileOutputEmDir = new JTextField("");
	
	JLabel lblMultiFileOutputMask = new JLabel("Output filename mask:");
	JTextField txtMultiFileOutputMask = new JTextField("");
	
	JLabel lblMultiFileOperations = new JLabel("Operations");
	WindowList lstMultiFileOperations;
	JButton cmdMultiFileAddOp = new JButton("Add");
	JButton cmdMultiFileRemOp = new JButton("Rem");
	
	JButton cmdMultiFileExecute = new JButton("Execute");
	
	MeshDataThresholdOptions thresholdDataOptions = new MeshDataThresholdOptions();
	MeshDataMaskOptions maskDataOptions = new MeshDataMaskOptions();
	MeshPlaneMaskOptions maskPlaneOptions = new MeshPlaneMaskOptions();
	
	//constants
	public static final String CMD_SUBDIVIDE = "Subdivide Mesh";
	public static final String CMD_SMOOTH = "Smooth Mesh";
	public static final String CMD_DECIMATE = "Decimate Mesh";
	public static final String CMD_MULTI_INPUT_EM = "Multi Input Embed Folder";
	public static final String CMD_MULTI_INPUT = "Multi Input Folder";
	public static final String CMD_MULTI_OUTPUT_EM = "Multi Output Embed Folder";
	public static final String CMD_MULTI_OUTPUT = "Multi Output Folder";
	
	public static final String CMB_DISPLAY_MESH = "Change Display Mesh";
	public static final String CHK_SEL = "Change Mesh Selected";
	
	public static final int UPDATE_THRESHOLD_OPTIONS = 0;
	
	public static final String CMB_NONE = "<---NONE--->";
	
	boolean doUpdate = true;
	MathExpressionOptions math_expression_options;
	MathExpressionDialogBox mesh_expression_dialog;
	
	public InterfaceMeshPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	protected void init(){
		_init();
		
		//set up panel categories and components
		setLayout(new CategoryLayout(InterfaceEnvironment.getLineHeight(), 5, 200, 10));
		chkAll.setSelected(true);
		
		chkAll.setActionCommand("Selection All Changed");
		chkAll.addActionListener(this);
		chkSelSet.setActionCommand("Selection Set Changed");
		chkSelSet.addActionListener(this);
		chkSel.setActionCommand("Selection Sel Changed");
		chkSel.addActionListener(this);
		
		barScaledColourBar.showAnchors = false;
		barScaledColourBar.showDivisions = true;
		barScaledColourBar.noDivisions = 4;
		barScaledColourBar.divSize = 0.3;
		//barScaledColourBar.setDisplayPanel(getDisplayPanel());
		
		lblMeshes.setHorizontalAlignment(JLabel.CENTER);
		lblSubdivision.setHorizontalAlignment(JLabel.CENTER);
		lblSmoothing.setHorizontalAlignment(JLabel.CENTER);
		lblDecimation.setHorizontalAlignment(JLabel.CENTER);
		lblSubmesh.setHorizontalAlignment(JLabel.CENTER);
		lblMeshParts.setHorizontalAlignment(JLabel.CENTER);
	
		cmdSubdivide.setActionCommand(CMD_SUBDIVIDE);
		cmdSubdivide.addActionListener(this);
		
		cmdDecimate.setActionCommand(CMD_DECIMATE);
		cmdDecimate.addActionListener(this);
		
		cmdInflate.setActionCommand("Inflate Mesh");
		cmdInflate.addActionListener(this);
		
		cmdRois.setActionCommand("Rois Apply");
		cmdRois.addActionListener(this);
		
		cmdSmooth.setActionCommand("Smoothing Apply");
		cmdSmooth.addActionListener(this);
		
		cmdSubmesh.setActionCommand("Submesh Apply");
		cmdSubmesh.addActionListener(this);
		
		cmdMeshParts.setActionCommand("Mesh Parts Apply");
		cmdMeshParts.addActionListener(this);
		
		cmdVolume.setActionCommand("Volume To Mesh");
		cmdVolume.addActionListener(this);
		
		cmdMesh2Volume.setActionCommand("Mesh To Volume");
		cmdMesh2Volume.addActionListener(this);
		
		cmdMultiFileInputDir.addActionListener(this);
		cmdMultiFileInputDir.setActionCommand(CMD_MULTI_INPUT);
		cmdMultiFileInputEmDir.addActionListener(this);
		cmdMultiFileInputEmDir.setActionCommand(CMD_MULTI_INPUT_EM);
		cmdMultiFileOutputDir.addActionListener(this);
		cmdMultiFileOutputDir.setActionCommand(CMD_MULTI_OUTPUT);
		cmdMultiFileOutputEmDir.addActionListener(this);
		cmdMultiFileOutputEmDir.setActionCommand(CMD_MULTI_OUTPUT_EM);
	
		cmdCurvature.setActionCommand("Curvature");
		cmdCurvature.addActionListener(this);
		
		cmbSubdivideMethod.addItem("Butterfly Scheme");
		cmbSmoothingMethod.addItem("Smooth vertex values - Isotropic Gaussian");
		cmbSmoothingMethod.addItem("Local Relaxation");
		cmbSmoothingMethod.setActionCommand("Smoothing Options");
		cmbSmoothingMethod.addActionListener(this);
		cmbDecimateMethod.addItem("Schroeder Decimate");
		cmbDecimateMethod.addItem("Decimate Neighbours");
		cmbDecimateMethod.setActionCommand("Decimate Options");
		cmbDecimateMethod.addActionListener(this);
		
		cmbInflateMethod.addItem("Inflate Mesh TRP");
		cmbVolumeMethod.addItem("Map From Volume (Gaussian)");
		cmbVolumeMethod.addItem("Map From Volume (Enclosing voxel)");
		cmbVolumeMethod.setActionCommand("Volume Options");
		cmbVolumeMethod.addActionListener(this);
		
		cmbMesh2VolumeMethod.addItem("Map To Volume (Gaussian)");
		cmbMesh2VolumeMethod.setActionCommand("Mesh2Volume Options");
		cmbMesh2VolumeMethod.addActionListener(this);
		
		cmbVolumeSource.addActionListener(this);
		cmbVolumeSource.setActionCommand("Volume To Mesh Source Changed");
		
		cmbRoisMethod.addItem("Subdivide ROIs");
		cmbRoisMethod.addItem("Split ROI with Plane");
		cmbRoisMethod.setActionCommand("Rois Options");
		cmbRoisMethod.addActionListener(this);
		
		cmbCurrentMesh.setActionCommand(CMB_DISPLAY_MESH);
		cmbCurrentMesh.addActionListener(this);
		
		cmbValidateOp.setActionCommand("Validate Operation");
		cmbValidateOp.addItemListener(this);
		
		cmdValidate.setActionCommand("Validate Apply");
		cmdValidate.addActionListener(this);
		
		chkSubmeshRetain.setSelected(true);
		chkSubmeshSelected.setSelected(true);
		chkSubmeshSelected.addActionListener(this);
		chkSubmeshSelected.setActionCommand("Submesh Selected");
		chkSubmeshMask.setSelected(false);
		chkSubmeshMask.addActionListener(this);
		chkSubmeshMask.setActionCommand("Submesh Mask");

		chkSubmeshRemove.setSelected(false);
		chkSubmeshRetain.setActionCommand("Submesh Retain");
		chkSubmeshRetain.addActionListener(this);
		chkSubmeshRemove.setActionCommand("Submesh Remove");
		chkSubmeshRemove.addActionListener(this);
		chkSubmeshCreate.setSelected(false);
		txtSubmeshCreate.setEnabled(false);
		chkSubmeshCreate.setActionCommand("Submesh Create");
		chkSubmeshCreate.addActionListener(this);
		
		txtMeshPartsShapeSet.setEnabled(false);
		chkMeshPartsShapeSet.setActionCommand("Mesh Parts ShapeSet");
		chkMeshPartsShapeSet.addActionListener(this);
		
		cmbCurvatureMethod.addItem("Compute Mean Curvature");
		cmdCurvature.setActionCommand("Curvature Apply");
		cmdCurvature.addActionListener(this);
		
		subdivideAttr = new InterfaceAttributePanel(meshEngine.getAttributes("Butterfly Scheme"));
		decimateAttr = new InterfaceAttributePanel(meshEngine.getAttributes("Schroeder Decimate"));
		volumeAttr = new InterfaceAttributePanel(meshEngine.getAttributes("Map From Volume"));
		mesh2VolumeAttr = new InterfaceAttributePanel(meshEngine.getAttributes("Map To Volume"));
		inflateAttr = new InterfaceAttributePanel(meshEngine.getAttributes("Inflate Mesh TRP"));
		roisAttr = new InterfaceAttributePanel(meshEngine.getAttributes("Subdivide ROIs"));
		smoothAttr = new InterfaceAttributePanel(meshEngine.getAttributes("Smooth vertex values - Isotropic Gaussian"));
		curvatureAttr = new InterfaceAttributePanel(meshEngine.getAttributes("Compute Mean Curvature"));
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblMeshes, c);
		lblMeshes.setParentObj(this);
		c = new CategoryLayoutConstraints("SHAPES", 1, 1, 0.05, 0.9, 1);
		add(chkAll, c);
		c = new CategoryLayoutConstraints("SHAPES", 2, 2, 0.05, 0.9, 1);
		add(chkSel, c);
		c = new CategoryLayoutConstraints("SHAPES", 3, 3, 0.15, 0.7, 1);
		add(cmbCurrentMesh, c);
		c = new CategoryLayoutConstraints("SHAPES", 4, 4, 0.05, 0.9, 1);
		add(chkSelSet, c);
		c = new CategoryLayoutConstraints("SHAPES", 5, 5, 0.15, 0.7, 1);
		add(cmbCurrentSet, c);
	
		c = new CategoryLayoutConstraints();
		lblCurvature.isExpanded = false;
		add(lblCurvature, c);
		lblCurvature.setParentObj(this);
		c = new CategoryLayoutConstraints("CURVATURE", 1, 1, 0.05, 0.9, 1);
		add(lblCurvatureMethod, c);
		c = new CategoryLayoutConstraints("CURVATURE", 2, 2, 0.05, 0.9, 1);
		add(cmbCurvatureMethod, c);
		c = new CategoryLayoutConstraints("CURVATURE", 3, 7, 0.05, 0.9, 1);
		add(curvatureAttr, c);
		c = new CategoryLayoutConstraints("CURVATURE", 8, 9, 0.1, 0.8, 1);
		add(cmdCurvature, c);
		
		c = new CategoryLayoutConstraints();
		lblSubdivision.isExpanded = false;
		add(lblSubdivision, c);
		lblSubdivision.setParentObj(this);
		c = new CategoryLayoutConstraints("SUBDIVISION", 1, 1, 0.05, 0.9, 1);
		add(lblSubdivideMethod, c);
		c = new CategoryLayoutConstraints("SUBDIVISION", 2, 2, 0.05, 0.9, 1);
		add(cmbSubdivideMethod, c);
		c = new CategoryLayoutConstraints("SUBDIVISION", 3, 3, 0.05, 0.9, 1);
		add(lblSubdivideParameters, c);
		c = new CategoryLayoutConstraints("SUBDIVISION", 4, 8, 0.05, 0.9, 1);
		add(subdivideAttr, c);
		c = new CategoryLayoutConstraints("SUBDIVISION", 9, 10, 0.1, 0.8, 1);
		add(cmdSubdivide, c);
		
		c = new CategoryLayoutConstraints();
		lblDecimation.isExpanded = false;
		add(lblDecimation, c);
		lblDecimation.setParentObj(this);
		c = new CategoryLayoutConstraints("DECIMATION", 1, 1, 0.05, 0.9, 1);
		add(lblDecimateMethod, c);
		c = new CategoryLayoutConstraints("DECIMATION", 2, 2, 0.05, 0.9, 1);
		add(cmbDecimateMethod, c);
		c = new CategoryLayoutConstraints("DECIMATION", 3, 3, 0.05, 0.9, 1);
		add(lblDecimateParameters, c);
		c = new CategoryLayoutConstraints("DECIMATION", 4, 8, 0.05, 0.9, 1);
		add(decimateAttr, c);
		c = new CategoryLayoutConstraints("DECIMATION", 9, 10, 0.1, 0.8, 1);
		add(cmdDecimate, c);
		
		c = new CategoryLayoutConstraints();
		lblInflation.isExpanded = false;
		add(lblInflation, c);
		lblInflation.setParentObj(this);
		c = new CategoryLayoutConstraints("INFLATION", 1, 1, 0.05, 0.9, 1);
		add(lblInflateMethod, c);
		c = new CategoryLayoutConstraints("INFLATION", 2, 2, 0.05, 0.9, 1);
		add(cmbInflateMethod, c);
		c = new CategoryLayoutConstraints("INFLATION", 3, 3, 0.05, 0.9, 1);
		add(lblInflateParameters, c);
		c = new CategoryLayoutConstraints("INFLATION", 4, 8, 0.05, 0.9, 1);
		add(inflateAttr, c);
		c = new CategoryLayoutConstraints("INFLATION", 9, 10, 0.1, 0.8, 1);
		add(cmdInflate, c);
		
		c = new CategoryLayoutConstraints();
		lblRois.isExpanded = false;
		add(lblRois, c);
		lblRois.setParentObj(this);
		c = new CategoryLayoutConstraints("ROIS", 1, 1, 0.05, 0.9, 1);
		add(lblRoisMethod, c);
		c = new CategoryLayoutConstraints("ROIS", 2, 2, 0.05, 0.9, 1);
		add(cmbRoisMethod, c);
		c = new CategoryLayoutConstraints("ROIS", 3, 3, 0.05, 0.9, 1);
		add(lblRoisParameters, c);
		c = new CategoryLayoutConstraints("ROIS", 4, 8, 0.05, 0.9, 1);
		add(roisAttr, c);
		c = new CategoryLayoutConstraints("ROIS", 9, 10, 0.1, 0.8, 1);
		add(cmdRois, c);
		
		c = new CategoryLayoutConstraints();
		lblSmoothing.isExpanded = false;
		add(lblSmoothing, c);
		lblSmoothing.setParentObj(this);
		c = new CategoryLayoutConstraints("SMOOTHING", 1, 1, 0.05, 0.9, 1);
		add(lblSmoothingMethod, c);
		c = new CategoryLayoutConstraints("SMOOTHING", 2, 2, 0.05, 0.9, 1);
		add(cmbSmoothingMethod, c);
		c = new CategoryLayoutConstraints("SMOOTHING", 3, 3, 0.05, 0.9, 1);
		add(lblSmoothParameters, c);
		c = new CategoryLayoutConstraints("SMOOTHING", 4, 8, 0.05, 0.9, 1);
		add(smoothAttr, c);
		c = new CategoryLayoutConstraints("SMOOTHING", 9, 10, 0.1, 0.8, 1);
		add(cmdSmooth, c);
		
		c = new CategoryLayoutConstraints();
		lblSubmesh.isExpanded = false;
		lblSubmesh.setParentObj(this);
		add(lblSubmesh, c);
		c = new CategoryLayoutConstraints("SUBMESH", 1, 1, 0.05, 0.9, 1);
		add(chkSubmeshSelected, c);
		c = new CategoryLayoutConstraints("SUBMESH", 2, 2, 0.05, 0.9, 1);
		add(chkSubmeshMask, c);
		c = new CategoryLayoutConstraints("SUBMESH", 3, 3, 0.05, 0.9, 1);
		add(cmbSubmeshMask, c);
		c = new CategoryLayoutConstraints("SUBMESH", 4, 4, 0.05, 0.9, 1);
		add(chkSubmeshRetain, c);
		c = new CategoryLayoutConstraints("SUBMESH", 5, 5, 0.05, 0.9, 1);
		add(chkSubmeshRemove, c);
		c = new CategoryLayoutConstraints("SUBMESH", 6, 6, 0.05, 0.4, 1);
		add(lblSubmeshValue, c);
		c = new CategoryLayoutConstraints("SUBMESH", 6, 6, 0.5, 0.45, 1);
		add(txtSubmeshValue, c);
		c = new CategoryLayoutConstraints("SUBMESH", 7, 7, 0.05, 0.9, 1);
		add(chkSubmeshCreate, c);
		c = new CategoryLayoutConstraints("SUBMESH", 8, 8, 0.1, 0.85, 1);
		add(txtSubmeshCreate, c);
		c = new CategoryLayoutConstraints("SUBMESH", 9, 9, 0.1, 0.85, 1);
		add(chkSubmeshData, c);
		c = new CategoryLayoutConstraints("SUBMESH", 10, 10, 0.1, 0.8, 1);
		add(cmdSubmesh, c);
		
		c = new CategoryLayoutConstraints();
		lblMeshParts.isExpanded = false;
		lblMeshParts.setParentObj(this);
		add(lblMeshParts, c);
		c = new CategoryLayoutConstraints("MESH PARTS", 1, 1, 0.05, 0.9, 1);
		add(chkMeshPartsShapeSet, c);
		c = new CategoryLayoutConstraints("MESH PARTS", 2, 2, 0.05, 0.9, 1);
		add(txtMeshPartsShapeSet, c);
		c = new CategoryLayoutConstraints("MESH PARTS", 3, 3, 0.05, 0.9, 1);
		add(chkMeshPartsData, c);
		c = new CategoryLayoutConstraints("MESH PARTS", 4, 4, 0.05, 0.9, 1);
		add(cmdMeshParts, c);
		
		c = new CategoryLayoutConstraints();
		lblVolume.isExpanded = false;
		add(lblVolume, c);
		lblVolume.setParentObj(this);
		c = new CategoryLayoutConstraints("VOLUME TO MESH", 1, 1, 0.05, 0.9, 1);
		add(lblVolumeSource, c);
		c = new CategoryLayoutConstraints("VOLUME TO MESH", 2, 2, 0.05, 0.9, 1);
		add(cmbVolumeSource, c);
		c = new CategoryLayoutConstraints("VOLUME TO MESH", 3, 3, 0.05, 0.9, 1);
		add(lblVolumeMethod, c);
		c = new CategoryLayoutConstraints("VOLUME TO MESH", 4, 4, 0.05, 0.9, 1);
		add(cmbVolumeMethod, c);
		c = new CategoryLayoutConstraints("VOLUME TO MESH", 5, 5, 0.05, 0.9, 1);
		add(lblVolumeParameters, c);
		c = new CategoryLayoutConstraints("VOLUME TO MESH", 6, 10, 0.05, 0.9, 1);
		add(volumeAttr, c);
		c = new CategoryLayoutConstraints("VOLUME TO MESH", 11, 11, 0.05, 0.9, 1);
		add(cmdVolume, c);
		
		c = new CategoryLayoutConstraints();
		lblMesh2Volume.isExpanded = false;
		add(lblMesh2Volume, c);
		lblMesh2Volume.setParentObj(this);
		c = new CategoryLayoutConstraints("MESH TO VOLUME", 1, 1, 0.05, 0.9, 1);
		add(lblMesh2VolumeSource, c);
		c = new CategoryLayoutConstraints("MESH TO VOLUME", 2, 2, 0.05, 0.9, 1);
		add(cmbMesh2VolumeSource, c);
		c = new CategoryLayoutConstraints("MESH TO VOLUME", 3, 3, 0.05, 0.9, 1);
		add(lblMesh2VolumeMethod, c);
		c = new CategoryLayoutConstraints("MESH TO VOLUME", 4, 4, 0.05, 0.9, 1);
		add(cmbMesh2VolumeMethod, c);
		c = new CategoryLayoutConstraints("MESH TO VOLUME", 5, 5, 0.05, 0.9, 1);
		add(lblMesh2VolumeParameters, c);
		c = new CategoryLayoutConstraints("MESH TO VOLUME", 6, 10, 0.05, 0.9, 1);
		add(mesh2VolumeAttr, c);
		c = new CategoryLayoutConstraints("MESH TO VOLUME", 11, 11, 0.05, 0.9, 1);
		add(cmdMesh2Volume, c);
		
		
		c = new CategoryLayoutConstraints();
		lblValidate.isExpanded = false;
		add(lblValidate, c);
		lblValidate.setParentObj(this);
		c = new CategoryLayoutConstraints("VALIDATE", 1, 1, 0.05, 0.3, 1);
		add(lblValidateOp, c);
		c = new CategoryLayoutConstraints("VALIDATE", 1, 1, 0.35, 0.6, 1);
		add(cmbValidateOp, c);
		initValidatePanels();
		
		//call this to update category appearances
		((CategoryLayout)this.getLayout()).updateParent(lblSmoothing);
		
		//currentDataOptions = thresholdDataOptions;
		updateDisplay();
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceMeshPanel.class.getResource("/mgui/resources/icons/mesh_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/mesh_3d_20.png");
		return null;
	}
	
	public void cleanUpPanel(){
		//clear combo lists to avoid memory leaks (references to mesh objects which may be deleted)
		doUpdate = false;
		cmbCurrentMesh.removeAllItems();
		//cmbDataOpsMesh.removeAllItems();
		cmbSubmeshMask.removeAllItems();
		cmbVolumeSource.removeAllItems();
		doUpdate = true;
	}
	
	public void shapeUpdated(ShapeEvent e){
		
		switch (e.eventType){
		
		case ShapeRemoved:
			
			if (e.getShape().equals(currentMesh)) currentMesh = null;
			if (this.isVisible()) showPanel();
			
			return;
		
		}
		
		
	}
	
	public void showPanel(){
		updateDisplay();
	}
	
	public void updateDisplay(){
		updateLists();
		initValidatePanels();
		updateParameters();
		updateVolumes();
		updateControls();
		updateUI();
	}
	
	protected void updateParameters(){
		decimateAttr.setAttributes(meshEngine.getAttributes((String)cmbDecimateMethod.getSelectedItem()));
		volumeAttr.setAttributes(meshEngine.getAttributes((String)cmbVolumeMethod.getSelectedItem()));
		mesh2VolumeAttr.setAttributes(meshEngine.getAttributes((String)cmbMesh2VolumeMethod.getSelectedItem()));
		roisAttr.setAttributes(meshEngine.getAttributes((String)cmbRoisMethod.getSelectedItem()));
		curvatureAttr.setAttributes(meshEngine.getAttributes((String)cmbCurvatureMethod.getSelectedItem()));
		AttributeList list = meshEngine.getAttributes((String)cmbSmoothingMethod.getSelectedItem());
		if (list != null)
			smoothAttr.setAttributes(list);
		else
			smoothAttr.setAttributes(new AttributeList());
		
		String item = (String)cmbRoisMethod.getSelectedItem();
		if (item != null && item.equals("Split ROI with Plane") && currentMesh != null){
			list = meshEngine.getAttributes(item);
			AttributeSelection<String> columns = (AttributeSelection<String>)list.getAttribute("roi_column");
			ArrayList<String> names = currentMesh.getVertexDataColumnNames();
			String current = columns.getValue();
			columns.setList(names);
			if (current != null)
				columns.setValue(current);
			}
		
		item = (String)cmbSmoothingMethod.getSelectedItem();
		if (item != null && item.equals("Smooth vertex values - Isotropic Gaussian") && currentMesh != null){
			list = meshEngine.getAttributes(item);
			AttributeSelection<String> columns = (AttributeSelection<String>)list.getAttribute("input_column");
			ArrayList<String> names = currentMesh.getVertexDataColumnNames();
			String current = columns.getValue();
			columns.setList(names);
			if (current != null)
				columns.setValue(current);
			}
		
		item = (String)cmbVolumeMethod.getSelectedItem();
		if (item != null && item.equals("Map From Volume (Gaussian)") && currentMesh != null){
			Volume3DInt source = (Volume3DInt)cmbVolumeSource.getSelectedItem();
			if (source != null){
				list = meshEngine.getAttributes(item);
				AttributeSelection<String> columns = (AttributeSelection<String>)list.getAttribute("input_column");
				ArrayList<String> names = source.getVertexDataColumnNames();
				String current = columns.getValue();
				columns.setList(names);
				if (current != null)
					columns.setValue(current);
				}
			}
		
		if (item != null && item.equals("Map From Volume (Enclosing voxel)") && currentMesh != null){
			Volume3DInt source = (Volume3DInt)cmbVolumeSource.getSelectedItem();
			if (source != null){
				list = meshEngine.getAttributes(item);
				AttributeSelection<String> columns = (AttributeSelection<String>)list.getAttribute("input_column");
				ArrayList<String> names = source.getVertexDataColumnNames();
				String current = columns.getValue();
				columns.setList(names);
				if (current != null)
					columns.setValue(current);
				}
			}
		
		item = (String)cmbCurvatureMethod.getSelectedItem();
		if (item != null && item.equals("Compute Mean Curvature") && currentMesh != null){
			list = meshEngine.getAttributes(item);
			AttributeSelection<String> columns = (AttributeSelection<String>)list.getAttribute("target_column");
			ArrayList<String> names = currentMesh.getVertexDataColumnNames();
			String current = columns.getValue();
			columns.setList(names);
			if (current != null)
				columns.setValue(current);
			}
		
	}
	
	protected void updateControls(){
		boolean isSel = chkSel.isSelected();
		
		doUpdate = false;
		
		cmbCurrentMesh.setEnabled(isSel);
		cmbCurrentSet.setEnabled(chkSelSet.isSelected());
		
		isSel &= currentMesh != null;
		isSel = chkSubmeshSelected.isSelected();
		cmbSubmeshMask.setEnabled(!isSel);
		lblSubmeshValue.setEnabled(!isSel);
		txtSubmeshValue.setEnabled(!isSel);
		
		doUpdate = true;
	}
	
	protected void updateLists(){
		//meshes
		doUpdate = false;
		cmbCurrentMesh.removeAllItems();
		//cmbDataOpsMesh.removeAllItems();
		Object current_sel_set = cmbCurrentSet.getSelectedItem();
		cmbCurrentSet.removeAllItems();
		
		//selection sets
		ArrayList<ShapeSelectionSet> sets = InterfaceSession.getDisplayPanel().getCurrentShapeModel().getSelectionSets();
		for (int i = 0; i < sets.size(); i++)
			cmbCurrentSet.addItem(sets.get(i));
		if (current_sel_set != null) cmbCurrentSet.setSelectedItem(current_sel_set);
		
		ShapeSet3DInt meshes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
		for (int i = 0; i < meshes.members.size(); i++){
			cmbCurrentMesh.addItem((Mesh3DInt)meshes.members.get(i));
			//cmbDataOpsMesh.addItem(meshes.members.get(i));
			}
		
		if (cmbCurrentMesh.getItemCount() == 0){
			currentMesh = null;
			dataOpsMesh = null;
			return;
			}
		
		if (currentMesh != null)
			cmbCurrentMesh.setSelectedItem(currentMesh);
		else{
			cmbCurrentMesh.setSelectedIndex(0);
			currentMesh = (Mesh3DInt)cmbCurrentMesh.getSelectedItem();
			}
		
		updateDataColumns();
		//updateNameMaps();
		doUpdate = true;
	}
	
	protected void updateVolumes(){
		
		cmbVolumeSource.removeAllItems();
		cmbMesh2VolumeSource.removeAllItems();
		ShapeSet3DInt volumes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Volume3DInt());
		
		for (int i = 0; i < volumes.members.size(); i++){
			cmbVolumeSource.addItem(volumes.members.get(i));
			cmbMesh2VolumeSource.addItem(volumes.members.get(i));
			}
		
	}
	
	protected void updateCurrentMesh(){
		if (cmbCurrentMesh.getItemCount() == 0) return;
		
		if (currentMesh != null)
			cmbCurrentMesh.setSelectedItem(currentMesh);
		else{
			cmbCurrentMesh.setSelectedIndex(0);
			currentMesh = (Mesh3DInt)cmbCurrentMesh.getSelectedItem();
			}
		
		updateDataColumns();
		updateParameters();
	}
	
	
	
	protected void updateDataColumns(){
		//fill mesh columns
		String currentDataOpCol = null, currentSubmeshCol = null, currentNameMapCol = null, currentLinkColumn = null, currentDataDisplayCol = null;
		boolean dataOpColFound = false, submeshColFound = false, namemapColFound = false, linkcolumnFound = false, dataDisplayFound = false;;
		
		if (cmbSubmeshMask.getSelectedItem() != null)
			currentSubmeshCol = (String)cmbSubmeshMask.getSelectedItem();
		
		cmbSubmeshMask.removeAllItems();
		
		if (currentMesh == null || ! currentMesh.hasData()) return;
		
		doUpdate = false;
		
		ArrayList<String> list = currentMesh.getVertexDataColumnNames();
		if (list == null){
			doUpdate = true;
			return;
			}
		
		for (int i = 0; i < list.size(); i++){
			cmbSubmeshMask.addItem(list.get(i));
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
			}
		
		if (submeshColFound) cmbSubmeshMask.setSelectedItem(currentSubmeshCol);
		
		doUpdate = true;
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().startsWith("Multi")){
			JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(false);
			fc.showDialog(InterfaceSession.getSessionFrame(), "Select Folder");
			File folder = fc.getSelectedFile();
			if (e.equals(CMD_MULTI_INPUT)){
				txtMultiFileInputDir.setText(folder.getAbsolutePath());
				}
			updateUI();
			return;
			}
		
		if (e.getActionCommand().startsWith("Selection")){
			if (!doUpdate) return;
			if (e.getActionCommand().endsWith("All Changed")){
				doUpdate = false;
				if (!chkAll.isSelected()){
					chkSel.setSelected(true);
				}else{
					chkSel.setSelected(false);
					chkSelSet.setSelected(false);
					}
				doUpdate = true;
				}
			if (e.getActionCommand().endsWith("Set Changed")){
				doUpdate = false;
				if (!chkSelSet.isSelected()){
					chkAll.setSelected(true);
				}else{
					chkAll.setSelected(false);
					chkSel.setSelected(false);
					}
				doUpdate = true;
				}
			
			if (e.getActionCommand().endsWith("Sel Changed")){
				doUpdate = false;
				if (!chkSel.isSelected()){
					chkAll.setSelected(true);
				}else{
					chkSelSet.setSelected(false);
					chkAll.setSelected(false);
					}
				doUpdate = true;
				}
			
			updateControls();
			updateUI();
			
			return;
			}
		
		if (e.getActionCommand().equals(CMB_DISPLAY_MESH) && doUpdate){
			
			currentMesh = (Mesh3DInt)cmbCurrentMesh.getSelectedItem();
			
			updateCurrentMesh();
			updateUI();
			return;
			}
		
		if (e.getActionCommand().compareTo(CMD_SUBDIVIDE) == 0){
			
			if (chkAll.isSelected()){
				/**@TODO allow for various subdivision schemes here ***/
				
				//subdivide all meshes
				ShapeSet3DInt meshSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
				Mesh3DInt thisMeshInt;
				
				if (chkAll.isSelected()){
					for (int i = 0; i < meshSet.members.size(); i++){
						thisMeshInt = (Mesh3DInt)meshSet.members.get(i);
						meshEngine.SubdivideButterflyScheme(thisMeshInt);
						thisMeshInt.setScene3DObject();
						thisMeshInt.fireShapeModified();
						}
					//return;
					}
				
				if (chkSel.isSelected()){
					meshEngine.SubdivideButterflyScheme(currentMesh);
					currentMesh.setScene3DObject();
					currentMesh.fireShapeModified();
					}
				
				}
			}
		
		if (e.getActionCommand().startsWith("Volume To Mesh")){
			
			if (e.getActionCommand().endsWith("Source Changed")){
				updateParameters();
				return;
				}
			
			if (cmbVolumeSource.getSelectedItem() == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No source volume specified!");
				return;
				}
			
			boolean currentDone = false;
			if (chkAll.isSelected()){
				
				//transform all meshes
				ShapeSet3DInt meshSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
				Mesh3DInt thisMeshInt;
				
				for (int i = 0; i < meshSet.members.size(); i++){
					thisMeshInt = (Mesh3DInt)meshSet.members.get(i);
					currentDone |= (thisMeshInt == currentMesh);
					InterfaceProgressBar progress_bar = new InterfaceProgressBar("Mapping '" + 
																				thisMeshInt.getName() + "': ");
				
					progress_bar.register();
					
					ArrayList<MguiNumber> mapped_values = 
						meshEngine.mapVolumeToMesh(thisMeshInt.getMesh(),
												   (Volume3DInt)cmbVolumeSource.getSelectedItem(),
												   (String)cmbVolumeMethod.getSelectedItem(),
												   progress_bar);
					
					progress_bar.deregister();
					
					if (mapped_values == null){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Problem mapping volume to surface, or process was cancelled..");
						//return;
					}else{
						thisMeshInt.addVertexData((String)volumeAttr.getAttribute("target_column").getValue(), mapped_values);
						thisMeshInt.updateShape();
						thisMeshInt.setScene3DObject();
						thisMeshInt.fireShapeModified();
						}
					}
				}
			
			if (chkSel.isSelected() && currentMesh != null && !currentDone){
				InterfaceProgressBar progress_bar = new InterfaceProgressBar("Mapping '" + 
						currentMesh.getName() +"': ");
			
				progress_bar.register();
				ArrayList<MguiNumber> mapped_values = 
					meshEngine.mapVolumeToMesh(currentMesh.getMesh(),
											   (Volume3DInt)cmbVolumeSource.getSelectedItem(),
											   (String)cmbVolumeMethod.getSelectedItem(),
											   progress_bar);
				progress_bar.deregister();
				
				if (mapped_values == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Problem mapping volume to surface..");
					return;
					}
				
				String column = (String)volumeAttr.getAttribute("target_column").getValue();
				if (currentMesh.hasColumn(column)){
					currentMesh.setVertexData(column, mapped_values);
				}else{
					currentMesh.addVertexData(column, mapped_values);
					}
				
				currentMesh.updateShape();
				currentMesh.setScene3DObject();
				currentMesh.fireShapeModified();
				}
			}
		
		if (e.getActionCommand().equals("Mesh To Volume")){
			
			if (cmbMesh2VolumeSource.getSelectedItem() == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No volume specified!");
				return;
				}
			
			boolean currentDone = false;
			Volume3DInt volume = ((Volume3DInt)cmbMesh2VolumeSource.getSelectedItem());
			
			if (chkSel.isSelected() && currentMesh != null && !currentDone){
				InterfaceProgressBar progress_bar = new InterfaceProgressBar("Mapping '" + 
						currentMesh.getName() +"': ");

				progress_bar.register();
			
				Volume3DInt mapped_volume = 
					meshEngine.mapMeshToVolume(currentMesh,
											   volume,
											   (String)cmbMesh2VolumeMethod.getSelectedItem(),
											   progress_bar);
				
				progress_bar.deregister();
				
				if (mapped_volume == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Mapping operation failed..");
					return;
					}
				
				String channel = (String)mesh2VolumeAttr.getAttribute("grid_channel").getValue();
				volume.addVertexData(channel, mapped_volume.getVertexData(channel));
				//volume.getGrid().copyChannel(mapped_grid, channel, channel);
//				volume.setTexture(false);
//				volume.setScene3DObject();
//				volume.fireShapeModified();
				
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Successfully mapped surface to volume.",
											  "Map Mesh to Volume",
											  JOptionPane.INFORMATION_MESSAGE);
				
				return;
				}
			}
		
		if (e.getActionCommand().equals("Curvature Apply")){
			
			//TODO: allow multiple surfaces..?
//			String operation = (String)cmbCurvatureMethod.getSelectedItem();
//			String method = "";
//			if (operation.contains(" - ")){
//				String[] parts = operation.split(" - ");
//				operation = parts[0];
//				method = parts[1];
//				}
			
			InterfaceProgressBar progress_bar = new InterfaceProgressBar("Computing mean curvature: ");
			if (meshEngine.computeMeanCurvature(currentMesh, progress_bar)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											 "Operation successful.", 
											 "Compute mean curvature", 
											 JOptionPane.INFORMATION_MESSAGE);
			}else{
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											 "Operation failed; see console.", 
											 "Compute mean curvature", 
											 JOptionPane.ERROR_MESSAGE);
				}
			
			return;
			}
		
		if (e.getActionCommand().startsWith("Smoothing")){
			
			String item = (String)cmbSmoothingMethod.getSelectedItem();
		
			if (e.getActionCommand().endsWith("Options")){
				updateParameters();
				updateUI();
				return;
				}
			
			
			if (e.getActionCommand().endsWith("Apply")){
				
				if (item.equals("Local Relaxation")){
					ShapeSet3DInt meshSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
					Mesh3DInt thisMeshInt;
					
					if (chkAll.isSelected()){
						for (int i = 0; i < meshSet.members.size(); i++){
							thisMeshInt = (Mesh3DInt)meshSet.members.get(i);
							meshEngine.SmoothEM(thisMeshInt);
							thisMeshInt.setScene3DObject();
							thisMeshInt.fireShapeModified();
							}
						//return;
						}
					
					if (chkSel.isSelected()){
						meshEngine.SmoothEM(currentMesh);
						currentMesh.setScene3DObject();
						currentMesh.fireShapeModified();
						//return;
						}
					
					return;
					}
				
				if (item.equals("Smooth vertex values - Isotropic Gaussian")){
					if (currentMesh == null) return;
					InterfaceProgressBar progress_bar = new InterfaceProgressBar("Smoothing vertex values for '" + 
							currentMesh.getName() + "': ");

					progress_bar.register();
					
					try{
						meshEngine.smoothVertexValuesIsotropicGaussian(currentMesh, progress_bar);
					}catch (MeshFunctionException ex){
						InterfaceSession.handleException(ex);
						}
					
					progress_bar.deregister();
					//currentMesh.updateShape();
					currentMesh.setScene3DObject();
					currentMesh.fireShapeModified();
					
					return;
					}
				
				
			}
			
		}
		
		if (e.getActionCommand().startsWith("Rois")){
			
			String item = (String)cmbRoisMethod.getSelectedItem();
		
			if (e.getActionCommand().endsWith("Options")){
				updateParameters();
				updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				
				if (currentMesh == null) return;
				
				if (item.equals("Subdivide ROIs")){
				
					InterfaceProgressBar progress_bar = new InterfaceProgressBar("Subdividing ROIs for '" + 
															((Mesh3DInt)cmbCurrentMesh.getSelectedItem()).getName() + "': ");
	
					progress_bar.register();
					
					try{
						meshEngine.doRoiOperation(this.currentMesh, item, progress_bar);
					}catch (MeshFunctionException ex){
						InterfaceSession.handleException(ex);
						}
					
					progress_bar.deregister();
					}
				
				if (item.equals("Split ROI with Plane")){
					
					try{
						meshEngine.doRoiOperation(this.currentMesh, item, null);
					}catch (MeshFunctionException ex){
						InterfaceSession.handleException(ex);
						}
					
					
					}
				
				
				}
			
			
			return;
			}
		
		if (e.getActionCommand().startsWith("Submesh")){
			if (e.getActionCommand().endsWith("Selected")){
				if (!doUpdate) return;
				doUpdate = false;
				chkSubmeshMask.setSelected(!chkSubmeshSelected.isSelected());
				doUpdate = true;
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Mask")){
				if (!doUpdate) return;
				doUpdate = false;
				chkSubmeshSelected.setSelected(!chkSubmeshMask.isSelected());
				doUpdate = true;
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Retain")){
				chkSubmeshRemove.setSelected(!chkSubmeshRetain.isSelected());
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				chkSubmeshRetain.setSelected(!chkSubmeshRemove.isSelected());
				return;
				}
			
			if (e.getActionCommand().endsWith("Create")){
				txtSubmeshCreate.setEnabled(chkSubmeshCreate.isSelected());
				chkSubmeshData.setEnabled(chkSubmeshCreate.isSelected());
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply") && 
				chkSel.isSelected() && currentMesh != null && 
				((chkSubmeshMask.isSelected() && cmbSubmeshMask.getSelectedItem() != null) ||
				  chkSubmeshSelected.isSelected())){
				
				Mesh3DInt mesh = new Mesh3DInt();
				
				if (chkSubmeshSelected.isSelected()){
					if (!chkSubmeshCreate.isSelected() || chkSubmeshData.isSelected()){
						meshEngine.getSubMesh(currentMesh,
											  mesh,
											  chkSubmeshRetain.isSelected(),
											  true);
					}else{
						meshEngine.getSubMesh(currentMesh,
											  mesh,
											  chkSubmeshRetain.isSelected(),
											  false);
						}
				}else{
					if (!chkSubmeshCreate.isSelected() || chkSubmeshData.isSelected()){
						meshEngine.getSubMesh(currentMesh,
											  mesh,
											  currentMesh.getVertexData((String)cmbSubmeshMask.getSelectedItem()), 
											  Integer.valueOf(txtSubmeshValue.getText()).intValue(), 
											  chkSubmeshRetain.isSelected(),
											  true);
					}else{
						meshEngine.getSubMesh(currentMesh,
											  mesh,
											  currentMesh.getVertexData((String)cmbSubmeshMask.getSelectedItem()), 
											  Integer.valueOf(txtSubmeshValue.getText()).intValue(), 
											  chkSubmeshRetain.isSelected(),
											  false);
						}
					}
				
				if (chkSubmeshCreate.isSelected()){
					String name = txtSubmeshCreate.getText(); 
					if (name == null || name.length() == 0)
						name = "No name";
					mesh.setName(name);
					InterfaceSession.getDisplayPanel().addShapeInt(mesh);
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "New mesh '" + name + "' created.");
				}else{
					currentMesh.setMesh(mesh.getMesh());
					currentMesh.setVertexDataMap(mesh.getVertexDataMap());
					currentMesh.fireShapeModified();
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Mesh '" + currentMesh.getName() + "' updated.");
					}
				}
			
		}
		
		if (e.getActionCommand().startsWith("Mesh Parts")){
			
			if (e.getActionCommand().endsWith("ShapeSet")) {
				
				txtMeshPartsShapeSet.setEnabled(chkMeshPartsShapeSet.isSelected());
				return;
				
			}
			
			
			if (e.getActionCommand().endsWith("Apply")) {
				
				if (chkMeshPartsShapeSet.isSelected()) {
					String txt = txtMeshPartsShapeSet.getText();
					if (txt == null || txt.length() == 0 || txt.equals("-")) {
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Invalid name for new shape set!", 
													  "Get Mesh Parts", 
													  JOptionPane.ERROR_MESSAGE);
						return;
						}
					}
			
				// Create new Shape Set
				ShapeSet3DInt shape_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
				
				if (chkMeshPartsShapeSet.isSelected()) {
					ShapeSet3DInt new_set = new ShapeSet3DInt(txtMeshPartsShapeSet.getText());
					shape_set.addShape(new_set, true);
					shape_set = new_set;
					}
					
				InterfaceProgressBar progress_bar = new InterfaceProgressBar(" Getting mesh parts for '" + 
																			 currentMesh.getName() + "': ");
	
				meshEngine.getMeshParts(currentMesh, shape_set, chkMeshPartsData.isSelected(), progress_bar);
				
				return;
				}
			}
		
		if (e.getActionCommand().equals("Decimate Options")){
			updateParameters();
			updateUI();
			return;
		}
		
		if (e.getActionCommand().equals("Transform Options")){
			updateParameters();
			updateUI();
			return;
		}
		
		if (e.getActionCommand().equals("Volume Options")){
			updateParameters();
			updateUI();
			return;
		}
		
		if (e.getActionCommand().equals("Mesh2Volume Options")){
			updateParameters();
			updateUI();
			return;
		}
		
		if (e.getActionCommand().compareTo(CMD_DECIMATE) == 0){
			
			boolean currentDone = false;
			if (chkAll.isSelected()){
				/**@TODO allow for various decimation schemes here ***/
				
				//subdivide all meshes
				ShapeSet3DInt meshSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
				Mesh3DInt thisMeshInt;
				Mesh3D thisMesh, newMesh = null;
				
				for (int i = 0; i < meshSet.members.size(); i++){
					thisMeshInt = (Mesh3DInt)meshSet.members.get(i);
					currentDone |= (thisMeshInt == currentMesh);
					thisMesh = thisMeshInt.getMesh();
					newMesh = new Mesh3D();
					//meshEngine.DecimateByDistance(thisMesh, newMesh);
					meshEngine.decimate(newMesh, (String)cmbDecimateMethod.getSelectedItem());
					thisMeshInt.setMesh(newMesh);
					thisMeshInt.setScene3DObject();
					thisMeshInt.fireShapeModified();
					}
				}
			
			if (chkSel.isSelected() && currentMesh != null && !currentDone){
				meshEngine.decimate(currentMesh.getMesh(), (String)cmbDecimateMethod.getSelectedItem());
				currentMesh.setScene3DObject();
				currentMesh.fireShapeModified();
				}
			
			}
		
		if (e.getActionCommand().equals("Inflate Mesh")){
			
			String method = (String)cmbInflateMethod.getSelectedItem();
			
			boolean currentDone = false;
			if (chkAll.isSelected()){
				ShapeSet3DInt meshSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
				for (int i = 0; i < meshSet.members.size(); i++){
					Mesh3DInt thisMesh =  (Mesh3DInt)meshSet.getMember(i);
					currentDone |= (thisMesh == currentMesh);
					InterfaceProgressBar progress = new InterfaceProgressBar("Inflating surface:");
					meshEngine.inflateMesh(thisMesh, method, progress);
					}
				}
			
			if (chkSel.isSelected() && currentMesh != null && !currentDone){
				InterfaceProgressBar progress = new InterfaceProgressBar("Inflating surface:");
				meshEngine.inflateMesh(currentMesh, method, progress);
				}
			
			}
		
		
		if (e.getActionCommand().startsWith("Validate")){
			
			if (e.getActionCommand().endsWith("Apply")){
				if (currentMesh == null) return;
				String op = (String)cmbValidateOp.getSelectedItem();
				if (op == null) return;
				
				if (op.equals("Self-intersection")){
					
					Mesh3D mesh = currentMesh.getMesh();
					InterfaceProgressBar progress = new InterfaceProgressBar("Checking mesh:");
					progress.setMinimum(0);
					progress.register();
					float search_max = MeshFunctions.getMaximumEdgeLength(mesh);
					ArrayList<Integer> faces = MeshFunctions.getSelfIntersections(mesh, search_max, progress);
					progress.deregister();
					if (faces.size() == 0){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "No self intersections.", 
													  "Mesh self-intersections", 
													  JOptionPane.INFORMATION_MESSAGE);
						return;
						}
					TreeSet<Integer> nodes = new TreeSet<Integer>();
					for (int i = 0; i < faces.size(); i++){
						MeshFace3D face = mesh.getFace(faces.get(i));
						nodes.add(face.A);
						nodes.add(face.B);
						nodes.add(face.C);
						}
					currentMesh.setSelectedVertices(new ArrayList<Integer>(nodes));
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  nodes.size() + " self intersection nodes.", 
							  "Mesh self-intersections", 
							  JOptionPane.INFORMATION_MESSAGE);
					return;
					}
				
				if (op.equals("Mesh-intersection")){
					
					Mesh3DInt meshint2 = (Mesh3DInt)cmbValidateMesh2.getSelectedItem();
					Mesh3D mesh = currentMesh.getMesh();
					Mesh3D mesh2 = meshint2.getMesh();
					
					InterfaceProgressBar progress = new InterfaceProgressBar("Comparing meshes:");
					progress.setMinimum(0);
					progress.register();
					float search_max = Math.max(MeshFunctions.getMaximumEdgeLength(mesh),
												MeshFunctions.getMaximumEdgeLength(mesh2));
					ArrayList<ArrayList<Integer>> all_faces = MeshFunctions.getIntersectingFaces(mesh, mesh2, search_max, progress);
					progress.deregister();
					
					ArrayList<Integer> faces_1 = all_faces.get(0);
					ArrayList<Integer> faces_2 = all_faces.get(1);
					
					if (faces_1.size() == 0){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "No mesh-mesh intersections.", 
													  "Mesh-mesh intersections", 
													  JOptionPane.INFORMATION_MESSAGE);
						return;
						}
					TreeSet<Integer> nodes = new TreeSet<Integer>();
					for (int i = 0; i < faces_1.size(); i++){
						MeshFace3D face = mesh.getFace(faces_1.get(i));
						nodes.add(face.A);
						nodes.add(face.B);
						nodes.add(face.C);
						}
					currentMesh.setSelectedVertices(new ArrayList<Integer>(nodes));
					
					nodes = new TreeSet<Integer>();
					for (int i = 0; i < faces_2.size(); i++){
						MeshFace3D face = mesh2.getFace(faces_2.get(i));
						nodes.add(face.A);
						nodes.add(face.B);
						nodes.add(face.C);
						}
					meshint2.setSelectedVertices(new ArrayList<Integer>(nodes));
					
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  nodes.size() + " intersection nodes.", 
							  "Mesh-mesh intersections", 
							  JOptionPane.INFORMATION_MESSAGE);
					}
				
				return;
				}
			
			}
		
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if (e.getSource().equals(cmbValidateOp)){
			
			if (e.getStateChange() == ItemEvent.SELECTED){
				updateValidatePanel();
				return;
				}
			
			}
		
	}
	
	protected void initValidatePanels(){
		
		String current_op = (String)cmbValidateOp.getSelectedItem();
		cmbValidateOp.removeAllItems();
		
		this.validate_panels.clear();
		
		// Self-intersection
		SubPanel panel = new SubPanel();
		panel.setLayout(new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 200));
		panel.setLineCount(0);
		validate_panels.put("Self-intersection", panel);
		cmbValidateOp.addItem("Self-intersection");
		
		// Mesh-mesh intersection
		panel = new SubPanel();
		panel.setLayout(new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 200));
		JLabel lblMesh = new JLabel("Second mesh:");
		cmbValidateMesh2.removeAllItems();
		ShapeSet3DInt meshes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
		for (int i = 0; i < meshes.members.size(); i++){
			if (meshes.members.get(i) != this.currentMesh)
				cmbValidateMesh2.addItem(meshes.members.get(i));
			}
		if (cmbValidateMesh2.getItemCount() > 0)
			cmbValidateMesh2.setSelectedIndex(0);
		LineLayoutConstraints c = new LineLayoutConstraints(0, 0, 0, 0.3, 1);
		panel.add(lblMesh, c);
		c = new LineLayoutConstraints(0, 0, 0.3, 0.7, 1);
		panel.add(cmbValidateMesh2, c);
		panel.setLineCount(1);
				
		validate_panels.put("Mesh-intersection", panel);
		cmbValidateOp.addItem("Mesh-intersection");
		
		if (current_op != null)
			cmbValidateOp.setSelectedItem(current_op);
	}
	
	protected void updateValidatePanel(){
		
		String selected = (String)cmbValidateOp.getSelectedItem();
		if (current_validate_panel != null)
			this.remove(current_validate_panel);
		current_validate_panel = validate_panels.get(selected);
		
		//System.out.println("Validate: " + selected);
		
		int size = current_validate_panel.getLineCount() - 1;
		if (size < 0) size = 0;
		
		//System.out.println("Size: " + size);
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints("VALIDATE", 2, size + 2, 0.05, 0.9, 1);
		add(current_validate_panel, c);
		
		remove(cmdValidate);
		c = new CategoryLayoutConstraints("VALIDATE", size + 3, size + 3, 0.15, 0.7, 1);
		add(cmdValidate, c);
		
		this.updateUI();
		
	}
	
	@Override
	public String toString(){
		return "Mesh Functions Panel";
	}
	
	class WindowList extends JPanel {
		public JTable table;
		public WindowListModel model;
		public JScrollPane scrollPane;
		
		public WindowList(int rows, int cols){
			model = new WindowListModel();
			model.setRowCount(rows);
			model.setColCount(cols);
			table = new JTable(model);
			scrollPane = new JScrollPane(table);
			setLayout(new GridLayout(1, 1));
			add(scrollPane);
			//updateEditors();
		}
		
		public void setFromList(WindowList w){
			scrollPane = w.scrollPane;
			table = w.table;
			model = w.model;
			removeAll();
			setLayout(new GridLayout(1, 1));
			add(scrollPane);
		}
		
		public void updateDisplay(){
			table.getTableHeader().updateUI();
			table.updateUI();
		}
		
		public void addWindow(String name, String type){
			model.addRow(name, type);
			//updateEditors();
		}
		
		public void removeWindow(int row){
			model.removeRow(row);
		}
		
		public void moveUp(int row){
			
		}
		
		public void moveDown(int row){
			
		}
		
		public void updateEditors(int col, ArrayList<String> list){
			TableColumn c = table.getColumnModel().getColumn(col);
			JComboBox types = new JComboBox();
			for (int i = 0; i < list.size(); i++)
				types.addItem(list.get(i));
			c.setCellEditor(new DefaultCellEditor(types));
		}
		
	}
	
	class SubPanel extends JPanel {
		
		int no_lines = 0;
		
		public int getLineCount(){
			return no_lines;
		}
		
		public void setLineCount(int n){
			this.no_lines = n;
		}
		
		
	}
	
	class WindowListModel extends DefaultTableModel{
	
		DefaultTableModel m = new DefaultTableModel();
		ArrayList<ArrayList> rowList = new ArrayList<ArrayList>();
		ArrayList<String> columnNames = new ArrayList<String>();
		int rows;
		int cols;
		
		public WindowListModel(){
			
		}
		
		public void setValueAt(Object val, int row, int col){
			rowList.get(row).set(col, val);
		}
		
		public Object getValueAt(int row, int col){
			return rowList.get(row).get(col);
		}
		
		public int getRowCount(){
			return rows;
		}
		
		public int getColumnCount(){
			return cols;
		}
		
		public void setRowCount(int r){
			rows = r;
			while (r > rowList.size())
				rowList.add(new ArrayList());
			while (r < rowList.size())
				rowList.remove(rowList.size() - 1);
		}
		
		public void setColCount(int c){
			cols = c;
			for (int i = 0; i < rows; i++){
				while (c > rowList.get(i).size())
					rowList.get(i).add(new String("."));
				while (c > columnNames.size())
					columnNames.add(new String("."));
				while (c < rowList.get(i).size())
					rowList.get(i).remove(rowList.get(i).size() - 1);
				while (c < columnNames.size())
					columnNames.remove(columnNames.size() - 1);
				}
		}
		
		public void addRow(String name, String type){
			ArrayList<String> thisList = new ArrayList<String>(2);
			thisList.add(type);
			thisList.add(name);
			rowList.add(thisList);
			rows++;
			this.fireTableRowsInserted(rows - 1, rows - 1);
		}
		
		public void removeRow(int row){
			rowList.remove(row);
			rows--;
			this.fireTableRowsDeleted(row, row);
		}
		
		public void setColumnName(int c, String name){
			columnNames.set(c, name);
			this.fireTableStructureChanged();
		}
		
		public String getColumnName(int c){
			return columnNames.get(c);
		}
		
		public Class getColumnClass(int c){
			return String.class;
		}
		
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		
	}
	
}