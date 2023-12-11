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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.avaje.ebean.Ebean;

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
	Map<String, User> administrativUsers;

	public static UserDB getInstance() {
		if (UserDB.db == null) {
			UserDB.db = new UserDB();
		}
		return UserDB.db;
	}

	private UserDB() {
		salt = Play.application().configuration().getString("regal-api.admin-salt");
		createAdministrativUsers();
	}

	private void createAdministrativUsers() {
		String pwd =
				Play.application().configuration().getString("regal-api.admin-hash");
		String adminPrefix =
				Play.application().configuration().getString("regal-api.admin-prefix");
		if (adminPrefix == null || adminPrefix.isEmpty()) {
			adminPrefix = "toscience";
		}
		administrativUsers = new HashMap<>();
		User admin =
				createAdministrativUser(adminPrefix + "-admin", pwd, Role.ADMIN);
		administrativUsers.put(admin.getUsername(), admin);
		User editor =
				createAdministrativUser(adminPrefix + "-editor", pwd, Role.EDITOR);
		administrativUsers.put(editor.getUsername(), editor);
		User reader =
				createAdministrativUser(adminPrefix + "-reader", pwd, Role.READER);
		administrativUsers.put(reader.getUsername(), reader);
		User subscriber = createAdministrativUser(adminPrefix + "-subscriber", pwd,
				Role.SUBSCRIBER);
		administrativUsers.put(subscriber.getUsername(), subscriber);
		User remote =
				createAdministrativUser(adminPrefix + "-remote", pwd, Role.REMOTE);
		administrativUsers.put(remote.getUsername(), remote);
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
		if (administrativUsers.containsKey(username)) {
			return administrativUsers.get(username);
		} else {
			return Ebean.find(User.class).where().eq("username", username)
					.findUnique();
		}
	}

	public boolean isLoggedIn(Context ctx) {
		return (getUser(ctx) != null);
	}

	public User getUser(Context ctx) {
		return getUser(ctx.session().get("username"));
	}

	public boolean isUser(String username) {
		return administrativUsers.containsKey(username) || Ebean.find(User.class)
				.where().eq("username", username).findUnique() != null;
	}

	public boolean isValid(String username, String password) {
		return ((username != null) && (password != null) && isUser(username)
				&& getUser(username).getPassword().equals(getSaltedPassword(password)));
	}

	public void addUser(User user, String password) {
		user.setPassword(getSaltedPassword(password));
		User oldUser = getUser(user.getUsername());
		if (oldUser != null) {
			Ebean.update(user);
		} else {
			user.setCreated("" + new Date().getTime());
			Ebean.save(user);
		}
	}
}
