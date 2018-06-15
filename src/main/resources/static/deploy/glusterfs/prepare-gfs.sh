#!/bin/bash

#准备部署glusterfs的环境

BASE_DIR="/root/{0}"


function prepare(){

   #修改dns
   sed -i "s/10.96.0.10/11.96.0.10/g" /etc/systemd/system/kubelet.service.d/10-kubeadm.conf
   systemctl daemon-reload
   systemctl restart kubelet

   #开始解压缩文件
   tar xvf ${BASE_DIR}/gfs-image.tar.gz -C ${BASE_DIR}
   tar xvf ${BASE_DIR}/glusterfs-fuse.tar.gz -C ${BASE_DIR}

   yum list installed|grep glusterfs-fuse|xargs yum remove -y

   #节点上安装glusterfs
   yum localinstall -y ${BASE_DIR}/glusterfs-fuse/*.rpm

   #加载glusterfs相关的内核
   modprobe dm_snapshot
   modprobe dm_mirror
   modprobe dm_thin_pool

   #关闭防火墙
   systemctl stop firewalld
   systemctl disable firewalld

   rm -rf /var/lib/heketi

   #加载镜像
   images=(
      centos-gfs.tar
      gluster-object.tar
      heketi-dev.tar
   )

   for image in ${images[@]};do
       docker load -i ${BASE_DIR}/gfs-image/${image}
   done
}

prepare