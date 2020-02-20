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
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Event;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

public class AppletHull extends Applet3d{

  Panel mainPanel;
  Choice modelChoice=null;
  Choice dataChoice;
  Choice dataChoice2d;
  Choice dataChoice3d;
  Choice dimensionChoice;
  TextField dataSize;
  Button moreButton;
  String moreMessage;
  Axes axes = new Axes();
  Points3d points3d = new Points3d();
  private Point3dObject3d[] points = new Point3dObject3d[32];

  Viewer makeViewer(){
    Viewer v = new Viewer(this);
    try {
      String view = getParameter("view");
      if (view != null){
	View3dInfo home = View3dInfo.fromString(view);
	v.setHome(home);
	v.setView(home);
      }
    } catch (NumberFormatException e) {
    }
    Object3dAdaptor.normalColor=v.addColor("Normal","normal objects",getBoolParameter("Normal"),Color.green,true);
    Triangle3d.backFaceColor=v.addColor(Color.blue);
    Object3dAdaptor.addColor=v.addColor("Added","added objects",getBoolParameter("Added"),Color.red,true);
    Object3dAdaptor.deleteColor=v.addColor("Deleted","deleted objects",getBoolParameter("Deleted"),Color.yellow,true);
    Axes.color=v.addColor("Axes","coordinate axes",getBoolParameter("Axes"),Color.black,true);
    Points3d.color=v.addColor("Points","input points",getBoolParameter("Points"),Color.black,true);
    DivideAndConquer.leftColor=v.addColor("Left Hull","left convex hull",getBoolParameter("Left Hull"),Color.cyan,false);
    DivideAndConquer.rightColor=v.addColor("Right Hull","right convex hull",getBoolParameter("Right Hull"),Color.magenta,false);
    Object3dAdaptor.selectColor=v.addColor("Selected","selected object",getBoolParameter("Selected"),Color.cyan,false);

    dataChoice.select(getParameter("distribution"));
    dataSize.setText(getParameter("npoints"));
    setDistribution();
    return v;
  }

  public void init(){
    mainPanel = new Panel();
    dimensionChoice = new Choice();
    dimensionChoice.addItem("2D");
    dimensionChoice.addItem("3D");
    mainPanel.add(dimensionChoice);
    dataChoice3d = new Choice();
    dataChoice3d.addItem("In Sphere");
    dataChoice3d.addItem("On Sphere");
    dataChoice3d.addItem("On Paraboloid");
    dataChoice3d.addItem("In Cube");
    dataChoice3d.addItem("Gaussian");
    dataChoice3d.addItem("Wedge Block");
    dataChoice2d = new Choice();    
    dataChoice2d.addItem("In Circle");
    dataChoice2d.addItem("On Circle");
    dataChoice2d.addItem("In Square");
    dataChoice = dataChoice3d;
    dataSize = new TextField(Integer.toString(points.length),3);
    super.init();
    mainPanel.add(createModelChoice());
    selectDimension();
    mainPanel.add(dataSize);
    mainPanel.add(new Label("Points"));
    moreButton = new Button(moreMessage="More Controls");
    if (!getBoolParameter("vrml")){
      mainPanel.add(moreButton);
    }

    if (getBoolParameter("controls")){
      add("South",mainPanel);
    }

    points3d.setLabels(getIntVectorParameter("labels"));
    int frame = getIntParameter("frame",-1);
    if (frame!=-1){
      viewer.stop();
      viewer.setFrameNo(frame);
    }   
    if (getParameter("Points")!=null) {
      viewer.setColorVisibility(Points3d.color,getBoolParameter("Points"));
    }
  }

  public void setDistribution(){
    int n;
    String choice =dataChoice.getSelectedItem();
    if (choice.equals("Wedge Block")) {
      n = wedgeBlockData.length;
    } else {
      try {
	n = Math.max(Integer.parseInt(dataSize.getText()),3);
      } catch (NumberFormatException e) {
	n = 32;
      }
    }
    Point3d[] pts = new Point3d[n];
    Object3dList model = new Object3dList(pts.length);
    Point3d.setSeed(getIntParameter("seed",(int)System.currentTimeMillis()));
    if (choice.equals("Wedge Block")) {
      wedgeBlock(pts);
    } else if (choice.equals("In Sphere")) {
      randomInSphere(pts);
    } else if (choice.equals("On Sphere")) {
      randomOnSphere(pts);
    } else if (choice.equals("On Paraboloid")) {
      randomOnParaboloid(pts);
    } else if (choice.equals("In Cube")) {
      randomInCube(pts);
    } else if (choice.equals("Gaussian")) {
      randomGaussian(pts);
    } else if (choice.equals("On Circle")) {
      randomOnCircle(pts);
    } else if (choice.equals("In Circle")) {
      randomInCircle(pts);
    } else if (choice.equals("In Square")) {
      randomInSquare(pts);
    }
    dataSize.setText(Integer.toString(pts.length));
    points = new Point3dObject3d[pts.length];
    for (int i=0; i <pts.length; i++) {
      points[i] = new Point3dObject3d(pts[i]);
    }
  }

  public boolean action(Event evt, Object arg) {
    //Timer.printall();
    if (evt.target.equals(dataChoice)||evt.target.equals(dataSize)) {
      setDistribution();
      viewer.setModel(selectModel(modelChoice.getSelectedItem()));
      return true;
    } else if (evt.target == moreButton){
      viewer.moreControls(moreButton);
      return true;
    } else if (evt.target == dimensionChoice){
      selectDimension();
      setDistribution();
      viewer.setModel(selectModel(modelChoice.getSelectedItem()));
      return true;
    } else {
      return super.action(evt,arg);
    }
  }
       
  
  private void randomInSphere(Point3d[] pts) {
    for (int i=0; i <pts.length; i++) {
      pts[i] = Point3d.randomInSphere().scale(0.75);
    }
  }      

  private void randomOnSphere(Point3d[] pts) {
    for (int i=0; i <pts.length; i++) {
      pts[i] = Point3d.randomOnSphere().scale(0.75);
    }
  }      

  private void randomOnCircle(Point3d[] pts) {
    for (int i=0; i <pts.length; i++) {
      pts[i] = Point3d.randomOnCircle().scale(0.75);
    }
  }      

  private void randomInCircle(Point3d[] pts) {
    for (int i=0; i <pts.length; i++) {
      pts[i] = Point3d.randomInCircle().scale(0.75);
    }
  }      

  private void randomInCube(Point3d[] pts) {
    for (int i=0; i <pts.length; i++) {
      pts[i] = Point3d.random().add(new Point3d(-0.5,-0.5,-0.5)).scale(1.25);
    }
  }      

  private void randomInSquare(Point3d[] pts) {
    for (int i=0; i <pts.length; i++) {
      pts[i] = Point3d.random().add(new Point3d(-0.5,-0.5,-0.5)).scale(1.25,1.25,0);
    }
  }      

  private void randomOnParaboloid(Point3d[] pts) {
    for (int i=0; i <pts.length; i++) {
      Point3d temp = Point3d.random().add(new Point3d(-0.5,-0.5,-0.5)).scale(2,2,0);
      pts[i] = temp.add(new Point3d(0,0,temp.dot(temp)-0.5)).scale(0.625);
    }
  }      

  private void randomGaussian(Point3d[] pts) {
    for (int i=0; i <pts.length; i++) {
      pts[i] = Point3d.randomGaussian().scale(0.3);
    }
  }      

  

  private static double[][] wedgeBlockData = {
    {-2,1,.5},{-2,2,.5},{-.5,1,.5},{-.5,2,.5},{-.5,1,.8},{-.5,2,.8},//wedge
    {-1,1.5,.6},{-1.5,1.5,.6},//extra points inside wedge
    {1,1.8,2},{2,1.8,2},{2,1.2,2},{1,1.2,2},{1,1.8,-1}, //block
    {2,1.8,-1},{2,1.2,-1},{1,1.2,-1}};                 //block
  private void wedgeBlock(Point3d[] pts){
    for (int i=0; i < wedgeBlockData.length; i++){
      pts[i] = new Point3d(wedgeBlockData[i][0],
			   wedgeBlockData[i][1],
			   wedgeBlockData[i][2]);
      pts[i]=pts[i].add(new Point3d(0,-1.5,-.5)).scale(0.375).add(Point3d.randomInSphere().scale(0.003));//add a bit of noise
    }
  }

  public boolean mouseEnter(Event e, int x, int y){
    if (dimensionChoice.getSelectedItem().equals("3D")) {
      showStatus("Hold the mouse button down and move the mouse to rotate the hull");
    }
    return true;
  }

  public Choice createModelChoice() {
    if (modelChoice == null) {
      modelChoice = new Choice();
      modelChoice.addItem("Incremental");
      modelChoice.addItem("Gift Wrap");
      modelChoice.addItem("Divide and Conquer");
      modelChoice.addItem("QuickHull");
    }
    return modelChoice;
  }

  /** override this method if we want a choice of models */
  public Object3dList selectModel(String choice) {
    Object3dList model;
    HullAlgorithm algorithm;
    if (choice.equals("Incremental")) {
      algorithm = new Incremental(points);
    } else if (choice.equals("Gift Wrap")) {
      algorithm = new GiftWrap(points);
    } else if (choice.equals("Divide and Conquer")) {
      algorithm = new DivideAndConquer(points);
    } else if (choice.equals("QuickHull")) {
      algorithm = new QuickHull(points);
    } else {
      algorithm = new Incremental(points); //should not get here
    }
    if (dimensionChoice.getSelectedItem().equals("2D")) {
      model = algorithm.build2D();
    } else {
      model = algorithm.build();
    }
    viewer.extraColors(algorithm.extraColors());
    model.addElement(points3d.set(points));

    //hack to render applet view of Incremental algorithm
    if (!getBoolParameter("vrml")&&choice.equals("Incremental")){
      for (int i = 0; i < points.length; i++){
	if (points[i].getSelectFrame()>=0){
	  model.addElement(new Point3dObject3d(points[i],points[i].getSelectFrame()));
	}
      }
    }
    
    model.addElement(axes);
    return model;
  }

  /** Set things up for 2D or 3D display */
  public void selectDimension(){
    if (dimensionChoice.getSelectedItem().equals("2D")) {
      viewer.setView("Down Z");
      viewer.setViewLock(true);
      viewer.setColorVisibility(Points3d.color,true);
      mainPanel.remove(dataChoice);
      mainPanel.add(dataChoice2d,2);
      dataChoice = dataChoice2d;
      validate();
    } else {
      viewer.setViewLock(false);
      viewer.setView("Home");
      viewer.setColorVisibility(Points3d.color,false);
      mainPanel.remove(dataChoice);
      mainPanel.add(dataChoice3d,2);
      dataChoice = dataChoice3d;
      validate();
    }
  }

  /** defaultModel to display */
  public Object3dList defaultModel() {
    dimensionChoice.select(getParameter("dimension"));
    modelChoice.select(getParameter("model"));
    return selectModel(getParameter("model"));
  }

  public String[][] getDefaultParameters() {
    String[][] defaults = {
      // Array of arrays of strings describing each parameter.
      // Format: parameter name, value
      {"bgcolor", "ffffff"},
      {"controls", "on"},
      {"dimension", "3D"},
      {"model","Incremental"},
      {"distribution","In Sphere"},
      {"npoints","32"},
      {"Normal","on"},
      {"Added","on"},
      {"Deleted","on"},
      {"Axes","on"},
      {"Left Hull","on"},
      {"Right Hull","on"},
      {"Selected","on"}
    };
    return defaults;
  }

  // Return information suitable for display in an About dialog box.
  public String getAppletInfo() {
    return "3D Hull Applet v1.13\nWritten by Tim Lambert.";
  }

  public static void main(String[] args){
    AppletHull applet = new AppletHull();
    applet.setStub(new FakeAppletStub("AppletHull.class?"+args[0]+"&cgi=on"));
    applet.init();
    System.exit(0);
  }
 
}