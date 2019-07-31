package mtkDrmServer
import (
    "android/soong/android"
    "android/soong/cc"
)
func mtkDrmServerDefaults(ctx android.LoadHookContext) {
    type props struct {
        Target struct {
            Android struct {
                Cflags []string
                Shared_libs []string
            }
        }
    }
    p := &props{}
    vars := ctx.Config().VendorConfig("mtkPlugin")
    if vars.Bool("MTK_DRM_APP") {
        if vars.Bool("MTK_OMADRM_SUPPORT") || vars.Bool("MTK_CTA_SET") {
            p.Target.Android.Cflags = append(p.Target.Android.Cflags, "-DMTK_OMA_DRM_SUPPORT")
            p.Target.Android.Shared_libs = append(p.Target.Android.Shared_libs, "libdrmmtkutil")
        }
    } else if vars.Bool("MTK_WVDRM_SUPPORT") {
        p.Target.Android.Cflags = append(p.Target.Android.Cflags, "-DMTK_WV_DRM_SUPPORT")
        p.Target.Android.Shared_libs = append(p.Target.Android.Shared_libs, "libdrmmtkutil")
    }
    ctx.AppendProperties(p)
}

func init() {
    android.RegisterModuleType("mtk_drm_server_defaults", mtkDrmServerDefaultsFactory)
}
func mtkDrmServerDefaultsFactory() (android.Module) {
    module := cc.DefaultsFactory()
    android.AddLoadHook(module, mtkDrmServerDefaults)
    return module
}
