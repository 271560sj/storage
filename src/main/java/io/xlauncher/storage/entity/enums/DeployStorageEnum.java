package io.xlauncher.storage.entity.enums;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-6 上午9:10
 */
public enum  DeployStorageEnum {


    GFS_KUBE_DEPLOY("glusterfs/kube-templates/deploy-heketi-deployment.yaml", 0),
    GFS_KUBE_PVC("glusterfs/kube-templates/gluster-s3-pvcs.yaml",1),
    GFS_KUBE_STORAGE("glusterfs/kube-templates/gluster-s3-storageclass.yaml",2),
    GFS_KUBE_S3("glusterfs/kube-templates/gluster-s3-template.yaml",3),
    GFS_KUBE_DAEMON("glusterfs/kube-templates/glusterfs-daemonset.yaml",4),
    GFS_KUBE_DEPLOYMENT("glusterfs/kube-templates/heketi-deployment.yaml",5),
    GFS_KUBE_HEKETI("glusterfs/kube-templates/heketi-service-account.yaml",6),
    GFS_KUBE_SHELL("glusterfs/gk-deploy",7),
    GFS_KUBE_HEKETI_JSON("glusterfs/heketi.json.template",8),
    GFS_KUBE_TOPOLOGY("glusterfs/topology.json",9),
    GFS_KUBE_INIT("glusterfs/install-glusterfs.sh",13),
    GFS_IMAGE_GLUSTERFS("centos-gfs.tar",10),
    GFS_IMAFE_HEKETI("heketi-dev.tar",11),
    GFS_IMAGE_OBJECT("gluster-object.tar",12),
    GFS_KUBE_PREPARE("glusterfs/prepare-gfs.sh",19),

    CEPH_INSTALL_VALUES("ceph/ceph/values.yaml",14),
    CEPH_INSTALL_OVERRIDES("ceph/overrides.json",15),
    CEPH_INSTALL_RBAC("ceph/rbac.yaml",16),
    CEPH_INSTALL_SHELL("ceph/install-ceph.sh",17),
    CEPH_INSTALL_PREPARE("ceph/prepare-node.sh",18);

    private String name;
    private int index;

    DeployStorageEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName(){
        return this.name;
    }
}
