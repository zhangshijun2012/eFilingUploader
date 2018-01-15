package com.sinosoft.util.timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sinosoft.util.DateHelper;
import com.sinosoft.util.Helper;
import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.SystemHelper;
import com.sinosoft.util.json.JSONObject;

/**
 * 定时任务
 * 
 * @author LuoGang
 * 
 */
public class TimerScheduler {
	/** 日志记录对象 */
	public static final Logger logger = LoggerFactory.getLogger(TimerScheduler.class);

	protected TimerScheduler() {
		timer = new Timer();
		timers = new ArrayList<Timer>();
		timers.add(timer);
		tasks = new ArrayList<TimerSchedulerTask>();
	}

	/** 任务调用的默认timer对象 */
	private Timer timer;
	/** 所有被调用的任务 */
	private List<Timer> timers;
	/** 所有被调用的任务 */
	private List<TimerSchedulerTask> tasks;

	/**
	 * 调度任务
	 * 
	 * @param task
	 */
	public void schedule(TimerSchedulerTask task) {
		logger.info("调度任务:" + task.getClass());
		tasks.add(task);
		if (task.isUseDefaultTimer()) {
			task.start(timer);
		} else {
			Timer timer = new Timer();
			timers.add(timer);
			task.start(timer);
		}
	}

	/**
	 * 取消任务.无论如何,此方法会调用task.cancel()
	 * 
	 * @param task
	 * @return true 如果此task是当前对象调度的,否则返回false
	 */
	public boolean cancel(TimerSchedulerTask task) {
		task.cancel();
		if (!tasks.remove(task)) {
			return false;
		}
		if (!task.isUseDefaultTimer()) {
			// 不是使用默认定时器,则需要取消timer
			Timer timer = task.getTimer();
			timers.remove(timer);
			timer.cancel();
		}
		return true;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		if (this.timer != null) this.timer.cancel(); // 取消定时器的调度
		int count = tasks.size();
		while (--count >= 0) {
			TimerSchedulerTask task = tasks.get(count);
			if (task.getTimer() == this.timer) {
				// 删除次timer对应的task
				tasks.remove(task);
			}
		}
		this.timer = timer;
		this.timers.remove(0);
		this.timers.add(0, this.timer);
	}

	/** 静态变量,外部调用时,可使用此对象进行处理,无需新建实例 */
	private static TimerScheduler scheduler = new TimerScheduler();

	/** 获取实例 */
	public static TimerScheduler getInstance() {
		return scheduler;
	}

	/** 配置文件中的关键字 */
	public static final String PROPERTY_KEY = "timer.tasks";

	/**
	 * 从配置文件中加载所有定时任务并执行.
	 * 读取在config.properties中配置的timer.tasks
	 * 
	 * @see #PROPERTY_KEY
	 */
	public static void start() {
		String tasks = SystemHelper.getProperty(PROPERTY_KEY);
		logger.info("配置timer.tasks=" + tasks);
		if (Helper.isEmpty(tasks)) return;
		String[] taskClasses = tasks.trim().split(",");
		TimerSchedulerTask task;
		int index = 0;
		for (String taskClass : taskClasses) {
			try {
				logger.info("处理timer.tasks." + (index++) + "=" + taskClass);
				task = create(taskClass.trim());
				scheduler.schedule(task);
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * 创建一个定时器任务
	 * 
	 * @param taskClass
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private static TimerSchedulerTask create(String taskClass) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		TimerSchedulerTask task = (TimerSchedulerTask) Class.forName(taskClass).newInstance();
		String values = SystemHelper.getProperty(PROPERTY_KEY + "." + taskClass);
		if (!StringHelper.isEmpty(values)) {
			logger.info(taskClass + " = " + values);
			JSONObject properties = new JSONObject();
			properties.read(values.trim());
			// 处理可以注入的属性
			Object value = properties.get("period");
			if (value != null) {
				task.setPeriod(NumberHelper.longValue(value));
			}
			value = properties.get("delay");
			if (properties.get("delay") != null) {
				task.setDelay(NumberHelper.longValue(value));
			}
			value = properties.get("scheduleAtFixedRate");
			if (value != null) {
				task.setScheduleAtFixedRate(Boolean.parseBoolean(value.toString().trim()));
			}
			String firstTime = StringHelper.trim(properties.get("firstTime"));
			if (firstTime != null) {
				Date date;
				if ("now".equalsIgnoreCase(firstTime) || "new Date()".equals(firstTime)) date = new Date();
				else if ("today".equalsIgnoreCase(firstTime)) date = DateHelper.clear(firstTime);
				else date = DateHelper.parse(firstTime);
				if (date != null) task.setFirstTime(date);
			}

			String useDefaultTimer = StringHelper.trim(properties.get("useDefaultTimer"));
			if (!StringHelper.isEmpty(useDefaultTimer)) {
				task.setUseDefaultTimer(StringHelper.parseBoolean(useDefaultTimer));
			}

			task.initialize(properties);
		}
		return task;
	}

	public List<Timer> getTimers() {
		return timers;
	}

	public List<TimerSchedulerTask> getTasks() {
		return tasks;
	}

	public static void main(String[] args) {
		start();

		// TimerSchedulerTask task = new TimerSchedulerTask() {
		// @Override
		// public void run() {
		// System.out.println(this.getPeriod());
		// }
		// };
		//
		// task.setFirstTime(new Date());
		// task.setPeriod(10 * 1000);
		// scheduler.schedule(task);
	}
}
