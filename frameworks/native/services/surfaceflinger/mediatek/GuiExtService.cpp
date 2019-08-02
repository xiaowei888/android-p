/*
* Copyright (C) 2018 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/

#include <log/log.h>
#include <dlfcn.h>

#ifdef MTK_SF_DEBUG_SUPPORT

void createGuiExtService() {
    // publish GuiExt service
    void* soHandle = dlopen("libgui_ext.so", RTLD_LAZY);
    if (soHandle) {
        void (*createGuiExtPtr)();
        createGuiExtPtr = (decltype(createGuiExtPtr))(dlsym(soHandle, "createGuiExtService"));
        if (NULL == createGuiExtPtr) {
            dlclose(soHandle);
            ALOGE("finding createGuiExtService() failed");
        } else {
            createGuiExtPtr();
        }
        dlclose(soHandle);
    } else {
        ALOGE("open libgui_ext failed");
    }
}

#endif
