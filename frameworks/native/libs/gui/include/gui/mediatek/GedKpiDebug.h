#ifndef ANDROID_GUI_GUIKPIDEBUG_H
#define ANDROID_GUI_GUIKPIDEBUG_H

#include <utils/Singleton.h>
#include <gedkpi/GedKpiWrap_def.h>
#include <binder/IBinder.h>
// ----------------------------------------------------------------------------
namespace android {
// ----------------------------------------------------------------------------

typedef GED_KPI_HANDLE (*createWrapPrototype)(uint64_t BBQ_ID);
typedef void (*destroyWrapPrototype)(GED_KPI_HANDLE hKPI);
typedef GED_ERROR (*dequeueBufferTagWrapPrototype)(GED_KPI_HANDLE hKPI, int32_t BBQ_api_type, int32_t fence, int32_t pid, intptr_t buffer_addr);
typedef GED_ERROR (*queueBufferTagWrapPrototype)(GED_KPI_HANDLE hKPI, int32_t BBQ_api_type, int32_t fence, int32_t pid, int32_t QedBuffer_length, intptr_t buffer_addr);
typedef GED_ERROR (*acquireBufferTagWrapPrototype)(GED_KPI_HANDLE hKPI, int32_t pid, intptr_t buffer_addr);

class GedKpiDebug : public RefBase {
public:
    GedKpiDebug();
    ~GedKpiDebug();
    // BufferQueueCore part
    void onConstructor(wp<BufferQueueCore> bq, const uint64_t& bqId);
    void onDestructor();
    void onAcquire(const sp<GraphicBuffer>& gb);
    void onDequeue(sp<GraphicBuffer>& gb, sp<Fence>& fence);
    void onQueue(const sp<GraphicBuffer>& gb, const sp<Fence>& fence);
    void onProducerConnect(const sp<IBinder>& token, const int api);
    void onProducerDisconnect();
private:
    // debug target BQ info
    wp<BufferQueueCore> mBq;
    int mConnectedApi;
    // process info
    int32_t mProducerPid;

    GED_KPI_HANDLE (*mGedKpiCreateWrap)(uint64_t BBQ_ID);
    void (*mGedKpiDestroyWrap)(GED_KPI_HANDLE hKPI);
    GED_ERROR (*mGedKpiDequeueBufferTagWrap)(GED_KPI_HANDLE hKPI, int32_t BBQ_api_type, int32_t fence, int32_t pid, intptr_t buffer_addr);
    GED_ERROR (*mGedKpiQueueBufferTagWrap)(GED_KPI_HANDLE hKPI, int32_t BBQ_api_type, int32_t fence, int32_t pid, int32_t QedBuffer_length, intptr_t buffer_addr);
    GED_ERROR (*mGedKpiAcquireBufferTagWrap)(GED_KPI_HANDLE hKPI, int32_t pid, intptr_t buffer_addr);

    // used to notify ged about queue/acquire events for fast DVFS
    GED_KPI_HANDLE mGedHnd;
};

// -----------------------------------------------------------------------------
// GuiDebug loader for dl open libgui_debug
class GedKpiModuleLoader : public Singleton<GedKpiModuleLoader> {
public:
    GedKpiModuleLoader();
    ~GedKpiModuleLoader();

    createWrapPrototype GedKpiCreate();
    destroyWrapPrototype GedKpiDestroy();
    dequeueBufferTagWrapPrototype GedKpiDequeue();
    queueBufferTagWrapPrototype GedKpiQueue();
    acquireBufferTagWrapPrototype GedKpiAcquire();
private:
    //for Ged Kpi
    void* mGedKpiSoHandle;
    GED_KPI_HANDLE (*mGedKpiCreate)(uint64_t BBQ_ID);
    void (*mGedKpiDestroy)(GED_KPI_HANDLE hKPI);
    GED_ERROR (*mGedKpiDequeueBuffer)(GED_KPI_HANDLE hKPI, int32_t BBQ_api_type, int32_t fence, int32_t pid, intptr_t buffer_addr);
    GED_ERROR (*mGedKpiQueueBuffer)(GED_KPI_HANDLE hKPI, int32_t BBQ_api_type, int32_t fence, int32_t pid, int32_t QedBuffer_length, intptr_t buffer_addr);
    GED_ERROR (*mGedKpiAcquireBuffer)(GED_KPI_HANDLE hKPI, int32_t pid, intptr_t buffer_addr);
};
// ----------------------------------------------------------------------------
}; // namespace android
// ----------------------------------------------------------------------------
#endif // ANDROID_GUI_GUIKPIDEBUG_H
