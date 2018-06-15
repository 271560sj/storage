#!/bin/bash

BASE_DIR={0}
NODES="{1}"
LABELS={2}
NAMESPACE={3}
#部署glusterfs
function install(){

   #解压缩部署问文件
   tar xvf ${BASE_DIR}/glusterfs/glusterfs.tar -C ${BASE_DIR}

   old_ifs="$IFS"
   IFS=" "
   nodes=(${NODES})
   IFS=${old_ifs}

   #创建namespace
   kubectl get namespace ${NAMESPACE}
   if [ $? -ne "0" ]; then
       kubectl create namespace ${NAMESPACE}
   fi

   #给节点打上标签
   for node in ${nodes[@]}
   do
      kubectl label node ${node} apollo-domain=${LABELS} --overwrite
   done

   kubectl label node ${nodes[0]} dns=dns tiller=tiller

   #增加执行权限
   chmod +x ${BASE_DIR}/glusterfs/gk-deploy
   #开始部署
   ${BASE_DIR}/glusterfs/gk-deploy -n ${NAMESPACE} -g ${BASE_DIR}/glusterfs/topology.json
}

install