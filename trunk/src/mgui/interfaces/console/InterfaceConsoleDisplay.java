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

package mgui.interfaces.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphicTextBox;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.ParallelOutputStream;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiInteger;

import org.xml.sax.Attributes;

/******************************************************
 * Window which displays console output.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceConsoleDisplay extends InterfaceGraphic<Tool> {

	ConsolePane txtConsole; // = new ConsolePane();
	JScrollPane scrConsole;
	
	PipedInputStream input_stream; // = new PipedInputStream();
	PipedInputStream error_input_stream; // = new PipedInputStream();
	PipedOutputStream output_stream;
	PipedOutputStream error_output_stream;
	ConsoleAdapter adapter; 
	Thread input_reader;
	Thread error_reader;
	
	enum MessageType{
		Output,
		Warning,
		Error;
	}
	
	public InterfaceConsoleDisplay(){
		txtConsole = new ConsolePane();
		start();
	}
	
	public void start(){
		init2();
	}
	
	public void destroy(){
		input_reader = null;
		error_reader = null;
		super.destroy();
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/console_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/console_20.png");
		return null;
	}
	
	private void init2(){
		if (init_once) return;
		super.init();
		
		type = "Console Display";
		txtConsole.addMouseListener(this);
		txtConsole.addMouseMotionListener(this);
		
		attributes.add(new Attribute<Font>("Font", new Font("Courier New", Font.PLAIN, 12)));
		attributes.add(new Attribute<Color>("NormalColour", Color.green));
		attributes.add(new Attribute<Color>("ErrorColour", Color.red));
		attributes.add(new Attribute<MguiBoolean>("ShowOutput", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowErrors", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiInteger>("CharLimit", new MguiInteger(1000000)));
		attributes.add(new Attribute<MguiBoolean>("WrapLines", new MguiBoolean(true)));
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBackgroundColour(Color.black);
		
		//tool input adapter
		this.addMouseListener(toolInputAdapter.getMouseAdapter());
		this.addMouseMotionListener(toolInputAdapter.getMouseAdapter());
		this.addMouseWheelListener(toolInputAdapter.getMouseAdapter());
		
		setConsole();
		add(scrConsole, BorderLayout.CENTER);
		
		StyledDocument doc = txtConsole.getStyledDocument();
		txtConsole.setEditable(true);
		
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(){
		
		InterfacePopupMenu menu = super.getPopupMenu();
		int start = super.getPopupLength();
		
		menu.add(new JSeparator(), start);
		menu.add(new JSeparator(), start);
		menu.addMenuItem(new JMenuItem("Console Window", getObjectIcon()));
		menu.add(new JSeparator(), start + 3);
		menu.add(new JSeparator(), start + 3);
		JMenuItem item = new JMenuItem("Edit attributes..");
		item.setActionCommand("Window attributes");
		menu.addMenuItem(item);
		menu.addMenuItem(new JMenuItem("Clear console"));
		
		return menu;
		
	}
	
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		InterfaceSession.log("Handle graphic3d popup...",
				 LoggingType.Debug);
		
		if (item.getActionCommand().startsWith("Edit attributes")){
			InterfaceSession.getWorkspace().showAttributeDialog(this);
			return;
			}
		
		if (item.getActionCommand().equals("Clear console")){
			txtConsole.setText("");
			return;
			}
		
		super.handlePopupEvent(e);
		
	}
	
	@Override
	public int updateStatusBox(InterfaceGraphicTextBox box, MouseEvent e){
		
		int index = super.updateStatusBox(box, e);
		if (index <= 0) return index;
		
		switch (index){
		
			case 1:
				int length = txtConsole.getDocument().getLength();
				int limit = ((MguiInteger)attributes.getValue("CharLimit")).getInt();
				box.setText("Chars: " + length + " / " + limit);
				break;
			
			case 2:
				LoggingType log_type = InterfaceEnvironment.getLoggingType();
				String mode = "?";
				switch (log_type){
					case Errors:
						mode = "Errors only";
						break;
					case Concise:
						mode = "Concise";
						break;
					case Verbose:
						mode = "Verbose";
						break;
					case Debug:
						mode = "Debugging";
						break;
					}
				box.setText("Logging mode: " + mode);
				break;
				
			default:
				box.setText("");	
		}
		
		return index;
	}
	
	public boolean getWrapLines(){
		return ((MguiBoolean)attributes.getValue("WrapLines")).getTrue();
	}
	
	public void setWrapLines(boolean b){
		attributes.setValue("WrapLines", new MguiBoolean(b));
	}
	
	public int getCharLimit(){
		return ((MguiInteger)attributes.getValue("CharLimit")).getInt();
	}
	
	public Font getConsoleFont(){
		return (Font)attributes.getValue("Font");
	}
	
	public void setConsoleFont(Font font){
		attributes.setValue("Font", font);
	}
	
	public Color getConsoleNormalColour(){
		return (Color)attributes.getValue("NormalColour");
	}
	
	public void setConsoleNormalColour(Color c){
		attributes.setValue("NormalColour", c);
	}
	
	public Color getConsoleErrorColour(){
		return (Color)attributes.getValue("ErrorColour");
	}
	
	public void setConsoleErrorColour(Color c){
		attributes.setValue("ErrorColour", c);
	}
	
	public boolean getShowErrors(){
		return ((MguiBoolean)attributes.getValue("ShowErrors")).getTrue();
	}
	
	public boolean getShowOutput(){
		return ((MguiBoolean)attributes.getValue("ShowOutput")).getTrue();
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		if (e.getAttribute().getName().equals("Font")){
			txtConsole.setFont(getConsoleFont());
			txtConsole.updateUI();
			return;
			}
		
		if (e.getAttribute().getName().startsWith("Background")){
			txtConsole.setBackground(getBackgroundColour());
			if (scrConsole != null)
				scrConsole.setBackground(getBackgroundColour());
			txtConsole.updateUI();
			return;
			}
		
		if (e.getAttribute().getName().equals("FontColour")){
			txtConsole.setForeground(getConsoleNormalColour());
			txtConsole.updateUI();
			return;
			}
		
		if (e.getAttribute().getName().equals("CharLimit")){
			appendText("");
			return;
			}
		
		if (e.getAttribute().getName().equals("WrapLines")){
			//why is it so hard to get a window to redraw itself?
			txtConsole.invalidate();
			txtConsole.setCaretPosition(txtConsole.getCaretPosition());
			scrConsole.invalidate();
			this.invalidate();
			this.repaint();
			return;
		}
		
	}
	
	/********************************************************
	 * Appends text to this console as type <code>Output</code>.
	 * 
	 * @param text
	 */
	public void appendText(final String text){
		
		appendText(text, MessageType.Output);
			
	}
	
	/********************************************************
	 * Appends text this console as the specified type. Thread safe.
	 * 
	 * @param text
	 * @param type
	 */
	public void appendText(final String text, final MessageType type){
		
		Runnable task = new Runnable(){
			
			public void run(){
				try{
					Document doc = txtConsole.getDocument();
					String s = doc.getText(0, doc.getLength());
					int wtf = getCharLimit();
					if (doc.getLength() >  wtf){
						s = s.substring(getCharLimit() / 2);
						if (s.indexOf("\n") >= 0 && s.length() > 1)
							s = s.substring(s.indexOf("\n") + 1);
						txtConsole.setText(s);
						}
					
					s = doc.getText(0, doc.getLength());
					if (s.length() > 0)
						txtConsole.setCaretPosition(s.length());
					
					Color colour = null;
					
					switch(type){
						case Output:
							colour = getConsoleNormalColour();
							break;
						case Warning:
						case Error:
							colour = getConsoleErrorColour();
						}
					
					SimpleAttributeSet aset = new SimpleAttributeSet();
					StyleConstants.setForeground(aset, colour);
					
					if (s.length() > 0)
						txtConsole.setCaretPosition(s.length());
					
					txtConsole.setCharacterAttributes(aset, false);
					txtConsole.replaceSelection(text);
				
				}catch (Exception ex){
					InterfaceSession.handleException(ex);
					}
			}
		};
		
		SwingUtilities.invokeLater(task);
		
	}
	
	void setConsole(){
		
		if (scrConsole == null){
			txtConsole.setOpaque(true);
			txtConsole.setEditable(false);
			txtConsole.setFont(getConsoleFont());
			txtConsole.setBackground(getBackgroundColour());
			txtConsole.setForeground(getConsoleNormalColour());
			scrConsole = new JScrollPane(txtConsole);
			scrConsole.setBackground(getBackgroundColour());
			scrConsole.setOpaque(true);
			
			try{
				if (input_stream == null){
					input_stream = new PipedInputStream();
					output_stream = new PipedOutputStream(input_stream);
					ParallelOutputStream out_stream = InterfaceEnvironment.getSystemOutputStream();
					out_stream.addStream(output_stream);
					error_input_stream = new PipedInputStream();
					error_output_stream = new PipedOutputStream(error_input_stream);
					out_stream = InterfaceEnvironment.getSystemErrorStream();
					out_stream.addStream(error_output_stream);
					}
				
				adapter = new ConsoleAdapter(this);
				adapter.quit = false;
				
				input_reader = new Thread(adapter);
				input_reader.setDaemon(true);	
				input_reader.start();	
				
				error_reader = new Thread(adapter);
				error_reader.setDaemon(true);	
				error_reader.start();	
				
			}catch (Exception e){
				InterfaceSession.log("InterfaceConsoleDisplay: Error setting console input stream..");
				e.printStackTrace();
				return;
				}
			
			}
		
	}
	
	@Override
	public String getTitle(){
		return "Console: " + getName();
	}
	
	@Override
	public String toString(){
		return "Console Display Panel: " + getName();
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.add(attributes.issueTreeNode());
	}
	
	static class ConsoleAdapter extends WindowAdapter implements WindowListener, Runnable {
		
		InterfaceConsoleDisplay display;
		public boolean quit;
		
		public ConsoleAdapter(InterfaceConsoleDisplay display){
			this.display = display;
		}
		
		public synchronized void run(){
			try {			
				while (Thread.currentThread()==display.input_reader || Thread.currentThread()==display.error_reader){
					PipedInputStream pin = null;
					if (Thread.currentThread()==display.input_reader)
						pin = display.input_stream;
					else
						pin = display.error_input_stream;
						
					try { 
						this.wait(100);
					}catch(InterruptedException ie) {}
					
					if (pin.available() != 0){
						String input = this.readLine(pin);
						if (Thread.currentThread()==display.input_reader && display.getShowOutput())
							display.appendText(input, MessageType.Output);
						else if (Thread.currentThread()==display.error_reader && display.getShowErrors())
							display.appendText(input, MessageType.Error);
						}
					if (quit) return;
					}
				
//				while (Thread.currentThread()==display.error_reader){
//					try { 
//						this.wait(100);
//					}catch(InterruptedException ie) {}
//					
//					if (display.error_input_stream.available(display.error_input_stream) != 0){
//						String input = this.readLine();
//						if (display.getShowErrors())
//							display.appendText(input, MessageType.Error);
//						}
//					if (quit) return;
//					}
				
			}catch (Exception e){
				display.appendText("Console reports an Internal error.", MessageType.Error);
				display.appendText("The error is: " + e.getMessage(), MessageType.Error);
				//display.txtConsole.append("\nConsole reports an Internal error.");
				//display.txtConsole.append("The error is: " + e);			
				}
		}
		
		public synchronized String readLine(PipedInputStream in) throws IOException
		{
			String input = "";
			do{
				int available = in.available();
				if (available == 0) break;
				byte b[] = new byte[available];
				in.read(b);
				input = input + new String(b, 0, b.length);														
			}while ( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
			
			return input;
		}
		
		@Override
		public synchronized void windowClosed(WindowEvent evt){
			quit = true;
			this.notifyAll(); // stop all threads
			try { 
				display.input_reader.join(1000);
				display.input_stream.close();
				display.error_reader.join(1000);
				display.error_input_stream.close(); 
			}catch (Exception e){
				
				}
		}
		
	}
	
	
	
	//************************ XML Stuff *****************************************
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<InterfaceConsoleDisplay>\n");
		
		attributes.writeXML(tab, writer, options, progress_bar);
		
		writer.write(_tab + "</InterfaceConsoleDisplay>\n");
		
	}

	class ConsolePane extends JTextPane{
		
		@Override
		public boolean getScrollableTracksViewportWidth(){
			if (getWrapLines()) return true;
			if (scrConsole != null)
				return txtConsole.getWidth() < scrConsole.getWidth();
			return false;
		}
		
		
		
	}
	
}