package mediaplayerservice

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkMediaplayerserviceDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags       []string
		Include_dirs []string
		Shared_libs  []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")

	if vars.Bool("MTK_DRM_APP") {
		p.Cflags = append(p.Cflags, "-DMTK_DRM_APP")
		p.Include_dirs = append(p.Include_dirs, "frameworks/av/media/libmtkavenhancements")
		p.Shared_libs = append(p.Shared_libs, "libdrmframework", "libfw_drmutils")
	}
	if vars.Bool("MTK_SLOW_MOTION_VIDEO_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_SLOW_MOTION_VIDEO_SUPPORT")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libmediaplayerservice_defaults", mtkMediaplayerserviceDefaultsFactory)
}

func mtkMediaplayerserviceDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkMediaplayerserviceDefaults)
	return module
}
