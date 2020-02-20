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

package mgui.interfaces.plots;

import mgui.numbers.MguiNumber;

/************************************************************************
 * An X-Y data pair.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class XYData<T extends MguiNumber> {

	protected T X, Y;
	
	public XYData(){
		
	}
	
	public XYData(T x, T y){
		X = (T)x.clone();
		Y = (T)y.clone();
	}
	
	/************************
	 * Returns the X value for this pair (cloned)
	 * 
	 * @return
	 */
	public T getX(){
		return (T)X.clone();
	}
	
	/************************
	 * Returns the Y value for this pair (cloned)
	 * 
	 * @return
	 */
	public T getY(){
		return (T)Y.clone();
	}
	
	/************************
	 * Sets the X value for this pair
	 * 
	 * @return
	 */
	public void setX(T x){
		if (X == null)
			X = (T)x.clone();
		else
			X.setValue(x.getValue());
	}
	
	/************************
	 * Sets the X value for this pair
	 * 
	 * @return
	 */
	public void setX(double x){
		X.setValue(x);
	}
	
	/************************
	 * Sets the Y value for this pair
	 * 
	 * @return
	 */
	public void setY(T y){
		if (Y == null)
			Y = (T)y.clone();
		else
			Y.setValue(y.getValue());
		Y.setValue(y.getValue());
	}
	
	/************************
	 * Sets the Y value for this pair
	 * 
	 * @return
	 */
	public void setY(double y){
		Y.setValue(y);
	}
	
	/************************
	 * Sets the X and Y values
	 * 
	 * @param xy
	 */
	public void set(XYData<T> xy){
		X.setValue(xy.getX());
		Y.setValue(xy.getY());
	}
	
}