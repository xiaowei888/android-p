package wavextractor

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkLibWavextractorDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_HIGH_RESOLUTION_AUDIO_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_HIGH_RESOLUTION_AUDIO_SUPPORT")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libwavextractor_defaults", mtkLibWavextractorDefaultsFactory)
}

func mtkLibWavextractorDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkLibWavextractorDefaults)
	return module
}
