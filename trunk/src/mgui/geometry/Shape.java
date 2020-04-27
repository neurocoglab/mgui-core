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

package mgui.geometry;

import org.jogamp.vecmath.Matrix4d;

import mgui.interfaces.xml.XMLObject;
import Jama.Matrix;

/*********************************************
 * This is the base interface for all shapes in mgui. All implementations of <code>Shape</code> must satisfy 
 * the following contract:
 * 
 * <ul>
 * <li>specify fixed (absolute) geometry; i.e., they must be representable in Cartesian coordinates, 
 * accessible through the <code>getCoords</code> method.</li>
 * <li>provide their coordinates or vertices as copies, guaranteeing that any alterations to their values will not
 * affect the shape from which they were obtained; updates to the shape must therefore be done via their update methods
 * (i.e., <code>setCoords</code>).
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public interface Shape extends Cloneable,
							   XMLObject{

	public float[] getCoords();
	public void setCoords(float[] f);
	public int getSize();
	
	/*********************************
	 * Transform this shape with affine transformation matrix <code>M</code>.
	 * 
	 * @param M
	 * @return
	 */
	public boolean transform(Matrix4d M);
	
	/*********************************
	 * Transform this shape with affine transformation Jama matrix <code>M</code>.
	 * 
	 * @param M
	 * @return
	 */
	public boolean transform(Matrix T);
	
}