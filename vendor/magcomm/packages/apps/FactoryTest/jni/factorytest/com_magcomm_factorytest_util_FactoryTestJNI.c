#include <jni.h>
#include <utils/Log.h>
#include <fcntl.h>
#include <linux/sensors_io.h>
//#include <libhwm.h>

#define ALSPS				0X84
#define ALSPS_GET_ALS_RAW_DATA2  	_IOR(ALSPS, 0x09, int)
#define ALSPS_GET_PS_THD              _IOR(ALSPS, 0x16, int)
#define ALSPS_SET_PS_CALI  						_IOR(ALSPS, 0x15, int)
#define ALSPS_SET_PS_CALI3  						_IOR(ALSPS, 0x17, int)
#define ALSPS_GET_PS_RAW_DATA_FOR_CALI              _IOR(ALSPS, 0x14, int)
#define ALSPS_GET_PS_RAW_DATA				_IOR(ALSPS, 0x04, int)
#define ALSPS_GET_ALS_RAW_DATA           	_IOR(ALSPS, 0x08, int)

#define AUD_DRV_IOC_MAGIC_CIT 'C'
#define SET_SPEAKER_ON_CIT            _IOW(AUD_DRV_IOC_MAGIC_CIT, 0xa1, int)
#define SET_SPEAKER_OFF_CIT          _IOW(AUD_DRV_IOC_MAGIC_CIT, 0xa2, int)
#define SET_HEADPHONE_ON_CIT      _IOW(AUD_DRV_IOC_MAGIC_CIT, 0xa4, int)
#define SET_HEADPHONE_OFF_CIT     _IOW(AUD_DRV_IOC_MAGIC_CIT, 0xa5, int)
#define SET_ANAAFE_REG         _IOWR(AUD_DRV_IOC_MAGIC_CIT, 0x02, Register_Control*)

#define LOG_TAG "JNICIT"
 struct PS_CALI_DATA_STRUCT
{
    int close;
    int far_away;
	int valid;
} ;
	
struct PS_CALI_FAR_DATA_STRUCT
{
    int far_away;
};
	
typedef struct
{
    unsigned long offset;
    unsigned long value;
    unsigned long mask;
}Register_Control;

struct CIT_REGISTER_CONTROL{
	unsigned long offset;
	unsigned long value;
	unsigned long mask;
};

	struct PS_CALI_FAR_DATA_STRUCT ps_cali_far_temp;
    struct PS_CALI_DATA_STRUCT ps_cali_temp;
    struct PS_CALI_DATA_STRUCT ps_thd_temp ;
    
    struct CIT_REGISTER_CONTROL cit_Register_Control;

int get_ps_data(void) {
  // __android_log_print(4, LOG_TAG, "get_ps_data");
    int handle = 0;
    handle = open("/dev/als_ps",O_RDONLY);
//sleep(1);
    if(handle < 0)
    {
        //__android_log_print(4, LOG_TAG, "get_ps_data open file error 111 %d",handle);
        return handle;
    }

    int err = -1;
    int ps_dat;
    if ((err = ioctl(handle, ALSPS_GET_PS_RAW_DATA, &ps_dat))) {
         //__android_log_print(4, LOG_TAG, "get_ps_data readfile file ps_dat error");
		 
		close(handle);
        return err;
    }


    close(handle);
    return ps_dat;
}

int get_als_data(void)
{
    //__android_log_print(4, LOG_TAG, "get_als_data");
    int handle = 0;
    handle = open("/dev/als_ps",O_RDONLY);
    if(handle < 0)
    {
        //__android_log_print(4, LOG_TAG, "get_als_data open file error 111 %d",handle);
        return handle;
    }

    int err = -1;
    int als_dat;

    if ((err = ioctl(handle, ALSPS_GET_ALS_RAW_DATA, &als_dat))) {
         //__android_log_print(4, LOG_TAG, "get_als_data readfile file ps_dat error");
		 
		close(handle);
        return err;
    }
 //__android_log_print(4, LOG_TAG, "get_als_data %d",als_dat);    


    close(handle);
    return als_dat;
}

int set_ps_cali3(void)
{
    //__android_log_print(4, LOG_TAG, "set_ps_cali3");
	int handle = 0;
    int err = -1;
    int als_dat;
    handle = open("/dev/als_ps",O_RDONLY);
    if(handle < 0)
    {
        //__android_log_print(4, LOG_TAG, "set_ps_cali3 open file error  %d",handle);
        return handle;
    }

    if ((err = ioctl(handle, ALSPS_SET_PS_CALI3, &ps_cali_far_temp))) {
         //__android_log_print(4, LOG_TAG, "set_ps_cali3 readfile file ps_dat error");
		 
		close(handle);
        return err;
    }
 //__android_log_print(4, LOG_TAG, "set_ps_cali3 %d",err);    

    close(handle);
    return err;
}

JNIEXPORT jint JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_getPSValue
(JNIEnv *env, jobject obj)
{

return get_ps_data(); 
}

JNIEXPORT jint JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_getALSValue
(JNIEnv *env, jobject obj)
{
  
    return get_als_data();
}

int get_ps_thd(void);
JNIEXPORT jintArray JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_getPSTHD
(JNIEnv *env, jobject obj)
{

   jintArray  array;
     int i;
     jint jinttemp[3];
      array = (*env)-> NewIntArray(env, 3);
     get_ps_thd();
   jinttemp[0]= ps_thd_temp.close;
   jinttemp[1]= ps_thd_temp.far_away;
   jinttemp[2]= ps_thd_temp.valid;
 //__android_log_print(4, LOG_TAG, "ava_sim_android_mtkcit_cittools_CITJNI_getPSTHD  jinttemp[0] %d jinttemp[1]%d  jinttemp[2]%d",jinttemp[0] ,jinttemp[1] , jinttemp[2]);  


(*env)->SetIntArrayRegion(env,array, 0, 3, jinttemp);
return array;
}


int  get_ps_raw_cali();
JNIEXPORT jintArray JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_PSCali
(JNIEnv *env, jobject obj)
{
    jintArray  array;
     int i;
     jint jinttemp[3];
      array = (*env)-> NewIntArray(env, 3);
     get_ps_raw_cali();
   jinttemp[0]=ps_cali_temp.close;
   jinttemp[1]=ps_cali_temp.far_away;
   jinttemp[2]=ps_cali_temp.valid;
 //__android_log_print(4, LOG_TAG, "sim_android_mtkcit_cittools_CITJNI_PSCali   jinttemp[0] %d jinttemp[1]%d  jinttemp[2]%d",jinttemp[0] ,jinttemp[1] , jinttemp[2]);    
(*env)->SetIntArrayRegion(env,array, 0, 3, jinttemp);
return array;
}

    int set_ps_cali();
 //    int get_ps_thd();
JNIEXPORT jint JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_PSCali2
(JNIEnv *env, jobject obj ,jint far,jint close ,jint valid)
{     

    int ps_far = far;
    int ps_close = close;
    int ps_valid = valid;
   ps_cali_temp.close=ps_close;
   ps_cali_temp.far_away=ps_far;
   ps_cali_temp.valid =valid;
 //__android_log_print(4, LOG_TAG, "sim_android_mtkcit_cittools_CITJNI_PSCali2   ps_far= %d ,ps_close=%d , valid=%d",ps_far ,ps_close , 1);  
   int m = set_ps_cali();
 //__android_log_print(4, LOG_TAG, "m= %d ",m);  
     return m;
    // get_ps_thd();
/*
    jintArray  array;
     int i;
     jint jinttemp[3];
      array = (*env)-> NewIntArray(env, 3);
   jinttemp[0]=ps_cali_temp.close;
   jinttemp[1]=ps_cali_temp.far_away;
   jinttemp[2]=ps_cali_temp.valid;
  
(*env)->SetIntArrayRegion(env,array, 0, 3, jinttemp);
return array;
*/
}

JNIEXPORT jint JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_PSCali3
(JNIEnv *env, jobject obj ,jint far)
{     

    int ps_far = far;

   ps_cali_far_temp.far_away = ps_far;

 //__android_log_print(4, LOG_TAG, "sim_android_mtkcit_cittools_CITJNI_PSCali3   ps_far= %d ",ps_far);  
   int m = set_ps_cali3();
 //__android_log_print(4, LOG_TAG, "m= %d ",m);  
     return m;
}

int alsps_write_nvram( struct PS_CALI_DATA_STRUCT *ps);
JNIEXPORT jint JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_PSwritenvram
(JNIEnv *env, jobject obj ,jint far,jint close ,jint valid)
{     
    struct PS_CALI_DATA_STRUCT dat;

   dat.valid = valid;
   dat.close = close;
   dat.far_away = far;

 //__android_log_print(4, LOG_TAG, "Java_sim_android_mtkcit_cittools_CITJNI_PS_write_nvram   valid= %d,dat.close=%d,dat.far_away=%d \n ",dat.valid,dat.close,dat.far_away);  
   int m = alsps_write_nvram(&dat);
 //__android_log_print(4, LOG_TAG, "m= %d ",m);  
     return m;
}

int get_ps_thd(void)
{

    //__android_log_print(4, LOG_TAG, "get_ps_thd");
    int handle = 0;
    handle = open("/dev/als_ps",O_RDONLY);
    if(handle < 0)
    {
        //__android_log_print(4, LOG_TAG, "get_ps_thd open file error 111 %d",handle);
        return handle;
    }
    int err = -1;

    if ((err == ioctl(handle, ALSPS_GET_PS_THD, &ps_thd_temp))) {
         //__android_log_print(4, LOG_TAG, "get_ps_thd readfile file ps_dat error");
		 
		 close(handle);
        return err;
    }
 //__android_log_print(4, LOG_TAG, "get_ps_thd   close %d  far_away%d valid%d",ps_thd_temp.close,ps_thd_temp.far_away,ps_thd_temp.valid);  


    if ((err = ioctl(handle, ALSPS_GET_PS_THD, &ps_thd_temp.close))) {
         //__android_log_print(4, LOG_TAG, "get_ps_thd readfile file ps_dat error");
		 
		close(handle);
        return err;
    }

    close(handle);
    return ps_thd_temp.close;

}




int get_ps_raw_cali(void)
{
    //__android_log_print(4, LOG_TAG, "get_ps_raw_cali");
    int handle = 0;
   handle = open("/dev/als_ps",O_RDONLY);
    if(handle < 0)
    {
        //__android_log_print(4, LOG_TAG, "get_ps_raw_cali open file error  %d",handle);
        return handle;
    }

    int err = -1;

    if ((err = ioctl(handle, ALSPS_GET_PS_RAW_DATA_FOR_CALI , &ps_cali_temp))) {
         //__android_log_print(4, LOG_TAG, "get_ps_raw_cali readfile file ps_dat error");
		 
		close(handle);
        return err;
    }

 //__android_log_print(4, LOG_TAG, "get_ps_raw_cali   close %d  far_away%d valid%d",ps_cali_temp.close ,ps_cali_temp.far_away ,ps_cali_temp.valid);    

    if ((err = ioctl(handle, ALSPS_SET_PS_CALI , &ps_cali_temp))) {
         //__android_log_print(4, LOG_TAG, "get_ps_raw_cali readfile file ps_dat error");
		 
		close(handle);
        return err;
    }
    close(handle);
    return ps_cali_temp.valid;
}



/*
 int get_ps_data(void) {
  // __android_log_print(4, LOG_TAG, "get_ps_data");
    int handle = 0;
    handle = open("/dev/als_ps",O_RDONLY);
//sleep(1);
    if(handle < 0)
    {
        __android_log_print(4, LOG_TAG, "get_ps_data open file error 111 %d",handle);
        return handle;
    }

    int err = -1;
    int ps_dat;
    if ((err = ioctl(handle, ALSPS_GET_PS_RAW_DATA, &ps_dat))) {
         __android_log_print(4, LOG_TAG, "get_ps_data readfile file ps_dat error");
		 
		close(handle);
        return err;
    }


    close(handle);
    return ps_dat;
}
*/

/*
 int get_als_data(void)
{
    __android_log_print(4, LOG_TAG, "get_als_data");
    int handle = 0;
    handle = open("/dev/als_ps",O_RDONLY);
    if(handle < 0)
    {
        __android_log_print(4, LOG_TAG, "get_als_data open file error 111 %d",handle);
        return handle;
    }

    int err = -1;
    int als_dat;

    if ((err = ioctl(handle, ALSPS_GET_ALS_RAW_DATA, &als_dat))) {
         __android_log_print(4, LOG_TAG, "get_als_data readfile file ps_dat error");
		 
		close(handle);
        return err;
    }
 __android_log_print(4, LOG_TAG, "get_als_data %d",als_dat);    


    close(handle);
    return als_dat;
}
*/


 int set_ps_cali(void)
{
    //__android_log_print(4, LOG_TAG, "set_ps_cali");
 //__android_log_print(4, LOG_TAG, "get_ps_raw_cali   close %d  far_away%d valid%d",ps_cali_temp.close ,ps_cali_temp. far_away ,ps_cali_temp.valid);    
    int handle = 0;
    int err = -1;
    int als_dat;
    handle = open("/dev/als_ps",O_RDONLY);
    if(handle < 0)
    {
        //__android_log_print(4, LOG_TAG, "set_ps_cali open file error  %d",handle);
        return handle;
    }

    if ((err = ioctl(handle, ALSPS_SET_PS_CALI, &ps_cali_temp))) {
         //__android_log_print(4, LOG_TAG, "set_ps_cali readfile file ps_dat error");
		 
		close(handle);
        return err;
    }
 //__android_log_print(4, LOG_TAG, "set_ps_cali %d",err);    

    close(handle);
    return err;
}

/*
 int set_ps_cali3(void)
{
    __android_log_print(4, LOG_TAG, "set_ps_cali3");
	int handle = 0;
    int err = -1;
    int als_dat;
    handle = open("/dev/als_ps",O_RDONLY);
    if(handle < 0)
    {
        __android_log_print(4, LOG_TAG, "set_ps_cali3 open file error  %d",handle);
        return handle;
    }

    if ((err = ioctl(handle, ALSPS_SET_PS_CALI3, &ps_cali_far_temp))) {
         __android_log_print(4, LOG_TAG, "set_ps_cali3 readfile file ps_dat error");
		 
		close(handle);
        return err;
    }
 __android_log_print(4, LOG_TAG, "set_ps_cali3 %d",err);    

    close(handle);
    return err;
}
*/

void cit_SetAnalogReg(unsigned long offset, unsigned long value, unsigned long mask) {
	cit_Register_Control.offset = offset;
	cit_Register_Control.value = value;
	cit_Register_Control.mask = mask | 0xffff0000; 
}

JNIEXPORT jint JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_setLeftSpeaker
(JNIEnv *env, jobject obj)
{
	//__android_log_print(4, LOG_TAG, "setLeftSpeaker");
	int handle = 0;
	int err = -1;
	
	handle = open("/dev/eac",O_RDONLY);
	if(handle < 0)
	{
	    //__android_log_print(4, LOG_TAG, "setLeftSpeaker open file error  %d",handle);
	    return handle;
	}

	if ((err = ioctl(handle, SET_SPEAKER_ON_CIT, NULL))) {
	     //__android_log_print(4, LOG_TAG, "setLeftSpeaker readfile file SET_SPEAKER_ON_CIT error");
	    return err;
	}
	//__android_log_print(4, LOG_TAG, "111setLeftSpeaker %d",err); 
	
	cit_SetAnalogReg(0x0700,0x0006,0xffff);
	if ((err = ioctl(handle, SET_ANAAFE_REG, &cit_Register_Control))) {
         //__android_log_print(4, LOG_TAG, "setRightSpeaker readfile file SET_ANAAFE_REG error");
        return err;
    }
	//__android_log_print(4, LOG_TAG, "222setLeftSpeaker %d",err);    
	
	close(handle);
	return err;
}
	
JNIEXPORT jint JNICALL Java_com_magcomm_factorytest_util_FactoryTestJNI_setRightSpeaker
(JNIEnv *env, jobject obj)
{
	//__android_log_print(4, LOG_TAG, "setRightSpeaker");
	int handle = 0;
    int err = -1;
    
    handle = open("/dev/eac",O_RDONLY);
    if(handle < 0)
    {
        //__android_log_print(4, LOG_TAG, "setRightSpeaker open file error  %d",handle);
        return handle;
    }

    if ((err = ioctl(handle, SET_SPEAKER_OFF_CIT, NULL))) {
         //__android_log_print(4, LOG_TAG, "setRightSpeaker readfile file SET_SPEAKER_OFF_CIT error");
        return err;
    }
     //__android_log_print(4, LOG_TAG, "000setRightSpeaker %d",err); 
	
 	cit_SetAnalogReg(0x0700,0x000f,0xffff);
	if ((err = ioctl(handle, SET_ANAAFE_REG, &cit_Register_Control))) {
         //__android_log_print(4, LOG_TAG, "setRightSpeaker readfile file SET_ANAAFE_REG error");
        return err;
    }
	//__android_log_print(4, LOG_TAG, "222setLeftSpeaker %d",err); 

    close(handle);
    return err;
}
