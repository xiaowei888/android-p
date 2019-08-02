package audioclient

import (
	"android/soong/android"
	"android/soong/cc"
)
func mtkLibAudioclientDefaults(ctx android.LoadHookContext) {
	type props struct {
		Cflags []string
	}
	p := &props{}
	vars := ctx.Config().VendorConfig("mtkPlugin")
	if vars.Bool("MTK_AUDIO") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIO", "-DMTK_AUDIO_DEBUG", "-DMTK_AUDIO_FIX_DEFAULT_DEFECT", "-DMTK_AUDIO_GAIN")
	}
	if vars.Bool("MTK_FM_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_FM_SUPPORT")
	}
	if vars.Bool("MTK_TTY_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_TTY_SUPPORT")
	}
	if vars.Bool("MTK_HAC_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_HAC_SUPPORT")
	}
	if vars.Bool("MTK_HIFIAUDIO_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_HIFIAUDIO_SUPPORT")
	}
	if vars.Bool("MTK_BESLOUDNESS_SUPPORT") {
		p.Cflags = append(p.Cflags, "-DMTK_AUDIOMIXER_ENABLE_DRC")
	}
	ctx.AppendProperties(p)
}

func init() {
	android.RegisterModuleType("mtk_libaudioclient_defaults", mtkLibAudioclientDefaultsFactory)
}

func mtkLibAudioclientDefaultsFactory() android.Module {
	module := cc.DefaultsFactory()
	android.AddLoadHook(module, mtkLibAudioclientDefaults)
	return module
}