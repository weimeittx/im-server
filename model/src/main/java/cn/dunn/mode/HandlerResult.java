package cn.dunn.mode;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2016/9/29.
 */
@Document
public class HandlerResult {

  @Id
  private String id;


}
