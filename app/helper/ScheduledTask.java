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

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import akka.actor.Cancellable;
import play.libs.Akka;
import play.libs.Time.CronExpression;
import scala.concurrent.duration.FiniteDuration;

/**
 * @author Jan Schnasse
 *
 */
public class ScheduledTask implements Cancellable {
	private CronExpression cronExpression;
	private String name;
	private boolean isCanceled;
	private Runnable task;

	/**
	 * @param name a name for the task
	 * @param cronExpr a cron expr as described in
	 *          https://www.playframework.com/documentation
	 *          /2.3.x/api/java/play/libs/Time.CronExpression.html
	 * @param task a runnable, that will be executed at specified time
	 * @throws ParseException
	 */
	public ScheduledTask(String name, String cronExpr, Runnable task)
			throws ParseException {
		cronExpression = new CronExpression(cronExpr);
		this.name = name;
		this.task = task;
	}

	/**
	 * will execute the actual task and schedule the next execution
	 */
	public void schedule() {
		if (!isCanceled) {
			try {
				Date nextValidTimeAfter =
						cronExpression.getNextValidTimeAfter(new Date());
				FiniteDuration d = FiniteDuration.create(
						nextValidTimeAfter.getTime() - System.currentTimeMillis(),
						TimeUnit.MILLISECONDS);

				play.Logger.info(name + " next run at " + nextValidTimeAfter);

				Akka.system().scheduler().scheduleOnce(d, () -> {
					task.run();
					schedule();
				} , Akka.system().dispatcher());
			} catch (Exception e) {
				play.Logger.error("", e);
			}
		}
	}

	@Override
	public boolean cancel() {
		isCanceled = true;
		return isCanceled;
	}

	@Override
	public boolean isCancelled() {
		return isCanceled;
	}

}
