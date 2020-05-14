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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import mgui.geometry.Plane3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Polygon3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeFunctions;

/*************************************************
 * Interface panel for performing statistical procedures on shapes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeStatsPanel extends InterfacePanel implements ActionListener {

	
	
	CategoryTitle lblShapes = new CategoryTitle("SHAPES");
	JCheckBox chkCurrentAll = new JCheckBox("All shapes in model");
	JCheckBox chkCurrentShape = new JCheckBox("Selected shape:");
	JComboBox cmbCurrentShape = new JComboBox();
	JCheckBox chkCurrentSet = new JCheckBox("Selection set:");
	JComboBox cmbCurrentSet = new JComboBox();
	
	CategoryTitle lblRPlane = new CategoryTitle("REGRESSION PLANE");
	JLabel lblRPlaneType = new JLabel("Type:");
	JComboBox cmbRPlaneType = new JComboBox();
	JLabel lblRPlaneShapeSet = new JLabel("Shape set:");
	JComboBox cmbRPlaneShapeSet = new JComboBox();
	JButton cmdRPlaneOptions = new JButton("Options");
	JButton cmdRPlaneCreate = new JButton("Create");
	
	
	/*********** MISC STUFF **********************/
	
	boolean doUpdate = true;
	InterfaceShape current_shape;
	ShapeSelectionSet current_selection;
	
	
	public ShapeStatsPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	@Override
	protected void init() {
		
		_init();
		
		//set up panel categories and components
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		
		/********* INIT LISTS ******************/
		
		
		/********* SET UP LISTENERS ************/
		
		chkCurrentAll.addActionListener(this);
		chkCurrentAll.setActionCommand("Shapes Changed All");
		chkCurrentShape.addActionListener(this);
		chkCurrentShape.setActionCommand("Shapes Changed Shape");
		cmbCurrentShape.addActionListener(this);
		cmbCurrentShape.setActionCommand("Shapes Current Shape");
		chkCurrentSet.addActionListener(this);
		chkCurrentSet.setActionCommand("Shapes Changed Set");
		cmbCurrentSet.addActionListener(this);
		cmbCurrentSet.setActionCommand("Shapes Current Set");
		
		cmdRPlaneCreate.addActionListener(this);
		cmdRPlaneCreate.setActionCommand("Regression Plane Create");

		
		/********* LAY OUT COMPONENTS ***********/
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblShapes, c);
		lblShapes.setParentObj(this);
		c = new CategoryLayoutConstraints("SHAPES", 1, 1, 0.05, 0.9, 1);
		add(chkCurrentAll, c);
		c = new CategoryLayoutConstraints("SHAPES", 2, 2, 0.05, 0.9, 1);
		add(chkCurrentShape, c);
		c = new CategoryLayoutConstraints("SHAPES", 3, 3, 0.1, 0.85, 1);
		add(cmbCurrentShape, c);
		c = new CategoryLayoutConstraints("SHAPES", 4, 4, 0.05, 0.9, 1);
		add(chkCurrentSet, c);
		c = new CategoryLayoutConstraints("SHAPES", 5, 5, 0.1, 0.85, 1);
		add(cmbCurrentSet, c);
		
		
		c = new CategoryLayoutConstraints();
		lblRPlane.isExpanded = false;
		add(lblRPlane, c);
		lblRPlane.setParentObj(this);
		c = new CategoryLayoutConstraints("REGRESSION PLANE", 1, 1, 0.05, 0.2, 1);
		add(lblRPlaneType, c);
		c = new CategoryLayoutConstraints("REGRESSION PLANE", 1, 1, 0.25, 0.7, 1);
		add(cmbRPlaneType, c);
		c = new CategoryLayoutConstraints("REGRESSION PLANE", 2, 2, 0.05, 0.2, 1);
		add(lblRPlaneShapeSet, c);
		c = new CategoryLayoutConstraints("REGRESSION PLANE", 2, 2, 0.25, 0.7, 1);
		add(cmbRPlaneShapeSet, c);
		c = new CategoryLayoutConstraints("REGRESSION PLANE", 3, 3, 0.15, 0.8, 1);
		add(cmdRPlaneOptions, c);
		c = new CategoryLayoutConstraints("REGRESSION PLANE", 4, 4, 0.15, 0.8, 1);
		add(cmdRPlaneCreate, c);
		
		
		
	}

	@Override
	public void showPanel(){
		updateDisplay();
	}
	
	@Override
	public void updateDisplay(){
		updateShapeLists();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		
		//********** SHAPE SELECTION *********************************
		
		if (e.getActionCommand().startsWith("Shape")){
			
			if (e.getActionCommand().contains("Changed")){
				
				if (e.getActionCommand().endsWith("All")){
					if (!chkCurrentAll.isSelected()) 
						chkCurrentShape.setSelected(true);
					else
						chkCurrentShape.setSelected(false);
					chkCurrentSet.setSelected(false);
					}
				
				else if (e.getActionCommand().endsWith("Shape")){
					if (!chkCurrentShape.isSelected()) 
						chkCurrentAll.setSelected(true);
					else
						chkCurrentAll.setSelected(false);
					chkCurrentSet.setSelected(false);
					}
				
				else if (e.getActionCommand().endsWith("Set")){
					if (!chkCurrentSet.isSelected()) 
						chkCurrentShape.setSelected(true);
					else
						chkCurrentShape.setSelected(false);
					chkCurrentAll.setSelected(false);
					}
				
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Current Shape")){
				if (!doUpdate) return;
				current_shape = (InterfaceShape)cmbCurrentShape.getSelectedItem();
				updateCurrentShape();
				
				return;
				}
			}
			
		//************* REGRESSION PLANE **********************
		
		if (e.getActionCommand().startsWith("Regression Plane")){
			
			// Compute a regression plane for the selected shape(s) and add
			// it as a 3D polygon bordering on its bounding box
			// TODO: allow different bounding boxes
			if (e.getActionCommand().endsWith("Create")){
				
				if (current_shape == null || 
				  !(current_shape instanceof Shape3DInt)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No 3D shape selection!", 
												  "Create regression plane", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				Shape3DInt shape3d = (Shape3DInt)current_shape;
				
				//TODO: allow choice of regression type
				Plane3D rplane = GeometryFunctions.getOrthogonalRegressionPlane(shape3d.getVertices());
				if (rplane == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No plane could be created.", 
												  "Create regression plane", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				Polygon2D poly2d = ShapeFunctions.getIntersectionPolygon(shape3d.getBoundBox(), 
																		 rplane);
				
				Polygon3D poly3d = GeometryFunctions.getPolygonFromPlane(poly2d, rplane);
				
				//TODO: use selected shape set
				ShapeSet3DInt shape_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
				
				//TODO: set name and attributes from options
				Polygon3DInt poly3d_int = new Polygon3DInt(poly3d);
				poly3d_int.setName(current_shape.getName() + "_rplane");
				
				shape_set.addShape(poly3d_int);
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Regression plane created.", 
											  "Create regression plane", 
											  JOptionPane.INFORMATION_MESSAGE);
				
				return;
				}
				
				
				
			}
			
		
		
	}
	
	protected void updateControls(){
		boolean isSel = chkCurrentShape.isSelected();
		doUpdate = false;
		
		cmbCurrentShape.setEnabled(isSel);
		
		isSel &= current_shape != null;
		
		doUpdate = true;
	}
	
	protected void updateCurrentShape(){
		if (cmbCurrentShape.getItemCount() == 0) return;
		
		if (current_shape != null)
			cmbCurrentShape.setSelectedItem(current_shape);
		else{
			cmbCurrentShape.setSelectedIndex(0);
			current_shape = (InterfaceShape)cmbCurrentShape.getSelectedItem();
			}
		
	}
	
	protected void updateShapeLists(){
		doUpdate = false;
		cmbCurrentShape.removeAllItems();
		
		//TODO: also add 2D shapes
		ShapeSet3DInt shapes3D = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		ArrayList<Shape3DInt> shapes = shapes3D.get3DShapes(true);
		cmbCurrentShape.addItem(shapes3D);
		
		for (int i = 0; i < shapes.size(); i++){
			cmbCurrentShape.addItem(shapes.get(i));
			}
		
		if (cmbCurrentShape.getItemCount() == 0){
			current_shape = null;
			return;
			}
		
		if (current_shape != null)
			cmbCurrentShape.setSelectedItem(current_shape);
		else{
			cmbCurrentShape.setSelectedIndex(0);
			current_shape = (InterfaceShape)cmbCurrentShape.getSelectedItem();
			}
		
		if (current_shape == null) current_shape = shapes3D;
		
		
		//selection sets
		cmbCurrentSet.removeAllItems();
		ArrayList<ShapeSelectionSet> list = InterfaceSession.getWorkspace().getSelectionSets();
		for (int i = 0 ; i < list.size(); i++)
			cmbCurrentSet.addItem(list.get(i));
		
		doUpdate = true;
	}
	
	/****************************************
	 * Determines which shapes are selected and returns them. If only one shape is selected, returns that shape;
	 * if all shapes are selected, returns the base shape set; if a selection set is selected, returns that set as
	 * a shape set.
	 * 
	 * @return
	 */
	public InterfaceShape getCurrentShapes(){
		
		if (chkCurrentShape.isSelected()) return current_shape;
		if (chkCurrentAll.isSelected()) return InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		if (chkCurrentSet.isSelected()) return current_selection.asShapeSet3D();
		
		return null;
	}
	
}