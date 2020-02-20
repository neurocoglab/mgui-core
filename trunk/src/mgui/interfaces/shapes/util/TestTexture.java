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

package mgui.interfaces.shapes.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.ImageComponent3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Texture3D;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.TexCoord3f;
import javax.vecmath.Vector4f;

import mgui.geometry.Polygon2D;
import mgui.geometry.Radius2D;
import mgui.geometry.Sphere3D;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.morph.sections.RadialRepresentation;

public class TestTexture {

	public int width;
    public int height;
    public int depth;
	//public Texture2D texture2D = new Texture2D(Texture2D.BASE_LEVEL,
	//										   Texture2D.RGB,
	//										   256,
	//										   256);
	public Texture2D texture2D = new Texture2D();
	public Texture3D texture3D; // = new Texture3D();
	public TexCoordGeneration texgen2D = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
																TexCoordGeneration.TEXTURE_COORDINATE_2);
	public TexCoordGeneration texgen3D = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
																TexCoordGeneration.TEXTURE_COORDINATE_3);
	
	public TestTexture(){
		File file = new File("C:\\Documents and Settings\\AndrewR\\My Documents\\Pictures\\java3d.jpg");
		try{
			LoadTexture2D(file.toURI().toURL());
			LoadTexture3D(file.toURI().toURL());
		}
		catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
	
	public void LoadTexture3D(URL url){
		//TextureLoader tl = new TextureLoader(url, null);
		BufferedImage urlImage;
		try{
		urlImage = ImageIO.read(url);
		}
		catch (IOException e){
			e.printStackTrace();
			return;
		}
		
		width = urlImage.getWidth();
        height = urlImage.getHeight();
        depth = 4; // urlImage.getWidth();
        urlImage = mgui.util.Colours.getRGBtoRGBA(urlImage, 1, 2);
        
        /**
        byte[] origData = ((DataBufferByte)urlImage.getData().getDataBuffer()).getData();

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB); 
        int[] nBits = {8, 8, 8, 8}; 
        ComponentColorModel colorModel = new ComponentColorModel(cs, nBits, true, false, 
        		Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
         
        WritableRaster raster = colorModel.createCompatibleWritableRaster(width, height); 
        BufferedImage bImage = new BufferedImage(colorModel, raster, false, null);
        byte[] byteData = ((DataBufferByte)raster.getDataBuffer()).getData(); 
        ImageComponent3D pArray = new ImageComponent3D(ImageComponent.FORMAT_RGBA, width, height, depth);

        for (int j = 0; j < height; j++)
                for (int i = 0; i < width; i++){ 
                    int indexb = ((j * width) + i) * 4;
                    int indexo = ((j * width) + i) * 3;
                    byteData[indexb + 2] = origData[indexo];
                    byteData[indexb + 1] = origData[indexo + 1];
                    byteData[indexb + 0] = origData[indexo + 2];
                    //calculate alpha
                    int alpha = Math.max(Math.max(byteToInt(origData[indexo]),
					                    		  byteToInt(origData[indexo + 1])),
					                    		  byteToInt(origData[indexo + 2]));
                    alpha = 255;
                    byte test = (byte)alpha;
                    byteData[indexb + 3] = test;
                } 
        **/
        
        ImageComponent3D pArray = new ImageComponent3D(ImageComponent.FORMAT_RGBA, width, height, depth);

        for (int k = 0; k < depth; k++) 
            pArray.set(k, urlImage);
        
        texture3D = new Texture3D(Texture.BASE_LEVEL,
                                  Texture.RGBA, 
                                  width, 
                                  height, 
                                  depth);
        texture3D.setImage(0, pArray);
        texture3D.setEnable(true);
        texture3D.setMinFilter(Texture.BASE_LEVEL_LINEAR);
        texture3D.setMagFilter(Texture.BASE_LEVEL_LINEAR);
        texture3D.setBoundaryModeS(Texture.CLAMP);
        texture3D.setBoundaryModeT(Texture.CLAMP);
        texture3D.setBoundaryModeR(Texture.CLAMP);
		
	}
	
	private int byteToInt(byte b){
		int val = b;
		if (val < 0) val += 256;
		return val;
	}
	
	public void LoadTexture2D(URL url){
		try{
			BufferedImage bi = ImageIO.read(url);
		
			ImageComponent2D image = new ImageComponent2D(ImageComponent.FORMAT_RGB, bi);
			
			texture2D = new Texture2D(Texture.BASE_LEVEL,
									  Texture.RGB,
									  powerOfTwo(image.getWidth()),
									  powerOfTwo(image.getHeight()));
			texture2D.setImage(0, image);
			
		}
		catch (IOException e){
			e.printStackTrace();
			return;
		}
		
		
	}
	
	public TexCoordGeneration getTexGen3D(Shape3DInt s){
		TexCoordGeneration gen = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
														TexCoordGeneration.TEXTURE_COORDINATE_3);
		Sphere3D sphere = s.getBoundSphere();
		float scale = -0.5f / sphere.radius;
		
		ArrayList<Point3f> nodes = s.shape3d.getVertices();
		
		Vector4f sPlane = new Vector4f(scale, 0, 0, -nodes.get(1).x);
		Vector4f tPlane = new Vector4f(0, scale, 0, -nodes.get(1).y);
		Vector4f rPlane = new Vector4f(0, 0, scale, -nodes.get(1).z);
		
		gen.setPlaneS(sPlane);
		gen.setPlaneT(tPlane);
		gen.setPlaneR(rPlane);
		
		return gen;
	}
	
	public TexCoordGeneration getTexGen2D(Shape3DInt s){
		TexCoordGeneration gen = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
														TexCoordGeneration.TEXTURE_COORDINATE_2);
		Sphere3D sphere = s.getBoundSphere();
		float rad = 1.0f / sphere.radius;
		float dist = sphere.center.distance(new Point3f(0,0,0)) - sphere.radius;
		Vector4f sPlane = new Vector4f(rad, 0, 0, sphere.center.x - sphere.radius * (float)Math.sin(Math.PI / 4));
		Vector4f tPlane = new Vector4f(0, rad, 0, sphere.center.y - sphere.radius * (float)Math.cos(Math.PI / 4));
		gen.setPlaneS(sPlane);
		gen.setPlaneT(tPlane);
		return gen;
	}
	
	public TexCoord3f[] getTexCoords3d(int d){
		
		float dist = (float)depth / (float)(d + 1);
		TexCoord3f[] texPts = new TexCoord3f[4];
		
		for (int i = 0; i < 4; i++)
			texPts[i] = new TexCoord3f(getTexPt3d(i, dist));
		//for (int i = 4; i < 8; i++)
		//	texPts[i] = new TexCoord3f(getTexPt3d(i, 1.0f));
		
		return texPts;
	}
	
	public TexCoord2f[] getTexCoords(){
		
		TexCoord2f[] texPts = new TexCoord2f[4];
		
		for (int i = 0; i < 4; i++)
			texPts[i] = new TexCoord2f(getTexPt(i));
		
		return texPts;
	}
	
	//return an array of texture coordinates to fit vertices of shape s
	public TexCoord2f[] getTexCoords(Polygon2D s){
		//for polygons, use radial rep
		//possibly this is extendable to any node set?
		RadialRepresentation radrep = new RadialRepresentation(s);
		
		//for each radius, assign a texture coordinate
		Radius2D radius;
		Point2f texPt;
		TexCoord2f[] texPts = new TexCoord2f[s.vertices.size()];

		for (int i = 0; i < radrep.RadialNodes.size(); i++){
			radius = radrep.RadialNodes.get(i);
			texPt = getTexPtFromAngle(radius.angle);
			//texPt = getTexPt(i);
			texPts[radrep.getNodeIndex(i)] = new TexCoord2f(texPt);
			}
		
		return texPts;
	}
	
	private Point3f getTexPt3d(int i, float z){
		Point2f pt2 = getTexPt(i);
		Point3f pt3 = new Point3f();
		pt3.x = pt2.x;
		pt3.y = pt2.y;
		pt3.z = z;
		return pt3;
	}
	
	private Point2f getTexPt(int i){
		Point2f retPt = new Point2f();
		
		switch (i){
		
		case 0:
			retPt.x = 0;
			retPt.y = 1;
			break;
		case 1:
			retPt.x = 0;
			retPt.y = 0;
			break;
		case 2:
			retPt.x = 1;
			retPt.y = 0;
			break;
		case 3:
			retPt.x = 1;
			retPt.y = 1;
			break;
		
		}
		return retPt;
	}
	
	private Point2f getTexPtFromAngle(float theta){
		Point2f retPt = new Point2f();
		
		//f(x,theta):
		if ((theta >= 0 && theta < Math.PI / 4.0) || 
			(theta >= 7.0 * (Math.PI / 4.0) && theta < 2 * Math.PI))
			retPt.x = 1;
		if (theta >= 3.0 * (Math.PI / 4.0) && theta < 5.0 * (Math.PI / 4.0))
			retPt.x = 0;
		if (theta >= Math.PI / 4.0 && theta < 3.0 * (Math.PI / 4.0))
			retPt.x = (float)((3.0 / 2.0) + ((2.0 * theta) / Math.PI));
		if (theta >= 5.0 * (Math.PI / 4.0) && theta < 7.0 * (Math.PI / 4.0))
			retPt.x = (float)((2.0 * theta / Math.PI) - (5.0 / 2.0));
		
		//f(y,theta):
		if (theta >= Math.PI / 4.0 && theta < 3.0 * (Math.PI / 4.0))
			retPt.y = 1;
		if (theta >= 5.0 * (Math.PI / 4.0) && theta < 7.0 * (Math.PI / 4.0))
			retPt.y = 0;
		if (theta >= 0 && theta < Math.PI / 4.0)
			retPt.y = (float)(2.0 * theta / Math.PI);
		if (theta >= 7.0 * (Math.PI / 4.0) && theta <= Math.PI * 2.0)
			retPt.y = (float)((2.0 * theta) - (7.0 * Math.PI / 2.0));
		if (theta >= 3.0 * (Math.PI / 4.0) && theta < 5.0 * (Math.PI / 4.0))
			retPt.y = (float)(5.0 / 2.0 - (2.0 * theta / Math.PI));
		
		return retPt;
	}
	
	private void normalize(Vector4f v){
		float max = v.x;
		if (max < v.y) max = v.y;
		if (max < v.z) max = v.z;
		v.x /= max;
		v.y /= max;
		v.z /= max;
	}
	
	private int powerOfTwo(int value) {
		int retval = 16;
		while (retval < value) {
		    retval *= 2;
		}
		return retval;
	    }
	
}