package libstagefright

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkLibstagefrightDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags       []string
		Include_dirs []string
		Shared_libs  []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_AUDIO_ADPCM_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_ADPCM_SUPPORT")
	}
	if vars.Bool("MTK_HIGH_RESOLUTION_AUDIO_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_HIGH_RESOLUTION_AUDIO_SUPPORT")
	}
	if vars.Bool("MTK_VILTE_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_VILTE_SUPPORT")
	}
	if vars.Bool("MTK_THUMBNAIL_OPTIMIZATION") {
		p.Cflags = append(p.Cflags, "-DMTK_THUMBNAIL_OPTIMIZATION")
	}
	if vars.Bool("MTK_AUDIO_ALAC_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_ALAC_SUPPORT")
	}
	if vars.Bool("MTK_AUDIO_APE_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_APE_SUPPORT")
	}
	if vars.Bool("MTK_DRM_APP") {
		p.Cflags = append(p.Cflags, "-DMTK_DRM_APP")
		p.Include_dirs = append(p.Include_dirs, "frameworks/av/media/libmtkavenhancements")
		p.Shared_libs = append(p.Shared_libs, "libfw_drmutils")
	}
	if vars.Bool("MTK_SLOW_MOTION_VIDEO_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_SLOW_MOTION_VIDEO_SUPPORT")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libstagefright_defaults", mtkLibstagefrightDefaultsFactory)
}

func mtkLibstagefrightDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkLibstagefrightDefaults)
	return module
}
