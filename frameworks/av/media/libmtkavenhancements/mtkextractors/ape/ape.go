package ape

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkApeextractorDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
		Srcs   []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_AUDIO_APE_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_APE_SUPPORT")
		p.Srcs = append(p.Srcs, "APEExtractor.cpp", "apetag.cpp")
	}
	if vars.Bool("MTK_HIGH_RESOLUTION_AUDIO_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_HIGH_RESOLUTION_AUDIO_SUPPORT")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_apeextractor_defaults", mtkApeextractorDefaultsFactory)
}

func mtkApeextractorDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkApeextractorDefaults)
	return module
}
