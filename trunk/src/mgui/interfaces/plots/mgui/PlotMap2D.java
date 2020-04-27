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

package mgui.interfaces.plots.mgui;

import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;

import org.jogamp.vecmath.Point2d;

import mgui.geometry.Shape2D;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;
import mgui.numbers.MguiDouble;
import mgui.util.MathFunctions;


/*******************************************************
 * Specifies a mapping from data units to plot units and to graphic units.
 * 
 * <p>Note on units:
 * 
 * <ul>
 * <li>Data units: values from the original data to be plotted
 * <li>Plot units: values transformed according to the plot layout (e.g., linear, log, exponential, etc.)
 * <li>Graphic units: plot units mapped to graphic units (screen units within the plottable graphics region)
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PlotMap2D implements InterfaceObject{

	public enum AxisType{
		Linear,
		Logarithmic,
		Exponential;
	}
	
	public AttributeList attributes = new AttributeList();
	
	protected Map2D graphic_map; 
	
	public PlotMap2D(){
		_init();
	}
	
	/********************************
	 * Initiate this plot object; to be called from subclasses
	 * 
	 */
	protected void _init(){
		
		ArrayList<String> axis_types = new ArrayList<String>();
		axis_types.add("Linear");
		axis_types.add("Log");
		axis_types.add("Exp");
		AttributeSelection<String> attr = new AttributeSelection<String>("AxisXType", axis_types, String.class, "Linear");
		attributes.add(attr);
		attr = new AttributeSelection<String>("AxisYType", axis_types, String.class, "Linear");
		attributes.add(attr);
		attributes.add(new Attribute<MguiDouble>("FactorX", new MguiDouble(1)));
		attributes.add(new Attribute<MguiDouble>("FactorY", new MguiDouble(1)));
		
	}
	
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	/*******************************************
	 * Returns the factor for the X axis
	 * 
	 * @return
	 */
	public double getFactorX(){
		return ((MguiDouble)attributes.getValue("FactorX")).getValue();
	}
	
	/*******************************************
	 * Returns the factor for the Y axis
	 * 
	 * @return
	 */
	public double getFactorY(){
		return ((MguiDouble)attributes.getValue("FactorY")).getValue();
	}
	
	/*******************************************
	 * Returns the X axis type for this map
	 * 
	 * @return
	 */
	public AxisType getAxisXType(){
		String type = (String)attributes.getValue("AxisXType");
		if (type.equals("Linear")) return AxisType.Linear;
		if (type.equals("Log")) return AxisType.Logarithmic;
		if (type.equals("Exp")) return AxisType.Exponential;
		return AxisType.Linear;
	}
	
	/*******************************************
	 * Returns the Y axis type for this map
	 * 
	 * @return
	 */
	public AxisType getAxisYType(){
		String type = (String)attributes.getValue("AxisYType");
		if (type.equals("Linear")) return AxisType.Linear;
		if (type.equals("Log")) return AxisType.Logarithmic;
		if (type.equals("Exp")) return AxisType.Exponential;
		return AxisType.Linear;
	}
	
	/*******************************************
	 * Maps a data point to a plot point.
	 * 
	 * @param data_point
	 * @return
	 */
	public Point2d mapDataToPlot(Point2d data_point){
		
		Point2d plot_point = new Point2d(data_point);
		double factor_x = this.getFactorX();
		double factor_y = this.getFactorY();
		
		switch (this.getAxisXType()){
			case Linear:
				plot_point.setX(plot_point.getX() * factor_x);
				break;
			case Logarithmic:
				plot_point.setX(MathFunctions.logn(plot_point.getX(), factor_x));
				break;
			case Exponential:
				plot_point.setX(Math.pow(plot_point.getX(), factor_x));
				break;
			}
		
		switch (this.getAxisYType()){
			case Linear:
				plot_point.setY(plot_point.getY() * factor_y);
				break;
			case Logarithmic:
				plot_point.setY(MathFunctions.logn(plot_point.getY(), factor_y));
				break;
			case Exponential:
				plot_point.setY(Math.pow(plot_point.getY(), factor_y));
				break;
			}
		
		
		return null;
	}
	
	/*******************************************
	 * Maps a data point to a graphic point.
	 * 
	 * @param data_point
	 * @return
	 */
	public Point mapDataToGraphic(Point2d data_point){
		
		return null;
	}
	
	/*******************************************
	 * Maps a plot point to a graphic point
	 * 
	 * @param plot_point
	 * @return
	 */
	public Point mapPlotToGraphic(Point2d plot_point){
		
		return null;
	}
	
	/*******************************************
	 * Maps a graphic point to a plot point
	 * 
	 * @param plot_point
	 * @return
	 */
	public Point2d mapGraphicToPlot(Point graphic_point){
		
		return null;
	}
	
	/*******************************************
	 * Maps a graphic point to a data point
	 * 
	 * @param plot_point
	 * @return
	 */
	public Point2d mapGraphicToData(Point graphic_point){
		
		return null;
	}
	
	/*******************************************
	 * Maps an mgui shape from data points to plot points
	 * 
	 * @param data_shape
	 * @return
	 */
	public Shape2D mapDataShapeToPlot(Shape2D data_shape){
		
		return null;
	}
	
	/*******************************************
	 * Maps an mgui shape from data points to graphic points
	 * 
	 * @param data_shape
	 * @return
	 */
	public Shape mapDataShapeToGraphic(Shape2D data_shape){
		
		return null;
	}
	
	/*******************************************
	 * Maps an mgui shape from data points to graphic points
	 * 
	 * @param data_shape
	 * @return
	 */
	public Shape mapPlotShapeToGraphic(Shape2D plot_shape){
		
		return null;
	}


	@Override
	public InterfaceTreeNode issueTreeNode() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setTreeNode(InterfaceTreeNode node) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getTreeLabel() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isDestroyed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}