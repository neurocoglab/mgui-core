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

package mgui.pipelines;

import java.util.EventObject;

import mgui.interfaces.pipelines.trees.TaskTreeNode;

/***********************************************
 * Represents an static event on a pipeline; i.e., a change to its structure, etc.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @see DynamicPipelineEvent
 */
public class StaticPipelineEvent extends EventObject {

	public static enum EventType{
		TaskAppended,
		TaskInserted,
		TaskRemoved,
		TaskModified,
		PipelineModified,
		TaskRenamed;
	}
	
	protected EventType type;
//	protected TaskTreeNode node;
	protected int task_index = -1;
	protected Object previous_value; 
	
	public StaticPipelineEvent(PipelineTask source, EventType type){
		super(source);
		this.type = type;
	}
	
	public StaticPipelineEvent(PipelineTask source, EventType type, Object previous_value){
		super(source);
		this.type = type;
		this.previous_value = previous_value;
	}

	public StaticPipelineEvent(PipelineTask source, int task_index, EventType type){
		super(source);
		//this.node = node;
		this.type = type;
		this.task_index = task_index;
	}
	
	public EventType getType(){
		return type;
	}
	
	public Object getPreviousValue(){
		return previous_value;
	}
	
	public PipelineTask getTask(){
		return (PipelineTask)this.getSource();
	}
	
//	public TaskTreeNode getNode(){
//		return node;
//	}
	
	public int getTaskIndex(){
		return task_index;
	}
	
}