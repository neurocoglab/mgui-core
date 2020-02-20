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

package mgui.interfaces.unix;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Enumeration;

import mgui.interfaces.InterfaceSession;
import mgui.io.util.IoFunctions;

public class UnixFunctions {

	public static class split {
		
		public static void main(String args[]){
			
			for (int i = 0; i < args.length; i++)
				if (args[i].equals("-b")){
					split_binary(args);
					return;
					}
			
			//TODO: split text file
			
			
		}
		
		static void split_binary(String[] args){
			
			int n = 0;
			int suffix_size = 2;
			
			String input_file = null, prefix = "x";
			
			for (int i = 0; i < args.length; i++){
				if (args[i].equals("-n"))
					n = Integer.parseInt(args[i + 1]);
				if (args[i].equals("-a"))
					suffix_size = Integer.parseInt(args[i + 1]);
				if (args[i].equals("-inputfile"))
					input_file = args[i + 1];
				if (args[i].equals("-prefix"))
					prefix = args[i + 1];
				}
			
			if (n == 0){
				InterfaceSession.log("split: invalid n..");
				return;
				}
			
			if (input_file == null){
				InterfaceSession.log("split: input file must be specified as -inputfile <filepath>");
				return;
				}
			
			File input = new File(input_file);
			if (input_file == null){
				InterfaceSession.log("split: cannot find input file '" + input_file + "'.");
				return;
				}
			
			String ext = "";
			if (input_file.contains("."))
				ext = input_file.substring(input_file.lastIndexOf("."));
			File dir = input.getParentFile();
			
			int i = 1;
			int bytes_read = 0;
			try{
				
				DataInputStream input_stream = new DataInputStream(new FileInputStream(input));
				
				do {
					//split it
					File output = new File(getNextFilename(dir.getAbsolutePath() + prefix, ext, suffix_size, i++));
				
					if ((output.exists() && !output.delete()) && !output.createNewFile()){
						InterfaceSession.log("Could not create file '" + output.getAbsolutePath() + "'.");
						return;
						}
					
					InterfaceSession.log("Writing " + n + " bytes to '" + output.getAbsolutePath() + "'..");
					
					byte[] b = new byte[n];
					bytes_read = input_stream.read(b);
				
					DataOutputStream output_stream = new DataOutputStream(new FileOutputStream(output));
					output_stream.write(b);
					output_stream.close();
			
				} while (bytes_read == n);
			
				input_stream.close();
				
			}catch (IOException e){
				e.printStackTrace();
				return;
				}
		}
		
	}
	
	private static String getNextFilename(String prefix, String ext, int size, int v){
		
		String s = Integer.toString(v);
		if (s.length() > size) size = s.length();
		
		while (s.length() < size)
			s = "0" + s;
		
		return prefix + s + ext;
		
	}
	
	public static class cp {
		
		public static void main(String args[]){
			
			File source = new File(args[0]);
			File dest = new File(args[1]);
			
			try{
				IoFunctions.copyFile(source, dest);
			}catch (Exception e){
				InterfaceSession.log("unix.cp: Exception");
				//e.printStackTrace();
				//LoggedException.logExceptionWarning(e, Thread.currentThread().getName());
				}
			
			//InterfaceSession.log("???");
			return;
		}
		
	}
	
	public static class cat {
		
		public static void main(String args[]){
			
			//args[0] is output file
			//args[1 to n] are files to concatenate
			try{
				int skip = -1;
				//skip -outputfile argument if it exists (necessary for Parameter usage)
				File output_file = null;
				for (int i = 0; i < args.length; i++){
					if (args[i].equals("-outputfile")){
						output_file = new File(args[i + 1]);
						skip = i;
						}
					}
				
				ArrayList<File> file_list = new ArrayList<File>(); 
				for (int i = 0; i < args.length; i++){
					if (i < skip || i > skip + 1){
						//InterfaceSession.log("cat: input(" + i + "): " + args[i]);
						String file = args[i];
						
						File dir = new File(file).getParentFile();
						String filter = "*.*";
						if (file.lastIndexOf(File.separator) >= 0)
							filter = file.substring(file.lastIndexOf(File.separator) + 1);
						
						if (dir != null && dir.exists()){
							File[] files = IoFunctions.getFiles(dir.getAbsolutePath(), filter);
							for (int j = 0; j < files.length; j++){
								if (output_file == null || !files[j].getAbsolutePath().equals(output_file.getAbsolutePath()))
									file_list.add(files[j]);
								}
							}
						}
					}
					
				//now concatenate these files
				//write all files sequentially to output
				int buff_size = 1024*1024*24;
				DataOutputStream test = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output_file), buff_size));
				long pos = 0;
				//
				for (int i = 0; i < file_list.size(); i++){
					InterfaceSession.log("cat file: " + file_list.get(i).getAbsolutePath());
					FileChannel source = new FileInputStream(file_list.get(i)).getChannel();
					long size = source.size();
					ByteBuffer buffer = source.map(MapMode.READ_ONLY, 0, size);
					int p = 0;
					int buff = buff_size;
					while (buff > 0){
						p += buff;
						byte[] b_array = new byte[buff];
						buffer.get(b_array);
						test.write(b_array, 0, buff);
						buff = (int)Math.min(buff, size - p);
						}
					//InterfaceSession.log(p + " bytes copied.. (size = " + size + ")");
					source.close();
					}
				
				test.close();
			
			}catch (IOException e){
				e.printStackTrace();
				}
			
		}
		
		
	}
	
	
	private static class InputStreamEnumeration implements Enumeration<InputStream>{

		ArrayList<InputStream> streams = new ArrayList<InputStream>();
		int index = 0;
		
		public InputStreamEnumeration(ArrayList<File> files){
			
			try{
				for (int i = 0; i < files.size(); i++)
					streams.add(new FileInputStream(files.get(i)));
			}catch (Exception e){
				e.printStackTrace();
				return;
				}
			
			index = 0;
		}
	
		public boolean hasMoreElements() {
			return index < streams.size();
		}

		public InputStream nextElement() {
			return streams.get(index++);
		}
		
	}
	
	
}