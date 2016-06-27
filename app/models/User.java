/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package models;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public interface User {

	/**
	 * @param username username
	 * @param password password
	 * @return null if credentials are not valid
	 */
	public abstract User authenticate(String username, String password);

	/**
	 * @return the role of the user
	 */
	public abstract String getRole();

	/**
	 * @param role a user role that exists in the system
	 */
	public abstract void setRole(String role);

}