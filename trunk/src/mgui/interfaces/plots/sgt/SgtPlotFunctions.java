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

import gov.noaa.pmel.sgt.CartesianGraph;
import gov.noaa.pmel.sgt.GridAttribute;
import gov.noaa.pmel.sgt.dm.SGTGrid;
import gov.noaa.pmel.sgt.dm.SGTLine;
import gov.noaa.pmel.sgt.dm.SGTVector;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.Range2D;
import gov.noaa.pmel.util.SoTRange;
import mgui.interfaces.Utility;

/***************************************************
 * Utility class for Scientific Graphics Toolkit (SGT) plots.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SgtPlotFunctions extends Utility {

	/************************************************
	 * Available axis types; one of:
	 * 
	 * <ul>
	 * <li>X
	 * <li>Y
	 * <li>Z
	 * <li>T
	 * </ul>
	 * 
	 * @author Andrew Reid
	 *
	 */
	public static enum AxisType{
		X,
		Y,
		Z,
		T;
	}
	
	/*************************************************
	 * Find the range of the <code>SGTGrid</code> object in the
	 * specified direction.
	 *
	 * @param data the data grid
	 * @param attr the grid attribute
	 * @param dir the direction
	 * 
	 *************************/
	public static Range2D findRange(SGTGrid data, GridAttribute attr, AxisType dir) {
	    int num, onum, i, first=0;
	    double amin=0.0, amax=0.0;
	    double[] values, orig;
	    boolean good = false;

	    switch(dir) {
		    case X:
		      if(attr.isRaster()) {
		        if(data.hasXEdges()) {
		          values = data.getXEdges();
		          num = values.length;
		        } else {
		          orig = data.getXArray();
		          onum = orig.length;
		          values = new double[onum+1];
		          values[0] = orig[0]-(orig[1]-orig[0])*0.5;
		          for(i=1; i < onum; i++) {
		            values[i] = (orig[i-1]+orig[i])*0.5;
		          }
		          values[onum] = orig[onum-1]+(orig[onum-1]-orig[onum-2])*0.5;
		          num = values.length;
		        }
		      } else {
		        values = data.getXArray();
		        num = values.length;
		      }
		      break;
		    case Y:
		      if(attr.isRaster()) {
		        if(data.hasYEdges()) {
		          values = data.getYEdges();
		          num = values.length;
		        } else {
		          orig = data.getYArray();
		          onum = orig.length;
		          values = new double[onum+1];
		          values[0] = orig[0]-(orig[1]-orig[0])*0.5;
		          for(i=1; i < onum; i++) {
		            values[i] = (orig[i-1]+orig[i])*0.5;
		          }
		          values[onum] = orig[onum-1]+(orig[onum-1]-orig[onum-2])*0.5;
		          num = values.length;
		        }
		      } else {
		        values = data.getYArray();
		        num = values.length;
		      }
		      break;
		    default:
		    case Z:
			      values = data.getZArray();
			      num = values.length;
		    }
	    
	    for(i=0; i < num; i++) {
	    	if(!Double.isNaN(values[i])) {
		        amin = (double)values[i];
		        amax = (double)values[i];
		        good = true;
		        first = i+1;
		        break;
	      		}
	    	}
	    if(!good) {
	    	return new Range2D(Double.NaN, Double.NaN);
	    } else {
	      for(i=first; i < num; i++) {
	    	  if(!Double.isNaN(values[i])) {
		          amin = Math.min(amin, (double)values[i]);
		          amax = Math.max(amax, (double)values[i]);
	    	      }
	      	  	}
	    	}
	    return new Range2D(amin, amax);
	}
	
	
	/**
	   * Find the range of the <code>SGTLine</code> object in the specific
	   * direction.
	   *
	   * @param data SGTLine object
	   * @param dir direction
	   * @see CartesianGraph
	   */
	  public Range2D findRange(SGTLine data, AxisType dir) {
	    int num, i, first=0;
	    double amin=0.0, amax=0.0;
	    double[] values;
	    boolean good = false;

	    switch(dir) {
	    case X:
	      values = data.getXArray();
	      num = values.length;
	      break;
	    default:
	    case Y:
	      values = data.getYArray();
	      num = values.length;
	    }
	    for(i=0; i < num; i++) {
	      if(!Double.isNaN(values[i])) {
	        amin = (double)values[i];
	        amax = (double)values[i];
	        good = true;
	        first = i+1;
	        break;
	      }
	    }
	    if(!good) {
	      return new Range2D(Double.NaN, Double.NaN);
	    } else {
	      for(i=first; i < num; i++) {
	        if(!Double.isNaN(values[i])) {
	          amin = Math.min(amin, (double)values[i]);
	          amax = Math.max(amax, (double)values[i]);
	        }
	      }
	    }
	    return new Range2D(amin, amax);
	  }
	  /**
	   * Find the range of the <code>SGTLine</code> object in the specific
	   * direction.
	   *
	   * @param xy_data SGTLine object
	   * @param dir direction
	   * @return range as an <code>SoTRange</code> object
	   * @see CartesianGraph
	   */
	  public SoTRange findSoTRange(SGTLine line, AxisType dir) {
	    switch(dir) {
	    case X:
	      return line.getXRange();
	    case Y:
	      return line.getYRange();
	    default:
	      return null;
	    }
	  }
	  /**
	   * Find the range of the <code>SGTVector</code> object in the
	   * specified direction.  Uses the U component to find X, Y ranges.
	   *
	   * @param data the data vector
	   * @param dir the direction
	   * @return range as an <code>SoTRange</code> object
	   */
	  public SoTRange findSoTRange(SGTVector data, AxisType dir) {
	    double[] veclen;
	    int num, i, first = 0;
	    double amin = 0.0, amax = 0.0;
	    boolean good = false;

	    switch(dir) {
	    case X:
	      return data.getU().getXRange();
	    case Y:
	      return data.getU().getYRange();
	    default:
	    case Z:
	      double[] ucomp = data.getU().getZArray();
	      double[] vcomp = data.getV().getZArray();
	      veclen = new double[ucomp.length];
	      for(i=0; i < veclen.length; i++) {
	        veclen[i] = Math.sqrt(ucomp[i]*ucomp[i] + vcomp[i]*vcomp[i]);
	      }
	      num = veclen.length;
	    }
	    for(i=0; i < num; i++) {
	      if(!Double.isNaN(veclen[i])) {
	        amin = (double)veclen[i];
	        amax = (double)veclen[i];
	        good = true;
	        first = i+1;
	        break;
	      }
	    }
	    if(!good) {
	      return new SoTRange.Double(Double.NaN, Double.NaN);
	    } else {
	      for(i=first; i < num; i++) {
	        if(!Double.isNaN(veclen[i])) {
	          amin = Math.min(amin, (double)veclen[i]);
	          amax = Math.max(amax, (double)veclen[i]);
	        }
	      }
	    }
	    return new SoTRange.Double(amin, amax);
	  }
	  /**
	   * Find the range of the <code>SGTGrid</code> object in the
	   * specified direction.
	   *
	   * @param data the data grid
	   * @param attr the grid attribute
	   * @param dir the direction
	   * @return range as an <code>SoTRange</code> object
	   */
	  public SoTRange findSoTRange(SGTGrid data, GridAttribute attr, AxisType dir) {
	    int num, onum, i, first=0;
	    double amin=0.0, amax=0.0;
	    GeoDate tmin, tmax;
	    double[] values, orig;
	    GeoDate[] taxis, torig;
	    boolean good = false;

	    if(attr.isRaster() &&
	       ((data.isXTime() && (dir == AxisType.X) && !data.hasXEdges()) ||
	            (data.isYTime() && (dir == AxisType.Y) && !data.hasYEdges()))) {
	        torig = data.getTimeArray();
	        onum = torig.length;
	        taxis = new GeoDate[onum+1];
	        taxis[0] = torig[0].subtract(
	                   (torig[1].subtract(torig[0])).divide(2.0));
	        for(i=1; i < onum; i++) {
	          taxis[i] = (torig[i-1].add(torig[i])).divide(2.0);
	        }
	        taxis[onum] = torig[onum-1].add(
	                      (torig[onum-1].subtract(torig[onum-2])).divide(2.0));
	        num = taxis.length;
	        return new SoTRange.Time(taxis[0].getTime(),
	                                 taxis[num-1].getTime());
	    }

	    switch(dir) {
	    case X:
	      if(attr.isRaster()) {
	        if(data.hasXEdges()) {
	          return data.getXEdgesRange();
	        } else {
	          orig = data.getXArray();
	          onum = orig.length;
	          values = new double[onum+1];
	          values[0] = orig[0]-(orig[1]-orig[0])*0.5;
	          for(i=1; i < onum; i++) {
	            values[i] = (orig[i-1]+orig[i])*0.5;
	          }
	          values[onum] = orig[onum-1]+(orig[onum-1]-orig[onum-2])*0.5;
	          num = values.length;
	        }
	      } else {
	        return data.getXRange();
	      }
	      break;
	    case Y:
	      if(attr.isRaster()) {
	        if(data.hasYEdges()) {
	          return data.getYEdgesRange();
	        } else {
	          orig = data.getYArray();
	          onum = orig.length;
	          values = new double[onum+1];
	          values[0] = orig[0]-(orig[1]-orig[0])*0.5;
	          for(i=1; i < onum; i++) {
	            values[i] = (orig[i-1]+orig[i])*0.5;
	          }
	          values[onum] = orig[onum-1]+(orig[onum-1]-orig[onum-2])*0.5;
	          num = values.length;
	        }
	      } else {
	        return data.getYRange();
	      }
	      break;
	    default:
	    case Z:
	      values = data.getZArray();
	      num = values.length;
	    }
	    for(i=0; i < num; i++) {
	      if(!Double.isNaN(values[i])) {
	        amin = (double)values[i];
	        amax = (double)values[i];
	        good = true;
	        first = i+1;
	        break;
	      }
	    }
	    if(!good) {
	      return new SoTRange.Double(Double.NaN, Double.NaN);
	    } else {
	      for(i=first; i < num; i++) {
	        if(!Double.isNaN(values[i])) {
	          amin = Math.min(amin, (double)values[i]);
	          amax = Math.max(amax, (double)values[i]);
	        }
	      }
	    }
	    return new SoTRange.Double(amin, amax);
	  }
	
	
	
}