#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#ifeq ($(strip $(MAGCOMM_CALCULATOR_SUPPORT)), yes)
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_CERTIFICATE := platform
# LOCAL_STATIC_JAVA_LIBRARIES := libarity android-support-v4 guava magcommspeech
#LOCAL_STATIC_JAVA_LIBRARIES := libarity android-support-v4 guava SpeechApi

LOCAL_SRC_FILES := $(call all-java-files-under, src)

#LOCAL_SDK_VERSION := current
LOCAL_PRIVATE_PLATFORM_APIS=true

LOCAL_PACKAGE_NAME := FactoryTest
LOCAL_JAVA_LIBRARIES += framework telephony-common mediatek-framework

LOCAL_JNI_SHARED_LIBRARIES := libfactoryjni

LOCAL_RESOURCE_DIR := \
	$(LOCAL_PATH)/res \
	$(LOCAL_PATH)/res_ext\
	$(LOCAL_PATH)/../../../../../prebuilts/sdk/current/support/v7/appcompat/res \
	$(LOCAL_PATH)/../../../../../frameworks/support/percent/res \
	$(LOCAL_PATH)/../../../../../frameworks/support/v7/recyclerview/res
	

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-percent \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.appcompat \
    --extra-packages android.support.percent \
    --extra-packages android.support.v7.recyclerview

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)
##################################################
#include $(CLEAR_VARS)

# LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libarity:lib/arity-2.1.2.jar
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += SpeechApi:lib/SpeechApi.jar

#include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

#endif
