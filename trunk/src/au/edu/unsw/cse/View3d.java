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
/** This class implements a 3D view.
  It provides a mapping from points in XYZ space to pixels on the screen.
@author Tim Lambert
 */

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

public class View3d extends View3dInfo implements Cloneable{
  public Graphics g;  //The graphics object to draw in
  private int width=100,height=100; //Width and height of g
  private double adjwx,adjwy;  //Adjusted window origin
  protected int frameNo = 0;
  protected int lastFrame = 0;
  public double xscale,yscale; // scale factors in window -> viewport mapping
  public static Color bgcolor = Color.white; // background colour to use in clear() method
  protected Color ambient = new Color(50,50,50);
  protected Color lightColor = new Color(205,205,205);
  protected Color[] colors = {Color.blue,Color.green,Color.red,Color.yellow,
			      Color.black,Color.black,Color.cyan,Color.magenta,
			      Color.cyan,Color.magenta};
  protected int defaultColor = 0;
  protected Point3d lightDirection = null; //direction of light source

  /** Creates a 3D view given a graphics object to draw in, a viewport
    within that graphics object, and a background colour
    */
  public View3d(Point3d dirn){
    super(dirn);
    setLightDirection(w);
  }

  @Override
public void set(View3dInfo v){
    super.set(v);
    setLightDirection(w);
  }

  /** set Graphics object to draw in
   */
  public void setGraphics(Graphics g,int width, int height){
    this.g = g;
    this.width = width;
    this.height = height;
    setWindow(wx,wy,wwidth,wheight);
    //g.clipRect(0,0,width,height);
  }    

  /** Set the window on the UV plane
   */
  @Override
public void setWindow(double wx,double wy,double wwidth,double wheight){
    super.setWindow(wx,wy,wwidth,wheight);
    if (width==0) {
      return;
    }
    xscale = width/wwidth;     
    yscale = height/wheight;
    if (yscale > xscale) {
      yscale = xscale;     
      double newwheight = height/yscale;
      adjwy += (wheight - newwheight)/2.0;
      adjwx = wx;
    } else {
      xscale = yscale;     
      double newwidth = width/xscale;
      adjwx += (wwidth - newwidth)/2.0;
      adjwy = wy;
    }
  }
  
  /** Adjust the position of the camera, given a displacement of the view
    position relative to U and V vectors
    */

  public void adjustCamera(double deltax,double deltay){
    adjustCameraDI(-deltax/width,deltay/height);
    setLightDirection(w);
  }
 
  
  public void pan(int deltax, int deltay){
    panDI(-(double)deltax/(xscale*wwidth),deltay/(yscale*wheight));
  }
  
  /** set the frame no */
  public void setFrameNo(int frameNo) {
    this.frameNo = frameNo;
  }

  /** get the frame no */
  public int getFrameNo() {
    return frameNo;
  }

  /** set the last frame no */
  public void setLastFrame(int frame) {
    this.lastFrame = frame;
  }

  /** get the last frame no */
  public int getLastFrame() {
    return lastFrame;
  }

  /** set direction of light source */
  public void setLightDirection(Point3d dirn){
    lightDirection = dirn.normalize();
  }

  
  /** set a colour for objects */
  public void setColor(int i,Color c){
    colors[i] = c;
  }

  /**get a colour index - translating -1 to default colour */
  public int getColorIndex(int i){
    return i==-1?defaultColor:i;
  }

  /** get a colour for objects */
  public Color getColor(int i){
    return colors[getColorIndex(i)];
  }


  /** get a colour for VRML objects */
  public String getVRMLColor(int i){
    Color c = getColor(i);
    if (c==null){
      return null;
    } else if (c.getRed()==0&&c.getGreen()==0&&c.getBlue()==0){
      return "1 1 1";
    } else {
      return (c.getRed()/255.0)+" "+
	(c.getGreen()/255.0)+" "+
	(c.getBlue()/255.0);
    }      
  }

  /** set a default colour for objects */
  public int setDefaultColor(int i){
    int save = defaultColor;
    defaultColor = i;
    return save;
  }
  	
  /** clamp a colour component to 0..255 range */
  private static int clamp(double c){
    if (c >= 255) {
      return 255;
    } else if (c<=0) {
      return 0;
    } else {
      return (int) c;
    }
  }
      

  /** compute shading colour */
  public Color shade(int c1, int c2, Point3d n, Point3d p) {
    if (colors[getColorIndex(c1)] == null){
      return null;
    }
    double k;
    if (dinverse == 0) {
      k = n.dot(w);
    } else {
      k = n.dot(w.scale(1/dinverse).subtract(p).normalize());
    }
    int i;
    if (k>0) {
      i = c1;
    } else {
      i = c2;
      k = -k;
    }
    if (i<0) {
      i = defaultColor;
    }
    Color c = colors[i];
    Color lc = lightColor;
    Color a = ambient;
    return new Color(
		     clamp(a.getRed()+k*c.getRed()*lc.getRed()/255.0),
		     clamp(a.getGreen()+k*c.getGreen()*lc.getGreen()/255.0),
		     clamp(a.getBlue()+k*c.getBlue()*lc.getBlue()/255.0));
  }
  

  /* viewing transformation from XYZ to screen space.
   */
  public Point toPoint(Point3d p){
    double divisor = 1-dinverse*w.dot(p);
    return new Point((int)((u.dot(p)/divisor-wx)*xscale),
		     height-(int)((v.dot(p)/divisor-wy)*yscale));
  }


  /* Clear the drawing area
   */
  public void clear(){
    g.setColor(bgcolor);
    g.fillRect(0,0,width,height);
  }

  /** Draw a 3D line.
   */
  public void drawLine(Point3d p1, Point3d p2){
    Point s1 = toPoint(p1);
    Point s2 = toPoint(p2);
    g.drawLine(s1.x,s1.y,s2.x,s2.y);
  }

  /** Place a string at a point in 3D space.
    Useful for labelling vertices when debugging
    */
  public void drawString(String s, Point3d p1){
    Point s1 = toPoint(p1);
    g.drawString(s,s1.x,s1.y);
  }

  /** Place a string below a point in 3D space.
    Useful for labelling vertices when debugging
    */
  public void drawStringBelow(String s, Point3d p1){
    Point s1 = toPoint(p1);
    FontMetrics fm = g.getFontMetrics();
    int w = fm.stringWidth(s);
    int h = fm.getAscent();
    g.drawString(s,s1.x-w/2,s1.y+h+2);
  }

  /* note that this does only a top level copy.
     This is OK because Point3d objects are not supposed to be modified. */
  @Override
public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
    }
    return null;
  }

}