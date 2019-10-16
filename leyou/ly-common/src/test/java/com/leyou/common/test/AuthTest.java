package com.leyou.common.test;


import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public class AuthTest {

    //私钥
    private String privateFilePath = "F:\\ssh\\id_rsa";
//公钥
  private String publicFilePath = "F:\\ssh\\id_rsa.pub";


    /**
     * 生成私钥和公钥
     * @throws Exception
     */
  @Test
    public void testCreateRsaFile() throws Exception {
      //生成公钥和私钥文件
        RsaUtils.generateKey(publicFilePath,privateFilePath,"hello",2048);
//        读取公钥文件，生成公钥对象
      PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
      System.out.println(publicKey);
//      读取私钥文件，生成私钥对象
      PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
      System.out.println(privateKey);
  }

    /**
     * 生成jwt
     */
  @Test
  public void testCreateJwt() throws Exception {
      UserInfo userInfo = new UserInfo();
      userInfo.setId(55L);
      userInfo.setUsername("jwtuser");
      userInfo.setRole("admin");
      //获取私钥对象
      PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
      //生成jwt
      String token = JwtUtils.generateTokenExpireInSeconds(userInfo, privateKey, 120);
      System.out.println(token);
      //解密jwt
      //获取公钥
      PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
      Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);
      String jwtId = payload.getId();
      System.out.println(jwtId);
      UserInfo userInfo1 = payload.getUserInfo();
      System.out.println("payload userinfo=="+userInfo1);
      Date expiration = payload.getExpiration();
      System.out.println(expiration);
  }

  @Test
    public void testGetToken() throws Exception {
      String token="eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjo1NSxcInVzZXJuYW1lXCI6XCJqd3R1c2VyXCIsXCJyb2xlXCI6XCJhZG1pblwifSIsImp0aSI6Ik1qQTNZakpsTTJRdE0yWTRZUzAwWTJJekxXRTFPVFF0TXpZME9UQTVPV1F4TnpndyIsImV4cCI6MTU2ODI4MDY1N30.ltDJQa9qZI3TOEvJvAjWOvlQ-8kEcu1n9BoI4aQS8-r8xkp8PF9wt7R9WQX4WL9Vsh-3Rxez2FKPsBRJQ1l5dxdIGO0l8ZKw-sWRjzIaxAU35c-rP2Em2VxfpGvdwpkBOT8Nmi2uOm-_GLQVOrKagexSb1ArTF0X-sdnQD_jQZDStGIruXrs1ZufpiU23Gb8KDCw3t8vp31pw0qGI9UdxOT67GIIjr6zb9zYNWmMwG8_qhKwEoJAazWABWUcEa2D_c5CujlUV2MCvpKPMjJPTwFNJskg900Cfdtr4EMQcI7OT83YwZG0lgVh-Mfx8-ltlDG2WzwdxBZHjL8IWf705w";
      //解密jwt
      //获取公钥
      PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
      Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);
  }
}
