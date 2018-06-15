#!/bin/bash

#操作的基本目录
BASE_DIR="/tmp/{0}"

#节点的名称
NODES="{1}"

#namespace
NAMESPACE={2}

#node需要打的标签
LABEL="{3}"

#OSD的数量
OSD_NUM={4}

#MON的数量
MON_NUM={5}

function install(){
   #设置分割标记
   old_ifs="$IFS"
   IFS=" "
   nodes=(${NODES})

   #给各个节点打上标签
   for node in ${nodes[@]};
   do
      kubectl label node ${node} ${LABEL}
   done

   #创建namespace
   kubectl get namespace ${NAMESPACE}

   if [ $? -ne "0" ]; then
       kubectl create namespace ${NAMESPACE}
   fi

   #创建rbac
   kubectl create -f ${BASE_DIR}/rbac.yaml

   chmod +x ${BASE_DIR}/ceph/charts/helm-toolkit-0.1.0.tgz
   chmod +x ${BASE_DIR}/ceph/recreate-pod.sh

   #部署ceph
   helm install --namespace ${NAMESPACE} -f ${BASE_DIR}/overrides.json ${BASE_DIR}/ceph/
}

#检查是否部署成功
function check() {
   #检查osd启动的数量
   for(( i = 0 ; i < 25; i ++ ));do
       osd=$(kubectl get pod -n ${NAMESPACE}|grep ceph-osd|grep Running|wc -l)
       if [ ${osd} -eq "${OSD_NUM}" ]; then
           echo "ceph osd start success"
       fi

       sleep 30
   done

   if [ ${i} -eq 25 ]; then
       echo "ceph osd start error"
       exit
   fi

   for ((i = 0 ; i < 25 ; i ++));do
      mon=$(kubectl get pod -n {6}|grep ceph-mon|grep Running|wc -l)
      if [ ${mon} -eq "${MON_NUM}" ]; then
          echo "ceph mon start success"
      fi

      sleep 30
   done

   if [ ${i} -eq 25 ]; then
       echo "ceph mon start error"
       exit
   fi
}

#配置ceph
function config(){
    mon=$(kubectl get pod -n ${NAMESPACE}|grep ceph-mon|grep 3/3|awk '{print $1}')
    old_ifs="$IFS"
    arr=($mon)
    IFS=${old_ifs}
    mon_name=${arr[0]}
    kubectl exec -it -n ${NAMESPACE} -c ceph-mon ${mon_name} -- ceph osd crush tunables hammer
}

#删除部署文件
function deleteFiles() {
   rm -rf ${BASE_DIR}
}
install
check
config
deleteFiles