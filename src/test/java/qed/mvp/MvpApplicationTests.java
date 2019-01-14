package qed.mvp;

import com.aliyuncs.exceptions.ClientException;
import com.ericbarnhill.niftijio.FourDimensionalArray;
import com.ericbarnhill.niftijio.NiftiVolume;
import com.mathworks.mps.client.MATLABException;
import com.mathworks.mps.client.MWClient;
import com.mathworks.mps.client.MWHttpClient;
import com.mathworks.mps.client.MWHttpClientDefaultConfig;
import com.mathworks.mps.client.annotations.MWStructureList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.SafeClose;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import qed.mvp.authentication.validatedcode.ValidatedCodeGenerator;
import qed.mvp.entity.*;
import qed.mvp.mapper.AnalyzeResultMapper;
import qed.mvp.mapper.UserBindImageMapper;
import qed.mvp.mapper.UserMapper;
import qed.mvp.service.AliyunMessagePwd;
import qed.mvp.service.MatlabService;
import qed.mvp.service.AliyunMessage;
import qed.mvp.utils.AnalysisUtil;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableTransactionManagement
public class MvpApplicationTests {

    @Autowired
    private AnalysisUtil analysisUtil;
    //    @Autowired
//    private ValidatedCodeGenerator validatedCodeGenerator;
//
//    @Autowired
//    private MatlabService matlabService;
//
//    @Autowired
//    UserMapper userMapper;
//    //
//    @Autowired
//    AliyunMessage aliyunMessage;
//
//    @Autowired
//    PasswordEncoder passwordEncoder;
//    //
//    @Autowired
//    BrainAtlasMapper brainAtlasMapper;
//
//    @Autowired
//    private UserBindImageMapper userBindImageMapper;
//
//    @Autowired
//    private AnalyzeResultMapper analyzeResultMapper;
//
//        normalControlMapper.batchInsert(b);
//    }

//    @Test
//    public void readXlxs() throws Exception {
//        String excelPath = "/home/qed/Downloads/预注册用户信息.xlsx";
//        File excel = new File(excelPath);
//        String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！
//        Workbook wb = null;
//        //根据文件后缀（xls/xlsx）进行判断
//        if ("xls".equals(split[1])) {
//            FileInputStream fis = new FileInputStream(excel);   //文件流对象
//            wb = new HSSFWorkbook(fis);
//        } else if ("xlsx".equals(split[1])) {
//            wb = new XSSFWorkbook(excel);
//        }
//        if (wb != null) {
//
//            Sheet sheet = wb.getSheetAt(0);
//            int firstRowIndex = sheet.getFirstRowNum() + 1;   //第一行是列名，所以不读
//            int lastRowIndex = sheet.getLastRowNum();
//            for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
//                System.out.println("rIndex: " + rIndex);
//                Row row = sheet.getRow(rIndex);
//                User user = new User();
//                user.setUsername(row.getCell(0).toString());
//                DecimalFormat df = new DecimalFormat("0");
//                user.setMobileNumber(df.format(row.getCell(1).getNumericCellValue()));
//                user.setUserId(df.format(row.getCell(1).getNumericCellValue()));
//                user.setHospital(row.getCell(2).toString());
//                user.setDepartment(row.getCell(3).toString());
//                user.setTitle(row.getCell(4).toString());
//                user.setPassword(passwordEncoder.encode("123456"));
//                user.setCreateDatetime(new Date());
//                user.setUserStatus(0);
//                user.setEmail("");
//                User findUser = userMapper.findUserByMobileNumber(user.getMobileNumber());
//                if (findUser == null) {
//                    userMapper.insert(user);
//                    System.out.println(user);
//                }
//
//            }
//        }
//    }


    @Test
    public void readNii() throws Exception {
        NiftiVolume spect = NiftiVolume.read("/bbq/result/13810014375/1.2.840.113619.2.112.25110.10.2.2.206.1470281188.924.11770/norm.nii");

        System.out.println(spect.header.info());
    }

    @Autowired
    private AliyunMessagePwd aliyunMessagePwd;

    @Test
    public void semi() throws ClientException {
        HashMap<String, String> params = new HashMap<>();

        String name = "fan";
        String pwd = "123456";
        String templateParam = "{\"pwd\":\"" + pwd + "\",\"name\":\"" + name + "\"}";
        System.out.println(templateParam);
        params.put("phoneNumbers", "13472801642");
        params.put("templateParam", templateParam);
        aliyunMessagePwd.sendSms(params);
    }

}
