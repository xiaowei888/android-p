package flacextractor

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkLibFlacextractorDefaults(ctx android.LoadHookContext) {
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
	android.RegisterModuleType("mtk_libflacextractor_defaults", mtkLibFlacextractorDefaultsFactory)
}

func mtkLibFlacextractorDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkLibFlacextractorDefaults)
	return module
}
