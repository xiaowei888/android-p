#!/bin/bash
function setCcache ()
{
    prebuilts/misc/linux-x86/ccache/ccache -M 75G
}

function getCCacheInfo()
{
    watch -n1 -d prebuilts/misc/linux-x86/ccache/ccache -s
}


if [ $# -eq 0 ] ; then
echo ""
echo "功能列表"
echo "    1. cache       设置Ccache 缓存"
echo "    2. cacheinfo   获取Ccache 信息"
echo ""
exit 0
fi

TAG=$1

if [ $TAG == "cache" ] ; then
    setCcache
elif [ $TAG == "cacheinfo" ] ; then
    getCCacheInfo
fi


