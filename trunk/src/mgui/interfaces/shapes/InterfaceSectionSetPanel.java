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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import org.jogamp.java3d.Transform3D;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Plane3D;
import mgui.interfaces.InterfaceHideablePanel;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.TabbedDisplayEvent;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.graphics.GraphicEvent;
import mgui.interfaces.graphics.GraphicEvent.EventType;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.graphics.InterfaceGraphicListener;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.graphics.WindowEvent;
import mgui.interfaces.graphics.WindowListener;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.util.ClipPlane;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;


/************************
 * Updated version of InterfaceSectionSet that sets a current section set for a
 * given Graphic2D window, and sets current sections for that window.
 * 
 * @author Andrew Reid
 *
 */

public class InterfaceSectionSetPanel extends InterfacePanel implements ShapeListener,
																	ActionListener,
																	ItemListener,
																	WindowListener,
																	InterfaceGraphicListener {

	public final String CMD_NEXT = "Next Section";
	public final String CMD_PREV = "Previous Section";
	public final String CMD_REF = "Reference Section";
	public final String CMD_NEXT_SUB = "Next Subsection";
	public final String CMD_PREV_SUB = "Previous Subsection";
	public final String CMD_REF_SUB = "Reference Subsection";
	public final String CMB_SECTIONS = "Section Set Change";
	public final String CMB_WINDOWS = "Window Change";
	public final String TXT_SECT = "Set Section";
	public final String CMD_UPDATE = "Update Section";
	public final String CMD_ROTX = "Rotate X";
	public final String CMD_ROTY = "Rotate Y";
	public final String CMD_ROTZ = "Rotate Z";
	
	ArrayList<ClipPlane> clip_planes = new ArrayList<ClipPlane>();
	
	//public InterfaceDisplayPanel displayPanel;
	SectionSet3DInt currentSet;
	SectionSet3DInt updateSet;
	InterfaceGraphicWindow currentWindow;
	
	//categories and components
	CategoryTitle lblWindows = new CategoryTitle("WINDOWS");
	JLabel lblSelectedWindow = new JLabel("Graphic2D Window:");
	//JComboBox cmbSelectedWindow = new JComboBox();
	InterfaceComboBox cmbSelectedWindow = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  													true, 500);
	JLabel lblSectionSet = new JLabel("Section Set:");
	//JComboBox cmbSectionSet = new JComboBox();
	InterfaceComboBox cmbSectionSet = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
															true, 500);
	InterfaceHideablePanel ihpSetDetails;
	InterfaceAttributePanel attrSetDetails;
	AttributeList sectionAttr;
	
	CategoryTitle lblSections = new CategoryTitle("SECTIONS");
	JLabel lblCurrentSection = new JLabel("Current Section:");
	JTextField txtCurrentSection;
	InterfaceHideablePanel ihpSectionDetails;
	InterfaceAttributePanel attrSectionDetails;
	JButton cmdNextSection = new JButton("Next");
	JButton cmdPrevSection = new JButton("Prev");
	JButton cmdRefSection = new JButton("Ref");
	
	CategoryTitle lblNewUpdate = new CategoryTitle("CREATE/UPDATE");
	final String NEW_SET = "<-New Set->"; 
	JLabel lblUpdateShapeSet = new JLabel("Shape 3D Set:");
	//JComboBox cmbUpdateShapeSet = new JComboBox();
	InterfaceComboBox cmbUpdateShapeSet = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
															    true, 500);
	JLabel lblUpdateSet = new JLabel("Section Set:");
	//JComboBox cmbUpdateSet = new JComboBox();
	InterfaceComboBox cmbUpdateSet = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
														   true, 500);
	JLabel lblUpdateName = new JLabel("Name:"); 
	JTextField txtUpdateName = new JTextField("New Section Set");
	JLabel lblOrigin = new JLabel("Origin:");
	JLabel lblOriginX = new JLabel(" X "); 
	JTextField txtOriginX = new JTextField("0");
	JLabel lblOriginY = new JLabel(" Y "); 
	JTextField txtOriginY = new JTextField("0");
	JLabel lblOriginZ = new JLabel(" Z "); 
	JTextField txtOriginZ = new JTextField("0");
	JLabel lblAxisX = new JLabel("X-Axis");
	JButton cmdDefX = new JButton("Def");
	JLabel lblAxisX_X = new JLabel(" X "); 
	JTextField txtAxisX_X = new JTextField("1");
	JLabel lblAxisX_Y = new JLabel(" Y "); 
	JTextField txtAxisX_Y = new JTextField("0");
	JLabel lblAxisX_Z = new JLabel(" Z "); 
	JTextField txtAxisX_Z = new JTextField("0");
	JLabel lblAxisY = new JLabel("Y-Axis");
	JButton cmdDefY = new JButton("Def");
	JLabel lblAxisY_X = new JLabel(" X "); 
	JTextField txtAxisY_X = new JTextField("0");
	JLabel lblAxisY_Y = new JLabel(" Y "); 
	JTextField txtAxisY_Y = new JTextField("1");
	JLabel lblAxisY_Z = new JLabel(" Z "); 
	JTextField txtAxisY_Z = new JTextField("0");
	JLabel lblAxisZ = new JLabel("Normal");
	JButton cmdDefZ = new JButton("Def");
	JLabel lblAxisZ_X = new JLabel(" X "); 
	JTextField txtAxisZ_X = new JTextField("0");
	JLabel lblAxisZ_Y = new JLabel(" Y "); 
	JTextField txtAxisZ_Y = new JTextField("0");
	JLabel lblAxisZ_Z = new JLabel(" Z "); 
	JTextField txtAxisZ_Z = new JTextField("1");
	JCheckBox chkAxisZ_flip = new JCheckBox(" Flip");
	JLabel lblUpdateSpacing = new JLabel("Spacing:");
	JTextField txtUpdateSpacing = new JTextField("1.0");
	
	JLabel lblRotation = new JLabel("Rotate:");
	JTextField txtRotation = new JTextField("0.0");
	JButton cmdRotX = new JButton("rotX");
	JButton cmdRotY = new JButton("rotY");
	JButton cmdRotZ = new JButton("rotZ");
	
	JButton cmdUpdate = new JButton("Create");
	JButton cmdDelete = new JButton("Delete");
	
//	item listener toggle
	boolean blnItemListen = true;
	boolean showSubsections = false;
	
	private boolean isDestroyed = false;
	
	public InterfaceSectionSetPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	public void destroy(){
		isDestroyed = true;
	}
	
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	protected void init(){
		//populate combo box with all Graphic2D windows
		//select first windows
		//if none are selected (i.e., none exist), disable everything except combo

		_init();
		
		if (currentSet != null)
			attrSetDetails = new InterfaceAttributePanel(currentSet);
		else
			attrSetDetails = new InterfaceAttributePanel();
		
		ihpSetDetails = new InterfaceHideablePanel(attrSetDetails);
		
		sectionAttr = new AttributeList();
		sectionAttr.add(new Attribute<MguiInteger>("SectionNo", new MguiInteger(0)));
		sectionAttr.add(new Attribute<MguiDouble>("SectionWidth", new MguiDouble(0)));
		sectionAttr.add(new Attribute<MguiDouble>("DistFromRef", new MguiDouble(0)));
		sectionAttr.add(new Attribute<MguiBoolean>("HasShapes", new MguiBoolean(false)));
		sectionAttr.add(new Attribute<MguiDouble>("Point x", new MguiDouble(0)));
		sectionAttr.add(new Attribute<MguiDouble>("Point y", new MguiDouble(0)));
		sectionAttr.add(new Attribute<MguiDouble>("Point z", new MguiDouble(0)));
		
		attrSectionDetails = new InterfaceAttributePanel(sectionAttr);
		attrSectionDetails.setEditable(false);
		
		ihpSectionDetails = new InterfaceHideablePanel(attrSectionDetails);
		
		updateLists();
		
		lblCurrentSection.setHorizontalAlignment(JLabel.CENTER);
		txtCurrentSection = new JTextField("0");
		txtCurrentSection.setHorizontalAlignment(JTextField.CENTER);
		txtCurrentSection.setFont(new Font("Arial", Font.BOLD, 16));
		
		/*
		lblCurrentSubSection.setHorizontalAlignment(JLabel.CENTER);
		txtCurrentSubSection = new JTextField("-1");
		txtCurrentSubSection.setHorizontalAlignment(JTextField.CENTER);
		txtCurrentSubSection.setFont(new Font("Arial", Font.BOLD, 16));
		*/
		
		//listeners setup
		cmdNextSection.setActionCommand(CMD_NEXT);
		cmdPrevSection.setActionCommand(CMD_PREV);
		cmdRefSection.setActionCommand(CMD_REF);
		cmdNextSection.addActionListener(this);
		cmdPrevSection.addActionListener(this);
		cmdRefSection.addActionListener(this);
		cmbSectionSet.addItemListener(this);
		cmbSelectedWindow.addItemListener(this);
		txtCurrentSection.setActionCommand(TXT_SECT);
		txtCurrentSection.addActionListener(this);
		cmbUpdateSet.addItemListener(this);
		cmdUpdate.setActionCommand(CMD_UPDATE);
		cmdUpdate.addActionListener(this);
		cmdRotX.setActionCommand(CMD_ROTX);
		cmdRotX.addActionListener(this);
		cmdRotY.setActionCommand(CMD_ROTY);
		cmdRotY.addActionListener(this);
		cmdRotZ.setActionCommand(CMD_ROTZ);
		cmdRotZ.addActionListener(this);
		txtAxisX_X.setActionCommand("Axis Changed X_x");
		txtAxisX_X.addActionListener(this);
		txtAxisX_Y.setActionCommand("Axis Changed X_y");
		txtAxisX_Y.addActionListener(this);
		txtAxisX_Z.setActionCommand("Axis Changed X_z");
		txtAxisX_Z.addActionListener(this);
		txtAxisY_X.setActionCommand("Axis Changed Y_x");
		txtAxisY_X.addActionListener(this);
		txtAxisY_Y.setActionCommand("Axis Changed Y_y");
		txtAxisY_Y.addActionListener(this);
		txtAxisY_Z.setActionCommand("Axis Changed Y_z");
		txtAxisY_Z.addActionListener(this);
		txtAxisZ_X.setEditable(false);
		txtAxisZ_Y.setEditable(false);
		txtAxisZ_Z.setEditable(false);
		chkAxisZ_flip.addActionListener(this);
		chkAxisZ_flip.setActionCommand("Axis Flip Z");
		
		//layout setup
		setLayout(new CategoryLayout(20, 5, 200, 10));
		lblWindows.setHorizontalAlignment(JLabel.CENTER);
		
		//Window Category
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblWindows, c);
		lblWindows.setParentObj(this);
		c = new CategoryLayoutConstraints("WINDOWS", 1, 1, 0.05, 0.9, 1);
		add(lblSelectedWindow, c);
		c = new CategoryLayoutConstraints("WINDOWS", 2, 2, 0.05, 0.9, 1);
		add(cmbSelectedWindow, c);
		c = new CategoryLayoutConstraints("WINDOWS", 3, 3, 0.05, 0.9, 1);
		add(lblSectionSet, c);
		c = new CategoryLayoutConstraints("WINDOWS", 4, 4, 0.05, 0.9, 1);
		add(cmbSectionSet, c);
		c = new CategoryLayoutConstraints("WINDOWS", 5, 10, 0.05, 0.9, 1);
		add(ihpSetDetails, c);
		
		//Sections Category
		c = new CategoryLayoutConstraints();
		add(lblSections, c);
		lblSections.setParentObj(this);
		c = new CategoryLayoutConstraints("SECTIONS", 1, 1, 0.1, 0.8, 1);
		add(lblCurrentSection, c);
		c = new CategoryLayoutConstraints("SECTIONS", 2, 3, 0.35, 0.3, 1);
		add(txtCurrentSection, c);
		c = new CategoryLayoutConstraints("SECTIONS", 4, 5, 0.05, 0.3, 1);
		add(cmdPrevSection, c);
		c = new CategoryLayoutConstraints("SECTIONS", 4, 5, 0.35, 0.3, 1);
		add(cmdRefSection, c);
		c = new CategoryLayoutConstraints("SECTIONS", 4, 5, 0.65, 0.3, 1);
		add(cmdNextSection, c);
		c = new CategoryLayoutConstraints("SECTIONS", 6, 11, 0.05, 0.9, 1);
		add(ihpSectionDetails, c);
		
		c = new CategoryLayoutConstraints();
		add(lblNewUpdate, c);
		lblNewUpdate.setParentObj(this);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 1, 1, 0.05, .7, 1);
		add(lblUpdateShapeSet, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 2, 2, 0.05, .9, 1);
		add(cmbUpdateShapeSet, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 3, 3, 0.05, .7, 1);
		add(lblUpdateSet, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 4, 4, 0.05, .9, 1);
		add(cmbUpdateSet, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 5, 5, 0.05, .3, 1);
		add(lblUpdateName, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 5, 5, 0.35, .6, 1);
		add(txtUpdateName, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 6, 6, 0.05, .7, 1);
		add(lblOrigin, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 7, 7, 0.05, .1, 1);
		add(lblOriginX, c);
		lblOriginX.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 7, 7, 0.15, .2, 1);
		add(txtOriginX, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 7, 7, 0.35, .1, 1);
		add(lblOriginY, c);
		lblOriginY.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 7, 7, 0.45, .2, 1);
		add(txtOriginY, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 7, 7, 0.65, .1, 1);
		add(lblOriginZ, c);
		lblOriginZ.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 7, 7, 0.75, .2, 1);
		add(txtOriginZ, c);
		
		//X-axis
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 8, 8, 0.05, 0.2, 1);
		add(lblAxisX, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 8, 8, 0.55, 0.4, 1);
		add(cmdDefX, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 9, 9, 0.05, .1, 1);
		add(lblAxisX_X, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 9, 9, 0.15, .2, 1);
		add(txtAxisX_X, c);
		lblAxisX_X.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 9, 9, 0.35, .1, 1);
		add(lblAxisX_Y, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 9, 9, 0.45, .2, 1);
		add(txtAxisX_Y, c);
		lblAxisX_Y.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 9, 9, 0.65, .1, 1);
		add(lblAxisX_Z, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 9, 9, 0.75, .2, 1);
		add(txtAxisX_Z, c);
		lblAxisX_Z.setHorizontalAlignment(SwingConstants.RIGHT);
		
		//Y-Axis
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 10, 10, 0.05, 0.2, 1);
		add(lblAxisY, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 10, 10, 0.55, 0.4, 1);
		add(cmdDefY, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 11, 11, 0.05, .1, 1);
		add(lblAxisY_X, c);
		lblAxisY_X.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 11, 11, 0.15, .2, 1);
		add(txtAxisY_X, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 11, 11, 0.35, .1, 1);
		add(lblAxisY_Y, c);
		lblAxisY_Y.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 11, 11, 0.45, .2, 1);
		add(txtAxisY_Y, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 11, 11, 0.65, .1, 1);
		add(lblAxisY_Z, c);
		lblAxisY_Z.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 11, 11, 0.75, .2, 1);
		add(txtAxisY_Z, c);
		
		//Z-Axis
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 12, 12, 0.05, 0.2, 1);
		add(lblAxisZ, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 12, 12, 0.55, 0.4, 1);
		add(chkAxisZ_flip, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 13, 13, 0.05, .1, 1);
		add(lblAxisZ_X, c);
		lblAxisZ_X.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 13, 13, 0.15, .2, 1);
		add(txtAxisZ_X, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 13, 13, 0.35, .1, 1);
		add(lblAxisZ_Y, c);
		lblAxisZ_Y.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 13, 13, 0.45, .2, 1);
		add(txtAxisZ_Y, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 13, 13, 0.65, .1, 1);
		add(lblAxisZ_Z, c);
		lblAxisZ_Z.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 13, 13, 0.75, .2, 1);
		add(txtAxisZ_Z, c);
		
	
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 14, 14, 0.05, .4, 1);
		add(lblUpdateSpacing, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 14, 14, 0.50, .45, 1);
		add(txtUpdateSpacing, c);
		
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 15, 15, 0.05, .43, 1);
		add(lblRotation, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 16, 16, 0.05, .43, 1);
		add(txtRotation, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 15, 15, 0.52, .43, 1);
		add(cmdRotX, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 16, 16, 0.52, .43, 1);
		add(cmdRotY, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 17, 17, 0.52, .43, 1);
		add(cmdRotZ, c);
		
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 18, 19, 0.52, 0.43, 1);
		add(cmdUpdate, c);
		c = new CategoryLayoutConstraints("CREATE/UPDATE", 18, 19, 0.05, 0.43, 1);
		add(cmdDelete, c);
		
		updateDisplay();
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceSectionSetPanel.class.getResource("/mgui/resources/icons/section_set_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/section_set_3d_20.png");
		return null;
	}
	
	public void tabbedDisplayChanged(TabbedDisplayEvent e){
		switch (e.getEventType()){
			case TabChanged:
				showPanel();
			}
	}
	
	private void setCurrentSection(int section){
		if (currentSet == null){
			txtCurrentSection.setText("" + 0);
			return;
			}
		
		//TODO replace with a suitable listener
		blnItemListen = false;
		InterfaceGraphic2D p = (InterfaceGraphic2D)currentWindow.getPanel();
		txtCurrentSection.setText(String.valueOf(section));
		
		p.setCurrentSection(section);
		p.setShapeSet2D();
		updateSectionAttributes();
		
		blnItemListen = true;
	}
	
	protected void updateSectionAttributes(){
		if (currentSet == null){
			attrSectionDetails.setEnabled(false);
			return;
			}
		attrSectionDetails.setEnabled(true);
		InterfaceGraphic2D p = (InterfaceGraphic2D)currentWindow.getPanel();
		int section = p.getCurrentSection();
		
		//set attributes
		sectionAttr.setValue("SectionNo", new MguiInteger(section));
		sectionAttr.setValue("HasShapes", new MguiBoolean(currentSet.hasSection(section)));
		sectionAttr.setValue("DistFromRef", new MguiDouble(currentSet.getSectionDist(section)));
		sectionAttr.setValue("SectionWidth", new MguiDouble(currentSet.width));
		Point3f origin = currentSet.getPlaneAt(section).origin;
		sectionAttr.setValue("Point x", new MguiDouble(origin.x));
		sectionAttr.setValue("Point y", new MguiDouble(origin.y));
		sectionAttr.setValue("Point z", new MguiDouble(origin.z));
		
		attrSectionDetails.updateUI();
	}
	
	public void showPanel(){
		updateLists();
	}
	
	private void updateLists(){
		
		updateWindowList();
		updateSectionSetList();
		updateSectionAttributes();
		updateShapeSetList();
		
	}
	
	void updateShapeSetList(){
		cmbUpdateShapeSet.removeAllItems();
		InterfaceSession.getWorkspace().populateShapeSetCombo(cmbUpdateShapeSet);
		
		if (updateSet != null)
			cmbUpdateShapeSet.setSelectedItem(updateSet.getParentSet());
		else
			cmbUpdateShapeSet.setSelectedItem(InterfaceSession.getDisplayPanel().getCurrentShapeSet());
	}
	
	private void updateWindowList(){
		blnItemListen = false;
		cmbSelectedWindow.removeAllItems();
		ArrayList<InterfaceGraphicWindow> panels = InterfaceSession.getDisplayPanel().getAllWindows();
		
		for (int i = 0; i < panels.size(); i++){
			if (panels.get(i).getPanel() instanceof InterfaceGraphic2D)
				cmbSelectedWindow.addItem(panels.get(i));
			}
		
		if (cmbSelectedWindow.getItemCount() == 0) 
			return;
		if (currentWindow == null){
			cmbSelectedWindow.setSelectedIndex(0);
			setCurrentWindow((InterfaceGraphicWindow)cmbSelectedWindow.getSelectedItem());
		}else{
			cmbSelectedWindow.setSelectedItem(currentWindow);
			}
		blnItemListen = true;
	}
	
	private void updateSectionSetList(){
		blnItemListen = false;
		cmbSectionSet.removeAllItems();
		cmbSectionSet.addItem("~");
		cmbUpdateSet.removeAllItems();
		cmbUpdateSet.addItem(NEW_SET);
		
		List<Shape3DInt> section_sets = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new SectionSet3DInt());
		for (Shape3DInt set : section_sets) {
			cmbSectionSet.addItem(set);
			cmbUpdateSet.addItem(set);
			}
		if (currentWindow != null && currentWindow.getPanel() instanceof InterfaceGraphic2D){
			InterfaceGraphic2D panel = (InterfaceGraphic2D)currentWindow.getPanel();
			boolean is_set = false;
			if (panel != null){
				currentSet = panel.getCurrentSectionSet();
				if (currentSet != null){
					cmbSectionSet.setSelectedItem(currentSet);
					txtCurrentSection.setText("" + panel.getCurrentSection());
					updateSectionAttributes();
					is_set = true;
					}
				}
			if (!is_set)
				cmbSectionSet.setSelectedItem("~");
			}
		if (updateSet != null){
			cmbUpdateSet.setSelectedItem(updateSet);
		}else{
			cmbUpdateSet.setSelectedItem(NEW_SET);
			}
		updateUpdateSet();
		
		if (currentSet != null){
			attrSetDetails.setEnabled(true);
			attrSetDetails.setAttributes(currentSet.attributes);
		}else{
			attrSetDetails.setEnabled(false);
			}
		
		blnItemListen = true;
	}
	
	protected void updateUpdateSet(){
		if (cmbUpdateSet.getSelectedItem().equals(NEW_SET)){
			txtOriginX.setText(MguiDouble.getString(0.0, "#0.000"));
			txtOriginY.setText(MguiDouble.getString(0.0, "#0.000"));
			txtOriginZ.setText(MguiDouble.getString(0.0, "#0.000"));
			
			txtAxisX_X.setText(MguiDouble.getString(1.0, "#0.000"));
			txtAxisX_Y.setText(MguiDouble.getString(0.0, "#0.000"));
			txtAxisX_Z.setText(MguiDouble.getString(0.0, "#0.000"));
			
			txtAxisY_X.setText(MguiDouble.getString(0.0, "#0.000"));
			txtAxisY_Y.setText(MguiDouble.getString(1.0, "#0.000"));
			txtAxisY_Z.setText(MguiDouble.getString(0.0, "#0.000"));
			
			Vector3f normal = new Vector3f(0.0f, 0.0f, 1.0f);
			setAxisZ(normal);
			
			txtUpdateSpacing.setText(MguiDouble.getString(1.0, "#0.000"));
			txtUpdateName.setText("New Section Set");
			
			chkAxisZ_flip.setSelected(false);
			cmdUpdate.setText("Create");
			
			this.updateUI();
			
			return;
			}
		
		if (updateSet == null) return;
		
		cmbUpdateShapeSet.setSelectedItem(updateSet.getParentSet());
		Plane3D plane = updateSet.getRefPlane();
		chkAxisZ_flip.setSelected(plane.flip_normal);
		
		txtOriginX.setText(MguiDouble.getString(plane.origin.x, "#0.000"));
		txtOriginY.setText(MguiDouble.getString(plane.origin.y, "#0.000"));
		txtOriginZ.setText(MguiDouble.getString(plane.origin.z, "#0.000"));
		
		txtAxisX_X.setText(MguiDouble.getString(plane.xAxis.x, "#0.000"));
		txtAxisX_Y.setText(MguiDouble.getString(plane.xAxis.y, "#0.000"));
		txtAxisX_Z.setText(MguiDouble.getString(plane.xAxis.z, "#0.000"));
		
		txtAxisY_X.setText(MguiDouble.getString(plane.yAxis.x, "#0.000"));
		txtAxisY_Y.setText(MguiDouble.getString(plane.yAxis.y, "#0.000"));
		txtAxisY_Z.setText(MguiDouble.getString(plane.yAxis.z, "#0.000"));
		
		setAxisZ(plane.getNormal());
		
		txtUpdateSpacing.setText(MguiDouble.getString(updateSet.getSpacing(), "#0.000"));
		txtUpdateName.setText(updateSet.getName());
		
		cmdUpdate.setText("Update");
		
		this.updateUI();
		
	}
	
	@Override
	public void graphicUpdated(GraphicEvent e) {
		// TODO Auto-generated method stub
		
		switch (e.getType()){
			case Modified:
				InterfaceGraphic2D window = (InterfaceGraphic2D)e.getGraphic();
				InterfaceGraphicWindow selected_window = (InterfaceGraphicWindow)cmbSelectedWindow.getSelectedItem();
				if (window.getParent() != selected_window) return;
				txtCurrentSection.setText("" + window.getCurrentSection()); 	// Shouldn't fire an event
				return;
			default:
			}
		
	}

	@Override
	public void graphicSourceChanged(GraphicEvent e) {
		
		updateSectionSetList(); 
		updateSectionAttributes();
		
	}
	
	@Override
	public void windowUpdated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowSourceChanged(WindowEvent e) {
		
		if (e.getSource() == currentWindow){
			InterfaceGraphicWindow window = (InterfaceGraphicWindow)e.getSource();
			if (window.getPanel() instanceof InterfaceGraphic2D){
				// This window no longer valid, update everything
				setCurrentWindow(null);
				updateLists();
				return;
				}
			}
		updateSectionSetList(); 
		updateSectionAttributes();
		
	}
	
	protected void setCurrentWindow(InterfaceGraphicWindow window){
		if (window == currentWindow) return; 
		if (currentWindow != null && currentWindow.getPanel() != null){
			currentWindow.removeWindowListener(this);
			currentWindow.getPanel().removeGraphicListener(this);
			}
		currentWindow = window;
		if (currentWindow != null && currentWindow.getPanel() != null){
			currentWindow.addWindowListener(this);
			currentWindow.getPanel().addGraphicListener(this);
			}
		
		updateSectionSetList(); 
		updateSectionAttributes();
	}
	
	public void itemStateChanged(ItemEvent e){
		if (!blnItemListen) return;
		//InterfaceSession.log("Item changed...");
		//window change event
		if (e.getStateChange() == ItemEvent.SELECTED && e.getSource().equals(cmbSelectedWindow)){
			if (((InterfaceGraphicWindow)e.getItem()).getPanel().equals(currentWindow)) return; 
			setCurrentWindow((InterfaceGraphicWindow)e.getItem());
			if (currentWindow != null)
				currentWindow.removeWindowListener(this);
			currentWindow = (InterfaceGraphicWindow)e.getItem();
			currentWindow.addWindowListener(this);
			
			updateSectionSetList(); 
			updateSectionAttributes();
			//setCurrentSection(((InterfaceGraphic2D)currentWindow.getPanel()).getCurrentSection());
			return;
		}
		
		//section set change event
		if (e.getStateChange() == ItemEvent.SELECTED && e.getSource().equals(cmbSectionSet)){
			blnItemListen = false;
			if (e.getItem() == null || e.getItem().equals("~")){
				currentSet = null;
				updateSectionAttributes();
				
				return;
				}
			currentSet = (SectionSet3DInt)e.getItem();
			InterfaceGraphic2D p = (InterfaceGraphic2D)currentWindow.getPanel();
			p.setSource(currentSet);
			if (currentSet != null && attrSetDetails != null)
				attrSetDetails.setAttributes(currentSet.attributes);
			updateSectionAttributes();
			setCurrentSection(p.getCurrentSection());
			blnItemListen = true;
			return;
		}
		
		if (e.getStateChange() == ItemEvent.SELECTED && e.getSource().equals(cmbUpdateSet)){
			if (cmbUpdateSet.getSelectedItem().equals(NEW_SET))
				updateSet = null;
			else
				updateSet = (SectionSet3DInt)cmbUpdateSet.getSelectedItem();
			updateUpdateSet();
		}
		
	}
	

	
	public void actionPerformed(ActionEvent e){
		//next section
		if (e.getActionCommand().equals(CMD_NEXT)){
			setCurrentSection(Integer.valueOf(txtCurrentSection.getText()).intValue() + 1);
			updateDisplay();
			return;
		}
		//prev section
		if (e.getActionCommand().equals(CMD_PREV)){
			setCurrentSection(Integer.valueOf(txtCurrentSection.getText()).intValue() - 1);
			updateDisplay();
			return;
		}
		//ref section
		if (e.getActionCommand().equals(CMD_REF)){
			setCurrentSection(Integer.valueOf(0));
			updateDisplay();
			return;
		}
		
		if (e.getActionCommand().equals(TXT_SECT)){
			if (txtCurrentSection.getText().equals("~")) return;
			setCurrentSection(Integer.valueOf(txtCurrentSection.getText()).intValue());
			currentWindow.getPanel().updateDisplay();
			updateDisplay();
			return;
		}
		
		if (e.getActionCommand().equals(CMD_UPDATE)){
			//if update
			if (cmdUpdate.getText().equals("Create"))
				updateSet = null;
			if (updateSet != null){
				updateSet.setName(txtUpdateName.getText());
				updateSet.setSpacing(Float.valueOf(txtUpdateSpacing.getText()).floatValue());
				Plane3D p = updateSet.getRefPlane();
				p.flip_normal = chkAxisZ_flip.isSelected();
				p.origin.x = Float.valueOf(txtOriginX.getText()).floatValue();
				p.origin.y = Float.valueOf(txtOriginY.getText()).floatValue();
				p.origin.z = Float.valueOf(txtOriginZ.getText()).floatValue();
				p.xAxis.x = Float.valueOf(txtAxisX_X.getText()).floatValue();
				p.xAxis.y = Float.valueOf(txtAxisX_Y.getText()).floatValue();
				p.xAxis.z = Float.valueOf(txtAxisX_Z.getText()).floatValue();
				p.yAxis.x = Float.valueOf(txtAxisY_X.getText()).floatValue();
				p.yAxis.y = Float.valueOf(txtAxisY_Y.getText()).floatValue();
				p.yAxis.z = Float.valueOf(txtAxisY_Z.getText()).floatValue();
				
				updateSet.setScene3DObject();
				updateSet.updateShape();
				updateSet.fireShapeModified();
				InterfaceSession.log("Section set '" + updateSet.getName() + "' updated.");
				
				return;
				}
			
			ShapeSet3DInt shape_set = (ShapeSet3DInt)cmbUpdateShapeSet.getSelectedItem();
			if (shape_set == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "No shape set selected!", 
											  "Create Section Set", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
				
			//otherwise create new
			updateSet = new SectionSet3DInt();
			try{
				updateSet.setName(txtUpdateName.getText());
				updateSet.setSpacing(Float.valueOf(txtUpdateSpacing.getText()));
				Plane3D p = updateSet.getRefPlane();
				p.flip_normal = chkAxisZ_flip.isSelected();
				p.origin.x = Float.valueOf(txtOriginX.getText()).floatValue();
				p.origin.y = Float.valueOf(txtOriginY.getText()).floatValue();
				p.origin.z = Float.valueOf(txtOriginZ.getText()).floatValue();
				p.xAxis.x = Float.valueOf(txtAxisX_X.getText()).floatValue();
				p.xAxis.y = Float.valueOf(txtAxisX_Y.getText()).floatValue();
				p.xAxis.z = Float.valueOf(txtAxisX_Z.getText()).floatValue();
				p.yAxis.x = Float.valueOf(txtAxisY_X.getText()).floatValue();
				p.yAxis.y = Float.valueOf(txtAxisY_Y.getText()).floatValue();
				p.yAxis.z = Float.valueOf(txtAxisY_Z.getText()).floatValue();
			}catch (NumberFormatException ex){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Number format error! Check your values..", 
											  "Create new section set", JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			InterfaceSession.log("Section set '" + updateSet.getName() + "' created.");
			
			//displayPanel.addShapeInt(updateSet);
			shape_set.addShape(updateSet);
			updateSectionSetList();
			updateUpdateSet();
			return;
		}
		
		if (e.getActionCommand().equals(CMD_ROTX)){
			Transform3D t = new Transform3D();
			t.rotX(Math.toRadians(Double.valueOf(txtRotation.getText()).doubleValue()));
			transformAxes(t);
			this.updateUI();
			return;
		}
		
		if (e.getActionCommand().equals(CMD_ROTY)){
			Transform3D t = new Transform3D();
			t.rotY(Math.toRadians(Double.valueOf(txtRotation.getText()).doubleValue()));
			transformAxes(t);
			this.updateUI();
			return;
		}
		
		if (e.getActionCommand().equals(CMD_ROTZ)){
			Transform3D t = new Transform3D();
			t.rotZ(Math.toRadians(Double.valueOf(txtRotation.getText()).doubleValue()));
			transformAxes(t);
			this.updateUI();
			return;
		}
		
		if (e.getActionCommand().startsWith("Axis")){
			
			if (e.getActionCommand().contains("Changed")){
				updateAxisZ();
				}
			
			if (e.getActionCommand().contains("Flip")){
				updateAxisZ();
				}
			
			return;
		}
				
		
		//next subsection
		//prev subsection
		//ref subsection
	}
	
	void updateAxisZ(){
		
		Vector3f x_axis = getAxisX();
		Vector3f y_axis = getAxisY();
		
		Vector3f normal = new Vector3f();
		normal.cross(x_axis, y_axis);
		
		if (chkAxisZ_flip.isSelected())
			normal.scale(-1);
		
		setAxisZ(normal);
	}
	
	Vector3f getAxisX(){
		
		return new Vector3f(MguiFloat.getValue(txtAxisX_X.getText()),
					  	    MguiFloat.getValue(txtAxisX_Y.getText()),
					  	  	MguiFloat.getValue(txtAxisX_Z.getText()));
				
	}
	
	Vector3f getAxisY(){
		
		return new Vector3f(MguiFloat.getValue(txtAxisY_X.getText()),
					  	    MguiFloat.getValue(txtAxisY_Y.getText()),
					  	    MguiFloat.getValue(txtAxisY_Z.getText()));
		
	}
	
	void setAxisZ(Vector3f v){
		
		txtAxisZ_X.setText("" + v.x);
		txtAxisZ_Y.setText("" + v.y);
		txtAxisZ_Z.setText("" + v.z);
		
	}
	
	private void transformAxes(Transform3D t){
		Vector3f xAxis = new Vector3f(Float.valueOf(txtAxisX_X.getText()).floatValue(),
									  Float.valueOf(txtAxisX_Y.getText()).floatValue(),
									  Float.valueOf(txtAxisX_Z.getText()).floatValue());
		Vector3f yAxis = new Vector3f(Float.valueOf(txtAxisY_X.getText()).floatValue(),
									  Float.valueOf(txtAxisY_Y.getText()).floatValue(),
									  Float.valueOf(txtAxisY_Z.getText()).floatValue());
		t.transform(xAxis);
		t.transform(yAxis);
		txtAxisX_X.setText(MguiDouble.getString(xAxis.x, "##0.000"));
		txtAxisX_Y.setText(MguiDouble.getString(xAxis.y, "##0.000"));
		txtAxisX_Z.setText(MguiDouble.getString(xAxis.z, "##0.000"));
		txtAxisY_X.setText(MguiDouble.getString(yAxis.x, "##0.000"));
		txtAxisY_Y.setText(MguiDouble.getString(yAxis.y, "##0.000"));
		txtAxisY_Z.setText(MguiDouble.getString(yAxis.z, "##0.000"));
	}
	
	public void shapeUpdated(ShapeEvent e){
		if (e.getSource() instanceof ShapeSet3DInt)
			updateDisplay();
	}
	
	public String toString(){
		return "Section Set Panel";
	}
	
	
}