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

/*************
 * Interface to specify and execute section-to-section morphing of 2D polylines
 * @author Andrew Reid
 * @version 1.0
 */

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

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceHideablePanel;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


public class InterfaceMorphPanel extends InterfacePanel implements ActionListener {

	public ShapeSet3DInt shapeSet;
	public SectionSet3DInt currentSet;
	//public InterfaceDisplayPanel displayPanel;

	JLabel lblTitle = new JLabel("MORPH POLYGONS PANEL");
	JLabel lblCurrentSet = new JLabel("Current set:"); 
	JTextField txtCurrentSet2 = new JTextField(""); 
	JLabel lblSectionHeader = new JLabel("SECTIONS");
	JCheckBox chkUseAllSections = new JCheckBox();
	JLabel lblUseAllSections = new JLabel("Use all with shapes"); 
	JCheckBox chkUseSpecSections = new JCheckBox();
	JLabel lblUseSpecSections = new JLabel("Use sections");
	JTextField txtUseSectFrom = new JTextField("0"); 
	JLabel lblUseSpecSections2 = new JLabel("to");
	JTextField txtUseSectTo = new JTextField("1");
	JLabel lblShapeHeader = new JLabel("SHAPES");
	JCheckBox chkUseAllShapes = new JCheckBox("Use all shapes");
	//JLabel lblUseAllShapes = new JLabel("Use all shapes"); 
	JCheckBox chkUseSelSet = new JCheckBox("Use current selection set");
	//JLabel lblUseSelSet = new JLabel("Use current selection set");
	JCheckBox chkUseAllSelSet = new JCheckBox("Use all selection sets");
	JComboBox cmbSelSet = new JComboBox();
	JLabel lblParamHeader = new JLabel("PARAMETERS");
	JLabel lblIterations = new JLabel("No. iterations");
	JTextField txtIterations = new JTextField("1");
	JCheckBox chkSplineX = new JCheckBox();
	JLabel lblSplineX = new JLabel("Apply X spline"); 
	JCheckBox chkSplineY = new JCheckBox();
	JLabel lblSplineY = new JLabel("Apply Y spline");
	JLabel lblSplineFactor = new JLabel("Endpoint spline factor");
	JTextField txtSplineFactor = new JTextField("2");
	JLabel lblMinAngle = new JLabel("Min. node angle (deg)");
	JTextField txtMinAngle = new JTextField("135");
	JCheckBox chkCornerPts = new JCheckBox("Detect corner points");
	JLabel lblLenThreshold = new JLabel("Length threshold:");
	JTextField txtLenThreshold = new JTextField("1.6");
	JLabel lblAngleThreshold = new JLabel("Angle threshold (deg):");
	JTextField txtAngleThreshold = new JTextField("105");
	JLabel lblOutputHeader = new JLabel("OUTPUT SHAPE");
	JLabel lblShapeName = new JLabel("Morph Set Name:");
	JTextField txtShapeName = new JTextField("Morph Set 1");
	JLabel lblShapeAttr = new JLabel("Attributes:");
	MorphSections3DInt attrSet = new MorphSections3DInt();
	
	InterfaceAttributePanel attrSetPanel = new InterfaceAttributePanel(attrSet);
	InterfaceHideablePanel ihpAttrSet = new InterfaceHideablePanel(attrSetPanel);
	
	JButton cmdExecute = new JButton("Create subsections");
	JButton cmdCorners = new JButton("Highlight corner pts");
	
	public static final String CMD_EXECUTE = "Create Subsections";
	public static final String CHK_ALLSECT = "Use All Sections";
	public static final String CHK_SPECSECT = "Use Specific Sections";
	public static final String CHK_ALLSHAPES = "Use All Shapes";
	public static final String CHK_SELSET = "Use Selection Set";
	public static final String CHK_ALLSELSET = "Use All Selection Sets";
	public static final String CMD_CORNERS = "Highlight Corner Points";
	
	public InterfaceMorphPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	/*
	public InterfaceMorphPanel(InterfaceDisplayPanel p){
		displayPanel = p;
		currentSet = (SectionSet3DInt)p.attributes.getValue("CurrentSectionSet");
		shapeSet = p.modelSet;
		init();
	}
	*/
	
	@Override
	protected void init(){
		setLayout(new LineLayout(20, 5, 200));
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0, 1, 1);
		
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblSectionHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblShapeHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblParamHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblOutputHeader.setHorizontalAlignment(SwingConstants.CENTER);
		
		chkUseAllSections.setSelected(true);
		chkUseAllShapes.setSelected(true);
		//chkSplineY.setSelected(true);
		//chkSplineX.setSelected(true);
		
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
		
		add(lblTitle, c);
		c = new LineLayoutConstraints(2, 2, 0, 1, 1);
		add(lblCurrentSet, c);
		c = new LineLayoutConstraints(3, 3, 0, 1, 1);
		add(txtCurrentSet2, c);
		c = new LineLayoutConstraints(4, 4, 0, 1, 1);
		add(lblSectionHeader, c);
		c = new LineLayoutConstraints(5, 5, 0, 0.15, 1);
		add(chkUseAllSections, c);
		c = new LineLayoutConstraints(5, 5, 0.15, 0.85, 1);
		add(lblUseAllSections, c);
		c = new LineLayoutConstraints(6, 6, 0, 0.15, 1);
		add(chkUseSpecSections, c);
		c = new LineLayoutConstraints(6, 6, 0.15, 0.40, 1);
		add(lblUseSpecSections, c);
		c = new LineLayoutConstraints(6, 6, 0.55, 0.175, 1);
		add(txtUseSectFrom, c);
		c = new LineLayoutConstraints(6, 6, 0.75, 0.15, 1);
		add(lblUseSpecSections2, c);
		c = new LineLayoutConstraints(6, 6, 0.825, 0.175, 1);
		add(txtUseSectTo, c);
		c = new LineLayoutConstraints(7, 7, 0, 1, 1);
		add(lblShapeHeader, c);
		c = new LineLayoutConstraints(8, 8, 0, 0.9, 1);
		add(chkUseAllShapes, c);
		c = new LineLayoutConstraints(9, 9, 0, 0.9, 1);
		add(chkUseSelSet, c);
		c = new LineLayoutConstraints(10, 10, 0, 0.9, 1);
		add(chkUseAllSelSet, c);
		
		c = new LineLayoutConstraints(11, 11, 0, 1, 1);
		add(lblParamHeader, c);
		c = new LineLayoutConstraints(12, 12, 0, 0.7, 1);
		add(lblIterations, c);
		c = new LineLayoutConstraints(12, 12, 0.7, 0.3, 1);
		add(txtIterations, c);
		c = new LineLayoutConstraints(13, 13, 0, 0.15, 1);
		add(chkSplineX, c);
		c = new LineLayoutConstraints(13, 13, 0.15, 0.85, 1);
		add(lblSplineX, c);
		c = new LineLayoutConstraints(14, 14, 0, 0.15, 1);
		add(chkSplineY, c);
		c = new LineLayoutConstraints(14, 14, 0.15, 0.85, 1);
		add(lblSplineY, c);
		c = new LineLayoutConstraints(15, 15, 0, 0.7, 1);
		add(lblSplineFactor, c);
		c = new LineLayoutConstraints(15, 15, 0.7, 0.3, 1);
		add(txtSplineFactor, c);
		c = new LineLayoutConstraints(16, 16, 0, 0.7, 1);
		add(lblMinAngle, c);
		c = new LineLayoutConstraints(16, 16, 0.7, 0.3, 1);
		add(txtMinAngle, c);
		c = new LineLayoutConstraints(17, 17, 0, 0.8, 1);
		add(chkCornerPts, c);
		c = new LineLayoutConstraints(18, 18, 0, 0.7, 1);
		add(lblLenThreshold, c);
		c = new LineLayoutConstraints(18, 18, 0.7, 0.3, 1);
		add(txtLenThreshold, c);
		c = new LineLayoutConstraints(19, 19, 0, 0.7, 1);
		add(lblAngleThreshold, c);
		c = new LineLayoutConstraints(19, 19, 0.7, 0.3, 1);
		add(txtAngleThreshold, c);
		
		c = new LineLayoutConstraints(20, 20, 0, 1, 1);
		add(lblOutputHeader, c);
		c = new LineLayoutConstraints(21, 21, 0, 0.5, 1);
		add(lblShapeName, c);
		c = new LineLayoutConstraints(21, 21, 0.55, 0.45, 1);
		add(txtShapeName, c);
		c = new LineLayoutConstraints(22, 22, 0, 1, 1);
		add(lblShapeAttr, c);
		c = new LineLayoutConstraints(23, 29, 0, 1, 1);
		add(ihpAttrSet, c);
		c = new LineLayoutConstraints(30, 31, 0.10, 0.8, 1);
		add(cmdExecute, c);
		c = new LineLayoutConstraints(32, 33, 0.10, 0.8, 1);
		add(cmdCorners, c);
		
		if (currentSet != null)
			txtCurrentSet2.setText(currentSet.getName());
		else
			txtCurrentSet2.setText("None");
		txtCurrentSet2.setEditable(false);
	
		updateDisplay();
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (CMD_EXECUTE.equals(e.getActionCommand())){
			
			MorphEngine morphEngine = new MorphEngine();
			morphEngine.morphAttr.setValue("NoIterations", 
										   new MguiInteger(Integer.valueOf(txtIterations.getText())));
			
			morphEngine.morphAttr.setValue("EndSplineFactor", 
					   new MguiDouble(Double.valueOf(txtSplineFactor.getText())));
			
			morphEngine.morphAttr.setValue("ApplySplineX", new MguiBoolean(chkSplineX.isSelected()));
			morphEngine.morphAttr.setValue("ApplySplineY", new MguiBoolean(chkSplineX.isSelected()));
			morphEngine.morphAttr.setValue("ShapeName", txtShapeName.getText());
			morphEngine.morphAttr.setValue("UseSelectionSet", new MguiBoolean(chkUseSelSet.isSelected()));
			if (chkUseSelSet.isSelected())
				morphEngine.morphAttr.setValue("SelectionSet", InterfaceSession.getDisplayPanel().getCurrentSelection());
			//morphEngine.morphAttr.setValue("UseAllSelectionSets", new arBoolean(chkUseSelSet.isSelected()));
			//if (chkUseAllSelSet.isSelected())
			//	morphEngine.morphAttr.setValue("SelectionSetList", displayPanel.selectionSets);
			morphEngine.morphAttr.setValue("MinAngle", new MguiDouble(
					GeometryFunctions.getDegToRad(Double.valueOf(txtMinAngle.getText()))));
			
			MorphSections3DInt morphSections = null;
			boolean blnUseAllSelSet = false;
			
			//generate subsections
			//if using all selection sets, iterate here
			ArrayList<ShapeSelectionSet> sel_sets = InterfaceSession.getWorkspace().getSelectionSets();
			if (chkUseAllSelSet.isSelected() && sel_sets.size() > 0){
				blnUseAllSelSet = true;
				((MguiBoolean)morphEngine.morphAttr.getValue("UseSelectionSet")).setTrue(true);
				morphEngine.morphAttr.setValue("SelectionSet", sel_sets.get(0));
			} 
			//morphSections = morphEngine.getMorphSections(currentSet, 0.8);
			
			if (blnUseAllSelSet){
				for (int i = 1; i < sel_sets.size(); i++){
					morphEngine.morphAttr.setValue("SelectionSet", sel_sets.get(i));
					morphSections.addUnionSet(morphEngine.getMorphSections(currentSet, 0.8), true);
				}
				
			}
			
			//morphSections.name = txtShapeName.getText();
			morphSections.setParent(currentSet, (Integer.valueOf((txtIterations.getText())).intValue()));
			InterfaceSession.getDisplayPanel().addShapeInt(morphSections);
			//shapeSet.addShape(morphSections);
			
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
			SectionSet3DInt thisSet = InterfaceSession.getDisplayPanel().getCurrentSectionSet();
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
			InterfaceSession.getDisplayPanel().updateDisplays();
		}
		
	}
	
	
	@Override
	public String toString(){
		return "Morph Polygons Panel";
	}
	
}