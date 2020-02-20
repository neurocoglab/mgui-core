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
import javax.swing.SwingConstants;
import javax.vecmath.Point2f;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.IntPolygon2DInt;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeFunctions;


public class InterfaceRadialRep extends InterfacePanel implements ActionListener {

	public InterfaceDisplayPanel displayPanel; 
	public ShapeSet3DInt shapeSet;
	public SectionSet3DInt currentSet;
	
	//data
	RadialRep2DInt currentRep;
	AttributeList repAttr;
	
	//controls
	JLabel lblTitle = new JLabel("RADIAL REP PANEL");
	JLabel lblCurrentSet = new JLabel("Current set:"); 
	JTextField txtCurrentSet2 = new JTextField("");
	JLabel lblDisplayStle = new JLabel("Display Style:"); 
	JComboBox cmbDisplayStle = new JComboBox();
	JLabel lblCircleHeader = new JLabel("CIRCLE");
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
	JLabel lblControlHeader = new JLabel("CONTROL POINTS");
	JCheckBox chkUseControls = new JCheckBox("Use control points");
	JLabel lblCornerPts = new JLabel("Corner points detection:");
	JLabel lblLenThreshold = new JLabel("Length threshold:");
	JTextField txtLenThreshold = new JTextField("1.6");
	JLabel lblAngleThreshold = new JLabel("Angle threshold (deg):");
	JTextField txtAngleThreshold = new JTextField("105");
	JCheckBox chkOverwrite = new JCheckBox("Overwrite existing");
	JButton cmdGenCornerPts = new JButton("Generate");
	JButton cmdAssignCornerPts = new JButton("Assign w RadRep");
	JLabel lblGroomHeader = new JLabel("LINE GROOMING");
	JCheckBox chkGroomMinLen = new JCheckBox("Min segment length");
	JTextField txtGroomMinLen = new JTextField("2");
	JCheckBox chkGroomMaxLen = new JCheckBox("Max segment length");
	JTextField txtGroomMaxLen = new JTextField("20");
	JLabel lblGroomMinAngle = new JLabel("Min rem angle (deg)");
	JTextField txtGroomMinAngle = new JTextField("160");
	JButton cmdGroomPolys = new JButton("Groom Polygons");
	JLabel lblGenHeader = new JLabel("GENERATE");
	JLabel lblGenMultiple = new JLabel("Multiple sections:");
	JCheckBox chkGenCurrentSet = new JCheckBox();
	JLabel lblGenCurrentSet = new JLabel("Use current section set");
	JCheckBox chkGenSelSet = new JCheckBox();
	JLabel lblGenSelSet = new JLabel("Use selection set");
	JButton cmdGenSel = new JButton("Individual");
	JButton cmdGenMulti = new JButton("Multiple");
	
	//constants
	public final String CMD_GEN_SEL = "Generate Selected";
	public final String CMD_GEN_MULTI = "Generate Multiple";
	public final String CMD_GEN_CORNERS = "Generate Corners";
	public final String CMD_ASSIGN_CORNERS = "Assign Corners";
	public final String CMD_GROOM_POLY = "Groom Polygons";
	
	public InterfaceRadialRep(){
		init();
	}
	
	public InterfaceRadialRep(InterfaceDisplayPanel panel){
		displayPanel = panel;
		currentSet = (SectionSet3DInt)panel.getAttributes().getValue("CurrentSectionSet");
		//ShapeSet3DInt shapeSet = p.getCurrentShapeSet();
		init();
	}
	
	@Override
	protected void init(){
		setLayout(new LineLayout(20, 5, 200));
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0, 1, 1);
		
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblCircleHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblGenHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblControlHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblGroomHeader.setHorizontalAlignment(SwingConstants.CENTER);
		
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
		
		
		add(lblTitle, c);
		c = new LineLayoutConstraints(2, 2, 0, 1, 1);
		add(lblCurrentSet, c);
		c = new LineLayoutConstraints(3, 3, 0, 1, 1);
		add(txtCurrentSet2, c);
		c = new LineLayoutConstraints(4, 4, 0, 1, 1);
		add(lblDisplayStle, c);
		c = new LineLayoutConstraints(5, 5, 0, 1, 1);
		add(cmbDisplayStle, c);
		c = new LineLayoutConstraints(6, 6, 0, 1, 1);
		add(lblCircleHeader, c);
		c = new LineLayoutConstraints(7, 7, 0, 1, 1);
		add(lblCenterHeader, c);
		c = new LineLayoutConstraints(8, 8, 0, 0.15, 1);
		add(chkUseCenterPt, c);
		c = new LineLayoutConstraints(8, 8, 0.15, 0.85, 1);
		add(lblUseCenterPt, c);
		c = new LineLayoutConstraints(9, 9, 0, 0.15, 1);
		add(chkUseFixedPt, c);
		c = new LineLayoutConstraints(9, 9, 0.15, 0.40, 1);
		add(lblUseFixedPt, c);
		c = new LineLayoutConstraints(9, 9, 0.5, 0.175, 1);
		add(txtFixedPtX, c);
		c = new LineLayoutConstraints(9, 9, 0.75, 0.15, 1);
		add(lblUseFixedPt2, c);
		c = new LineLayoutConstraints(9, 9, 0.825, 0.175, 1);
		add(txtFixedPtY, c);
		c = new LineLayoutConstraints(10, 10, 0, 0.15, 1);
		add(chkUseOffset, c);
		c = new LineLayoutConstraints(10, 10, 0.15, 0.40, 1);
		add(lblUseOffset, c);
		c = new LineLayoutConstraints(10, 10, 0.5, 0.175, 1);
		add(txtOffsetX, c);
		c = new LineLayoutConstraints(10, 10, 0.75, 0.15, 1);
		add(lblUseOffset2, c);
		c = new LineLayoutConstraints(10, 10, 0.825, 0.175, 1);
		add(txtOffsetY, c);
		c = new LineLayoutConstraints(11, 11, 0, 1, 1);
		add(lblRadiusHeader, c);
		c = new LineLayoutConstraints(12, 12, 0, 0.15, 1);
		add(chkMaxRadius, c);
		c = new LineLayoutConstraints(12, 12, 0.15, 0.85, 1);
		add(lblMaxRadius, c);
		c = new LineLayoutConstraints(13, 13, 0, 0.15, 1);
		add(chkFixedRadius, c);
		c = new LineLayoutConstraints(13, 13, 0.15, 0.6, 1);
		add(lblFixedRadius, c);
		c = new LineLayoutConstraints(13, 13, 0.75, 0.25, 1);
		add(txtFixedRadius, c);
		c = new LineLayoutConstraints(15, 15, 0, 1, 1);
		add(lblControlHeader, c);
		c = new LineLayoutConstraints(16, 16, 0, 1, 1);
		add(chkUseControls, c);
		c = new LineLayoutConstraints(17, 17, 0, 1, 1);
		add(lblCornerPts, c);
		c = new LineLayoutConstraints(18, 18, 0, 0.7, 1);
		add(lblLenThreshold, c);
		c = new LineLayoutConstraints(18, 18, 0.7, 0.3, 1);
		add(txtLenThreshold, c);
		c = new LineLayoutConstraints(19, 19, 0, 0.7, 1);
		add(lblAngleThreshold, c);
		c = new LineLayoutConstraints(19, 19, 0.7, 0.3, 1);
		add(txtAngleThreshold, c);
		c = new LineLayoutConstraints(20, 20, 0, 1, 1);
		add(chkOverwrite, c);
		c = new LineLayoutConstraints(21, 21, 0.1, 0.8, 1);
		add(cmdGenCornerPts, c);
		c = new LineLayoutConstraints(22, 22, 0.1, 0.8, 1);
		add(cmdAssignCornerPts, c);
		
		c = new LineLayoutConstraints(24, 24, 0, 1, 1);
		add(lblGroomHeader, c);
		
		c = new LineLayoutConstraints(25, 25, 0, 0.7, 1);
		add(chkGroomMinLen, c);
		c = new LineLayoutConstraints(25, 25, 0.75, 0.25, 1);
		add(txtGroomMinLen, c);
		c = new LineLayoutConstraints(26, 26, 0, 0.7, 1);
		add(chkGroomMaxLen, c);
		c = new LineLayoutConstraints(26, 26, 0.75, 0.25, 1);
		add(txtGroomMaxLen, c);
		c = new LineLayoutConstraints(27, 27, 0, 0.7, 1);
		add(lblGroomMinAngle, c);
		c = new LineLayoutConstraints(27, 27, 0.75, 0.25, 1);
		add(txtGroomMinAngle, c);
		c = new LineLayoutConstraints(28, 28, 0.1, 0.8, 1);
		add(cmdGroomPolys, c);
		
		c = new LineLayoutConstraints(30, 30, 0, 1, 1);
		add(lblGenHeader, c);
		c = new LineLayoutConstraints(31, 31, 0, 1, 1);
		add(lblGenMultiple, c);
		c = new LineLayoutConstraints(32, 32, 0, 0.15, 1);
		add(chkGenCurrentSet, c);
		c = new LineLayoutConstraints(32, 32, 0.15, 0.85, 1);
		add(lblGenCurrentSet, c);
		c = new LineLayoutConstraints(33, 33, 0, 0.15, 1);
		add(chkGenSelSet, c);
		c = new LineLayoutConstraints(33, 33, 0.15, 0.85, 1);
		add(lblGenSelSet, c);
		c = new LineLayoutConstraints(34, 35, 0.1, 0.8, 1);
		add(cmdGenSel, c);
		c = new LineLayoutConstraints(36, 37, 0.1, 0.8, 1);
		add(cmdGenMulti, c);
		
		
		if (currentSet != null)
			txtCurrentSet2.setText(currentSet.getName());
		else
			txtCurrentSet2.setText("None");
		txtCurrentSet2.setEditable(false);
	
		updateDisplay();
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
			//for (int i = 0; i < currentSet.sectionSet.items.size(); i++){
			while (itr.hasNext()){
				//thisItem = currentSet.sectionSet.items.get(i);
				
				thisSect = itr.next();
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
			ShapeModel3D model = thisSet.getModel();
			ShapeSet2DInt polySet;
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
						//displayPanel.removeSelectionSet(displayPanel.selectionSets.get(i), model);
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
			//for (int i = 0; i < thisSet.sectionSet.items.size(); i++){
			//	shape2DSet = (ShapeSet2DInt)thisSet.sectionSet.items.get(i).objValue;
			while (itr.hasNext()){
				shape2DSet = itr.next();
				polySet = shape2DSet.getShapeType(new Polygon2DInt());
				for (int j = 0; j < polySet.getSize(); j++){
					thisPoly = (Polygon2DInt)polySet.getMember(j);
					newPoly = ShapeFunctions.getIntCornerPoints(thisPoly,
							   								 length_threshold, 
							   								 angle_threshold);
					//selSets = displayPanel.getSelectionSets(thisPoly, model);
					selSets = model.getSelectionSets(thisPoly);
					setStr = selSetStr;
					if (selSets.size() > 0)
						setStr += "." + selSets.get(0).getName();
					
					thisSelSet = model.getSelectionSet(setStr);
					if (thisSelSet == null){
						thisSelSet = new ShapeSelectionSet(setStr);
						model.addSelectionSet(thisSelSet);
						}
					
					shape2DSet.addShape(newPoly, true, true);
					thisSelSet.addShape(newPoly); //, shape2DSet);
				}
				
			}
			//displayPanel.fireSelectionListeners(new SelectionEvent());
			displayPanel.updateDisplays();
		}
		
		if (CMD_GROOM_POLY.equals(e.getActionCommand())){
			//groom polygons
			ShapeSet2DInt thisShapeSet, polySet;
			SectionSet3DInt sectionSet = displayPanel.getCurrentSectionSet();
			Iterator<ShapeSet2DInt> itr = sectionSet.sections.values().iterator();
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
	
	@Override
	public String toString(){
		return "Radial Rep Panel";
	}
	
}