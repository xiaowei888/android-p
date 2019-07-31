//#define LOG_NDEBUG 0
//#define MTK_LOG_ENABLE 1
#include <cmath>
#include <dlfcn.h>

#include <cutils/properties.h>
#include <log/log.h>
#include <binder/IPCThreadState.h>

#include <gui/BufferQueueCore.h>

#include <gui/mediatek/GedKpiDebug.h>

#include <gedkpi/GedKpiWrap_def.h>

namespace android {

GedKpiDebug::GedKpiDebug() :
    mBq(NULL),
    mConnectedApi(BufferQueueCore::NO_CONNECTED_API),
    mProducerPid(-1),
    mGedKpiCreateWrap(NULL),
    mGedKpiDestroyWrap(NULL),
    mGedKpiDequeueBufferTagWrap(NULL),
    mGedKpiQueueBufferTagWrap(NULL),
    mGedKpiAcquireBufferTagWrap(NULL),
    mGedHnd(NULL)
{
}

GedKpiDebug::~GedKpiDebug() {
}

void GedKpiDebug::onConstructor(wp<BufferQueueCore> bq, const uint64_t& bqId) {
    mBq = bq;
    mGedKpiCreateWrap = GedKpiModuleLoader::getInstance().GedKpiCreate();
    mGedKpiDestroyWrap = GedKpiModuleLoader::getInstance().GedKpiDestroy();
    mGedKpiDequeueBufferTagWrap = GedKpiModuleLoader::getInstance().GedKpiDequeue();
    mGedKpiQueueBufferTagWrap = GedKpiModuleLoader::getInstance().GedKpiQueue();
    mGedKpiAcquireBufferTagWrap = GedKpiModuleLoader::getInstance().GedKpiAcquire();

    if (mGedKpiCreateWrap) {
        mGedHnd = mGedKpiCreateWrap(bqId);
        if (mGedHnd == nullptr)
            ALOGE("an error ged handle");
    }
    else
    {
        ALOGE("finding createWrapPrototype() failed");
    }
}

void GedKpiDebug::onDestructor() {
    if (mGedHnd != nullptr)
    {
        if (mGedKpiCreateWrap) {
            mGedKpiDestroyWrap(mGedHnd);
        } else {
            ALOGE("finding createWrapPrototype() failed");
        }
    }
}

void GedKpiDebug::onAcquire(const sp<GraphicBuffer>& gb) {
    // notify ged about acquire events for fast DVFS
    const sp<BufferQueueCore> core = mBq.promote();
    if (core != nullptr && mGedHnd != nullptr && gb != nullptr) {
        if (mGedKpiAcquireBufferTagWrap)
        {
            const int32_t err = mGedKpiAcquireBufferTagWrap(mGedHnd, mProducerPid, reinterpret_cast<intptr_t>(gb->handle));
            if (err != GED_OK)
                ALOGE("ged queue fail: hnd:%p api:%d pid:%d size:%d" PRIu64 ,
                    mGedHnd, mConnectedApi, mProducerPid, static_cast<int>(core->mQueue.size()));
        }
    } else {
        if (core == nullptr)
            ALOGE("queueBuffer: BufferQueueCore promoting failed");
        else if (gb == nullptr)
            ALOGE("queueBuffer: gb is null");
        else
        {
            if (mGedKpiCreateWrap)
                ALOGE("an error ged handle");
        }
    }
}

void GedKpiDebug::onDequeue(sp<GraphicBuffer>& gb, sp<Fence>& fence) {
    const sp<BufferQueueCore> core = mBq.promote();
    if (core != nullptr && mGedHnd != nullptr && gb != nullptr) {
        const int32_t dupFenceFd = fence->isValid() ? fence->dup() : -1;
        if (mGedKpiDequeueBufferTagWrap)
        {
            const int32_t err = mGedKpiDequeueBufferTagWrap(
                mGedHnd, mConnectedApi, dupFenceFd, mProducerPid, reinterpret_cast<intptr_t>(gb->handle));
            if (err != GED_OK)
                ALOGE("ged dequeue fail: hnd:%p api:%d pid:%d" PRIu64 ,
                    mGedHnd, mConnectedApi, mProducerPid);
        }
        ::close(dupFenceFd);
    } else {
        if (core == nullptr)
            ALOGE("dequeueBuffer: BufferQueueCore promoting failed");
        else if (gb == nullptr)
            ALOGE("dequeueBuffer: gb is null");
        else
        {
            if (mGedKpiCreateWrap)
                ALOGE("an error ged handle line:%d", __LINE__);
        }
    }
}

void GedKpiDebug::onQueue(const sp<GraphicBuffer>& gb, const sp<Fence>& fence) {
    const sp<BufferQueueCore> core = mBq.promote();
    if (core != nullptr && mGedHnd != nullptr && gb != nullptr) {
        const int32_t dupFenceFd = fence->isValid() ? fence->dup() : -1;
        if (mGedKpiQueueBufferTagWrap)
        {
            const int32_t err = mGedKpiQueueBufferTagWrap(
                mGedHnd, mConnectedApi, dupFenceFd, mProducerPid, static_cast<int32_t>(core->mQueue.size()), reinterpret_cast<intptr_t>(gb->handle));
            if (err != GED_OK)
                ALOGE("ged queue fail: hnd:%p api:%d pid:%d size:%d" PRIu64 ,
                    mGedHnd, mConnectedApi, mProducerPid, static_cast<int>(core->mQueue.size()));
        }
        ::close(dupFenceFd);
    } else {
        if (core == nullptr)
            ALOGE("queueBuffer: BufferQueueCore promoting failed");
        else if (gb == nullptr)
            ALOGE("queueBuffer: gb is null");
        else
        {
            if (mGedKpiCreateWrap)
                ALOGE("an error ged handle");
        }
    }
}

void GedKpiDebug::onProducerConnect(const sp<IBinder>& token, const int api) {
    IPCThreadState* ipc = IPCThreadState::selfOrNull();
    mProducerPid = (token != NULL && NULL != token->localBinder())
        ? getpid()
        : (ipc != nullptr)?ipc->getCallingPid():-1;
    mConnectedApi = api;
}

void GedKpiDebug::onProducerDisconnect() {
    mProducerPid = -1;
}
// -----------------------------------------------------------------------------
ANDROID_SINGLETON_STATIC_INSTANCE(GedKpiModuleLoader);

GedKpiModuleLoader::GedKpiModuleLoader() :
    mGedKpiSoHandle(NULL),
    mGedKpiCreate(NULL),
    mGedKpiDestroy(NULL),
    mGedKpiDequeueBuffer(NULL),
    mGedKpiQueueBuffer(NULL),
    mGedKpiAcquireBuffer(NULL)
{
    // used to notify ged about queue/acquire events for fast DVFS
    mGedKpiSoHandle = dlopen("libged_kpi.so", RTLD_LAZY);
    if (mGedKpiSoHandle) {
        mGedKpiCreate = reinterpret_cast<createWrapPrototype>(dlsym(mGedKpiSoHandle, "ged_kpi_create_wrap"));
        if (NULL == mGedKpiCreate) {
            ALOGE("finding createWrapPrototype() failed [%s]", dlerror());
        }

        mGedKpiDestroy = reinterpret_cast<destroyWrapPrototype>(dlsym(mGedKpiSoHandle, "ged_kpi_destroy_wrap"));
        if (NULL == mGedKpiDestroy) {
            ALOGE("finding destroyWrapPrototype() failed [%s]", dlerror());
        }

        mGedKpiDequeueBuffer = reinterpret_cast<dequeueBufferTagWrapPrototype>(dlsym(mGedKpiSoHandle, "ged_kpi_dequeue_buffer_tag_wrap"));
        if (NULL == mGedKpiDequeueBuffer) {
            ALOGE("finding dequeueBufferTagWrapPrototype() failed [%s]", dlerror());
        }

        mGedKpiQueueBuffer = reinterpret_cast<queueBufferTagWrapPrototype>(dlsym(mGedKpiSoHandle, "ged_kpi_queue_buffer_tag_wrap"));
        if (NULL == mGedKpiQueueBuffer) {
            ALOGE("finding queueBufferTagWrapPrototype() failed [%s]", dlerror());
        }

        mGedKpiAcquireBuffer = reinterpret_cast<acquireBufferTagWrapPrototype>(dlsym(mGedKpiSoHandle, "ged_kpi_acquire_buffer_tag_wrap"));
        if (NULL == mGedKpiAcquireBuffer) {
            ALOGE("finding acquireBufferTagWrapPrototype() failed [%s]", dlerror());
        }
    } else {
        ALOGE("open libged_kpi.so failed");
    }
}

GedKpiModuleLoader::~GedKpiModuleLoader() {
    if(mGedKpiSoHandle != NULL) {
        dlclose(mGedKpiSoHandle);
    }
}

createWrapPrototype GedKpiModuleLoader::GedKpiCreate() {
    if (mGedKpiCreate) {
        return mGedKpiCreate;
    } else {
        return NULL;
    }
}

destroyWrapPrototype GedKpiModuleLoader::GedKpiDestroy() {
    if (mGedKpiDestroy) {
        return mGedKpiDestroy;
    } else {
        return NULL;
    }
}

dequeueBufferTagWrapPrototype GedKpiModuleLoader::GedKpiDequeue() {
    if (mGedKpiDequeueBuffer) {
        return mGedKpiDequeueBuffer;
    } else {
        return NULL;
    }
}

queueBufferTagWrapPrototype GedKpiModuleLoader::GedKpiQueue() {
    if (mGedKpiQueueBuffer) {
        return mGedKpiQueueBuffer;
    } else {
        return NULL;
    }
}

acquireBufferTagWrapPrototype GedKpiModuleLoader::GedKpiAcquire() {
    if (mGedKpiAcquireBuffer) {
        return mGedKpiAcquireBuffer;
    } else {
        return NULL;
    }
}

}; // namespace android
