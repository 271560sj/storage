package io.xlauncher.storage;

import io.xlauncher.storage.dao.GfsVolumeDaoInterface;
import io.xlauncher.storage.entity.GfsVolumePersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StorageApplicationTests {

	@Autowired
	GfsVolumeDaoInterface daoInterface;

	@Test
	public void contextLoads() throws Exception{

//		List<String> list = new ArrayList<>();
//
//		for (int i = 0 ; i < 10; i ++) {
//			list.add("my name is " + i);
//		}
//
//		list.parallelStream().forEach(x -> {
//			String hi = "nice to meet you";
//			System.out.println(x + hi);
//		});

//		for (int i = 0 ; i < 100; i ++){
//			GfsVolumePersistence persistence = new GfsVolumePersistence();
//			persistence.setUserId(1);
//			persistence.setUserName("alan");
//			persistence.setVolumeId(UUID.randomUUID().toString().replace("-",""));
//			persistence.setVolumeName("sujin-image-01");
//			persistence.setVolumeSize("10");
//			persistence.setVolumeStatus("create");
//			persistence.setVolumeType("xfs");
//			GfsVolumePersistence result = daoInterface.insertGfsVolumeInfoToDB(persistence);
//		}

		Page<GfsVolumePersistence> page = daoInterface.getGfsVolumesInDBByUser(0,10,"alan");
		System.out.println(page.getContent());
//		daoInterface.deleteGfsVolumeInDB(persistence);
	}
}
