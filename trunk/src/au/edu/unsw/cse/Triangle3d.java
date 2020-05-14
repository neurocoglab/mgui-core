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

public class Triangle3d extends Object3dAdaptor{

  public Point3d[] tri ;
  int col;
  HalfSpace h;
  static int backFaceColor; //colour for back faces
   
  /** Create a triangle with given colour
   */
  public Triangle3d(Point3d[] tri,int col){
    this.tri = tri;
    this.col = col;
    computeHalfSpace();
  }
  
  public Triangle3d(Point3d a, Point3d b, Point3d c, int frameNo){
    tri = new Point3d[3];
    tri[0] = a; tri[1] = b; tri[2] = c;
    col = -1;
    this.firstFrame = frameNo;
    computeHalfSpace();
  }

  private void computeHalfSpace() {
    h = new HalfSpace(tri[0],tri[1],tri[2]);
    centre = tri[0].add(tri[1]).add(tri[2]).scale(1.0/3.0);
  }

  /** render the triangle, given a 3D view */
  @Override
public void render(View3d v){
    int[] ix = new int[3]; // tri in
    int[] iy = new int[3]; // screen space
      
    // first transform corners to screen space
    Point p;
    for (int i = 0; i < tri.length; i++){
      p = v.toPoint(tri[i]);
      ix[i] = p.x;
      iy[i] = p.y;
    }
    Color c = v.shade(getColorIndex(v,col),backFaceColor,h.normal,tri[0]);
    if (c != null) {
      v.g.setColor(c);
      v.g.fillPolygon(ix,iy,3);
    }
    v.g.setColor(Color.black);
    v.g.drawPolygon(ix,iy,3);
    v.g.drawLine(ix[2],iy[2],ix[0],iy[0]);
  }

  public boolean inside (Point3d x){
    return h.inside(x);
  }
  
  @Override
public double depthBias(View3d v) {
    if (v.frontFace(h.normal,tri[0])) {
      return FRONTBIAS;
    } else {
      return BACKBIAS;
    }
  }

  /** compute new position of tri
   */
  @Override
public void transform(Matrix3D T){
    T.transform(tri);
    centre = tri[0].add(tri[1]).add(tri[2]).scale(1.0/3.0);
  }

  @Override
public String toString(){
    return"("+tri[0]+","+tri[1]+","+tri[2]+") "+firstFrame+ " "+lastFrame;
  }


  @Override
public String vrmlPROTOName() {
    return "Triangle3d";
  }

  @Override
public String vrmlPROTOExtraFields() {
    return 
      super.vrmlPROTOExtraFields()+
      "  field MFVec3f point [] \n"+
      "  field MFColor color [0 1 0,0 0 1]\n";
  }


  //
  @Override
public String[] vrmlPROTOMaterials() {
    String[] result = super.vrmlPROTOMaterials();
    for (int i=0; i < result.length; i++){
      result[i] += "point IS point\n";
    }
    result[0] += "color IS color\n";
    result[1] += "color [1 0 0,0 0 1]\n";
    result[2] += "color [1 1 0,0 0 1]\n";
    result[3] += "color [0 1 1,0 0 1]\n";
    return result;
  }

  @Override
public String vrmlPROTOINBody() {
    return
     " Shape {\n"+
     "	appearance Appearance {\n"+
     "	  material IS material\n"+
     "	}\n"+
     "	geometry IndexedFaceSet {\n"+
     "	  coord Coordinate {\n"+
     "	    point IS point\n"+
     "	  }\n"+
     "	  coordIndex [0,1,2,-1,0,2,1,-1]\n"+
     "	  color Color{ color IS color}\n"+
     "	  colorIndex [0,1]\n"+
     "	  colorPerVertex FALSE\n"+
     "	}\n"+
     "      }\n"+
     "      Shape {\n"+
     "	appearance Appearance {\n"+
     "	  material Material {\n"+
     "	    diffuseColor 0 0 0\n"+
     "	  }\n"+
     "	}\n"+
     "	geometry IndexedLineSet {\n"+
     "	  coord Coordinate {\n"+
     "	    point IS point\n"+
     "	  }\n"+
     "	  coordIndex [0,1,2,0,-1]\n"+
     "	}\n"+
     "      }\n";
  }


  @Override
public String toVRMLBody(VRMLState v) {
    StringBuffer s = new StringBuffer();
    String vcol = v.getVRMLColor(col);
    if (vcol!=null) {
      s.append("color ["+vcol+","+v.getVRMLColor(backFaceColor)+"]\n");
    }
    s.append("point [\n");
    for (int i = 0; i < tri.length; i++){
      s.append(tri[i].toVRML()+",\n");
    }
    s.append("]\n");
    return s.toString();
  }

}