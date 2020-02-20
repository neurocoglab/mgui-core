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

package mgui.datasources.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import cern.colt.Arrays;

import mgui.command.CommandInstance;
import mgui.datasources.security.SecureDataSourceFunctions;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.security.StringEncrypter.EncryptionException;

/*****************************************************
 * Utility for defining a JDBC driver with login and encrypted password.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceCommands extends CommandInstance {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		//System.out.println("Parameters: " + Arrays.toString(args));
		DataSourceCommands instance = new DataSourceCommands();
		
		instance.execute(args);
		
		System.out.println("Success. Operation took " + instance.getInstanceTimer() + "ms.");
		
	}

	
	@Override
	protected boolean run_command(String command) {
		
		boolean success = false;
		
		if (command.equals("define-jdbc-driver")){
			success = define_jdbc_driver();
			}
		
		return success;
	}

	
	/********************************************************
	 * Defines a JDBC driver file (*.driver) with login and encrypted password. If the file exists, it will be
	 * altered; otherwise it will be created.
	 * 
	 * <p>Parameters [required]
	 * <ul>
	 * <li>file_name			The file name of the driver; defines the file name as [file_name].driver
	 * <li>login				Login for the driver.
	 * <li>password				Plain text password for the driver.
	 * </ul>
	 * 
	 * <p>Parameters [optional]
	 * <ul>
	 * <li>driver_name			The name of the driver; defaults to the file name for new files; otherwise keeps the old 
	 * 							name.
	 * <li>driver_url 					The JDBC-specific url specifying the location of the data source
	 * <li>driver_class			The full path to the mgui driver class. Defaults to 'mgui.datasources.DataSourceDriver'
	 * 							for new files; otherwise keeps the old class.
	 * </ul>
	 * 
	 */
	public boolean define_jdbc_driver(){
		
		String file_name = parameters.get("file_name");
		if (file_name == null){
			InterfaceSession.log("define_jdbc_driver: Parameter 'file_name' is not optional.", LoggingType.Errors);
			return false;
			}
		
		file_name = "/data_sources/" + file_name + ".driver";
		java.net.URL file_url = InterfaceEnvironment.class.getResource(file_name);
		
		File file = null;
		
		String login = parameters.get("login");
		if (login == null){
			InterfaceSession.log("define_jdbc_driver: Parameter 'login' is not optional.", LoggingType.Errors);
			return false;
			}
		
		String password = parameters.get("password");
		if (password == null){
			InterfaceSession.log("define_jdbc_driver: Parameter 'password' is not optional.", LoggingType.Errors);
			return false;
			}
		
		String encrypted = null;
		try{
			encrypted = SecureDataSourceFunctions.getEncryptedPassword(password.toCharArray());
		}catch (EncryptionException ex){
			InterfaceSession.log("define_jdbc_driver: Could not encrypt password: " + ex.getMessage(), LoggingType.Errors);
			return false;
			}
		
		String driver_url = "jdbc:";
		String driver_name = file_name;
		String driver_class = "mgui.datasources.DataSourceDriver";
		
		if (file_url != null){
			try{
				BufferedReader reader = new BufferedReader(new FileReader(file_url.getFile()));
				file = new File(file_url.getFile());
				
				String line = reader.readLine();
				String[] parts = line.split(" ");
				if (parts.length > 0)
					driver_class = parts[0];
				if (parts.length > 1)
					driver_url = parts[1];
				if (parts.length > 2)
					driver_name = parts[2];
				
				reader.close();
				
			}catch (IOException ex){
				InterfaceSession.log("define_jdbc_driver: Problem reading driver file '" + file.getAbsolutePath() + "':" + ex.getMessage());
				return false;
				}
		}else{
			file = new File(file_name);
			}
		
		String param = parameters.get("driver_name");
		if (param != null)
			driver_name = param;
		param = parameters.get("driver_url");
		if (param != null)
			driver_url = param;
		param = parameters.get("driver_class");
		if (param != null)
			driver_class = param;
		
		// Write data
		try{
			if (file.exists() && !file.delete()){
				InterfaceSession.log("define_jdbc_driver: Could not delete existing driver file '" + file.getAbsolutePath() + "'.");
				return false;
				}
			
			if (!file.createNewFile()){
				InterfaceSession.log("define_jdbc_driver: Problem creating driver file '" + file.getAbsolutePath() + "'.");
				return false;
				}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(driver_class + " " + driver_url + " " + driver_name + " " + login + " " + encrypted);
			
			writer.close();
			
		}catch (IOException ex){
			InterfaceSession.log("define_jdbc_driver: Problem reading driver file '" + file.getAbsolutePath() + "':" + ex.getMessage());
			return false;
			}
		
		return true;
		
	}
	
	
	
}