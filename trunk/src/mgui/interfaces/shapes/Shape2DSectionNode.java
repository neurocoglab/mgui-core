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

/*******************************
 * Class serving as listener to update a Shape3DInt scene node based upon a
 * Shape2DInt object, as specified within a Section3DNode object.
 * 
 * 
 */

import org.jogamp.java3d.Node;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;


public class Shape2DSectionNode extends ShapeSceneNode {

	public SectionSet3DInt section_set;
	public int section;
	public ShapeSet2DInt shape_set;
	
	public Shape2DSectionNode(SectionSet3DInt set, int sect, ShapeSet2DInt thisShape){
		super();
		section_set = set;
		section = sect;
		setNode(thisShape);
		this.setCapability(ALLOW_DETACH);
		this.setCapability(ALLOW_CHILDREN_EXTEND);
		this.setCapability(ALLOW_CHILDREN_WRITE);
		//setNode2D();
	}
	
	public void setNode(ShapeSet2DInt shape_set){
			if (this.shape_set != null)
				this.shape_set.removeShapeListener(this);
			this.shape_set = shape_set;
			this.shape_set.addShapeListener(this);
			setNode2D();
	}
	
	private void setNode2D(){
		setNode2D(null);
	}
	
	private void setNode2D(ShapeSelectionSet s){
		if (shape_set == null) return;
		
		float dist =(section_set.getSpacing() * section);
		Shape3DInt shape3D = null;
		if (shape_set.isVisible() && shape_set.show3D()){
			shape3D = ShapeFunctions.getShape3DIntFromSection(section_set.getRefPlane(),
															  dist,
															  shape_set,
															  true);
			}
		if (shapeNode != null)
			shapeNode.detach();
		
		if (shape3D != null){
			shape3D.setScene3DObject();
			shape3D.setShapeSceneNode();
			shapeNode = shape3D.getShapeSceneNode();
			//((Shape3DSceneNode)shapeNode).setParentShape(shape_set);
		}else{
			shapeNode = null;
			//shapeNode = new BranchGroup();
			//shapeNode.setCapability(BranchGroup.ALLOW_DETACH);
			}
	
		this.removeAllChildren();
		if (shapeNode != null){
			shapeNode.detach();
			addChild(shapeNode);
			}
	}
	
	//update node tree from shape event
	@Override
	public void shapeUpdated(ShapeEvent e) {
		
		if (section_set == null) return;
		Shape2DInt shape;
		Shape3DInt shape3D;
		Shape3DSceneNode node;
		
		if (e.getShape() != shape_set){
			if (e.getShape() instanceof Shape2DInt){
				switch (e.eventType){
					case AttributeModified:
						shape = (Shape2DInt)e.getShape();	//shape_set.getLastModified();
						if (shape == null) return;
						shape3D = shape.getChild3D();
						if (shape3D == null) return;
						Attribute attribute = shape.getModifiedAttribute();
						if (attribute == null) return;
						shape3D.attributes.setAttribute(attribute);
						//node = (Shape3DSceneNode)shape3D.getShapeSceneNode();
						//if (node == null) return;
						//node.shape.attributes.setAttribute(attribute);
						break;
					}
				}
			
			return;
			}
		
		float dist =(section_set.getSpacing() * section);
		
		switch (e.eventType){
		
			case AttributeModified:
				
				
				break;
		
			case ShapeModified:
				shape = (Shape2DInt)shape_set.getLastModified();
				if (shape == null) return;
				shape3D = shape.getChild3D();
				if (shape3D == null) return;
				node = (Shape3DSceneNode)shape3D.getShapeSceneNode(); //findNodeForShape(shape);
				if (node == null) return;
				shape3D = ShapeFunctions.getShape3DIntFromSection(section_set.getRefPlane(),
																  dist,
																  shape,
																  true);
				node.setNode(shape3D);
				break;
				
			case ShapeAdded:
				shape = (Shape2DInt)shape_set.getLastAdded();
				if (shape == null) return;
				shape3D = ShapeFunctions.getShape3DIntFromSection(section_set.getRefPlane(),
																  dist,
																  shape,
																  true);
				if (shape3D != null){
					//node = (Shape3DSceneNode)shape3D.getShapeSceneNode();
					ShapeSet3DInt set3d = (ShapeSet3DInt)shape_set.getChild3D();
					if (set3d == null) return;
					set3d.addShape(shape3D);
					}
				
				break;
				
			case ShapeRemoved:
				shape = (Shape2DInt)shape_set.getLastRemoved();
				if (shape == null) return;
				shape3D = shape.getChild3D();
				if (shape3D == null) return;
				node = (Shape3DSceneNode)shape3D.getShapeSceneNode();
				if (node != null)
					removeChild(node);
				break;
			}
	}

	/*
	protected Shape3DSceneNode findNodeForShape(Shape2DInt shape){
		
		Enumeration children = ((Shape3DSceneNode)shapeNode).getAllChildren();
		
		while (children.hasMoreElements()){
			BranchGroup bg = (BranchGroup)children.nextElement();
			if (bg instanceof Shape3DSceneNode){
				Shape3DSceneNode scene_node = (Shape3DSceneNode)bg;
				if (scene_node.getParentShape().equals(shape))
					return scene_node;
				Shape3DSceneNode node = scene_node.findNodeForShape(shape);
				if (node != null) return node;
				}
			}
		
		return null;
		
	}
	*/
	
	//overrides Node method to return a cloned node
	@Override
	public Node cloneNode(boolean forceDuplicate){
		Shape2DSectionNode node = new Shape2DSectionNode(section_set, section, shape_set);
		node.duplicateNode(this, forceDuplicate);
		return node;
	}
	
	@Override
	public void duplicateNode(Node originalNode, boolean forceDuplicate){
		
	}
}