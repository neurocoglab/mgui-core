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

package mgui.morph.sections;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.jogamp.vecmath.Point2f;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.IntPolygon2DInt;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeFunctions;


public class InterfaceRadialRep2 extends InterfacePanel implements ActionListener {

	public InterfaceDisplayPanel displayPanel; 
	public ShapeSet3DInt shapeSet;
	public SectionSet3DInt currentSet;
	
	//data
	RadialRep2DInt currentRep;
	AttributeList repAttr;
	
	//controls
	CategoryTitle lblTitle = new CategoryTitle("RADIAL REP PANEL");
	JLabel lblCurrentSet = new JLabel("Current set:"); 
	JComboBox cmbSectionSet = new JComboBox();
	JLabel lblDisplayStle = new JLabel("Display Style:"); 
	JComboBox cmbDisplayStle = new JComboBox();
	CategoryTitle lblCircleHeader = new CategoryTitle("CIRCLE");
	JLabel lblCenterHeader = new JLabel("Center Point:");
	JCheckBox chkUseCenterPt = new JCheckBox();
	JLabel lblUseCenterPt = new JLabel("Polygon center pt"); 
	JCheckBox chkUseFixedPt = new JCheckBox();
	JLabel lblUseFixedPt = new JLabel("Fixed pt:  x"); 
	JTextField txtFixedPtX = new JTextField("0.0"); 
	JLabel lblUseFixedPt2 = new JLabel(" y");
	JTextField txtFixedPtY = new JTextField("0.0");
	JCheckBox chkUseOffset = new JCheckBox();
	JLabel lblUseOffset = new JLabel("Offset:     x"); 
	JTextField txtOffsetX = new JTextField("0.0"); 
	JLabel lblUseOffset2 = new JLabel(" y");
	JTextField txtOffsetY = new JTextField("0.0");
	JLabel lblRadiusHeader = new JLabel("Radius:");
	JCheckBox chkMaxRadius = new JCheckBox();
	JLabel lblMaxRadius = new JLabel("Max radius length");
	JCheckBox chkFixedRadius = new JCheckBox();
	JLabel lblFixedRadius = new JLabel("Fixed radius length");
	JTextField txtFixedRadius = new JTextField("1.0");
	CategoryTitle lblControlHeader = new CategoryTitle("CONTROL POINTS");
	JCheckBox chkUseControls = new JCheckBox("Use control points");
	JLabel lblCornerPts = new JLabel("Corner points detection:");
	JLabel lblLenThreshold = new JLabel("Length threshold:");
	JTextField txtLenThreshold = new JTextField("1.6");
	JLabel lblAngleThreshold = new JLabel("Angle threshold (deg):");
	JTextField txtAngleThreshold = new JTextField("105");
	JCheckBox chkOverwrite = new JCheckBox("Overwrite existing");
	JButton cmdGenCornerPts = new JButton("Generate");
	JButton cmdAssignCornerPts = new JButton("Assign w RadRep");
	CategoryTitle lblGroomHeader = new CategoryTitle("LINE GROOMING");
	JCheckBox chkGroomMinLen = new JCheckBox("Min segment length");
	JTextField txtGroomMinLen = new JTextField("2");
	JCheckBox chkGroomMaxLen = new JCheckBox("Max segment length");
	JTextField txtGroomMaxLen = new JTextField("20");
	JLabel lblGroomMinAngle = new JLabel("Min rem angle (deg)");
	JTextField txtGroomMinAngle = new JTextField("160");
	JButton cmdGroomPolys = new JButton("Groom Polygons");
	CategoryTitle lblGenHeader = new CategoryTitle("GENERATE");
	JLabel lblGenMultiple = new JLabel("Multiple sections:");
	JCheckBox chkGenCurrentSet = new JCheckBox();
	JLabel lblGenCurrentSet = new JLabel("Use current section set");
	JCheckBox chkGenSelSet = new JCheckBox();
	JLabel lblGenSelSet = new JLabel("Use selection set");
	CategoryTitle lblExecute = new CategoryTitle("EXECUTE");
	JButton cmdGenSel = new JButton("Individual");
	JButton cmdGenMulti = new JButton("Multiple");
	
	//constants
	public final String CMD_GEN_SEL = "Generate Selected";
	public final String CMD_GEN_MULTI = "Generate Multiple";
	public final String CMD_GEN_CORNERS = "Generate Corners";
	public final String CMD_ASSIGN_CORNERS = "Assign Corners";
	public final String CMD_GROOM_POLY = "Groom Polygons";
	
	public InterfaceRadialRep2(){
		init();
	}
	
	public InterfaceRadialRep2(InterfaceDisplayPanel p){
		displayPanel = p;
		currentSet = (SectionSet3DInt)p.getAttributes().getValue("CurrentSectionSet");
		//ShapeSet3DInt shapeSet = p.modelSet;
		init();
	}
	
	protected void init(){
		updateSectionSetList();
		
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		lblTitle.setHorizontalAlignment(JLabel.CENTER);
		lblCircleHeader.setHorizontalAlignment(JLabel.CENTER);
		lblGenHeader.setHorizontalAlignment(JLabel.CENTER);
		lblControlHeader.setHorizontalAlignment(JLabel.CENTER);
		lblGroomHeader.setHorizontalAlignment(JLabel.CENTER);
		
		chkUseOffset.setSelected(true);
		chkMaxRadius.setSelected(true);
		chkGenCurrentSet.setSelected(true);
		
		cmdGenSel.setActionCommand(CMD_GEN_SEL);
		cmdGenSel.addActionListener(this);
		cmdGenMulti.setActionCommand(CMD_GEN_MULTI);
		cmdGenMulti.addActionListener(this);
		cmdGenCornerPts.setActionCommand(CMD_GEN_CORNERS);
		cmdGenCornerPts.addActionListener(this);
		cmdAssignCornerPts.setActionCommand(CMD_ASSIGN_CORNERS);
		cmdAssignCornerPts.addActionListener(this);
		cmdGroomPolys.setActionCommand(CMD_GROOM_POLY);
		cmdGroomPolys.addActionListener(this);
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblTitle, c);
		lblTitle.setParentObj(this);
		c = new CategoryLayoutConstraints("RADIAL REP PANEL", 1, 1, 0, 1, 1);
		add(lblCurrentSet, c);
		c = new CategoryLayoutConstraints("RADIAL REP PANEL", 2, 2, 0, 1, 1);
		add(cmbSectionSet, c);
		c = new CategoryLayoutConstraints("RADIAL REP PANEL", 3, 3, 0, 1, 1);
		add(lblDisplayStle, c);
		c = new CategoryLayoutConstraints("RADIAL REP PANEL", 4, 4, 0, 1, 1);
		add(cmbDisplayStle, c);
		c = new CategoryLayoutConstraints();
		add(lblCircleHeader, c);
		lblCircleHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("CIRCLE", 1, 1, 0, 1, 1);
		add(lblCenterHeader, c);
		c = new CategoryLayoutConstraints("CIRCLE", 2, 2, 0, 0.15, 1);
		add(chkUseCenterPt, c);
		c = new CategoryLayoutConstraints("CIRCLE", 2, 2, 0.15, 0.85, 1);
		add(lblUseCenterPt, c);
		c = new CategoryLayoutConstraints("CIRCLE", 3, 3, 0, 0.15, 1);
		add(chkUseFixedPt, c);
		c = new CategoryLayoutConstraints("CIRCLE", 3, 3, 0.15, 0.40, 1);
		add(lblUseFixedPt, c);
		c = new CategoryLayoutConstraints("CIRCLE", 3, 3, 0.5, 0.175, 1);
		add(txtFixedPtX, c);
		c = new CategoryLayoutConstraints("CIRCLE", 3, 3, 0.75, 0.15, 1);
		add(lblUseFixedPt2, c);
		c = new CategoryLayoutConstraints("CIRCLE", 3, 3, 0.825, 0.175, 1);
		add(txtFixedPtY, c);
		c = new CategoryLayoutConstraints("CIRCLE", 4, 4, 0, 0.15, 1);
		add(chkUseOffset, c);
		c = new CategoryLayoutConstraints("CIRCLE", 4, 4, 0.15, 0.40, 1);
		add(lblUseOffset, c);
		c = new CategoryLayoutConstraints("CIRCLE", 4, 4, 0.5, 0.175, 1);
		add(txtOffsetX, c);
		c = new CategoryLayoutConstraints("CIRCLE", 4, 4, 0.75, 0.15, 1);
		add(lblUseOffset2, c);
		c = new CategoryLayoutConstraints("CIRCLE", 4, 4, 0.825, 0.175, 1);
		add(txtOffsetY, c);
		c = new CategoryLayoutConstraints("CIRCLE", 5, 5, 0, 1, 1);
		add(lblRadiusHeader, c);
		c = new CategoryLayoutConstraints("CIRCLE", 6, 6, 0, 0.15, 1);
		add(chkMaxRadius, c);
		c = new CategoryLayoutConstraints("CIRCLE", 6, 6, 0.15, 0.85, 1);
		add(lblMaxRadius, c);
		c = new CategoryLayoutConstraints("CIRCLE", 7, 7, 0, 0.15, 1);
		add(chkFixedRadius, c);
		c = new CategoryLayoutConstraints("CIRCLE", 7, 7, 0.15, 0.6, 1);
		add(lblFixedRadius, c);
		c = new CategoryLayoutConstraints("CIRCLE", 7, 7, 0.75, 0.25, 1);
		add(txtFixedRadius, c);
		c = new CategoryLayoutConstraints();
		add(lblControlHeader, c);
		lblControlHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 1, 1, 0, 1, 1);
		add(chkUseControls, c);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 2, 2, 0, 1, 1);
		add(lblCornerPts, c);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 3, 3, 0, 0.7, 1);
		add(lblLenThreshold, c);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 3, 3, 0.7, 0.3, 1);
		add(txtLenThreshold, c);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 4, 4, 0, 0.7, 1);
		add(lblAngleThreshold, c);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 4, 4, 0.7, 0.3, 1);
		add(txtAngleThreshold, c);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 5, 5, 0, 1, 1);
		add(chkOverwrite, c);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 6, 6, 0.1, 0.8, 1);
		add(cmdGenCornerPts, c);
		c = new CategoryLayoutConstraints("CONTROL POINTS", 7, 7, 0.1, 0.8, 1);
		add(cmdAssignCornerPts, c);
		c = new CategoryLayoutConstraints();
		add(lblGroomHeader, c);
		lblGroomHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("LINE GROOMING", 1, 1, 0, 0.7, 1);
		add(chkGroomMinLen, c);
		c = new CategoryLayoutConstraints("LINE GROOMING", 1, 1, 0.75, 0.25, 1);
		add(txtGroomMinLen, c);
		c = new CategoryLayoutConstraints("LINE GROOMING", 2, 2, 0, 0.7, 1);
		add(chkGroomMaxLen, c);
		c = new CategoryLayoutConstraints("LINE GROOMING", 2, 2, 0.75, 0.25, 1);
		add(txtGroomMaxLen, c);
		c = new CategoryLayoutConstraints("LINE GROOMING", 3, 3, 0, 0.7, 1);
		add(lblGroomMinAngle, c);
		c = new CategoryLayoutConstraints("LINE GROOMING", 3, 3, 0.75, 0.25, 1);
		add(txtGroomMinAngle, c);
		c = new CategoryLayoutConstraints("LINE GROOMING", 4, 4, 0.1, 0.8, 1);
		add(cmdGroomPolys, c);
		c = new CategoryLayoutConstraints();
		add(lblGenHeader, c);
		lblGenHeader.setParentObj(this);
		c = new CategoryLayoutConstraints("GENERATE", 1, 1, 0, 1, 1);
		add(lblGenMultiple, c);
		c = new CategoryLayoutConstraints("GENERATE", 2, 2, 0, 0.15, 1);
		add(chkGenCurrentSet, c);
		c = new CategoryLayoutConstraints("GENERATE", 2, 2, 0.15, 0.85, 1);
		add(lblGenCurrentSet, c);
		c = new CategoryLayoutConstraints("GENERATE", 3, 3, 0, 0.15, 1);
		add(chkGenSelSet, c);
		c = new CategoryLayoutConstraints("GENERATE", 3, 3, 0.15, 0.85, 1);
		add(lblGenSelSet, c);
		c = new CategoryLayoutConstraints();
		add(lblExecute, c);
		lblExecute.setParentObj(this);
		c = new CategoryLayoutConstraints("EXECUTE", 1, 2, 0.1, 0.8, 1);
		add(cmdGenSel, c);
		c = new CategoryLayoutConstraints("EXECUTE", 3, 4, 0.1, 0.8, 1);
		add(cmdGenMulti, c);
		
		
		//if (currentSet != null)
		//	txtCurrentSet2.setText(currentSet.name);
		//else
		//	txtCurrentSet2.setText("None");
		//txtCurrentSet2.setEditable(false);
	
		updateDisplay();
	}
	
	private void updateSectionSetList(){
		cmbSectionSet.removeAllItems();
		ShapeSet3DInt sectionSets = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new SectionSet3DInt());
		for (int i = 0; i < sectionSets.members.size(); i++)
			cmbSectionSet.addItem(sectionSets.members.get(i));
		if (currentSet != (SectionSet3DInt)cmbSectionSet.getSelectedItem())
			currentSet = (SectionSet3DInt)cmbSectionSet.getSelectedItem();
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (CMD_GEN_SEL.equals(e.getActionCommand())){
			//code to instantiate individual Radial Reps tool
			
			
		}
		
		if (CMD_GEN_MULTI.equals(e.getActionCommand())){
			//code to generate multiple Radial Reps
			//if use current set
			//compile all polygons in section set
			//for each
				//create radial rep based on parameters for:
				//center point
				//radius
			ShapeSet2DInt polySet;
			RadialRep2DInt thisRep;
			RadialRep2D repShape;
			Point2f thisCenter;
			//ValueMapItem thisItem;
			int thisSect;
			ShapeSet2DInt thisShapeSet;
			Iterator<Integer> itr = currentSet.sections.keySet().iterator();
			while (itr.hasNext()){
				thisSect = itr.next().intValue();
				thisShapeSet = currentSet.getShapeSet(thisSect);
				polySet = thisShapeSet.getShapeType(new Polygon2DInt());
				for (int j = 0; j < polySet.getSize(); j++) {
					thisRep = new RadialRep2DInt(((Polygon2DInt)polySet.getShape(j)).getPolygon());
					repShape = (RadialRep2D)thisRep.getShape();
					if (chkUseOffset.isSelected()){
						thisCenter = repShape.source.getCenterPt();
						repShape.circle.centerPt.x = thisCenter.x + Float.valueOf(txtOffsetX.getText());
						repShape.circle.centerPt.y = thisCenter.y + Float.valueOf(txtOffsetY.getText());
						}
					if (chkMaxRadius.isSelected()){
						repShape.circle.radius = repShape.maxRadius;
					}
					currentSet.addShape2D(thisRep, thisSect, true);
				}
			}
			
			
		}
		
		if (CMD_GEN_CORNERS.equals(e.getActionCommand())){
			//get corner points for all polygons
			//add BoolPolygon2DInt objects to layers
			SectionSet3DInt thisSet = displayPanel.getCurrentSectionSet();
			ShapeSet2DInt polySet;
			ShapeModel3D model = thisSet.getModel();
			ShapeSet2DInt shape2DSet;
			Polygon2DInt thisPoly;
			double length_threshold = Double.valueOf(txtLenThreshold.getText());
			double angle_threshold = GeometryFunctions.getDegToRad(Double.valueOf(txtAngleThreshold.getText()));
			
			//if control points selection set exists
				//if chkOverwrite, delete it and create new
				//otherwise simply keep existing
			//otherwise create new
			//boolean blnFound = false;
			//Shape2DSelectionSet selectionSet = new Shape2DSelectionSet("Control Points");
			if (chkOverwrite.isSelected())
			for (int i = 0; i < model.selections.size(); i++){
				if (model.selections.get(i).getName().length() > 9)
				if (model.selections.get(i).getName().substring(0, 9).compareTo("ControlPts") == 0){
					//if (chkOverwrite.isSelected()){
						//delete all shapes in this selection set
					InterfaceSession.getDisplayPanel().getCurrentSectionSet().removeSelectionSet(
							InterfaceSession.getWorkspace().getSelectionSets().get(i));
						model.removeSelectionSet(model.selections.get(i));
						//displayPanel.addSelectionSet(selectionSet);
					//}else{
						//selectionSet = displayPanel.selectionSets.get(i);
					//}
					//blnFound = true;
				}
			}
			//if (!blnFound)
			//	displayPanel.addSelectionSet(selectionSet);
			
			IntPolygon2DInt newPoly;
			ArrayList<ShapeSelectionSet> selSets;
			String selSetStr = "ControlPts";
			String setStr;
			ShapeSelectionSet thisSelSet;
			Iterator<ShapeSet2DInt> itr = thisSet.sections.values().iterator();
			while (itr.hasNext()){
				shape2DSet = itr.next();
				polySet = shape2DSet.getShapeType(new Polygon2DInt());
				for (int j = 0; j < polySet.getSize(); j++){
					thisPoly = (Polygon2DInt)polySet.getMember(j);
					newPoly = ShapeFunctions.getIntCornerPoints(thisPoly,
							   								 length_threshold, 
							   								 angle_threshold);
					selSets = model.getSelectionSets(thisPoly);
					setStr = selSetStr;
					if (selSets.size() > 0)
						setStr += "." + selSets.get(0).getName();
					shape2DSet.addShape(newPoly, true, true);
				}
				
			}
			//displayPanel.fireSelectionListeners();
			displayPanel.updateDisplays();
		}
		
		if (CMD_GROOM_POLY.equals(e.getActionCommand())){
			//groom polygons
			ShapeSet2DInt thisShapeSet, polySet;
			//SectionSet3DInt sectionSet = displayPanel.getCurrentSectionSet();
			if (currentSet == null) return;
			Iterator<ShapeSet2DInt> itr = currentSet.sections.values().iterator();
			while (itr.hasNext()){
				thisShapeSet = itr.next();
				polySet = thisShapeSet.getShapeType(new Polygon2DInt());
				for (int j = 0; j < polySet.members.size(); j++){
					polySet.members.set(j, ShapeFunctions.getGroomedPolygon(
							 	(Polygon2DInt)polySet.members.get(j),
							 	Float.valueOf(txtGroomMinLen.getText()).floatValue(),
							 	chkGroomMinLen.isSelected(),
							 	Float.valueOf(txtGroomMaxLen.getText()).floatValue(),
							 	chkGroomMaxLen.isSelected(),
							 	(float)GeometryFunctions.getDegToRad(
							 			Float.valueOf(txtGroomMinAngle.getText()).floatValue())));
					}
				}
			displayPanel.updateDisplays();
			}
			
		
	}
	
	public String toString(){
		return "Radial Rep Panel";
	}
	
}