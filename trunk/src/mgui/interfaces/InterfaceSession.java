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

package mgui.interfaces;

import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mgui.interfaces.frames.SessionFrame;
import mgui.interfaces.logs.LoggingTarget;
import mgui.interfaces.logs.LoggingType;
import mgui.util.IDFactory;

/****************************************************
 * This is the main entry point into the mgui application. The main method instantiates a
 * <code>MainFrame</code> with an optionally specified init file. If no file is specified,
 * <code>InterfaceEnvironment</code> uses its default init file.
 * 
 * <p>This class represents a static session of modelGUI. It sets up the static 
 * <code>InterfaceEnvironment</code> and then displays a corresponding <code>SessionFrame</code>
 * and <code>InterfaceWorkspace</code> instance.
 * 
 * <p>See <a href="http://mgui.wikidot.com/init-files">this wiki page</a> for a description of
 * init files.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.frames.SessionFrame SessionFrame
 * @see mgui.interfaces.InterfaceEnvironment InterfaceEnvironment
 */

public class InterfaceSession {

	static SessionFrame session_frame;
	static boolean show_errors_on_console = true;
	static boolean show_stack_trace_on_console = true;
	
	static IDFactory uid_factory = new IDFactory();
	
	static InterfaceWorkspace workspace;
	
	static Clipboard clipboard = new Clipboard();
	
	/*******************************************
	 * Main entry point into modelGUI application, without a frame being created. 
	 * Initializes <code>InterfaceEnvironment</code>
	 * from the default init file. Constructs and displays an instance of <code>MainFrame</code>, 
	 * and sets the look and feel.
	 */
	public static void start_no_gui(){
		start_no_gui(null);
	}
	
	/*******************************************
	 * Main entry point into modelGUI application. Initializes <code>InterfaceEnvironment</code>
	 * from the default init file. Constructs and displays an instance of <code>MainFrame</code>, 
	 * and sets the look and feel.
	 */
	public static void start_no_gui(String init){
		String init_file = null;
		
		//if (args != null && args.length > 0)
			init_file = init;
		
		if (init_file == null && !InterfaceEnvironment.init()){
			InterfaceSession.log("Fatal error: Could not initialize InterfaceEnvironment.", LoggingType.Errors);
			System.exit(0);
		}else{
			InterfaceEnvironment.setFrame(null);
			}
		
		if (init_file != null && !InterfaceEnvironment.init(null, init_file)){
			InterfaceSession.log("Fatal error: Could not initialize InterfaceEnvironment.", LoggingType.Errors);
			System.exit(0);
			}
		
		//set user-specified look and feel
		String look_and_feel = InterfaceEnvironment.getLookAndFeel();
		boolean repeat = false;
		do{
		try{
			if (look_and_feel.equals("native"))
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			else if (look_and_feel.equals("java"))
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			else
				UIManager.setLookAndFeel(look_and_feel);
			repeat = false;
		}catch (Exception e){
			if (repeat){
				//failed on second try; exit
				//if this happens something is wrong with the world..
				InterfaceSession.log("Fatal error: could not set look and feel.");
				System.exit(0);
				}
			if (look_and_feel.equals("java")){
				InterfaceSession.log("Error setting Java look and feel.. " +
								   "trying Native look and feel.");
				look_and_feel = "native";
			}else if (look_and_feel.equals("java")){
				InterfaceSession.log("Error setting Native look and feel.. " +
				   				   "trying Java look and feel.");
				look_and_feel = "java";
			}else{
				InterfaceSession.log("Error setting look and feel '" + look_and_feel + "'. " +
				   				   "Trying Java look and feel.");
				look_and_feel = "java";
				}
			repeat = true;
			}
		}while (repeat);

		// Instantiate workspace
		workspace = InterfaceEnvironment.getWorkspaceInstance();
		String message = "ModelGUI session started";
		if (!InterfaceEnvironment.logging_timestamp)
			message = message + " at " + InterfaceEnvironment.getNow("dd.MM.yyyy HH:mm:ss z");
		log(message, LoggingType.Concise);
	}
	
	/*******************************************
	 * Main entry point into ModelGUI application. Initializes <code>InterfaceEnvironment</code>
	 * from the default init file. Constructs and displays an instance of <code>MainFrame</code>, 
	 * and sets the look and feel.
	 */
	public static void start(){
		start(null, true);
	}
	
	/*******************************************
	 * Main entry point into ModelGUI application. Initializes <code>InterfaceEnvironment</code>
	 * from the default init file. Constructs and displays an instance of <code>MainFrame</code>, 
	 * and sets the look and feel.
	 */
	public static void start(boolean show_gui){
		start(null, true);
	}
	
	/*******************************************
	 * Main entry point into modelGUI application. Initializes <code>InterfaceEnvironment</code>
	 * from the init file <code>init</code>, or a default init file if <code>init</code> is <code>null</code>.
	 * Constructs and displays an instance of <code>MainFrame</code>, and sets the look and feel.
	 * 
	 * @param init the init file; if <code>null</code>, the environment loads from the default init file
	 */
	public static void start(String init) {
		start(init, true);
	}
	
	/*******************************************
	 * Main entry point into modelGUI application. Initializes <code>InterfaceEnvironment</code>
	 * from the init file <code>init</code>, or a default init file if <code>init</code> is <code>null</code>.
	 * Constructs and displays an instance of <code>MainFrame</code>, and sets the look and feel.
	 * 
	 * @param init the init file; if <code>null</code>, the environment loads from the default init file
	 * @param how_gui  Whether to make the session frame visible
	 */
	public static void start(String init, boolean show_gui) {
		session_frame = new SessionFrame();
		session_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String init_file = init;
		
		if (init_file == null && !InterfaceEnvironment.init()){
			InterfaceSession.log("Fatal error: Could not initialize InterfaceEnvironment.", LoggingType.Errors);
			System.exit(0);
		}else{
			InterfaceEnvironment.setFrame(session_frame);
			}
		
		if (init_file != null && !InterfaceEnvironment.init(session_frame, init_file)){
			InterfaceSession.log("Fatal error: Could not initialize InterfaceEnvironment.", LoggingType.Errors);
			System.exit(0);
			}
		
		//set user-specified look and feel
		String look_and_feel = InterfaceEnvironment.getLookAndFeel();
		boolean repeat = false;
		do{
		try{
			if (look_and_feel.equals("native"))
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			else if (look_and_feel.equals("java"))
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			else
				UIManager.setLookAndFeel(look_and_feel);
			repeat = false;
		}catch (Exception e){
			if (repeat){
				//failed on second try; exit
				//if this happens something is wrong with the world..
				InterfaceSession.log("Fatal error: could not set look and feel.");
				System.exit(0);
				}
			if (look_and_feel.equals("java")){
				InterfaceSession.log("Error setting Java look and feel.. " +
								   "trying Native look and feel.");
				look_and_feel = "native";
			}else if (look_and_feel.equals("java")){
				InterfaceSession.log("Error setting Native look and feel.. " +
				   				   "trying Java look and feel.");
				look_and_feel = "java";
			}else{
				InterfaceSession.log("Error setting look and feel '" + look_and_feel + "'. " +
				   				   "Trying Java look and feel.");
				look_and_feel = "java";
				}
			repeat = true;
			}
		}while (repeat);

		String message = "ModelGUI session started";
		if (!InterfaceEnvironment.logging_timestamp)
			message = message + " at " + InterfaceEnvironment.getNow("dd.MM.yyyy HH:mm:ss z");
		log(message, LoggingType.Concise);
		
		// Instantiate workspace
		workspace = InterfaceEnvironment.getWorkspaceInstance();
		
		
        //Display the window maximized.
		if (show_gui){
			session_frame.init();
			session_frame.pack();
			session_frame.setExtendedState(session_frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
			session_frame.setVisible(true);
			}
		
		InterfaceEnvironment.ready();
		
	}
	
	/*******************************************
	 * Terminates this ModelGUI session and exits the JVM.
	 * 
	 */
	public static void terminate(){
		
		String message = "ModelGUI session ended";
		if (!InterfaceEnvironment.logging_timestamp)
			message = message + " at " + InterfaceEnvironment.getNow("dd.MM.yyyy HH:mm:ss z");
		log(message, LoggingType.Concise);
		
		System.exit(0);
	}
	
	/*******************************************
	 * Returns a unique identifier for this session.
	 * 
	 * @return
	 */
	public static long getUID(){
		return uid_factory.getID();
	}
	
	/*******************************************
	 * Returns the frame for the current session.
	 * 
	 * @return
	 */
	public static SessionFrame getSessionFrame(){
		return session_frame;
	}
	
	/*******************************************
	 * Returns the workspace for the current session. 
	 * 
	 * @return
	 */
	public static InterfaceWorkspace getWorkspace(){
		return workspace;
	}
	
	/*******************************************
	 * Returns the top display panel for the current session.
	 * 
	 * @return
	 */
	public static InterfaceDisplayPanel getDisplayPanel(){
		return getWorkspace().getDisplayPanel();
	}
	
	/*****************************************
	 * Specifies whether this session has been completely initialized yet.
	 * 
	 * @return
	 */
	public static boolean isInit(){
		return session_frame != null && workspace != null;
	}
	
	/******************************************
	 * Logs a message according the logging type of {@link InterfaceEnvironment}. 
	 * 
	 * @param message
	 * @return
	 */
	public static boolean log(String message){
		return log(message, InterfaceEnvironment.getLoggingType());
	}

	/******************************************
	 * Logs a message according the logging type of {@link InterfaceEnvironment}. Whether the log occurs
	 * depends on the environment logging type for this session, and the <code>type</code> parameter:
	 * 
	 * <ul>
	 * <li><code>Errors</code>: Will always be logged
	 * <li><code>Concise</code>: Will always be logged unless environment logging type is <code>Errors</code>
	 * <li><code>Verbose</code>: Will only be logged if environment logging type is <code>Verbose</code> or <code>Debug</code>
	 * <li><code>Debug</code>: Will only be logged is environment logging type is <code>Debug</code> 
	 * </ul>
	 * 
	 * <p> This method is thread safe, since it calls {@code invokeLater}.
	 * 
	 * @param message
	 * @param type
	 * @return
	 */
	public static boolean log(final String _message, final LoggingType type){
		return log(_message, type, InterfaceEnvironment.logging_timestamp);
	}
	
	/******************************************
	 * Logs a message according the logging type of {@link InterfaceEnvironment}. Whether the log occurs
	 * depends on the environment logging type for this session, and the <code>type</code> parameter:
	 * 
	 * <ul>
	 * <li><code>Errors</code>: Will always be logged
	 * <li><code>Concise</code>: Will always be logged unless environment logging type is <code>Errors</code>
	 * <li><code>Verbose</code>: Will only be logged if environment logging type is <code>Verbose</code> or <code>Debug</code>
	 * <li><code>Debug</code>: Will only be logged is environment logging type is <code>Debug</code> 
	 * </ul>
	 * 
	 * @param message 			- message to log
	 * @param type				- logging type
	 * @param timestamp 		- whether to log the timestamp
	 * @return
	 */
	public static boolean log(final String _message, final LoggingType type, boolean timestamp){
		
				LoggingType log_type = InterfaceEnvironment.getLoggingType();
				String message = _message;
				if (timestamp)
					message = InterfaceEnvironment.getNow("yyyy.MM.dd hh:mm:ss z") + ": " + message;
				
				//only log if type is right
				switch (log_type){
					case Errors:
					case Warnings:
						if (!type.equals(LoggingType.Errors))
							return false;
						break;
					case Concise:
						if (!(type.equals(LoggingType.Concise) || type.equals(LoggingType.Errors)))
							return false;
						break;
					case Verbose:
						if (type.equals(LoggingType.Debug))
							return false;
						break;
					case Debug:
						message = "DEBUG: " + message;
					}
				
				LoggingTarget target = InterfaceEnvironment.getLoggingTarget();
				
				switch (target){
					case ToConsole:
						if (type == LoggingType.Errors || type == LoggingType.Warnings)
							System.err.println(message);
						else
							System.out.println(message);
						return true;
					case ToFileAndConsole:
						if (type == LoggingType.Errors || type == LoggingType.Warnings)
							System.err.println(message);
						else
							System.out.println(message);
					case ToFile:
						File file = InterfaceEnvironment.getLogFile();
						if (file == null){
							System.out.println("InterfaceSession: No log file specified!\nSwitching to console.");
							InterfaceEnvironment.logging_target = LoggingTarget.ToConsole;
							System.out.println(message);
							return false;
							}
						try{
							BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
							writer.write(message + "\n");
							writer.close();
							return true;
						}catch (IOException e){
							System.out.println("InterfaceSession: Error writing to log file: " + file.getAbsolutePath());
							return false;
							}
					case None:
					}
		
		return false;
		
	}

	public static boolean showErrorsOnConsole(){
		return show_errors_on_console;
	}
	
	public static boolean showStackTraceOnConsole(){
		return show_stack_trace_on_console;
	}
	
	/**********************************************************
	 * Handles an exception, assuming the {@code LoggingType} is {@code Errors.}
	 * 
	 * @param ex
	 */
	public static void handleException(Exception ex){
		handleException(ex, LoggingType.Errors);
	}
	
	/**********************************************************
	 * Handles an exception, based on the specified {@code LoggingType}.
	 * 
	 * @param ex
	 * @param type 	The type of exception, determines how it will be displayed based on the current
	 * 				environment setting
	 */
	public static void handleException(Exception ex, LoggingType type){
		LoggingType log_type = InterfaceEnvironment.getLoggingType();
		switch(log_type){
			case Errors:
				if (!type.equals(LoggingType.Errors)) return;
				if (show_stack_trace_on_console){
					log("Exception encountered: " + ex.getMessage(), type);
					ex.printStackTrace();
				}else{
					log("Exception encountered: " + ex.getMessage(), type);
					}
				return;
			case Concise:
				if (!type.equals(LoggingType.Errors)) return;
				log("Error: " + ex.getMessage(), type);
				return;
			case Verbose:
				log("Error: " + ex.getMessage(), type);
				if (show_stack_trace_on_console)
					ex.printStackTrace();
				return;
			case Debug:
				log("Error: " + ex.getMessage(), type);
				ex.printStackTrace();
				return;
			case Warnings:
				if (type.equals(LoggingType.Errors)){
					if (show_stack_trace_on_console){
						log("Error: " + ex.getMessage(), type);
						ex.printStackTrace();
					}else{
						log("Error: " + ex.getMessage(), type);
						}
					return;
					}
				if (type.equals(LoggingType.Warnings)){
					if (show_stack_trace_on_console){
						log("Warning: " + ex.getMessage(), type);
						ex.printStackTrace();
					}else{
						log("Warning: " + ex.getMessage(), type);
						}
					return;
					}
			default:
				break;
			}
	}
	
	/*********************************
	 * 
	 * Returns the clipboard for the current session. This can be used for simple copy/paste
	 * procedures.
	 * 
	 * @return
	 */
	public static Clipboard getClipboard() {
		return clipboard;
	}
	
	
}