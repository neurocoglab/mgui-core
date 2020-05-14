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

package mgui.interfaces.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.ProgressUpdater.Mode;
import mgui.numbers.MguiFloat;

/***************************************
 * Panel to be used as a progress bar for all operations which require one. All methods requiring such functionality
 * should accept an instance of the <code>ProgressUpdater</code> interface as an argument, and if the updater is
 * an instance of <code>InterfaceProgressBar</code>, should run a Foxtrot <code>Job</code> or <code>Task</code> thread.
 * 
 * <p>To show in an <code>InterfaceDisplayPanel</code>, this progress bar must be registered using the <code>register</code>
 * method. To remove it once a job is done, the <code>deregister</code> method must be called.
 * 
 * <p><b>Policy</b>: Utility classes will update progress bars, and register and deregister them. Registration must
 * be done from the EDT; only those methods indicated are safe to call from a Worker Thread; including: {@code update},
 * {@code setMinimum}, {@code setMaximum}, and {@code setMode}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class InterfaceProgressBar extends JPanel implements PropertyChangeListener,
															ProgressUpdater,
															ActionListener{

	public JProgressBar progressBar = new JProgressBar();
	//public String message;
	public boolean isDestroyed;
	//public InterfaceDisplayPanel displayPanel;
	JLabel label = new JLabel("Progress:");
	JButton cancel = new JButton("Cancel");
	int last_progress;
	float update_interval;
	boolean is_registered = false;
	boolean is_cancelled = false;
	boolean allow_changes = true; 
	
	public InterfaceProgressBar(){
		//this.displayPanel = displayPanel;
		init();
	}
	
	public InterfaceProgressBar(String message){
		//this.displayPanel = displayPanel;
		label.setText(message);
		init();
	}
	
	public InterfaceProgressBar(String message, int min, int max){
		this(message, min, max, true);
	}
	
	public InterfaceProgressBar(String message, int min, int max, boolean allow_changes){
		//this.displayPanel = displayPanel;
		label.setText(message);
		progressBar.setMinimum(min);
		progressBar.setMaximum(max);
		this.allow_changes = allow_changes;
		init();
	}
	
	public void setIndeterminate(boolean b){
		progressBar.setIndeterminate(b);
	}
	
	public boolean allowChanges(){
		return allow_changes;
	}
	
	private void init(){
		
		if (progressBar == null) progressBar = new JProgressBar();
		
		cancel.setActionCommand("Cancel");
		cancel.addActionListener(this);
		
		progressBar.setStringPainted(true);
		this.setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);
		add(progressBar, BorderLayout.CENTER);
		add(cancel, BorderLayout.EAST);
		
		reset();
	}
	
	/************************************************
	 * Sets the mode of this progress bar; one of {@code Mode.Determinate} or 
	 * {@code Mode.Indeterminate}. Calls SwingUtilities.invokeLater,
	 * so safe to call from a Thread.
	 * 
	 */
	public void setMode(final Mode mode){
		
		SwingUtilities.invokeLater(new Runnable(){
            public void run(){
				switch (mode){
					case Determinate:
						progressBar.setIndeterminate(false);
						return;
					case Indeterminate:
						progressBar.setIndeterminate(true);
						progressBar.setString("unknown");
						return;
					}
            }
        });
	}
	
	public void cancel(){
		is_cancelled = true;
	}
	
	public boolean isCancelled(){
		return is_cancelled;
	}
	
	/******************************************
	 * Sets the maximum for this progress bar. Calls SwingUtilities.invokeLater,
	 * so safe to call from a Thread.
	 * 
	 */
	public void setMinimum(final int min){
		 SwingUtilities.invokeLater(new Runnable(){
	            public void run(){
	            	progressBar.setMinimum(min);
	            }
	        });
	}
	
	/******************************************
	 * Sets the maximum for this progress bar. Calls SwingUtilities.invokeLater,
	 * so safe to call from a Thread.
	 * 
	 */
	public void setMaximum(final int max){
		SwingUtilities.invokeLater(new Runnable(){
            public void run(){
            	progressBar.setMaximum(max);
            }
        });
	}
	
	public int getMinimum(){
		return progressBar.getMinimum();
	}
	
	public int getMaximum(){
		return progressBar.getMaximum();
	}
	
	/***********************************************
	 * Sets this progress bar's message. Calls SwingUtilities.invokeLater,
	 * so safe to call from a Thread.
	 * 
	 */
	public void setMessage(final String message){
		SwingUtilities.invokeLater(new Runnable(){
            public void run(){
				label.setText(message);
				setSize(getWidth(), getHeight());
            }
        });
	}
	
	@Override
	public void setSize(int width, int height){
		this.setPreferredSize(new Dimension(width, height));
		int text_width = label.getFontMetrics(label.getFont()).stringWidth(label.getText());
		label.setPreferredSize(new Dimension(text_width, height));
		cancel.setPreferredSize(new Dimension(width / 10, height));
		progressBar.setPreferredSize(new Dimension(width, height));
	}
	
	/*************************
	 * Resets the progress bar with its minimum and maximum values.
	 */
	public void reset(){
		SwingUtilities.invokeLater(new Runnable(){
            public void run(){
				is_cancelled = false;
				last_progress = progressBar.getMinimum();
				progressBar.setValue(progressBar.getMinimum());
				update_interval = 100f / (progressBar.getMaximum() - progressBar.getMinimum());
            }
        });
	}
	
	/*************************
	 * Sets the current value of the progress bar
	 * 
	 * @param progress
	 */
	public void setValue(final int progress){
		SwingUtilities.invokeLater(new Runnable(){
            public void run(){
        		progressBar.setValue(progress);
            }
        });
	}
	
	/**************************
	 * Returns the instance of <code>JProgressBar</code> used to render this panel.
	 * @return
	 */
	public JProgressBar getProgressBar(){
		return progressBar;
	}
	
	/**************************
	 * Registers this progress bar with its display panel (i.e., by calling <code>registerProgressBar()</code>.
	 * This effectively shows the progress bar in the display panel so the user can see it.
	 * 
	 * <p>Note: must be called from EDT
	 * 
	 * @see mgui.interfaces.InterfaceDisplayPanel#registerProgressBar(InterfaceProgressBar) 
	 * InterfaceDisplayPanel.registerProgressBar
	 */
	public void register(){
		InterfaceSession.getDisplayPanel().registerProgressBar(this);
		progressBar.paintImmediately(0, 0, progressBar.getWidth(), progressBar.getHeight());
		is_registered = true;
	}
	
	/**************************
	 * Deregisters this progress bar with its display panel (i.e., by calling <code>deregisterProgressBar()</code>).
	 * This effectively removes the progress bar from the panel.
	 * 
	 * <p>Note: must be called from EDT
	 * 
	 * @see mgui.interfaces.InterfaceDisplayPanel#deregisterProgressBar(InterfaceProgressBar) 
	 * InterfaceDisplayPanel.deregisterProgressBar
	 */
	public void deregister(){
		InterfaceSession.getDisplayPanel().deregisterProgressBar();
		is_registered = false;
	}
	
	public boolean isRegistered(){
		return is_registered;
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        } 
	}
	
	public void iterate(){
		int value = progressBar.getValue();
		update(value + 1);
	}
	
	public void update(final int value) {
        // This method is called by the Foxtrot Worker thread, but I want to
        // update the GUI, so I use SwingUtilities.invokeLater, as the Task
        // is not finished yet.

        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
            	String s = MguiFloat.getString((float)value / (float)progressBar.getMaximum() * 100, "##0.0");
                progressBar.setValue(value);
                progressBar.setString(s);
            }
        });
    }
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Cancel")){
			//cancel button was pressed; inform the process
			is_cancelled = true;
			return;
			}
		
	}
	
}