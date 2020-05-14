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

package mgui.io.foreign.vol;

/*********************************
 * Original class: VolFile
 * 
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

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.domestic.shapes.ShapeIOException;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.domestic.shapes.VolumeInputOptions;


public class VolFileLoader extends VolumeFileLoader {

    int xDim = 0;
    int dummyint = 0;
    byte dummybyte = 0;
    short dummyshort = 0;
    int yDim = 0;
    int zDim = 0;
    int tDim = 0;
    float xSpace = 0;
    float ySpace = 0;
    float zSpace = 0;
    int dataOffset = 0;
    int minVal = 0;
    int maxVal = 0;
    int bytesPerVoxel = 0;
    byte[] id = new byte[64];
    int pos = 0;
    DataInputStream file = null;
    byte[][][] fileData;

    // This windowing stuff is for the 16->8 mapping for the CT data
    // TODO: make this another attribute
    double windowCenter = 880.0;
    double windowWidth = 930.0;
    double windowBase = windowCenter - windowWidth / 2;
    
    /**
     * Creates a VolFile from a URL.
     * @param voldat  The URL for a .vol file holding the volume
     */
    public VolFileLoader(URL voldat)  {
    	setURL(voldat);
    }
    
    @Override
	public VolumeMetadata getVolumeMetadata(){
		return null;
	}
    
    public void loadData() {
    	pos = 0;
    	try{
   		file = new DataInputStream(dataURL.openStream());
		byte[] magicBuffer = new byte[4];
		file.read(magicBuffer, 0, 4);
		pos += 4;
		String magic = new String(magicBuffer, 0, 4); 
		if (!magic.equals("vol3")) {
		    InterfaceSession.log("file specified is not a .vol file" +
			" header begins with " + magic);
		    System.exit(0);
			}
    	
		xDim = file.readInt();
		pos += 4;
		yDim = file.readInt();
		pos += 4;
		zDim = file.readInt();
		pos += 4;
		xSpace = file.readFloat();
		pos += 4;
		ySpace = file.readFloat();
		pos += 4;
		zSpace = file.readFloat();
		pos += 4;
		dataOffset = file.readInt();
		pos += 4;
		minVal = file.readShort();
		pos += 2;
		maxVal = file.readShort();
		pos += 2;
		bytesPerVoxel = file.readByte();
		pos++ ;
		/* Following is padding to avoid need to seek */
		dummybyte = file.readByte();
		pos ++ ;
		for (int i = 0; i < 13; i++) {
			dummyshort = file.readShort();
		pos += 2;
		}
		file.read(id, 0, 64);
		pos += 64;
	
		fileData = new byte[zDim][yDim][xDim];
		if (pos != dataOffset) System.out.print("VolFile: Pointer Mismatch");
		if (bytesPerVoxel == 1) {
		    System.out.print("Reading data...");
		    for (int z = 0; z < zDim; z++) {
			for (int y = 0; y < yDim; y++) {
			    int vIndex = (z * xDim * yDim + y * xDim);
			    byte[] dataRow = fileData[z][y];
			    file.readFully(dataRow, 0, xDim);
			}
		    }
		    InterfaceSession.log("done");
		} else {
		    byte[] buffer = new byte[xDim * 2];
		    System.out.print("Reading and windowing data");
		    for (int z = 0; z < zDim; z++) {
			for (int y = 0; y < yDim; y++) {
			    int vIndex = (z * xDim * yDim + y * xDim) * 2;
			    byte[] dataRow = fileData[z][y];
			    file.readFully(buffer, 0, xDim * 2);
			    for (int x = 0; x < xDim; x++) {
				int index = x * 2;
				// Map the pair of bytes into a short and then window 
				// into a byte
				int low = buffer[index+1];
				if (low < 0) {
				    low += 256;
				}
				int high = buffer[index];
				if (high < 0) {
				    high += 256;
				}
				short fileValue =  (short)((high << 8)  + (low << 0));
				fileValue -= minVal;
				double scaleValue = (fileValue-windowBase)/windowWidth;
				if (scaleValue > 1.0) {
				    scaleValue = 1.0;
				}
				if (scaleValue < 0.0) {
				    scaleValue = 0.0;
				}
				dataRow[x] = (byte)(scaleValue * 255);
			    }
			}
			System.out.print(".");
		    }
		    InterfaceSession.log("done");
		}
    	} catch(IOException fnf) {
		     InterfaceSession.log(fnf.getMessage());
		    }
    }

    /**
     * Returns the ID string for the volume file
     */
    public String getId() {
	return new String(id);
    }

    /**
     * Gets the X dimension of the volume
     */
    public int getXDim() {
	return xDim;
    }

    /**
     * Gets the Y dimension of the volume
     */
    public int getYDim() {
	return yDim;
    }

    /**
     * Gets the Z dimension of the volume
     */
    public int getZDim() {
	return zDim;
    }
    
    public byte[][][] getFileData(){
    	return fileData;
    }
    
    /******************************************
     * Fills a grid with this file's data. Data with be truncated if grid dimensions
     * are too small, and will not set values for extra elements if grid is too large.
     * TODO set extra elements to 0
     * TODO allow scaling of data to fit grid dimensions
     * @param grid Grid3D object to fill
     */
    @Override
	public boolean setVolume3D(Volume3DInt volume, String column, int v, VolumeInputOptions options, ProgressUpdater progress){
    	
    	loadData();
    	
    	if (fileData == null)
    		return false;
    	
    
    	releaseData();
    	
    	return false;
    }
    
    protected void releaseData(){
    	fileData = null;
    }
    
    private static int byteToInt(byte b){
		int val = b;
		if (val < 0) val += 256;
		return val;
	}
    
    @Override
	public Volume3DInt getVolume3D(){
    	return getVolume3D(0);
    }
    
    /************************
     * Return a Grid3D object representing the geometrical boundary of this volume
     * @return Grid3D object
     */
    @Override
	public Volume3DInt getVolume3D(int v){
    	//set base point and basis axes
    	//temp*** is origin and ortho
    	Point3f basePt = new Point3f(0,0,0);
    	Vector3f sAxis = new Vector3f(1,0,0);
    	Vector3f tAxis = new Vector3f(0,1,0);
    	Vector3f rAxis = new Vector3f(0,0,1);
    	Box3D box = new Box3D(basePt, sAxis, tAxis, rAxis);

    	box.setSDim(xDim * xSpace);
    	box.setTDim(yDim * ySpace);
    	box.setRDim(zDim * zSpace);
    	
    	return null;
    	//return new Grid3D(fileData, box);
    }

	@Override
	protected boolean setVolume3DBlocking(Volume3DInt volume, String column,
			int v, VolumeInputOptions options, ProgressUpdater progress)
			throws ShapeIOException {
		// TODO Auto-generated method stub
		return false;
	}

		
}