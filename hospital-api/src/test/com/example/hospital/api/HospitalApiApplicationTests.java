package example.hospital.api;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.hospital.api.HospitalApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@SpringBootTest(classes = HospitalApiApplication.class)
public class HospitalApiApplicationTests {
    @Resource
    private AmqpTemplate amqpTemplate;

    @Test
    void contextLoads() {
        ArrayList rp = new ArrayList<>() {{
            add(new HashMap<>(){{
                put("name","甲硝唑片");
                put("spec","200mg*24片");
                put("method","1片/次;每日三次；口服");
                put("num",1);
            }});
        }};
        HashMap map = new HashMap(){{
            put("uuid", IdUtil.simpleUUID().toUpperCase());
            put("patientCardId",2);
            put("diagnosis","急性牙髓炎");
            put("subDeptId",2);
            put("doctorId",18);
            put("registrationId",2);
            put("rp",rp);
        }};
        amqpTemplate.convertAndSend("prescription",
                JSONUtil.parse(map).toString());
    }
}
