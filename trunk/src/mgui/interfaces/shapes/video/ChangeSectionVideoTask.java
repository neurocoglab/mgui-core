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

package mgui.interfaces.shapes.video;

import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.graphics.video.VideoException;
import mgui.interfaces.graphics.video.VideoTask;
import mgui.interfaces.graphics.video.VideoTask3D;
import mgui.interfaces.shapes.SectionSet3DInt;

/**********************************************************
 * Video task which changes the current section, either once or multiple times over an interval.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ChangeSectionVideoTask extends VideoTask3D {

	int start_section, end_section;
	InterfaceGraphic2D graphic_2d;
	
	public ChangeSectionVideoTask(){
		
	}
	
	/**********************************************************
	 * Instantiates this task with the given arguments. Sections in {@code section_set} will be varied
	 * from {@code section_start} to {@code section_end}.
	 * 
	 * @param section_set
	 * @param start_section
	 * @param end_section
	 */
	public ChangeSectionVideoTask(int start, int stop, InterfaceGraphic2D graphic_2d, int start_section, int end_section){
		setStart(start);
		setStop(stop);
		this.graphic_2d = graphic_2d;
		this.start_section = start_section;
		this.end_section = end_section;
		
		
	}

	@Override
	protected boolean do_it(InterfaceGraphic<?> g, long time) throws VideoException {
		
		double duration = stop_time - start_time;
		
		if (duration == 0){
			// Do once
			started = true;
			graphic_2d.setCurrentSection(start_section);
			return true;
			}
		
		if (!started){
			started = true;
			}
		
		double prop = (time - start_time) / duration;
		int section = start_section + (int)Math.round(prop * (double)(end_section - start_section));
		
		if (graphic_2d.getCurrentSection() != section)
		graphic_2d.setCurrentSection(section);
		
		return true;
	}

	@Override
	public String getName() {
		return "Change Section Task";
	}

	@Override
	public void setFromTask(VideoTask task) {
		
		ChangeSectionVideoTask _task = (ChangeSectionVideoTask)task;
		this.graphic_2d = _task.graphic_2d;
		this.start_section = _task.start_section;
		this.end_section = _task.end_section;
		this.start_time = task.start_time;
		this.stop_time = task.stop_time;
		
		
	}
	
	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone(){
		ChangeSectionVideoTask task = new ChangeSectionVideoTask();
		task.setFromTask(this);
		return task;
	}
	
}