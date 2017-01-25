/* Cydia Substrate - Powerful Code Insertion Platform
 * Copyright (C) 2008-2011  Jay Freeman (saurik)
*/

/* GNU Lesser General Public License, Version 3 {{{ */
/*
 * Substrate is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Substrate is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Substrate.  If not, see <http://www.gnu.org/licenses/>.
**/
/* }}} */

#ifndef SUBSTRATE_LOG_HPP
#define SUBSTRATE_LOG_HPP

#include <android/log.h>

#define MSLogLevelNotice ANDROID_LOG_INFO
#define MSLogLevelWarning ANDROID_LOG_WARN
#define MSLogLevelError ANDROID_LOG_ERROR
#define LOG_TAG "zzz"
#define MS_DEBUG 1
//#define MS_EXE_PRINTF 0
#ifndef MS_LOG_TAG
	#define MS_LOG_TAG "VA-Native"
#endif

#if MS_DEBUG
	#ifdef MS_EXE_PRINTF
		#define MS_LOGD(fmt,...) printf("[%12s] " fmt "\n", __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGI(fmt,...) printf("[%12s] " fmt "\n", __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGV(fmt,...) printf("[%12s] " fmt "\n", __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGW(fmt,...) printf("[%12s] " fmt "\n", __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGE(fmt,...) printf("[%12s] " fmt "\n", __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGF(fmt,...) printf("[%12s] " fmt "\n", __FUNCTION__,##__VA_ARGS__)

	#else
		#define MS_LOGD(fmt,...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "[%s]" fmt, __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGI(fmt,...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "[%s]" fmt, __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGV(fmt,...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "[%s]" fmt, __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGW(fmt,...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, "[%s]" fmt, __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGE(fmt,...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "[%s]" fmt, __FUNCTION__,##__VA_ARGS__)
		#define MS_LOGF(fmt,...) __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, "[%s]" fmt, __FUNCTION__,##__VA_ARGS__)
	#endif
#else
	#define MS_LOGD(...) while(0){}
	#define MS_LOGI(...) while(0){}
	#define MS_LOGV(...) while(0){}
	#define MS_LOGW(...) while(0){}
	#define MS_LOGE(...) while(0){}
	#define MS_LOGW(...) while(0){}
#endif

#define MSLog(level, fmt,...) do { \
	printf("[%12s] " fmt "\n", __FUNCTION__,##__VA_ARGS__); \
    __android_log_print(level, MS_LOG_TAG, "[%s]" fmt, __FUNCTION__,##__VA_ARGS__); \
} while (false)
#endif//SUBSTRATE_LOG_HPP
