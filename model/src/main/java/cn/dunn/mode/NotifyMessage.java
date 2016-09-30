package cn.dunn.mode;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 通知消息
 */
@Document
public class NotifyMessage {
  @Id
  private String id;

  /**
   * 消息所属
   */
  @DBRef
  private User user;

  /**
   * 消息类型
   */
  private String type;

  /**
   * 如果是添加好友
   */
  @DBRef
  private User friend;

  /**
   * 如果是添加群组
   */
  @DBRef
  private ChatGroup chatGroup;

  /**
   * 验证信息
   */
  private String validateInfo;

  /**
   * 消息时间
   */
  private Long createTime;

}
