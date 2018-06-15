#!/bin/bash

#准备部署ceph的环境
BASE_DIR="/root/{0}"
function prepare(){
   #解压文件
   tar xvf ${BASE_DIR}/ceph-common.tar.gz -C ${BASE_DIR}/
   tar xvf ${BASE_DIR}/ceph-images.tar.gz -C ${BASE_DIR}/
   #部署ceph-common
   yum localinstall -y ${BASE_DIR}/ceph-common/*.rpm

   rbd=$(lsmod|grep rbd)
   if [ -z "${rbd}" ]; then
       modprobe rbd
   fi

   rm -rf /var/lib/ceph-helm/
}

function loadImage(){
   images=(alpine
          ceph-daemon
          source-entrypoint.4.0.0
          ceph-config-v1.7.55
          rbd-provision-v0.1.1
          source-heat-3.0.3)
   for image in ${images[@]};do
     docker load -i ${BASE_DIR}/ceph-images/${image}.tar
   done
}

prepare
loadImage