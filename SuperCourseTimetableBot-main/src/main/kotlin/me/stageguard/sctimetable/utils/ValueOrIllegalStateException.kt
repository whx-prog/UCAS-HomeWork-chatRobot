/*
 * Copyright 2020-2021 StageGuard.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/KonnyakuCamp/SuperCourseTimetableBot/blob/main/LICENSE
 */
package me.stageguard.sctimetable.utils

typealias ValueOrISE<T> = Either<IllegalStateException, T>

@Suppress("FunctionName")
inline fun <reified T> InferredEitherOrISE(v: T) = Either.invoke<IllegalStateException, T>(v)