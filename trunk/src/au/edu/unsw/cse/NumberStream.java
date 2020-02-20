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

package au.edu.unsw.cse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
/** This class provides a convenient way to read numbers from a text file */

public class NumberStream {

  protected StreamTokenizer st;  //The StreamTokenizer we use to get numbers

  /** Creates a NumberStream that parses the specified input stream.
@param is - an input stream.
   */
  public NumberStream(InputStream is) {
    this(new BufferedReader(new InputStreamReader(is)));
  }

  /** Creates a NumberStream that parses the specified character stream.
   */
  public NumberStream(Reader r) {
    st = new StreamTokenizer(r);
  }

  /** return the next double from the stream.
    Return Double.NaN if end of file has been reached.
    Note: you must use Double.isNaN() to test if a double is NaN
    */
  public double next() throws IOException{
    int t;
    while ((t=st.nextToken()) != StreamTokenizer.TT_EOF &&
	    t != StreamTokenizer.TT_NUMBER) {
      //nothing
    }
    if (t== StreamTokenizer.TT_EOF) {
      return Double.NaN;
    } else {
      return st.nval;
    }
  }
}