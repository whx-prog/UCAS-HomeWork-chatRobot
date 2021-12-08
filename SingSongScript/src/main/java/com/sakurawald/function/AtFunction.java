package com.sakurawald.function;

import com.sakurawald.PluginMain;
import com.sakurawald.api.QingYunKe_API;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.MessageManager;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;

public class AtFunction {

    public static void handleEvent(GroupMessageEvent event) {

        if (!FileManager.applicationConfig_File.getSpecificDataInstance().Functions.AtFunction.enable) {
            return;
        }

        // Has @Bot ?
        if (event.getMessage().contains(new At(PluginMain.getCurrentBot().getId()))) {
            long fromGroup = event.getGroup().getId();
            long fromQQ = event.getSender().getId();
            String receiveMsg = event.getMessage().contentToString();

            // Get Answer And SendMsg.
            String sendMsg = QingYunKe_API.getAnswer(receiveMsg);
            MessageManager.sendMessageBySituation(fromGroup, fromQQ, sendMsg);
        }

    }
}
