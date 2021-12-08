package com.sakurawald;


import com.sakurawald.api.MusicPlatAPI;
import com.sakurawald.command.RobotCommandChatType;
import com.sakurawald.command.RobotCommandManager;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import com.sakurawald.function.AtFunction;
import com.sakurawald.function.NudgeFunction;
import com.sakurawald.timer.RobotTimerManager;
import io.github.mzdluo123.silk4j.AudioUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.*;

import java.io.File;
import java.io.IOException;

public final class PluginMain extends JavaPlugin {

    // WARNING: INSTANCE字段必须设置为public, 否则mirai-console在反射时会失败.
    public static final PluginMain INSTANCE = new PluginMain();
    private static boolean pluginLoaded = false;
    private static RobotCommandManager commandManager = null;
    private static Bot CURRENT_BOT = null;

    public static PluginMain getInstance() {
        return INSTANCE;
    }

    public static Bot getCurrentBot() {
        return CURRENT_BOT;
    }

    public static boolean isPluginLoaded() {
        return pluginLoaded;
    }

    public static RobotCommandManager getCommandManager() {
        return commandManager;
    }

    private PluginMain() {
        super(new JvmPluginDescriptionBuilder("com.sakurawald.Plum", "1.0")
                .name("Plum")
                .author("SakuraWald")
                .build());
    }

    @Override
    public void onEnable() {




        pluginLoaded = true;
        LoggerManager.logDebug("Plum >> Enable.", true);
        LoggerManager.logDebug("Start Init...", true);

        // Init CommandSystem.
        LoggerManager.logDebug("CommandSystem", "Init CommandSystem.", true);
        commandManager = new RobotCommandManager();

        // Init FileSystem.
        try {
            LoggerManager.logDebug("FileSystem", "Init FileSystem.", true);
            FileManager.getSingleInstance();
        } catch (IllegalArgumentException e) {
            LoggerManager.reportException(e);
        }

        // Init AudioUtils.
        LoggerManager.logDebug("Init AudioUtils.", true);
        try {
            File cacheFile = new File(MusicPlatAPI.getVoicesPath());
            cacheFile.mkdirs();
            AudioUtils.init(cacheFile);
        } catch (IOException e) {
            LoggerManager.reportException(e);
        }

        /** 接收群消息事件 **/
        LoggerManager.logDebug("EventSystem", "Start to subscribe events.");
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class,
                event -> {
                    {
                        commandManager.receiveMessage(
                                    RobotCommandChatType.GROUP_CHAT.getType(),
                                    event.getTime(), event.getGroup().getId(),
                                    event.getSender().getId(), event.getMessage());

                        // Call -> AtFunction
                        AtFunction.handleEvent(event);
                    }
                });


        /** 接收好友消息事件 **/
        GlobalEventChannel.INSTANCE.subscribeAlways(
                FriendMessageEvent.class,
                event -> {
                    {
                            commandManager.receiveMessage(
                                    RobotCommandChatType.FRIEND_CHAT.getType(),
                                    event.getTime(), -1,  event.getSender().getId(),
                                    event.getMessage());

                    }

                });

        /** 接收陌生人消息事件 **/
        GlobalEventChannel.INSTANCE.subscribeAlways(
                StrangerMessageEvent.class,
                event -> {
                    {
                            commandManager.receiveMessage(
                                    RobotCommandChatType.STRANGER_CHAT.getType(),
                                    event.getTime(), -1, event.getSender().getId(), event.getMessage());
                    }

                });


        /** 接收群临时消息事件 **/
        GlobalEventChannel.INSTANCE.subscribeAlways(
                GroupTempMessageEvent.class,
                event -> {
                    {

                            commandManager.receiveMessage(
                                    RobotCommandChatType.GROUP_TEMP_CHAT
                                            .getType(), event.getTime(),
                                            event.getGroup().getId(),
                                             event.getSender().getId(), event.getMessage());


                    }
                });

        /** 机器人登陆事件 **/
        GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, event -> {
            {
                /** 初始化Bot实例 **/
                tryInitBot(event.getBot());
            }
        });

        /** 戳一戳事件 **/
        // Call -> NudgeFunction
        GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class, NudgeFunction::handleEvent);


        /** 接收好友添加请求事件 **/
        GlobalEventChannel.INSTANCE
                .subscribeAlways(
                        NewFriendRequestEvent.class,
                        event -> {

                            {
                                // 自动处理好友邀请
                                if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.InvitationManager.QQFriendInvitation.autoAcceptAddQQFriend) {
                                    // 同意 -> 好友添加请求
                                    LoggerManager.logDebug(
                                            "ContactSystem",
                                            "Accept -> FriendAddRequest: "
                                                    + event.getFromId());
                                    event.accept();


                                } else {
                                    // 拒绝 -> 好友添加请求
                                    event.reject(false);
                                }

                            }

                        });



        /** 邀请入群请求事件 **/
        GlobalEventChannel.INSTANCE
                .subscribeAlways(
                        BotInvitedJoinGroupRequestEvent.class,
                        event -> {
                            {
                                if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.InvitationManager.QQGroupInvitation.autoAcceptAddQQGroup) {
                                    event.accept();
                                    LoggerManager.logDebug(
                                            "ContactSystem",
                                            "Accept -> InvitedJoinGroupRequest: "
                                                    + event.getGroupId());
                                }

                            }
                        });


        // 初始化时间任务系统
        LoggerManager.logDebug("TimerSystem", "Start TimerSystem.", true);
        RobotTimerManager.getInstance();

        LoggerManager.logDebug("End Init...", true);
    }


    public void tryInitBot(Bot bot) {
        if (CURRENT_BOT == null) {
            CURRENT_BOT = bot;
        }
    }


    @Override
    public void onDisable() {
        super.getLogger().info("Plum >> Disable.");
    }

}

