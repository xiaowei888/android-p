#ifndef ANDROID_MTK_FPS_COUNTER_LOADER_H
#define ANDROID_MTK_FPS_COUNTER_LOADER_H

#include <utils/Singleton.h>
#include <utils/String8.h>
#include <utils/Timers.h>
#include <ui_ext/FpsCounter.h>
#include <DisplayDevice.h>

namespace android {
// ---------------------------------------------------------------------------

typedef FpsCounter* (*createPrototype)();
typedef void (*destroyPrototype)(FpsCounter* hnd);
typedef bool (*updatePrototype)(FpsCounter* hnd);
typedef void (*dumpPrototype)(FpsCounter* hnd, String8* result, const char* prefix);

class FpsCounterLoader : public Singleton<FpsCounterLoader>
{
public:
    FpsCounterLoader();
    ~FpsCounterLoader();

    FpsCounter* create();
    void destroy(FpsCounter* hnd);
    void update(FpsCounter* hnd, String8 name, DisplayDevice::DisplayType mType);
    void dump(FpsCounter* hnd, String8* result, const char* prefix);
private:
    void* mSoHandle;
    FpsCounter* (*mFpsCounterCreateFunc)();
    void (*mFpsCounterDestroyFunc)(FpsCounter* hnd);
    bool (*mFpsCounterUpdateFunc)(FpsCounter* hnd);
    void (*mFpsCounterDumpFunc)(FpsCounter* hnd, String8* result, const char* prefix);
};

// ---------------------------------------------------------------------------
}; // namespace android

#endif // ANDROID_MTK_FPS_COUNTER_LOADER_H