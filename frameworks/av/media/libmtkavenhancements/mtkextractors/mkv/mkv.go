package mkv

import (
    "android/soong/android"
    "android/soong/cc"
)

func mtkmkvextractorDefaults(ctx android.LoadHookContext) {
    type props struct {
        Cflags []string
    }
    p := &props{}
    vars := ctx.Config().VendorConfig("mtkPlugin")
    if vars.Bool("MTK_MKV_PLAYBACK_ENHANCEMENT") {
        p.Cflags = append(p.Cflags, "-DMTK_MKV_PLAYBACK_ENHANCEMENT")
    }
    ctx.AppendProperties(p)
}

func init() {
    android.RegisterModuleType("mtk_mkvextractor_defaults", mtkmkvextractorDefaultsFactory)
}

func mtkmkvextractorDefaultsFactory() android.Module {
    module := cc.DefaultsFactory()
    android.AddLoadHook(module, mtkmkvextractorDefaults)
    return module
}
