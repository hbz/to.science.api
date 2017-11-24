/*
 * Copyright 2017 hbz NRW (http://www.hbz-nrw.de/)
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
package authenticate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import play.data.validation.ValidationError;

/**
 * @author Jan Schnasse
 *
 */
public class User {

	String username;
	String password;
	String email;
	Role role;
	Date created;

	public User() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * Called automatically in the controller by bindFromRequest().
	 * 
	 * @return Null if valid, or a List[ValidationError].
	 */
	public List<ValidationError> validate() {

		List<ValidationError> errors = new ArrayList<>();

		if (!models.Globals.users.isValid(username, password)) {
			errors.add(new ValidationError("username", ""));
			errors.add(new ValidationError("password", ""));
		}

		return (errors.size() > 0) ? errors : null;
	}
}
