package cn.dunn.web;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016/9/27.
 */
public class WebTest {
  public static void main(String[] args) throws IOException {
    String md5 = DigestUtils.md5Hex(new FileInputStream("C:\\Users\\Administrator\\Desktop\\57ea7b2a7294bb226933d061.png"));
    System.out.println(md5);
  }
}
