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
package helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Globals;

/**
 * 
 * @author Jan Schnasse
 *
 */
public class TaskManager {

	private List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

	/**
	 * register all jobs
	 */
	public void init() {
		play.Logger.info("Taskmanager initialized!");
		if (Globals.heartbeatTask != null && !Globals.heartbeatTask.isEmpty()) {
			play.Logger.info(
					"Register Job: heartbeat. Will run every " + Globals.heartbeatTask);
			addTask("heartbeat",
					() -> play.Logger.debug("Boom: " + new Date().toString()),
					Globals.heartbeatTask);
		}

		if (Globals.webgatherTask != null && !Globals.webgatherTask.isEmpty()) {
			play.Logger.info(
					"Register Job: wabgatherer. Will run every " + Globals.webgatherTask);
			addTask("web gatherer", new Webgatherer(), Globals.webgatherTask);
		}

		if (Globals.urnTask != null && !Globals.urnTask.isEmpty()) {
			play.Logger.info(
					"Register Job: urn allocator. Will run every " + Globals.urnTask);
			addTask("urn allocator", new UrnAllocator(), Globals.urnTask);
		}

		if (Globals.doiTask != null && !Globals.doiTask.isEmpty()) {
			play.Logger.info(
					"Register Job: doi allocator. Will run every " + Globals.doiTask);
			addTask("doi allocator", new DoiAllocator(), Globals.doiTask);
		}
	}

	private void addTask(String name, Runnable r, String cronExpression) {
		try {
			scheduledTasks.add(new ScheduledTask(name, cronExpression, r));
		} catch (Exception e) {
			play.Logger.error("Not able to schedule \"" + name + "\" task", e);
		}
	}

	/**
	 * activate all jobs
	 */
	public void execute() {
		for (ScheduledTask c : scheduledTasks) {
			c.schedule();
		}
	}

	/**
	 * cancel execution of all jobs
	 */
	public void shutdown() {
		for (ScheduledTask c : scheduledTasks) {
			c.cancel();
		}
	}
}
