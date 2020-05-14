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

package mgui.interfaces.shapes;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputAdapter;
import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point2f;

import mgui.geometry.Text2D;
import mgui.interfaces.graphics.GraphicMouseListener;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.maps.Map;
import mgui.interfaces.maps.Map2D;
import mgui.numbers.MguiDouble;

public class Text2DIntMouseCoords extends Text2DInt implements GraphicMouseListener {

	public InterfaceGraphic2D parent;
	public String messageStr = "";
	public Text2DIntMouseCoords thisRef = this;
	private ShapeMouseListener thisAdapter;
	private Map2D theMap;
	public int interfaceType = 0;
	
	//constants
	public static final int MOUSE_COORDS = 0;
	public static final int MOUSE_PICK_SHAPE = 1;
	public static final int MOUSE_PICK_SHAPE_PROXIMITY = 2;
	
	public Text2DIntMouseCoords(){
		super();
		init();
	}
	
	public Text2DIntMouseCoords(Text2D thisText2D){
		super(thisText2D);
		init();
	}
	
	public void setInterfaceType(int thisType){
		interfaceType = thisType;
	}
	
	class ShapeMouseListener extends MouseInputAdapter {
		@Override
		public void mouseMoved(MouseEvent e) {
			switch (interfaceType){
			case MOUSE_COORDS:
				//updateCoords(theMap.getMapPoint(e.getPoint()));
				Point2f thisPt = theMap.getMapPoint(e.getPoint());
				setText(messageStr + new MguiDouble(thisPt.x).toString("###,###.00") + ", " +
									 new MguiDouble(thisPt.y).toString("###,###.00"));
				
				parent.drawMouseShapes();
				break;
			}
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			Shape2DInt pickShape;
			
			switch (interfaceType){
			
			case MOUSE_PICK_SHAPE:
				pickShape = parent.pickShape(e.getPoint(), 10);
				if (pickShape == null)
					setText(messageStr);
					else
					setText("Object: " + pickShape.toString());
				//parent.drawShape(thisRef);
				parent.drawMouseShapes();
				break;
			
			case MOUSE_PICK_SHAPE_PROXIMITY:
				pickShape = parent.pickShape(e.getPoint(), 10);
				
				if (pickShape == null)
					setText(messageStr);
					else{
						Point2f pickPoint = ((Map2D)parent.getMap()).getMapPoint(e.getPoint());
						Point2f intPoint =  pickShape.thisShape.getProximityPoint(pickPoint);
						if (intPoint != null){ 
							setText("Proximity: " + MguiDouble.getString(pickPoint.distance(intPoint), "###,###.00"));
							/*
							Line2DInt pickLine = new Line2DInt(new Line2D(pickPoint, intPoint));
							pickLine.setAttribute("CoordSys", DrawEngine.DRAW_MAP);
							pickLine.setAttribute("Line Colour", Color.RED);
							float[] dashPattern = {5.0f};
							pickLine.setAttribute("Line Style", new BasicStroke(8.0f, BasicStroke.CAP_BUTT,
																				BasicStroke.JOIN_MITER, 10.0f, 
																				dashPattern, 0.0f));
							parent.drawShape(pickLine); */
							} 
						}
				//parent.drawShape(thisRef);
				parent.drawMouseShapes();
				break;
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			
		}
		
	}
	
	private void init(){
		thisAdapter = new ShapeMouseListener();
	}
	
	
	public void updateCoords(Point2d thisPt){
		updateCoords(thisPt.x, thisPt.y);
	}
	
	public void updateCoords(double x, double y){
		((Text2D)thisShape).setText(messageStr + new MguiDouble(x).toString("###,###.##") + ", " +
									  			 new MguiDouble(y).toString("###,###.##"));

	}
		
	public void setParentWindow(InterfaceGraphic thisParent){
		parent = (InterfaceGraphic2D)thisParent;
	}
	
	public MouseInputAdapter getMouseListener(){
		return thisAdapter;
	}
	
	public MouseWheelListener getMouseWheelListener(){
		return null;
	}
	
	public boolean isShape(){
		return true;
	}
	
	public void setMap(Map thisMap){
		if (thisMap instanceof Map2D)
			theMap = (Map2D)thisMap;
	}
	
	public void setMapType(int mt){
		
	}
	
	public void windowUpdated(InterfaceGraphic g) {
		// TODO Auto-generated method stub
		
	}

	
}