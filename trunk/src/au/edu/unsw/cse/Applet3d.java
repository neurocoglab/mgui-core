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
/**
 * Applet allowing interactive rotation of 3D logo
 */
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

public
class Applet3d extends Applet{
  TextField transformValues; //for entering parameters of transformation
  TextField windowValues; //for entering window dimensions
  Choice transform; //so user can select what sort of transformation to do
  Choice modelChoice; //so user can select which model to display
  Button reset;
  Viewer viewer;
  Hashtable parameters = new Hashtable();; //parameters defined in query string of URL
  
  Viewer makeViewer(){
    Viewer v = new Viewer(this);
    v.setWindow(-6,-6,12,12);
    return v;
  }


  @Override
public void init(){
    //fill in default parameters
    String[][] defaults = getDefaultParameters();
    for(int i = 0; i < defaults.length; i++){
      if(super.getParameter(defaults[i][0])==null){
	parameters.put(defaults[i][0],defaults[i][1]);
      }
    }
    //fill in parameters hash table from URL query suffix
    String docbase = getDocumentBase().toString();
    int queryindex = docbase.indexOf('?');
    if (queryindex != -1){
      String query = urlDecode(docbase.substring(queryindex+1));
      StringTokenizer st = new StringTokenizer(query,"&");
      while (st.hasMoreTokens()){
	String namevalue = st.nextToken();
	int equalsindex = namevalue.indexOf('=');
	if (equalsindex != -1){
	  parameters.put(namevalue.substring(0,equalsindex),namevalue.substring(equalsindex+1));
	}
      }
    }
	  
    Color bgcolor = getColorParameter("bgcolor");
    if (bgcolor != null){
      setBackground(bgcolor);
    }
    
    //We want the canvas to take all the space not used by the
    //Panels, so we can't use the default FlowLayout manager but the
    //BorderLayout manager
    setLayout(new BorderLayout());
    add("Center",viewer=makeViewer());
    viewer.setViewLock(getBoolParameter("locked"));

    if (getParameter("transforms")!=null) {
      
      //panel containing p1 and p2
      Panel p = new Panel();
      p.setLayout(new BorderLayout());
      
      //panel containing definition of window
      Panel p1 = new Panel();
      reset = new Button("Reset");
      p1.add(reset);
      p1.add(new Label("Window"));
      windowValues = new TextField("-6 -6 12 12",20);
      p1.add(windowValues);
      p.add("North",p1);
      
      //panel containing definition of a transformation
      Panel p2 = new Panel();
      transform = new Choice();
      transform.addItem("translation");
      transform.addItem("scale");
      transform.addItem("rotation x");
      transform.addItem("rotation y");
      transform.addItem("rotation z");
      p2.add(transform);
      transformValues = new TextField("0 0 0",20);
      p2.add(transformValues);
      p.add("South",p2);
      add("North", p);
    }

    modelChoice = createModelChoice();
    viewer.setModel(defaultModel());
    if (modelChoice!=null) {
      add("South", modelChoice);
    }
  }

  @Override
public void start(){
    getGraphics().clearRect(0,0,size().width,size().height);
    viewer.start();
  }
  
  @Override
public void stop(){
    viewer.stop();
  }

  @Override
public void paint(Graphics g) {
    viewer.paint(g);
  }
  
  @Override
public void update(Graphics g) {
    paint(g);
  }


  @Override
public boolean mouseEnter(Event e, int x, int y){
    showStatus("Hold the mouse button down and move the mouse to rotate the logo");
    return true;
  }

  @Override
public boolean mouseExit(Event e, int x, int y){
    showStatus("");
    return true;
  }
  @Override
public boolean action(Event evt, Object arg) {
    Matrix3D transformMat = null;
    if (evt.target.equals(reset) || evt.target.equals(modelChoice)) {
      viewer.setModel(selectModel(modelChoice.getSelectedItem()));
    } else if (evt.target.equals(windowValues)) {
      String s = (String) arg; //user pressed return. s is the String they entered
      try {
	StringTokenizer st = new StringTokenizer(s);
	float[] w = new float[4]; //4 element array to hold the result
	for (int i = 0; i < w.length; i++) {
	  w[i] = Float.valueOf(st.nextToken()).floatValue();
	}
	viewer.setWindow(w[0],w[1],w[2]-w[0],w[3]-w[1]);

      } catch (NoSuchElementException e) {
	showStatus("Enter four numbers separated by spaces");
      } catch (NumberFormatException e) {
	showStatus("Enter four numbers separated by spaces");
      }
    } else if (evt.target.equals(transformValues)) {
      String s = (String) arg; //user pressed return. s is the String they entered
      try {
	transformMat = new Matrix3D();
	StringTokenizer st = new StringTokenizer(s);
	// Java has a rather long winded way of converting a string to a float
	float x = Float.valueOf(st.nextToken()).floatValue();
	if (transform.getSelectedItem().equals("rotation x")) {
	  transformMat.xrot(x);
	} else if (transform.getSelectedItem().equals("rotation y")) {
	  transformMat.yrot(x);
	} else if (transform.getSelectedItem().equals("rotation z")) {
	  transformMat.zrot(x);
	} else {
	  float y = Float.valueOf(st.nextToken()).floatValue();
	  float z = Float.valueOf(st.nextToken()).floatValue();
	  if (transform.getSelectedItem().equals("translation")) {
	    transformMat.translate(x,y,z);
	  } else 	if (transform.getSelectedItem().equals("scale")) {
	    transformMat.scale(x,y,z);
	  }
	}
	System.out.println(transformMat);
	viewer.transform(transformMat);

      } catch (NoSuchElementException e) {
	showStatus("Enter one or two numbers separated by a space");
      } catch (NumberFormatException e) {
	showStatus("Enter one or two numbers separated by a space");
      }
    }
    return true; //true means we handled the event
  }



  /** override this method if we want a choice of models */
  public Choice createModelChoice() {
    return null;
  }

  /** override this method if we want a choice of models */
  public Object3dList selectModel(String choice) {
    return defaultModel();
  }

  /** defaultModel to display */
  public Object3dList defaultModel() {
    return new CSELogo();
  }

  /** decode URL, replacing escapes like %20 and + with the appropriate char */
  public String urlDecode(String s){
    StringBuffer sb = new StringBuffer();
    int i = 0;
    while (i < s.length()) {
      if (s.charAt(i)=='%'){
	sb.append((char)Integer.parseInt(s.substring(i+1,i+3),16));
	i += 3;
      } else {
	if (s.charAt(i)=='+'){
	  sb.append(' ');
	} else {
	  sb.append(s.charAt(i));
	}
	i++;
      }
    }
    return sb.toString();
  }

  @Override
public String getParameter(String name){
    String value = (String)parameters.get(name);
    if (value == null){
      return super.getParameter(name);
    } else {
      return value;
    }
  }

  public String[][] getDefaultParameters() {
    String[][] defaults = {
      // Array of arrays of strings describing each parameter.
      // Format: parameter name, value
      {"bgcolor", "ffffff"}
    };
    return defaults;
  }

  // Read the specified parameter.  Interpret it as a hexadecimal
  // number of the form RRGGBB and convert it to a color.
  protected Color getColorParameter(String name) {
    String value = this.getParameter(name);
    int intvalue;
    try { intvalue = Integer.parseInt(value, 16); }
    catch (NumberFormatException e) { return null; }
    return new Color(intvalue);
  }

  public double getDoubleParameter(String name, double deefault) {
    String value = this.getParameter(name);
    double doublevalue;
    try { doublevalue = Double.valueOf(value).doubleValue(); }
    catch (NumberFormatException e) { return deefault; }
    return doublevalue;
  }

  public int getIntParameter(String name, int deefault) {
    String value = this.getParameter(name);
    int intvalue;
    try { intvalue = Integer.parseInt(value); }
    catch (NumberFormatException e) { return deefault; }
    return intvalue;
  }

  public Vector getIntVectorParameter(String name) {
    String value = this.getParameter(name);
    if (value==null){
      return null;
    }
    Vector v = new Vector();
    try {
      StringTokenizer st = new StringTokenizer(value,",");
      while (st.hasMoreTokens()){
	v.addElement(Integer.valueOf(st.nextToken()));
      }
    }
    catch (NumberFormatException e) { return null; }
    return v;
  }
   
  public boolean getBoolParameter(String name) {
    String value = this.getParameter(name);
    return value!=null && value.equals("on");
  }

  // Return info about the supported parameters.  Web browsers and applet
  // viewers should display this information, and may also allow users to
  // set the parameter values.
  @Override
public String[][] getParameterInfo() {
    String[][] info = {
      // Array of arrays of strings describing each parameter.
      // Format: parameter name, parameter type, parameter description
      {"bgcolor", "hexadecimal colour value", "background colour"}
    };
    return info;
  }
    
  // Return information suitable for display in an About dialog box.
  @Override
public String getAppletInfo() {
    return "3D Applet.\nWritten by Tim Lambert.";
  }
 
}