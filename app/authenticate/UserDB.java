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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import play.Play;
import play.mvc.Http.Context;

/**
 * 
 * @author Jan Schnasse
 *
 */
public class UserDB {

	private static UserDB db = null;

	private String salt;
	Map<String, User> users;

	public static UserDB getInstance() {
		if (UserDB.db == null) {
			UserDB.db = new UserDB();
		}
		return UserDB.db;
	}

	private UserDB() {
		salt = Play.application().configuration().getString("regal-api.admin-salt");
		String pwd =
				Play.application().configuration().getString("regal-api.admin-hash");
		users = new HashMap<>();
		User admin = createAdministrativUser("edoweb-admin", pwd, Role.ADMIN);
		users.put(admin.getUsername(), admin);
		User editor = createAdministrativUser("edoweb-editor", pwd, Role.EDITOR);
		users.put(editor.getUsername(), editor);
		User reader = createAdministrativUser("edoweb-reader", pwd, Role.READER);
		users.put(reader.getUsername(), reader);
		User subscriber =
				createAdministrativUser("edoweb-subscriber", pwd, Role.SUBSCRIBER);
		users.put(subscriber.getUsername(), subscriber);
		User remote = createAdministrativUser("edoweb-remote", pwd, Role.REMOTE);
		users.put(remote.getUsername(), remote);
	}

	private static User createAdministrativUser(String name, String pwd,
			Role role) {
		User user = new User();
		user.setPassword(pwd);
		user.setUsername(name);
		user.setRole(role);
		return user;
	}

	private String getSaltedPassword(String password) {
		return DigestUtils.sha256Hex(salt + password);
	}

	public User getUser(String username) {
		return users.get(username);
	}

	public boolean isLoggedIn(Context ctx) {
		return (getUser(ctx) != null);
	}

	public User getUser(Context ctx) {
		return getUser(ctx.session().get("username"));
	}

	public boolean isUser(String username) {
		return users.containsKey(username);
	}

	public boolean isValid(String usernane, String password) {
		return ((usernane != null) && (password != null) && isUser(usernane)
				&& getUser(usernane).getPassword().equals(getSaltedPassword(password)));
	}
}
