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

/**********************************************************
 * Specifies a listener which handles dynamic changes to an executing pipeline.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface DynamicPipelineListener {

	/***********************************************
	 * Called when a pipeline is initially launched
	 * 
	 * @param event
	 */
	public void pipelineLaunched(DynamicPipelineEvent event);
	
	/***********************************************
	 * Called when a pipeline is terminated. The state of the pipeline can be queried via
	 * the {@link StaticPipelineEvent} object.
	 * 
	 * @param event
	 */
	public void pipelineTerminated(DynamicPipelineEvent event);
	
	/***********************************************
	 * Called when a pipeline task is terminated. The state of the pipeline can be queried via
	 * the {@link StaticPipelineEvent} object.
	 * 
	 * @param event
	 * @param task
	 */
	public void pipelineTaskTerminated(DynamicPipelineEvent event, PipelineTask task);
	
	/***********************************************
	 * Called when a pipeline task requests to publish an update.
	 * 
	 * @param event
	 * @param task
	 */
	public void pipelineTaskUpdated(DynamicPipelineEvent event, PipelineTask task);
	
	/***********************************************
	 * Called when a pipeline task is launched.
	 * 
	 * @param event
	 * @param task
	 */
	public void pipelineTaskLaunched(DynamicPipelineEvent event, PipelineTask task);
	
	
}