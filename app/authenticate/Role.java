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

/**
 * @author Jan Schnasse
 *
 */
public enum Role {
	ADMIN, /* Admin is allowed to trigger all API actions. */
	EDITOR, /*
					 * Editor is allowed to create and modify objects. Everything under
					 * /resource is visible for the editor
					 */
	READER, /*
					 * The reader visits the system from a certain IP The reader can read
					 * all data flagged with "public","restricted","remote" The reader is
					 * not able to perform modifying, creating, or deleting operations
					 */
	SUBSCRIBER, /*
							 * The subscriber behaves like the reader but can als access date
							 * flagged with "single"
							 */
	REMOTE, /*
					 * The remote user behaves like the reader
					 */
	GUEST /*
				 * The anonymous user can read everything flagged with "public".
				 */
}
