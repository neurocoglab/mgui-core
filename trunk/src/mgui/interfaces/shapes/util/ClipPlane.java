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

package mgui.interfaces.shapes.util;

import java.util.ArrayList;

import javax.media.j3d.ModelClip;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;

import mgui.geometry.Plane3D;
import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.shapes.SectionSet3DInt;


/*******************************************
 * 
 * Class specifies a clipping region around a plane, with back and front clip distances. 
 * 
 * 
 * @author Andrew Reid
 *
 */

public class ClipPlane extends AbstractInterfaceObject {

	public Plane3D plane;
	public float up_distance, down_distance;
	public boolean apply_clip;
	
	ArrayList<ModelClip> model_clips = new ArrayList<ModelClip>();  
	
	public ClipPlane(Plane3D plane, float up_distance, float down_distance){
		this.plane = plane;
		this.up_distance = up_distance;
		this.down_distance = down_distance;
	}
	
	/***************************************
	 * Plane for model clip defined as 4-vector, with elements A, B, C, D obtained from the plane
	 * equation Ax + By + Cz + D <= 0. See also:
	 * http://en.wikipedia.org/wiki/Plane_(mathematics)
	 * 
	 * @return ModelClip to add to a java3d scene
	 */
	public ModelClip getModelClip(){
		
		if (!apply_clip){
			ModelClip clip = new ModelClip(new Vector4d[]{new Vector4d(), new Vector4d(), new Vector4d(), 
								 new Vector4d(), new Vector4d(), new Vector4d()},
								 new boolean[]{false, false, false, false, false, false});
			clip.setCapability(ModelClip.ALLOW_PLANE_WRITE);
			clip.setCapability(ModelClip.ALLOW_ENABLE_WRITE);
		}
		
		//up plane
		Vector3f normal = plane.getNormal();
		Point3f pt = new Point3f(plane.origin);
		normal.scale(up_distance);
		pt.add(normal);
		float D = -normal.dot(new Vector3f(pt));
		Vector4d up_plane = new Vector4d(normal.x, normal.y, normal.z, D);
		
		//down plane
		normal.set(plane.getNormal());
		normal.scale(-down_distance);
		pt.set(plane.origin);
		pt.add(normal);
		D = -normal.dot(new Vector3f(pt));
		Vector4d down_plane = new Vector4d(normal.x, normal.y, normal.z, D);
		
		ModelClip clip = new ModelClip(new Vector4d[]{up_plane, down_plane, new Vector4d(), 
							 new Vector4d(), new Vector4d(), new Vector4d()},
							 new boolean[]{true, true, false, false, false, false});
		
		clip.setCapability(ModelClip.ALLOW_PLANE_WRITE);
		clip.setCapability(ModelClip.ALLOW_ENABLE_WRITE);
		
		return clip;
	}
	
	public void setModelClip(ModelClip clip){
		clip.setEnables(new boolean[]{apply_clip, apply_clip, false, false, false, false} );
		if (!apply_clip) return;
		
		//up plane
		Vector3f normal = plane.getNormal();
		Point3f pt = new Point3f(plane.origin);
		normal.scale(up_distance);
		pt.add(normal);
		float D = -normal.dot(new Vector3f(pt));
		Vector4d up_plane = new Vector4d(normal.x, normal.y, normal.z, D);
		
		//down plane
		normal.set(plane.getNormal());
		normal.scale(-down_distance);
		pt.set(plane.origin);
		pt.add(normal);
		D = -normal.dot(new Vector3f(pt));
		Vector4d down_plane = new Vector4d(normal.x, normal.y, normal.z, D);
		
		clip.setPlane(0, up_plane);
		clip.setPlane(1, down_plane);
	}
	
	public void registerModelClip(ModelClip clip){
		model_clips.add(clip);
	}
	
	public void deregisterModelClip(ModelClip clip){
		model_clips.remove(clip);
	}
	
	void updateModelClips(){
		ArrayList<ModelClip> clips = new ArrayList<ModelClip>(model_clips);
		for (int i = 0; i < clips.size(); i++){
			if (clips.get(i).getParent() == null)
				model_clips.remove(clips.get(i));
			else{
				setModelClip(model_clips.get(i));
			
				}
		}
	}
	
	public void setFromSectionSet(SectionSet3DInt set, int section){
		plane = set.getPlaneAt(section);
		apply_clip = set.getApplyClip();
		up_distance = set.getClipDistUp();
		down_distance = set.getClipDistDown();
		
	}
	
	
}