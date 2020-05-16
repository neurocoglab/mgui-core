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

package mgui.morph.sections;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceHideablePanel;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


public class InterfaceMorphPanel2 extends InterfacePanel implements	ActionListener {

	public ShapeSet3DInt shapeSet;
	public SectionSet3DInt currentSet;
	public InterfaceDisplayPanel displayPanel;

	CategoryTitle lblTitle = new CategoryTitle("MORPH POLYGONS PANEL");
	JLabel lblCurrentSet = new JLabel("Current set:"); 
	JTextField txtCurrentSet2 = new JTextField(""); 
	JComboBox cmbCurrentSet = new JComboBox();
	CategoryTitle lblSectionHeader = new CategoryTitle("SECTIONS");
	JCheckBox chkUseAllSections = new JCheckBox();
	JLabel lblUseAllSections = new JLabel("Use all with shapes"); 
	JCheckBox chkUseSpecSections = new JCheckBox();
	JLabel lblUseSpecSections = new JLabel("Use sections");
	JTextField txtUseSectFrom = new JTextField("0"); 
	JLabel lblUseSpecSections2 = new JLabel("to");
	JTextField txtUseSectTo = new JTextField("1");
	CategoryTitle lblShapeHeader = new CategoryTitle("SHAPES");
	JCheckBox chkUseAllShapes = new JCheckBox("Use all shapes");
	//JLabel lblUseAllShapes = new JLabel("Use all shapes"); 
	JCheckBox chkUseSelSet = new JCheckBox("Use current selection set");
	//JLabel lblUseSelSet = new JLabel("Use current selection set");
	JCheckBox chkUseAllSelSet = new JCheckBox("Use all selection sets");
	JComboBox cmbSelSet = new JComboBox();
	CategoryTitle lblParamHeader = new CategoryTitle("PARAMETERS");
	JLabel lblIterations = new JLabel("No. iterations");
	JTextField txtIterations = new JTextField("1");
	JCheckBox chkSpline = new JCheckBox(" Apply spline");
	//JLabel lblSpline = new JLabel("Apply spline"); 
	JCheckBox chkSurface = new JCheckBox(" Generate surface");
	//JLabel lblSurface = new JLabel("Generate surface");
	JCheckBox chkAddPolylines = new JCheckBox(" Generate polygons");
	JCheckBox chkSurfaceConst = new JCheckBox(" Generate constraints");
	JCheckBox chkMapMulti = new JCheckBox(" Handle branching");
	//JLabel lblSurfaceConst = new JLabel("Generate constraints");
	JLabel lblSplineFactor = new JLabel("Endpoint spline factor");
	JTextField txtSplineFactor = new JTextField("2");
	JLabel lblMinAngle = new JLabel("Min. node angle (deg)");
	JTextField txtMinAngle = new JTextField("135");
	JCheckBox chkCornerPts = new JCheckBox("Detect corner points");
	JLabel lblLenThreshold = new JLabel("Length threshold:");
	JTextField txtLenThreshold = new JTextField("0.1");
	JLabel lblAngleThreshold = new JLabel("Angle threshold (deg):");
	JTextField txtAngleThreshold = new JTextField("105");
	CategoryTitle lblOutputHeader = new CategoryTitle("OUTPUT SECTIONS");
	JLabel lblShapeName = new JLabel("Morph Set Name:");
	JTextField txtShapeName = new JTextField("Morph Set 1");
	JCheckBox chkLabelNodes = new JCheckBox("Label nodes");
	JLabel lblShapeAttr = new JLabel("Attributes:");
	CategoryTitle lblMeshHeader = new CategoryTitle("OUTPUT MESH");
	JLabel lblMeshAttr = new JLabel("Attributes2:");
	
	
	//MorphSections3DInt attrSet = new MorphSections3DInt();
	SectionSet3DInt attrSet = new SectionSet3DInt();
	//Mesh3DInt attrMesh = new Mesh3DInt();
	AttributeList meshAttr;
	
	InterfaceAttributePanel attrSetPanel = new InterfaceAttributePanel(attrSet);
	InterfaceHideablePanel ihpAttrSet = new InterfaceHideablePanel(attrSetPanel);
	InterfaceAttributePanel attrMeshPanel; // = new InterfaceAttributePanel(attrMesh);
	InterfaceHideablePanel ihpAttrMesh; // = new InterfaceHideablePanel(attrMeshPanel);
	
	CategoryTitle lblExecuteHeader = new CategoryTitle("EXECUTE");
	
	JButton cmdExecute = new JButton("Create subsections");
	JButton cmdCorners = new JButton("Highlight corner pts");
	
	public static final String CMD_EXECUTE = "Create Subsections";
	public static final String CHK_ALLSECT = "Use All Sections";
	public static final String CHK_SPECSECT = "Use Specific Sections";
	public static final String CHK_ALLSHAPES = "Use All Shapes";
	public static final String CHK_SELSET = "Use Selection Set";
	public static final String CHK_ALLSELSET = "Use All Selection Sets";
	public static final String CMD_CORNERS = "Highlight Corner Points";
	
	public InterfaceMorphPanel2(){
		if (InterfaceSession.isInit())
			init();
	}
	
	/*
	public InterfaceMorphPanel2(InterfaceDisplayPanel p){
		displayPanel = p;
		//currentSet = (SectionSet3DInt)p.attributes.getValue("CurrentSectionSet");
		shapeSet = p.modelSet;
		init();
	}
	*/
	
	protected void init(){
		setLayout(new CategoryLayout(20, 5, 200, 10));
		CategoryLayoutConstraints c;
		
		Mesh3DInt attrMesh = new Mesh3DInt();
		meshAttr = (AttributeList)attrMesh.getAttributes().clone();
		attrMeshPanel = new InterfaceAttributePanel(meshAttr);
		ihpAttrMesh = new InterfaceHideablePanel(attrMeshPanel);
		
		updateSectionSetList();
		
		lblTitle.setHorizontalAlignment(JLabel.CENTER);
		lblSectionHeader.setHorizontalAlignment(JLabel.CENTER);
		lblShapeHeader.setHorizontalAlignment(JLabel.CENTER);
		lblParamHeader.setHorizontalAlignment(JLabel.CENTER);
		lblOutputHeader.setHorizontalAlignment(JLabel.CENTER);
		lblMeshHeader.setHorizontalAlignment(JLabel.CENTER);
		
		chkUseAllSections.setSelected(true);
		chkUseAllShapes.setSelected(true);
		
		cmdExecute.setActionCommand(CMD_EXECUTE);
		cmdExecute.addActionListener(this);
		chkUseAllSections.setActionCommand(CHK_ALLSECT);
		chkUseAllSections.addActionListener(this);
		chkUseSpecSections.setActionCommand(CHK_SPECSECT);
		chkUseSpecSections.addActionListener(this);
		chkUseAllShapes.setActionCommand(CHK_ALLSHAPES);
		chkUseAllShapes.addActionListener(this);
		chkUseSelSet.setActionCommand(CHK_SELSET);
		chkUseSelSet.addActionListener(this);
		chkUseAllSelSet.setActionCommand(CHK_ALLSELSET);
		chkUseAllSelSet.addActionListener(this);
		cmdCorners.setActionCommand(CMD_CORNERS);
		cmdCorners.addActionListener(this);
		
		c = new CategoryLayoutConstraints();
		add(lblTitle, c);
		lblTitle.setParentObj(this);
		c = new CategoryLayoutConstraints("MORPH POLYGONS PANEL", 1, 1, 0, 1, 1);
		add(lblCurrentSet, c);
		c = new CategoryLayoutConstraints("MORPH POLYGONS PANEL", 2, 2, 0, 1, 1);
		add(cmbCurrentSet, c);
		c = new CategoryLayoutConstraints();
		add(lblSectionHeader, c);
		lblSectionHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("SECTIONS", 1, 1, 0, 0.15, 1);
		add(chkUseAllSections, c);
		c = new CategoryLayoutConstraints("SECTIONS", 1, 1, 0.15, 0.85, 1);
		add(lblUseAllSections, c);
		c = new CategoryLayoutConstraints("SECTIONS", 2, 2, 0, 0.15, 1);
		add(chkUseSpecSections, c);
		c = new CategoryLayoutConstraints("SECTIONS", 2, 2, 0.15, 0.40, 1);
		add(lblUseSpecSections, c);
		c = new CategoryLayoutConstraints("SECTIONS", 2, 2, 0.55, 0.175, 1);
		add(txtUseSectFrom, c);
		c = new CategoryLayoutConstraints("SECTIONS", 2, 2, 0.75, 0.15, 1);
		add(lblUseSpecSections2, c);
		c = new CategoryLayoutConstraints("SECTIONS", 2, 2, 0.825, 0.175, 1);
		add(txtUseSectTo, c);
		c = new CategoryLayoutConstraints();
		add(lblShapeHeader, c);
		lblShapeHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("SHAPES", 1, 1, 0, 0.9, 1);
		add(chkUseAllShapes, c);
		c = new CategoryLayoutConstraints("SHAPES", 2, 2, 0, 0.9, 1);
		add(chkUseSelSet, c);
		c = new CategoryLayoutConstraints("SHAPES", 3, 3, 0, 0.9, 1);
		add(chkUseAllSelSet, c);
		
		c = new CategoryLayoutConstraints();
		add(lblParamHeader, c);
		lblParamHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("PARAMETERS", 1, 1, 0, 0.7, 1);
		add(lblIterations, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 1, 1, 0.7, 0.3, 1);
		add(txtIterations, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 2, 2, 0, 0.8, 1);
		add(chkSpline, c);
		//c = new CategoryLayoutConstraints("PARAMETERS", 2, 2, 0.15, 0.85, 1);
		//add(lblSpline, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 3, 3, 0, 0.8, 1);
		add(chkSurface, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 4, 4, 0, 0.8, 1);
		add(chkAddPolylines, c);
		//c = new CategoryLayoutConstraints("PARAMETERS", 3, 3, 0.15, 0.85, 1);
		//add(lblSurface, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 5, 5, 0, 0.8, 1);
		add(chkSurfaceConst, c);
		//c = new CategoryLayoutConstraints("PARAMETERS", 5, 5, 0.15, 0.85, 1);
		//add(lblSurfaceConst, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 6, 6, 0, 0.7, 1);
		add(lblSplineFactor, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 6, 6, 0.7, 0.3, 1);
		add(txtSplineFactor, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 7, 7, 0, 0.7, 1);
		add(lblMinAngle, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 7, 7, 0.7, 0.3, 1);
		add(txtMinAngle, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 8, 8, 0, 0.8, 1);
		add(chkCornerPts, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 9, 9, 0, 0.7, 1);
		add(lblLenThreshold, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 9, 9, 0.7, 0.3, 1);
		add(txtLenThreshold, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 10, 10, 0, 0.7, 1);
		add(lblAngleThreshold, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 10, 10, 0.7, 0.3, 1);
		add(txtAngleThreshold, c);
		c = new CategoryLayoutConstraints("PARAMETERS", 11, 11, 0, 0.8, 1);
		add(chkMapMulti, c);
		
		c = new CategoryLayoutConstraints();
		add(lblOutputHeader, c);
		lblOutputHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("OUTPUT SECTIONS", 1, 1, 0, 0.5, 1);
		add(lblShapeName, c);
		c = new CategoryLayoutConstraints("OUTPUT SECTIONS", 1, 1, 0.55, 0.45, 1);
		add(txtShapeName, c);
		c = new CategoryLayoutConstraints("OUTPUT SECTIONS", 2, 2, 0, 1, 1);
		add(chkLabelNodes, c);
		c = new CategoryLayoutConstraints("OUTPUT SECTIONS", 3, 3, 0, 1, 1);
		add(lblShapeAttr, c);
		c = new CategoryLayoutConstraints("OUTPUT SECTIONS", 4, 10, 0, 1, 1);
		add(ihpAttrSet, c);
		
		c = new CategoryLayoutConstraints();
		add(lblMeshHeader, c);
		lblMeshHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("OUTPUT MESH", 1, 7, 0, 1, 1);
		add(ihpAttrMesh, c);
		lblMeshHeader.isExpanded = false;
		
		c = new CategoryLayoutConstraints();
		add(lblExecuteHeader, c);
		lblExecuteHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("EXECUTE", 1, 2, 0.10, 0.8, 1);
		add(cmdExecute, c);
		c = new CategoryLayoutConstraints("EXECUTE", 3, 4, 0.10, 0.8, 1);
		add(cmdCorners, c);
		
		if (currentSet != null)
			txtCurrentSet2.setText(currentSet.getName());
		else
			txtCurrentSet2.setText("None");
		txtCurrentSet2.setEditable(false);
	
		updateDisplay();
	}
	
	private void updateSectionSetList(){
		cmbCurrentSet.removeAllItems();
		if (shapeSet == null) return;
		
		List<Shape3DInt> section_sets = shapeSet.getShapeType(new SectionSet3DInt());
		
		boolean blnFound = false;
		for (Shape3DInt set : section_sets) {
			cmbCurrentSet.addItem(set);
			if (currentSet != null)
				if (set.equals(currentSet)) 
					blnFound = true;
			}
		if (blnFound)
			cmbCurrentSet.setSelectedItem(currentSet);
		else
			if (cmbCurrentSet.getSelectedItem() != null)
				currentSet = (SectionSet3DInt)cmbCurrentSet.getSelectedItem();
	}
	
	public void showPanel(){
		updateSectionSetList();
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (CMD_EXECUTE.equals(e.getActionCommand())){
			
			//displayPanel.setUpdateable(false);
			MorphEngine morphEngine = new MorphEngine();
			morphEngine.morphAttr.setValue("NoIterations", 
										   new MguiInteger(Integer.valueOf(txtIterations.getText())));
			
			morphEngine.morphAttr.setValue("EndSplineFactor", 
					   new MguiDouble(Double.valueOf(txtSplineFactor.getText())));
			
			morphEngine.morphAttr.setValue("ApplySpline", new MguiBoolean(chkSpline.isSelected()));
			morphEngine.morphAttr.setValue("GenerateSurface", new MguiBoolean(chkSurface.isSelected()));
			morphEngine.morphAttr.setValue("ShapeName", txtShapeName.getText());
			morphEngine.morphAttr.setValue("UseSelectionSet", new MguiBoolean(chkUseSelSet.isSelected()));
			if (chkUseSelSet.isSelected())
				morphEngine.morphAttr.setValue("SelectionSet", displayPanel.getCurrentSelection());
			morphEngine.morphAttr.setValue("MinAngle", new MguiDouble(
					GeometryFunctions.getDegToRad(Double.valueOf(txtMinAngle.getText()))));
			
			morphEngine.morphAttr.setValue("LengthThreshold", 
					new MguiDouble(Double.valueOf(txtLenThreshold.getText()).doubleValue()));
			morphEngine.morphAttr.setValue("AngleThreshold", 
					new MguiDouble(Double.valueOf(txtAngleThreshold.getText()).doubleValue()));
			morphEngine.morphAttr.setValue("MapMulti", new MguiBoolean(chkMapMulti.isSelected()));
			
			//MorphSections3DInt morphSections, thisSections;
			SectionSet3DInt morphSections, thisSections;
			Mesh3DInt thisMesh;
			ShapeSet3DInt meshSet = new ShapeSet3DInt();
			boolean blnUseAllSelSet = chkUseAllSelSet.isSelected();
			ShapeModel3D model = currentSet.getModel();
			AttributeList attr = attrMeshPanel.attrModel.attributes;
			boolean blnAddPolylines = chkAddPolylines.isSelected();
			boolean blnSetMesh = chkSurface.isSelected();
			
			//generate subsections
			//if using all selection sets, iterate here
			if (blnUseAllSelSet && model.selections.size() > 0){
				System.out.print("\nProcessing set: " + model.selections.get(0).getName());
				((MguiBoolean)morphEngine.morphAttr.getValue("UseSelectionSet")).setTrue(true);
				morphEngine.morphAttr.setValue("SelectionSet", model.selections.get(0));
				//attr.setValue("FillColour", 
				//			  model.selections.get(0).shapes.get(0).getAttribute("LineColour"));
				}
			morphSections = morphEngine.getMorphSections(currentSet, 0.8);
			attrSet.setSpacing(morphSections.getSpacing());
			morphSections.getAttributes().setIntersection(attrSetPanel.attrModel.attributes);
			
			if (blnSetMesh && morphSections != null){
				thisMesh = morphEngine.getMorphMesh(morphSections, 0.8);
				if (thisMesh != null){
					thisMesh.getAttributes().setIntersection(attr);
					if (chkSurfaceConst.isSelected()){
						ShapeFunctions.setMeshConstraints(thisMesh, 
														  morphSections.getVertices());
						}
					meshSet.addShape(thisMesh, true, true);
					if (blnUseAllSelSet){
						//add mesh to selection set
						model.selections.get(0).addShape(thisMesh);
						}
					}
				}
			
			if (blnUseAllSelSet && morphSections != null){
				for (int i = 1; i < model.selections.size(); i++){
					while (i < model.selections.size() && 
						   model.selections.get(i).shapes.size() == 0) i++;
					//attr.setValue("FillColour", 
					//		  	  model.selections.get(i).shapes.get(0).getAttribute("LineColour"));
					morphEngine.morphAttr.setValue("SelectionSet", model.selections.get(i));
					System.out.print("\nProcessing set: " + model.selections.get(i).getName());
					thisSections = morphEngine.getMorphSections(currentSet, 0.8);
					if (blnAddPolylines)
						morphSections.addUnionSet(thisSections, true);
					
					if (blnSetMesh && thisSections.hasSections()){
						thisMesh = morphEngine.getMorphMesh(thisSections, 0.8);
						if (thisMesh != null){
							thisMesh.getAttributes().setIntersection(attr);
							meshSet.addShape(thisMesh, true, true);
							model.selections.get(i).addShape(thisMesh);
							}
						}
					}
				}
			
			if (morphSections != null){
				((MguiBoolean)morphSections.shapeAttr.getValue("LabelNodes")).setTrue(chkLabelNodes.isSelected());
				morphSections.updateLightShapes();
				
				//displayPanel.setUpdateable(true);
				if (blnAddPolylines)
					displayPanel.addShapeInt(morphSections);
				if (blnSetMesh)
					InterfaceSession.getDisplayPanel().getCurrentShapeSet().addUnionSet(meshSet, true);
				}
			displayPanel.updateDisplays();
			
		}
		
		//switch checkboxes
		if (CHK_ALLSECT.equals(e.getActionCommand()))
			chkUseSpecSections.setSelected(!chkUseAllSections.isSelected());
		if (CHK_SPECSECT.equals(e.getActionCommand()))
			chkUseAllSections.setSelected(!chkUseSpecSections.isSelected());
		if (CHK_ALLSHAPES.equals(e.getActionCommand()))
			if (chkUseAllShapes.isSelected()){
			chkUseSelSet.setSelected(false);
			chkUseAllSelSet.setSelected(false);
			}else{
				chkUseSelSet.setSelected(!chkUseAllSelSet.isSelected());
			}
		if (CHK_SELSET.equals(e.getActionCommand()))
			if (chkUseSelSet.isSelected()){
			chkUseAllSelSet.setSelected(false);
			chkUseAllShapes.setSelected(false);
			}else{
				chkUseAllShapes.setSelected(!chkUseAllSelSet.isSelected());
			}
		if (CHK_ALLSELSET.equals(e.getActionCommand()))
			if (chkUseAllSelSet.isSelected()){
			chkUseSelSet.setSelected(false);
			chkUseAllShapes.setSelected(false);
			}else{
				chkUseAllShapes.setSelected(!chkUseSelSet.isSelected());
			}
		if (CMD_CORNERS.equals(e.getActionCommand())){
			//get corner points for all polygons
			//add BoolPolygon2DInt objects to layers
			SectionSet3DInt thisSet = displayPanel.getCurrentSectionSet();
			ShapeSet2DInt polySet;
			ShapeSet2DInt shape2DSet;
			Polygon2DInt thisPoly;
			double length_threshold = Double.valueOf(txtLenThreshold.getText());
			double angle_threshold = GeometryFunctions.getDegToRad(Double.valueOf(txtAngleThreshold.getText()));
			
			Iterator<ShapeSet2DInt> itr = thisSet.sections.values().iterator();
			while (itr.hasNext()){
				shape2DSet = itr.next();
				polySet = shape2DSet.getShapeType(new Polygon2DInt());
				for (int j = 0; j < polySet.getSize(); j++){
					thisPoly = (Polygon2DInt)polySet.getMember(j);
					shape2DSet.addShape(ShapeFunctions.getBoolCornerPoints(thisPoly,
																	   length_threshold, 
																	   angle_threshold),
																	   true,
																	   true);
				}
				
			}
			
			displayPanel.updateDisplays();
		}
		
	}
	
	
	public String toString(){
		return "Morph Polygons Panel";
	}
}