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

package mgui.interfaces.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector2d;
import org.jogamp.vecmath.Vector3d;

import org.apache.commons.collections15.Transformer;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.InterfaceTabbedDisplayPanel;
import mgui.interfaces.TabbedDisplayEvent;
import mgui.interfaces.TabbedDisplayListener;
import mgui.interfaces.events.CameraEvent;
import mgui.interfaces.graphics.video.SetView3DTask;
import mgui.interfaces.graphics.video.Video;
import mgui.interfaces.graphics.video.Video3D;
import mgui.interfaces.graphics.video.VideoEvent;
import mgui.interfaces.graphics.video.VideoException;
import mgui.interfaces.graphics.video.VideoListener;
import mgui.interfaces.graphics.video.VideoTask;
import mgui.interfaces.graphics.video.VideoTaskDialog;
import mgui.interfaces.graphics.video.VideoTaskOptions;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Camera3DListener;
import mgui.interfaces.maps.Map3D;
import mgui.interfaces.xml.XMLException;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.domestic.videos.ImageStackVideoDialog;
import mgui.io.domestic.videos.ImageStackVideoOptions;
import mgui.io.domestic.videos.ImageStackVideoWriter;
import mgui.io.domestic.videos.VideoOutputDialog;
import mgui.io.domestic.videos.VideoOutputOptions;
import mgui.io.domestic.videos.VideoXMLLoader;
import mgui.numbers.MguiDouble;
import mgui.resources.icons.IconObject;
import mgui.util.TimeFunctions;


/***********************
 * Panel to define the display windows in InterfaceDisplayPanel, including:
 * <ul>
 * <li>1. Number of windows
 * <li>2. Order of windows
 * <li>3. Type of windows (instances of InterfaceGraphic)
 * <li>4. Source objects - depending on window type
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class InterfaceGraphicPanel extends InterfacePanel implements ActionListener, 
																	 Camera3DListener,
																	 VideoListener,
																	 TabbedDisplayListener{
	
	//displayable objects tree nodes
	private ArrayList<DefaultMutableTreeNode> displayNodes = new ArrayList<DefaultMutableTreeNode>();
	private InterfacePanel currentSourceWindow;
	private ArrayList<InterfaceGraphic<?>> sourceTypes = InterfaceGraphic.getSourceTypes();
	private int[] windowNew = new int[6];
	
	//action commands
	public final String CMD_ADD_WIN = "Add Window";
	public final String CMD_REM_WIN = "Remove Window";
	public final String CMD_UP_WIN = "Move Up";
	public final String CMD_DOWN_WIN = "Move Down";
	public final String CMD_APPLY_WIN = "Apply Windows";
	public final String CMD_SET_SOURCE = "Set Source";
	public final String CMD_REF_SOURCE = "Refresh Source";
	public final String CMB_SOURCE_WIN = "Source Window";
	
	//defaults
	private String newWindowName = "NoName";
	private String newWindowType = "Graphic2D";
	
	//events
	private mouseAdapter ma = new mouseAdapter();
	
	//because the combo box action event is strange
	boolean handleCombo = true;

	//windows list as an attributes table with order, window type, label, and source
	CategoryTitle lblWindows = new CategoryTitle("WINDOWS");
	JLabel lblWindowList = new JLabel("Current Windows:");
	//WindowList lstWindows;
	WindowListModel window_list_model;
	JTable window_list;
	JScrollPane scrWindowList;
	
	JLabel lblWindowCount = new JLabel("Count: ");
	JTextField txtWindowCount = new JTextField();
	JButton cmdAddWindow = new JButton("Add");
	JButton cmdRemoveWindow = new JButton("Rem");
	JLabel lblOrder = new JLabel("Order:");
	JButton cmdMoveUp = new JButton("Up");
	JButton cmdMoveDown = new JButton("Down");
	JButton cmdApplyWindows = new JButton("Apply");
	JButton cmdResetWindows = new JButton("Reset");
	
	//tree of possible source objects
	//to populate, use an isDisplayable(Object o) function in InterfaceGraphic?
	CategoryTitle lblSources = new CategoryTitle("SOURCES");
	JLabel lblSourceWindow = new JLabel("Window:");
	//JComboBox cmbSourceWindow = new JComboBox();
	InterfaceComboBox cmbSourceWindow = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  true, 500);
	
	JLabel lblSourceTree = new JLabel("Possible sources:");
	JTree treeSources;
	JPanel treeSourcePanel = new JPanel();
	JScrollPane treeSourceScrollPane = new JScrollPane();
	DefaultMutableTreeNode rootNode; // = new DefaultMutableTreeNode("Sources");
	JButton cmdSetSource = new JButton("Set");
	JButton cmdRefreshSource = new JButton("Refresh");
	
	//set selected window to selected source
	JButton cmdSetWindowSource = new JButton("Set Source");
	JButton cmdRefreshSourceList = new JButton("Refresh");
	
	CategoryTitle lblViews3D = new CategoryTitle("VIEWS (3D)");
	JCheckBox chkViews3DWindow = new JCheckBox(" Window view:");
	//JComboBox cmbViews3DWindow = new JComboBox();
	InterfaceComboBox cmbViews3DWindow = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  												   true, 500);
	JCheckBox chkViews3DNamed = new JCheckBox(" Named view:");
	//JComboBox cmbViews3DNamed = new JComboBox();
	InterfaceComboBox cmbViews3DNamed = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  												  true, 500);
	JLabel lblViews3DName = new JLabel("Name:");
	JTextField txtViews3DName = new JTextField("-");
	JLabel lblViews3DLOS = new JLabel("Line of sight:");
	JTextField txtViews3DLOS_x = new JTextField("0.0");
	JTextField txtViews3DLOS_y = new JTextField("0.0");
	JTextField txtViews3DLOS_z = new JTextField("0.0");
	JButton cmdViews3DLOS_flip = new JButton("Flip");
	JCheckBox chkViews3DZoom = new JCheckBox(" Zoom distance:");
	JSlider sldViews3DZoom = new JSlider();
	JTextField txtViews3DZoom = new JTextField("0.0");
	JLabel lblViews3DUp = new JLabel("Up vector:");
	JTextField txtViews3DUp_x = new JTextField("0.0");
	JTextField txtViews3DUp_y = new JTextField("0.0");
	JTextField txtViews3DUp_z = new JTextField("0.0");
	JButton cmdViews3DUp_flip = new JButton("Flip");
	JSlider sldViews3DUp = new JSlider();
	JCheckBox chkViews3DTarget = new JCheckBox(" Target point:");
	JTextField txtViews3DTarget_x = new JTextField("0.0");
	JTextField txtViews3DTarget_y = new JTextField("0.0");
	JTextField txtViews3DTarget_z = new JTextField("0.0");
	JLabel lblViews3DTranslation = new JLabel("Translation (x,y):");
	JTextField txtViews3DTranslation_x = new JTextField("0.0");
	JTextField txtViews3DTranslation_y = new JTextField("0.0");
	JButton cmdViews3DTranslation_zero = new JButton("Zero");
	JButton cmdViews3DSave = new JButton("Save View");
	JButton cmdViews3DDelete = new JButton("Delete View");
	JLabel lblViews3DSet = new JLabel("Set view for:");
	JCheckBox chkViews3DSetAll = new JCheckBox(" All windows");
	JCheckBox chkViews3DSetSelected = new JCheckBox(" Selected windows:");
	JList lstViews3DWindows;
	JScrollPane scrViews3DSetSelected;
	JCheckBox chkViews3DSetImmediate = new JCheckBox(" Apply immediately");
	JButton cmdViews3DSet = new JButton("Apply");
	
	CategoryTitle lblVideo3D = new CategoryTitle("VIDEO (3D)");
	JLabel lblVideo3DWindow = new JLabel("Window:");
	//JComboBox cmbVideo3DWindow = new JComboBox();
	InterfaceComboBox cmbVideo3DWindow = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  true, 500);
	
	JLabel lblVideo3DVideo = new JLabel("Video:");
	JComboBox cmbVideo3DVideo = new JComboBox();
	JLabel lblVideo3DName = new JLabel("Name:");
	JTextField txtVideo3DName = new JTextField("-");
	JLabel lblVideo3DRefresh = new JLabel("Refresh rate (ms):");
	JTextField txtVideo3DRefresh = new JTextField("10");
	JLabel lblVideo3DDuration = new JLabel("Duration:");
	JTextField txtVideo3DDuration = new JTextField("60");
	JButton cmdVideo3DSave = new JButton("Save");
	JButton cmdVideo3DDelete = new JButton("Delete");
	JButton cmdVideo3DNew = new JButton("New");
	JButton cmdVideo3DLoad = new JButton("Load");
	JButton cmdVideo3DUpdate = new JButton("Update");
	
	JLabel lblVideo3DTask = new JLabel("TASK LIST");
	JTable lstVideo3DTasks;
	JScrollPane scrVideo3DTasks;
	VideoTaskListModel video3DTaskModel;
	JButton cmdVideo3DEditTask = new JButton("Edit task");
	JButton cmdVideo3DOffsetTask = new JButton("Offset task(s)");
	JButton cmdVideo3DNewTask = new JButton("New task");
	JButton cmdVideo3DRemoveTask = new JButton("Remove task");
	
	JLabel lblVideo3DControls = new JLabel("CONTROLS");
	JButton cmdVideo3DStart = new JButton("|<");
	JButton cmdVideo3DReverse = new JButton("<");
	JButton cmdVideo3DStop = new JButton("||");
	JButton cmdVideo3DForward = new JButton(">");
	JButton cmdVideo3DEnd = new JButton(">|");
	
	JLabel lblVideo3DClock = new JLabel("Clock:");
	JTextField txtVideo3DClock = new JTextField("00:00:00");
	JCheckBox chkVideo3DLoop = new JCheckBox(" Loop");
	JSlider sldVideoClock;
	
	//TODO recording stuff
	JLabel lblVideo3DOutput = new JLabel("OUTPUT");
	
	JLabel lblVideo3DOutputFormat = new JLabel("Format:");
	JComboBox cmbVideo3DOutputFormat = new JComboBox();
	JLabel lblVideo3DOutputFile = new JLabel("File/Folder:");
	JTextField txtVideo3DOutputFile = new JTextField("");
	JButton cmdVideo3DOutputFile = new JButton("Browse..");
	JButton cmdVideo3DOutputOptions = new JButton("Options..");
	JButton cmdVideo3DOutput = new JButton("Write");
	
	View3D currentView;
	Video3D currentVideo3D;
	VideoTaskOptions videoTaskOptions;
	VideoOutputOptions video3DOutputOptions;
	
	boolean doUpdate = true;
	
	public InterfaceGraphicPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	public void showPanel(){
		
		updateDisplay();
		
	}
	
	
	
	protected void init(){
		
		type = "Interface Graphic Panel";
		
		initWindowList();
		//initViews3D();
		initVideo3D();
		
		//sources tree
		rootNode = new DefaultMutableTreeNode("Sources");
		rootNode.add(getDisplayObjectsNode());
		treeSources = new JTree(new DefaultTreeModel(rootNode));
		treeSourceScrollPane = new JScrollPane(treeSources);
		
		//buttons
		cmdAddWindow.addActionListener(this);
		cmdAddWindow.setActionCommand(CMD_ADD_WIN);
		cmdRemoveWindow.addActionListener(this);
		cmdRemoveWindow.setActionCommand(CMD_REM_WIN);
		cmdMoveUp.addActionListener(this);
		cmdMoveUp.setActionCommand(CMD_UP_WIN);
		cmdMoveDown.addActionListener(this);
		cmdMoveDown.setActionCommand(CMD_DOWN_WIN);
		cmdApplyWindows.addActionListener(this);
		cmdApplyWindows.setActionCommand(CMD_APPLY_WIN);
		cmdResetWindows.addActionListener(this);
		cmdResetWindows.setActionCommand("Reset Windows");
		cmdSetSource.addActionListener(this);
		cmdSetSource.setActionCommand(CMD_SET_SOURCE);
		cmdRefreshSource.addActionListener(this);
		cmdRefreshSource.setActionCommand(CMD_REF_SOURCE);
		cmdViews3DSave.addActionListener(this);
		cmdViews3DSave.setActionCommand("Views3D Save");
		cmdViews3DDelete.addActionListener(this);
		cmdViews3DDelete.setActionCommand("Views3D Delete");
		cmdViews3DSet.addActionListener(this);
		cmdViews3DSet.setActionCommand("Views3D Set");
		cmdViews3DTranslation_zero.addActionListener(this);
		cmdViews3DTranslation_zero.setActionCommand("Views3D Translate Zero");
		cmdViews3DLOS_flip.addActionListener(this);
		cmdViews3DLOS_flip.setActionCommand("Views3D Flip LOS");
		cmdViews3DUp_flip.addActionListener(this);
		cmdViews3DUp_flip.setActionCommand("Views3D Flip Up");
		cmdVideo3DStart.addActionListener(this);
		cmdVideo3DStart.setActionCommand("Video3D Start");
		cmdVideo3DForward.addActionListener(this);
		cmdVideo3DForward.setActionCommand("Video3D Forward");
		cmdVideo3DStop.addActionListener(this);
		cmdVideo3DStop.setActionCommand("Video3D Stop");
		cmdVideo3DNewTask.addActionListener(this);
		cmdVideo3DNewTask.setActionCommand("Video3D New Task");
		cmdVideo3DEditTask.addActionListener(this);
		cmdVideo3DEditTask.setActionCommand("Video3D Edit Task");
		cmdVideo3DRemoveTask.addActionListener(this);
		cmdVideo3DRemoveTask.setActionCommand("Video3D Remove Task");
		cmdVideo3DOffsetTask.addActionListener(this);
		cmdVideo3DOffsetTask.setActionCommand("Video3D Offset Tasks");
		cmdVideo3DDelete.addActionListener(this);
		cmdVideo3DDelete.setActionCommand("Video3D Delete Video");
		cmdVideo3DNew.addActionListener(this);
		cmdVideo3DNew.setActionCommand("Video3D New Video");
		cmdVideo3DSave.addActionListener(this);
		cmdVideo3DSave.setActionCommand("Video3D Save Video");
		cmdVideo3DLoad.addActionListener(this);
		cmdVideo3DLoad.setActionCommand("Video3D Load Video");
		cmdVideo3DUpdate.addActionListener(this);
		cmdVideo3DUpdate.setActionCommand("Video3D Update Video");
		cmdVideo3DOutputOptions.addActionListener(this);
		cmdVideo3DOutputOptions.setActionCommand("Video3D Output Options");
		cmdVideo3DOutputFile.addActionListener(this);
		cmdVideo3DOutputFile.setActionCommand("Video3D Output File");
		cmdVideo3DOutput.addActionListener(this);
		cmdVideo3DOutput.setActionCommand("Video3D Output");
		
		//combo box
		cmbSourceWindow.addActionListener(this);
		cmbSourceWindow.setActionCommand(CMB_SOURCE_WIN);
		cmbViews3DWindow.addActionListener(this);
		cmbViews3DWindow.setActionCommand("Views3D Window Changed");
		cmbViews3DNamed.addActionListener(this);
		cmbViews3DNamed.setActionCommand("Views3D Named Changed");
		cmbVideo3DVideo.addActionListener(this);
		cmbVideo3DVideo.setActionCommand("Video3D Video Changed");
		
		chkViews3DWindow.setSelected(true);
		chkViews3DWindow.addActionListener(this);
		chkViews3DWindow.setActionCommand("Views3D Windows");
		chkViews3DNamed.addActionListener(this);
		chkViews3DNamed.setActionCommand("Views3D Named");
		chkViews3DZoom.addActionListener(this);
		chkViews3DZoom.setActionCommand("Views3D Zoom");
		chkViews3DSetImmediate.addActionListener(this);
		chkViews3DSetImmediate.setActionCommand("Views3D Set Immediate");
		chkViews3DSetAll.setSelected(true);
		chkViews3DSetAll.addActionListener(this);
		chkViews3DSetAll.setActionCommand("Views3D Set All");
		chkViews3DSetSelected.addActionListener(this);
		chkViews3DSetSelected.setActionCommand("Views3D Set Selected");
		chkViews3DTarget.addActionListener(this);
		chkViews3DTarget.setActionCommand("Views3D Target");
		txtViews3DName.addActionListener(this);
		txtViews3DName.setActionCommand("Views3D Name Changed");
		
		//alignment
		lblVideo3DOutput.setHorizontalAlignment(SwingConstants.CENTER);
		lblVideo3DTask.setHorizontalAlignment(SwingConstants.CENTER);
		lblVideo3DControls.setHorizontalAlignment(SwingConstants.CENTER);
		
		//set up other stuff
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblWindows, c);
		lblWindows.setParentObj(this);
		c = new CategoryLayoutConstraints("WINDOWS", 1, 1, 0.05, 0.9, 1);
		add(lblWindowList, c);
		c = new CategoryLayoutConstraints("WINDOWS", 2, 6, 0.05, 0.9, 1);
		add(scrWindowList, c);
		c = new CategoryLayoutConstraints("WINDOWS", 7, 7, 0.05, .24, 1);
		add(lblWindowCount, c);
		c = new CategoryLayoutConstraints("WINDOWS", 7, 7, .30, .32, 1);
		add(cmdAddWindow, c);
		c = new CategoryLayoutConstraints("WINDOWS", 7, 7, .63, .32, 1);
		add(cmdRemoveWindow, c);
		c = new CategoryLayoutConstraints("WINDOWS", 8, 8, 0.05, .24, 1);
		add(lblOrder, c);
		c = new CategoryLayoutConstraints("WINDOWS", 8, 8, .3, .32, 1);
		add(cmdMoveUp, c);
		c = new CategoryLayoutConstraints("WINDOWS", 8, 8, .63, .32, 1);
		add(cmdMoveDown, c);
		c = new CategoryLayoutConstraints("WINDOWS", 9, 10, 0.05, .43, 1);
		add(cmdResetWindows, c);
		c = new CategoryLayoutConstraints("WINDOWS", 9, 10, .52, .43, 1);
		add(cmdApplyWindows, c);
		
		c = new CategoryLayoutConstraints();
		add(lblSources, c);
		lblSources.setParentObj(this);
		c = new CategoryLayoutConstraints("SOURCES", 1, 1, 0.05, 0.9, 1);
		add(lblSourceWindow, c);
		c = new CategoryLayoutConstraints("SOURCES", 2, 2, 0.05, 0.9, 1);
		add(cmbSourceWindow, c);
		c = new CategoryLayoutConstraints("SOURCES", 3, 3, 0.05, 0.9, 1);
		add(lblSourceTree, c);
		c = new CategoryLayoutConstraints("SOURCES", 4, 11, 0.05, 0.9, 1);
		add(treeSourceScrollPane, c);
		c = new CategoryLayoutConstraints("SOURCES", 12, 13, 0.05, .43, 1);
		add(cmdSetSource, c);
		c = new CategoryLayoutConstraints("SOURCES", 12, 13, .52, .43, 1);
		add(cmdRefreshSource, c);
		
		c = new CategoryLayoutConstraints();
		add(lblViews3D, c);
		lblViews3D.setParentObj(this);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 1, 1, 0.05, 0.9, 1);
		add(chkViews3DWindow, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 2, 2, 0.1, 0.85, 1);
		add(cmbViews3DWindow, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 3, 3, 0.05, 0.9, 1);
		add(chkViews3DNamed, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 4, 4, 0.1, 0.85, 1);
		add(cmbViews3DNamed, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 5, 5, 0.05, 0.2, 1);
		add(lblViews3DName, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 5, 5, 0.25, 0.7, 1);
		add(txtViews3DName, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 6, 6, 0.05, 0.62, 1);
		add(lblViews3DLOS, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 6, 6, 0.68, 0.27, 1);
		add(cmdViews3DLOS_flip, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 7, 7, 0.1, 0.27, 1);
		add(txtViews3DLOS_x, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 7, 7, 0.39, 0.27, 1);
		add(txtViews3DLOS_y, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 7, 7, 0.68, 0.27, 1);
		add(txtViews3DLOS_z, c);
		
		c = new CategoryLayoutConstraints("VIEWS (3D)", 8, 8, 0.05, 0.62, 1);
		add(chkViews3DZoom, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 8, 8, 0.68, 0.27, 1);
		add(txtViews3DZoom, c);
		
		//TODO: add slider
		
		c = new CategoryLayoutConstraints("VIEWS (3D)", 9, 9, 0.05, 0.62, 1);
		add(lblViews3DUp, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 9, 9, 0.68, 0.27, 1);
		add(cmdViews3DUp_flip, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 10, 10, 0.1, 0.27, 1);
		add(txtViews3DUp_x, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 10, 10, 0.39, 0.27, 1);
		add(txtViews3DUp_y, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 10, 10, 0.68, 0.27, 1);
		add(txtViews3DUp_z, c);
		
		//TODO: add slider
		
		c = new CategoryLayoutConstraints("VIEWS (3D)", 11, 11, 0.05, 0.9, 1);
		add(chkViews3DTarget, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 12, 12, 0.1, 0.27, 1);
		add(txtViews3DTarget_x, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 12, 12, 0.39, 0.27, 1);
		add(txtViews3DTarget_y, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 12, 12, 0.68, 0.27, 1);
		add(txtViews3DTarget_z, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 13, 13, 0.05, 0.9, 1);
		add(lblViews3DTranslation, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 14, 14, 0.1, 0.27, 1);
		add(txtViews3DTranslation_x, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 14, 14, 0.39, 0.27, 1);
		add(txtViews3DTranslation_y, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 14, 14, 0.68, 0.27, 1);
		add(cmdViews3DTranslation_zero, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 15, 16, 0.05, 0.44, 1);
		add(cmdViews3DSave, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 15, 16, 0.51, 0.44, 1);
		add(cmdViews3DDelete, c);
		
		c = new CategoryLayoutConstraints("VIEWS (3D)", 17, 17, 0.05, 0.9, 1);
		add(lblViews3DSet, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 18, 18, 0.1, 0.85, 1);
		add(chkViews3DSetAll, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 19, 19, 0.1, 0.85, 1);
		add(chkViews3DSetSelected, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 20, 25, 0.1, 0.85, 1);
		add(scrViews3DSetSelected, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 26, 26, 0.05, 0.9, 1);
		add(chkViews3DSetImmediate, c);
		c = new CategoryLayoutConstraints("VIEWS (3D)", 27, 28, 0.28, 0.44, 1);
		add(cmdViews3DSet, c);
		
		c = new CategoryLayoutConstraints();
		add(lblVideo3D, c);
		lblVideo3D.setParentObj(this);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 1, 1, 0.05, 0.25, 1);
		add(lblVideo3DWindow, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 1, 1, 0.3, 0.65, 1);
		add(cmbVideo3DWindow, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 2, 2, 0.05, 0.25, 1);
		add(lblVideo3DVideo, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 2, 2, 0.3, 0.65, 1);
		add(cmbVideo3DVideo, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 3, 3, 0.05, 0.35, 1);
		add(lblVideo3DName, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 3, 3, 0.4, 0.55, 1);
		add(txtVideo3DName, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 4, 4, 0.05, 0.35, 1);
		add(lblVideo3DDuration, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 4, 4, 0.4, 0.55, 1);
		add(txtVideo3DDuration, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 5, 5, 0.05, 0.35, 1);
		add(lblVideo3DRefresh, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 5, 5, 0.4, 0.55, 1);
		add(txtVideo3DRefresh, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 6, 6, 0.05, 0.44, 1);
		add(cmdVideo3DNew, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 6, 6, 0.51, 0.44, 1);
		add(cmdVideo3DDelete, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 7, 7, 0.05, 0.44, 1);
		add(cmdVideo3DSave, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 7, 7, 0.51, 0.44, 1);
		add(cmdVideo3DLoad, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 8, 8, 0.05, 0.44, 1);
		add(cmdVideo3DUpdate, c);
		
		c = new CategoryLayoutConstraints("VIDEO (3D)", 10, 10, 0.05, 0.9, 1);
		add(lblVideo3DTask, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 11, 17, 0.05, 0.9, 1);
		add(scrVideo3DTasks, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 18, 18, 0.05, 0.44, 1);
		add(cmdVideo3DEditTask, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 18, 18, 0.51, 0.44, 1);
		add(cmdVideo3DOffsetTask, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 19, 19, 0.05, 0.44, 1);
		add(cmdVideo3DNewTask, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 19, 19, 0.51, 0.44, 1);
		add(cmdVideo3DRemoveTask, c);
		
		c = new CategoryLayoutConstraints("VIDEO (3D)", 21, 21, 0.05, 0.9, 1);
		add(lblVideo3DControls, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 22, 22, 0.05, 0.29, 1);
		cmdVideo3DStart.setToolTipText("To start");
		add(cmdVideo3DStart, c);
//		c = new CategoryLayoutConstraints("VIDEO (3D)", 22, 22, 0.23, 0.18, 1);
//		cmdVideo3DReverse.setToolTipText("Reverse");
//		add(cmdVideo3DReverse, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 22, 22, 0.36, 0.29, 1);
		cmdVideo3DStop.setToolTipText("Stop");
		add(cmdVideo3DStop, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 22, 22, 0.66, 0.29, 1);
		cmdVideo3DForward.setToolTipText("Forward");
		add(cmdVideo3DForward, c);
//		c = new CategoryLayoutConstraints("VIDEO (3D)", 22, 22, 0.77, 0.18, 1);
//		cmdVideo3DEnd.setToolTipText("To end");
//		add(cmdVideo3DEnd, c);
		
		txtVideo3DClock.setHorizontalAlignment(JTextField.CENTER);
		txtVideo3DClock.setFont(new Font("Arial", Font.BOLD, 22));
		txtVideo3DClock.setEditable(false);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 23, 24, 0.05, 0.9, 1);
		add(txtVideo3DClock, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 25, 25, 0.05, 0.9, 1);
		add(chkVideo3DLoop, c);
		
		c = new CategoryLayoutConstraints("VIDEO (3D)", 27, 27, 0.05, 0.9, 1);
		add(lblVideo3DOutput, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 28, 28, 0.05, 0.25, 1);
		add(lblVideo3DOutputFormat, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 28, 28, 0.3, 0.65, 1);
		add(cmbVideo3DOutputFormat, c);
		cmbVideo3DOutputFormat.addItem("Image stack (png)");
		c = new CategoryLayoutConstraints("VIDEO (3D)", 29, 29, 0.05, 0.25, 1);
		add(lblVideo3DOutputFile, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 29, 29, 0.3, 0.65, 1);
		add(txtVideo3DOutputFile, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 30, 30, 0.05, 0.44, 1);
		add(cmdVideo3DOutputFile, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 30, 30, 0.51, 0.44, 1);
		add(cmdVideo3DOutputOptions, c);
		c = new CategoryLayoutConstraints("VIDEO (3D)", 31, 32, 0.28, 0.44, 1);
		add(cmdVideo3DOutput, c);
		
		updateDisplay();
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceGraphicPanel.class.getResource("/mgui/resources/icons/window_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/window_20.png");
		return null;
	}
	
	private void initVideo3D(){
		video3DTaskModel = new VideoTaskListModel(new ArrayList<VideoTask>());
		lstVideo3DTasks = new JTable(video3DTaskModel);
		lstVideo3DTasks.setDefaultRenderer(Object.class, new VideoTableRenderer());
		scrVideo3DTasks = new JScrollPane(lstVideo3DTasks);
		
	}
	
	private void initWindowList(){
		//	populate window list from display panel
		InterfaceDisplayPanel display_panel = InterfaceSession.getDisplayPanel();
		ArrayList<InterfaceGraphicWindow> windows = InterfaceSession.getDisplayPanel().getWindowsDepthFirst();
		
		lblWindowList.setText("Current windows:");
		if (display_panel instanceof InterfaceTabbedDisplayPanel){
			InterfaceDisplayPanel cdp = ((InterfaceTabbedDisplayPanel)display_panel).getCurrentPanel();
			if (cdp != null)
				lblWindowList.setText("Current windows for '" + cdp.getName() + "':");
			}
		
		if (window_list_model == null){
			window_list_model = new WindowListModel();
			window_list = new JTable(window_list_model);
			
			TableColumn column = window_list.getColumnModel().getColumn(0);
			column.setCellEditor(new GraphicWindowListEditor());
			column.setCellRenderer(new GraphicWindowListRenderer());
			
			scrWindowList = new JScrollPane(window_list);
			
			window_list_model.setPanelsFromWindows(windows);
			
		}else{
			
			window_list_model.setPanelsFromWindows(windows);
			
			}
		
		/*
		if (panels == null){
			currentSourceWindow = null;
			if (lstWindows != null){
				lstWindows.removeAll();
				lstWindows.setEnabled(false);
				lstWindows.model.removeAll();
				}
			
		}else{
		
			WindowList w = new WindowList(panels.size(), 2);
			if (lstWindows == null)
				lstWindows = w;
			else
				lstWindows.setFromList(w);
			
			lstWindows.setEnabled(true);
			
			lstWindows.model.setColumnName(0, "Type");
			lstWindows.model.setColumnName(1, "Name");
			lstWindows.updateEditors();
			lstWindows.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lstWindows.addMouseListener(ma);
			
			for (int i = 0; i < 5; i++)
				if (i < panels.size())
					windowNew[i] = i;
				else
					windowNew[i] = -1;
			
			for (int i = 0; i < panels.size(); i++){
				//lstWindows.model.setValueAt(String.valueOf(i), i, 0);
				
				lstWindows.model.setValueAt(panels.get(i).getPanel(), i, 0);
				lstWindows.model.setValueAt(panels.get(i).getPanel().getName(), i, 1);
				}
			
			//lstWindows.updateUI();
			lstWindows.repaint();
			}
		*/
		
		lblWindowCount.setText("Count: " + window_list_model.getRowCount());
		
		//populate source window list and sources tree node
		setSourceWindowList();
		setVideo3DList();
		setDisplayObjectsNodes();
		
		initViews3D();
	}
	
	private void initViews3D(){
		
		cmbViews3DWindow.removeAllItems();
		ArrayList<InterfaceGraphicWindow> panels = InterfaceSession.getDisplayPanel().getWindows();
		
		DefaultListModel model = new DefaultListModel();
		
		if (panels != null){
			for (int i = 0; i < panels.size(); i++)
				if (panels.get(i).getPanel() instanceof InterfaceGraphic3D){
					cmbViews3DWindow.addItem(panels.get(i).getPanel());
					model.addElement(panels.get(i).getPanel());
					}
			}
		
		updateView3DWindow();
		
		//fill window list
		if (lstViews3DWindows == null){
			lstViews3DWindows = new JList(model);
			scrViews3DSetSelected = new JScrollPane(lstViews3DWindows);
		}else{
			lstViews3DWindows.setModel(model);
			}
		
		//TODO preserve selection
		
		//named views
		ArrayList<View3D> views = InterfaceSession.getWorkspace().getViews3D();
		
		View3D current = (View3D)cmbViews3DNamed.getSelectedItem();
		cmbViews3DNamed.removeAllItems();
		boolean current_found = false;
		
		for (int i = 0; i < views.size(); i++){
			if (current != null && views.get(i).equals(current))
				current_found = true;
			cmbViews3DNamed.addItem(views.get(i));
			}
		
		if (current_found) cmbViews3DNamed.setSelectedItem(current);
		
	}
	
	//return a node of all displayable objects
	private DefaultMutableTreeNode getDisplayObjectsNode(){
		if (currentSourceWindow == null) return new DefaultMutableTreeNode("No window");
		for (int i = 0; i < sourceTypes.size(); i++)
			if (sourceTypes.get(i).getClass().isInstance(currentSourceWindow))
				return displayNodes.get(i);
		return new DefaultMutableTreeNode("Bad window class..");
	}
	
	private void setDisplayObjectsNodes(){
		displayNodes = new ArrayList<DefaultMutableTreeNode>();
		if (currentSourceWindow == null) return;
		DefaultMutableTreeNode thisNode;
		
		for (int i = 0; i < sourceTypes.size(); i++){
			//get a list for each type
			thisNode = sourceTypes.get(i).getDisplayObjectsNode();
			displayNodes.add(thisNode);
			}
	}
	
	private void setSourceWindowList(){
		handleCombo = false;
		cmbSourceWindow.removeAllItems();
		InterfaceGraphic3D currentVideo3DWindow = null;
		if (cmbVideo3DWindow.getSelectedItem() != null)
			
		cmbVideo3DWindow.removeAllItems();
		boolean blnFound = false;
		boolean videoFound = false;
		ArrayList<InterfaceGraphicWindow> panels = InterfaceSession.getDisplayPanel().getWindows();
		
		if (panels != null){
			for (int i = 0; i < panels.size(); i++){
				cmbSourceWindow.addItem(panels.get(i));
				cmbVideo3DWindow.addItem(panels.get(i).getPanel());
				if (panels.get(i).equals(currentSourceWindow))
					blnFound = true;
				if (currentVideo3DWindow != null && panels.get(i).equals(currentVideo3DWindow))
					videoFound = true;
				}
			}
		if (blnFound)
			cmbSourceWindow.setSelectedItem(currentSourceWindow);
		else if (cmbSourceWindow.getSelectedItem() != null)
			currentSourceWindow = ((InterfaceGraphicWindow)cmbSourceWindow.getSelectedItem()).getPanel();
		else
			currentSourceWindow = null;
		
		if (videoFound)
			cmbVideo3DWindow.setSelectedItem(currentVideo3DWindow);
		
		handleCombo = true;
	}
	
	private void setVideo3DList(){
		handleCombo = false;
		
		cmbVideo3DVideo.removeAllItems();
		
		boolean videoFound = false;
		ArrayList<Video3D> videos = InterfaceSession.getWorkspace().getVideos3D();
		
		for (int i = 0; i < videos.size(); i++){
			cmbVideo3DVideo.addItem(videos.get(i));
			if (currentVideo3D != null && videos.get(i).equals(currentVideo3D))
				videoFound = true;
			}
		
		if (videoFound)
			cmbVideo3DVideo.setSelectedItem(currentVideo3D);
		
		handleCombo = true;
	}
	
	public String toString(){
		return "Windows Panel";
	}
	
	//TODO throw exception if class not found or class not instance of InterfaceGraphic
	protected InterfaceGraphic getInstance(String name){
		InterfaceGraphic p = InterfaceEnvironment.getInterfaceGraphicInstance(name);
		if (p == null)
			return null;
		return p;	
	}
	
	public void updateDisplay(){
		initWindowList();
		//lstWindows.updateDisplay();
		setDisplayObjectsNodes();
		rootNode.removeAllChildren();
		rootNode.add(getDisplayObjectsNode());
		treeSources.updateUI();
		updateControls();
	}
	
	void updateControls(){
		
		cmbViews3DWindow.setEnabled(chkViews3DWindow.isSelected());
		cmbViews3DNamed.setEnabled(chkViews3DNamed.isSelected());
		txtViews3DZoom.setEnabled(chkViews3DZoom.isSelected());
		
		lstViews3DWindows.setEnabled(chkViews3DSetSelected.isSelected());
		
		txtViews3DTarget_x.setEnabled(chkViews3DTarget.isSelected());
		txtViews3DTarget_y.setEnabled(chkViews3DTarget.isSelected());
		txtViews3DTarget_z.setEnabled(chkViews3DTarget.isSelected());
		
		cmdViews3DSave.setEnabled((!txtViews3DName.getText().equals("-") &&
					  				txtViews3DName.getText().length() > 0));
		
		if (chkViews3DNamed.isSelected())
			cmdViews3DSave.setEnabled(cmdViews3DSave.isEnabled() && 
									  (currentView.isEditable ||
									   !txtViews3DName.getText().equals(currentView.getName())));
		
	}
	
	void updateView3DWindow(){
		
		if (!chkViews3DWindow.isSelected() ||
			cmbViews3DWindow.getSelectedItem() == null){
			if (currentView != null)
				currentView.camera.removeListener(this);
			return;
		}
		
		currentView = new View3D();
		currentView.camera = ((InterfaceGraphic3D)cmbViews3DWindow.getSelectedItem()).getCamera();
		currentView.camera.addListener(this);
		txtViews3DName.setText("-");
		
		updateView3DValues();
		updateControls();
		
	}
	
	void updateView3DNamed(){
		
		if (!chkViews3DNamed.isSelected() ||
			cmbViews3DNamed.getSelectedItem() == null){
			return;
			}
		
		currentView = (View3D)cmbViews3DNamed.getSelectedItem();
		if (currentView == null){
			cmdViews3DSave.setEnabled(false);
			return;
			}
		
		txtViews3DName.setText(currentView.getName());
		updateView3DValues();
		updateControls();
		
	}
	
	void updateView3DValues(){
		//if (true) return;
		if (currentView == null) return;
		Vector3d v = currentView.camera.lineOfSight;
		txtViews3DLOS_x.setText(MguiDouble.getString(v.x, "#0.00000"));
		txtViews3DLOS_y.setText(MguiDouble.getString(v.y, "#0.00000"));
		txtViews3DLOS_z.setText(MguiDouble.getString(v.z, "#0.00000"));
	
		txtViews3DZoom.setText(MguiDouble.getString(currentView.camera.getDistance(), "#0.00000"));
		
		v = new Vector3d(currentView.camera.centerOfRotation);
		
		txtViews3DTarget_x.setText(MguiDouble.getString(v.x, "#0.00000"));
		txtViews3DTarget_y.setText(MguiDouble.getString(v.y, "#0.00000"));
		txtViews3DTarget_z.setText(MguiDouble.getString(v.z, "#0.00000"));
		
		Vector2d v2 = currentView.camera.translateXY;
		
		txtViews3DTranslation_x.setText(MguiDouble.getString(v2.x, "#0.00000"));
		txtViews3DTranslation_y.setText(MguiDouble.getString(v2.y, "#0.00000"));
		
		v = new Vector3d(currentView.camera.upVector);
		
		txtViews3DUp_x.setText(MguiDouble.getString(v.x, "#0.00000"));
		txtViews3DUp_y.setText(MguiDouble.getString(v.y, "#0.00000"));
		txtViews3DUp_z.setText(MguiDouble.getString(v.z, "#0.00000"));
		
	}
	
	private void updateVideo3DValues(){
		
		//TODO: reset controls here
		if (currentVideo3D == null) return;
		
		txtVideo3DName.setText(currentVideo3D.getName());
		txtVideo3DDuration.setText(TimeFunctions.getTimeStr(currentVideo3D.duration));
		txtVideo3DRefresh.setText("" + currentVideo3D.refresh);
		
		ArrayList<VideoTask> tasks = new ArrayList<VideoTask>(currentVideo3D.getTasks());
		video3DTaskModel.setTasks(tasks);
		
	}
	
	void setCurrentVideo3D(Video3D video){
		
		if (currentVideo3D != null)
			currentVideo3D.removeListener(this);
		
		currentVideo3D = video;
		currentVideo3D.addListener(this);
		
		if (cmbVideo3DWindow.getSelectedItem() == null) return;
		
		video.setWindow((InterfaceGraphic3D)cmbVideo3DWindow.getSelectedItem());
	}
	
	public void cameraAngleChanged(CameraEvent e) {
		updateView3DValues();
		
		if (chkViews3DSetImmediate.isSelected()){
			updateFromCameraChange(e.getCamera());
			}
		
	}

	public void cameraChanged(CameraEvent e) {
		updateView3DValues();
		
		if (chkViews3DSetImmediate.isSelected()){
			updateFromCameraChange(e.getCamera());
			}
	}
	
	private void updateFromCameraChange(Camera3D src_camera){
		if (!doUpdate) return;
		doUpdate = false;
		Object[] windows = null;
		
		if (chkViews3DSetAll.isSelected()){
			int n = lstViews3DWindows.getModel().getSize();
			windows = new Object[n];
			for (int i = 0; i < n; i++)
				windows[i] = lstViews3DWindows.getModel().getElementAt(i);
		}else{
			//set camera for each selected window
			windows = lstViews3DWindows.getSelectedValues();
			}
		
		for (int i = 0; i < windows.length; i++){
			InterfaceGraphic3D g = (InterfaceGraphic3D)windows[i];
			Map3D map = (Map3D)g.getMap();
			Camera3D camera = map.getCamera();
			if (camera != src_camera){
				camera.setFromCamera(src_camera, 
											 chkViews3DTarget.isSelected(), 
											 chkViews3DZoom.isSelected());
				map.updateTargetTransform();
				}
			}
		doUpdate = true;
		
	}
	
	View3D getCurrentView(){
		
		Camera3D camera = new Camera3D();
		Vector3d v = new Vector3d();
		v.x = Double.valueOf(txtViews3DLOS_x.getText());
		v.y = Double.valueOf(txtViews3DLOS_y.getText());
		v.z = Double.valueOf(txtViews3DLOS_z.getText());
		
		camera.lineOfSight = v;
		
		v = new Vector3d();
		v.x = Double.valueOf(txtViews3DUp_x.getText());
		v.y = Double.valueOf(txtViews3DUp_y.getText());
		v.z = Double.valueOf(txtViews3DUp_z.getText());
		
		camera.upVector = v;
		
		Point3d p = new Point3d();
		p.x = Double.valueOf(txtViews3DTarget_x.getText());
		p.y = Double.valueOf(txtViews3DTarget_y.getText());
		p.z = Double.valueOf(txtViews3DTarget_z.getText());
		
		camera.centerOfRotation = p;
		
		Vector2d t = new Vector2d();
		t.x = Double.valueOf(txtViews3DTranslation_x.getText());
		t.y = Double.valueOf(txtViews3DTranslation_y.getText());
		
		camera.translateXY = t;
		
		if (chkViews3DZoom.isSelected())
			camera.setDistance(Double.valueOf(txtViews3DZoom.getText()));
		else
			camera.setDistance(1.0);
		
		return new View3D(txtViews3DName.getText(), camera);
	}
	
	public void tabbedDisplayChanged(TabbedDisplayEvent e){
		switch (e.getEventType()){
			case TabChanged:
				showPanel();
			}
	}
	
	protected void updateWindowViews(){
		
		Object[] windows = null;
		
		if (chkViews3DSetAll.isSelected()){
			int n = lstViews3DWindows.getModel().getSize();
			windows = new Object[n];
			for (int i = 0; i < n; i++)
				windows[i] = lstViews3DWindows.getModel().getElementAt(i);
		}else{
			//set camera for each selected window
			windows = lstViews3DWindows.getSelectedValues();
			}
		
		for (int i = 0; i < windows.length; i++){
			InterfaceGraphic3D g = (InterfaceGraphic3D)windows[i];
			
			Map3D map = (Map3D)g.getMap();
			map.getCamera().setFromCamera(getCurrentView().camera, 
										 chkViews3DTarget.isSelected(), 
										 chkViews3DZoom.isSelected());
			map.updateTargetTransform();
			}
	}
	
	public void actionPerformed(ActionEvent e){
		
		InterfaceDisplayPanel displayPanel = InterfaceSession.getDisplayPanel();
		
		if (e.getActionCommand().equals(CMD_ADD_WIN)){
			// Add first window type
			ArrayList<String> names = InterfaceEnvironment.getInterfaceGraphicNames();
			InterfaceGraphic<?> panel = InterfaceEnvironment.getInterfaceGraphicInstance(names.get(0));
			panel.setName(window_list_model.getDefaultName(panel));
			
			window_list_model.addPanel(panel);
			return;
			}
		
		if (e.getActionCommand().equals(CMD_REM_WIN)){
			
			int row = window_list.getSelectedRow();
			if (row < 0) return;
			
			window_list_model.removePanel(row);
			return;
			
			}
		
		//move selected window up in list if possible
		if (e.getActionCommand().equals(CMD_UP_WIN)){
			
			int row = window_list.getSelectedRow();
			if (row < 0) return;
			
			window_list_model.moveUp(row);
			return;
					
			}
		
		//move selected window down in list if possible
		if (e.getActionCommand().equals(CMD_DOWN_WIN)){
			
			int row = window_list.getSelectedRow();
			if (row < 0) return;
			
			window_list_model.moveDown(row);
			return;
		
			}
		
		if (e.getActionCommand().equals("Reset Windows")){
			initWindowList();
			//lstWindows.updateDisplay();
			return;
		}
		
		//apply new windows list to display panel
		if (e.getActionCommand().equals(CMD_APPLY_WIN)){
			//for each window in list
			//destroy discarded windows
			InterfaceDisplayPanel display_panel = InterfaceSession.getDisplayPanel();
			
			if (display_panel instanceof InterfaceTabbedDisplayPanel){
				display_panel = ((InterfaceTabbedDisplayPanel)display_panel).getCurrentPanel();
				}
			
			if (display_panel == null) return;
			
			ArrayList<InterfaceGraphic<?>> skip = new ArrayList<InterfaceGraphic<?>>();
			ArrayList<InterfaceGraphicWindow> windows = display_panel.getWindowsDepthFirst();
			display_panel.removeAllPanels();
			
			for (int i = 0; i < window_list_model.panels.size(); i++){
				InterfaceGraphicWindow window = new InterfaceGraphicWindow(window_list_model.panels.get(i));
				display_panel.addWindow(window);
				}
			
			display_panel.updateDisplay();
			
			initWindowList();
			updateDisplay();
			}
		
		//set source for selected window
		if (e.getActionCommand().equals(CMD_SET_SOURCE)){
			
			if (currentSourceWindow == null) return;
			
			//is object at selected tree node displayable?
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeSources.getLastSelectedPathComponent();

			if (node == null) return;

			Object nodeInfo = node.getUserObject();
			if (node.isLeaf())
				currentSourceWindow.setSource(nodeInfo);
				
		}
		
		//refresh source tree nodes
		if (e.getActionCommand().equals(CMD_REF_SOURCE)){
			setDisplayObjectsNodes();
			rootNode.removeAllChildren();
			rootNode.add(getDisplayObjectsNode());
			treeSources.updateUI();
			
			}
		
		//update source list for selected window type
		if (e.getActionCommand().equals(CMB_SOURCE_WIN)){
			
			if (!handleCombo) return;
			//set current window
			currentSourceWindow = ((InterfaceGraphicWindow)cmbSourceWindow.getSelectedItem()).getPanel();
			
			rootNode.removeAllChildren();
			rootNode.add(getDisplayObjectsNode());
			treeSources.updateUI();
			}
		
		//3D views
		if (e.getActionCommand().startsWith("Views3D")){
			
			if (e.getActionCommand().endsWith("Named")){
				chkViews3DWindow.setSelected(!chkViews3DNamed.isSelected());
				updateView3DNamed();
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Windows")){
				chkViews3DNamed.setSelected(!chkViews3DWindow.isSelected());
				updateView3DWindow();
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Set All")){
				chkViews3DSetSelected.setSelected(!chkViews3DSetAll.isSelected());
				updateControls();
				//updateView3DWindow();
				return;
				}
			
			if (e.getActionCommand().endsWith("Set Selected")){
				chkViews3DSetAll.setSelected(!chkViews3DSetSelected.isSelected());
				updateControls();
				//updateView3DWindow();
				return;
				}
			
			if (e.getActionCommand().endsWith("Window Changed")){
				updateView3DWindow();
				updateView3DNamed();
				return;
				}
			
			if (e.getActionCommand().endsWith("Named Changed")){
				updateView3DNamed();
				updateView3DWindow();
				return;
				}
			
			if (e.getActionCommand().endsWith("Name Changed")){
				
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Set") && currentView != null){
				
				updateWindowViews();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Zoom")){
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Target")){
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Flip LOS")){
				Vector3d v = new Vector3d();
				v.x = Double.valueOf(txtViews3DLOS_x.getText());
				v.y = Double.valueOf(txtViews3DLOS_y.getText());
				v.z = Double.valueOf(txtViews3DLOS_z.getText());
				v.scale(-1.0);
				txtViews3DLOS_x.setText(MguiDouble.getString(v.x, "#0.00000"));
				txtViews3DLOS_y.setText(MguiDouble.getString(v.y, "#0.00000"));
				txtViews3DLOS_z.setText(MguiDouble.getString(v.z, "#0.00000"));
				//updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Flip Up")){
				Vector3d v = new Vector3d();
				v.x = Double.valueOf(txtViews3DUp_x.getText());
				v.y = Double.valueOf(txtViews3DUp_y.getText());
				v.z = Double.valueOf(txtViews3DUp_z.getText());
				v.scale(-1.0);
				txtViews3DUp_x.setText(MguiDouble.getString(v.x, "#0.00000"));
				txtViews3DUp_y.setText(MguiDouble.getString(v.y, "#0.00000"));
				txtViews3DUp_z.setText(MguiDouble.getString(v.z, "#0.00000"));
				//updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Translate Zero")){
				txtViews3DTranslation_x.setText("0.00000");
				txtViews3DTranslation_y.setText("0.00000");
				return;
				}
			
			if (e.getActionCommand().endsWith("Save")){
				View3D view = getCurrentView();
				
				view = InterfaceSession.getWorkspace().addView3D(view);
				if (view == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Save View3D", 
												  "View is not editable..", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				initViews3D();
				cmbViews3DNamed.setSelectedItem(view);
				updateView3DNamed();
				updateView3DWindow();
				return;
				}
			
			if (e.getActionCommand().endsWith("Delete")){
				if (!chkViews3DNamed.isSelected()) return;
				
				View3D view = (View3D)cmbViews3DNamed.getSelectedItem();
				InterfaceSession.getWorkspace().removeView3D(view);
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Delete View3D", 
											  "View '" + view.getName() + "' deleted.", 
											  JOptionPane.INFORMATION_MESSAGE);
				return;
				}
			
			}
		
		if (e.getActionCommand().startsWith("Video3D")){
			
			if (e.getActionCommand().endsWith("Video Changed")){
				if (!handleCombo) return;
				setCurrentVideo3D((Video3D)cmbVideo3DVideo.getSelectedItem());
				
				updateVideo3DValues();
				return;
				}
			
			if (e.getActionCommand().endsWith("Start")){
				if (currentVideo3D == null) return;
				currentVideo3D.reset();
				return;
				}
			
			if (e.getActionCommand().endsWith("Stop")){
				if (currentVideo3D == null) return;
				currentVideo3D.stop();
				return;
				}
			
			if (e.getActionCommand().endsWith("Forward")){
				if (currentVideo3D == null) return;
				try{
					currentVideo3D.resume();
				}catch (VideoException ex){
					ex.printStackTrace();
					}
				return;
				}
			
			if (e.getActionCommand().endsWith("New Task")){
				
				if (currentVideo3D == null) return;
				
				videoTaskOptions = new VideoTaskOptions();
				VideoTaskDialog dialog = new VideoTaskDialog(displayPanel.getParentFrame(),
															 this, 
															 videoTaskOptions);
				
				dialog.setVisible(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Edit Task")){
				if (currentVideo3D == null) return;
				
				int row = lstVideo3DTasks.getSelectedRow();
				if (row < 0) return;
				
				videoTaskOptions = new VideoTaskOptions(video3DTaskModel.getTaskAtRow(row));
				VideoTaskDialog dialog = new VideoTaskDialog(displayPanel.getParentFrame(),
															 this, 
															 videoTaskOptions);
				
				dialog.setVisible(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove Task")){
				
				if (currentVideo3D == null) return;
				
				int[] rows = lstVideo3DTasks.getSelectedRows();
				if (rows.length == 0) return;
				ArrayList<VideoTask> tasks = new ArrayList<VideoTask>();
				int r = 0;
				
				for (int i = 0; i < currentVideo3D.tasks.size(); i++){
					if (r >= rows.length || i != rows[r])
						tasks.add(currentVideo3D.tasks.get(i));
					else
						r++;
					}
				
				currentVideo3D.setTasks(tasks);
				updateVideo3DValues();
				return;
			}
			
			if (e.getActionCommand().endsWith("Delete Video")){
				if (currentVideo3D == null) return;
				
				InterfaceSession.getWorkspace().removeVideo(currentVideo3D);
				currentVideo3D = null;
				setVideo3DList();
				return;
				}
			
			if (e.getActionCommand().endsWith("New Video")){
				
				String name = JOptionPane.showInputDialog("Name for new Video3D:");
				if (name == null) return;
				
				Video3D video = new Video3D(name);
				InterfaceSession.getWorkspace().addVideo(video);
				setVideo3DList();
				
				cmbVideo3DVideo.setSelectedItem(video);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Save Video")){
				
				if (currentVideo3D == null) return;
				
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(XMLFunctions.getXMLFileFilter());
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fc.showSaveDialog(displayPanel) != JFileChooser.APPROVE_OPTION)
					return;
				try{
					XMLFunctions.writeToXML(fc.getSelectedFile(), currentVideo3D);
					JOptionPane.showMessageDialog(displayPanel, 
							  "Wrote video to '" + fc.getSelectedFile().getAbsolutePath() + "'",
							  "Write Video3D",
							  JOptionPane.INFORMATION_MESSAGE);
				}catch (XMLException ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(displayPanel, 
												  "Error writing video to '" + fc.getSelectedFile().getAbsolutePath() + "'",
												  "Write Video3D",
												  JOptionPane.ERROR_MESSAGE);
					}
				return;
				}
			
			if (e.getActionCommand().endsWith("Load Video")){
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(XMLFunctions.getXMLFileFilter());
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fc.showOpenDialog(displayPanel) != JFileChooser.APPROVE_OPTION)
					return;
				
				VideoXMLLoader loader = new VideoXMLLoader(fc.getSelectedFile());
				Video video = loader.loadVideo();
				if (video == null){
					JOptionPane.showMessageDialog(displayPanel, 
							  "Error loading video '" + fc.getSelectedFile().getAbsolutePath() + "'",
							  "Load Video3D",
							  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				InterfaceSession.getWorkspace().addVideo(video);
				//add any new views as well
				for (int i = 0; i < video.tasks.size(); i++)
					if (video.tasks.get(i) instanceof SetView3DTask)
						InterfaceSession.getWorkspace().addView3D(((SetView3DTask)video.tasks.get(i)).view_target);
				
				video.updateTasks(displayPanel);
				
				updateDisplay();
				setCurrentVideo3D((Video3D)video);
				JOptionPane.showMessageDialog(displayPanel, 
						  "Loaded video '" + video.getName() + "'",
						  "Load Video3D",
						  JOptionPane.INFORMATION_MESSAGE);
				return;
				}
			
			if (e.getActionCommand().endsWith("Update Video")){
				if (currentVideo3D == null) return;
				currentVideo3D.setName(txtVideo3DName.getText());
				currentVideo3D.duration = Long.valueOf(TimeFunctions.getTimeFromStr(txtVideo3DDuration.getText()));
				currentVideo3D.refresh = Long.valueOf(txtVideo3DRefresh.getText());
				currentVideo3D.window = (InterfaceGraphic<?>)cmbVideo3DWindow.getSelectedItem();
				setVideo3DList();
				return;
				}
			
			if (e.getActionCommand().endsWith("Output Options")){
				
				//temp while only image stack available
				if (video3DOutputOptions == null){
					video3DOutputOptions = new ImageStackVideoOptions(30, 0, 60000, 20, null);
					if (cmbVideo3DWindow.getSelectedItem() != null){
						video3DOutputOptions.window =(InterfaceGraphic<?>)cmbVideo3DWindow.getSelectedItem();
						}
					}
				
				ImageStackVideoDialog dialog = new ImageStackVideoDialog(displayPanel.getParentFrame(),
																		 this,
																		 video3DOutputOptions);
				
				dialog.setVisible(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Output File")){
				
				if (video3DOutputOptions == null){
					video3DOutputOptions = new ImageStackVideoOptions(30, 0, 60000, 20, null);
					if (cmbVideo3DWindow.getSelectedItem() != null){
						video3DOutputOptions.window =(InterfaceGraphic<?>)cmbVideo3DWindow.getSelectedItem();
						}
					}
				
				JFileChooser fc = video3DOutputOptions.getFileChooser();
				if (fc.showSaveDialog(displayPanel) == JFileChooser.APPROVE_OPTION)
					video3DOutputOptions.setFiles(new File[]{fc.getSelectedFile()});
				else
					return;
				
				txtVideo3DOutputFile.setText(video3DOutputOptions.getFiles()[0].getAbsolutePath());
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Output")){
				
				//temp while image stack writer is only option
				
				if (video3DOutputOptions == null) return;
				if (cmbVideo3DWindow.getSelectedItem() == null) return;
				if (video3DOutputOptions.getFiles() == null) return;
				
				InterfaceGraphic3D window = (InterfaceGraphic3D)cmbVideo3DWindow.getSelectedItem();
				video3DOutputOptions.window = window;
				
				if (cmbVideo3DVideo.getSelectedItem() == null) return;
				
				Video3D video = (Video3D)cmbVideo3DVideo.getSelectedItem();
				video3DOutputOptions.video = video;
				
				InterfaceProgressBar progress_bar = new InterfaceProgressBar("Writing video '" + 
																			 video.getName() +"': ");
				
				ImageStackVideoWriter writer = new ImageStackVideoWriter();
				progress_bar.register();
				if (writer.write(video3DOutputOptions, progress_bar))
					JOptionPane.showMessageDialog(displayPanel, "Images written to '" + 
																video3DOutputOptions.getFiles()[0].getAbsolutePath() + "'");
				else
					JOptionPane.showMessageDialog(displayPanel, "Error writing images to '" + 
																video3DOutputOptions.getFiles()[0].getAbsolutePath() + 
																"' (or user cancelled).");
				progress_bar.deregister();
				
				return;
				}
			
			}
		
		
	}
	
	public void updateFromDialog(InterfaceDialogBox dialog){
		
		if (dialog instanceof VideoTaskDialog){
			if (currentVideo3D == null) return;
			
			VideoTaskDialog v_dialog = (VideoTaskDialog)dialog;
			VideoTaskOptions options = (VideoTaskOptions)v_dialog.getOptions();
			VideoTask task = (VideoTask)options.task;
			if (options.old_task == null)
				currentVideo3D.addTask(task);
			else{
				if (options.old_task.getClass().equals(task)){
					options.old_task.setFromTask(task);
				}else{
					currentVideo3D.removeTask(options.old_task);
					currentVideo3D.addTask(task);
					}
				currentVideo3D.sortTasks();
				}
			
			updateVideo3DValues();
			return;
			}
		
		if (dialog instanceof VideoOutputDialog){
			video3DOutputOptions = (VideoOutputOptions)((VideoOutputDialog)dialog).options;
			return;
			}
		
	}
	
	protected long getVideo3DClock(){
	
		if (currentVideo3D == null) return -1;
		return currentVideo3D.getClock();
		
	}
	
	public void ClockChanged(VideoEvent e){
		
		if (e.getSource() instanceof Video3D){
			Video3D video = (Video3D)e.getSource();
			txtVideo3DClock.setText(TimeFunctions.getTimeStr(video.getClock()));
			lstVideo3DTasks.repaint();
			return;
			}
		
	}
	
	public void ClockStarted(VideoEvent e){
		
	}
	
	public void ClockLagged(VideoEvent e, long lag) {
		// TODO Auto-generated method stub
		
	}

	public void ClockStopped(VideoEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void VideoEnded(VideoEvent e){
		if (!chkVideo3DLoop.isSelected()) return;
		
		try{
			currentVideo3D.restart();
		}catch (VideoException ex){
			ex.printStackTrace();
			}
	}
	
	class mouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e){
			if (e.getSource() instanceof JTable && e.getClickCount() >= 2){
				//edit this row
				JTable t = (JTable)e.getSource();
				if (t.getSelectedRow() < 0) return;
				t.setEditingRow(t.getSelectedRow());
				t.editCellAt(t.rowAtPoint(e.getPoint()), t.rowAtPoint(e.getPoint()));
				}
		}
	}
	
	class WindowListModel extends AbstractTableModel{
		
		public ArrayList<InterfaceGraphic<?>> panels = new ArrayList<InterfaceGraphic<?>>();
		ArrayList<Boolean> is_new = new ArrayList<Boolean>();
				
		public WindowListModel(){
			
			
		}
		
		public void addPanel(InterfaceGraphic<?> panel){
			panels.add(panel);
			is_new.add(true);
			this.fireTableDataChanged();
		}
		
		public void removePanel(InterfaceGraphic<?> panel){
			for (int i = 0; i < panels.size(); i++){
				if (panels.get(i).equals(panel)){
					panels.remove(i);
					is_new.remove(i);
					}
				}
			this.fireTableDataChanged();
		}
		
		public void removePanel(int index){
			panels.remove(index);
			is_new.remove(index);
			this.fireTableDataChanged();
		}
		
		public void moveUp(int index){
			if (index < 1 || index >= panels.size()) return;
			
			InterfaceGraphic<?> panel = panels.remove(index);
			Boolean b = is_new.remove(index);
			panels.add(index - 1, panel);
			is_new.add(index - 1, b);
			this.fireTableDataChanged();
		}
		
		public void moveDown(int index){
			if (index >= panels.size() - 1) return;
			
			InterfaceGraphic<?> panel = panels.remove(index);
			Boolean b = is_new.remove(index);
			panels.add(index + 1, panel);
			is_new.add(index + 1, b);
			this.fireTableDataChanged();
		}
		
		public void setPanelsFromWindows(ArrayList<InterfaceGraphicWindow> windows){
			panels = new ArrayList<InterfaceGraphic<?>>(windows.size());
			is_new = new ArrayList<Boolean>(windows.size());
			for (int i = 0; i < windows.size(); i++){
				panels.add(windows.get(i).getPanel());
				is_new.add(false);
				}
			this.fireTableDataChanged();
		}
		
		public void setPanels(ArrayList<InterfaceGraphic<?>> new_panels){
			panels = new ArrayList<InterfaceGraphic<?>>(new_panels.size());
			is_new = new ArrayList<Boolean>(new_panels.size());
			for (int i = 0; i < new_panels.size(); i++){
				panels.add(new_panels.get(i));
				is_new.add(false);
				}
			this.fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return panels.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			switch (column){
				case 0:
					return "Type";
				case 1:
					return "Name";
				}
			return "?";
		}

		@Override
		public Class<?> getColumnClass(int column){
			switch (column){
				case 0:
					return InterfaceGraphic.class;
				case 1:
					return String.class;
				}
			return Object.class;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return true;
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column){
				case 0:
					return panels.get(row);
				case 1:
					return panels.get(row).getName();
				}
			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			
			switch (column){
				case 0:
					InterfaceGraphic<?> panel = (InterfaceGraphic<?>)value;
					if (panel.getClass().equals(panels.get(row).getClass())) return;
					String name = "?";
					if (is_new.get(row))
						name = getDefaultName(panel);
					else
						name = panels.get(row).getName();
					String type = InterfaceEnvironment.getNameForInterfaceGraphicClass(panel.getClass());
					InterfaceGraphic<?> panel2 = InterfaceEnvironment.getInterfaceGraphicInstance(type);
					panel2.setName(name);
					panels.set(row, panel2);
					fireTableDataChanged();
					return;
				case 1:
					panels.get(row).setName((String)value);
					fireTableDataChanged();
					return;
				}
			
		}
		
		public String getDefaultName(InterfaceGraphic<?> panel){
			String name = InterfaceEnvironment.getNameForInterfaceGraphicClass(panel.getClass());
			String start = name;
			int n = 1;
			for (int i = 0; i < panels.size(); i++){
				if (panels.get(i).getName().equals(name)){
					name = start + " " + n;
					n++;
					i = -1;
					}
				}
			return name;
		}
		
	}
	
	Transformer<Object,String> graphic_transformer = new Transformer<Object,String>(){
		public String transform(Object obj){
			if (obj instanceof InterfaceGraphic){
				InterfaceGraphic<?> panel = (InterfaceGraphic<?>)obj;
				return InterfaceEnvironment.getNameForInterfaceGraphicClass(panel.getClass());
				}
			return "?";
		}
	};
	
	protected class GraphicWindowListRenderer extends InterfaceComboBox implements TableCellRenderer {
		
		public GraphicWindowListRenderer(){
			super(InterfaceComboBox.RenderMode.LongestItem, true, 500, graphic_transformer);
			
			ArrayList<String> types = InterfaceEnvironment.getInterfaceGraphicNames();
			Collections.sort(types);
			
			for (int i = 0; i < types.size(); i++){
				InterfaceGraphic<?> panel = InterfaceEnvironment.getInterfaceGraphicInstance(types.get(i));
				panel.setName(types.get(i));
				this.addItem(panel);
				}
			
		}
		
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
													   Object value, boolean isSelected, boolean hasFocus, int row,
													   int column) {
		
			InterfaceGraphic<?> panel = (InterfaceGraphic<?>)value;
			this.setSelectedItem(panel);
			
			if (panel != null){
				for (int i = 0; i < this.getItemCount(); i++){
					InterfaceGraphic<?> panel2 = (InterfaceGraphic<?>)this.getItemAt(i);
					if (panel.getClass().equals(panel2.getClass()))
						this.setSelectedItem(panel2);
					}
				}
			
			return this;
		}
		
	}
	
	protected class GraphicWindowListEditor extends AbstractCellEditor implements TableCellEditor, ItemListener{

		InterfaceComboBox combo_box;
		boolean update = true;
		
		public GraphicWindowListEditor(){
			
			combo_box = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500, graphic_transformer);
			
			ArrayList<String> types = InterfaceEnvironment.getInterfaceGraphicNames();
			Collections.sort(types);
			
			for (int i = 0; i < types.size(); i++){
				InterfaceGraphic<?> panel = InterfaceEnvironment.getInterfaceGraphicInstance(types.get(i));
				panel.setName(types.get(i));
				combo_box.addItem(panel);
				}
			
			combo_box.addItemListener(this);
		}
		
		@Override
		public Object getCellEditorValue() {
			return combo_box.getSelectedItem();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			
			InterfaceGraphic<?> panel = (InterfaceGraphic<?>)value;
			combo_box.setSelectedItem(panel);
			/*
			if (panel != null){
				for (int i = 0; i < combo_box.getItemCount(); i++){
					InterfaceGraphic<?> panel2 = (InterfaceGraphic<?>)combo_box.getItemAt(i);
					if (panel.getClass().equals(panel2.getClass())){
						update = false;
						combo_box.setSelectedItem(panel2);
						update = true;
						}
					}
				}
			*/
			
			return combo_box;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (!update) return;
			if (e.getStateChange() == ItemEvent.SELECTED){
				stopCellEditing();
				return;
				}
			
		}
				
		
	}
	
	
	class VideoTaskListModel extends DefaultTableModel{
		
		ArrayList<VideoTask> tasks = new ArrayList<VideoTask>();
		
		public VideoTaskListModel(ArrayList<VideoTask> tasks){
			this.tasks = tasks;
		}
		
		public void setTasks(ArrayList<VideoTask> tasks){
			this.tasks = tasks;
			this.fireTableDataChanged();
		}
		
		public Object getValueAt(int row, int col){
			VideoTask task = tasks.get(row);
			
			switch (col){
				case 0:
					//return task.isActive(getVideo3DClock());
					return task.isOn;
				case 1:
					return TimeFunctions.getTimeStr(task.getStart());
				case 2:
					return TimeFunctions.getTimeStr(task.getStop());
				case 3:
					return task.getName();
				}
			
			return 0;
			
		}
		
		public int getRowCount(){
			if (tasks == null) return 0;
			return tasks.size();
		}
		
		public int getColumnCount(){
			return 4;
		}
		
		public VideoTask getTaskAtRow(int row){
			if (row < 0 || row > getRowCount()) return null;
			
			return tasks.get(row);
		}
		
		public String getColumnName(int c){
			switch (c){
				case 0:
					return "Active";
				case 1:
					return "Start";
				case 2:
					return "Stop";
				case 3:
					return "Task";
				}
			return "";
		}
		
		public Class getColumnClass(int c){
			switch (c){
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
		}
		
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
	}
	
	class VideoTableRenderer extends DefaultTableCellRenderer{
		
		public Color highlightColour = new Color(255, 153, 153);
		public Color backgroundColour = Color.white;
		
		public VideoTableRenderer(){
			super();
			this.setOpaque(true);
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
			
			if (currentVideo3D != null && video3DTaskModel.getTaskAtRow(row).isActive(currentVideo3D.clock)){
				setBackground(highlightColour);
				isSelected = false;
			}else{
				setBackground(table.getBackground());
				
				}
			
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
		}
		
	}
	
}