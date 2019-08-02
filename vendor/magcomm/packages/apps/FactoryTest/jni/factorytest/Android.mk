
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
#LOCAL_MODULE_TAGS := optional


LOCAL_SRC_FILES:= \
  com_magcomm_factorytest_util_FactoryTestJNI.c
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	mediatek/external/sensor-tools

 LOCAL_SHARED_LIBRARIES	:= \
 libdl  \
 libutils   \
 libcutils  \
 libfile_op_mtk \
 libnvram_mtk \
 libhwm_mtk	

LOCAL_MODULE:= libfactoryjni

LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)
