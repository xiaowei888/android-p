package alac

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkAlacextractorDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
		Srcs   []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_AUDIO_ALAC_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_ALAC_SUPPORT")
		p.Srcs = append(p.Srcs, "CAFExtractor.cpp")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_alacextractor_defaults", mtkAlacextractorDefaultsFactory)
}

func mtkAlacextractorDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkAlacextractorDefaults)
	return module
}
