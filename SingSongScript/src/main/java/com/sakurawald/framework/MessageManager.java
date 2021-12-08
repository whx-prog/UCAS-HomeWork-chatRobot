package com.sakurawald.framework;

import java.io.File;
import java.io.IOException;

import com.sakurawald.PluginMain;
import com.sakurawald.api.MusicPlatAPI;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import io.github.mzdluo123.silk4j.AudioUtils;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

//用于管理Message的类
public class MessageManager {

	public static String checkLengthAndModifySendMsg(String sendMsg) {
		return checkLengthAndModifySendMsg(sendMsg, "很抱歉，本次发送的字数超过上限，已取消发送！\n字数："
				+ sendMsg.length());
	}

	public static String checkLengthAndModifySendMsg(String sendMsg, String defaultMsg) {

		if (sendMsg == null) {
			return null;
		}

		LoggerManager.logDebug("SendSystem",
				"checkSendMsgLength() -> length = " + sendMsg.length());

		if (sendMsg.length() >= FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.sendMsgMaxLength) {
			return defaultMsg;
		} else {
			return sendMsg;
		}
	}

	public static String transSendMsgSpecialCode(String message) {
		return message.replace("#SPACE", " ")
				.replace("#space", " ").replace("#ENTER", "\n")
				.replace("#enter", "\n");
	}

	public static void sendMessageBySituation(long fromGroup, long fromQQ,
											 String msg) {

		LoggerManager.logDebug("SendSystem", "sendBySituation(): fromGroup = " + fromGroup
				+ ", fromQQ = " + fromQQ);

		// 发送目标: QQ群 ?
		if (fromGroup != -1) {

			// 发送目标: QQ群的指定成员 ?
			if (fromQQ != -1) {
				Member member = PluginMain.getCurrentBot().getGroup(fromGroup)
						.get(fromQQ);
				MessageManager.sendMessageToQQGroup(fromGroup, MessageUtils
						.newChain(new At(member.getId())).plus(MiraiCode.deserializeMiraiCode("\n" + msg)));
			} else {
				MessageManager.sendMessageToQQGroup(fromGroup, MessageUtils
						.newChain(new PlainText(msg)));
			}

		} else {
			// 发送目标: QQ好友 ?
			if (PluginMain.getCurrentBot().getFriends().contains(fromQQ)) {
				MessageManager.sendMessageToQQFriend(fromQQ,
						MiraiCode.deserializeMiraiCode(msg));
				return;
			}

			// 发送目标: 陌生人?
			if (PluginMain.getCurrentBot().getStrangers().contains(fromQQ)) {
				MessageManager.sendMessageToStranger(fromQQ,
						MiraiCode.deserializeMiraiCode(msg));
				return;
			}

			// Report Error.
			PluginMain.getInstance().getLogger().error("sendMessageBySituation(): can't find send target -> fromGroup = " + fromGroup + ", fromQQ = " + fromQQ);
		}


	}


	public static void sendMessageToQQFriend(long QQ,
											MessageChain messageChain) {

		LoggerManager.logDebug("SendSystem", "sendMessageToQQFriend: " + QQ);
		Friend friend = PluginMain.getCurrentBot().getFriend(QQ);
		friend.sendMessage(messageChain);
	}

	public static void sendMessageToStranger(long QQ,
											MessageChain messageChain) {

		LoggerManager.logDebug("SendSystem", "sendMessageToStranger: " + QQ);

		Stranger stranger = PluginMain.getCurrentBot().getStranger(QQ);
		stranger.sendMessage(messageChain);

	}

	public static void sendMessageToStranger(long QQ,
											String msg) {
		sendMessageToStranger(QQ, MiraiCode.deserializeMiraiCode(msg));
	}

	public static void sendMessageToQQFriend(long QQ,
											String msg) {
		/** 对发送的文本进行字数检测 **/
		msg = checkLengthAndModifySendMsg(msg);

		sendMessageToQQFriend(QQ, MiraiCode.deserializeMiraiCode(msg));
	}


	public static void sendDelay(boolean isSendToGroups) {

		long delayTimeMS = 0;

		if (isSendToGroups) {
			if (FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.SendDelay.SendToGroups.enable) {
				delayTimeMS = FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.SendDelay.SendToGroups.delayTimeMS;
			}
		} else {
			if (FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.SendDelay.SendToFriends.enable) {
				delayTimeMS = FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.SendDelay.SendToFriends.delayTimeMS;
			}
		}

		if (delayTimeMS == 0) return;

		LoggerManager.logDebug("GuardSystem", "Send Delay" + delayTimeMS + "MS~");
		try {
			Thread.sleep(delayTimeMS);
		} catch (InterruptedException e) {
			LoggerManager.reportException(e);
		}

	}


	public static void sendMessageToAllQQFriends(String msg) {

		LoggerManager.logDebug("SendSystem", "sendMessageToAllQQFriends: totally "
				+ BotManager.getAllQQFriends().getSize() + " friends.");

		/** 机器人行为核心控制 **/
		if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.RobotControl.forceCancel_FriendMessage) {
			LoggerManager.logDebug("SendSystem", "Force Cancel Send!");
			return;
		}

		ContactList<Friend> friends = BotManager.getAllQQFriends();
		for (Friend friend : friends) {
			sendDelay(false);
			sendMessageToQQFriend(friend.getId(), msg);
		}
	}

	public static void sendMessageToAllStrangers(String msg) {

		LoggerManager.logDebug("SendSystem", "sendMessageToAllStrangers: totally"
				+ BotManager.getAllQQFriends().getSize() + " strangers.");

		/** 机器人行为核心控制 **/
		if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.RobotControl.forceCancel_FriendMessage) {
			LoggerManager.logDebug("SendSystem", "Force Cancel Send!");
			return;
		}

		ContactList<Stranger> strangers = BotManager.getAllStrangers();
		for (Stranger stranger : strangers) {
			sendDelay(false);
			sendMessageToQQFriend(stranger.getId(), msg);
		}
	}

	public static void sendMessageToAllQQGroups(String msg) {

		ContactList<Group> groups = BotManager.getAllQQGroups();
		LoggerManager.logDebug("SendSystem", "sendMessageToAllQQGroups: totally " + groups.size()
				+ " groups.");


		/** 机器人行为核心控制 **/
		if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.RobotControl.forceCancel_GroupMessage == true) {
			LoggerManager.logDebug("SendSystem", "Force Cancel Send!");
			return;
		}

		/** 对发送的文本进行字数检测 **/
		 msg = checkLengthAndModifySendMsg(msg);

		for (Group group : groups) {
			sendDelay(true);
			sendMessageToQQGroup(group.getId(), msg);
		}

	}

	public static void sendMessageToQQGroup(long group, MessageChain messageChain) {
		LoggerManager.logDebug("SendSystem", "给某个QQ群发送信息-QQ群的号码为：" + group);

		try {
			PluginMain.getCurrentBot().getGroup(group).sendMessage(messageChain);
		} catch (IllegalStateException e) {
			LoggerManager.logDebug("SendSystem",
					"IllegalStateException: Group = " + group);
		}

	}

	public static void sendMessageToQQGroup(long group, String msg) {
		/** 对发送的文本进行字数检测 **/
		msg = checkLengthAndModifySendMsg(msg);
		sendMessageToQQGroup(group, MiraiCode.deserializeMiraiCode(msg));
	}

	public static void sendVoiceToQQGroup(long QQGroup, String voice_file_name) {

		LoggerManager.logDebug("SendSystem", "sendMusic() -> voice_file_name = "
				+ voice_file_name, true);


		// MP3 -> Silk
		File uploadVoiceFile = new File(MusicPlatAPI.getVoicesPath() + voice_file_name);
		File silkVoiceFile = null;
		try {
			LoggerManager.logDebug("SendSystem", "Start MP3 to Silk: " + voice_file_name);
			silkVoiceFile = AudioUtils.mp3ToSilk(uploadVoiceFile);
			LoggerManager.logDebug("SendSystem", "Finish MP3 to Silk: " + voice_file_name);
		} catch (IOException e) {
			LoggerManager.reportException(e);
		}

		// Send SilkVoiceFile.
		LoggerManager.logDebug("SendSystem", "Ready to send VoiceFile: voice_file_name = "
				+ voice_file_name, true);
		Voice uploadVoice = PluginMain.getCurrentBot().getGroup(QQGroup).uploadVoice(ExternalResource.create(silkVoiceFile));
		PluginMain.getCurrentBot().getGroup(QQGroup).sendMessage(uploadVoice);
	}


}
