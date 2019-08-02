package mp4extrator

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkLibmp4extratorDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_AUDIO_ALAC_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_ALAC_SUPPORT")
	}
	if vars.Bool("MTK_AUDIO_RAW_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_RAW_SUPPORT")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libmp4extractor_defaults", mtkLibmp4extratorDefaultsFactory)
}

func mtkLibmp4extratorDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkLibmp4extratorDefaults)
	return module
}
