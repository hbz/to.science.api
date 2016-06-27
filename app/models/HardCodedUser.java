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

import controllers.MyController;
import play.Play;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public class HardCodedUser implements User {
	String role = null;

	/**
	 * Creates a new HardCoded-User and checks if passwords are set
	 * 
	 */
	public HardCodedUser() {
		String adminPwd;
		String editorPwd;
		String readerPwd;
		String subscriberPwd;
		String remotePwd;
		adminPwd = Play.application().configuration()
				.getString("regal-api.admin-password");
		editorPwd = Play.application().configuration()
				.getString("regal-api.editor-password");
		readerPwd = Play.application().configuration()
				.getString("regal-api.reader-password");
		subscriberPwd = Play.application().configuration()
				.getString("regal-api.subscriber-password");
		remotePwd = Play.application().configuration()
				.getString("regal-api.remote-password");

		if (adminPwd == null || editorPwd == null || readerPwd == null
				|| subscriberPwd == null || remotePwd == null)
			throw new RuntimeException(
					"Please set passwords for all roles in application.conf");
	}

	@Override
	public User authenticate(String username, String password) {
		role = null;
		if (username == null || username.isEmpty()) {
			role = MyController.ANONYMOUS_ROLE;
		} else if (MyController.ANONYMOUS_ROLE.equals(username)) {
			role = MyController.ANONYMOUS_ROLE;
		} else if (password == null || password.isEmpty()) {
			role = MyController.ANONYMOUS_ROLE;
		} else if (MyController.ADMIN_ROLE.equals(username) && password.equals(Play
				.application().configuration().getString("regal-api.admin-password"))) {
			role = MyController.ADMIN_ROLE;
		} else if (MyController.EDITOR_ROLE.equals(username)
				&& password.equals(Play.application().configuration()
						.getString("regal-api.editor-password"))) {
			role = MyController.EDITOR_ROLE;
		} else if (MyController.READER_ROLE.equals(username)
				&& password.equals(Play.application().configuration()
						.getString("regal-api.reader-password"))) {
			role = MyController.READER_ROLE;
		} else if (MyController.SUBSCRIBER_ROLE.equals(username)
				&& password.equals(Play.application().configuration()
						.getString("regal-api.subscriber-password"))) {
			role = MyController.SUBSCRIBER_ROLE;
		} else if (MyController.REMOTE_ROLE.equals(username)
				&& password.equals(Play.application().configuration()
						.getString("regal-api.remote-password"))) {
			role = MyController.REMOTE_ROLE;
		}
		if (role == null)
			throw new RuntimeException("No valid credentials!");
		play.Logger.debug("You are authorized with role " + role);
		return this;
	}

	@Override
	public String getRole() {
		return role;
	}

	@Override
	public void setRole(String role) {
		this.role = role;
	}
}
