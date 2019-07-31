/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2018. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

#ifndef ANDROID_ML_NN_RUNTIME_NEURO_PILOT_PRIVATE_H
#define ANDROID_ML_NN_RUNTIME_NEURO_PILOT_PRIVATE_H

#if __ANDROID_API__ >= __ANDROID_API_O_MR1__

#include <thread>
#include "NeuroPilotDef.h"

/*************************************************************************************************/
// Create Instance
typedef int (*ANeuroPilotModel_create_fn)(ANeuralNetworksModel** model);

typedef int (*ANeuroPilotCompilation_create_fn)(
        ANeuralNetworksModel* model, ANeuralNetworksCompilation** compilation);

typedef int (*ANeuroPilotExecution_create_fn)(
        ANeuralNetworksCompilation* compilation, ANeuralNetworksExecution** execution);

// Profiler
typedef int (*ANeuroPilotExecution_setCurrentExecutionStep_fn)(
        ANeuralNetworksExecution *execution, uint32_t step);

typedef int (*ANeuroPilotExecution_setExecution_fn)(
        std::thread::id tid, const ANeuralNetworksExecution *execution);

typedef int (*ANeuroPilotExecution_eraseExecution_fn)(std::thread::id tid);

typedef int (*ANeuroPilotExecution_startProfile_fn)(std::thread::id tid, const char* device);

typedef int (*ANeuroPilotExecution_stopProfile_fn)(
        std::thread::id tid, const char* request, int err);

typedef int (*ANeuroPilotExecution_clearProfilerInfo_fn)(
        ANeuralNetworksExecution *execution);

// Sys trace
typedef int (*ANeuroPilotUtils_sysTraceStart_fn)(const char* name);

typedef int (*ANeuroPilotUtils_sysTraceStop_fn)();

// Utils
typedef bool (*ANeuroPilotUtils_forbidCpuExecution_fn)();

// Shared Memory Extension
typedef int (*ANeuroPilotMemory_create_fn)(uint32_t size, ANeuralNetworksMemory **memory);

typedef int (*ANeuroPilotMemory_getPointer_fn)(uint8_t** buffer, ANeuralNetworksMemory **memory);

typedef int (*ANeuroPilotMemory_freeMemory_fn)(ANeuralNetworksMemory **memory);

/*************************************************************************************************/
// Create Instance
inline bool ANeuroPilotModelPrivate_makeModelBuilder(ANeuralNetworksModel **model, int* ret) {
    LOAD_NP_FUNCTION(ANeuroPilotModel_create);
    EXECUTE_NP_CREATE_INSTANCE(model);
}

inline bool ANeuroPilotCompilationPrivate_makeCompilationBuilder(
        ANeuralNetworksModel* model, ANeuralNetworksCompilation** compilation, int* ret) {
    LOAD_NP_FUNCTION(ANeuroPilotCompilation_create);
    EXECUTE_NP_CREATE_INSTANCE(model, compilation);
}

inline bool ANeuroPilotExecutionPrivate_makeExecutionBuilder(
        ANeuralNetworksCompilation* compilation, ANeuralNetworksExecution** execution, int* ret) {
    LOAD_NP_FUNCTION(ANeuroPilotExecution_create);
    EXECUTE_NP_CREATE_INSTANCE(compilation, execution);
}

// Profiler
inline int ANeuroPilotExecutionPrivate_setCurrentExecutionStep(
        ANeuralNetworksExecution *execution, uint32_t step) {
    LOAD_NP_FUNCTION(ANeuroPilotExecution_setCurrentExecutionStep);
    EXECUTE_NP_FUNCTION_RETURN_INT(execution, step);
}

inline int ANeuroPilotExecutionPrivate_setExecution(
        std::thread::id tid, const  ANeuralNetworksExecution *execution) {
    LOAD_NP_FUNCTION(ANeuroPilotExecution_setExecution);
    EXECUTE_NP_FUNCTION_RETURN_INT(tid, execution);
}

inline int ANeuroPilotExecutionPrivate_eraseExecution(std::thread::id tid) {
    LOAD_NP_FUNCTION(ANeuroPilotExecution_eraseExecution);
    EXECUTE_NP_FUNCTION_RETURN_INT(tid);
}

inline int ANeuroPilotExecutionPrivate_startProfile(std::thread::id tid, const char* device) {
    LOAD_NP_FUNCTION(ANeuroPilotExecution_startProfile);
    EXECUTE_NP_FUNCTION_RETURN_INT(tid, device);
}

inline int ANeuroPilotExecutionPrivate_stopProfile(
        std::thread::id tid, const char* request, int err) {
    LOAD_NP_FUNCTION(ANeuroPilotExecution_stopProfile);
    EXECUTE_NP_FUNCTION_RETURN_INT(tid, request, err);
}

inline int ANeuroPilotExecutionPrivate_clearProfilerInfo(
        ANeuralNetworksExecution *execution) {
    LOAD_NP_FUNCTION(ANeuroPilotExecution_clearProfilerInfo);
    EXECUTE_NP_FUNCTION_RETURN_INT(execution);
}


// Sys trace
inline int ANeuroPilotUtilsPrivate_sysTraceStart(const char* name) {
    LOAD_NP_FUNCTION(ANeuroPilotUtils_sysTraceStart);
    EXECUTE_NP_FUNCTION_RETURN_INT(name);
}

inline int ANeuroPilotUtilsPrivate_sysTraceStop() {
    LOAD_NP_FUNCTION(ANeuroPilotUtils_sysTraceStop);
    EXECUTE_NP_FUNCTION_RETURN_INT();
}

// Utils
inline bool ANeuroPilotUtilsPrivate_forbidCpuExecution() {
    LOAD_NP_FUNCTION(ANeuroPilotUtils_forbidCpuExecution);
    EXECUTE_NP_FUNCTION_RETURN_BOOL();
}

// Shared Memory Extension
inline int ANeuroPilotMemoryPrivate_create(uint32_t size, ANeuralNetworksMemory *memory) {
    LOAD_NP_FUNCTION(ANeuroPilotMemory_create);
    EXECUTE_NP_FUNCTION_RETURN_INT(size, &memory);
}

inline int ANeuroPilotMemoryPrivate_getPointer(uint8_t** buffer, ANeuralNetworksMemory *memory) {
    LOAD_NP_FUNCTION(ANeuroPilotMemory_getPointer);
    EXECUTE_NP_FUNCTION_RETURN_INT(buffer, &memory);
}

inline int ANeuroPilotMemoryPrivate_freeMemory(ANeuralNetworksMemory *memory) {
    LOAD_NP_FUNCTION(ANeuroPilotMemory_freeMemory);
    EXECUTE_NP_FUNCTION_RETURN_INT(&memory);
}

#endif  //  __ANDROID_API__ >= 27
#endif  // ANDROID_ML_NN_RUNTIME_NEURO_PILOT_PRIVATE_H

