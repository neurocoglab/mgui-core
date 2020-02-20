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

package au.edu.unsw.cse;
import java.awt.Color;
import java.awt.Point;
/** A point in 3d space that can be rendered */

public class Site3d extends Object3dAdaptor {
  

  public Site3d(Point3d pt) {
    centre = pt;
  }

  public Site3d(Point3d pt, int frameNo) {
    this(pt);
    this.firstFrame = frameNo;
  }
    
  private static int nfront;
  private static int[][] ix = new int[Cube.nfront][Cube.npoints+1]; // nfront visible cube faces in 
  private static int[][] iy = new int[Cube.nfront][Cube.npoints+1]; // screen space
  private static Color[] icols = new Color[Cube.nfront];
  private static Color[] iselectcols = new Color[Cube.nfront];
  private static Point ip[] = new Point[Cube.unitCube.length];


  // build ix and iy
  private void cache(View3d v) {
    // first transform corners to screen space
    for (int i = 0; i < Cube.unitCube.length; i++){
      ip[i] = v.toPoint(Cube.unitCube[i].scale(3/v.xscale));//3 is cube side in pixels
    }

    // now store forward facing faces
    nfront = 0; //counts front faces
    for (int i = 0; i < Cube.faces.length; i++){
      if(v.w.dot(Cube.unitCubeNormal[i])>0){ //if facing forward
      	icols[nfront] = Cube.cols[i];
      	iselectcols[nfront] = Cube.selectcols[i];
	for (int j=0; j<Cube.npoints; j++){
	  ix[nfront][j] = ip[Cube.faces[i][j]].x;
	  iy[nfront][j] = ip[Cube.faces[i][j]].y;
	}
	ix[nfront][Cube.npoints] = ix[nfront][0]; //jdk1.0 bug workaround
	iy[nfront][Cube.npoints] = iy[nfront][0];
	nfront++;
      }
    }
  }
	
  static Point3d cachevw = null;
  @Override
public void render(View3d v){
    Color[] cols;
    if (v.orthographic()){
      if (cachevw != v.w) {
	cachevw = v.w;
	cache(v);
      }
      Point p = v.toPoint(centre);
      v.g.translate(p.x-ip[0].x,p.y-ip[0].y);
      if (selectFrameNo==v.getFrameNo()){
	cols = iselectcols;
      } else {
	cols = icols;
      }
      for (int ii = 0; ii < nfront; ii++){
	v.g.setColor(cols[ii]);
	v.g.fillPolygon(ix[ii],iy[ii],Cube.npoints);
	v.g.setColor(Color.black);
	v.g.drawPolygon(ix[ii],iy[ii],Cube.npoints+1);
      }
      v.g.translate(-p.x+ip[0].x,-p.y+ip[0].y);      
    } else {
      int[] ix = new int[Cube.npoints+1]; // one cube face in 
      int[] iy = new int[Cube.npoints+1]; // screen space
      
      // first transform corners to screen space
      Point p[] = new Point[Cube.unitCube.length];
      for (int i = 1; i < Cube.unitCube.length; i++){
	p[i] = v.toPoint(Cube.unitCube[i].scale(3/v.xscale).add(centre));
      }
      
      
      if (selectFrameNo==v.getFrameNo()){
	cols = Cube.selectcols;
      } else {
	cols = Cube.cols;
      }
      // now render forward facing faces
      for (int i = 0; i < Cube.faces.length; i++){
	if(v.frontFace(Cube.unitCubeNormal[i],centre)){ //if facing forward
	  for (int j=0; j<Cube.npoints; j++){
	    ix[j] = p[Cube.faces[i][j]].x;
	    iy[j] = p[Cube.faces[i][j]].y;
	  }
	  ix[Cube.npoints] = ix[0]; iy[Cube.npoints] = iy[0]; //JDK bug work around
	  v.g.setColor(cols[i]);
	  v.g.fillPolygon(ix,iy,Cube.npoints);
	  v.g.setColor(Color.black);
	  v.g.drawPolygon(ix,iy,Cube.npoints+1);
	}
      }      
    }
  }

  @Override
public String toString(){
    return centre.toString();
  }

  @Override
public String vrmlPROTOName() {
    return "Site3d";
  }

  @Override
public String vrmlPROTOExtraFields() {
    return 
      super.vrmlPROTOExtraFields()+
      "  field SFVec3f point 0 0 0 \n"+
      "  field MFColor color 1 1 1 \n";
  }

  @Override
public String[] vrmlPROTOMaterials() {
    String[] result = super.vrmlPROTOMaterials();
    for (int i=0; i < result.length; i++){
      result[i] += "point IS point\n";
    }
    result[0] += "color IS color\n";
    result[1] += "color 1 0 0\n";
    result[2] += "color 1 1 0\n";
    result[3] += "color 0 1 1\n";
    return result;
  }

  @Override
public String vrmlPROTOINBody() {
    return
      "Transform{\n"+
      "translation IS point\n"+
      "children [\n"+
      "Shape {\n"+
      "	appearance Appearance {\n"+
      "	  material IS material\n"+
      "	}\n"+
      " geometry  Sphere {radius 0.02}"+
      "	}\n"+
      "	]\n"+
      "}\n";
  }

  @Override
public String toVRMLBody(VRMLState v) {
    StringBuffer s = new StringBuffer();
    String vcol = v.getVRMLColor(-1);
    if (vcol!=null) {
      s.append("color "+vcol+"\n");
    }
    s.append("point "+centre.toVRML()+"\n");
    return s.toString();
  }
}