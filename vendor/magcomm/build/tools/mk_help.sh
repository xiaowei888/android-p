#!/bin/bash

function print_help(){

cat<<HELP
********************************************************************************
使用案例：

1.下载并编译 82平台的 s807B_td （User）版本
	./quickbuild.sh  MT6582  s807B_td
	注：想把代码下载到指定路径，请加入第三个参数，如果没加则默认当前路径
		例如：./quickbuild.sh  MT6582  s807B_td  ~/ReleaseVersion/mt6582/test

2.下载并编译 82平台的 s807B_td eng版本
	./quickbuild.sh  MT6582  s807B_td  eng
	注：想把代码下载到指定路径，请加入第三个参数，如果没加则默认当前路径
		例如：./quickbuild.sh  MT6582  s807B_td  ~/ReleaseVersion/mt6582/test  eng

3.下载并编译 82平台的 s807B_td  user版本 ， ota版本
	./quickbuild.sh  MT6582  s807B_td  ota
	注：想把代码下载到指定路径，请加入第三个参数，如果没加则默认当前路径
		例如： ./quickbuild.sh  MT6582  s807B_td  ~/ReleaseVersion/mt6582/test  ota

4.仅下载 82平台代码
	./quickbuild.sh -d MT6582 路径
	注：想把代码下载到指定路径，请加入第三个参数，如果没加则默认当前路径
		例如： ./quickbuild.sh  -d  MT6582  ~/ReleaseVersion/mt6582/test

5.仅编译 82平台代码
	./quickbuild.sh -b MT6582
	注：编译指定代码路径，请加入第三个参数，如果没加则默认当前路径， 这里需要注意的是
	不是alps目录
		例如： ./quickbuild.sh  -b  项目  ~/ReleaseVersion/mt6582/test

6.仅编译 82平台代码 （eng版本）
	./quickbuild.sh -b MT6582  eng
	注：编译指定代码路径，请加入第三个参数，如果没加则默认当前路径， 这里需要注意的是
	不是alps目录
		例如： ./quickbuild.sh  -b  项目  ~/ReleaseVersion/mt6582/test  eng

7. 仅编译 82平台代码 并执行 make clear
	./quickbuild.sh -b MT6582  new
	注：编译指定代码路径，请加入第三个参数，如果没加则默认当前路径， 这里需要注意的是
	不是alps目录
		例如： ./quickbuild.sh  -b  项目  ~/ReleaseVersion/mt6582/test  ota
		
注： 
	1. -b 的意思： build 
	2. -d 的意思： download
	3. 脚本参数顺序 1.平台  2.项目  3.路径  4.其他（eng ，ota, pe , new , r）
	
********************************************************************************

HELP

exit 0

}

print_help
