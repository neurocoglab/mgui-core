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

package mgui.io.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.Utility;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;

/*******************************************************************
 * Utility class which provides functions for common I/O operations.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class IoFunctions extends Utility {

	public static enum FileType{
		Zip,
		Gzip;
	}
	
	//switch the order of the bytes in b
	public static byte[] switchOrder(byte[] b, int size){
		byte[] r = new byte[b.length];
		for (int i = 0; i < b.length; i++)
			for (int pos = 0; pos < size; pos++)
				r[i + pos] = b[i + size - pos - 1];
		return r;
	}
	
	/******************************
	 * Returns this system's temporary directory
	 * 
	 * @return
	 */
	public static File getTempDir(){
		
		return new File(System.getProperty("java.io.tmpdir"));
		
	}
	
	/******************************
	 * Extracts the tar archive {@code tar_file} into the directory {@code target_dir}.
	 * 
	 * @param tar_file
	 * @param target_dir
	 * @return
	 */
	public static boolean extractTar(File tar_file, File target_dir){
		
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
		
		try{
			archiver.extract(tar_file, target_dir);
		}catch (IOException ex){
			InterfaceSession.handleException(ex, LoggingType.Errors);
			return false;
			}
		
		return true;
	}
	
	/*******************************
	 * Returns the lowest directory in this path; <code>path</code> must specify a directory.
	 * 
	 * @param path
	 * @return
	 */
	public static String getDirectory(String path){
		if (!path.contains(File.separator)) return path;
		return path.substring(path.lastIndexOf(File.separator) + 1);
	}
	
	/********************************
	 * Returns the path to the parent directory of the file or directory specified by <code>path</code>.
	 * 
	 * @param path
	 * @return
	 */
	public static String getParentPath(String path){
		if (!path.contains(File.separator)) return "";
		return path.substring(0, path.lastIndexOf(File.separator));
	}
	
	/*******************************
	 * Parses {@code file} and creates any non-existent directories in its path
	 * 
	 * @param file
	 */
	public static boolean createDirs(File file) throws IOException{
		
		//ArrayList<String> dirs = getDirs(file); // file.getAbsolutePath().split(File.separator);
		String[] dirs = file.getAbsolutePath().split(Pattern.quote(File.separator));
		
		String path = dirs[0];
		for (int i = 1; i < dirs.length; i++){
			path = path + File.separator;
			path = path + dirs[i];
			File dir = new File(path);
			if (!dir.exists())
				if (!dir.mkdir()){
					InterfaceSession.log("IOfunctions.createDirs: Could not create directory '" + dir.getAbsolutePath() + "'.", 
										 LoggingType.Errors);
					return false;
					}
			}
		
		return true;
	}
	
	/*******************************************
	 * Returns all directories, in top-down order, found in path
	 * 
	 * @param path
	 * @return
	 */
	public static ArrayList<String> getDirs(File path){
		
		ArrayList<String> dirs = new ArrayList<String>();
		File parent = path.getParentFile();
		while (parent != null && parent.getName().length() > 0){
			dirs.add(parent.getName());
			parent = parent.getParentFile();
			}
		
		ArrayList<String> dirs2 = new ArrayList<String>(dirs.size()+1);
		for (int i = dirs.size() - 1; i >-1; i--)
			dirs2.add(dirs.get(i));
		
		return dirs2;
		
	}
	
//	public static File unzipArchiveToFolder(File archive, File target) throws IOException {
//		
//		int BUFFER = 2048;
//		String archive_str = archive.getAbsolutePath();
//		FileType type = getFileType(archive);
//		String path = getParentPath(archive.getAbsolutePath());
//		if (target != null)
//			path = target.getAbsolutePath();
//		
//		if (type == null) return null;
//		
//		switch (type){
//		
//			case Gzip:
//				
//				String new_file = archive.getAbsolutePath();
//				if (target != null)
//					new_file = target.getAbsolutePath() + File.separator + archive.getName();
//				new_file = new_file.substring(0, new_file.lastIndexOf(".gz"));
//				
//				fis = new FileInputStream(archive_str);
//				GZIPInputStream gs = new GZIPInputStream(fis);
//				
//				gs.
//				
//				
//				break;
//			
//			default:
//				
//				InterfaceSession.log("Unzip type not recognised: " + type.toString(), LoggingType.Errors);
//			
//			
//		}
//		
//		
//		
//	}
	
	/********************************
	 * Unzips the contents of <code>archive</code> to the current folder, and returns the path to the folder.
	 * <code>archive</code> must be either a zip or a gzip archive, as specified by its extension.
	 * 
	 * @param archive
	 * @return the resulting unzipped archive (parent path or file, depending on zip type)
	 */
	public static File unzipArchiveToFile(File archive) throws IOException{
		return unzipArchiveToFile(archive, null);
	}
	
	/********************************
	 * 
	 * Unzips the contents of <code>archive</code> to a new file, and returns the path to the file.
	 * <code>archive</code> must be either a zip or a gzip archive, as specified by its extension.
	 * 
	 * @param archive
	 * @param target 			The target directory; if {@code null}, the archive directory is used.
	 * @return the resulting unzipped archive (parent path or file, depending on zip type)
	 */
	public static File unzipArchiveToFile(File archive, File target) throws IOException{
		
		int BUFFER = 2048;
		String archive_str = archive.getAbsolutePath();
		FileType type = getFileType(archive);
		String path = getParentPath(archive.getAbsolutePath());
		if (target != null)
			path = target.getAbsolutePath();
		
		if (type == null) return null;
		
		BufferedOutputStream dest = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		switch (type){
		
			case Gzip:
				
				//strip .gz extension
				String new_file = archive.getAbsolutePath();
				if (target != null)
					new_file = target.getAbsolutePath() + File.separator + archive.getName();
				new_file = new_file.substring(0, new_file.lastIndexOf(".gz"));
				
				fis = new FileInputStream(archive_str);
				GZIPInputStream gs = new GZIPInputStream(fis);
				fos = new FileOutputStream(new_file);
	            dest = new BufferedOutputStream(fos, BUFFER);
				
	            int len;
				byte[] buf = new byte[1024];
				while ((len = gs.read(buf)) > 0) {
					dest.write(buf, 0, len);
			        }

				dest.flush();
				dest.close();
				
				return new File(new_file);
				
			case Zip:
				
				fis = new FileInputStream(archive_str);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
				ZipEntry entry;
				while((entry = zis.getNextEntry()) != null) {
					String new_entry = path + File.separator + entry.getName();
					InterfaceSession.log("Extracting: " + new_entry);
		            int count;
		            byte data[] = new byte[BUFFER];
		            // write the files to the disk
		            fos = new FileOutputStream(new_entry);
		            dest = new BufferedOutputStream(fos, BUFFER);
		            while ((count = zis.read(data, 0, BUFFER)) != -1) {
		            	dest.write(data, 0, count);
		            	}
		            dest.flush();
		            dest.close();
				}
		        zis.close();
				
				return new File(path);
		
			}
		
		return null;
		
	}
	
	/********************************
	 * Unzips the contents of <code>archive</code> to a new folder, and returns the path to the folder.
	 * <code>archive</code> must be either a zip or a gzip archive, as specified by its extension.
	 * 
	 * @param archive
	 * @param target 			The target file; if {@code null}, the archive name, minus ".gz", is used.
	 * @return the resulting unzipped archive (parent path or file, depending on zip type)
	 */
	public static File unzipFile(File archive, File target) throws IOException{
		
		int BUFFER = 2048;
		String archive_str = archive.getAbsolutePath();
		FileType type = getFileType(archive);
		String path = getParentPath(archive.getAbsolutePath());
		
		if (type == null) return null;
		
		BufferedOutputStream dest = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		switch (type){
		
			case Gzip:
				
				//strip .gz extension
				String new_file = archive.getAbsolutePath();
				if (target != null)
					new_file = archive.getParent() + File.separator + target.getName();
				else
					new_file = archive.getAbsolutePath().substring(0, new_file.lastIndexOf(".gz"));
				
				fis = new FileInputStream(archive_str);
				GZIPInputStream gs = new GZIPInputStream(fis);
				fos = new FileOutputStream(new_file);
	            dest = new BufferedOutputStream(fos, BUFFER);
				
	            int len;
				byte[] buf = new byte[1024];
				while ((len = gs.read(buf)) > 0) {
					dest.write(buf, 0, len);
			        }

				dest.flush();
				dest.close();
				
				return new File(new_file);
				
			case Zip:
				
				fis = new FileInputStream(archive_str);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
				ZipEntry entry;
				while((entry = zis.getNextEntry()) != null) {
					String new_entry = path + File.separator + entry.getName();
					InterfaceSession.log("Extracting: " + new_entry);
		            int count;
		            byte data[] = new byte[BUFFER];
		            // write the files to the disk
		            fos = new FileOutputStream(new_entry);
		            dest = new BufferedOutputStream(fos, BUFFER);
		            while ((count = zis.read(data, 0, BUFFER)) != -1) {
		            	dest.write(data, 0, count);
		            	}
		            dest.flush();
		            dest.close();
				}
		        zis.close();
				
				return new File(path);
		
			}
		
		return null;
		
	}
	
	/**************************
	 * Compresses <code>file</code> using gzip compression.
	 * 
	 * @param file_in File to compress
	 * @param file_out File for output
	 * 
	 * @throws IOException If the compression operation failed
	 */
	public static void gzipFile(File file_in, File file_out) throws IOException{
		
		// Create the GZIP output stream
        GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(file_out));
    
        // Open the input file
        FileInputStream in = new FileInputStream(file_in);
    
        // Transfer bytes from the input file to the GZIP output stream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        	}
        in.close();
    
        // Complete the GZIP file
        out.finish();
        out.close();
        
	}
	
	/**************************
	 * 
	 * Compresses <code>file</code> using tar/gzip compression.
	 * 
	 * @param zip_files 	List of paths for files to add to archive
	 * @param file_out 		File for output
	 * @param root_dir 		Root directory, will be removed from item names
	 * 
	 * @throws IOException If the compression operation failed
	 */
	public static void gzipAndTarFiles(List<File> zip_files, File file_out) throws IOException{
		gzipAndTarFiles(zip_files, file_out, null);
	}
	
	/**************************
	 * 
	 * Compresses <code>file</code> using tar/gzip compression.
	 * 
	 * @param zip_files 	List of paths for files to add to archive
	 * @param file_out 		File for output
	 * 
	 * @throws IOException If the compression operation failed
	 */
	public static void gzipAndTarFiles(List<File> zip_files, File file_out, String root_dir) throws IOException{
		
		OutputStream fo = Files.newOutputStream(Paths.get(file_out.toURI()));
				
		OutputStream gzo = new GzipCompressorOutputStream(fo);
		ArchiveOutputStream o = new TarArchiveOutputStream(gzo);
		
		for (File file : zip_files) {
			// maybe skip directories for formats like AR that don't store directories
			String name = file.getAbsolutePath();
			
			// Remove root dir if specified, to make name relative
			if (root_dir != null) {
				name = name.replace(root_dir + "/", "");
				}
			ArchiveEntry entry = o.createArchiveEntry(file, name);
	        
			o.putArchiveEntry(entry);
			if (file.isFile()) {
				
				InputStream i = Files.newInputStream(file.toPath());
				IOUtils.copy(i, o);
				
				InterfaceSession.log(file_out.getName() + ": Added " + entry.getName(), LoggingType.Debug);
				
	        } else {
	        	throw new IOException( "File " + file.getAbsolutePath() + " not found." );
	        	}
			
	        o.closeArchiveEntry();
	    	}
		
	    o.finish();
	    o.close();
	    
	}
	
	/*****************************
	 * 
	 * Decompresses the contents of the tar.gz archive in the file {@code archive} to {@code target_dir}.
	 * 
	 * @param archive
	 * @param target_dir
	 * 
	 * @throws IOException
	 */
	public static void gunzipAndTarFiles(File archive, File target_dir) throws IOException {
		
		//int len = 1024;
		
		InputStream fi = Files.newInputStream(Paths.get(archive.toURI()));
		InputStream bi = new BufferedInputStream(fi);
		InputStream gzi = new GzipCompressorInputStream(bi);
		TarArchiveInputStream ain = new TarArchiveInputStream(gzi);
		
		TarArchiveEntry entry = ain.getNextTarEntry();
		
		while (entry != null) {
			
			File file_out = new File(target_dir.getAbsolutePath() + File.separator + entry.getName());
			
			Path parent = Paths.get(file_out.getParent());
			if (!Files.isDirectory(parent))
				Files.createDirectories(parent);
			
			OutputStream out = Files.newOutputStream(Paths.get(file_out.toURI()));
			
			int len = (int)entry.getSize();
			byte[] buffer = new byte[len];
			
			ain.read(buffer, 0, len);
			out.write(buffer, 0, len);

			out.close();
			
			InterfaceSession.log(archive.getName() + ": Extracted " + entry.getName(), LoggingType.Debug);
			
			entry = ain.getNextTarEntry();
			}
		
		ain.close();
		
		
	}
	
	
	/*************************************************************
	 * Decompresses a gzipped byte array.
	 * 
	 * @param contentBytes
	 * @return
	 */
	public static byte[] decompressGZipped(byte[] contentBytes) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(contentBytes)), out);
        
        return out.toByteArray();
    }
	
	/*************************************************************
	 * Compresses a gzipped byte array.
	 * 
	 * @param contentBytes
	 * @return
	 */
	public static byte[] compressGZipped(byte[] contentBytes) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream out_stream = new GZIPOutputStream(out);
        out_stream.write(contentBytes);
        
        return out.toByteArray();
    }
	
	/*************************************************************
	 * Decompresses a zipped byte array.
	 * 
	 * @param contentBytes
	 * @return
	 */
	public static byte[] decompressZipped(byte[] contentBytes) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(new InflaterInputStream(new ByteArrayInputStream(contentBytes)), out);
        
        return out.toByteArray();
    }
	
	/*************************************************************
	 * Compresses a byte array.
	 * 
	 * @param contentBytes
	 * @return
	 */
	public static byte[] compressZipped(byte[] contentBytes) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(new DeflaterInputStream(new ByteArrayInputStream(contentBytes)), out);
        
        return out.toByteArray();
    }
	
	private static int readUShort(InputStream in) throws IOException {
		int b = readUByte(in);
		return ((int)readUByte(in) << 8) | b;
	}
	
	private static int readUByte(InputStream in) throws IOException {
		int b = in.read();
		if (b == -1) {
		    throw new EOFException();
		}
	        if (b < -1 || b > 255) {
	            // Report on this.in, not argument in; see read{Header, Trailer}.
	            throw new IOException();
	        }
		return b;
	}
	
	/*********************************************
	 * Converts a {@code float} value to four bytes.
	 * 
	 * @param f
	 * @return
	 */
	public static byte[] floatToBytes(float f){
		
		byte[] b = new byte[4];
		int data=Float.floatToIntBits(f);
		b[0]=(byte)(data>>>24);
		b[1]=(byte)(data>>>16);
		b[2]=(byte)(data>>>8);
		b[3]=(byte)(data>>>0);
		return b;
		
	}
	
	/*********************************************
	 * Converts an {@code int} value to four bytes.
	 * 
	 * @param f
	 * @return
	 */
	public static byte[] intToBytes(int i){
		
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(i);

		byte[] result = b.array();
		return result;
		
	}
	
	/*****************************************
	 * Returns a file for the parent, path combination; i.e, appending {@code path} to
	 * {@code parent}. 
	 * 
	 * @param parent
	 * @param path
	 * @return
	 */
	public static File fullFile(File parent, String path){
		String parent_path = parent.getAbsolutePath();
		File file = new File(parent_path + File.separator + path);
		return file;
	}
	
	static FileType getFileType(File file){
		String s = file.getAbsolutePath();
		
		if (s.endsWith(".gzip") ||
				s.endsWith(".gz"))
			return FileType.Gzip;
		
		if (s.endsWith(".zip"))
			return FileType.Zip;
		
		return null;
	}
	
	public static File getCurrentDir(){
		return new File(System.getProperty("user.dir"));
	}
	
	/******************************************************
	 * Returns the root directory of the current operating system. If the OS has multiple roots (drives),
	 * finds the one which is the ancestor of the user's current directory.
	 * 
	 * @return
	 */
	public static File getSystemRoot(){
		
		File dir = getCurrentDir();
		while(dir.getParentFile() != null)
			dir = dir.getParentFile();
		
		return dir;
	}
	
	public static File[] getFiles(final String dir, final String filter){
		File fileDir = new File(dir);
		return fileDir.listFiles(new WildcardFileFilter2(filter));
	}
	
	/*****************************************
	 * Counts the number of files contained in {@code directory} (recursive).
	 * 
	 * @param directory
	 * @return
	 */
	public static int getFileCount(File directory){
		return getFileCount(directory, true);
	}
	
	/*****************************************
	 * Counts the number of files contained in {@code directory}.
	 * 
	 * @param directory
	 * @param recurse 			If {@code true}, returns all files in subdirectories too
	 * @return
	 */
	public static int getFileCount(File directory, boolean recurse){
		if (!directory.isDirectory()) return 0;
		
		int count = 0;
		String[] list = directory.list();
		
		for (int i = 0; i < list.length; i++){
			File file = new File(directory.getAbsolutePath() + File.separator + list[i]);
			if (file.isFile())
				count++;
			if (file.isDirectory() && recurse)
				count += getFileCount(file, true);
			//System.out.print(".");
			}
		
		//System.out.println(directory.getAbsolutePath() + ": " + list.length + "; " + count);
		return count;
		
	}
	
	/*****************************************
	 * Copies the directory <code>dir</code> and all its contents to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>. Method is
	 * recursive, meaning all subdirectories will also be copied.
	 * 
	 * @param dir
	 * @param dest
	 * @throws IOException
	 */
	public static boolean copyDir(File dir, File dest) throws IOException {
		return copyDir(dir, dest, true, null);
	}
	
	/*****************************************
	 * Copies the directory <code>dir</code> and all its contents to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>.
	 * 
	 * @param dir
	 * @param dest
	 * @param recurse
	 * @throws IOException
	 */
	public static boolean copyDir(File dir, File dest, boolean recurse) throws IOException {
		return copyDir(dir, dest, recurse, null);
	}
	
	/*****************************************
	 * Copies the directory <code>dir</code> and all its contents to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>.
	 * 
	 * @param dir
	 * @param dest
	 * @param recurse
	 * @throws IOException
	 */
	public static boolean copyDir(File dir, File dest, boolean recurse, ProgressUpdater progress) throws IOException {
		
		//if (!dest.exists() && !dest.mkdir()) return false;
		
		if (!dir.exists() || !dir.isDirectory() || 
			!dest.isDirectory() || !dest.isDirectory())
			return false;
		
		//create new dir
		File new_dir = new File(dest.getAbsolutePath() + File.separator + dir.getName());
		new_dir.mkdir();
		
		//copy files and subdirectories
		String[] contents = dir.list();
		
		for (int i = 0; i < contents.length; i++){
			File this_file = new File(dir.getAbsolutePath() + File.separator + contents[i]);
			if (this_file.isDirectory()){
				if (recurse && !copyDir(this_file, new_dir, true))
					return false;
			}else{
				File new_file = new File(new_dir.getAbsolutePath() + File.separator + contents[i]);
				if (!copyFile(this_file, new_file))
					return false;
				if (progress != null){
					if (progress.isCancelled()){
						InterfaceSession.log("IoFunctions.copyDirContents: Cancelled by user.", LoggingType.Warnings);
						return false;
						}
					progress.iterate();
					}
				}
			}
		
		return true;
		
	}
	
	/*****************************************
	 * Copies the contents of directory <code>dir</code> (but not the directory itself) to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>. Method is
	 * recursive, meaning all subdirectories will also be copied.
	 * 
	 * @param dir
	 * @param dest
	 * @throws IOException
	 */
	public static boolean copyDirContents(File dir, File dest) throws IOException {
		return copyDirContents(dir, dest, true, null);
	}
	
	/*****************************************
	 * Copies the contents of directory <code>dir</code> (but not the directory itself) to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>.
	 * 
	 * @param dir
	 * @param dest
	 * @param recurse
	 * @throws IOException
	 */
	public static boolean copyDirContents(File dir, File dest, boolean recurse) throws IOException {
		return copyDirContents(dir, dest, recurse, null);
	}
	
	/*****************************************
	 * Copies the contents of directory <code>dir</code> (but not the directory itself) to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>.
	 * 
	 * @param dir
	 * @param dest
	 * @param recurse
	 * @throws IOException
	 */
	public static boolean copyDirContents(File dir, File dest, boolean recurse, ProgressUpdater progress) throws IOException {
		
		if (!dir.exists() || !dir.isDirectory() || 
			!dest.isDirectory() || !dest.isDirectory())
			return false;
		
		//copy files and subdirectories
		String[] contents = dir.list();
		
		for (int i = 0; i < contents.length; i++){
			File this_file = new File(dir.getAbsolutePath() + File.separator + contents[i]);
			if (this_file.isDirectory()){
				if (recurse){ 
					if (!copyDir(this_file, dest, true, progress))
						return false;
					}
			}else{
				File new_file = new File(dest.getAbsolutePath() + File.separator + contents[i]);
				if (!copyFile(this_file, new_file))
					return false;
				if (progress != null){
					if (progress.isCancelled()){
						InterfaceSession.log("IoFunctions.copyDirContents: Cancelled by user.", LoggingType.Warnings);
						return false;
						}
					progress.iterate();
					}
				}
			}
		
		return true;
		
	}
	
	/*****************************************
	 * Moves the directory <code>dir</code> and all its contents to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>. Method is
	 * recursive, meaning all subdirectories will also be copied.
	 * 
	 * @param dir
	 * @param dest
	 * @throws IOException
	 */
	public static boolean moveDir(File dir, File dest) throws IOException {
		return moveDir(dir, dest, true, null);
	}
	
	/*****************************************
	 * Move the directory <code>dir</code> and all its contents to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>.
	 * 
	 * @param dir
	 * @param dest
	 * @param recurse
	 * @throws IOException
	 */
	public static boolean moveDir(File dir, File dest, boolean recurse) throws IOException {
		return moveDir(dir, dest, recurse, null);
	}
	
	/*****************************************
	 * Move the directory <code>dir</code> and all its contents to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>.
	 * 
	 * @param dir
	 * @param dest
	 * @param recurse
	 * @throws IOException
	 */
	public static boolean moveDir(File dir, File dest, boolean recurse, ProgressUpdater progress) throws IOException {
		
		if (!copyDir(dir, dest, recurse, progress)) return false;
		
		String[] contents = dir.list();
				
        for (int i=0; i < contents.length; i++) {
        	String name = contents[i];
        	File file = new File(dir, name);
            if (file.isDirectory()){
            	if (recurse && !deleteDir(file))
            		return false;
            }else{
            	if (!file.delete())
            		return false;
            	}
            }
		
		return dir.delete();
		
	}
	
	/*****************************************
	 * Move the contents of directory <code>dir</code> (but not the directory itself) to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>. Method is
	 * recursive, meaning all subdirectories will also be copied.
	 * 
	 * @param dir
	 * @param dest
	 * @throws IOException
	 */
	public static boolean moveDirContents(File dir, File dest) throws IOException {
		return moveDirContents(dir, dest, true);
	}
	
	/*****************************************
	 * Move the contents of directory <code>dir</code> (but not the directory itself) to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>.
	 * 
	 * @param dir
	 * @param dest
	 * @param recurse
	 * @throws IOException
	 */
	public static boolean moveDirContents(File dir, File dest, boolean recurse) throws IOException {
		return moveDirContents(dir, dest, recurse, null);
	}
	
	/*****************************************
	 * Move the contents of directory <code>dir</code> (but not the directory itself) to the directory <code>dest</code>.
	 * Both must exist and be directories, otherwise method returns <code>false</code>.
	 * 
	 * @param dir
	 * @param dest
	 * @param recurse
	 * @throws IOException
	 */
	public static boolean moveDirContents(File dir, File dest, boolean recurse, ProgressUpdater progress) throws IOException {
		
		if (!copyDirContents(dir, dest, recurse, progress)) return false;
		
		String[] contents = dir.list();
				
        for (int i=0; i < contents.length; i++) {
        	String name = contents[i];
        	File file = new File(dir, name);
            if (file.isDirectory()){
            	if (recurse && !deleteDir(file))
            		return false;
            }else{
            	if (!file.delete())
            		return false;
            	}
            }
		
		return dir.delete();
		
	}
	
	public static ArrayList<String> getSubdirs(File dir, boolean recurse){
		return getSubdirs(dir, recurse, "");
	}
		
	static ArrayList<String> getSubdirs(File dir, boolean recurse, String current_path){
		
		ArrayList<String> subdirs = new ArrayList<String>();
		String path = current_path;
		if (path.length() > 0) path = path + File.separator;
		
		if (dir.isDirectory()){
			String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
            	File f = new File(dir.getAbsoluteFile() + File.separator + children[i]);
            	if (f.isDirectory()){
            		subdirs.add(path + children[i]);
            		if (recurse)
            			subdirs.addAll(getSubdirs(f, true, path + children[i]));
            		}
            	}
			}
		
		return subdirs;
		
	}
	
	public static javax.swing.filechooser.FileFilter getFileChooserFilter(ArrayList<String> extensions, String description){
		return new GenericFileFilter(extensions, description);
		
	}
	
	static class GenericFileFilter extends javax.swing.filechooser.FileFilter{

		ArrayList<String> extensions;
		String description;
		
		public GenericFileFilter(ArrayList<String> extensions, String description){
			this.extensions = extensions;
			this.description = description;
		}
		
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			String ext = f.getAbsolutePath();
			if (!ext.contains("."))
				ext = "";
			else
				ext = ext.substring(ext.lastIndexOf(".") + 1);
			for (String ex : extensions)
				if (ex.equals(ext)) return true;
			return false;
		}

		@Override
		public String getDescription() {
			return description;
		}
		
	}
	
	/*************************************************
	 * Deletes this directory and all of its subdirectories.
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
	                }
	            }
	        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

	
	static class WildcardFileFilter2 implements FileFilter
	{
	   Pattern pattern;
	   String description = "";
	 
	   WildcardFileFilter2(String search){
		   this(search, "");
	   }
	   
	   WildcardFileFilter2(String search, String description){
		   this.description = description;
		   StringBuffer buffer = new StringBuffer();
		   
		    char [] chars = search.toCharArray();
		 
		    for (int i = 0; i < chars.length; ++i)
		    {
		        if (chars[i] == '*')
		            buffer.append(".*");
		        else if (chars[i] == '?')
		            buffer.append(".");
		        else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1)
		            buffer.append('\\').append(chars[i]);
		        else
		            buffer.append(chars[i]);
		    }
	 
	      pattern = Pattern.compile(buffer.toString());
	   }
	 
	   public boolean accept(File file)
	   {
	      Matcher matcher = pattern.matcher(file.getName());
	 
	      return matcher.matches();
	   }
	}

	static void copy(final File src, File dst, final boolean overwrite) throws IOException {

		//long q = System.currentTimeMillis();
		if ( !src.isFile() || !src.exists() )
			throw new IOException("Source file '"+ src.getAbsolutePath() +"' not found!");
		
		if ( dst.exists() )
			if ( dst.isDirectory() )  // Directory? -> use source file name
				dst = new File(dst, src.getName());
			else if ( dst.isFile() ) {
				if ( !overwrite )
					throw new IOException ("Destination file '"+ dst.getAbsolutePath() +"' already exists!");
				}
			else
				throw new IOException ("Invalid destination object '"+ dst.getAbsolutePath() +"'!");
		
		File dstParent = dst.getParentFile();
		if ( !dstParent.exists())
			if ( !dstParent.mkdirs())
				throw new IOException("Failed to create directory "+ dstParent.getAbsolutePath());
		
		long fileSize = src.length();
		if ( fileSize > 20971520l ) { // for larger files (20Mb) use streams
		
			FileInputStream in = new FileInputStream(src);
			FileOutputStream out = new FileOutputStream(dst);
			
			try {
				int doneCnt = -1, bufSize = 32768;
				byte buf[] = new byte[bufSize];
				while ((doneCnt = in.read(buf, 0, bufSize)) >= 0)
				if ( doneCnt == 0 )  
					Thread.yield();
				else  
					out.write(buf, 0, doneCnt);
				out.flush();
			
			}finally {
				try { in.close(); } catch (IOException e) {throw e;}
				try { out.close(); } catch (IOException e) {throw e;}
				}
		
		}else {                      // smaller files, use channels
			FileInputStream  fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(dst);
			FileChannel      in = fis.getChannel(), out = fos.getChannel();
			try{
				long offs =0, doneCnt =0, copyCnt = Math.min(65536, fileSize);
				do {
					doneCnt = in.transferTo(offs, copyCnt, out);
					offs += doneCnt;
					fileSize -= doneCnt;
				} while ( fileSize > 0 );
			}finally {                 // cleanup
				try { in.close(); } catch (IOException e) {throw e;}
				try { out.close(); } catch (IOException e) {throw e;}
				try { fis.close(); } catch (IOException e) {throw e;}
				try { fos.close(); } catch (IOException e) {throw e;}
				}
		
			} // else
		
		//InterfaceSession.log(">>> "+ String.valueOf(src.length()/1024) +" Kb, "+ String.valueOf(System.currentTimeMillis()-q));
		
		
	} // copy

	
	/********************************************************
	 * Copies <code>sourceFile</code> to the path specified by <code>destFile</code>.
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static boolean copyFile(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists())
			destFile.createNewFile();
		
		copy(sourceFile, destFile, true);
		return true;
	}

	public static FilenameFilter getDirFilter(){
		return new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return new File(dir, name).isDirectory();
	        }
		};
	}
	
	public static javax.swing.filechooser.FileFilter getDirFilter2(){
		return new javax.swing.filechooser.FileFilter() {
	        public boolean accept(File dir) {
	            return dir.isDirectory();
	        }
	        public String getDescription() {
	            return "Directories";
	        }
		};
	}
	
	/******************************************************
	 * Matches {@code pattern} as a regular expression, returning only directories
	 * 
	 * @param pattern
	 * @return
	 */
	public static FilenameFilter getRegexDirFilter(final String pattern){
		return new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	        	String regex_is_a_headache = pattern.replaceAll("\\.", "\\\\.");
	        	regex_is_a_headache = pattern.replaceAll("\\*", ".*");
	            return new File(dir, name).isDirectory() &&
	            		name.matches(regex_is_a_headache);
	        }
		};
	}
	
	/******************************************************
	 * Contains {@code pattern}
	 * 
	 * @param pattern
	 * @return
	 */
	public static FilenameFilter getPatternFilter(final String pattern){
		return new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.contains(pattern);
	        }
		};
	}
	
	/******************************************************
	 * Matches {@code pattern} as a regular expression
	 * 
	 * @param pattern
	 * @return
	 */
	public static FilenameFilter getRegexFilter(final String pattern){
		return new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	        	String regex_is_a_headache = pattern.replaceAll("\\.", "\\\\.");
	        	regex_is_a_headache = pattern.replaceAll("\\*", ".*");
	            return name.matches(regex_is_a_headache);
	        }
		};
	}
	
	public static FilenameFilter getPatternFilter(final String pattern, final String ext){
		return new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.contains(pattern) && name.endsWith(ext);
	        }
		};
	}
	
	public static FilenameFilter getExtensionFilter(final String ext){
		return new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.endsWith(ext);
	        }
		};
	}
	
	static class FileCopy {
		 
		public static void copy(File source, File dest) throws IOException {
			 if(!dest.exists()) {
			  dest.createNewFile();
			 }
			 InputStream in = null;
			 OutputStream out = null;
			 try {
			  in = new FileInputStream(source);
			  out = new FileOutputStream(dest);
			    
			  // Transfer bytes from in to out
			  byte[] buf = new byte[1024];
			  int len;
			  while ((len = in.read(buf)) > 0) {
			   out.write(buf, 0, len);
			  }
			 }
			 finally {
			  if(in != null) {
			   in.close();
			  }
			  if(out != null) {
			   out.close();
			  }
			 }
			}

		}
	
	public static double getValue(byte[] b, DataType type, ByteOrder bo) throws IOException{
		int size = DataTypes.getSize(type);
		if (size > b.length) throw new IOException("Byte array to small for data type.");
		switch (type.val){
			case DataTypes.DOUBLE:
				return getDoubleFromBytes(b, bo);
			case DataTypes.INTEGER:
				return byteArrayToInt(b, bo);
			case DataTypes.SHORT:
				return byteArrayToShort(b, bo);
			case DataTypes.FLOAT:
				return getFloatFromBytes(b, bo);
			}
		return 0;
	}
	
	public static double getDoubleFromBytes(byte[] b, ByteOrder bo){
		return 0;
	}
	
	public static float getFloatFromBytes(byte[] b, ByteOrder bo){
		return 0;
	}
	
	public static byte[] getBytes(double val, DataType type, ByteOrder bo){
		switch (type.val){
		
		case DataTypes.SHORT:
			return shortToByteArray((int)val, bo);
		
		case DataTypes.INTEGER:
			return intToByteArray((int)val, bo);
			
		
		}
		
		
		return null;
	}
	
//	set byte array b from the given file in, and header h
	public static byte[] nextBytes(RandomAccessFile in, DataType type, boolean hasAlpha){
		//int size = h.image.bitpix / 8;
		int size = DataTypes.getSize(type);
		int alpha = 0;
		int i;
		float f;
		if (hasAlpha) alpha++;
		byte[] b = null;
		//String debug = "Data type: ";
		
		try{
		switch(type.val){
		
		//TODO add more cases
		case DataTypes.FLOAT:
			//debug += "DT_FLOAT";
			b = new byte[size + alpha];
			f = in.readFloat();
			b[0] = Float.valueOf(f).byteValue();
			break;
			
		case DataTypes.INTEGER:
			//debug += "DT_SIGNED_INT";
			b = new byte[size + alpha];
			i = in.readInt();
			//b = Grid3D.intToByteArray(i, Grid3D.)
			//TODO implement int32
			b[0] = Integer.valueOf(i).byteValue();
			break;
			
		case DataTypes.RGB:
		case DataTypes.RGBA:
			//debug += "DT_RGB";
			b = new byte[size + alpha];
			for (int a = 0; a < 3 && a < size - 1 ; a++)
				b[a] = in.readByte();
			break;
			
		case DataTypes.SHORT:
			//byte[] b1 = new byte[2];
			b = new byte[size + alpha];
			//i = in.readInt();
			b[0] = in.readByte();
			b[1] = in.readByte();
			/*
			if (h.byteOrder == ByteOrder.nativeOrder()){
				b[0] = b1[0];
				b[1] = b1[1];
				}else{
				b[1] = b1[0];
				b[0] = b1[1];
				}*/
			//int s = ioFunctions.getUInt(b1, h.byteOrder);
			//b[0] = Integer.valueOf(s).byteValue();
			break;
			
		default:
			b = new byte[size + alpha];
			for (int a = 0; a < size; a++)
				b[a] = in.readByte();
		
		}
		}catch (IOException e){
			e.printStackTrace();
			return null;
		}
		//InterfaceSession.log(debug);
		return b;
	}
	
	/************************
	 * Thanks to http://forum.java.sun.com/thread.jspa?threadID=609364&start=15&tstart=0 for this.
	 * @param i
	 * @param bo
	 * @return
	 */
	
	 public static byte[] intToByteArray(int i, ByteOrder bo){
    	ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
    	buff.order(bo);
    	buff.putInt(i);
    	return buff.array();
    }
	    
    public static byte[] shortToByteArray(int i, ByteOrder bo){
    	ByteBuffer buff = ByteBuffer.allocate(Short.SIZE / 8);
    	buff.order(bo);
    	buff.putShort((short)i);
    	return buff.array();
    }
    
    public static int byteArrayToInt(byte[] b, ByteOrder bo){
    	ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
    	
    	if (b.length > Integer.SIZE / 8){
    		byte[] b0 = new byte[Integer.SIZE];
    		System.arraycopy(b, 0, b0, 0, Integer.SIZE / 8);
    		b = b0;
    		}
    	
    	buff.order(bo);
    	buff.put(b);
    	return buff.getInt(0);
    }
    
    public static int byteArrayToShort(byte[] b, ByteOrder bo){
    	ByteBuffer buff = ByteBuffer.allocate(Short.SIZE / 8);
    	//if too long, truncate
    	if (b.length > Short.SIZE / 8){
    		byte[] b0 = new byte[Short.SIZE];
    		System.arraycopy(b, 0, b0, 0, Short.SIZE / 8);
    		b = b0;
    		}
    	buff.order(bo);
    	buff.put(b);
    	return buff.getShort(0);
    }
	
    public static double byteArrayToVal(byte b[], int type, ByteOrder bo){
		
		switch (type){
		case DataTypes.BYTE:
			return b[0];
		case DataTypes.SHORT:
			return byteArrayToShort(b, bo);
		case DataTypes.INTEGER:
			return byteArrayToInt(b, bo);
		case DataTypes.FLOAT:
			//TODO implement this
			break;
		case DataTypes.DOUBLE:
			//TODO ditto
			break;
		}
		
		return Double.NaN;
	}
    
    public static float byteArrayToFloat(byte[] b, ByteOrder bo){
    	ByteBuffer buff = ByteBuffer.allocate(Float.SIZE / 8);
    	//if too long, truncate
    	if (b.length > Float.SIZE / 8){
    		byte[] b0 = new byte[Float.SIZE];
    		System.arraycopy(b, 0, b0, 0, Float.SIZE / 8);
    		b = b0;
    		}
    	buff.order(bo);
    	buff.put(b);
    	return buff.getFloat(0);
    }
	
	public static byte[] valToByteArray(double val, int type, ByteOrder bo){
		switch (type){
		case DataTypes.BYTE:
		case DataTypes.SHORT:
			return shortToByteArray((int)val, bo);
		case DataTypes.INTEGER:
			return intToByteArray((int)val, bo);
		case DataTypes.FLOAT:
			//TODO implement this
			break;
		case DataTypes.DOUBLE:
			//TODO ditto
			break;
		}
		return null;
	}
    
	/********************************************************
	 * Returns a list of IO types which are compatible (i.e., handle the same set of objects)
	 * as {@code type}.
	 * 
	 * @param type
	 * @return
	 */
	public static ArrayList<String> getSupportingTypes(InterfaceObject object){
		
		HashMap<String,InterfaceIOType> types = InterfaceEnvironment.getIOTypes();
		
		ArrayList<String> keys = new ArrayList<String>(types.keySet());
		ArrayList<String> matches = new ArrayList<String>();
		
		for (int i = 0; i < keys.size(); i++){
			ArrayList<Class<?>> supported = types.get(keys.get(i)).getSupportedObjects();
			for (int j = 0; j < supported.size(); j++){
				if (supported.get(j).isInstance(object)){
					matches.add(keys.get(i));
					break;
					}
				}
			}
		
		return matches;
		
	}
	
	/*******************************************************
	 * Returns the first instance in {@link InterfaceEnvironment} of an {@link InterfaceIOType}
	 * compatible with {@code shape}.
	 * 
	 * @param shape			Shape for which to retrieve a type
	 * @param io_type 		Operation type of IO type
	 * @see InterfaceIOType
	 * @return
	 */
	public static InterfaceIOType getDefaultIOType(InterfaceShape shape, int io_type){
		ArrayList<String> supported = IoFunctions.getSupportingTypes(shape);
		for (int j = 0; j < supported.size(); j++){
			InterfaceIOType type = InterfaceEnvironment.getIOType(supported.get(j));
			if (type != null && type.getType() == io_type){
				return type;
				}
			}
		return null;
	}
	
	/*********************************************************
	 * Matches a wildcard string to all paths, relative to the directory {@code root}. If {@code root} is
	 * {@code null}, paths must be absolute (relative to the system root). The wildcard pattern matching uses 
	 * regular expressions (regex).
	 * 
	 * <p>Example:<br>
	 * data/&#42/subdir/somedata_&#42.dat    - Searchs "subdir" (if it exists) in all subdirectories of "data" 
	 * 										   for file names matching "somedata_&#42.dat" 
	 * 
	 * @param root
	 * @param pattern
	 * @return
	 */
	public static ArrayList<File> getWildcardPathFiles(File root, String pattern){
		
//		if (root == null){
//			root = getSystemRoot();
//			}
		
		// Get parts
		String[] file_parts = pattern.split(File.separator);
		ArrayList<File> files = new ArrayList<File>();
		
		if (file_parts.length == 1){
			if (root == null){
				root = getSystemRoot();
				}
			// These are files, apply filter add append to list
			FilenameFilter filter = getRegexFilter(pattern);
			File[] _files = root.listFiles(filter);
			for (int i = 0; i < _files.length; i++)
				files.add(_files[i]);
			return files;
			}
		
		// Deal with subdirectory parts
		String new_pattern = "";
		for (int i = 0; i < file_parts.length; i++){
			String part = file_parts[i];
			if (part.contains("*")){
				if (i == file_parts.length-1){
					// These are files, return the wildcard matches
					files.addAll(getWildcardPathFiles(new File(new_pattern), part));
				}else{
					// These are directories, examine matching subdirectories recursively
					String new_root = "";
					if (root != null){
						new_root = root.getAbsolutePath();
						if (!new_root.endsWith(File.separator))
							new_root = new_root + File.separator;
						new_root = new_root + new_pattern;
					}else{
						new_root = new_pattern;
						}
					File dir = new File(new_root);
					File[] dirs = dir.listFiles(getRegexDirFilter(part));
					String patt = "";
					for (int j = i+1; j < file_parts.length; j++){
	//					if (!patt.endsWith(File.separator))
	//						patt = patt + File.separator;
						patt = patt + file_parts[j];
						}
					for (int j = 0; j < dirs.length; j++)
						files.addAll(getWildcardPathFiles(dirs[j], patt));
				}
			}else{
				// Otherwise append to pattern and carry on
				if (!new_pattern.endsWith(File.separator))
					new_pattern = new_pattern + File.separator;
				new_pattern = new_pattern + part;
				}
				
			}
		
		return files;
		
	}
	
	
}