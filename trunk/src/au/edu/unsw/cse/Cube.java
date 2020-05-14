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

public class Cube extends Object3dAdaptor{

  Point3d[] cube = {
    new Point3d(0,0,0), //cube[0] is the centre of the cube
    new Point3d(-1,-1,-1),new Point3d(1,-1,-1), //corners of cube
    new Point3d(-1,1,-1),new Point3d(1,1,-1),
    new Point3d(-1,-1,1),new Point3d(1,-1,1),
    new Point3d(-1,1,1),new Point3d(1,1,1)};

  final static Point3d[] unitCube = {
    new Point3d(0,0,0), //cube[0] is the centre of the cube
    new Point3d(-1,-1,-1),new Point3d(1,-1,-1), //corners of cube
    new Point3d(-1,1,-1),new Point3d(1,1,-1),
    new Point3d(-1,-1,1),new Point3d(1,-1,1),
    new Point3d(-1,1,1),new Point3d(1,1,1)};
  
  final static int[][] faces = {
    {1,2,6,5}, //This means that face 0 has corners 1 2 6 and 5
    {2,4,8,6},
    {8,7,5,6},
    {8,4,3,7},
    {1,3,4,2},
    {5,7,3,1}};
    
  final static Point3d[] unitCubeNormal = { //normals to faces of unit cube
    new Point3d(0,-1,0),
    new Point3d(1,0,0),
    new Point3d(0,0,1),
    new Point3d(0,1,0),
    new Point3d(0,0,-1),
    new Point3d(-1,0,0)};
  	
  final static int npoints = faces[0].length; //no of points on a face
  final static int nfront = 3; //no of front faces on cube
  static Color[] cols = { //colours of the faces
    Color.yellow,
    Color.blue,
    Color.red,
    Color.yellow,
    Color.red,
    Color.blue};

  static Color[] selectcols = { //colours of faces of slected cube
    Color.green,
    Color.magenta,
    Color.cyan,
    Color.green,
    Color.cyan,
    Color.magenta};

  /** Create a cube with centre at specified point and with given radius
   */
  public Cube(Point3d centre,double radius){
    Matrix3D T; //matrix that transforms unit cube into world space.
    this.centre = centre;
    lastRadius = radius;
    T = new Matrix3D();
    T.scale(radius);
    T.translate(centre);
    T.transform(cube);
  }
  
  static double lastRadius = 0.02;
  /** Create a cube with centre at specified point and with same radius as last one
   */
  public Cube(Point3d centre){
    this(centre, lastRadius);
  }

  @Override
public double depthBias(View3d v) {
    return FRONTBIAS;
  }

  /** render the cube, given a 3D view */
  @Override
public void render(View3d v){
    int[] ix = new int[npoints+1]; // one cube face in 
    int[] iy = new int[npoints+1]; // screen space
      
    // first transform corners to screen space
    Point p[] = new Point[cube.length];
    for (int i = 1; i < cube.length; i++){
      p[i] = v.toPoint(cube[i]);
    }

    // now render forward facing faces
    for (int i = 0; i < faces.length; i++){
      if(v.frontFace(faceNormal(i),cube[faces[i][0]])){ //if facing forward
	for (int j=0; j<npoints; j++){
	  ix[j] = p[faces[i][j]].x;
	  iy[j] = p[faces[i][j]].y;
	}
	ix[npoints] = ix[0]; iy[npoints] = iy[0]; //JDK bug work around
	v.g.setColor(cols[i]);
	v.g.fillPolygon(ix,iy,npoints);
	v.g.setColor(Color.black);
	v.g.drawPolygon(ix,iy,npoints+1);
      }
    }
  }

  /** method needed by render to compute normal to a face
   */
  Point3d faceNormal(int i) {
    return cube[faces[i][1]].subtract(cube[faces[i][0]]).cross(cube[faces[i][2]].subtract(cube[faces[i][0]]));
  }

  /** compute new position of cube
   */
  @Override
public void transform(Matrix3D T){
    T.transform(cube);
    centre = cube[0];
  }

}