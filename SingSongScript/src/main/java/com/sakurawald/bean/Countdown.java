package com.sakurawald.bean;


import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import com.sakurawald.utils.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * 描述一个具体的Countdown项目
 **/
public class Countdown {

    private final ArrayList<String> special_Countdown_Msg = new ArrayList<String>();
    private String countdown_Command = null;
    private String countdown_BasicCountdownMsg = null;
    private long countdown_Timestamp_Ms = 0;
    private Calendar countdown_Calendar = null;
    private String special_Countdown_Msgs = null;

    public Countdown(String countdown_command) {

        this.countdown_Command = countdown_command;
        /** 对单条Countdown_Command进行分割 **/
        String[] temp = countdown_command.split("\\|", 3);
        this.countdown_Command = countdown_command;
        this.countdown_BasicCountdownMsg = temp[0];
        this.countdown_Timestamp_Ms = Long.parseLong(temp[1]);
        this.countdown_Calendar = DateUtil
                .translate_TimeStamp_Ms_To_Calendar(countdown_Timestamp_Ms);
        this.special_Countdown_Msgs = temp[2];
        special_Countdown_Msg.addAll(Arrays.asList(special_Countdown_Msgs.split("&")));

    }

    public static ArrayList<Countdown> getCountdownsByCommands() {
        return getCountdownsByCommands(FileManager.applicationConfig_File.getSpecificDataInstance().Functions.DailyCountdown.countdown_commands);
    }

    /**
     * 传入countdown_commands, 解析并构造出所有的countdown
     **/
    public static ArrayList<Countdown> getCountdownsByCommands(
            String countdown_commands) {

        ArrayList<Countdown> result = new ArrayList<Countdown>();

        /** 分离出单条的countdown_command **/
        for (String single_command : countdown_commands.split("\\[DIV\\]")) {

            // 逐个构造countdown对象
            Countdown cd = new Countdown(single_command);
            result.add(cd);

            LoggerManager.logDebug("Countdown", "Get Countdown: " + cd);
        }

        return result;
    }

    /**
     * 获取今天距离倒计时日期还有几天, 若今天为倒计时当天, 则返回0. 超过倒计时当天, 则返回负数
     **/
    private int getDistanceDays() {
        return DateUtil.differentDaysByMillisecond(
                Calendar.getInstance(), countdown_Calendar);
    }

    private String getSpecialCountdownMsg() {

        int distanceDays = getDistanceDays();

        int index = Math.abs(distanceDays);

        /** 判断超出天数是否超过了<特殊文本的数量> **/
        if (index >= special_Countdown_Msg.size()) {
            index = special_Countdown_Msg.size() - 1;
        }

        return special_Countdown_Msg.get(index);
    }

    /**
     * 获取当前Countdown任务要发送的倒计时文本
     **/
    public String getTodayCountdownMsg() {

        String result;

        /** 判断是否已经到达<倒计时日期> **/
        int distanceDays = getDistanceDays();
        if (distanceDays > 0) {

            // 使用<基础语句>
            result = this.countdown_BasicCountdownMsg;

            // 替换$diff_days
            result = result.replace("$diff_days", String.valueOf(distanceDays));
            return result;
        }

        /** 若已经到达<倒计时日期>, 则获取<特殊的倒计时文本> **/
        result = getSpecialCountdownMsg();

        return result;
    }

    @Override
    public String toString() {
        return "Countdown [countdown_Command=" + countdown_Command
                + ", countdown_Name=" + countdown_BasicCountdownMsg
                + ", countdown_Timestamp_Ms=" + countdown_Timestamp_Ms
                + ", countdown_Calendar=" + DateUtil.getDateSimple(countdown_Calendar)
                + ", countdown_Msg=" + special_Countdown_Msg
                + ", countdown_Msgs=" + special_Countdown_Msgs + "]";
    }

}
