package libmediajni

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtklibmediaJniDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_HIGH_QUALITY_THUMBNAIL") {
		p.Cflags = append(p.Cflags, "-DMTK_HIGH_QUALITY_THUMBNAIL")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libmedia_jni_defaults", mtklibmediaJniDefaultsFactory)
}

func mtklibmediaJniDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtklibmediaJniDefaults)
	return module
}
