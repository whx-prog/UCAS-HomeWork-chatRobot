/*
 * Copyright 2020-2021 StageGuard.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/KonnyakuCamp/SuperCourseTimetableBot/blob/main/LICENSE
 */
package me.stageguard.sctimetable.database.model

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * Courses 存储一个用户的所有课程的列表。
 *
 * 会在目标数据库下建立一个新的[Table]来存储这个用户的课程表。
 *
 * 它是一个class而不是object，因为会有多个用户。
 * @param qq table名称，一般表示为```用户的qq号```
 **/
class Courses(qq: Long) : Table("courses_$qq") {
    /**
     * 课程ID
     **/
    var courseId: Column<Int> = integer("courseId")
    /**
     * 课程名称
     **/
    var courseName: Column<String> = varchar("courseName", 50)
    /**
     * 教师姓名
     **/
    var teacherName: Column<String> = varchar("teacherName", 50)
    /**
     * 地点
     **/
    var locale: Column<String> = varchar("locale", 50)
    /**
     * 这个课程在周几
     **/
    var whichDayOfWeek: Column<Int> = integer("whichDayOfWeek")
    /**
     * 第几节课开始
     **/
    var sectionStart: Column<Int> = integer("sectionStart")
    /**
     * 第几节课结束
     **/
    var sectionEnd: Column<Int> = integer("sectionEnd")
    /**
     * 时间计划(哪些周上这一节课)
     *
     * 它的格式是```week week week ...```
     *
     * 例如：```1 3 5 7 ...```表示这是单周课程
     **/
    var weekPeriod: Column<String> = varchar("weekPeriod", 100)
    /**
     * 这个时间表的开始年份
     **/
    var beginYear: Column<Int> = integer("beginYear")
    /**
     * 这个时间表对应的学期
     **/
    var semester: Column<Int> = integer("semester")
}