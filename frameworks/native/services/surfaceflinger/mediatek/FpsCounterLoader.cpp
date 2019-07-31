#include "FpsCounterLoader.h"
#include <dlfcn.h>
#include <log/log.h>

namespace android {

ANDROID_SINGLETON_STATIC_INSTANCE(FpsCounterLoader);

FpsCounterLoader::FpsCounterLoader()
{
    mSoHandle = dlopen("libui_ext_fwk.so", RTLD_LAZY);

    if (mSoHandle) {
        mFpsCounterCreateFunc = reinterpret_cast<createPrototype>(dlsym(mSoHandle, "createFpsCounter"));
        if (NULL == mFpsCounterCreateFunc) {
            ALOGE("finding createFpsCounter() failed [%s]", dlerror());
        }

        mFpsCounterDestroyFunc = reinterpret_cast<destroyPrototype>(dlsym(mSoHandle, "destroyFpsCounter"));
        if (NULL == mFpsCounterDestroyFunc) {
            ALOGE("finding destroyFpsCounter() failed [%s]", dlerror());
        }

        mFpsCounterUpdateFunc = reinterpret_cast<updatePrototype>(dlsym(mSoHandle, "updateFpsCounter"));
        if (NULL == mFpsCounterUpdateFunc) {
            ALOGE("finding updateFpsCounter() failed [%s]", dlerror());
        }

        mFpsCounterDumpFunc = reinterpret_cast<dumpPrototype>(dlsym(mSoHandle, "dumpFpsCounter"));
        if (NULL == mFpsCounterDumpFunc) {
            ALOGE("finding dumpFpsCounter() failed [%s]", dlerror());
        }
    } else {
        ALOGE("open libged_kpi.so failed");
    }
}

FpsCounterLoader::~FpsCounterLoader()
{
    if(mSoHandle != NULL) {
        dlclose(mSoHandle);
    }
}

FpsCounter* FpsCounterLoader::create()
{
    if (NULL == mFpsCounterCreateFunc) {
        return NULL;
    }

    return mFpsCounterCreateFunc();
}

void FpsCounterLoader::destroy(FpsCounter* hnd)
{
    if (NULL == mFpsCounterDestroyFunc) {
        return;
    }

    mFpsCounterDestroyFunc(hnd);
}

void FpsCounterLoader::update(FpsCounter* hnd, String8 displayName, DisplayDevice::DisplayType type)
{
    if (NULL == mFpsCounterUpdateFunc) {
        return;
    }

    if (true == mFpsCounterUpdateFunc(hnd))
    {
        ALOGI("[%s (type:%d)] fps:%f,dur:%.2f,max:%.2f,min:%.2f",
            displayName.string(), type,
            static_cast<double>(hnd->getFps()),
            hnd->getLastLogDuration() / 1e6,
            hnd->getMaxDuration() / 1e6,
            hnd->getMinDuration() / 1e6);
    }
}

void FpsCounterLoader::dump(FpsCounter* hnd, String8* result, const char* prefix)
{
    if (NULL == mFpsCounterDumpFunc) {
        return;
    }

    mFpsCounterDumpFunc(hnd, result, prefix);
}

}; // namespace android