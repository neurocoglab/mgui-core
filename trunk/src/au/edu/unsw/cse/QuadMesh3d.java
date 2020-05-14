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
import java.awt.Color;
import java.awt.Point;

public class QuadMesh3d extends Object3dAdaptor{

  protected Point3d[][] pts;
  protected Color ci = Color.black; //colour in i direction
  protected Color cj = Color.black; //colour in j direction

  /** Create a Quad mesh
   */
  public QuadMesh3d(){
  }

  /** Create a Quad mesh, given controls
   */
  public QuadMesh3d(Point3d[][] pts){
    this();
    setPoints(pts);
  }

  public void setPoints(Point3d[][] pts) {
    this.pts = pts;
    centre = pts[0][0];
  }

  public void setColors(Color ci, Color cj) {
    this.ci = ci;
    this.cj = cj;
  }

  /** render the Mesh, given a 3D view */
  @Override
public void render(View3d v){
    if (pts != null) {
      Point[][] spts = new Point[pts.length][pts[0].length]; // Mesh in screen space
      
      // first transform corners to screen space
      for (int i = 0; i < pts.length; i++){
	for (int j = 0; j < pts[0].length; j++){
	  spts[i][j] = v.toPoint(pts[i][j]);
	}
      }
      v.g.setColor(ci);
      for (int i = 0; i < pts.length; i++){
	for (int j = 1; j < pts[0].length; j++){
	  v.g.drawLine(spts[i][j-1].x,spts[i][j-1].y,spts[i][j].x,spts[i][j].y);
	}
      }
      v.g.setColor(cj);
      for (int i = 1; i < pts.length; i++){
	for (int j = 0; j < pts[0].length; j++){
	  v.g.drawLine(spts[i-1][j].x,spts[i-1][j].y,spts[i][j].x,spts[i][j].y);
	}
      }
    }
  }

  /** compute new position of pol
   */
  @Override
public void transform(Matrix3D T){
    //T.transform(pol);
    //centre = pol[0].add(pol[1]).add(pol[2]).scale(1.0/3.0);
  }

  /** Turn into VRML
   */
  public String toVRML() {
    //NumberFormat nf =  NumberFormat.getInstance();
    //nf.setMaximumFractionDigits(3);
    
    StringBuffer s = new StringBuffer();
    s.append("Shape {\nappearance Appearance {\n");
    s.append("material Material{\n");
    s.append("transparency 0.5");
    s.append("}\n}\n");
    s.append("geometry IndexedFaceSet {\nsolid FALSE creaseAngle 0.3\n coord Coordinate {\n");
    s.append("point [\n");
    for (int i = 0; i < pts.length; i++){
      for (int j = 0; j < pts[0].length; j++){
	Point3d p = pts[i][j];
	s.append((float)(p.x())+" "+(float)(p.y())+" "+(float)(p.z())+",\n");
	//s.append(nf.format(p.x())+" "+nf.format(p.y())+" "+nf.format(p.z())+",\n");
      }
    }
    s.append("]\n}\n");
    s.append("coordIndex [\n");
    for (int i = 1; i < pts.length; i++){
      int l = pts[0].length;
      for (int j = 1; j < l; j++){
	int k = l*i+j;
	s.append((k-l-1)+"," +(k-l)+"," +k+"," +(k-1)+",-1,\n");
      }
    }
    s.append("]\n}\n}\n");
    return s.toString();
  }
}