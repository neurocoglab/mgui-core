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

package mgui.interfaces.shapes;

import java.util.ArrayList;
import java.util.Set;

import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.NamedObject;
import mgui.interfaces.shapes.util.ShapeListener;

/************************************************************
 * Interface for all shape sets.
 * 
 * <p>TODO: make this an abstract class
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface ShapeSet extends ShapeListener, NamedObject{

	/************************************
	 * Adds a shape to this set
	 * 
	 * @param shape
	 * @return
	 */
	public boolean addShape(InterfaceShape shape);
	
	/************************************
	 * Removes a shape from this set
	 * 
	 * @param shape
	 */
	public void removeShape(InterfaceShape shape);
	
	/*************************************
	 * Moves the order of {@code shape} to the spot before {@code target}.
	 * 
	 * @param shape
	 * @param target
	 * @return
	 */
	public boolean moveShapeBefore(InterfaceShape shape, InterfaceShape target);
	
	/*************************************
	 * Returns the index of {@code shape} in this set.
	 * 
	 * @param shape
	 * @return
	 */
	public int getIndexOf(InterfaceShape shape);
	public Set<InterfaceShape> getShapeSet();
	public Set<ShapeSet> getSubSets();
	public boolean hasShape(InterfaceShape s);
	public boolean hasShape(InterfaceShape s, boolean recurse);
	
	/***************************************
	 * Returns the number of shapes in this set
	 * 
	 * @return
	 */
	public int getSize();
	public InterfaceShape getLastAdded();
	public InterfaceShape getLastRemoved();
	public InterfaceShape getLastInserted();
	public int getLastInsert();
	public InterfaceShape getLastModified();
	public SpatialUnit getUnit();
	public void setUnit(SpatialUnit unit);
	public ShapeModel3D getModel();
	
	/****************************************
	 * Determines whether {@code set} is an ancestor set of this set; i.e., whether
	 * this set is a subset of {@code set}.
	 * 
	 * @param set
	 * @return
	 */
	public boolean isAncestorSet(ShapeSet set);
	public boolean isVisible();
	public boolean isSelectable();
	public boolean show2D();
	public boolean show3D();
	public ArrayList<InterfaceShape> getMembers();
	public String getFullName();
	
}