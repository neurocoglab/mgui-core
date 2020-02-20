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

package mgui.interfaces.io;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JFrame;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.NamedObject;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.TreeObject;
import mgui.io.InterfaceIO;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.util.WildcardFileFilter;
import mgui.resources.icons.IconObject;


/*******************************************************
 * Specifies an Input/Output interface object; e.g., file loaders or writers.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceIOType implements IconObject, NamedObject, TreeObject {

	public static int TYPE_INPUT = 0;
	public static int TYPE_OUTPUT = 1;
	public static int TYPE_RANDOM = 2;
	
	protected String name;
	protected String label;
	protected WildcardFileFilter filter;
	protected Class<InterfaceIO> io;
	protected Class<InterfaceIODialogBox> dialog;
	protected Class<InterfaceIOOptions> options;
	protected int type;
	
	public void setType(String t){
		t = t.toLowerCase();
		if (t.equals("input")) type = TYPE_INPUT;
		if (t.equals("output")) type = TYPE_OUTPUT;
		if (t.equals("random")) type = TYPE_RANDOM;
	}
	
	public int getType(){
		return type;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void setName(String name){
		this.name = name;
	}
	
	public Class<InterfaceIO> getIO(){
		return io;
	}
	
	public void setIO(Class<InterfaceIO> io){
		this.io = io;
	}
	
	public void setDialog(Class<InterfaceIODialogBox> dialog){
		this.dialog = dialog;
	}
	
	public Class<InterfaceIODialogBox> getDialog(){
		return dialog;
	}
	
	public WildcardFileFilter getFilter(){
		return filter;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	public void setFilter(WildcardFileFilter filter){
		this.filter = filter;
	}
	
	
	/********************************************
	 * Returns an instance of the dialog box associated with this I/O type. If
	 * there is no associated dialog box, returns {@code null}.
	 * 
	 * @param frame
	 * @param panel
	 * @param options
	 * @return
	 */
	public InterfaceIODialogBox getDialogInstance(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		if (dialog == null) return null;
		try{
			Constructor<InterfaceIODialogBox> constr = dialog.getConstructor(new Class[]{JFrame.class, InterfaceIOPanel.class, InterfaceIOOptions.class});
			InterfaceIODialogBox box = constr.newInstance(new Object[]{frame, panel, options}); // (InterfaceIODialogBox)dialog.newInstance();
			return box;
		}catch (Exception e){
			//e.printStackTrace();
			InterfaceSession.handleException(e);
			InterfaceSession.log("InterfaceIOType: Could not instantiate dialog for class '" + dialog.getCanonicalName() +"'",
								 LoggingType.Errors);
			}
		return null;
	}
	
	/*******************************************
	 * Returns an instance of {@link InterfaceIOOptions} appropriate for this IO type.
	 * 
	 * @return
	 */
	public InterfaceIOOptions getOptionsInstance(){
		if (options == null) return null;
		try{
			InterfaceIOOptions opt = (InterfaceIOOptions)options.newInstance();
			return opt;
		}catch (Exception e){
			e.printStackTrace();
			}
		return null;
	}
	
	public void setOptions(Class<InterfaceIOOptions> options ){
		this.options = options;
	}
	
	/********************************************
	 * Returns a new instance of this type.
	 * 
	 * @return
	 */
	public InterfaceIO getIOInstance(){
		try{
			InterfaceIO intIO = io.newInstance();
			return intIO;
		}catch (Exception e){
			InterfaceSession.handleException(e);
			//e.printStackTrace();
			}
		return null;
	}
	
	/********************************************
	 * Is this input?
	 * 
	 * @return
	 */
	public boolean isInput(){
		return type == TYPE_INPUT;
	}
	
	/********************************************
	 * Is this output?
	 * 
	 * @return
	 */
	public boolean isOutput(){
		return type == TYPE_OUTPUT;
	}
	
	/********************************************
	 * Does this I/O type handle objects of type {@code object}?
	 * 
	 * @param object
	 * @return
	 */
	public boolean isCompatibleObject(InterfaceObject object){
		ArrayList<Class<?>> objects = getIOInstance().getSupportedObjects();
		for (int i = 0; i < objects.size(); i++)
			if(objects.get(i).isInstance(object)) return true;
		return false;
	}
	
	/********************************************
	 * Determines whether this type supports all of the objects supported by {@code type}
	 * 
	 * @param type
	 * @return
	 */
	public boolean isCompatibleType(InterfaceIOType type){
		
		ArrayList<Class<?>> this_supports = getSupportedObjects();
		ArrayList<Class<?>> type_supports = getSupportedObjects();
		
		for (int i = 0; i < type_supports.size(); i++){
			Class<?> class_i = type_supports.get(i);
			for (int j = 0; j < this_supports.size(); j++){
				if (!class_i.isAssignableFrom(this_supports.get(j)))
					return false;
				}
			}
		return true;
	}
	
	/**********************************************
	 * Determines whether {@code options} is compatible with this type
	 * 
	 * @param options
	 * @return
	 */
	public boolean isCompatibleOptions(InterfaceIOOptions options){
		
		InterfaceIOOptions oi = this.getOptionsInstance();
		if (oi == null) return false;
		
		return oi.getClass().isAssignableFrom(options.getClass());
		
	}
	
	
	/********************************************
	 * Returns a list of classes for objects which are supported by this I/O interface
	 * 
	 * @see InterfaceIO
	 * @return
	 */
	public ArrayList<Class<?>> getSupportedObjects(){
		return getIOInstance().getSupportedObjects();
	}
	
	@Override
	public String toString(){
		return label;
	}
	
	@Override
	public Icon getObjectIcon() {
		return getIOInstance().getObjectIcon();
	}
	
	
	// ********************** TREE OBJECT STUFF *************************************
	
	@Override
	public InterfaceTreeNode issueTreeNode() {
		return null;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode node) {
		
	}

	@Override
	public String getTreeLabel() {
		return label;
	}

	
}