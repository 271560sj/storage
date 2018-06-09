package io.xlauncher.storage.entity.enums;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-6 上午9:10
 */
public enum  DeployStorageEnum {


    GFS_KUBE_DEPLOY("kube-templates/deploy-heketi-deployment.yaml", 0),
    GFS_KUBE_PVC("kube-templates/gluster-s3-pvcs.yaml",1),
    GFS_KUBE_STORAGE("kube-templates/gluster-s3-storageclass.yaml",2),
    GFS_KUBE_S3("kube-templates/gluster-s3-template.yaml",3),
    GFS_KUBE_DAEMON("kube-templates/glusterfs-daemonset.yaml",4),
    GFS_KUBE_DEPLOYMENT("kube-templates/heketi-deployment.yaml",5),
    GFS_KUBE_HEKETI("kube-templates/heketi-service-account.yaml",6),
    GFS_KUBE_SHELL("gk-deploy",7),
    GFS_KUBE_HEKETI_JSON("heketi.json.template",8),
    GFS_KUBE_TOPOLOGY("topology.json",9),
    GFS_KUBE_INIT("install-glusterfs.sh",13),
    GFS_IMAGE_GLUSTERFS("centos-gfs.tar",10),
    GFS_IMAFE_HEKETI("heketi-dev.tar",11),
    GFS_IMAGE_OBJECT("gluster-object.tar",12),

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
