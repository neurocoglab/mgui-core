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

package mgui.io.domestic.variables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import mgui.interfaces.ProgressUpdater;
import mgui.io.InterfaceIOException;
import mgui.io.InterfaceIOOptions;
import Jama.Matrix;
import foxtrot.Job;
import foxtrot.Worker;

/***********************************************
 * Default implementation of a matrix loader. Loads four standard formats:
 * 
 *  <ul>
 *  <li>Ascii Full
 *  <li>Ascii Sparse
 *  <li>Binary Full
 *  <li>Binary Sparse
 *  </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class DefaultMatrixFileLoader extends MatrixFileLoader {

	static Exception last_exception;
	
	public DefaultMatrixFileLoader(){
		
	}
	
	public DefaultMatrixFileLoader(File file){
		super(file);
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadMatrix(progress_bar);
	}
		
	@Override
	public Matrix loadMatrix(final ProgressUpdater progress_bar) throws IOException, InterfaceIOException {
		
		if (dataFile == null) throw new InterfaceIOException("No input file specified.");
		
		//if progress bar is set, run this as a Foxtrot thread; otherwise run blocking
		if (progress_bar == null){
			switch (options.format){
				case AsciiFull:
					return loadMatrixAsciiFullBlocking(progress_bar);
				case AsciiSparse:
					return loadMatrixAsciiSparseBlocking(progress_bar);
				case BinaryFull:
					return loadMatrixBinaryFullBlocking(progress_bar);
				case BinarySparse:
					return loadMatrixBinarySparseBlocking(progress_bar);
				}
			throw new InterfaceIOException("Invalid matrix format specified.");
		}else{
			Matrix matrix = ((Matrix)Worker.post(new Job(){
				@Override
				public Matrix run(){
					try{
						switch (options.format){
							case AsciiFull:
								return loadMatrixAsciiFullBlocking(progress_bar);
							case AsciiSparse:
								return loadMatrixAsciiSparseBlocking(progress_bar);
							case BinaryFull:
								return loadMatrixBinaryFullBlocking(progress_bar);
							case BinarySparse:
								return loadMatrixBinarySparseBlocking(progress_bar);
							}
					}catch (Exception e){ 
						e.printStackTrace();
						last_exception = e; 
						}
					return null;
				}
			}));
			//TODO: define this exception
			if (matrix == null){
				if (last_exception != null){
					if (last_exception instanceof IOException)
						throw (IOException)last_exception;
					if (last_exception instanceof InterfaceIOException)
						throw (InterfaceIOException)last_exception;
					}
				throw new InterfaceIOException("Unspecified exception encountered..");
			}
			return matrix;
			}
	}

	public Matrix loadMatrixAsciiFullBlocking(ProgressUpdater progress_bar) throws IOException, InterfaceIOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		
		int header_size = 1;
		int line_no = 1;
		String line = reader.readLine();
		if (line == null) throw new InterfaceIOException("Unexpected EOF at line 1.");
		
		//skip any comments
		while (line.startsWith("%")){
			line = reader.readLine();
			header_size++;
			if (line == null) throw new InterfaceIOException("Unexpected EOF at line " + line_no + ".");
			}
			
		//read header
		//TODO: header optional?
		StringTokenizer tokens = null;
		if (options.delimiter != null)
			tokens = new StringTokenizer(line);
		else
			tokens = new StringTokenizer(line, options.delimiter);
		
		int m, n;
		Matrix matrix = null;
		
		if (options.has_header){
			m = Integer.valueOf(tokens.nextToken());
			n = Integer.valueOf(tokens.nextToken());
			matrix = new Matrix(m, n);
			line = reader.readLine();
		}else{
			m = -1;
			n = -1;
			}
		
		if (progress_bar != null){
			progress_bar.setMinimum(0);
			progress_bar.setMaximum(m);
			}
		
		while (line != null){
			tokens = new StringTokenizer(line);
			if (n < 0){
				n = tokens.countTokens();
				// Fill matrix in increments of 100 rows
				matrix = new Matrix(100, n);
				if (progress_bar != null){
					progress_bar.setMaximum(100);
					}
				}
			if (m < 0){
				if (line_no > matrix.getRowDimension()){
					// Resize matrix
					Matrix m2 = new Matrix(line_no + 100, n);
					m2.setMatrix(0, matrix.getRowDimension() - 1, 0, n - 1, matrix);
					matrix = m2;
					if (progress_bar != null){
						progress_bar.setMaximum(line_no + 100);
						}
					}
					
			}else if (line_no > m){
				throw new InterfaceIOException("Matrix m dimension is greater than indicated in header: " + line_no + " > " + m);
				}
			
			if (tokens.countTokens() != n)
				throw new InterfaceIOException("Matrix n dimension is incorrect: " + tokens.countTokens() +
						" != " + n);
			int j = 0;
			while (tokens.hasMoreTokens())
				matrix.set(line_no - 1, j++, Double.valueOf(tokens.nextToken()));
			if (progress_bar != null)
				progress_bar.update(line_no);
			line = reader.readLine();
			line_no++;
			}
		
		if (m < 0 && line_no > 1){
			//Get filled portion of matrix
			Matrix t = matrix.getMatrix(0, line_no - 2, 0, n - 1);
			matrix = t;
			}
		
		reader.close();
		return matrix;
	}
	
	//TODO: implement
	public Matrix loadMatrixAsciiSparseBlocking(ProgressUpdater progress_bar) throws IOException {
		
		
		return null;
	}

	//TODO: implement
	public Matrix loadMatrixBinaryFullBlocking(ProgressUpdater progress_bar) throws IOException {
	
	
		return null;
	}

	//TODO: implement
	public Matrix loadMatrixBinarySparseBlocking(ProgressUpdater progress_bar) throws IOException {
	
	
		return null;
	}
	
}