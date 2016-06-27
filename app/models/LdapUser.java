package models;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import play.Play;

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

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 * @see <a href=
 *      "http://digitalsanctum.com/2012/06/07/basic-authentication-in-the-play-framework-using-custom-action-annotation/">
 *      digitalsanctum</a> and http://stackoverflow.com/a/4412867/1485527
 */
@SuppressWarnings("javadoc")
public class LdapUser implements User {
	String role = null;
	String ldapServer = null;

	public LdapUser() {
		ldapServer =
				Play.application().configuration().getString("regal-api.ldapServer");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see models.User#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public User authenticate(String username, String password) {
		InitialDirContext context = null;
		try {

			Properties properties = new Properties();
			properties.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			properties.put(Context.PROVIDER_URL, ldapServer);
			properties.put(Context.REFERRAL, "ignore");
			String dn = dnFromUser(username);
			properties.put(Context.SECURITY_PRINCIPAL, dn);
			properties.put(Context.SECURITY_CREDENTIALS, password);
			/*
			 * the next call ends in an exception if credentials are not valid
			 */

			context = new InitialDirContext(properties);
			String group = groupFromUser(username);
			role = group;
		} catch (Exception e) {
			// play.Logger.debug("Credentials not valid proceed as anonymous");
		} finally {
			if (context != null)
				try {
					context.close();
				} catch (NamingException e) {
					play.Logger.error(
							"This one is serious! LDAP connection not closed. This can result in to many open connections.",
							e);
				}
		}
		return this;
	}

	private String dnFromUser(String username) throws NamingException {
		InitialDirContext context = null;
		try {
			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			props.put(Context.PROVIDER_URL, ldapServer);
			props.put(Context.REFERRAL, "ignore");
			context = new InitialDirContext(props);
			SearchControls ctrls = new SearchControls();
			ctrls.setReturningAttributes(
					new String[] { "givenName", "sn", "gidNumber", "cn", "ou", "dc" });
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<SearchResult> answers =
					context.search("dc=edoweb-rlp,dc=de", "(cn=" + username + ")", ctrls);
			SearchResult result = answers.next();
			return result.getNameInNamespace();
		} finally {
			if (context != null)
				try {
					context.close();
				} catch (NamingException e) {
					play.Logger.error(
							"This one is serious! LDAP connection not closed. This can result in to many open connections.",
							e);
				}
		}
	}

	private String groupFromUser(String username) throws NamingException {
		InitialDirContext context = null;
		try {
			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			props.put(Context.PROVIDER_URL, ldapServer);
			props.put(Context.REFERRAL, "ignore");
			context = new InitialDirContext(props);
			SearchControls ctrls = new SearchControls();
			ctrls.setReturningAttributes(
					new String[] { "givenName", "sn", "gidNumber", "cn", "ou", "dc" });
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<SearchResult> answers =
					context.search("dc=edoweb-rlp,dc=de", "(cn=" + username + ")", ctrls);
			Attributes result = answers.next().getAttributes();
			String groupNumber = result.get("gidNumber").get().toString();
			ctrls.setReturningAttributes(new String[] { "cn" });
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			answers = context.search("ou=groups,dc=edoweb-rlp,dc=de",
					"(gidNumber=" + groupNumber + ")", ctrls);
			result = answers.next().getAttributes();
			String groupName = result.get("cn").get().toString();
			return groupName;
		} finally {
			if (context != null)
				try {
					context.close();
				} catch (NamingException e) {
					play.Logger.error(
							"This one is serious! LDAP connection not closed. This can result in to many open connections.",
							e);
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see models.User#getRole()
	 */
	@Override
	public String getRole() {
		return role;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see models.User#setRole(java.lang.String)
	 */
	@Override
	public void setRole(String role) {
		this.role = role;
	}

}
