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

package mgui.interfaces.models;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.models.dynamic.DynamicModel;
import mgui.models.dynamic.DynamicModelEngine;
import mgui.models.dynamic.DynamicModelException;
import mgui.models.environments.SimpleEnvironment;


public class InterfaceDynamicModelPanel extends InterfacePanel 
										implements ActionListener,
												   InterfaceDialogUpdater{

	
	public DynamicModel currentModel, selectedModel;
	public DynamicModelEngine engine;
	
	CategoryTitle lblModels = new CategoryTitle("MODELS");
	JLabel lblModelType = new JLabel("Type:");
	JComboBox cmbModelType = new JComboBox();
	JLabel lblModel = new JLabel("Model:");
	JComboBox cmbModel = new JComboBox();
	JButton cmdSetModel = new JButton("Set");
	JButton cmdAddModel = new JButton("Add");
	JButton cmdCopyModel = new JButton("Copy");
	JButton cmdDelModel = new JButton("Del");
	
	CategoryTitle lblExecution = new CategoryTitle("EXECUTION");
	JLabel lblExecTask = new JLabel("Task:");
	JComboBox cmbExecTask = new JComboBox();
	JButton cmdExecSetEngine = new JButton("Set Engine");
	JButton cmdExecSetModel = new JButton("Set Model");
	JLabel lblExecControlParams = new JLabel("<html><u>Control Parameters</u> </html>");
	JLabel lblExecIterStep = new JLabel("Iters/step:");
	JTextField txtExecIterStep = new JTextField("1");
	JLabel lblExecStepTo = new JLabel("Step to:");
	JTextField txtExecStepTo = new JTextField("0");
	JLabel lblExecTimeStep = new JLabel("Timestep (ms):");
	JTextField txtExecTimeStep = new JTextField("1.00");
	JLabel lblExecPlayRate = new JLabel("Play rate:");
	JTextField txtExecPlayRate = new JTextField("1");
	JLabel lblExecConverge = new JLabel("Converge:");
	JTextField txtExecConverge = new JTextField("0.05");
	JLabel lblExecExecute = new JLabel("<html><u>Execute</u> </html>");
	JButton cmdExecReset = new JButton("|<");
	JButton cmdExecStop = new JButton("||");
	JButton cmdExecPlay = new JButton(">");
	JButton cmdExecStep = new JButton("|>");
	JButton cmdExecPlayAll = new JButton(">|");
	JLabel lblExecSysInfo = new JLabel("<html><u>System Info</u> </html>");
	JLabel lblExecEnergy = new JLabel("Energy:");
	JTextField txtExecEnergy = new JTextField();
	JLabel lblExecDeltaE = new JLabel("DeltaE:");
	JTextField txtExecDeltaE = new JTextField();
	JLabel lblExecIteration = new JLabel("Iteration:");
	JTextField txtExecIteration = new JTextField();
	JLabel lblExecTime = new JLabel("Time:");
	JTextField txtExecTime = new JTextField();
	JLabel lblExecSample = new JLabel("Sample:");
	JTextField txtExecSample = new JTextField();
	
	
	public InterfaceDynamicModelPanel(){
		init();
	}
	
	/*
	public InterfaceDynamicModelPanel(InterfaceDisplayPanel p){
		displayPanel = p;
		init();
	}
	*/
	
	
	public boolean setParameters(InterfaceOptions p, int code) {
		
		return false;
	}

	protected void init(){
		
		cmdExecSetEngine.setActionCommand("Execute Set Engine");
		cmdExecSetEngine.addActionListener(this);
		cmdExecSetEngine.setToolTipText("Set engine with current model");
		cmdExecSetModel.setActionCommand("Execute Set Model");
		cmdExecSetModel.addActionListener(this);
		cmdExecSetModel.setToolTipText("Set current model from engine");
		cmdExecStep.setActionCommand("Execute Model Step");
		cmdExecStep.addActionListener(this);
		
		cmdExecReset.setActionCommand("Execute Reset");
		cmdExecReset.addActionListener(this);
		
		
		//init layout
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		//add controls
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		
		add(lblModels, c);
		lblModels.setParentObj(this);
		c = new CategoryLayoutConstraints("MODELS", 1, 1, 0.05, 0.25, 1);
		add(lblModelType, c);
		c = new CategoryLayoutConstraints("MODELS", 1, 1, 0.32, 0.63, 1);
		add(cmbModelType, c);
		c = new CategoryLayoutConstraints("MODELS", 2, 2, 0.05, 0.25, 1);
		add(lblModel, c);
		c = new CategoryLayoutConstraints("MODELS", 2, 2, 0.32, 0.63, 1);
		add(cmbModel, c);
		c = new CategoryLayoutConstraints("MODELS", 3, 3, 0.05, 0.44, 1);
		add(cmdSetModel, c);
		c = new CategoryLayoutConstraints("MODELS", 3, 3, 0.51, 0.44, 1);
		add(cmdAddModel, c);
		c = new CategoryLayoutConstraints("MODELS", 4, 4, 0.05, 0.44, 1);
		add(cmdCopyModel, c);
		c = new CategoryLayoutConstraints("MODELS", 4, 4, 0.51, 0.44, 1);
		add(cmdDelModel, c);
		
		c = new CategoryLayoutConstraints();
		add(lblExecution, c);
		lblExecution.setParentObj(this);
		
		c = new CategoryLayoutConstraints("EXECUTION", 1, 1, 0.05, 0.25, 1);
		add(lblExecTask, c);
		c = new CategoryLayoutConstraints("EXECUTION", 1, 1, 0.32, 0.63, 1);
		add(cmbExecTask, c);
		c = new CategoryLayoutConstraints("EXECUTION", 2, 2, 0.05, 0.44, 1);
		add(cmdExecSetEngine, c);
		c = new CategoryLayoutConstraints("EXECUTION", 2, 2, 0.51, 0.44, 1);
		add(cmdExecSetModel, c);
		c = new CategoryLayoutConstraints("EXECUTION", 3, 3, 0.05, 0.9, 1);
		add(lblExecControlParams, c);
		c = new CategoryLayoutConstraints("EXECUTION", 4, 4, 0.07, 0.43, 1);
		add(lblExecIterStep, c);
		c = new CategoryLayoutConstraints("EXECUTION", 4, 4, 0.52, 0.43, 1);
		add(txtExecIterStep, c);
		c = new CategoryLayoutConstraints("EXECUTION", 5, 5, 0.07, 0.43, 1);
		add(lblExecStepTo, c);
		c = new CategoryLayoutConstraints("EXECUTION", 5, 5, 0.52, 0.43, 1);
		add(txtExecStepTo, c);
		c = new CategoryLayoutConstraints("EXECUTION", 6, 6, 0.07, 0.43, 1);
		add(lblExecTimeStep, c);
		c = new CategoryLayoutConstraints("EXECUTION", 6, 6, 0.52, 0.43, 1);
		add(txtExecTimeStep, c);
		c = new CategoryLayoutConstraints("EXECUTION", 7, 7, 0.07, 0.43, 1);
		add(lblExecPlayRate, c);
		c = new CategoryLayoutConstraints("EXECUTION", 7, 7, 0.52, 0.43, 1);
		add(txtExecPlayRate, c);
		c = new CategoryLayoutConstraints("EXECUTION", 8, 8, 0.07, 0.43, 1);
		add(lblExecConverge, c);
		c = new CategoryLayoutConstraints("EXECUTION", 8, 8, 0.52, 0.43, 1);
		add(txtExecConverge, c);
		c = new CategoryLayoutConstraints("EXECUTION", 9, 9, 0.05, 0.5, 1);
		add(lblExecExecute, c);
		c = new CategoryLayoutConstraints("EXECUTION", 10, 10, 0.05, 0.18, 1);
		cmdExecReset.setToolTipText("Reset");
		add(cmdExecReset, c);
		c = new CategoryLayoutConstraints("EXECUTION", 10, 10, 0.23, 0.18, 1);
		cmdExecStop.setToolTipText("Stop");
		add(cmdExecStop, c);
		c = new CategoryLayoutConstraints("EXECUTION", 10, 10, 0.41, 0.18, 1);
		cmdExecPlay.setToolTipText("Play");
		add(cmdExecPlay, c);
		c = new CategoryLayoutConstraints("EXECUTION", 10, 10, 0.59, 0.18, 1);
		cmdExecStep.setToolTipText("Step");
		add(cmdExecStep, c);
		c = new CategoryLayoutConstraints("EXECUTION", 10, 10, 0.77, 0.18, 1);
		cmdExecPlayAll.setToolTipText("Play All");
		add(cmdExecPlayAll, c);
		c = new CategoryLayoutConstraints("EXECUTION", 11, 11, 0.05, 0.5, 1);
		add(lblExecSysInfo, c);
		c = new CategoryLayoutConstraints("EXECUTION", 12, 12, 0.07, 0.43, 1);
		add(lblExecEnergy, c);
		c = new CategoryLayoutConstraints("EXECUTION", 12, 12, 0.52, 0.43, 1);
		add(txtExecEnergy, c);
		c = new CategoryLayoutConstraints("EXECUTION", 13, 13, 0.07, 0.43, 1);
		add(lblExecDeltaE, c);
		c = new CategoryLayoutConstraints("EXECUTION", 13, 13, 0.52, 0.43, 1);
		add(txtExecDeltaE, c);
		c = new CategoryLayoutConstraints("EXECUTION", 14, 14, 0.07, 0.43, 1);
		add(lblExecTime, c);
		c = new CategoryLayoutConstraints("EXECUTION", 14, 14, 0.52, 0.43, 1);
		add(txtExecTime, c);
		c = new CategoryLayoutConstraints("EXECUTION", 15, 15, 0.07, 0.43, 1);
		add(lblExecIteration, c);
		c = new CategoryLayoutConstraints("EXECUTION", 15, 15, 0.52, 0.43, 1);
		add(txtExecIteration, c);
		c = new CategoryLayoutConstraints("EXECUTION", 16, 16, 0.07, 0.43, 1);
		add(lblExecSample, c);
		c = new CategoryLayoutConstraints("EXECUTION", 16, 16, 0.52, 0.43, 1);
		add(txtExecSample, c);
		
		updateModelCombo();
		resetEngineValues();
		updateExecControls();
		
	}
	
	protected void updateModelCombo(){
		cmbModel.removeAllItems();
		ArrayList<InterfaceAbstractModel> models = InterfaceSession.getWorkspace().getDynamicModels();
		
		for (int i = 0; i < models.size(); i++)
			cmbModel.addItem(models.get(i));
		
		if (currentModel != null)
			cmbModel.setSelectedItem(currentModel);
		else
			currentModel = (InterfaceAbstractModel)cmbModel.getItemAt(0);
		
		cmbModel.updateUI();
	}
	
	public void actionPerformed(ActionEvent e) {

		//***EXECUTION***
		//model execution
		if (e.getActionCommand().startsWith("Execute Model")){
			if (engine == null) return;
			
			if (e.getActionCommand().endsWith("Step")){
				//step once
				try{
					engine.setTimeStep(Double.valueOf(txtExecTimeStep.getText()).doubleValue());
					engine.executeModel(Integer.valueOf(txtExecIterStep.getText()).intValue());
				}catch (DynamicModelException ex){
					ex.printStackTrace();
					return;
					}
				
				updateUI();
				}
			
			updateEngineValues();
			
			return;
			}
		
		
		//execute
		if (e.getActionCommand().startsWith("Execute")){
			
			//set engine
			if (e.getActionCommand().endsWith("Set Engine")){
								
				if (currentModel == null) return;
				
				double t = Double.valueOf(txtExecTimeStep.getText()).doubleValue();
				
				//((SimpleEnvironment)copy.getEnvironment()).setValues(getInputSample(0));
				engine = new DynamicModelEngine(t, currentModel);
				
				//int sample = Integer.valueOf(txtExecSample.getText()).intValue();
				SimpleEnvironment env = (SimpleEnvironment)(engine.getModel()).getEnvironment();
				env.reset();
				
				//TODO put in separate routine that gets called whenever engine's input
				//	   sample changes
				cmdExecSetEngine.setText("Reset Engine");
				cmdExecSetEngine.setActionCommand("Execute Reset Engine");
				resetEngineValues();
				updateExecControls();
				updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Execute Reset Engine")){
				resetEngineValues();
				updateExecControls();
				updateUI();
				if (engine != null)
					engine.reset();
				return;
				}
			
			//set model from engine
			if (e.getActionCommand().endsWith("Set Model")){
				if (engine == null) return;
				currentModel = engine.getModel();
				}
		
			}
	}
	
	void updateExecControls(){
		boolean enabled = (currentModel != null);
		cmdExecSetEngine.setEnabled(enabled);
		
		enabled = (engine != null);
		cmdExecSetModel.setEnabled(enabled);
		cmdExecReset.setEnabled(enabled);
		cmdExecStop.setEnabled(enabled);
		cmdExecPlay.setEnabled(enabled);
		cmdExecStep.setEnabled(enabled);
		cmdExecPlayAll.setEnabled(enabled);
		lblExecSysInfo.setEnabled(enabled);
		lblExecEnergy.setEnabled(enabled);
		txtExecEnergy.setEnabled(enabled);
		lblExecDeltaE.setEnabled(enabled);
		txtExecDeltaE.setEnabled(enabled);
		lblExecIteration.setEnabled(enabled);
		txtExecIteration.setEnabled(enabled);
		lblExecTime.setEnabled(enabled);
		txtExecTime.setEnabled(enabled);
		lblExecSample.setEnabled(enabled);
		txtExecSample.setEnabled(enabled);
	}
	
	void resetEngineValues(){
		//if (engine == null){
			txtExecIteration.setText("0");
			txtExecEnergy.setText("0.000000");
			txtExecDeltaE.setText("0.000000");
			txtExecTime.setText("0.000000");
			txtExecSample.setText("0");
			
			//return;
			//}
	}
	
	void updateEngineValues(){
		if (engine == null){
			resetEngineValues();
			return;
			}
		//update from stats
		DecimalFormat df = new DecimalFormat("#0.000000");
		txtExecIteration.setText("" + engine.getIterations());
		double last = Double.valueOf(txtExecEnergy.getText()).doubleValue();
		txtExecTime.setText(df.format(engine.getClock()));
		
	}
	
	
	public String toString(){
		return "Dynamic Model Panel";
	}
	
}