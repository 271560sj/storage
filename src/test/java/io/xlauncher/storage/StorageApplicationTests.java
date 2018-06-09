package io.xlauncher.storage;

import com.alibaba.fastjson.JSONObject;
import io.xlauncher.storage.entity.HostEntity;
import io.xlauncher.storage.utils.ReadFilesUtils;
import io.xlauncher.storage.utils.SshUtils;
import io.xlauncher.storage.utils.TarFileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StorageApplicationTests {

	@Test
	public void contextLoads() throws Exception{

//		ReadPropertiesUtils propertiesUtils = ReadPropertiesUtils.getInstance("storage.properties");
//
//		HostEntity entityOne = new HostEntity();
//		entityOne.setIp("8.16.0.80");
//		entityOne.setPassword("root");
//		entityOne.setPort(21);
//		entityOne.setUser("root");
//
//		HostEntity entityTwo = new HostEntity();
//		entityTwo.setPassword("root");
//		entityTwo.setIp("8.16.0.81");
//		entityTwo.setUser("root");
//		entityTwo.setPort(21);
//		FtpUtils ftpUtils = new FtpUtils();
//
//		ftpUtils.FTPSendFile(new HostEntity[]{entityOne,entityTwo}, propertiesUtils.getProperties("storage.host.file.store.path"),
//				propertiesUtils.getProperties("storage.host.file.send.path"),new String[]{"etcd-master.tar"});
//
//		SshUtils sshUtils = new SshUtils();
//		sshUtils.setHostEntity(entityOne);
//
//		sshUtils.execute("journalctl -u kubelet -f");
//		HostEntity entity = new HostEntity();
//		entity.setPort(22);
//		entity.setUser("root");
//		entity.setIp("8.16.0.44");
//		entity.setPassword("root");
//
//		StorageController controller = new StorageController();
//
//		List<HostEntity> list = new ArrayList<HostEntity>();
//		list.add(entity);
//		controller.initStorageNode(list);

//		YamlConfigUtils configUtils = new YamlConfigUtils();
//		String str = configUtils.readYamlToString("glusterfs/template/kube-templates/gluster-s3-template.yaml",
//				new String[]{"sujin-01", "sujin-02", "sujin-03"});
//		boolean bool = configUtils.writeYamlFile(str,"/home/alan/gluster-s3.yaml");

//		List<HostEntity> list = new ArrayList<HostEntity>();
//
//		for (int i = 0 ; i < 10 ;  i ++){
//			HostEntity entity = new HostEntity();
//			entity.setIp("8.16.0.5" + i);
//			entity.setDevices(new String[]{"/dev/sdb","/dev/sdc","/dev/sdd"});
//			entity.setName("host-name-" + i);
//			list.add(entity);
//		}
//		JsonConfigUtils configUtils = new JsonConfigUtils();
//		String json = configUtils.readJsonFile("glusterfs/template/topology.json");
//		JSONObject jsonObject = configUtils.addHostEntity(json,list);
//		ReadFilesUtils configUtils = new ReadFilesUtils();
//		String json = configUtils.readFileToString("glusterfs/template/topology.json");
//		JSONObject jsonObject = configUtils.addHostEntity(json, list);
//		System.out.println(JSONObject.toJSONString(jsonObject,true));
//		configUtils.writeFile(JSONObject.toJSONString(jsonObject,true), "/home/alan/topology.json");

//		TarFileUtils tarFileUtils = new TarFileUtils();
//		String path = "/home/alan/workspace/storage/src/main/resources/static/deploy/glusterfs/template/";
//
//		tarFileUtils.tarFiles("/home/alan/workspace/storage/src/main/resources/static/deploy/glusterfs/template/",
//				path.substring(0,path.lastIndexOf("/",2)),"template.tar", false);


//		ReadFilesUtils readFilesUtils = new ReadFilesUtils();
//		String json = readFilesUtils.readFileToString("topology.json");
//		readFilesUtils.writeFile(json, "/home/alan/hjshdjsdshdjshdjshdjs/topology/topology.json");

//		SshUtils utils = new SshUtils();
//
//		HostEntity entity = new HostEntity();
//		entity.setUser("root");
//		entity.setPassword("root");
//		entity.setName("om1");
//		entity.setIp("8.16.0.71");
//		utils.setHostEntity(entity);
//		utils.execute("/tmp/6e2872d7464d468dab7cd46fb38af413/gk-deploy -n domain -g " +
//				"/tmp/6e2872d7464d468dab7cd46fb38af413/topology.json");

		ReadFilesUtils utils = new ReadFilesUtils();
		utils.copyFile("static/deploy/ceph", "/home/alan/" + UUID.randomUUID().toString().
				replace("-",""));

	}
}
