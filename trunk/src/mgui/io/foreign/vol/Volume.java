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

package mgui.io.foreign.vol;

/*
 *	%Z%%M% %I% %E% %U%
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.net.URL;

import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;

import mgui.interfaces.InterfaceSession;



public class Volume implements VolRendConstants {

    Context	context; // context which holds attrs

    // should loadXXX flip the t axis
    boolean	tFlip = true;

    // attrs this object cares about
    StringAttr	dataFileAttr;
    CoordAttr	volRefPtAttr;

    // current values derived from attrs
    String 	filename = null;

    boolean	reloadNeeded = true;

    // The file specifies the hard limits on the size: xDim, yDim, zDim
    VolFile 	vol = null;
    int 	xDim = 0;
    int 	yDim = 0;
    int 	zDim = 0;

    // The texture space (VOI) limits for the volume
    int		xMin, xMax;
    int		yMin, yMax;
    int		zMin, zMax;

    // The texture -> geometry scaling for the volume
    double	xSpace, ySpace, zSpace;

    // The texture sizes for the volume (powers of two)
    int		xTexSize;
    int		yTexSize;
    int		zTexSize;

    // Texture scaling factors (for tex gen, coord * scale = texCoord)
    // These are floats since the TG interface takes Vector4f's
    float	xTexGenScale, yTexGenScale, zTexGenScale;

    // The 3D space limits for the volume
    Point3d	minCoord = new Point3d();
    Point3d	maxCoord = new Point3d();

    // VOI box points
    Point3d[]	voiPts = new Point3d[8];
    Point3d[] 	lineCoords = new Point3d[24];
    LineArray 	lineArray;
    Shape3D	voiBoxShape;

    // VOI box faces (AR: planes defining VOI)
    Point3d[][] facePoints = new Point3d[6][];

    // The edit id, changes each time the volume changes
    int		editId = 0;

    // The center of the view limits-- the initial Vol ref pt
    Point3d 	initVolRefPt = new Point3d();

    byte[] emptyByteRow = new byte[1024];
    int[] emptyIntRow = new int[1024];

//   boolean debug = false;

    //constructor changed AR 011907
    public Volume(StringAttr file, CoordAttr refPt) {
//	debug = Boolean.getBoolean("debug");
        //context = initContext;
	//dataFileAttr = (StringAttr) context.getAttr("Data File");
	//volRefPtAttr = (CoordAttr) context.getAttr("Vol Ref Pt");
	dataFileAttr = file;
	volRefPtAttr = refPt;

	for (int i = 0; i < 8; i++) {
	   voiPts[i] = new Point3d();
	}
        for (int i = 0; i < 6; i++) {
            facePoints[i] = new Point3d[4];
        }

	facePoints[PLUS_X][0] =  voiPts[5];
	facePoints[PLUS_X][1] =  voiPts[4];
	facePoints[PLUS_X][2] =  voiPts[7];
	facePoints[PLUS_X][3] =  voiPts[6];

	facePoints[PLUS_Y][0] =  voiPts[2];
	facePoints[PLUS_Y][1] =  voiPts[3];
	facePoints[PLUS_Y][2] =  voiPts[7];
	facePoints[PLUS_Y][3] =  voiPts[6];

	facePoints[PLUS_Z][0] =  voiPts[1];
	facePoints[PLUS_Z][1] =  voiPts[2];
	facePoints[PLUS_Z][2] =  voiPts[6];
	facePoints[PLUS_Z][3] =  voiPts[5];

	facePoints[MINUS_X][0] =  voiPts[0];
	facePoints[MINUS_X][1] =  voiPts[1];
	facePoints[MINUS_X][2] =  voiPts[2];
	facePoints[MINUS_X][3] =  voiPts[3];

	facePoints[MINUS_Y][0] =  voiPts[0];
	facePoints[MINUS_Y][1] =  voiPts[4];
	facePoints[MINUS_Y][2] =  voiPts[5];
	facePoints[MINUS_Y][3] =  voiPts[1];

	facePoints[MINUS_Z][0] =  voiPts[0];
	facePoints[MINUS_Z][1] =  voiPts[3];
	facePoints[MINUS_Z][2] =  voiPts[7];
	facePoints[MINUS_Z][3] =  voiPts[4];
    }

    public void setTFlip(boolean tFlip) {
	this.tFlip  = tFlip;
    }

    private void updateVoiShape() {
	//lineArray.setCoordinates(0, lineCoords);
    }

    Shape3D getVoiShape() {
	return voiBoxShape;
    }

    // returns the edit id for the volume
    public int update() {

	String curFilename = dataFileAttr.getValue();
	if (filename == curFilename) {
	   return editId;
	}
	// Going to reload the volume, bump the id
	editId++;

	filename = curFilename;
	vol = null;
	if (filename == null) {
	   return editId; // got new data, none!
	}
	URL fileURL = null;
	try {
	    URL path = context.getCodeBase();
	    fileURL = new URL(path.toString() + filename);
	} catch (java.net.MalformedURLException ex) {
	    InterfaceSession.log(ex.getMessage());
	    return editId;
	}
	try {
	    vol = new VolFile(fileURL);
	} catch (java.io.IOException e) {
	    InterfaceSession.log("Exception " + e + " opening VolFile with " +
		" filename " + filename);
	    return editId;
	}

	InterfaceSession.log("Id is : " + vol.getId());
	InterfaceSession.log("Volume is " + vol.xDim + "x" + vol.yDim + "x" +
		vol.zDim);

	// These are the real size of the data
	xDim = vol.xDim;
	yDim = vol.yDim;
	zDim = vol.zDim;

	// init VOI is whole volume
	xMin = yMin = zMin = 0;
	xMax = xDim;
	yMax = yDim;
	zMax = zDim;

	// Note: texture is always loaded the same, VOI just changes the
	// coordinates of the points (and through TexGen, the tex coords
	/// of the points).

	// tex size is next power of two greater than max - min
	xTexSize = powerOfTwo(xMax - xMin);
	yTexSize = powerOfTwo(yMax - yMin);
	zTexSize = powerOfTwo(zMax - zMin);

	InterfaceSession.log("tex size is " + xTexSize + "x" + yTexSize + "x" +
		zTexSize);

	maxCoord.x = xMax * vol.xSpace;
	maxCoord.y = yMax * vol.ySpace;
	maxCoord.z = zMax * vol.zSpace;
	double max = maxCoord.x;
	if (max < maxCoord.y) {
	    max = maxCoord.y;
	}
	if (max < maxCoord.z) {
	    max = maxCoord.z;
	}
	double scale = 1.0 / max;
	xSpace = vol.xSpace * scale;
	ySpace = vol.ySpace * scale;
	zSpace = vol.zSpace * scale;

	xTexGenScale =  (float)(1.0 / (xSpace * xTexSize));
	yTexGenScale =  (float)(1.0 / (ySpace * yTexSize));
	zTexGenScale =  (float)(1.0 / (zSpace * zTexSize));

	// the min and max coords are for the usable area of the texture,
	// which is has a half-texel boundary.  Otherwise the boundary
	// gets sampled, leading to artifacts with a texture color table.
	minCoord.x = (xMin + 0.5f) * xSpace;
	minCoord.y = (yMin + 0.5f) * ySpace;
	minCoord.z = (zMin + 0.5f) * zSpace;

	maxCoord.x = (xMax - 0.5f) * xSpace;
	maxCoord.y = (yMax - 0.5f) * ySpace;
	maxCoord.z = (zMax - 0.5f) * zSpace;
	/*
	InterfaceSession.log("zMin = " + zMin);
	InterfaceSession.log("zMax = " + zMax);
	InterfaceSession.log("minCoord.z = " + minCoord.z);
	InterfaceSession.log("maxCoord.z = " + maxCoord.z);
	InterfaceSession.log("minTexCoord.z = " + minCoord.z * zTexGenScale);
	InterfaceSession.log("maxTexCoord.z = " + maxCoord.z * zTexGenScale);
	*/

	// setup the VOI box points
        voiPts[0].x = voiPts[1].x = voiPts[2].x = voiPts[3].x = minCoord.x;
        voiPts[4].x = voiPts[5].x = voiPts[6].x = voiPts[7].x = maxCoord.x;
        voiPts[0].y = voiPts[1].y = voiPts[4].y = voiPts[5].y = minCoord.y;
        voiPts[2].y = voiPts[3].y = voiPts[6].y = voiPts[7].y = maxCoord.y;
        voiPts[0].z = voiPts[3].z = voiPts[4].z = voiPts[7].z = minCoord.z;
        voiPts[1].z = voiPts[2].z = voiPts[5].z = voiPts[6].z = maxCoord.z;

	updateVoiShape();

	// TODO: how to set here, but not clobber value from restore()?
	// perhaps set in VolRend?
	initVolRefPt.x = (maxCoord.x + minCoord.x) / 2;
	initVolRefPt.y = (maxCoord.y + minCoord.y) / 2;
	initVolRefPt.z = (maxCoord.z + minCoord.z) / 2;
	//InterfaceSession.log("init view pt is " + initVolRefPt);
	volRefPtAttr.set(initVolRefPt);


	//InterfaceSession.log("minCoord = " + minCoord);
	//InterfaceSession.log("maxCoord = " + maxCoord);

	return editId;

    }

    public boolean hasData() {
	return (vol != null);
    }

    private int powerOfTwo(int value) {
	int retval = 16;
	while (retval < value) {
	    retval *= 2;
	}
	return retval;
    }

    // Note:
    // Java3D "flips" images along the "t" axis, so we load the images into
    // the buffer from the "top" down.  That is, we use (numRows - row - 1)
    // instead of (row).

    // load byteData with Intensity values
    void loadZIntensity(int zValue, byte[] byteData) {
    	loadZIntensity(zValue, byteData, 0);
    }

    void loadZIntensity(int zValue, byte[] byteData, int byteOffset) {
	for (int y=0; y < yDim; y++){
	    byte[] vRow = vol.fileData[zValue][y];
	    int rowIndex = 0;
	    if (tFlip) {
		rowIndex = (yTexSize - y - 1) * xTexSize;
	    } else {
		rowIndex = y * xTexSize;
	    }
	    System.arraycopy(vRow, 0, byteData, byteOffset + rowIndex, xDim);
	}
    }

    // this routine loads values for constant yValue, the texture map is
    // stored in x,z format (x changes fastest)
    void loadYIntensity(int yValue, byte[] byteData)  {
	for (int z=0; z < zDim; z++){
	    int rowIndex;
	    if (tFlip) {
	    	rowIndex = (zTexSize - z - 1) * xTexSize;
	    } else {
	    	rowIndex = z * xTexSize;
	    }
	    byte[] vRow;
	    if (z < zDim) {
		vRow = vol.fileData[z][yValue];
	    } else {
		vRow = emptyByteRow;
	    }
	    System.arraycopy(vRow, 0, byteData, rowIndex, xDim);
	}
    }

    // this routine loads values for constant xValue, into byteData in y,z
    // order (y changes fastest)
    void loadXIntensity(int xValue, byte[] byteData)  {
	for (int z=0; z < zDim; z++){
	    int rowIndex;
	    if (tFlip) {
	    	rowIndex = (zTexSize - z - 1) * yTexSize;
	    } else {
	    	rowIndex = z * yTexSize;
	    }
	    for (int y=0; y < yDim; y++){
		byte value;
		value = vol.fileData[z][y][xValue];
		int tIndex = rowIndex + y;
		try {
		byteData[tIndex] = value;
		} catch (ArrayIndexOutOfBoundsException e) {
		    InterfaceSession.log("tIndex = " + tIndex +
			" byteData.length = " + byteData.length);
		    InterfaceSession.log("rowIndex =  " + rowIndex);
		    InterfaceSession.log("zTexSize =  " + zTexSize);
		    InterfaceSession.log("xDim =  " + xDim);
		    InterfaceSession.log("z =  " + z + " y = " + y);
		    System.exit(0);
		}
	    }
	}
    }

    // load byteData with the Luminance/Alpha values-- in this case, the same
    // value repeated twice for each value in the array.

    void loadZLumAlpha(int zValue, byte[] byteData) {
	for (int y=0; y < yDim; y++){
	    byte[] vRow = vol.fileData[zValue][y];
	    int rowIndex;
	    if (tFlip) {
		rowIndex = (yTexSize - y - 1) * xTexSize * 2;
	    } else {
		rowIndex = y * xTexSize * 2;
	    }
	    for (int x=0; x < xDim; x++){
		byte value = vRow[x];
		int tIndex = (rowIndex + x) * 2;
		byteData[tIndex] = value;
		byteData[tIndex+1] = value;
	    }
	}
    }

    // this routine loads values for constant yValue, the texture map is
    // stored in x,z format (x changes fastest)
    void loadYLumAlpha(int yValue, byte[] byteData)  {
	for (int z=0; z < zTexSize; z++){
	    byte[] vRow;
	    if (z < zDim) {
		vRow = vol.fileData[z][yValue];
	    } else {
		vRow = emptyByteRow;
	    }
	    int rowIndex;
	    if (tFlip) {
		rowIndex = (zTexSize - z - 1) * xTexSize * 2;
	    } else {
		rowIndex = z * xTexSize * 2;
	    }
	    for (int x=0; x < xDim; x++){
		byte value = vRow[x];
		int tIndex = (rowIndex + x) * 2;
		byteData[tIndex] = value;
		byteData[tIndex+1] = value;
	    }
	}
    }

    // this routine loads values for constant xValue, into byteData in y,z
    // order (y changes fastest)
    void loadXLumAlpha(int xValue, byte[] byteData)  {
	for (int z=0; z < zTexSize; z++){
	    int rowIndex;
	    if (tFlip) {
	    	rowIndex = (zTexSize - z - 1) * xTexSize * 2;
	    } else {
	    	rowIndex = z  * xTexSize * 2;
	    }
	    for (int y=0; y < yDim; y++){
		byte value;
		if (z < zDim) {
		    value = vol.fileData[z][y][xValue];
		} else {
		    value = 0;
		}
		int tIndex = (rowIndex + y) * 2;
		byteData[tIndex] = value;
		byteData[tIndex+1] = value;
	    }
	}
    }

    // this routine loads values for constant zValue, into byteData in x,y
    // order (x changes fastest)
    void loadZRGBA(int zValue, int[] intData, Colormap cmap) {
	loadZRGBA(zValue, intData, 0, cmap);
    }

    // this routine loads values for constant zValue, into byteData in x,y
    // order (x changes fastest)
    void loadZRGBA(int zValue, int[] intData, int intOffset, Colormap cmap) {

	for (int y=0; y < yDim; y++){
	    byte[] vRow = vol.fileData[zValue][y];
	    int rowIndex;
	    if (tFlip) {
	    	rowIndex = (yTexSize - y - 1) * xTexSize;
	    } else {
	    	rowIndex = y * xTexSize;
	    }
	    for (int x=0; x < xDim; x++){
		byte value = vRow[x];
		int mapIndex = value;
		if (mapIndex < 0) {
		   mapIndex += 256;
		}
		int tIndex = intOffset + rowIndex + x;
		intData[tIndex] = cmap.colorMapping[mapIndex];
	    }
	}
    }

    // this routine loads values for constant yValue, into byteData in x,y
    // order (x changes fastest)
    void loadYRGBA(int yValue, int[] intData, Colormap cmap) {

	for (int z=0; z < zTexSize; z++){
	    byte[] vRow;
	    if (z < zDim) {
		vRow = vol.fileData[z][yValue];
	    } else {
		vRow = emptyByteRow;
	    }
	    int rowIndex;
	    if (tFlip) {
	    	rowIndex = (zTexSize - z - 1) * xTexSize;
	    } else {
	    	rowIndex = z * xTexSize;
	    }
	    for (int x=0; x < xDim; x++){
		byte value = vRow[x];
		int mapIndex = value;
		if (mapIndex < 0) {
		   mapIndex += 256;
		}
		int tIndex = rowIndex + x;
		intData[tIndex] = cmap.colorMapping[mapIndex];
	    }
	}
    }


    // this routine loads values for constant xValue, into byteData in y,z
    // order (y changes fastest)
    void loadXRGBA(int xValue, int[] intData, Colormap cmap) {

	for (int z=0; z < zTexSize; z++){
	    int rowIndex;
	    if (tFlip) {
	    	rowIndex = (zTexSize - z - 1) * yTexSize;
	    } else {
	    	rowIndex = z * yTexSize;
	    }
	    for (int y=0; y < yDim; y++){
		byte value;
		if (z < zDim) {
		    value = vol.fileData[z][y][xValue];
		} else {
		    value = 0;
		}
		int mapIndex = value;
		if (mapIndex < 0) {
		   mapIndex += 256;
		}
		int tIndex = rowIndex + y;
		intData[tIndex] = cmap.colorMapping[mapIndex];
	    }
	}
    }
}