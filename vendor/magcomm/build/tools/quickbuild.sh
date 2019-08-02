#!/bin/bash

# Copyright Statement:
# --------------------
# This software is protected by Copyright and the information contained
# herein is confidential. The software may not be copied and the information
# contained herein may not be used or disclosed except with the written
# permission of Magcomm Inc. (C) 2014
# -----------------
# Author : y.haiyang
# Version: V1.1.1
# Update : 2014-06-16
############################################################################

#常量区域
#DISPLAY_TAG="ro.custom.build.version"
readonly SAVE_PATH="/home/$USER/DownloadVersion"
readonly SERVER_PATH_1="git clone liubo@192.168.0.133:/home/liubo/git/"
readonly SERVER_PATH_2="git clone lenovo@192.168.0.6:/home/lenovo/magcomm_project/git/"
readonly TOOL_PATH="./vendor/magcomm/build/tools/" 
readonly LOG_FILE_PATH="./out/target/product"

readonly HOST_1="192.168.0.133"
readonly HOST_2="192.168.0.6"

readonly DISPLAY_TAG="ro.build.display.id"
readonly MODEL_TAG="ro.product.model"

VARIANT="user"
BUILD_TAG="normal"
BUILD_MODE_CONFIG="all"

BAK_IMG="no"

# 配置区域
if [[ $1 = "-d" ]] ; then
	BUILD_MODE_CONFIG="download_only"
	core_name=$2
	core_path=$3
elif [[ $1 = "-b" ]] ; then
	BUILD_MODE_CONFIG="build_only"
	project_name=$2
	core_path=$3
else
	core_name=$1
	project_name=$2
	core_path=$3
fi

#如果是空则是当前路径
if [ -z $core_path ] ; then
	core_path=$(dirname $(pwd))
fi

# 如果是 eng或者user或者ota 则是当前路径
if [ $core_path == "eng"  -o  $core_path == "user"  -o $core_path == "r" -o  $core_path == "new" ] ; then
	#core_path=$PWD/../
	core_path=$(dirname $(pwd))
fi

# 循环参数 取出 版本信息
# ota版本 or eng|user 版本
for tag in $* ; do

	if [[ $tag = "eng" ]] ; then
		VARIANT="eng"
	elif [[ $tag = "user" ]] ; then
		VARIANT="user"
	elif [[ $tag = "r" ]] ; then
		BUILD_TAG="r"
	elif [[ $tag = "new" ]] ; then
        BUILD_TAG="new"
    fi
done


#判断最后一个字符是否是 / 如果是 删除
last_str=${core_path:((${#core_path} - 1))}
if [ $last_str = "/" ] ; then
	core_path=${core_path%?}
fi
#  说明：打印加粗的红色的字符串
#  用法：传入 ${1}，要着重显示的字符串
function print_red_string() {
    echo -e "\e[01;31m${*}\e[0m"
}

#  说明：打印加粗的绿色的字符串
#  用法：传入 ${1}，要着重显示的字符串
function print_green_string() {
    echo -e "\e[01;32m${*}\e[0m"
}

#  说明：打印加粗的蓝色的字符串
#  用法：传入 ${1}，要着重显示的字符串
function print_blue_string() {
    echo -e "\e[01;34m${*}\e[0m"
}

#
# 获取Custom 文件夹
function get_custom_path(){
local flag="projects"

 for file in `ls $1`
    do
        local last=${file:((${#file} - 8))}
        if [[ $last  = $flag  ]] ; then
            echo $1"/"$file
        fi
    done

}
# 说明： 下载版本
# 用法： 传入 $1 $2
function download ()
{
  if [ ! -d $core_path ] ; then
	echo "路径不存在， 创建路径"
	mkdir -p $core_path
  else
	rm -rf $core_path
	mkdir -p $core_path
  fi

	cd $TOOL_PATH
    local result=$(./list_core.sh $1)
	local name=`echo $result |awk -F '#' '{print $1}'`
	local host=`echo $result |awk -F '#' '{print $2}'`
	cd -

  if [[ $host == $HOST_1 ]] ; then
    $SERVER_PATH_1$name $core_path
  elif [[ $host == $HOST_2 ]] ; then
    $SERVER_PATH_2$name $core_path
  fi
}

# 简单的打印
function print_can_down()
{
	cd $TOOL_PATH
	./list_core.sh
	exit 0
}

function print_config()
{
	echo
	echo -e "\e[01;34m++++++++++++++++++++++++++++++++++++++++\e[0m"
	echo
	echo " 拉取代码信息："
	echo -e "    拉取的代码：\e[01;31m$core_name\e[0m"
    if [ $BUILD_MODE_CONFIG = "all" ] ;	then
	    echo -e "    编译的工程：\e[01;31m$project_name\e[0m"
        echo -e "    编译的类型：\e[01;31m$VARIANT 版本\e[0m"
    fi
	    echo -e "    存放的路径：\e[01;31m$core_path\e[0m"
    if [  -d $core_path ] ; then
	    echo " "
	    echo -e "\e[01;31m 注意："
	    echo -e "   \e[01;31m 代码存放的路径已存在,路径将会被删除，并重新创建！"
    fi
	echo
	echo -e "\e[01;34m++++++++++++++++++++++++++++++++++++++++\e[0m"
	echo
}


if [[ $1 == "--help" ]] ;then
	cd $TOOL_PATH
    ./mk_help.sh
	exit 0
fi


if [[ $1 == "info" ]] ; then

    if [ -z "$2" ] ; then
        print_can_down
        exit 0
    fi

    custom_path=$(get_custom_path $core_path)
    configure_path=$custom_path/$2/device/magcomm/
    configure_file=$(find $configure_path -name "full_*.mk")

    if [ -n "$configure_file" ] ; then
        DEVICE=$(cat $configure_file | grep "^\s*PRODUCT_DEVICE" | sed 's/.*\s*:=\s*//g')
    else
        configure_file=$(find $configure_path -name "ProjectConfig.mk")
        DEVICE=${configure_file:${#configure_path}}
        DEVICE=$( echo $DEVICE | awk -F '/' '{print $1}')
    fi

    echo -e "\e[01;32m*************************************************\e[0m"
    echo -e "\e[01;32m*\e[0m"
    echo -e "\e[01;32m*\e[0m 项目\e[01;34m$2\e[0m可选择的Flag："
    echo -e "\e[01;32m*\e[0m"
    echo -e "\e[01;32m*\e[0m      \e[01;34m full_$DEVICE-eng\e[0m"
    echo -e "\e[01;32m*\e[0m"
    echo -e "\e[01;32m*\e[0m      \e[01;34m full_$DEVICE-user\e[0m"
    echo -e "\e[01;32m*\e[0m"
    echo -e "\e[01;32m*************************************************\e[0m"

    exit 0
fi


#下载代码
if [ $BUILD_MODE_CONFIG =  "download_only"  -o   $BUILD_MODE_CONFIG = "all" ] ;then
	# 下载代码
	# $# 参数的个数， 如果参数个数为0 则打印信息
	if [ $# == 0 ] ; then
		print_can_down
	fi

	if [ -z $core_name ] ; then
		echo
		print_blue_string "++++++++++++++++++++++++++++++++++++++++"
		echo  -e "\e[01;31m拉取的代码参数为空，请输入 。。。。\e[0m"
		print_can_down
		exit 0
	fi

	print_config
	CHECK_TIME=5
	while [ $CHECK_TIME -ge 0 ]
		do
		echo -e  "请确认拉取代码信息正确，\e[01;31m$CHECK_TIME \e[0m秒后开始下载版本"
		sleep 1
		let "CHECK_TIME-=1"
	done

	download $core_name $core_path

fi

#编译代码

if [ $BUILD_MODE_CONFIG = "build_only"  -o  $BUILD_MODE_CONFIG = "all"  ] ;then
	# 编译的路径
	mk_path=$core_path/alps
	#ProjectConfig路径
	custom_path=$(get_custom_path $core_path)

	#判断 客户路径是否为空
	if [  ! -d $custom_path ] ; then
		 print_red_string "* Error $custom_path 为空 "
		exit 0
	fi

	configure_path=$custom_path/$project_name/device/magcomm/
    configure_file=$(find $configure_path -name "full_*.mk")
    echo configure_file = $configure_file

    if [ -n "$configure_file" ] ; then
        DEVICE=$(cat $configure_file | grep "^\s*PRODUCT_DEVICE" | sed 's/.*\s*:=\s*//g')
    else
        configure_file=$(find $configure_path -name "ProjectConfig.mk")
        DEVICE=${configure_file:${#configure_path}}
        DEVICE=$( echo $DEVICE | awk -F '/' '{print $1}')
    fi

    echo DEVICE = $DEVICE

	cd  $mk_path
	if [  $BUILD_TAG = "r" ] ; then
		echo
		echo "WARNING : buildtag is $BUILD_TAG, mag is not runing.."
		echo
	else
		./mag  $project_name
    fi

    MK_INFO=$mk_path/build/tools/buildinfo.sh
    DISPLAY_ID=$(grep $DISPLAY_TAG  $MK_INFO | awk -F'=' '{print $2}')
	DISPLAY_ID=$(echo $DISPLAY_ID | cut -d '"' -f1)
	#去掉id里面的空格
	DISPLAY_ID=$(echo $DISPLAY_ID | tr -d '[:blank:]')
    MODEL=$(grep $MODEL_TAG  $MK_INFO | awk -F'=' '{print $2}')
	MODEL=$(echo $MODEL | cut -d '"' -f1)

    echo -e "\e[01;32m*************************************************\e[0m"
    echo -e "\e[01;32m*\e[0m"
    echo -e "\e[01;32m*\e[0m  版本信息："
    echo -e "\e[01;32m*\e[0m    项目 ：\e[01;34m$project_name\e[0m"
    echo -e "\e[01;32m*\e[0m    版本号 ：\e[01;34m$DISPLAY_ID\e[0m"
	echo -e "\e[01;32m*\e[0m    版本类型：\e[01;34m$VARIANT 版本\e[0m"
    echo -e "\e[01;32m*\e[0m    版本型号：\e[01;34m$MODEL\e[0m"
    echo -e "\e[01;32m*\e[0m    代码路径：\e[01;34m$core_path\e[0m"
    echo -e "\e[01;32m*\e[0m"
    echo -e "\e[01;32m*************************************************\e[0m"

    if [ $((RANDOM%5)) == 3 ] ; then
        echo""
        echo""
        cat  $(pwd)"/vendor/magcomm/build/tools/Bless"
        echo ""
        echo "            佛祖保佑     编译通过"
        echo ""
        echo ""
    fi

	CHECK_TIME=5
    while [ $CHECK_TIME -ge 0 ]
        do
        echo -e  "请确认编译信息正确，\e[01;31m$CHECK_TIME \e[0m秒后开始编译版本"
        sleep 1
        let "CHECK_TIME-=1"
    done

    source  build/envsetup.sh
    lunch_mode=full_$DEVICE-$VARIANT
    lunch $lunch_mode

    if [ $BUILD_TAG == "new" ] ; then
        echo "clearing OUT........"
        make clear
        rm -rf out/
    fi

    mkdir -p $LOG_FILE_PATH
    make -j8 2>&1 | tee  $LOG_FILE_PATH/build.log
    make_result=${PIPESTATUS[0]}
    echo make_result=$make_result
	# 编译完成需要判断 编译是否报错
	if [ $make_result  -ne  0  ] ; then
	    print_red_string "****************************************"
	    print_red_string "* 编译报错 !"
        print_red_string "* 编译LOG：$LOG_FILE_PATH/build.log"
    	print_red_string "* 代码路径：$core_path"
	    print_red_string "****************************************"
    	exit 0
	fi

	OUT_INFO=$mk_path/out/target/product/$DEVICE/system/build.prop
	DISPLAY_ID=$(grep $DISPLAY_TAG  $OUT_INFO | awk -F'=' '{print $2}')
	DISPLAY_ID=$(echo $DISPLAY_ID | tr -d '[:blank:]')
	MODEL=$(grep  $MODEL_TAG  $OUT_INFO | awk -F'=' '{print $2}')

	if [[ $VARIANT = "eng"  ]]	; then
		img_path=$SAVE_PATH/$project_name/$DISPLAY_ID"_"$VARIANT"_"$(date +%Y_%m%d)
	else
		img_path=$SAVE_PATH/$project_name/$DISPLAY_ID"_"$(date +%Y_%m%d)
	fi
	if [ ! -d $img_path ] ; then
		mkdir -p $img_path
	else
		bak_img_path=$img_path"_bak_"`date +%m%d_%H_%M`
		mv $img_path $bak_img_path
		mkdir -p $img_path
		BAK_IMG="yes"
	fi

	 ./copybin.sh $lunch_mode $img_path clear

	echo -e "\e[01;32m*************************************************\e[0m"
	echo -e "\e[01;32m*\e[0m"
	echo -e "\e[01;32m*\e[0m  版本信息："
	echo -e "\e[01;32m*\e[0m    项目 ：\e[01;34m$project_name\e[0m"
	echo -e "\e[01;32m*\e[0m    版本号 ：\e[01;34m$DISPLAY_ID\e[0m"
    echo -e "\e[01;32m*\e[0m    版本类型：\e[01;34m$VARIANT 版本\e[0m"
	echo -e "\e[01;32m*\e[0m    版本型号：\e[01;34m$MODEL\e[0m"
	echo -e "\e[01;32m*\e[0m    代码路径：\e[01;34m$core_path\e[0m"
	echo -e "\e[01;32m*\e[0m    版本存放路径：\e[01;34m$img_path\e[0m"
	echo -e "\e[01;32m*\e[0m"
	echo -e "\e[01;32m*\e[0m 时间:\e[01;34m `date +%Y_%m%d_%H:%M:%S` \e[0m"
	echo -e "\e[01;32m*\e[0m"
	echo -e "\e[01;32m*************************************************\e[0m"

	if [ $BAK_IMG == "yes" ] ;then
		echo -e "\e[01;32m* 版本存放路径重复,已经把以前版本备份到：\e[0m"
		echo -e "\e[01;32m*     $bak_img_path \e[0m"
		echo -e "\e[01;32m*************************************************\e[0m"
	fi
fi
