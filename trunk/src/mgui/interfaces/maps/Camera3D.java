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

package mgui.interfaces.maps;

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.AxisAngle4d;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix3d;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector2d;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;
import org.xml.sax.Attributes;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.events.CameraEvent;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.resources.icons.IconObject;
import mgui.util.Colours;

/**********************************************************
 * Represents a particular camera position in R3, defined by a center of
 * rotation, line of sight vector, and a distance. The "up" position of the
 * camera is defined by an addition vector.
 * 
 * <p>In addition, the camera can be associated with an arbitrary number of
 * directed light sources, which are defined - and move - with respect to the
 * camera's line of sight.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Camera3D extends AbstractInterfaceObject implements AttributeObject,
																 XMLObject,
																 IconObject{

	//camera position (relative to some center of rotation)
		//center of rotation
		public Point3d centerOfRotation = new Point3d();
		//line of sight vector
		public Vector3d lineOfSight = new Vector3d(0, 0, 1);
		//camera distance from center of rotation
		public double distance;
		public double minDistance;
	//the up direction of the viewing surface
		public Vector3d upVector = new Vector3d(0, 1, 0);
	//camera target
		//translation from center-of-rotation
		public Point3d targetPt = (Point3d)centerOfRotation.clone();
	//translateXY screen
		public Vector2d translateXY = new Vector2d(0, 0);
	//light source object
		ArrayList<CameraLightSource> lights = new ArrayList<CameraLightSource>();
		//DirectionalLight lightSource;
		
	//working objects
		Vector3d vTemp = new Vector3d();
		Vector3d vTemp2 = new Vector3d();
		Vector3d vTemp3 = new Vector3d();
		//Vector3d vTemp2 = new Vector3d();
		Point3d eye = new Point3d();
		Transform3D R = new Transform3D();
		//Transform3D T = new Transform3D();
		Matrix4d M = new Matrix4d();
		Matrix3d M3 = new Matrix3d();
		AxisAngle4d axis = new AxisAngle4d();
	
	//camera listeners
		private ArrayList<Camera3DListener> listeners = new ArrayList<Camera3DListener>();
		boolean listen = true;
		
		
		AttributeList attributes;
		
		protected Icon icon;
		
	//constructor
	public Camera3D(){
		init();
	}
	
	public Camera3D(String name){
		init();
		setName(name);
	}
	
	protected void init(){
		attributes = new AttributeList();
		attributes.add(new Attribute<String>("Name", "Camera3D"));
		
		setIcon();
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
		
	//methods
	//set center of view (translation)
	public void setTargetPoint(Point3d t){
		targetPt = t;
		fireCameraListeners(CameraEvent.EventType.TargetChanged);
	}
	
	public Point3d getLensPoint(){
		Vector3d los = new Vector3d(lineOfSight);
		los.scale(-distance);
		Point3d lens_pt = new Point3d(targetPt);
		lens_pt.add(los);
		return lens_pt;
	}
	
	//set center of rotation
	public void setRotationPoint(Point3d r){
		centerOfRotation = r;
		fireCameraListeners(CameraEvent.EventType.CenterChanged);
	}
	
	//set line of sight vector
	public void setLineOfSight(float x, float y, float z){
		if (x == 0 && y == 0 && z == 0) return;
		lineOfSight = new Vector3d(x, y, z);
		lineOfSight.normalize();
		fireCameraAngleChanged();
	}
	
	//set line of sight vector
	public void setLineOfSight(Vector3d v){
		if (!(v.x == 0 && v.y == 0 && v.z == 0)){
			lineOfSight = v;
			fireCameraAngleChanged();
			}
		
	}

	//set the up vector
	public void setUpVector(Vector3d uv){
		upVector.normalize(uv);
		fireCameraListeners(CameraEvent.EventType.UpChanged);
	}
	
	//set the up vector
	public void setUpVector(double x, double y, double z){
		upVector.normalize(new Vector3d(x, y, z));
		fireCameraListeners(CameraEvent.EventType.UpChanged);
	}
	
	//make the up vector orthogonal to lineOfSight
	public void fixUpVector(){
		
	}

	//set distance
	public void setDistance(double d){
		if (d > minDistance)
			distance = d;
		fireCameraListeners(CameraEvent.EventType.ZoomChanged);
	}
	
	public void setMinDistance(double m){
		minDistance = m;
	}
	
	public void setTranslateXY(Vector2d vxy){
		translateXY = vxy;
		fireCameraListeners(CameraEvent.EventType.TranslationChanged);
	}

	public Point3d getTargetPoint(){
		return targetPt;
	}
	
	public void setCenterOfRotation(Point3f p){
		setCenterOfRotation(new Point3d(p));
	}
	
	public void setCenterOfRotation(Point3d p){
		centerOfRotation = p;
		//reset translation
		targetPt = (Point3d)p.clone();
		fireCameraListeners(CameraEvent.EventType.CenterChanged);
	}
	
	/*
	public void setLightSource(DirectionalLight ls){
		lightSource = ls;
	}
	*/
	
	public Point3d getCenterOfRotation(){
		return centerOfRotation;
	}

	public Vector3d getLineOfSight(){
		return lineOfSight;
	}
	
	public Vector3d getUpVector(){
		return upVector;
	}
	
	
	public double getDistance(){
		return distance;
	}
	
	public double getMinDistance(){
		return minDistance;
	}
	
	public Vector2d getTranslateXY(){
		return translateXY;
	}
	
	/*
	public DirectionalLight getLightSource(){
		return lightSource;
	}
	*/
	
	//alter the distance
	public void zoomDistance(double z){
		if (minDistance <= 0) minDistance = 1;
		distance += z;
		if (distance < minDistance) distance = minDistance;
		fireCameraListeners(CameraEvent.EventType.ZoomChanged);
	}
	
	//rotate around this transform's X-axis (Cross-product of upVector and lineOfSight)
	public void rotateX(double r){
		vTemp.cross(lineOfSight, upVector);
		axis.set(vTemp, -r);
		//axis = getAxisX(lineOfSight, r);
		R.set(axis);
		R.transform(lineOfSight);
		R.transform(upVector);
		lineOfSight.normalize();
		upVector.normalize();
		//updateLightSources();
		fireCameraAngleChanged();
	}
	
	//rotate around this transform's Y-axis (upVector)
	public void rotateY(double r){
		axis.set(upVector, -r);
		R.set(axis);
		R.transform(lineOfSight);
		R.transform(upVector);
		lineOfSight.normalize();
		//updateLightSources();
		fireCameraAngleChanged();
	}
	
	
	
	
	/**************
	 * Add a directional light which moves with this camera's line of sight, with zero offset
	 * @param light
	 * @param offset
	 */
	public void addLightSource(DirectionalLight light){
		addLightSource(light, new Vector2d(0,0));
	}
	
	/**************
	 * Add a directional light which moves with this camera's line of sight, offset by a specific
	 * vector.
	 * @param light
	 * @param offset
	 */
	public void addLightSource(DirectionalLight light, Vector2d offset){
		CameraLightSource s = new CameraLightSource(light, offset);
		lights.add(s);
		s.setName("Light source " + lights.size());
	}
	
	public void removeLightSource(DirectionalLight light){
		boolean isGone = false;
		for (int i = 0; i < lights.size(); i++)
			if (lights.get(i).getSource().equals(light)){
				lights.remove(i);
				isGone = true;
				i--;
			}else if (isGone){
				lights.get(i).setName("" + (i + 1));
				}
	}
	
	public void updateLightSources(){
		//vTemp.scale(-1, lineOfSight);
		for (int i = 0; i < lights.size(); i++){
			vTemp.scale(-1, lineOfSight);
			//vTemp.set(lineOfSight);
			rotateVector(vTemp, vTemp2, lights.get(i).getRotX(), lights.get(i).getRotY());
			lights.get(i).setDirection(new Vector3f(vTemp2));
			}
		
	}
	
	protected void rotateVector(Vector3d v, Vector3d r, double x, double y){
		r.set(v);
		getAxisX(v, x);
		R.set(axis);
		R.transform(r);
		getAxisY(r, y);
		R.set(axis);
		R.transform(r);
	}
	
	protected AxisAngle4d getAxisY(Vector3d v, double a){
		vTemp3.cross(v, upVector);
		axis.set(vTemp3, -a);
		return axis;
	}
	
	protected AxisAngle4d getAxisX(Vector3d v, double a){
		axis.set(upVector, -a);
		return axis;
	}
	
	//translate along this transform's X-axis
	public void translateX(double t){
		translateXY.x += t;
		fireCameraListeners(CameraEvent.EventType.TranslationChanged);
	}
	
	//translate along this transform's Y-axis
	public void translateY(double t){
		translateXY.y += t;
		fireCameraListeners(CameraEvent.EventType.TranslationChanged);
	}
	
	/****************************
	 * 
	 * Returns a transform to apply to ViewPlatform, corresponding to this camera.
	 * 
	 * @return
	 */
	public Transform3D getCameraTransform(){
		vTemp.scale(distance, lineOfSight);
		eye.add(centerOfRotation, vTemp);
		//orient line of sight
		R.lookAt(eye, centerOfRotation, upVector);
		try{
			R.invert();
		}catch (org.jogamp.vecmath.SingularMatrixException e){
			InterfaceSession.log("Matrix inversion error:");
			printCamera();
			}
		R.get(M3, vTemp);
		
		//set translation from transform (thanks Paxinos3D for this!)
		vTemp.x += M3.m00 * -translateXY.x;
		vTemp.y += M3.m10 * -translateXY.x;
		vTemp.z += M3.m20 * -translateXY.x;
		vTemp.x += M3.m01 * translateXY.y;
		vTemp.y += M3.m11 * translateXY.y;
		vTemp.z += M3.m21 * translateXY.y;
		
		R.set(M3, vTemp, 1.0);
		int flags = R.getType();
		if ((flags & Transform3D.CONGRUENT) == 0){
			InterfaceSession.log("\nNon-congruent transform:");
			printCamera();
			}

		return R;
	}
	
	/****************************
	 * 
	 * Returns a transform corresponding to this camera's rotation, but not translation.
	 * 
	 * @return
	 */
	public Transform3D getCameraRotation(double distance, Point3d center){
		vTemp.scale(distance, lineOfSight);
		eye.add(center, vTemp);
		//orient line of sight
		R.lookAt(eye, center, upVector);
		try{
			R.invert();
		}catch (org.jogamp.vecmath.SingularMatrixException e){
			InterfaceSession.log("Matrix inversion error:");
			printCamera();
			}
		//R.get(M3, vTemp2);
		//R.set(M3, vTemp, 1);
		return R;
	}
	
	public void printCamera(){
		InterfaceSession.log("Camera3D state:");
		InterfaceSession.log("Line of sight: " + lineOfSight.toString());
		InterfaceSession.log("Center of rotation: " + centerOfRotation.toString());
		InterfaceSession.log("Distance: " + MguiDouble.getString(getDistance(), "##0.00"));
		InterfaceSession.log("Up Vector: " + upVector.toString());
		InterfaceSession.log("TranslateXY: " + translateXY.toString());
	}
	
	public void addListener(Camera3DListener c){
		listeners.add(c);
	}
	
	public void removeListener(Camera3DListener c){
		listeners.remove(c);
	}
	
	public void setListen(boolean b){
		listen = b;
	}
	
	public void fireCameraAngleChanged(){
		if (!listen) return;
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).cameraAngleChanged(new CameraEvent(this));
	}
	
	public void fireCameraListeners(CameraEvent.EventType type){
		if (!listen) return;
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).cameraChanged(new CameraEvent(this, type));
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.add(attributes.issueTreeNode());
		
		for (int i = 0; i < lights.size(); i++)
			treeNode.add(lights.get(i).issueTreeNode());
		
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	//override to set a specific icon
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/camera_3d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/camera_3d_20.png");
	}
	
	@Override
	public Icon getObjectIcon() {
		if (icon == null) setIcon();
		return icon;
	}
	
	@Override
	public String toString(){
		return "Camera3D: " + getName();
	}
	
//	attribute object methods
	@Override
	public Attribute<?> getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}

	@Override
	public AttributeList getAttributes() {
		return attributes;
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}
	
	/************************************
	 * Sets this camera from {@code camera}.
	 * 
	 * @param camera
	 */
	public void setFromCamera(Camera3D camera){
		setFromCamera(camera, true, true);
	}
	
	public void setFromCamera(Camera3D camera, boolean set_center, boolean set_distance){
		this.lineOfSight.set(camera.lineOfSight);
		this.targetPt.set(camera.targetPt);
		this.translateXY.set(camera.translateXY);
		this.upVector.set(camera.upVector);
		if (set_center)	this.centerOfRotation.set(camera.centerOfRotation);
		if (set_distance) this.distance = camera.distance;
		fireCameraListeners(CameraEvent.EventType.CameraChanged);
		fireCameraAngleChanged();
	}
	
	public String getDTD(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getXMLSchema(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getXML(int tab){
		//get schema or dtd
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = _tab + "<Camera3D\n";
		xml = xml + _tab2 + "name = '" + getName() + "'\n";		//name
		xml = xml + _tab2 + "center = '" + XMLFunctions.getTupleStr(centerOfRotation, 8) + "'\n";		//center
		xml = xml + _tab2 + "line_of_sight = '" + XMLFunctions.getTupleStr(lineOfSight, 8) + "'\n";		//line of sight
		xml = xml + _tab2 + "up_vector = '" + XMLFunctions.getTupleStr(upVector, 8) + "'\n";			//up vector
		xml = xml + _tab2 + "distance = '" + MguiDouble.getString(distance, 8) + "'\n";					//distance
		xml = xml + _tab2 + "translate = '" +XMLFunctions.getVectorStr(translateXY, 8) + "'\n";			//translate
		
		//TODO: light sources?
		
		xml = xml + _tab + "/>\n";
		return xml;
	}
	
	public String getShortXML(int tab){
		return XMLFunctions.getTab(tab) + "<Camera3D name = '" + getName() + "' />\n";
	}
	
	public String getXML(){
		return getXML(0);
	}
	
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type){
		
		if (localName.equals(getLocalName())){
			setName(attributes.getValue("name"));
			lineOfSight = XMLFunctions.getVector3d(attributes.getValue("line_of_sight"));
			upVector = XMLFunctions.getVector3d(attributes.getValue("up_vector"));
			centerOfRotation = XMLFunctions.getPoint3d(attributes.getValue("center"));
			translateXY = XMLFunctions.getVector2d(attributes.getValue("translate"));
			distance = Double.valueOf(attributes.getValue("distance"));
			return;
			}
		
	}
	
	public void handleXMLElementEnd(String localName){
		
	}
	
	public void handleXMLString(String s){
		
	}
	
	public String getLocalName(){
		return "Camera3D";
	}
	
	public XMLObject getXMLInstance(Attributes attributes){
		return new Camera3D(attributes.getValue("name"));
	}
	
	public class CameraLightSource extends AbstractInterfaceObject implements AttributeObject,
																			  AttributeListener,
																			  IconObject {

		protected Icon light_icon;
		public DirectionalLight source;
		public AttributeList attributes;
		
		public CameraLightSource(DirectionalLight s, Vector2d o){
			source = s;
			init2();
			setRotX(o.x);
			setRotY(o.y);
		}
		
		protected void init2(){
			attributes = new AttributeList();
			attributes.add(new Attribute("Name", "0"));
			attributes.add(new Attribute("RotX", new MguiDouble(0)));
			attributes.add(new Attribute("RotY", new MguiDouble(0)));
			Color3f c = new Color3f();
			source.getColor(c);
			attributes.add(new Attribute("Colour", Colours.getAwtColor(c)));
			attributes.add(new Attribute("IsEnabled", new MguiBoolean(source.getEnable())));
			attributes.addAttributeListener(this);
			
			setIcon();
		}
		
		//override to set a specific icon
		protected void setIcon(){
			java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/lightsource_3d_20.png");
			if (imgURL != null)
				light_icon = new ImageIcon(imgURL);
			else
				InterfaceSession.log("Cannot find resource: mgui/resources/icons/lightsource_3d_20.png");
		}
		
		public void setDirection(Vector3f d){
			source.setDirection(d);
		}
		
		public void setRotX(double r){
			attributes.setValue("RotX", new MguiDouble(r));
		}
		
		public double getRotX(){
			return ((MguiDouble)attributes.getValue("RotX")).getValue();
		}
		
		@Override
		public void setName(String name){
			attributes.setValue("Name", name);
		}
		
		@Override
		public String getName(){
			return (String)attributes.getValue("Name");
		}
		
		public void setRotY(double r){
			attributes.setValue("RotY", new MguiDouble(r));
		}
		
		public double getRotY(){
			return ((MguiDouble)attributes.getValue("RotY")).getValue();
		}
		
		public void setColour(Color colour){
			attributes.setValue("Colour", colour);
			source.setColor(Colours.getColor3f(colour));
		}
		
		public Color getColour(){
			return (Color)attributes.getValue("Colour");
		}
		
		public void setIsEnabled(boolean b){
			attributes.setValue("IsEnabled", new MguiBoolean(b));
			source.setEnable(b);
		}
		
		public boolean getIsEnabled(){
			return ((MguiBoolean)attributes.getValue("IsEnabled")).getTrue();
		}
		
		public DirectionalLight getSource(){
			return source;
		}
		
		@Override
		public Attribute<?> getAttribute(String attrName) {	
			return attributes.getAttribute(attrName);
		}

		@Override
		public AttributeList getAttributes() {
			return attributes;
		}
		
		@Override
		public Object getAttributeValue(String name) {
			Attribute<?> attribute = getAttribute(name);
			if (attribute == null) return null;
			return attribute.getValue();
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
		
		public void attributeUpdated(AttributeEvent e) {
			if (e.getAttribute().getName().equals("IsEnabled"))
				source.setEnable(getIsEnabled());
				
			if (e.getAttribute().getName().equals("Colour"))
				source.setColor(Colours.getColor3f(getColour()));
			
			updateLightSources();
		}

		
		@Override
		public String toString(){
			return "Light Source: " + getName();
		}

		@Override
		public Icon getObjectIcon() {
			return light_icon;
		}
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		//XML string should be small, so get it directly
		writer.write(getXML(tab));
	}
	
}