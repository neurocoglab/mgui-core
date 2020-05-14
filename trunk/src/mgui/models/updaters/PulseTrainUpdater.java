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

package mgui.models.updaters;

import java.util.ArrayList;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


public class PulseTrainUpdater extends SimpleEnvironmentUpdater {

	public ArrayList<CurrentPulse> pulses = new ArrayList<CurrentPulse>();
	protected double clock = 0;
	
	public PulseTrainUpdater(){
		init();
	}
	
	public PulseTrainUpdater(boolean repeat, double rate){
		init();
		setRepeat(repeat);
		setRate(rate);
	}
	
	@Override
	protected void init(){
		super.init();
		attributes.add(new Attribute("Repeat", new MguiBoolean(true)));
		attributes.add(new Attribute("Rate[Hz]", new MguiDouble(50)));
	}
	
	public void reset(){
		clock = 0;
	}
	
	public void setRepeat(boolean repeat){
		attributes.setValue("Repeat", new MguiBoolean(repeat));
		
	}
	
	public boolean getRepeat(){
		return ((MguiBoolean)attributes.getValue("Repeat")).getTrue();
	}
	
	public void setRate(double rate){
		attributes.setValue("Rate[Hz]", new MguiDouble(rate));
	}
	
	public double getRate(){
		return ((MguiDouble)attributes.getValue("Rate[Hz]")).getValue();
	}
	
	public CurrentPulse addPulse(int index, double delay, double duration, double amplitude){
		CurrentPulse pulse = new CurrentPulse(index, delay, duration, amplitude);
		pulses.add(pulse);
		return pulse;
	}
	
	public void removePulse(CurrentPulse pulse){
		pulses.remove(pulse);
	}
	
	@Override
	protected boolean doUpdate(DynamicModelEnvironment c, double timeStep) {
		//send pulses that fall in the current window
		//multiple pulses are summated
		//inputs are discrete and signals do not spread across inputs...
		
		//reset if necessary
		updateRepeat();
		
		double[] state = c.getInputState();
		
		//reset to zeros
		for (int i = 0; i < state.length; i++)
			state[i] = 0;
		
		for (int i = 0; i < pulses.size(); i++){
			CurrentPulse pulse = pulses.get(i);
			int index = pulse.getIndex();
			if (index > -1 && index < state.length)
				if (pulse.isActive(clock, timeStep))
					state[index] += pulse.getAmplitude();
			}
		
		clock += timeStep;
		return true;
	}
	
	protected void updateRepeat(){
		if (!getRepeat()) return;
		
		//get period in ms
		double period = 1000.0 / getRate();
		
		if (clock <= period) return;
		reset();
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		for (int i = 0; i < pulses.size(); i++)
			treeNode.add(pulses.get(i).issueTreeNode());
	}
	
	@Override
	public String toString(){
		return "PulseTrainUpdater";
	}
	
	public class CurrentPulse extends AbstractInterfaceObject 
							  implements AttributeObject{
		
		AttributeList attributes = new AttributeList();
		
		public CurrentPulse(int index, double delay, double duration, double amplitude){
			init();
			setIndex(index);
			setDelay(delay);
			setDuration(duration);
			setAmplitude(amplitude);
		}
		
		protected void init(){
			attributes.add(new Attribute("Index", new MguiInteger(-1)));
			attributes.add(new Attribute("Delay[ms]", new MguiDouble(0)));
			attributes.add(new Attribute("Duration[ms]", new MguiDouble(0)));
			attributes.add(new Attribute("Amplitude[mA]", new MguiDouble(0)));
		}
		
		public boolean isActive(double start, double step){
			//is active if 
			//delay + step > clock
			//AND
			//delay + duration <= clock + step
			
			double end = start + step;
			double onset = getDelay();
			double offset = onset + getDuration();
			
			return ((onset >= start && onset < end) ||
					(onset <= start && offset > start));
			
		}
		
		public void setIndex(int index){
			attributes.setValue("Index", new MguiInteger(index));
		}
		
		public int getIndex(){
			return ((MguiInteger)attributes.getValue("Index")).getInt();
		}
		
		public void setDelay(double offset){
			attributes.setValue("Delay[ms]", new MguiDouble(offset));
		}
		
		public void setDuration(double factor){
			attributes.setValue("Duration[ms]", new MguiDouble(factor));
		}
		
		public void setAmplitude(double step){
			attributes.setValue("Amplitude[mA]", new MguiDouble(step));
		}
		
		public double getDelay(){
			return ((MguiDouble)attributes.getValue("Delay[ms]")).getValue();
		}
		
		public double getDuration(){
			return ((MguiDouble)attributes.getValue("Duration[ms]")).getValue();
		}
		
		public double getAmplitude(){
			return ((MguiDouble)attributes.getValue("Amplitude[mA]")).getValue();
		}
		
		public Attribute getAttribute(String attrName) {	
			return attributes.getAttribute(attrName);
		}

		public AttributeList getAttributes() {
			return attributes;
		}

		public void setAttribute(String attrName, Object newValue) {
			attributes.setValue(attrName, newValue);	
		}

		public void setAttributes(AttributeList thisList) {
			attributes = thisList;
		}
		
		@Override
		public void setTreeNode(InterfaceTreeNode treeNode){
			super.setTreeNode(treeNode);
			treeNode.add(attributes.issueTreeNode());
		}
		
		@Override
		public String toString(){
			return "Current Pulse";
		}
		
	}

}