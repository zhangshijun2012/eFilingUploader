package com.sinosoft.util.timer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sinosoft.util.json.JSONObject;

/**
 * 定时器任务
 * 
 * @author LuoGang
 * 
 */
public abstract class TimerSchedulerTask extends TimerTask {
	/** 日志记录对象 */
	public static final Logger logger = LoggerFactory.getLogger(TimerSchedulerTask.class);

	/** 一个小时的毫秒数 */
	public static final long HOUR = 60 * 60 * 1000L;
	/** 半天的毫秒数 */
	public static final long DAY_HALF = HOUR * 12;
	/** 一天的毫秒数 */
	public static final long DAY = HOUR * 24;

	public TimerSchedulerTask() {
		super();
		// 默认使用scheduleAtFixedRate方法进行调用
		scheduleAtFixedRate = true;
	}

	/** 是否使用Timer的scheduleAtFixedRate方法进行调用 */
	protected boolean scheduleAtFixedRate = true;
	/** 任务的第一次调用时间,如果为null则使用delay参数进行调用 */
	protected Date firstTime;
	/** 第一次调用的延迟时间,单位毫秒 */
	protected long delay;
	/** 两次任务之间的间隔时间,单位毫秒 */
	protected long period;
	/** 调用任务的定时器 */
	protected Timer timer;
	/** 运行的次数 */
	protected int times;

	/**
	 * 是否使用默认的Timer对象,即TimerScheduler的timer对象,可用于有依赖关系的定时任务.
	 * 如果A,B两个定时任务需要B在A之后执行,则A,B的useDefaultTimer都应该设为true.
	 * 默认为false
	 */
	protected boolean useDefaultTimer;

	public void run() {
		int times = ++this.times;
		try {
			logger.debug(this.getClass() + "第" + times + "次开始执行!");
			this.execute();
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
			logger.error(this.getClass() + "第" + times + "次执行异常:" + e.getMessage(), e);
		}
	}

	/**
	 * 根据json数据初始化参数
	 * 
	 * @param properties 配置的参数
	 */
	public void initialize(JSONObject properties) {
		// TODO
	}

	/**
	 * 执行具体的任务的方法,子类必须实现
	 */
	protected abstract void execute();

	/**
	 * 使用timer对象启动任务
	 * 
	 * @param timer
	 */
	public void start(Timer timer) {
		this.timer = timer;
		this.start();
	}

	/**
	 * 执行定时器任务
	 */
	private void start() {
		if (this.scheduleAtFixedRate) {
			if (firstTime == null) {
				timer.scheduleAtFixedRate(this, delay, period);
			} else {
				timer.scheduleAtFixedRate(this, firstTime, period);
			}
		} else {
			if (firstTime == null) {
				if (period == 0) timer.schedule(this, delay);
				else timer.schedule(this, delay, period);
			} else {
				if (period == 0) timer.schedule(this, firstTime);
				else timer.schedule(this, firstTime, period);
			}
		}
	}

	public boolean isScheduleAtFixedRate() {
		return scheduleAtFixedRate;
	}

	public void setScheduleAtFixedRate(boolean scheduleAtFixedRate) {
		this.scheduleAtFixedRate = scheduleAtFixedRate;
	}

	public Date getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public Timer getTimer() {
		return timer;
	}

	public int getTimes() {
		return times;
	}

	public boolean isUseDefaultTimer() {
		return useDefaultTimer;
	}

	public void setUseDefaultTimer(boolean useDefaultTimer) {
		this.useDefaultTimer = useDefaultTimer;
	}
}
