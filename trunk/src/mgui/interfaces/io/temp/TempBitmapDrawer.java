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

package mgui.interfaces.io.temp;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import javax.swing.JPanel;

import mgui.numbers.NumberFunctions;


public class TempBitmapDrawer extends JPanel implements MouseListener {

	static final byte WHITE = (byte)255; // Byte.MAX_VALUE;
	static final byte BLACK = 0; // Byte.MIN_VALUE;

	byte[] data;
	BufferedImage image;
	public boolean editable = true;
	
	public TempBitmapDrawer(){
		
	}
	
	public TempBitmapDrawer(int N){
		init(N);
	}
	
	public void setData(double[] d){
		byte[] b = new byte[d.length];
		for (int i = 0; i < d.length; i++)
			b[i] = (byte)d[i];
		setData(b);
	}
	
	public void setData(byte[] b){
		int N = (int)Math.sqrt(NumberFunctions.getNextSquare(b.length));
		init(N);
		System.arraycopy(b, 0, data, 0, N * N);
	}
	
	public byte[] getData(){
		return data;
	}
	
	void init(int N){
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ComponentColorModel model = new ComponentColorModel(cs, null, false, false, 
															Transparency.OPAQUE, 0);
		//get raster
		WritableRaster raster = model.createCompatibleWritableRaster(N, N);
		
		data = ((DataBufferByte)raster.getDataBuffer()).getData();
		for (int i = 0; i < N * N; i++)
			data[i] = WHITE;
        
		//get BufferedImage
		image = new BufferedImage(model, raster, false, null);
		
		removeMouseListener(this);
		addMouseListener(this);
	}
	
	public void setEditable(boolean e){
		editable = e;
	}
	
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		if (image == null) return;
		
		Graphics2D g2 = (Graphics2D)g.create();
		
		int width = getWidth() - getInsets().left - getInsets().right;
		int height = getHeight() - getInsets().bottom - getInsets().top;
		
		//paint image
		g2.drawImage(image, getInsets().left, getInsets().top, width, height, null);
	
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		if (!editable) return;
		//get image coordinate
		int N = image.getHeight();
		//int w_img = image.getWidth();
		int h_panel = getHeight();
		int w_panel = getWidth();
		float v_fact = (float)N / (float)h_panel;
		float h_fact = (float)N / (float)w_panel;
		
		//invert y
		Point p = e.getPoint();
		//p.y = h_panel - p.y;
		
		int v_pos = Math.min(N, (int)(p.y * v_fact));
		int h_pos = Math.min(N, (int)(p.x * h_fact));
		
		//if (v_pos > 0) v_pos -= 1;
		//if (h_pos > 0) h_pos -= 1;
		
		
		//toggle data value
		byte b = data[N * v_pos + h_pos];
		if (b == BLACK) 
			b = WHITE;
		else 
			b = BLACK;
		data[N * v_pos + h_pos] = b;
		
		updateUI();
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}