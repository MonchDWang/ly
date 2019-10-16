package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.OSSproperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UploadService {

    // 支持的文件类型
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg", "image/bmp");
    /**
     * 本地上传图片
     * @param file
     * @return
     */
    public String upload(MultipartFile file) {

        //检查是否是符合要求的图片
//        a、检查文件的type
        String contentType = file.getContentType();
        if (!suffixes.contains(contentType)) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
//        b、使用工具来检查是否图片文件
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage == null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
//        2、创建新的文件名字
        String filename = file.getOriginalFilename();
        String newFileName = UUID.randomUUID().toString() + filename.substring(filename.lastIndexOf("."));
//        3、构造新文件的目录
        File dir = new File("D:\\coding-software\\nginx-1.12.2\\html");
//        检查目录是否存在，不存在就创建
        if(!dir.exists()){
            dir.mkdir();
        }
//        4、上传
        try {
            file.transferTo(new File(dir,newFileName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
        return "http://image.leyou.com/"+newFileName;
    }

    @Autowired
    private OSSproperties prop;
    @Autowired
    private OSS client;
    public Map<String, Object> signature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }
}
