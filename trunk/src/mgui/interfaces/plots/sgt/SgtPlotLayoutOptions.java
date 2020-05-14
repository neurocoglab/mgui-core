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

package mgui.interfaces.plots.sgt;

import java.awt.Color;

import mgui.interfaces.InterfaceOptions;

/*****************************************************
 * Options defining an SGT plot layout
 * 
 * @author Andrew Reid
 *
 */
public class SgtPlotLayoutOptions extends InterfaceOptions {

	 /** Width of graph in physical units */
	  public double size_x = 6.00;
	  /** Start of X axis in physical units */
	  public double min_x  = 0.60;
	  /** End of X axis in physical units */
	  public double max_x  = 5.40;
	  /** Height of graph in physical units */
	  public double size_y = 4.50;
	  /** Start of Y axis in physical units */
	  public double min_y  = 0.75;
	  /** End of Y axis in physical units */
	  public double max_y  = 3.30;
	  //
	  /** Height of main title in physical units */
	  public double title_height = 0.25;
	  /** Height of axis title in physical units */
	  public double axis_title_height = 0.22;
	  /** Height of axis labels in physical units */
	  public double axis_label_height = 0.18;
	  /** Height of 2nd and 3rd main titles */
	  public double subtitle_height = 0.15;
	  /** Height of line or color key labels */
	  public double legend_height = 0.16;
	  //  public double KEY_HEIGHT_ = 0.20;
	  //
	  /** Width of key if in separate pane */
	  public double legend_size_x =  6.00;
	  /** Height of key if in separate pane */
	  public double legend_size_y = 12.00;
	  //
	  /** Main pane color */
	  public Color plot_background = Color.white;
	  /** Key pane color */
	  public Color legend_background = Color.white;
	
	
	
}