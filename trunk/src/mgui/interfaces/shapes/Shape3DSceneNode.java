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

import javax.media.j3d.Node;

import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeEvent;


/**************************
 * Holds a Java3D scene node representing a Shape3DInt object
 * @author Andrew Reid
 *
 */

public class Shape3DSceneNode extends ShapeSceneNode {

	Shape3DInt shape;
	//Shape2DInt parent_shape;
	
	public Shape3DSceneNode(Shape3DInt thisShape){
		super();
		this.setCapability(ALLOW_DETACH);
		this.setCapability(ALLOW_CHILDREN_EXTEND);
		setNode(thisShape);
	}
	
	public Shape3DSceneNode(Shape3DInt thisShape, ShapeSelectionSet s){
		super();
		shape = thisShape;
		this.setCapability(ALLOW_DETACH);
		this.setCapability(ALLOW_CHILDREN_EXTEND);
		setNode3D();
	}
	
	@Override
	public void setNode(InterfaceShape thisShape){
		if (thisShape instanceof Shape3DInt){
			shape = (Shape3DInt)thisShape;
			setNode3D();
			}
	}
	
	//update node from shape
	protected void setNode3D(){
		if (shape == null) return;
		if (shapeNode != null) shapeNode.detach();
		shapeNode = shape.getScene3DObject();
		if (shapeNode == null) return;
		if (shapeNode.getParent() != null)
			shapeNode.detach();
		this.removeAllChildren();
		this.addChild(shapeNode);
		//}
	}
	
	//update node from object
	@Override
	public void shapeUpdated(ShapeEvent e) {
		if (e.getSource() != shape) return;
		
		Shape3DInt _shape;
		Shape3DSceneNode node;
		
		if (!(shape instanceof ShapeSet3DInt)){
			setNode3D();
			return;
			}
		
		ShapeSet3DInt shape_set = (ShapeSet3DInt)shape;
		switch (e.eventType){
		
			case ShapeModified:
				_shape = (Shape3DInt)shape_set.getLastModified();
				node = (Shape3DSceneNode)_shape.getShapeSceneNode(); // this.findNodeForShape(_shape);
				if (node == null) return;
				node.setNode3D();
				break;
				
			case ShapeAdded:
				_shape = (Shape3DInt)shape_set.getLastAdded();
				node = (Shape3DSceneNode)_shape.getShapeSceneNode();
				if (node.getParent() == null)
					node.detach();
				addChild(node);
				break;
				
			case ShapeRemoved:
				_shape = (Shape3DInt)shape_set.getLastRemoved();
				node = (Shape3DSceneNode)_shape.getShapeSceneNode(); //this.findNodeForShape(_shape);
				if (node != null)
					removeChild(node);
				break;
			}
		
	}
	
	@Override
	public void duplicateNode(Node originalNode, boolean forceDuplicate){
		return;
	}
	
}