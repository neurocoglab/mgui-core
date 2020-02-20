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

package mgui.interfaces.graphics.video;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;


public class VideoTaskType {

	public static int TYPE_2D_3D = 0;
	public static int TYPE_2D = 1;
	public static int TYPE_3D = 2;
	
	public Class task_class;
	public String name;
	public Class dialog_class;
	
	public int type = TYPE_2D_3D;
	
	public void setType(String t){
		t = t.toLowerCase();
		if (t.equals("2D")) type = TYPE_2D;
		if (t.equals("3D")) type = TYPE_3D;
		if (t.equals("both")) type = TYPE_2D_3D;
	}
	
	public VideoTask getTaskInstance(){
		try{
			return (VideoTask)task_class.newInstance();
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}
	}
	
	public static VideoTaskType getTypeForTask(VideoTask task){
		HashMap<String, VideoTaskType> types = InterfaceEnvironment.getVideoTaskTypes();
		Iterator<VideoTaskType> itr = types.values().iterator();
		
		while (itr.hasNext()){
			VideoTaskType type = itr.next();
			if (type.task_class.isInstance(task)) return type;
			}
		
		return null;
			
	}
	
	public VideoTaskDialogPanel getDialogPanelInstance(VideoTask task){
		if (dialog_class == null) return null;
		try{
			//Constructor<VideoTaskDialogPanel> constr = dialog_class.getConstructor(new Class[]{InterfaceDisplayPanel.class, task_class});
			Constructor<VideoTaskDialogPanel> constr = dialog_class.getConstructor(new Class[]{task_class});
			VideoTaskDialogPanel v_panel = constr.newInstance(new Object[]{task}); // (InterfaceIODialogBox)dialog.newInstance();
			//box.setIOPanel(panel);
			//box.setOptions(options);
			return v_panel;
		}catch (Exception e){
			InterfaceSession.handleException(e);
			}
		return null;
	}
	
	/*
	public VideoTaskOptions getOptionsInstance(VideoTask task){
		if (options_class == null) return null;
		try{
			VideoTaskOptions opt = (VideoTaskOptions)options_class.newInstance();
			opt.setTask(task);
			return opt;
		}catch (Exception e){
			e.printStackTrace();
			}
		return null;
	}
	*/
	
	@Override
	public String toString(){
		return name;
	}
	
}