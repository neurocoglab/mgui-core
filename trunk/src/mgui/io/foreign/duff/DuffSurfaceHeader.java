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

package mgui.io.foreign.duff;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;

import mgui.io.util.ByteOrderedFile;


public class DuffSurfaceHeader {

	public ByteOrder byte_order = ByteOrder.BIG_ENDIAN;
	
	//component					  	  bits		total		descr
	public char[] magic;			//8*8		128			...
	public byte[] version;			//4*8		192			...
	public int hdr_size;			//32		224			offset of first data element
	public int md_offset;			//32		256			start of meta data
	public int pd_offset;			//32		288			start of patient data header
	public int n_faces;				//32		320			# faces
	public int n_nodes;				//32		352			# nodes
	public int n_strips;			//32		384			# strips
	public int strip_size;			//32		416			size of strips
	public int normals;				//32		448			start of normal data (0 if not in file)
	public int uv_start;			//32		480			start of surface parameters (0 if not in file)
	public int vc_offset;			//32		512			start of vertex colour data
	public int precision;			//8			520			32 (float) or 64 (double)
	public byte[] pad;				//8*3		544			padding
	public double[][] orientation;	//64*16		1568		affine transformation matrix
	
	public boolean isSet = false;
	
	public DuffSurfaceHeader(File file){
		try{
			isSet = readHeader(file);
		} catch (IOException e){
			e.printStackTrace();
			}
	}
	
	public boolean readHeader (File file) throws IOException {
		
		if (file == null || !file.exists()) throw new IOException ("DuffSurfaceHeader: invalid input file.");
		
		ByteOrderedFile in = new ByteOrderedFile(file, "r");
		
		//test for byte order
		magic = new char[8];
		magic[0] = (char)in.readByte();
		
		if (magic[0] == 'F'){
			byte_order = ByteOrder.LITTLE_ENDIAN;
			in.setByteOrder(byte_order);
			}
		
		for (int i = 1; i < 8; i++)
			magic[i] = (char)in.readByte();
		version = new byte[4];
		for (int i = 0; i < 4; i++)
			version[i] = in.readByte();
		hdr_size = in.readOrderedInt();
		md_offset = in.readOrderedInt();
		pd_offset = in.readOrderedInt();
		n_faces = in.readOrderedInt();
		n_nodes = in.readOrderedInt();
		n_strips = in.readOrderedInt();
		strip_size = in.readOrderedInt();
		normals = in.readOrderedInt();
		uv_start = in.readOrderedInt();
		vc_offset = in.readOrderedInt();
		precision = in.readByte();
		pad = new byte[3];
		for (int i = 0; i < 3; i++)
			pad[i] = in.readByte();
		orientation = new double[4][4];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				orientation[i][j] = in.readOrderedDouble();
		
		in.close();
		return true;
	}
	
}