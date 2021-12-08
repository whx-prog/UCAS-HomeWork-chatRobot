package com.sakurawald.timer.timers;

import com.sakurawald.debug.LoggerManager;
import com.sakurawald.timer.DefaultPlan;
import com.sakurawald.timer.RobotAbstractTimer;
import com.sakurawald.timer.TimerController;
import com.sakurawald.utils.DateUtil;

public abstract class DailyTimer extends RobotAbstractTimer implements TimerController {

    protected int lastPrepareDay = 0;
    protected int lastSendDay = 0;
    protected String sendMsg;

    public DailyTimer(String timerName, long firstTime, long delayTime) {
        super(timerName, firstTime, delayTime);
    }

    @Override
    public void logDebugTimerState() {
        LoggerManager.logDebug("TimerSystem",  getTimerName() + ": Run");
        LoggerManager.logDebug("TimerSystem", getTimerName() + ": lastPrepareDay = "
                + lastPrepareDay);
        LoggerManager.logDebug("TimerSystem", getTimerName() + ": lastSendDay = "
                + lastSendDay);
        LoggerManager.logDebug("TimerSystem",
                getTimerName() + ": nowDay = " + DateUtil.getNowDay());
    }

    @Override
    public void run() {
        autoControlTimer();
    }

}
