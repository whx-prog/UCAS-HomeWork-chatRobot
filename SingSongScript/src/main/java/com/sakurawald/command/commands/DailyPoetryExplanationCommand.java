package com.sakurawald.command.commands;


import com.sakurawald.api.JinRiShiCi_API;
import com.sakurawald.command.RobotCommand;
import com.sakurawald.command.RobotCommandChatType;
import com.sakurawald.command.RobotCommandUser;
import com.sakurawald.files.FileManager;
import com.sakurawald.utils.DateUtil;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.timer.timers.DailyPoetry_Timer;
import net.mamoe.mirai.message.data.MessageChain;

public class DailyPoetryExplanationCommand extends RobotCommand {

	public DailyPoetryExplanationCommand(String rule) {
		super(rule);
		getRange().add(RobotCommandChatType.FRIEND_CHAT);
		getRange().add(RobotCommandChatType.GROUP_TEMP_CHAT);
		getRange().add(RobotCommandChatType.GROUP_CHAT);
		getRange().add(RobotCommandChatType.STRANGER_CHAT);

		getUser().add(RobotCommandUser.NORMAL_USER);
		getUser().add(RobotCommandUser.GROUP_ADMINISTRATOR);
		getUser().add(RobotCommandUser.GROUP_OWNER);
		getUser().add(RobotCommandUser.BOT_ADMINISTRATOR);
	}

	@Override
	public void runCommand(int msgType, int time, long fromGroup, long fromQQ, MessageChain messageChain) {

		if (!FileManager.applicationConfig_File.getSpecificDataInstance().Functions.DailyPoetry.explanation_Enable) {
			MessageManager.sendMessageBySituation(fromGroup, fromQQ,
					FileManager.applicationConfig_File.getSpecificDataInstance().Functions.FunctionManager.functionDisableMsg);
			return;
		}

		if (DailyPoetry_Timer.getInstance().getTodayPoetry() == null) {
			MessageManager.sendMessageBySituation(fromGroup, fromQQ,
					"很抱歉，目前暂时没有任何诗词可以解读，请稍后再试吧.");
			return;
		}

		JinRiShiCi_API.Poetry targetPoetry = DailyPoetry_Timer.getInstance().getTodayPoetry();

		String sendMsg;
		sendMsg = "诗词解读，"
				+ DateUtil.getNowYear()
				+ "年"
				+ DateUtil.getNowMonth()
				+ "月"
				+ DateUtil.getNowDay()
				+ "日！\n\n"
				+ "●今日诗词\n"
				+ "〖标题〗"
				+ targetPoetry.getTitle()
				+ "\n"
				+ "〖作者〗"
				+ "（"
				+ targetPoetry.getDynasty()
				+ "） "
				+ targetPoetry.getAuthor()
				+ "\n"
				+ "〖作者简介〗\n"
				+ targetPoetry.getAuthorIntroduction()
				+ "\n"
				+ "〖译文〗\n"
				+ targetPoetry.getTranslation()
				+ "\n"
				+ "〖注释〗\n"
				+ targetPoetry.getNote() + "\n";
		;

		sendMsg = sendMsg.trim();

		/** 字数检测 **/
		String defaultMsg = "诗词解读，" + DateUtil.getNowYear() + "年"
				+ DateUtil.getNowMonth() + "月" + DateUtil.getNowDay()
				+ "日！\n\n" + "●今日诗词\n" + "〖链接〗由于本次诗词解读文本过长，请直接点击链接查看："
				+ "\n" + "https://hanyu.baidu.com/";

		sendMsg = MessageManager.checkLengthAndModifySendMsg(sendMsg, defaultMsg);

		/** 发送sendMsg **/
		MessageManager.sendMessageBySituation(fromGroup, fromQQ, sendMsg);
	}

}
