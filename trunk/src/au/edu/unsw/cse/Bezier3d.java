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

public class Bezier3d extends QuadMesh3d{

  protected Point3d[][] ctl;

  // the basis function for a Bezier spline
  static float b(int i, float t) {
    switch (i) {
    case 0:
      return (1-t)*(1-t)*(1-t);
    case 1:
      return 3*t*(1-t)*(1-t);
    case 2:
      return 3*t*t*(1-t);
    case 3:
      return t*t*t;
    }
    return 0; //we only get here if an invalid i is specified
  }
  
  //evaluate a point on the B spline
  static Point3d p(Point3d[][] ctl, float s, float t) {
    Point3d result = new Point3d(0,0,0);
    for (int i = 0; i<=3; i++){
      for (int j = 0; j<=3; j++){
	result = result.add(ctl[i][j].scale(b(i,s)).scale(b(j,t)));
      }
    }
    return result;
  }

  private int steps = 10;
  
  /** set step size for Bezier
   */
  public void setSteps(int steps){
    this.steps = steps;
    setPoints();
  }


  /** Create a Bezier patch given 4x4 grid of controls
      Colours are used for wireframe in s direction and t direction
   */
  public void setPoints(){
    Point3d[][] poly = new Point3d[steps+1][steps+1];
    for (int i = 0; i<=steps; i++){
      for (int j = 0; j<=steps; j++){
	poly[i][j] = p(ctl,((float) i)/steps,((float) j)/steps);
      }
    }
    setPoints(poly);
  }

  /** Create a Bezier patch given 4x4 grid of controls
      Colours are used for wireframe in s direction and t direction
      Bezier is approximated by a steps x steps quad mesh
   */
  public Bezier3d(Point3d[][] ctl, Color ci, Color cj, int steps){
    this.ctl = ctl;
    setSteps(steps);
    setColors(ci,cj);
  }



}