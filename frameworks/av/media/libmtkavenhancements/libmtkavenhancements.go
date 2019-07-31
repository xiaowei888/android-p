package libmtkavenhancements

import (
	"android/soong/android"
	"android/soong/cc"
)

func mtkLibmtkavenhancementsDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags       []string
		Include_dirs []string
		Shared_libs  []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_WMV_PLAYBACK_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_WMV_PLAYBACK_SUPPORT")
	}
	if vars.Bool("MTK_FLV_PLAYBACK_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_FLV_PLAYBACK_SUPPORT")
	}
	if vars.Bool("MTK_MP2_PLAYBACK_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_MP2_PLAYBACK_SUPPORT")
	}
	if vars.Bool("MTK_AUDIO_ADPCM_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_ADPCM_SUPPORT")
	}
	if vars.Bool("MTK_AUDIO_ALAC_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_ALAC_SUPPORT")
	}
	if vars.Bool("MTK_AUDIO_APE_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO_APE_SUPPORT")
	}
	if vars.Bool("MTK_HIGH_RESOLUTION_AUDIO_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_HIGH_RESOLUTION_AUDIO_SUPPORT")
	}
	if vars.Bool("MTK_WMA_PLAYBACK_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_WMA_PLAYBACK_SUPPORT")
	}
	if vars.Bool("MTK_SWIP_WMAPRO") {
		p.Cflags = append(p.Cflags, "-DMTK_SWIP_WMAPRO")
	}
	if vars.Bool("MTK_DRM_APP") {
		p.Cflags = append(p.Cflags, "-DMTK_DRM_APP")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libmtkavenhancements_defaults", mtkLibmtkavenhancementsDefaultsFactory)
}

func mtkLibmtkavenhancementsDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkLibmtkavenhancementsDefaults)
	return module
}
