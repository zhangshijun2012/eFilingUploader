package com.sinosoft.filenet;

import java.io.File;

import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.json.JSONObject;
import com.sinosoft.util.timer.TimerSchedulerTask;

/**
 * 清理FileNet的缓存图片.每次清理1个月以上的文件
 * 
 * @author LuoGang
 * 
 */
public class CacheCleanerTimerTask extends TimerSchedulerTask {

	/**
	 * 缓存图片保留时间,单位毫秒,如果<=0则表示永久保留
	 */
	private long lifetime;

	@Override
	protected void execute() {
		logger.debug("缓存文件存留时间(<=0表示不清理缓存):" + lifetime);
		if (lifetime <= 0) {
			return;
		}
		FileIndexAction.initPreviewCacheDir();
		File cache = FileIndexAction.getPreviewCacheDir();
		logger.debug("清理缓存目录:" + cache.getAbsolutePath());
		int count = clean(cache);
		logger.debug("清理缓存成功,共删除" + count + "个文件和目录.");
	}

	/**
	 * 清理缓存文件或目录
	 * 
	 * @param cache 缓存文件或目录
	 * @return 被删除的文件及目录数量
	 */
	public int clean(File cache) {
		long now = System.currentTimeMillis();
		if (now - cache.lastModified() < lifetime) return 0;
		int count = 0;
		if (cache.isFile()) {
			if (cache.delete()) {
				logger.debug("删除缓存文件:" + cache.getAbsolutePath());
				count++;
			}
		} else if (cache.isDirectory()) {
			logger.debug("清理缓存目录:" + cache.getAbsolutePath());
			File[] caches = cache.listFiles();
			for (File c : caches) {
				count += clean(c);
			}
			if (cache.delete()) {
				count++;
				logger.debug("删除缓存目录:" + cache.getAbsolutePath());
			}
		}
		return count;
	}

	@Override
	public void initialize(JSONObject properties) {
		// TODO Auto-generated method stub
		super.initialize(properties);

		lifetime = NumberHelper.longValue(properties.get("lifetime"));
	}

	public long getLifetime() {
		return lifetime;
	}

	public void setLifetime(long lifetime) {
		this.lifetime = lifetime;
	}
}
