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

package au.edu.unsw.cse;
/** interface for 3d objects that can be rendered on screen */
public interface Object3d{
  public abstract Point3d centre();
  public abstract void setCentre(Point3d c);
  public abstract void setFirstFrame(int f);
  public abstract  int getLastFrame();
  public abstract void setLastFrame(int f);
  public abstract  int getFirstFrame();
  public abstract void select(int f);
  public abstract  int getSelectFrame();
  /** Depth bias - used to adjust depth in depth sort so that front facing  faces
            appear in front of back faces */ 
  public abstract double depthBias(View3d v);
  public static final double FRONTBIAS = 1000.;//bias for front faces
  public static final double BACKBIAS = -1000.;//bias for back faces
  /** Is this object visible in specified frame ? */
  public abstract  boolean visible(int frame);
  /** render this object3d */
  public abstract void render(View3d v);

  public abstract void transform(Matrix3D T);
  /** turn into VRML */
  public abstract void toVRML(VRMLState v);
  public abstract String id();

}