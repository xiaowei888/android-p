package mediametrics

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkLibMediametricsDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_GMO_RAM_OPTIMIZE") {
		p.Cflags = append(p.Cflags, "-DMTK_DISABLE_METRICS")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libmediametrics_defaults", mtkLibMediametricsDefaultsFactory)
}

func mtkLibMediametricsDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkLibMediametricsDefaults)
	return module
}
