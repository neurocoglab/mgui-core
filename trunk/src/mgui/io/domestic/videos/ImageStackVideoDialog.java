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

package mgui.io.domestic.videos;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.util.TimeFunctions;

/**************************************************
 * Sets parameters for an image stack output operation, via {@linkplain ImageStackVideoWriter}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ImageStackVideoDialog extends VideoOutputDialog {

	JLabel lblFormat = new JLabel("Format:");
	JComboBox cmbFormat = new JComboBox();
	JLabel lblImagesPerSecond = new JLabel("Images per sec:");
	JTextField txtImagesPerSecond = new JTextField("10");
	JLabel lblStart = new JLabel("Start time:");
	JTextField txtStart = new JTextField("00:00:00");
	JLabel lblStop = new JLabel("Stop time:");
	JTextField txtStop = new JTextField("00:01:00");
	JLabel lblWait = new JLabel("Wait for (ms):");
	JTextField txtWait = new JTextField("20");
	JCheckBox chkUseBuffer = new JCheckBox(" Use offscreen buffer");
	JCheckBox chkResample = new JCheckBox(" Resample to:");
	JTextField txtResample = new JTextField("0,0");
	
	public ImageStackVideoDialog(){
		super();
	}
	
	public ImageStackVideoDialog(JFrame aFrame, InterfacePanel panel, VideoOutputOptions options){
		super(aFrame, panel, options);
		_init();
	}
	
	private void _init(){
		if (options == null) options = new ImageStackVideoOptions(30, 0, 60000, 20, null);
		
		this.setButtonType(BT_OK_CANCEL);
		super.init();
		
		chkResample.addActionListener(this);
		chkResample.setActionCommand("Resample Changed");
		
		updateControls();
		
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		setDialogSize(550,340);
		setTitle("Image Stack Video Output Options");
	
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblFormat, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbFormat, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		mainPanel.add(lblImagesPerSecond, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		mainPanel.add(txtImagesPerSecond, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.24, 1);
		mainPanel.add(lblStart, c);
		c = new LineLayoutConstraints(3, 3, 0.3, 0.65, 1);
		mainPanel.add(txtStart, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.24, 1);
		mainPanel.add(lblStop, c);
		c = new LineLayoutConstraints(4, 4, 0.3, 0.65, 1);
		mainPanel.add(txtStop, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.24, 1);
		mainPanel.add(lblWait, c);
		c = new LineLayoutConstraints(5, 5, 0.3, 0.65, 1);
		mainPanel.add(txtWait, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.9, 1);
		mainPanel.add(chkUseBuffer, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.24, 1);
		mainPanel.add(chkResample, c);
		c = new LineLayoutConstraints(7, 7, 0.3, 0.65, 1);
		mainPanel.add(txtResample, c);
		
		cmbFormat.addItem("Portable Network Graphics (*.png)");
		
		this.setLocationRelativeTo(getParent().getParent());
	}
	
	void updateControls(){
		
		if (this.options == null) return;
		
		ImageStackVideoOptions options = (ImageStackVideoOptions)this.options;
		
		txtImagesPerSecond.setText("" + options.images_per_second);
		txtStart.setText(TimeFunctions.getTimeStr(options.start_time));
		txtStop.setText(TimeFunctions.getTimeStr(options.stop_time));
		txtWait.setText("" + options.wait);
		chkUseBuffer.setSelected(options.use_offscreen_buffer);
		chkResample.setSelected(options.resample != null);
		if (options.resample != null){
			txtResample.setText(options.resample.width + "," + options.resample.height);
			txtResample.setEnabled(true);
		}else{
			if (options.window != null){
				Dimension size = options.window.getSize();
				txtResample.setText(size.width + "," + size.height);
			}else{
				txtResample.setText("0,0");
				}
			txtResample.setEnabled(false);
			}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Resample Changed")){
			txtResample.setEnabled(chkResample.isSelected());
			if (!chkResample.isSelected()){
				ImageStackVideoOptions options = (ImageStackVideoOptions)this.options;
				if (options.window != null){
					Dimension size = options.window.getSize();
					txtResample.setText(size.width + "," + size.height);
				}else{
					txtResample.setText("0,0");
					}
				}
				
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			ImageStackVideoOptions options = (ImageStackVideoOptions)this.options;
			
			options.images_per_second = Integer.valueOf(txtImagesPerSecond.getText());
			options.start_time = TimeFunctions.getTimeFromStr(txtStart.getText());
			options.stop_time = TimeFunctions.getTimeFromStr(txtStop.getText());
			options.wait = Long.valueOf(txtWait.getText());
			options.use_offscreen_buffer = chkUseBuffer.isSelected();
			options.resample = null;
			if (chkResample.isSelected()){
				String[] vals = txtResample.getText().split(",");
				if (vals.length != 2){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Invalid resample text.", 
												  "Image Stack Video Options", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				try{
					options.resample = new Dimension(Integer.valueOf(vals[0]), Integer.valueOf(vals[1]));
				}catch (NumberFormatException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Invalid resample text.", 
							  "Image Stack Video Options", 
							  JOptionPane.ERROR_MESSAGE);
					return;
					}
				}
			
			panel.updateFromDialog(this);
			
			this.setVisible(false);
			return;
			}
		
		
		
		super.actionPerformed(e);
	}
	
}