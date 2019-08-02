#!/bin/bash
IFS=$'\n'
declare -i i=1     #声明一个变量I 并赋值为1
CUSTOM_PATH="$(pwd)/../../../../../CXprojects/"
CODE_LIST_PATH="$(pwd)/code_list.xml"

BUILD_HOME="$(pwd)/../../../../../"

# 82时 zprojects 变成 CXprojects导致
if [ ! -d $CUSTOM_PATH ] ; then
	CUSTOM_PATH="$(pwd)/../../../../../zprojects/"
fi
#local last=${AAA:((${#AAA} - 1))}


function get_projects(){
local flag="projects"

 for file in `ls $BUILD_HOME`
    do
		local last=${file:((${#file} - 8))}
		if [[ $last  = $flag  ]] ; then
			echo $BUILD_HOME$file
		fi
    done

}


################################
#列举可以下载的版本和编译的版本
#参数 ： code_list.xml 路径
################################
function print_download_platform(){
let i=1
let wrap=-1
echo -e "\e[01;32m*************************************************\e[0m"
echo -e "\e[01;32m*\e[0m \e[01;34m可以下载的版本: \e[0m"

cat $1 | while read line
do

#获取xml中name对应的值
platform[i]=`echo $line | awk -F[=\"] '/name/{print $3}' `

# 如果不是platform 行 进行的处理
if [ -z ${platform[i]} ] ; then
	# 获取 对应代码的路径
	branch[i]=`echo $line | awk -F[=\"] '/key/{print $3}' `

	if [ ! -z ${branch[i]} ] ; then
		# 三个之后进行换行处理
		num=`expr $i % 3`

		if [ $num -eq 1 ] ; then
            echo -e "\e[01;32m*\e[0m\c"
        fi

        if [ $num -eq 0 ] ; then
            echo -e "\t${branch[i]} "
			let wrap=1
        else
            echo -e "\t ${branch[i]} \c"
			let wrap=-1
        fi


	let i++
	fi
else
	if [ $i -ne 1 ] ; then
		if [ $wrap -eq 1 ] ;then
			echo -e "\e[01;32m*  ${platform[i]} \e[0m"
			let wrap=-1
		else
			echo -e "\n\e[01;32m*  ${platform[i]} \e[0m"
		fi
	else
		echo -e "\e[01;32m*  ${platform[i]} \e[0m"
	fi
	let i=1
fi
done
echo -e "\n\e[01;32m*************************************************\e[0m"

}

function get_download_platform(){
    let i=1

    local host

    cat $1 | while read line
    do
        address=`echo $line | awk -F[=\"] '/key/{print $6}' `
        branch=`echo $line | awk -F[=\"] '/key/{print $3}' `
        host_temp=`echo $line | awk -F[=\"] '/host/{print $6}' `

        echo "$line" |grep -q "host"

        if [ $? -eq 0 ] ; then
            if [ ! -z "$host_temp" ] ; then
                host=$host_temp
            fi
        fi

        if [[ $2 == $branch ]] ; then
            echo $address"#"$host
        fi

    let i++
done

}

function print_build_custom (){
let i=1
echo -e "\e[01;32m*\e[0m \e[01;34m当前工程可编译的版本: \e[0m"
	for file in `ls $1`
	do
		num=`expr $i % 5`
		if [ $num -eq 1 ] ; then
			echo -e "\e[01;32m*\e[0m\c"
		fi

		if [ $num -eq 0 ] ; then
			echo -e "\t$file "
		else
			echo -e "\t $file \c"
		fi

		let i++
	done
if [ $num -eq 0 ] ; then
	echo -e "\e[01;32m*************************************************\e[0m"
else
	echo -e "\n\e[01;32m*************************************************\e[0m"
fi
}

CUSTOM_PATH=$(get_projects)

if [ ! -z $1 ] ; then
	get_download_platform $CODE_LIST_PATH $1
else
	print_download_platform $CODE_LIST_PATH
	print_build_custom $CUSTOM_PATH
fi

