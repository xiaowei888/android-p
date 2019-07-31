package libgui

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkLibguiDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_AFPSGO_FBT_GAME") {
		p.Cflags = append(p.Cflags, "-DMTK_AFPSGO_FBT_GAME")
	}

	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libgui_defaults", mtkLibguiDefaultsFactory)
}

func mtkLibguiDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkLibguiDefaults)
	return module
}
