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

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;

import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;


public class ShapeSceneNode extends BranchGroup implements ShapeListener {

	public BranchGroup shapeNode;
	private boolean isDestroyed = false;
	
	public ShapeSceneNode(){
		super();
		init();
	}
	
	private void init(){
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_READ);
		this.setCapability(BranchGroup.ALLOW_DETACH);
	}
	
	public void setNode(InterfaceShape thisShape){}
	public void shapeUpdated(ShapeEvent e){}
	public BranchGroup getNode(){ return shapeNode;}
	public void destroy(){
		isDestroyed = true;
		if (shapeNode != null){
			shapeNode.detach();
			shapeNode = null;
			}
		this.removeAllChildren();
		this.detach();
	}
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
}