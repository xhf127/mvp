package qed.mvp.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AliyunMessagePwd {

    private static final Logger log = LoggerFactory.getLogger(AliyunMessagePwd.class);

    //短信API产品名称（短信产品名固定，无需修改）
    final private static String product = "Dysmsapi";
    //短信API产品域名（接口地址固定，无需修改）
    final private static String domain = "dysmsapi.aliyuncs.com";

    private static final String accessKeyId = "LTAIPQYISO0mZrv0";
    private static final String accessKeySecret = "8fQRxMx4ZIXzYlCQSGHa0iB4B1TvVu";

    @Value("${aliyun.signName}")
    String signName;

    @Value("SMS_152283551")
    String templateCode;

    public boolean sendSms(Map<String, String> paramMap) throws ClientException {
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId,
                accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //使用post提交
        request.setMethod(MethodType.POST);
        //必填:待发送手机号
        request.setPhoneNumbers(paramMap.get("phoneNumbers"));
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(signName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(templateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为$[code]"时,此处的值为
        request.setTemplateParam(paramMap.get("templateParam"));

        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        System.out.println(sendSmsResponse);
        log.info(sendSmsResponse.getCode());
        return sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK");
    }
}
