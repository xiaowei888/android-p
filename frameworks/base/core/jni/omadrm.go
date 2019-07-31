package omadrm

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkOmadrmDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_OMADRM_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_OMADRM_SUPPORT")
	}

	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libandroid_runtime_defaults", mtkOmadrmDefaultsFactory)
}

func mtkOmadrmDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkOmadrmDefaults)
	return module
}
