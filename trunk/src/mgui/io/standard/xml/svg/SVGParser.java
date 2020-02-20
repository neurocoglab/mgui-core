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

package mgui.io.standard.xml.svg;

/**********
 * 
 * @author borrowed from Paxinos3D
 *
 */

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mgui.interfaces.InterfaceSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class SVGParser extends Object {

	public final static String ext = "svg";
	
	public SVGParser() {
		paths = new ArrayList();
	}

	public static boolean isSVGFile(File file) {
		return isSVGFileName(file.getName());
	}
	
	public static boolean isSVGFileName(String file_name) {
		String extension = null;
		int i = file_name.lastIndexOf('.');

		if (i > 0 && i < file_name.length() - 1) {
			extension = file_name.substring(i + 1).toLowerCase();
		}
		if (extension != null) {
			if (extension.equals(SVGParser.ext)) {
				return true;
			}
		}
		return false;
	}
	
	public void parse(InputStream input_Stream) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			factory.setCoalescing(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler() {
				public void fatalError(SAXParseException exception) {
				}

				public void error(SAXParseException e) throws SAXParseException {
					throw e;
				}

				public void warning(SAXParseException err) {
					InterfaceSession.log("Warning" + ",line " + err.getLineNumber() + ",uri "
													 + err.getSystemId());
				InterfaceSession.log("   " + err.getMessage());
			}
		});

		Document document = builder.parse(input_Stream);

		Element root = document.getDocumentElement();
		NodeList defs = root.getElementsByTagName("defs");
		for (int i = 0; i < defs.getLength(); i++) {
			Element def = (Element)defs.item(i);
			NodeList styles = def.getElementsByTagName("style");
			for (int j = 0; j < styles.getLength(); j++) {
				Element style = (Element)styles.item(j);
				String type = style.getAttribute("type");
				if (type.equals("text/css")) {
					svg_style = parseStyle(new SVGStyle(), style.getTextContent());
				}
			}
		}

		NodeList gns = root.getElementsByTagName("g");
		for (int i = 0; i < gns.getLength(); i++) {
			Element g = (Element)gns.item(i);
			NodeList pathns = g.getElementsByTagName("path");
			for (int j = 0; j < pathns.getLength(); j++) {
				Element path_element = (Element)pathns.item(j);
				SVGPath path = new SVGPath();
				String fill = path_element.getAttribute("fill");
				parseFill(path, fill);

				String class_data = path_element.getAttribute("class");
				parseClass(path, class_data);

				String data = path_element.getAttribute("d");
				parsePathData(path, data);
				paths.add(path);
			}
		}
	} catch (Exception ex) {
		InterfaceSession.log("ERROR: " + ex);
		ex.printStackTrace();
	}
}

	public SVGPath[] getPaths() {
		return (SVGPath[])paths.toArray(new SVGPath[0]);
	}

	protected SVGStyle parseStyle(SVGStyle style, String data) {
		//.str0 {stroke:#000000;stroke-width:3}
		//.fil44 {fill:#001E96}
		// ... and so on

		String SPACE = " ";
		String NEWLINE = "\n";
		String DOT = ".";
		String RIGHT_CURLY = "{";
		String LEFT_CURLY = "}";

		String delimiters = new String(SPACE + NEWLINE + DOT + RIGHT_CURLY + LEFT_CURLY);
		StringTokenizer tokenizer = new StringTokenizer(data, delimiters, false);

		while (tokenizer.hasMoreTokens()) {
			style.add(tokenizer.nextToken(), tokenizer.nextToken());
		}
		return style;
	}

	protected void parseFill(SVGPath path, String fill) {

		if (!fill.equals("") && !fill.equals("none") && !fill.equals("url(#id0)")) {
			Integer integer = Integer.decode(fill);
			path.colour = new Color(integer.intValue());
		}
	}

	protected SVGPath parsePathData(SVGPath path, String data) {
		String SPACE = " ";
		String COMMA = ",";
		String M = "M";
		String m = "m";
		String L = "L";
		String l = "l";
		String Z = "Z";
		String z = "z";
		String C = "C";
		String c = "c";
		// NDB TODO Use a StreamTokenizer instead
		String delimiters = new String(" ,MmZzLlHhVvCcSsQqTtAa");
		StringTokenizer tokenizer = new StringTokenizer(data, delimiters, true);

		Instruction current_instruction = null;

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals(SPACE) || token.equals(COMMA)) {
				// ignore it!
			} else if (token.equals(M)) {
				current_instruction = new MoveTo(Mode.ABSOLUTE);
				path.addInstruction(current_instruction);
				//NDB InterfaceSession.log("New MoveTo Instruction");
			} else if (token.equals(m)) {
				current_instruction = new MoveTo(Mode.RELATIVE);
				path.addInstruction(current_instruction);
				//NDB InterfaceSession.log("New MoveTo Instruction");
			} else if (token.equals(L)) {
				current_instruction = new LineTo(Mode.ABSOLUTE);
				path.addInstruction(current_instruction);
				//NDB InterfaceSession.log("New LineTo Instruction");
			} else if (token.equals(l)) {
				current_instruction = new LineTo(Mode.RELATIVE);
				path.addInstruction(current_instruction);
				//NDB InterfaceSession.log("New LineTo Instruction");
			} else if (token.equals(C)) {
				current_instruction = new CurveTo(Mode.ABSOLUTE);
				path.addInstruction(current_instruction);
				//NDB InterfaceSession.log("New CurveTo Instruction");
			} else if (token.equals(c)) {
				current_instruction = new CurveTo(Mode.RELATIVE);
				path.addInstruction(current_instruction);
				//NDB InterfaceSession.log("New CurveTo Instruction");
			} else if (token.equals(Z)) {
				current_instruction = new Close(Mode.ABSOLUTE);
				path.addInstruction(current_instruction);
				//NDB InterfaceSession.log("New Close Instruction");
				return path;
			} else if (token.equals(z)) {
				current_instruction = new Close(Mode.RELATIVE);
				path.addInstruction(current_instruction);
				//NDB InterfaceSession.log("New Close Instruction");
//					return path;
			} else {
				try {
					int number = Integer.parseInt(token);
					if (current_instruction != null) {
						if (!current_instruction.addNumber(number)) {
							Instruction old_instruction = current_instruction;
							if (old_instruction instanceof MoveTo) {
								current_instruction = new MoveTo(old_instruction.getMode());
								path.addInstruction(current_instruction);
								//NDB InterfaceSession.log("New MoveTo Instruction");
							} else if (old_instruction instanceof LineTo) {
								current_instruction = new LineTo(old_instruction.getMode());
								path.addInstruction(current_instruction);
								//NDB InterfaceSession.log("New LineTo Instruction");
							} else if (old_instruction instanceof CurveTo) {
								current_instruction = new CurveTo(old_instruction.getMode());
								path.addInstruction(current_instruction);
								//NDB InterfaceSession.log("New CurveTo Instruction");
							}
							current_instruction.addNumber(number);
							//NDB InterfaceSession.log("Added "+number);
						} else {
							//NDB InterfaceSession.log("Added "+number);
						}
					} else {
						InterfaceSession.log("SVGHandeler Error: No Mode is currently set." + token);
					}
				} catch (NumberFormatException e) {
					InterfaceSession.log("SVGHandeler Error: Number Format Exception." + token);
				}
			}
		}
		return path;
	}

	protected void parseClass(SVGPath path, String data) {
		//class="fil0 str0"

		String SPACE = " ";

		String delimiters = new String(SPACE);
		StringTokenizer tokenizer = new StringTokenizer(data, delimiters, false);

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			String value = svg_style.getValue(token);

			//fill:#001E96
			String COLON = ":";
			String FILL = "fill";
			String value_delimiters = new String(COLON);
			StringTokenizer value_tokenizer = new StringTokenizer(value, value_delimiters, false);
			while (value_tokenizer.hasMoreTokens()) {
				String name = value_tokenizer.nextToken();
				if (name.equals(FILL)) {
					String fill = value_tokenizer.nextToken();
					if (!fill.equals("") && !fill.equals("none") && !fill.equals("url(#id0)")) {
						Integer integer = Integer.decode(fill);
						path.colour = new Color(integer.intValue());
					}
				}
			}
		}
	}

	public enum Mode {
		RELATIVE, ABSOLUTE
	};

	protected ArrayList paths;
	protected SVGStyle svg_style;
}