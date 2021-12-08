package com.sakurawald.files;

import java.util.ArrayList;

public class ApplicationConfig_Data {

	public Debug Debug = new Debug();
	public class Debug {
		public boolean enable = false;
	}

	public Admin Admin = new Admin();
	public class Admin {
		public InvitationManager InvitationManager = new InvitationManager();
		public class InvitationManager {

			public QQFriendInvitation QQFriendInvitation = new QQFriendInvitation();
			public class QQFriendInvitation {
				public boolean autoAcceptAddQQFriend = false;
			}

			public QQGroupInvitation QQGroupInvitation = new QQGroupInvitation();
			public class QQGroupInvitation {
				public boolean autoAcceptAddQQGroup = false;
			}
		}

		// 机器人的核心控制配置
		public RobotControl RobotControl = new RobotControl();
		public class RobotControl {
			public boolean forceCancel_GroupMessage = false;
			public boolean forceCancel_FriendMessage = true;
		}

		public ArrayList<Long> botAdministrators = new ArrayList<Long>() {
			{
				this.add(3172906506L);
			}
		};

	}

	public Functions Functions = new Functions();
	public class Functions {

		public FunctionManager FunctionManager = new FunctionManager();
		public class FunctionManager {
			public String callTooOftenMsg = "调用该功能过于频繁，请稍后再试试吧~";
			public String functionDisableMsg = "很抱歉，该功能已被禁用。";
		}

		public DailyCountdown DailyCountdown = new DailyCountdown();
		public class DailyCountdown {
			public boolean enable = true;
			public String countdown_commands = "◆距离2021年高考还有$diff_days天！|1623027804000|高考加油！&高考加油！&2021年高考已结束~[DIV]◆距离2020年考研还有$diff_days天！|1608512604000|考研加油&考研加油&考研加油&2020年考研已结束~[DIV]◆距离2020年四六级考试还有$diff_days天！|1600477404000|四六级考试加油！&四六级考试已结束~";
		}

		public DailyPoetry DailyPoetry = new DailyPoetry();

		public class DailyPoetry {
			public JinRiShiCi JinRiShiCi = new JinRiShiCi();
			public class JinRiShiCi {
				// 注意: 这里最好换成你自己的[今日诗词]Token, 可以到今日诗词网站获取属于自己的Token.
				public String token = "paOa0DqOdpLn4FVVHNtEDgU5Imk89kXZ";

			}
			public boolean explanation_Enable = true;
			public int maxRetryLimit = 3;
		}

		public AtFunction AtFunction = new AtFunction();
		public class AtFunction {
			public boolean enable = false;

			public RandomImage RandomImage = new RandomImage();
			public class RandomImage {
				/** 获取随机图片的网站 **/
				public String Random_Image_URLs = "http://www.dmoe.cc/random.php|http://api.mtyqx.cn/tapi/random.php|http://api.mtyqx.cn/api/random.php";
			}
		}

		public NudgeFunction NudgeFunction = new NudgeFunction();
		public class NudgeFunction {
			public boolean enable = true;
			public int perUseIntervalSecond = 3;

			public HitoKoto HitoKoto = new HitoKoto();
			public class HitoKoto {
				public String get_URL_Params = "";
			}
		}

		public SingSongFunction SingSongFunction = new SingSongFunction();
		public class SingSongFunction {
			public boolean enable = true;
			public boolean forceSendCard = false;
			public int perUseIntervalSecond = 5;
			public int maxVoiceFileSize = 15000000;
			public String music_need_paid_msg = "很抱歉，这可能是一首付费歌曲。";
			public String download_music_file_too_big_msg = "这首歌太长啦！";
			public String not_found_music_msg = "哎呀，咱还不会《[OBJECT1]》这首歌！";
		}

	}

	public Systems Systems = new Systems();
	public class Systems {

		public SendSystem SendSystem = new SendSystem();
		public class SendSystem {

			public SendDelay SendDelay = new SendDelay();
			public class SendDelay {

				public SendToFriends SendToFriends = new SendToFriends();
				public class SendToFriends {
					public boolean enable = false;
					public long delayTimeMS = 0;
				}

				public SendToGroups SendToGroups = new SendToGroups();
				public class SendToGroups {
					public boolean enable = true;
					public long delayTimeMS = 1000;
				}

			}

			public int sendMsgMaxLength = 4500;
		}

	}

}
