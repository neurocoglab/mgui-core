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

public class Bspline3d extends QuadMesh3d{

  protected Point3d[][] ctl;

  // the basis function for a Bspline spline
  static float b(int i, float t) {
    switch (i) {
    case -2:
      return (((-t+3)*t-3)*t+1)/6;
    case -1:
      return (((3*t-6)*t)*t+4)/6;
    case 0:
      return (((-3*t+3)*t+3)*t+1)/6;
    case 1:
      return (t*t*t)/6;
    }
    return 0; //we only get here if an invalid i is specified
  }
  
  //evaluate a point on the B spline
  static Point3d p(Point3d[][] ctl, int ii, float s, int jj, float t) {
    Point3d result = new Point3d(0,0,0);
    for (int i = -2; i<=1; i++){
      for (int j = -2; j<=1; j++){
	result = result.add(ctl[ii+i][jj+j].scale(b(i,s)).scale(b(j,t)));
      }
    }
    return result;
  }

  private int steps = 10;
  
  /** set step size for Bspline
   */
  public void setSteps(int steps){
    this.steps = steps;
    setPoints();
  }


  /** Create a Bspline patch given 4x4 grid of controls
      Colours are used for wireframe in s direction and t direction
   */
  public void setPoints(){
    int width = ctl.length - 3;
    int length = ctl[0].length - 3;
    Point3d[][] poly = new Point3d[width*steps+1][length*steps+1];
    for (int ii = 2; ii < width+2; ii++) {
      for (int i = 0; i<=steps; i++){
	for (int jj = 2; jj < length+2; jj++) {
	  for (int j = 0; j<=steps; j++){
	    poly[(ii-2)*steps+i][(jj-2)*steps+j] = p(ctl,ii,((float) i)/steps,jj,((float) j)/steps);
	  }
	}
      }
    }
    setPoints(poly);
  }

  /** Create a Bspline patch given 4x4 grid of controls
      Colours are used for wireframe in s direction and t direction
      Bspline is approximated by a steps x steps quad mesh
   */
  public Bspline3d(Point3d[][] ctl, Color ci, Color cj, int steps){
    this.ctl = ctl;
    setSteps(steps);
    setColors(ci,cj);
  }



}