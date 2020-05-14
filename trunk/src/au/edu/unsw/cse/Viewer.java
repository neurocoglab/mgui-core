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
/**
 * This class implements a viewer for 3d objects
 */

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.util.Hashtable;

public class Viewer extends Panel{
  private View3d v = new View3d(Point3d.ijk);
  private View3dInfo home = new View3dInfo(Point3d.ijk);
  private boolean repaint=false;
  private Button set = new Button("Set Home");
  private int noColors = 0;
  private Panel panelPanel; //panel containing all the others
  private Panel viewPanel;
  private Choice projectionChoice;
  private Panel visPanel = null;
  private AnimationWidget animationWidget;
  private int noPanelsDisplayed = 0; //how many panels are displayed
  private Button lessButton;
  private Painter painter;
  private Thread engine;
  private Object3d model;
  private boolean locked = false;  //is view direction locked?
  private boolean spinning = false; // are we spinning the model?
  private double spinx,spiny; //spin speed in pixels/microsecond
  private long timeget; //when did the last get happen?
  private ColorControl[] colcontrols = new ColorControl[10];
  private int[] extras = new int[0];

  private Object[][] views = {
    {"Home",home},
    {"Up Z",new View3dInfo(Point3d.k.scale(-1),false)},
    {"Down Z",new View3dInfo(Point3d.k,false)},
    {"Up Y",new View3dInfo(Point3d.j.scale(-1),Point3d.i,false)},
    {"Down Y",new View3dInfo(Point3d.j,Point3d.i,false)},
    {"Up X",new View3dInfo(Point3d.i.scale(-1),false)},
    {"Down X",new View3dInfo(Point3d.i,false)}};
  private Hashtable hash = new Hashtable(views.length);
  protected Applet3d parent; //parent applet

  public Viewer(Applet3d parent) {
    this.parent=parent;
    setLayout(new BorderLayout());
    if (parent.getParameter("vrml")!=null){
      painter=new PaintVRML(parent);
    } else {
      painter=new Paint3d(this);
      add("Center",(Canvas)painter);
    }
    panelPanel = new Panel();
    panelPanel.setLayout(new BorderLayout());
    viewPanel = new Panel();
    projectionChoice = new Choice();
    projectionChoice.addItem("Perspective");
    projectionChoice.addItem("Orthographic");
    viewPanel.add(projectionChoice);
    for (int i = 0; i < views.length; i++){
      viewPanel.add(new Button((String)views[i][0]));
      hash.put(views[i][0],views[i][1]);
    }
    viewPanel.add(set);

    visPanel = new Panel();
    visPanel.add(new Label("Show:"));
    animationWidget = new AnimationWidget(this);
    animationWidget.add(lessButton=new Button("Less Controls"));
    animationWidget.setAutoPlay(true);
    panelPanel.add("South",animationWidget);
  }

  Button moreButton; //the button user clicks to get more controls
  /** show controls for colour, animation, etc */
  public void moreControls(Button moreButton){
    this.moreButton = moreButton;
    if (noPanelsDisplayed == 0){
      add("South",panelPanel);
      animationWidget.setAutoPlay(false);
    } else if (noPanelsDisplayed == 1) {
      panelPanel.add("Center",visPanel);
      if (locked) {
	moreButton.disable();
      }
    } else if (noPanelsDisplayed == 2) {
      panelPanel.add("North",viewPanel);
      moreButton.disable();
    }
    remove(panelPanel); // Netscape bug
    add("South",panelPanel); //work around 
    validate();
    noPanelsDisplayed++;
 }

  /** remove controls for colour, animation, etc */
  public void lessControls(){
    if (noPanelsDisplayed == 1){
      remove(panelPanel);
      animationWidget.setAutoPlay(true);
    } else if (noPanelsDisplayed == 2) {
      panelPanel.remove(visPanel);
      if (locked) {
	moreButton.enable();
      }
    } else if (noPanelsDisplayed == 3) {
      panelPanel.remove(viewPanel);
      moreButton.enable();
    }
    remove(panelPanel);
    add("South",panelPanel);
    validate();
    noPanelsDisplayed--;
  }

  /** Add a checkbox allowing user to set objects of this colour to be invisible */ 
  public int addColor(String label, String tip, boolean state, Color c, boolean shouldAdd){
    if (visPanel == null) {
    }
    colcontrols[noColors] = new ColorControl(label,tip,state,c,noColors,this);
    if (shouldAdd) {
      visPanel.add (colcontrols[noColors]);
    }
    return noColors++;
  }

  /** Add a color that the user cannot make invisible*/ 
  public int addColor(Color c){
    v.setColor(noColors,c);
    return noColors++;
  }


  /** Remove any existing extra colors  and replace with a new set */
  public void extraColors(int[] extras){
    for (int i=0; i<this.extras.length; i++){
      visPanel.remove(colcontrols[this.extras[i]]);
    }
    this.extras = extras;
    for (int i=0; i<extras.length; i++){
      visPanel.add(colcontrols[extras[i]]);
    }
    visPanel.validate();
  }

  /** Set the visibility of a colour */
  public void setColorVisibility(int col, boolean visibility){
    colcontrols[col].setState(visibility);
    colcontrols[col].action(null,null);
  }

  public void setModel(Object3d model) {
    this.model = model;
    v.setLastFrame(model.getLastFrame());
    painter.setModel(model);
    animationWidget.setLastFrame(model.getLastFrame());
    animationWidget.first();
  }

  public void transform(Matrix3D T) {
    model.transform(T);
  }

	
  public void start(){
    engine = new Thread(painter);
    engine.setPriority(Thread.MIN_PRIORITY);
    engine.start();
  }
  
  public void stop(){
    if (engine != null) {
      engine.stop();
      engine = null; // so it can be garbage collected
    }
    animationWidget.stop();
    spinning = false;
  }

  @Override
public void paint(Graphics g) {
    painter.paint(g);
  }
 

  /** set the view direction.  We pass the name on the button.
   return true if we are succesful.
   */
  public boolean setView(String viewname){
    View3dInfo view = (View3dInfo)hash.get(viewname);
    if (!locked && view != null) {
      spinning = false;
      v.set(view);
      if (v.orthographic()) {
	projectionChoice.select(1);
      } else {
	projectionChoice.select(0);
      }
      put();
      return true;
    } else {
      return false;
    }
  }

  public boolean setView(View3dInfo view){
    v.set(view);
    return true;
  }

  public void setHome(View3dInfo view){
    home.set(view);
  }

  public void setViewLock(boolean locked){
    this.locked = locked;
    if (locked) {
      if (noPanelsDisplayed == 3){
	lessControls();
	moreButton.disable();
      } else if (noPanelsDisplayed == 2){
	moreButton.disable();
      }
    } else if (noPanelsDisplayed == 2){
      moreButton.enable();
    }
  }
	
  @Override
public boolean action(Event e, Object arg){
    if (e.target == lessButton){
      lessControls();
      return true;
    } else if (e.target == set){
      home.set(v);
      System.out.println(v);
      return true;
    } else if (e.target == projectionChoice){
      v.setPerspective(((String)arg).equals("Perspective"));
      put();
      return true;
    } else {
      return setView((String)arg);
    }
  }
			
  protected static final int SENSITIVITY = 3; //how sensitive are we to mouse movement?
  public synchronized void put (int deltax, int deltay, int modifiers){
    if ((Event.ALT_MASK & modifiers) !=0) {
      v.pan(deltax,deltay);
    } else if ((Event.META_MASK & modifiers) !=0) {
      v.zoom(Math.pow(1.01,-deltax));
    } else if (modifiers == 0&&!locked) {
      v.adjustCamera(SENSITIVITY*deltax,SENSITIVITY*deltay);
    }
    put();
  }

  public synchronized void put (){
    repaint = model != null; //no point in painting if there is nothing to paint
    notify();
  }

  public synchronized void put (int i, Color c){
    v.setColor(i,c);
    put();
  }

  public void setGraphics(Graphics g,int width, int height){
    v.setGraphics(g,width,height);
    put();
  }

  public void setWindow(double wx,double wy,double wwidth,double wheight){
    v.setWindow(wx,wy,wwidth,wheight);
  }

  /** this sets the frame no in the View */
  public synchronized void putFrameNo(int frameNo) {
    v.setFrameNo(frameNo);
    put();
  }
  
  /** this sets the frame no in the AnimationWidget */
  public void setFrameNo(int frameNo) {
    animationWidget.setFrameNo(frameNo);
  }
  
  public void spinOn(double spinx, double spiny) {
    spinning = true;
    timeget = System.currentTimeMillis();
    this.spinx = spinx;
    this.spiny = spiny;
    put();
  }
  
  public void spinOff() {
    spinning = false;
  }
  
  //these mouse event methods ought to be in Paint3d, but don't work there (Java bug)
  //remember where mouse clicked
  protected int lastx, lasty;
  protected long lastt;
  @Override
public boolean mouseDown(Event e, int x, int y){
    if(e.target == this) {
      lastx = x; lasty = y; lastt = e.when;
      lastsect = 0;
      spinOff();
      return true;
    } else {
      return false;
    }
  }
  
  protected static final int THRESHOLD = 2;
  @Override
public boolean mouseUp(Event e, int x, int y){
    if(e.target == this) {
      calcLastSec(x,y,e.when);
      if (e.modifiers==0&&lastsect!=0&&!locked&&
	  ((Math.abs(lastsecx) > THRESHOLD) || (Math.abs(lastsecy) > THRESHOLD))) {
	spinOn(lastsecx / (float) lastsect, lastsecy / (float) lastsect);
      } else {
	lastsect = 0;
      }
      return true;
    } else {
      return false;
    }
  }
  
  @Override
public boolean mouseDrag(Event e, int x, int y){
    if (e.target == this) {
      put(x - lastx,y - lasty,e.modifiers);
      if (e.modifiers == 0){
	calcLastSec(x,y,e.when);
      } else {
	//work around Netscape bug
	lastt = Long.MAX_VALUE;
	lastsecx = 0;
	lastsecy = 0;
      }
      lastx = x; lasty = y; lastt = e.when;
      return true;
    } else {
      return false;
    }
  }
  
  protected int lastsecx, lastsecy; // mouse movement in last second
  protected int lastsect; //if mouse has moved for less than a second this has that time
  protected final static int TIMESLICE = 1000; 
	/** Estimate mouse movement in last second */
  protected void calcLastSec(int x, int y, long t) {
    if (t <= lastt) return;
    int deltat = (int) (t - lastt);
    int deltax = (x - lastx) * TIMESLICE / deltat;
    int deltay = (y - lasty) * TIMESLICE / deltat;
    if (deltat > TIMESLICE) {
      lastsect = TIMESLICE;
      lastsecx = deltax;
      lastsecy = deltay;
    } else {
      int weight = Math.min(TIMESLICE-deltat,lastsect);
      int totweight = weight + deltat;
      lastsecx = (deltax*deltat + lastsecx*weight)/totweight;
      lastsecy = (deltay*deltat + lastsecy*weight)/totweight;
      lastsect = Math.min(deltat+lastsect, TIMESLICE);
    }
  }

  public synchronized View3d get(){
    if (spinning) {
      int deltat = (int) (System.currentTimeMillis() - timeget);
      timeget = System.currentTimeMillis();
      v.adjustCamera(SENSITIVITY*deltat*spinx,SENSITIVITY*deltat*spiny);
      put();
    }
    while (!repaint) {
      try {
	wait();
      } catch (InterruptedException e) {
      }
    }
    repaint = false;
    return (View3d)v.clone();
  }

  public View3d getView(){
    return v;
  }

  public String vrmlPROTO() {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i < views.length; i++){
      sb.append(((View3dInfo)views[i][1]).toVRML((String)views[i][0]));
    }
    Component[] controls = visPanel.getComponents();
    for (int i=0; i<controls.length; i++){
      if (controls[i] instanceof ColorControl){
	sb.append(((ColorControl)controls[i]).vrmlPROTO());
      }
    }
    return sb.toString();
  }
  public void toVRML(VRMLState v) {
    int index = 0;
    v.putPROTO("Viewer",vrmlPROTO());
    Component[] controls = visPanel.getComponents();
    for (int i=0; i<controls.length; i++){
      if (controls[i] instanceof ColorControl){
	ColorControl cc = (ColorControl)controls[i];
	cc.toVRML(v,index);
	index += cc.idVRML().length();
      }
    }
  }
  
  public String idVRML(int col) {
    return colcontrols[v.getColorIndex(col)].idVRML();
  }

} 