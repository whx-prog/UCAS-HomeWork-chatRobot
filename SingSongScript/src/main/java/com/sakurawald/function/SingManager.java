package com.sakurawald.function;

import com.sakurawald.command.commands.SingSongCommand;
import com.sakurawald.files.FileManager;


public class SingManager extends FunctionManager {

	private static final SingManager instance = new SingManager();
	public static SingManager getInstance() {
		return instance;
	}

	/** 判断发送间隔是否合法 **/
	@Override
	public boolean canUse(long QQGroup) {
		int interval = FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.perUseIntervalSecond;
		return this.getFunctionUseHistoryManager().isCallSuccessIntervalLegal(
				QQGroup, interval);
	}

	/** 清理掉所有的与"唱歌功能"有关的附加参数 **/
	public String deleteParams(String music_name_text) {
		return music_name_text.toLowerCase().replace(SingSongCommand.RANDOM_SING_FLAG, "");
	}

	/** 判断是否要从音乐列表中随机抽取音乐，而不是抽取第一首 **/
	public boolean isRandomSing(String music_name_text) {
		return music_name_text.toLowerCase().contains(SingSongCommand.RANDOM_SING_FLAG);
	}

}
