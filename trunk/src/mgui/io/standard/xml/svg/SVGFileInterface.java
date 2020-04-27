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

package mgui.io.standard.xml.svg;

/**********************
 * Extracts data from an SVG file and stores it in a SectionSet3DInt object,
 * based upon SVGOptions. Can assign objects to selection sets based upon colour
 * 
 */

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.jogamp.vecmath.Point2f;

import mgui.geometry.Polygon2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.io.DataFileInterface;
import mgui.numbers.MguiBoolean;

//import uk.ac.ed.paxinos_handler.PaxinosHandler;


public class SVGFileInterface extends DataFileInterface {

	public static final int SET_BY_COLOUR = 0;
	
	public int thisMode;
	//private ArrayList<Color> selectionColour = new ArrayList<Color>();
	protected HashMap<Color, ShapeSelectionSet> colourMap = new HashMap<Color, ShapeSelectionSet>();
	public ArrayList<ShapeSelectionSet> selectionSets = new ArrayList<ShapeSelectionSet>();
	public ShapeModel3D model;
	public SVGInputOptions SVGOptions;
	
	//public PaxinosHandler colourTable;
	//public File colourTableFile;
	
	private Point2f currentPt = new Point2f();
	private Point2f lastPt = new Point2f();
	
	public SVGFileInterface(){
		
	}
	
	public ShapeSet3DInt getShapes3D(){
		
		return null;
	}
	
	public void setOptions (SVGInputOptions opt){
		SVGOptions = opt;
	}
	
	public void getSection3DShapes(SectionSet3DInt thisSet, int section){
		try{
			ShapeSet2DInt thisShapeSet = getInputShapeSet2D(new FileInputStream(inputFile));
			if (thisShapeSet != null)
				thisSet.addUnionSet(thisShapeSet, section, true, true);
				//setMode(SET_BY_COLOUR);
		}
		catch (FileNotFoundException e){
			InterfaceSession.log("SVG input file not found: " + inputFile.getName());
		}
	}
	
	public void setMode(int mode){
		thisMode = mode;
	}
	
	//return selection set associated with this Color
	//if none exists, create new one
	private ShapeSelectionSet getSelectionSet(Color thisColour, String name, boolean addItr){
		//for (int i = 0; i < selectionColour.size(); i++)
		//	if (selectionColour.get(i).getRGB() == thisColour.getRGB())
		//		return selectionSets.get(i);
		ShapeSelectionSet selSet = colourMap.get(thisColour);
		if (selSet != null) return selSet;
		
		selSet = new ShapeSelectionSet();
		selSet.setName(name);
		if (addItr)
			selSet.setName(name + String.valueOf(model.selections.size()));
		//selectionColour.add(thisColour);
		colourMap.put(thisColour, selSet);
		
		selectionSets.add(selSet);
		//model.addSelectionSet(selSet);
		return selSet;
	}
	
	public ShapeSet2DInt getInputShapeSet2D(InputStream input_stream){
		ShapeSet2DInt shapeSet = new ShapeSet2DInt();
		//shapeSet.setUpdates(false);
		ShapeSelectionSet thisSet = null; // = new Shape2DSelectionSet();
		Polygon2D thisPoly;
		Polygon2DInt newPoly;
		//PaxinosHandler colourTable;
		
		try {
			SVGParser parser = new SVGParser();
			parser.parse(input_stream);
			SVGPath[] paths = parser.getPaths();	
		
			for (int i = 0; i < paths.length; i++) {
				SVGPath path = paths[i];
				
				if (path.colour != null && path.colour.equals(Color.yellow)) {
					// the color of the yellow crosshairs, so ignore!
				} else {
						
					if (SVGOptions.colourFileOptions == SVGInputOptions.SVGMap.Increment)
						thisSet = getSelectionSet(path.colour, SVGOptions.prefixStr, true);
					
					//if (SVGOptions.colourFileOptions == SVGInputOptions.SVGMap.TableMap && 
					//		colourTable != null)
					//	thisSet = getSelectionSet(path.colour, colourTable.getAcronym(path.colour), false);
					
					thisPoly = new Polygon2D();
					
					currentPt.x = 0;
					currentPt.y = 0;
					
					for (int j = 0; j < path.instructions.size(); j++) {
						if (path.instructions.get(j) instanceof MoveTo){
							handleMoveTo((MoveTo)path.instructions.get(j));
							if (thisPoly.vertices.size() > 0){
								//add current polygon and start a new one
								newPoly = new Polygon2DInt(thisPoly);
								if (SVGOptions.setLineColour)
									newPoly.setAttribute("LineColour", path.colour);
								if (SVGOptions.setFillColour){
									newPoly.setAttribute("FillColour", path.colour);
									newPoly.setAttribute("HasFill", new MguiBoolean(true));
									}
								newPoly.setAttribute("LabelNodes", new MguiBoolean(false));
								
								shapeSet.addShape(newPoly, true, true);
								thisPoly = new Polygon2D();
								}
							}
							
						if (path.instructions.get(j) instanceof LineTo){
							handleLineTo((LineTo)path.instructions.get(j));
							if (thisPoly.vertices.size() == 0)
								thisPoly.vertices.add((Point2f)lastPt.clone());
							thisPoly.vertices.add((Point2f)currentPt.clone());
							
							}
						if (path.instructions.get(j) instanceof CurveTo){
							handleCurveTo((CurveTo)path.instructions.get(j));
							if (thisPoly.vertices.size() == 0)
								thisPoly.vertices.add((Point2f)lastPt.clone());
							thisPoly.vertices.add((Point2f)currentPt.clone());
							}
						}
					
					//correct for first-last coincidence
					if (GeometryFunctions.isCoincident(thisPoly.vertices.get(0),
							thisPoly.vertices.get(thisPoly.vertices.size() - 1)))
							thisPoly.vertices.remove(thisPoly.vertices.size() - 1);
					
					newPoly = new Polygon2DInt(thisPoly);
					if (SVGOptions.setLineColour)
						newPoly.setAttribute("LineColour", path.colour);
					if (SVGOptions.setFillColour){
						newPoly.setAttribute("FillColour", path.colour);
						newPoly.setAttribute("HasFill", new MguiBoolean(true));
						}
					newPoly.setAttribute("LabelNodes", new MguiBoolean(false));
					if (!newPoly.validateNodes()){
						InterfaceSession.log("Invalid polygon: " + i);
						newPoly.printNodes();
					}
					shapeSet.addShape(newPoly, true, true);
					
					if (thisSet != null)
						thisSet.addShape(newPoly); //, shapeSet);
					
				}	
			}	
	} catch (Exception ex) {
		InterfaceSession.log("SVG Handler Error: " + ex);
		ex.printStackTrace();
		return null;
		}
		return shapeSet;
	}
	
	public void handleMoveTo(MoveTo i){
		if (i.getMode() == SVGParser.Mode.RELATIVE){
			currentPt.x += i.numbers[0];
			currentPt.y += i.numbers[1];
		} else {
			currentPt.x = i.numbers[0];
			currentPt.y = i.numbers[1];
		}
			
	}
	
	public void handleLineTo(LineTo i){
		lastPt.x = currentPt.x;
		lastPt.y = currentPt.y;
		
		if (i.getMode() == SVGParser.Mode.RELATIVE){
			currentPt.x += i.numbers[0];
			currentPt.y += i.numbers[1];
		} else {
			currentPt.x = i.numbers[0];
			currentPt.y = i.numbers[1];
		}
		
	}
	
	public void handleCurveTo(CurveTo i){
		lastPt.x = currentPt.x;
		lastPt.y = currentPt.y;
		
		if (i.getMode() == SVGParser.Mode.RELATIVE){
			currentPt.x += i.numbers[4];
			currentPt.y += i.numbers[5];
		} else {
			currentPt.x = i.numbers[4];
			currentPt.y = i.numbers[5];
		}
	}
	
}