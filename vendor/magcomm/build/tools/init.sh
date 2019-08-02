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
# Version: V1.1
# Update : 2014-06-12
############################################################################
SCRIPT_FILES=(config.sh  copybin.sh mag quickbuild.sh  ymm)

LN_PATH=$1
NOW_PATH=$PWD

EXTRA_PATH=${NOW_PATH:${#LN_PATH}}

if [ $# -eq 0 ]; then
    echo ""
    echo "请输入:"
    echo "     ./init.sh 路径"
    echo ""
    exit 0
fi

cd $LN_PATH

for script in ${SCRIPT_FILES[@]}
do
    echo "ln -s $EXTRA_PATH$script ./"
    ln -s $EXTRA_PATH"/"$script ./
    git add $script
done

git add $EXTRA_PATH
git commit



