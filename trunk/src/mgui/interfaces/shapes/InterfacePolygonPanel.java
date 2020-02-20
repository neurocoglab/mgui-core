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

package mgui.interfaces.shapes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import mgui.geometry.Polygon2D;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.util.ShapeFunctions;


public class InterfacePolygonPanel extends InterfacePanel 
								   implements ActionListener {

	//categories and components
	CategoryTitle lblPolygons = new CategoryTitle("SHAPES");
	JCheckBox chkAll = new JCheckBox("All polygons in model");
	JCheckBox chkSel = new JCheckBox("Selected polygons:");
	JComboBox cmbCurrentPolygon = new JComboBox();
	JCheckBox chkCurrentSet = new JCheckBox("Current selection set");
	JCheckBox chkAllSets = new JCheckBox("All selection sets");
	
	CategoryTitle lblMerge = new CategoryTitle("MERGING");
	
	JCheckBox chkMergeRemoveExisting = new JCheckBox(" Remove Existing");
	JButton cmdMerge = new JButton("Execute");
	
	
	static final String CMD_MERGE = "Merge Polygons";
	
	public InterfacePolygonPanel(){
		//displayPanel = p;
		if (InterfaceSession.isInit())
			init();
	}
	
	
	@Override
	protected void init(){
		setLayout(new CategoryLayout(20, 5, 200, 10));
		chkAll.setSelected(true);
		
		lblPolygons.setHorizontalAlignment(SwingConstants.CENTER);
		lblMerge.setHorizontalAlignment(SwingConstants.CENTER);
		
		cmdMerge.setActionCommand(CMD_MERGE);
		cmdMerge.addActionListener(this);
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblPolygons, c);
		lblPolygons.setParentObj(this);
		c = new CategoryLayoutConstraints("SHAPES", 1, 1, 0.05, 0.9, 1);
		add(chkAll, c);
		c = new CategoryLayoutConstraints("SHAPES", 2, 2, 0.05, 0.9, 1);
		add(chkSel, c);
		c = new CategoryLayoutConstraints("SHAPES", 3, 3, 0.05, 0.9, 1);
		add(cmbCurrentPolygon, c);
		c = new CategoryLayoutConstraints("SHAPES", 4, 4, 0.05, 0.9, 1);
		add(chkCurrentSet, c);
		c = new CategoryLayoutConstraints("SHAPES", 5, 5, 0.05, 0.9, 1);
		add(chkAllSets, c);
		
		c = new CategoryLayoutConstraints();
		add(lblMerge, c);
		lblMerge.setParentObj(this);
		
		c = new CategoryLayoutConstraints("MERGING", 1, 1, 0.05, 0.9, 1);
		add(chkMergeRemoveExisting, c);
		c = new CategoryLayoutConstraints("MERGING", 2, 2, 0.25, 0.5, 1);
		add(cmdMerge, c);
		
	}
	
	
	
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(CMD_MERGE)){
			
			ShapeSet3DInt modelSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
			Shape3DInt thisShape;
			SectionSet3DInt sectionSet;
			ShapeSet2DInt shapeSet, mergeSet;
			
			//for each section set
			for (int i = 0; i < modelSet.members.size(); i++){
				thisShape = modelSet.getShape(i);
				if (thisShape instanceof SectionSet3DInt){
					sectionSet = (SectionSet3DInt)thisShape;
					//for (int j = 0; j < sectionSet.sectionSet.items.size(); j++){
					//	shapeSet = (ShapeSet2DInt)sectionSet.sectionSet.items.get(j).objValue;
					Iterator<ShapeSet2DInt> itr = sectionSet.sections.values().iterator();
					while (itr.hasNext()){
						shapeSet = itr.next();
						mergeSet = shapeSet.getShapeType(new Polygon2DInt());
						
						//if delete, do it here
						if (chkMergeRemoveExisting.isSelected()){
							for (int s = 0; s < mergeSet.members.size(); s++)
								shapeSet.removeShape(mergeSet.getMember(s));
							}
						
						ArrayList<Polygon2D> polygons = new ArrayList<Polygon2D>();
						
						for (int s = 0; s < mergeSet.members.size(); s++)
							polygons.add(((Polygon2DInt)mergeSet.getMember(s)).getPolygon());
						
						polygons = ShapeFunctions.getMergedPolygons(polygons);
						
						for (int s = 0; s < polygons.size(); s++)
							shapeSet.addShape(new Polygon2DInt(polygons.get(s)), true, true);
							
						//shapeSet.fireShapeListeners();
						shapeSet.fireShapeModified();
						}
					}
				}
				
			
			return;
			}
		
		
		
	}

}