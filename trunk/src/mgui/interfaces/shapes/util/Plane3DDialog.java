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

package mgui.interfaces.shapes.util;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Plane3D;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;

/*******************************************************
 * Dialog box to specify a plane 3D
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Plane3DDialog extends InterfaceDialogBox {

	protected Plane3D current_plane;
	protected boolean plane_is_changed = false;
	
	JLabel lblOrigin = new JLabel("Origin:");
	JLabel lblOriginX = new JLabel(" X "); 
	JTextField txtOriginX = new JTextField("0");
	JLabel lblOriginY = new JLabel(" Y "); 
	JTextField txtOriginY = new JTextField("0");
	JLabel lblOriginZ = new JLabel(" Z "); 
	JTextField txtOriginZ = new JTextField("0");
	JLabel lblAxisX = new JLabel("X-Axis");
	JButton cmdDefX = new JButton("Def");
	JLabel lblAxisX_X = new JLabel(" X "); 
	JTextField txtAxisX_X = new JTextField("1");
	JLabel lblAxisX_Y = new JLabel(" Y "); 
	JTextField txtAxisX_Y = new JTextField("0");
	JLabel lblAxisX_Z = new JLabel(" Z "); 
	JTextField txtAxisX_Z = new JTextField("0");
	JLabel lblAxisY = new JLabel("Y-Axis");
	JButton cmdDefY = new JButton("Def");
	JLabel lblAxisY_X = new JLabel(" X "); 
	JTextField txtAxisY_X = new JTextField("0");
	JLabel lblAxisY_Y = new JLabel(" Y "); 
	JTextField txtAxisY_Y = new JTextField("1");
	JLabel lblAxisY_Z = new JLabel(" Z "); 
	JTextField txtAxisY_Z = new JTextField("0");
	JLabel lblAxisZ = new JLabel("Normal");
	JButton cmdDefZ = new JButton("Def");
	JLabel lblAxisZ_X = new JLabel(" X "); 
	JTextField txtAxisZ_X = new JTextField("0");
	JLabel lblAxisZ_Y = new JLabel(" Y "); 
	JTextField txtAxisZ_Y = new JTextField("0");
	JLabel lblAxisZ_Z = new JLabel(" Z "); 
	JTextField txtAxisZ_Z = new JTextField("1");
	JCheckBox chkAxisZ_flip = new JCheckBox(" Flip");
	
	public Plane3DDialog(JFrame frame){
		this(frame, new Plane3D(new Point3f(0,0,0),
								new Vector3f(1,0,0),
								new Vector3f(0,1,0)));
	}
	
	public Plane3DDialog(JFrame frame, Plane3D current_plane){
		super(frame);
		_init();
		setCurrentPlane(current_plane);
	}
	
	private void _init(){
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		super.init();
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(500,400);
		this.setTitle("Specify Plane3D");
		
		txtAxisX_X.setActionCommand("Axis Changed X_x");
		txtAxisX_X.addActionListener(this);
		txtAxisX_Y.setActionCommand("Axis Changed X_y");
		txtAxisX_Y.addActionListener(this);
		txtAxisX_Z.setActionCommand("Axis Changed X_z");
		txtAxisX_Z.addActionListener(this);
		txtAxisY_X.setActionCommand("Axis Changed Y_x");
		txtAxisY_X.addActionListener(this);
		txtAxisY_Y.setActionCommand("Axis Changed Y_y");
		txtAxisY_Y.addActionListener(this);
		txtAxisY_Z.setActionCommand("Axis Changed Y_z");
		txtAxisY_Z.addActionListener(this);
		txtAxisZ_X.setEditable(false);
		txtAxisZ_Y.setEditable(false);
		txtAxisZ_Z.setEditable(false);
		chkAxisZ_flip.addActionListener(this);
		chkAxisZ_flip.setActionCommand("Axis Flip Z");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, .7, 1);
		mainPanel.add(lblOrigin, c);
		c = new LineLayoutConstraints(2, 2, 0.05, .1, 1);
		mainPanel.add(lblOriginX, c);
		lblOriginX.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(2, 2, 0.15, .2, 1);
		mainPanel.add(txtOriginX, c);
		c = new LineLayoutConstraints(2, 2, 0.35, .1, 1);
		mainPanel.add(lblOriginY, c);
		lblOriginY.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(2, 2, 0.45, .2, 1);
		mainPanel.add(txtOriginY, c);
		c = new LineLayoutConstraints(2, 2, 0.65, .1, 1);
		mainPanel.add(lblOriginZ, c);
		lblOriginZ.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(2, 2, 0.75, .2, 1);
		mainPanel.add(txtOriginZ, c);
		
		//X-axis
		c = new LineLayoutConstraints(3, 3, 0.05, 0.2, 1);
		mainPanel.add(lblAxisX, c);
		c = new LineLayoutConstraints(3, 3, 0.55, 0.4, 1);
		mainPanel.add(cmdDefX, c);
		c = new LineLayoutConstraints(4, 4, 0.05, .1, 1);
		mainPanel.add(lblAxisX_X, c);
		c = new LineLayoutConstraints(4, 4, 0.15, .2, 1);
		mainPanel.add(txtAxisX_X, c);
		lblAxisX_X.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(4, 4, 0.35, .1, 1);
		mainPanel.add(lblAxisX_Y, c);
		c = new LineLayoutConstraints(4, 4, 0.45, .2, 1);
		mainPanel.add(txtAxisX_Y, c);
		lblAxisX_Y.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(4, 4, 0.65, .1, 1);
		mainPanel.add(lblAxisX_Z, c);
		c = new LineLayoutConstraints(4, 4, 0.75, .2, 1);
		mainPanel.add(txtAxisX_Z, c);
		lblAxisX_Z.setHorizontalAlignment(SwingConstants.RIGHT);
		
		//Y-Axis
		c = new LineLayoutConstraints(5, 5, 0.05, 0.2, 1);
		mainPanel.add(lblAxisY, c);
		c = new LineLayoutConstraints(5, 5, 0.55, 0.4, 1);
		mainPanel.add(cmdDefY, c);
		c = new LineLayoutConstraints(6, 6, 0.05, .1, 1);
		mainPanel.add(lblAxisY_X, c);
		lblAxisY_X.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(6, 6, 0.15, .2, 1);
		mainPanel.add(txtAxisY_X, c);
		c = new LineLayoutConstraints(6, 6, 0.35, .1, 1);
		mainPanel.add(lblAxisY_Y, c);
		lblAxisY_Y.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(6, 6, 0.45, .2, 1);
		mainPanel.add(txtAxisY_Y, c);
		c = new LineLayoutConstraints(6, 6, 0.65, .1, 1);
		mainPanel.add(lblAxisY_Z, c);
		lblAxisY_Z.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(6, 6, 0.75, .2, 1);
		mainPanel.add(txtAxisY_Z, c);
		
		//Z-Axis
		c = new LineLayoutConstraints(7, 7, 0.05, 0.2, 1);
		mainPanel.add(lblAxisZ, c);
		c = new LineLayoutConstraints(7, 7, 0.55, 0.4, 1);
		mainPanel.add(chkAxisZ_flip, c);
		c = new LineLayoutConstraints(8, 8, 0.05, .1, 1);
		mainPanel.add(lblAxisZ_X, c);
		lblAxisZ_X.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(8, 8, 0.15, .2, 1);
		mainPanel.add(txtAxisZ_X, c);
		c = new LineLayoutConstraints(8, 8, 0.35, .1, 1);
		mainPanel.add(lblAxisZ_Y, c);
		lblAxisZ_Y.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(8, 8, 0.45, .2, 1);
		mainPanel.add(txtAxisZ_Y, c);
		c = new LineLayoutConstraints(8, 8, 0.65, .1, 1);
		mainPanel.add(lblAxisZ_Z, c);
		lblAxisZ_Z.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(8, 8, 0.75, .2, 1);
		mainPanel.add(txtAxisZ_Z, c);
	
		
		
	}
	
	void setCurrentPlane(Plane3D plane){
		
		chkAxisZ_flip.setSelected(plane.flip_normal);
		
		txtOriginX.setText(MguiDouble.getString(plane.origin.x, "#0.000"));
		txtOriginY.setText(MguiDouble.getString(plane.origin.y, "#0.000"));
		txtOriginZ.setText(MguiDouble.getString(plane.origin.z, "#0.000"));
		
		txtAxisX_X.setText(MguiDouble.getString(plane.xAxis.x, "#0.000"));
		txtAxisX_Y.setText(MguiDouble.getString(plane.xAxis.y, "#0.000"));
		txtAxisX_Z.setText(MguiDouble.getString(plane.xAxis.z, "#0.000"));
		
		txtAxisY_X.setText(MguiDouble.getString(plane.yAxis.x, "#0.000"));
		txtAxisY_Y.setText(MguiDouble.getString(plane.yAxis.y, "#0.000"));
		txtAxisY_Z.setText(MguiDouble.getString(plane.yAxis.z, "#0.000"));
		
		setAxisZ(plane.getNormal());
		
	}
	
	void updateAxisZ(){
		
		Vector3f x_axis = getAxisX();
		Vector3f y_axis = getAxisY();
		
		Vector3f normal = new Vector3f();
		normal.cross(x_axis, y_axis);
		
		if (chkAxisZ_flip.isSelected())
			normal.scale(-1);
		
		setAxisZ(normal);
	}
	
	Vector3f getAxisX(){
		
		return new Vector3f(MguiFloat.getValue(txtAxisX_X.getText()),
					  	    MguiFloat.getValue(txtAxisX_Y.getText()),
					  	  	MguiFloat.getValue(txtAxisX_Z.getText()));
				
	}
	
	Vector3f getAxisY(){
		
		return new Vector3f(MguiFloat.getValue(txtAxisY_X.getText()),
					  	    MguiFloat.getValue(txtAxisY_Y.getText()),
					  	    MguiFloat.getValue(txtAxisY_Z.getText()));
		
	}
	
	
	
	void setAxisZ(Vector3f v){
		
		txtAxisZ_X.setText("" + v.x);
		txtAxisZ_Y.setText("" + v.y);
		txtAxisZ_Z.setText("" + v.z);
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().startsWith("Axis")){
			
			if (e.getActionCommand().contains("Changed")){
				updateAxisZ();
				}
			
			if (e.getActionCommand().contains("Flip")){
				updateAxisZ();
				}
			
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			this.current_plane = new Plane3D();
			
			current_plane.flip_normal = chkAxisZ_flip.isSelected();
			current_plane.origin.x = Float.valueOf(txtOriginX.getText()).floatValue();
			current_plane.origin.y = Float.valueOf(txtOriginY.getText()).floatValue();
			current_plane.origin.z = Float.valueOf(txtOriginZ.getText()).floatValue();
			current_plane.xAxis.x = Float.valueOf(txtAxisX_X.getText()).floatValue();
			current_plane.xAxis.y = Float.valueOf(txtAxisX_Y.getText()).floatValue();
			current_plane.xAxis.z = Float.valueOf(txtAxisX_Z.getText()).floatValue();
			current_plane.yAxis.x = Float.valueOf(txtAxisY_X.getText()).floatValue();
			current_plane.yAxis.y = Float.valueOf(txtAxisY_Y.getText()).floatValue();
			current_plane.yAxis.z = Float.valueOf(txtAxisY_Z.getText()).floatValue();
			
			plane_is_changed = true;
			setVisible(false);
			return;
			}
		
		
		super.actionPerformed(e);
	}
	
	public boolean isChanged(){
		return plane_is_changed;
	}
	
	public Plane3D getCurrentPlane(){
		return current_plane;
	}
	
	/********************************************************
	 * Shows a dialog box to specify a plane.
	 * 
	 * @param current_plane
	 * @return the new plane, or {@code null} if user cancelled.
	 */
	public static Plane3D getPlane3D(){
		 return getPlane3D(new Plane3D(new Point3f(0,0,0),
										new Vector3f(1,0,0),
										new Vector3f(0,1,0)));
	}
	
	/********************************************************
	 * Shows a dialog box to change the current plane.
	 * 
	 * @param current_plane
	 * @return the modified plane, or {@code null} if user cancelled.
	 */
	public static Plane3D getPlane3D(Plane3D current_plane){
		
		Plane3DDialog dialog = new Plane3DDialog(InterfaceSession.getSessionFrame(), current_plane);
		dialog.setVisible(true);
		
		if (dialog.isChanged())
			return dialog.getCurrentPlane();
		
		return null;
	}
}