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

package mgui.interfaces.shapes.volume;

import javax.swing.JComboBox;

import mgui.interfaces.InterfaceOptions;


/*************************************
 * 
 * Class to specify parameters for volume masking.
 * 
 * @author Andrew Reid
 * @version 1.0
 */

public class VolumeMaskOptions_old extends InterfaceOptions {

	//constants for multiplying input channel
	public static final int FACT_EXP = 1;
	public static final int FACT_LINEAR = 0;
	public static final int FACT_LOG = 2;
	
	//constants defining input type
	public static final int INPUT_DATA = 0;		//data channel input
	public static final int INPUT_CONST = 1;	//constant value as input
	
	//constants for mask shape
	public static final int SHAPE_ALL = 0;
	public static final int SHAPE_BOX = 1;
	public static final int SHAPE_WEDGE = 2;
	public static final int SHAPE_SPHERE = 3;
	public static final int SHAPE_AXIS1 = 4;
	public static final int SHAPE_AXIS2 = 5;
	public static final int SHAPE_AXIS3 = 6;
	
	//general parameters
	public double inputFactor = 1.0;
	public int inputOp = FACT_LINEAR;
	public int inputType = INPUT_DATA;
	public int inputChannel = 0;
	public int outputChannel = 0;
	public double inputValue = 0.0;
	public int maskShape = SHAPE_ALL;
	public double minThreshold = 0.0;
	public double maxThreshold = 1.0;
	public double smoothingExp = 1.0;
	public boolean invertData = false;
	public boolean invertShape = false;
	public boolean invertMin = false;
	public boolean invertMax = false;
	
	//shape-specific parameters
	public int x1, y1, z1;
	public int x2, y2, z2;
	public int p1, p2, p3;
	public int a1, a2, a3;
	
	//static functions to set up combos
	public static void setInputTypeList(JComboBox b){
		b.removeAllItems();
		b.addItem("Data channel");
		b.addItem("Constant value");
	}
	
	public static void setInputOpList(JComboBox b){
		b.removeAllItems();
		b.addItem("Linear");
		b.addItem("Exp");
		b.addItem("Log");
	}
	
	public static void setMaskShapeList(JComboBox b){
		b.removeAllItems();
		b.addItem("Entire volume");
		b.addItem("Box");
		b.addItem("Wedge");
		b.addItem("Sphere");
		b.addItem("1-Axis");
		b.addItem("2-Axis");
		b.addItem("3-Axis");	
	}
	
	public static void setAxesList(JComboBox b){
		b.removeAllItems();
		b.addItem("X");
		b.addItem("Y");
		b.addItem("Z");
	}
	
	public int getAxis(int n){
		if (n == 1) return a1;
		if (n == 2) return a2;
		if (n == 3) return a3;
		
		return -1;
	}
	
	public static String getAxisString(int i){
		if (i == 1) return "X";
		if (i == 2) return "Y";
		if (i == 3) return "Z";
		return null;
	}
	
	public static int getAxis(String s){
		if (s.equals("X")) return 1;
		if (s.equals("Y")) return 2;
		if (s.equals("Z")) return 3;
		return -1;
	}
	
	public void setInputType(String t){
		if (t.equals("Data channel")) 
			inputType = INPUT_DATA;
		if (t.equals("Constant value")) 
			inputType = INPUT_CONST;
	}
	
	public void setInputOp(String t){
		if (t.equals("Linear")) inputOp = FACT_LINEAR;
		if (t.equals("Exp")) inputOp = FACT_EXP;
		if (t.equals("Log")) inputOp = FACT_LOG;
	}
	
	public void setMaskShape(String t){
		if (t.equals("Entire volume")) maskShape = SHAPE_ALL;
		if (t.equals("Box")) maskShape = SHAPE_BOX;
		if (t.equals("Sphere")) maskShape = SHAPE_SPHERE;
		if (t.equals("Wedge")) maskShape = SHAPE_WEDGE;
		if (t.equals("1-Axis")) maskShape = SHAPE_AXIS1;
		if (t.equals("2-Axis")) maskShape = SHAPE_AXIS2;
		if (t.equals("3-Axis")) maskShape = SHAPE_AXIS3;	
	}
	
	public void setInputChannel(String t){
		inputChannel = getChannel(t);
	}
	
	public void setOutputChannel(String t){
		outputChannel = getChannel(t);
	}
	
	public static int getChannel(String t){
		String val = t.substring(8, t.length());
		return Integer.valueOf(val).intValue() - 1;
	}
	
	public static int getShape(String t){
		if (t.equals("Entire volume")) return SHAPE_ALL;
		if (t.equals("Box")) return SHAPE_BOX;
		if (t.equals("Sphere")) return SHAPE_SPHERE;
		if (t.equals("Wedge")) return SHAPE_WEDGE;
		if (t.equals("1-Axis")) return SHAPE_AXIS1;
		if (t.equals("2-Axis")) return SHAPE_AXIS2;
		if (t.equals("3-Axis")) return SHAPE_AXIS3;	
		return -1;
	}
	
	public String getShapeString(){
		return getShape(maskShape);
	}
	
	public static String getShape(int i){
		if (i == SHAPE_ALL) return "Entire volume";
		if (i == SHAPE_BOX) return "Box";
		if (i == SHAPE_SPHERE) return "Sphere";
		if (i == SHAPE_WEDGE) return "Wedge";
		if (i == SHAPE_AXIS1) return "1-Axis";
		if (i == SHAPE_AXIS2) return "2-Axis";
		if (i == SHAPE_AXIS3) return "3-Axis";	
		return null;
	}
	
	public String getTypeString(){
		return getType(inputType);
	}
	
	public static String getType(int i){
		if (i == INPUT_CONST) return "Constant value";
		if (i == INPUT_DATA) return "Data channel";
		return null;
	}
	
	public String getOpString(){
		return getOp(inputOp);
	}
	
	public static String getOp(int i){
		if (i == FACT_LINEAR) return "Linear";
		if (i == FACT_EXP) return "Exp";
		if (i == FACT_LOG) return "Log";	
		return null;
	}
}