package com.sakurawald.timer.timers;

import java.util.ArrayList;

import com.sakurawald.PluginMain;
import com.sakurawald.api.PowerWord_API;
import com.sakurawald.bean.Countdown;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.timer.DefaultPlan;
import com.sakurawald.timer.RobotAbstractTimer;
import com.sakurawald.timer.TimerController;
import com.sakurawald.utils.DateUtil;
import com.sakurawald.utils.NetworkUtil;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;


public class DailySentenceTimer extends DailyTimer implements DefaultPlan {

	private PowerWord_API.Motto todayMotto;

	public DailySentenceTimer(String timerName, long firstTime,
							  long delayTime) {
		super(timerName, firstTime, delayTime);
	}

	public void defaultPlan() {
		todayMotto = PowerWord_API.Motto.getDefaultMotto();
	}

	@Override
	public boolean isPrepareStage() {

		int nowDay = DateUtil.getNowDay();

		if (nowDay != lastPrepareDay) {

			int nowHour = DateUtil.getNowHour();

			// 判断是否是4点，即5点之前
			if (nowHour == 4) {

				int nowMinute = DateUtil.getNowMinute();

				if (55 <= nowMinute && nowMinute <= 59) {

					lastPrepareDay = nowDay;
					return true;
				}
			}

			// 判断是否已经5点了，但是自己还没准备。也就是说，程序是在5点的时候临时运行的
			// 那么就赶快return一个true，临时准备，临时发送。两个阶段一起做
			if (nowHour == 5) {
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

			if (nowHour == 5 && nowMinute <= 10) {
				lastSendDay = nowDay;
				return true;
			}

		}

		return false;
	}

	@Override
	public void prepareStage() {


		sendMsg = "早安，" + DateUtil.getNowYear() + "年" + DateUtil.getNowMonth()
				+ "月" + DateUtil.getNowDay() + "日！";

		/** Function: Countdown. **/
		ArrayList<Countdown> cda = Countdown.getCountdownsByCommands();
		if (FileManager.applicationConfig_File.getSpecificDataInstance().Functions.DailyCountdown.enable
				&& cda.size() != 0) {
			sendMsg = sendMsg + "\n\n●倒计时：";

			/** Append all countdowns. **/
			for (Countdown cd : cda) {
				sendMsg = sendMsg + "\n" + cd.getTodayCountdownMsg();
			}

		}

		/** Function: DailySentence. **/
		defaultPlan();
		try {
			todayMotto = PowerWord_API.getTodayMotto();
		} catch (Exception e) {
			// Do nothing.
		}

		sendMsg = sendMsg.trim() + "\n\n●今日格言：\n" + todayMotto.getContent_cn() + "( "
				+ todayMotto.getContent_en() + " )";

		// Has Translation ?
		if (todayMotto.getTranslation() != null) {
			sendMsg = sendMsg + "\n\n" + "〖解读〗" + todayMotto.getTranslation();
		}

		try {
			// Add Sentence's ShareImg.
			Image uploadImage = ExternalResource.uploadAsImage(NetworkUtil.getInputStream(todayMotto.getFenxiang_img()),
					PluginMain.getCurrentBot().getGroups().stream().findAny().get());
			sendMsg = sendMsg + "\n" + "[mirai:image:" + uploadImage.getImageId() + "]";
		} catch (Exception e) {
			// Do nothing.
		}

		LoggerManager.logDebug("TimerSystem", "Daily Sentence: \n" + sendMsg);
	}

	@Override
	public void sendStage() {
		MessageManager.sendMessageToAllQQFriends(sendMsg);
		MessageManager.sendMessageToAllQQGroups(sendMsg);
	}


}
