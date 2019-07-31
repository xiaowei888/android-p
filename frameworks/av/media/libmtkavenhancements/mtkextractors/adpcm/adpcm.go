package adpcm

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkAdpcmextractorDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
		Srcs   []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_AUDIO_ADPCM_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_ADPCM_SUPPORT")
		p.Srcs = append(p.Srcs, "MtkADPCMExtractor.cpp")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_adpcmextractor_defaults", mtkAdpcmextractorDefaultsFactory)
}

func mtkAdpcmextractorDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkAdpcmextractorDefaults)
	return module
}
