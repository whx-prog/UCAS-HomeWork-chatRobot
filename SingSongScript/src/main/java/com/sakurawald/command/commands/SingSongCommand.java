package com.sakurawald.command.commands;

import com.sakurawald.api.KugouMusicAPI;
import com.sakurawald.api.MusicPlatAPI;
import com.sakurawald.api.NeteaseCloudMusicAPI;
import com.sakurawald.api.TencentMusicAPI;
import com.sakurawald.bean.SongInformation;
import com.sakurawald.command.RobotCommand;
import com.sakurawald.command.RobotCommandChatType;
import com.sakurawald.command.RobotCommandUser;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.exception.CanNotDownloadFileException;
import com.sakurawald.exception.FileTooBigException;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.BotManager;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.function.SingManager;

import com.sakurawald.utils.LanguageUtil;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingSongCommand extends RobotCommand {

	private static final Pattern pattern = Pattern
			.compile("^(?:(?:唱歌)|(?:唱)|(?:点歌)|(?:听歌)|(?:我想听)|(?:来首)|(?:想听)|(?:给我唱))\\s?([\\s\\S]*)$");

	public static final String RANDOM_SING_FLAG = "-random";



	public SingSongCommand(String rule) {
		super(rule);
		getRange().add(RobotCommandChatType.GROUP_CHAT);

		getUser().add(RobotCommandUser.NORMAL_USER);
		getUser().add(RobotCommandUser.GROUP_ADMINISTRATOR);
		getUser().add(RobotCommandUser.GROUP_OWNER);
		getUser().add(RobotCommandUser.BOT_ADMINISTRATOR);
	}

	@Override
	public void runCommand(int msgType, int time, long fromGroup, long fromQQ, MessageChain messageChain) {

		/** 功能开关判断 **/
		if (! FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.enable) {
			return;
		}

		/** 引导式帮助 **/
		String msg = messageChain.contentToString();
		if (msg
				.matches("^(?:(?:唱歌)|(?:唱)|(?:点歌)|(?:听歌)|(?:我想听)|(?:来首)|(?:想听)|(?:给我唱))[\\s]*$")) {
			String help =  "用法示例：\n"
					+ "\"唱歌 霜雪千年\"" + "\n"
					+ "\"唱歌 霜雪千年 " + RANDOM_SING_FLAG +"\"" + "\n"
					+ "\"唱歌 霜雪千年 网易云/酷狗/QQ\"";

			MessageManager.sendMessageBySituation(fromGroup, fromQQ, help);
			return;
		}
		msg = msg.toLowerCase();

		/** 唱歌指令判断逻辑 **/
		final Matcher matcher = pattern.matcher(msg);

		// 判断 是否符合 唱歌指令要求
		if (matcher.find()) {

			/** Function系统 **/
			String finalMsg = msg;
			new Thread(() -> {

				LoggerManager.logDebug("SingSong", "收到唱歌指令，开始执行核心代码", true);

				/** 判断唱歌间隔是否合法 **/
				if (!SingManager.getInstance().canUse(fromGroup)) {
					MessageManager.sendMessageBySituation(fromGroup, fromQQ, FileManager.applicationConfig_File.getSpecificDataInstance().Functions.FunctionManager.callTooOftenMsg);
					LoggerManager.logDebug("SingSongFunction", "Call too often. Cancel!", true);
					return;
				}

				/**
				 * 更新唱歌间隔 [!] 只要执行了唱歌核心代码，无论最后是否成功发送语音文件，都更新lastSingTime
				 * **/
				SingManager.getInstance().updateUseTime(fromGroup);

				/** SongInformation获取逻辑 **/
				String input_music_name = matcher.group(1);

				LoggerManager.logDebug("SingSong",
						"用户输入的需要唱的歌曲: input_music_name = "
								+ input_music_name, true);

				// [!] 使用用户输入的歌曲名在网络上找歌曲
				SongInformation si = null;

				// 点歌 -> 以卡片形式分享.
				boolean send_card_flag = finalMsg.contains("点歌");
				if (FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.forceSendCard) {
					send_card_flag = true;
				}

				boolean random_music_flag;
				random_music_flag = SingManager.getInstance().isRandomSing(
						finalMsg);

				// 获得干净的音乐名
				input_music_name = SingManager.getInstance().deleteParams(
						input_music_name);

				/** Construct MusicPlats. **/
				ArrayList<MusicPlatAPI> musicPlatAPIS = new ArrayList<>();
				musicPlatAPIS.add(NeteaseCloudMusicAPI.getInstance());
				musicPlatAPIS.add(KugouMusicAPI.getInstance());
				musicPlatAPIS.add(TencentMusicAPI.getInstance());
				// Select MusicPlat.
				for (int i = 0; i < musicPlatAPIS.size(); i++) {
					// Has Any SelectCodes ?
					for (String selectCode : musicPlatAPIS.get(i).getSelectCodes()) {
						if (finalMsg.contains(" " + selectCode)) {
							Collections.swap(musicPlatAPIS, 0, i);
							finalMsg.replace(" " + selectCode, "");
							break;
						}
					}

				}

				/** Try All MusicPlats. **/
				LoggerManager.logDebug("SingSong",
						"input_music_name = "
								+ input_music_name, true);
				MusicPlatAPI mpa = null;
				for (int i = 0; i < musicPlatAPIS.size(); i++) {
					LoggerManager.logDebug("SingSong",
							"尝试第" + (i+1) + "乐库: " + musicPlatAPIS.get(i).getLogTypeName(), true);
					mpa = musicPlatAPIS.get(i);
					si = mpa.checkAndGetSongInformation(input_music_name,
							random_music_flag);

					if (si != null) {
						// FIX: 当音乐平台是QQ音乐时, 强制以卡片形式发送.
						if (mpa instanceof  TencentMusicAPI) send_card_flag = true;
						break;
					}

				}

				/** 搜索不到指定的音乐, 结束代码 **/
				if (si == null) {
					LoggerManager.logDebug("SingSong",
							"所唱的歌曲搜索不到, 结束代码: input_music_name = "
									+ input_music_name, true);
					MessageManager.sendMessageBySituation(fromGroup, fromQQ,
							LanguageUtil
									.transObject_X(
											1,
											FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.not_found_music_msg,
											input_music_name));
					return;
				}


				/** 音乐发送逻辑 **/
				if (!send_card_flag) {

					/** 音乐文件下载逻辑 **/
					// 若音乐文件不存在时，尝试下载音乐
					mpa.getDownloadPath(si.getMusic_Name(),
							si.getMusic_ID());

					try {
						mpa.downloadMusic(si);
					} catch (CanNotDownloadFileException e) {
						MessageManager.sendMessageBySituation(fromGroup, fromQQ, FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.music_need_paid_msg);
						return;
					} catch (FileTooBigException e) {
						MessageManager.sendMessageBySituation(fromGroup, fromQQ, FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.download_music_file_too_big_msg);
						return;
					}

					MessageManager.sendVoiceToQQGroup(
							fromGroup,
							mpa.getDownloadFileName(si.getMusic_Name(), si.getMusic_ID()));
				} else {

					// 将 命令调用者 的信息 附加到SongInformation上.
					Member orderMember = BotManager.getGroupMemberCard(fromGroup, fromQQ);
					si.setSummary("[点歌] " + BotManager.getGroupMemberName(orderMember));
					si.setImg_URL(orderMember.getAvatarUrl());

					MessageManager.sendMessageBySituation(fromGroup, fromQQ,
							mpa.getCardCode(si));

				}

			}).start();

		}
	}

}
