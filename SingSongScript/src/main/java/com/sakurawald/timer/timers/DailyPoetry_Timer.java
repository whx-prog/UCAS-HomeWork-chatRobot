package com.sakurawald.timer.timers;

import com.sakurawald.api.BaiDuHanYu_API;
import com.sakurawald.api.JinRiShiCi_API;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.timer.DefaultPlan;
import com.sakurawald.timer.RobotAbstractTimer;
import com.sakurawald.timer.TimerController;
import com.sakurawald.utils.DateUtil;

public class DailyPoetry_Timer extends DailyTimer {

	private JinRiShiCi_API.Poetry todayPoetry = null;
	private static DailyPoetry_Timer instance = null;

	public DailyPoetry_Timer(String timerName, long firstTime, long delayTime) {
		super(timerName, firstTime, delayTime);
	}

	public static DailyPoetry_Timer getInstance() {
		if (instance == null) {
			instance = new DailyPoetry_Timer(
					"DailyPoetry", 1000 * 5, 1000 * 60);
		}
		return instance;
	}

	@Override
	public boolean isPrepareStage() {

		int nowDay = DateUtil.getNowDay();

		if (nowDay != lastPrepareDay) {

			int nowHour = DateUtil.getNowHour();

			if (nowHour == 20) {

				int nowMinute = DateUtil.getNowMinute();

				if (55 <= nowMinute && nowMinute <= 59) {
					lastPrepareDay = nowDay;
					return true;
				}
			}

			if (nowHour == 21) {
				lastPrepareDay = nowDay;
				return true;
			}

		}

		return false;
	}

	@Override
	public boolean isSendStage() {

		int nowDay = DateUtil.getNowDay();

		if (nowDay != lastSendDay) {

			int nowHour = DateUtil.getNowHour();
			int nowMinute = DateUtil.getNowMinute();

			if (nowHour == 21 && nowMinute <= 10) {
				lastSendDay = nowDay;
				return true;
			}

		}

		return false;
	}

	@Override
	public void prepareStage() {
		/** 准备sendMsg **/
		todayPoetry = BaiDuHanYu_API.getRandomPoetry();


		sendMsg = "晚安，" + DateUtil.getNowYear() + "年"
				+ DateUtil.getNowMonth() + "月" + DateUtil.getNowDay()
				+ "日~\n\n" + "●今日诗词\n" + "〖标题〗" + todayPoetry.getTitle() + "\n" + "〖作者〗"
				+ "（" + todayPoetry.getDynasty()+ "） " + todayPoetry.getAuthor() + "\n" + "〖诗词〗\n"
				+ todayPoetry.getContent();

		LoggerManager.logDebug("TimerSystem", "DailyPoetry: \n" + sendMsg);
	}

	public JinRiShiCi_API.Poetry getTodayPoetry() {
		return todayPoetry;
	}

	@Override
	public void sendStage() {
		MessageManager.sendMessageToAllQQFriends(sendMsg);
		MessageManager.sendMessageToAllQQGroups(sendMsg);
	}

}
