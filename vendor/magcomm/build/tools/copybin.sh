#!/bin/bash
###########################################################################
# Copyright Statement:
# --------------------
# This software is protected by Copyright and the information contained
# herein is confidential. The software may not be copied and the information
# contained herein may not be used or disclosed except with the written
# permission of Magcomm Inc. (C) 2015
# -----------------
# Author : y.haiyang
# Version: V1.0
# Update : 2014-06-02
############################################################################
readonly DEFAULT_BASE_SAVE_PATH=$HOME/DownloadVersion
readonly MAIN_MK_HEADER="full_"
readonly BASE_OUT_PATH="out/target/product/"
readonly BUILD_INFO_PATH="system/build.prop"

readonly DISPLAY_TAG="ro.build.display.id"
readonly MODEL_TAG="ro.product.model"
readonly DEVICE_TAG="ro.product.device"
readonly VERSION_TYPE_TAG="ro.build.type"

COPY_DOWNLOAD_FILES=(
"preloader_*"  "lk.bin"         "*.img"
"logo.bin"	"logo-verified.bin"     "trustzone.bin"   "*_Android_scatter.txt" "*.zip" )

CAN_PRINT_INFO=true

function check_choise_vesion ()
{
    local answer
    if [ "$1" ] ; then
        answer=$1
    else
        print_lunch_menu
        echo -n "Which version you want to compile? [full-base] "
        read answer
    fi

    # 这有三种情况 
    # 1. 无参数的时候在menu 里面什么也没输入
    # 2. 输入数字
    # 3. 输入对应选项 例如：full_lc180-eng
    # 注意： 如果 输入的选项不符合规则 selection 为空
    local selection=
    if [ -z "$answer" ]
    then
        echo -n "Please choise version"
        exit 0
    elif (echo -n $answer | grep -q -e "^[0-9][0-9]*$")
    then
        if [ $answer -le ${#LUNCH_MENU_CHOICES[@]} ]
        then
            selection=${LUNCH_MENU_CHOICES[$(($answer-1))]}
        fi
    elif (echo -n $answer | grep -q -e "^[^\-][^\-]*-[^\-][^\-]*$")
    then
        selection=$answer
    fi
    # 如果选择的是空 则打印选择并回调此函数（无参）
    if [ -z "$selection" ]
        then
        echo
        echo "Invalid version combo: $answer"
        check_choise_vesion
    else
        FULL_TARGET_DEVICE=$selection
    fi

}

#在这个方法中不能再打印输出 否则会导出参数有问题
function parseFullTargetDevice()
{
    local target=$1
    target=`echo $target |awk -F '-' '{print $1}'`
    target=${target:${#MAIN_MK_HEADER}}
    echo "$target"

}


function getInfo()
{

    if [ -d "$OUT" ] ; then
        local info=$OUT"/"$BUILD_INFO_PATH
        DISPLAY_ID=$(grep $DISPLAY_TAG  $info | awk -F'=' '{print $2}')
        DISPLAY_ID=$(echo $DISPLAY_ID | tr -d '[:blank:]')  #去掉id里面的空格

        MODEL=$(grep $MODEL_TAG  $info | awk -F'=' '{print $2}')
        MODEL=$(echo $MODEL | tr -d '[:blank:]')  #去掉id里面的空格
 
        #DEVICE=$(grep $DEVICE_TAG  $info | awk -F'=' '{print $2}')
        #DEVICE=$(echo $DEVICE | tr -d '[:blank:]')  #去掉id里面的空格

        VERSION_TYPE=$(grep $VERSION_TYPE_TAG  $info | awk -F'=' '{print $2}')
        VERSION_TYPE=$(echo $VERSION_TYPE | tr -d '[:blank:]')  #去掉id里面的空格


        echo DISPLAY_ID=$DISPLAY_ID
        echo MODEL=$MODEL
        echo DEVICE=$DEVICE

    else
        echo OUT不存在
    fi

}

function getOutPath ()
{
    check_choise_vesion $1
    DEVICE=$(parseFullTargetDevice $FULL_TARGET_DEVICE)
    OUT=$BASE_OUT_PATH$DEVICE
}

function getDeviceByOut(){
    OUT_BASE=$(dirname $OUT)"/"
    DEVICE=${OUT:${#OUT_BASE}}
}
#######################################
# Start
#######################################

for tag in $* ; do

    if [[ $tag = "clear" ]] ; then
        CAN_PRINT_INFO="false"
    fi
done

source build/envsetup.sh 
if [ $# -eq 0 ] ; then 
    if [ -z "$TARGET_PRODUCT" ] ; then
        getOutPath
    else
        getDeviceByOut
    fi
else
    
    if [  "$1" = "-b"  -a  "$3" = "-d" ] ; then
        FULL_TARGET_DEVICE=$2
        SAVE_PATH=$4
        getOutPath $FULL_TARGET_DEVICE    
    elif [[ "$1" = "-d" ]] ; then
        SAVE_PATH=$2
        if [ -z "$TARGET_PRODUCT" ] ; then
            getOutPath
        else
            getDeviceByOut
        fi
    elif [[ "$1" = "-b" ]] ; then
        FULL_TARGET_DEVICE=$2
        getOutPath $FULL_TARGET_DEVICE
    
    else
        FULL_TARGET_DEVICE=$1
        SAVE_PATH=$2
        getOutPath $FULL_TARGET_DEVICE
    fi

fi

getInfo
if [ -z "$SAVE_PATH" ] ; then
    time=$(date +%m%d_%H%M)
    SAVE_PATH=$DEFAULT_BASE_SAVE_PATH/$MODEL/$DISPLAY_ID"_"$time
fi

PROJECT_CONFIG="$PWD/device/mediateksample/$DEVICE/ProjectConfig.mk"
modom=$(grep "CUSTOM_MODEM"  $PROJECT_CONFIG | awk -F'=' '{print $2}')
echo "modom = $modom"
modoms=($modom)

#
if [ ! -d "$SAVE_PATH" ] ; then 
  echo "路径不存在创建路径：$SAVE_PATH"
  mkdir -p $SAVE_PATH
fi


for file in ${COPY_DOWNLOAD_FILES[@]}
do
   echo "复制 $file中..."
   cp -a $OUT/$file  $SAVE_PATH
done

MODEM_PATH="$PWD/vendor/mediatek/proprietary/modem/"
SAVE_PATH_MODOM=$SAVE_PATH"/modemdb"
mkdir -p $SAVE_PATH_MODOM
for modom in ${modoms[@]}
do
   echo "复制 $modom中..."
   cp -a $MODEM_PATH$modom/MDDB*  $SAVE_PATH_MODOM
done

#apdb
APDB_PATH="$PWD/out/target/product/$DEVICE/obj/CGEN"
SAVE_PATH_APDB=$SAVE_PATH"/apdb"
mkdir -p $SAVE_PATH_APDB
cp -a $APDB_PATH/APDB_* $SAVE_PATH_APDB

# Memory info
INFO_PATH="$PWD/vendor/mediatek/proprietary/bootable/bootloader/preloader/custom/$DEVICE/inc/"
##INFO_PATH="$PWD/bootable/bootloader/preloader/custom/$DEVICE/inc/"
SAVE_PATH_INFO=$SAVE_PATH"/info"
mkdir -p $SAVE_PATH_INFO
cp -a $INFO_PATH"custom_MemoryDevice.h" $SAVE_PATH_INFO

if [ $CAN_PRINT_INFO == true ] ; then
    echo -e "\e[01;32m*************************************************\e[0m"
    echo -e "\e[01;32m*\e[0m"
    echo -e "\e[01;32m*\e[0m  版本信息："
    echo -e "\e[01;32m*\e[0m    设备名称：\e[01;34m$DEVICE\e[0m"
    echo -e "\e[01;32m*\e[0m    版本类型：\e[01;34m$VERSION_TYPE\e[0m"
    echo -e "\e[01;32m*\e[0m    软件版本：\e[01;34m$DISPLAY_ID\e[0m"
    echo -e "\e[01;32m*\e[0m    版本型号：\e[01;34m$MODEL\e[0m"
    echo -e "\e[01;32m*\e[0m    存放路径：\e[01;34m$SAVE_PATH\e[0m"
    echo -e "\e[01;32m*\e[0m"
    echo -e "\e[01;32m*************************************************\e[0m"
fi
