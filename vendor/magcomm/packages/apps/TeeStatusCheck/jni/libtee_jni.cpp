#include <jni.h>
#include <stdio.h>
#include "pl.h"
#include "kphproxy.h"
#include "com_magcomm_teecheck_TeeCheckActivity.h"

jint Java_com_magcomm_teecheck_TeeCheckActivity_getDeviceStatus(JNIEnv *env, jobject thiz) {
	int r;
	uint32_t verify, authorize;
	uint32_t __tmp0 = 2,__tmp1, __tmp2;
	r = pl_device_get_status(&__tmp0, &verify, &authorize, &__tmp1, &__tmp2, NULL);
	if (r) {
		/* 函数执行失败, 直接返回 */
		return r;
		printf("pl_device_get_status failed \n");
	}
	if (verify != DEVICE_VERIFIED) {
		printf("pl_device_get_status failed \n");
		return -1;
	}
	if (authorize != DEVICE_AUTHORIZED) {
		printf("pl_device_get_status failed \n");
		return -2;
	}
	return 0;
}

jint Java_com_magcomm_teecheck_TeeCheckActivity_getTeeKeyStatus(JNIEnv *env, jobject thiz) {
	printf("verify_tee_all \n");
	return verify_tee_all();
}

jint Java_com_magcomm_teecheck_TeeCheckActivity_getKeyboxStatus(JNIEnv *env, jobject thiz) {
	printf("verify_ta_data2 \n");
	return verify_ta_data2(6,"keybox");		
}
