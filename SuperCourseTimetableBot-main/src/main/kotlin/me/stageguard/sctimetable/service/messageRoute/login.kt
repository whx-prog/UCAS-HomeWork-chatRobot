/*
 * Copyright 2020-2021 StageGuard.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/KonnyakuCamp/SuperCourseTimetableBot/blob/main/LICENSE
 */
package me.stageguard.sctimetable.service.messageRoute

import kotlinx.coroutines.CoroutineScope
import me.stageguard.sctimetable.api.edu_system.`super`.LoginInfoData
import me.stageguard.sctimetable.service.RequestHandlerService
import me.stageguard.sctimetable.service.request.Login
import me.stageguard.sctimetable.utils.*
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
suspend fun FriendMessageEvent.login(coroutineScope: CoroutineScope) {
    interactiveConversation(coroutineScope, eachTimeLimit = 30000L) {
        send("请输入超级课表账号")
        receivePlain(key = "account")
        send("请输入超级课表密码\n注意：若用手机验证码登录的超级课表，请先在超级课表app设置账号密码")
        receivePlain(key = "password")
    }.finish {
        subject.sendMessage("正在登录。。。")
        RequestHandlerService.sendRequest(
            Login(
                subject.id,
                LoginInfoData(it["account"].cast(), it["password"].cast())
            )
        )
    }.exception {
        if (it is QuitConversationExceptions.TimeoutException) {
            subject.sendMessage("太长时间未输入，请重新登录")
        }
    }
}
