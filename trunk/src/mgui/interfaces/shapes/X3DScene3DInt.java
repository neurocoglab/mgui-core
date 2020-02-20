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

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import mgui.geometry.Sphere3D;


public class X3DScene3DInt extends Shape3DInt {

	public BranchGroup scene;
	
	public X3DScene3DInt(){
		init();
	}
	
	public X3DScene3DInt(BranchGroup s){
		init();
		setScene(s);
	}
	
	public void setScene(BranchGroup s){
		scene = s;
	}
	
	@Override
	public void setScene3DObject(){
		super.setScene3DObject();
		if (!this.isVisible()) return;
		
		if (scene != null)
			scene3DObject.addChild(scene);
		
		setShapeSceneNode();
	}
	
	@Override
	protected void init(){
		super.init();
		
		
	}
	
	@Override
	public void updateShape(){
		if (scene == null) return;
		scene.setBoundsAutoCompute(true);
		BoundingSphere sphere = (BoundingSphere)scene.getBounds();
		Point3d p = new Point3d();
		sphere.getCenter(p);
		centerPt = new Point3f(p);
		//centerPt.set(p);
		boundSphere = new Sphere3D(centerPt, (float)sphere.getRadius());
	}
	
	
}