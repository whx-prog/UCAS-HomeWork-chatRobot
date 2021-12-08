package com.sakurawald.framework;

import com.sakurawald.PluginMain;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.ApplicationConfig_Data;
import com.sakurawald.files.ConfigFile;
import com.sakurawald.files.FileManager;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;

import java.io.File;

public class BotManager {

	public static ContactList<Friend> getAllQQFriends() {
		return PluginMain.getCurrentBot().getFriends();
	}

	public static ContactList<Group> getAllQQGroups() {
		return PluginMain.getCurrentBot().getGroups();
	}

	public static ContactList<Stranger> getAllStrangers() {
		return PluginMain.getCurrentBot().getStrangers();
	}

	public static Member getGroupMemberCard(long fromGroup, long fromQQ) {
		Group group = PluginMain.getCurrentBot().getGroup(fromGroup);
		Member groupMember = group.get(fromQQ);
		return groupMember;
	}

	public static String getGroupMemberName(Member groupMember) {

		String ret = groupMember.getNameCard();
		if (ret.isEmpty()) {
			ret = groupMember.getNick();
		}

		return ret;
	}

	public static String getVoicesPath() {
		return ConfigFile.getApplicationConfigPath() + "voice-files" + File.separator;

	}

}
