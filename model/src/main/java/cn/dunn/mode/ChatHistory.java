package cn.dunn.mode;

import cn.dunn.constant.Constant;
import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * 最后一条历史消息
 */
public class ChatHistory {
  public ChatHistory() {
  }

  public ChatHistory(User user, Message message) {
    this.user = user;
    this.message = message;
  }

  public ChatHistory(ChatGroup chatGroup, Message message) {
    this.chatGroup = chatGroup;
    this.message = message;
  }

  /**
   * 会话对象
   */
  @DBRef
  private ChatGroup chatGroup;
  /**
   * 会话对象
   */
  @DBRef
  private User user;

  /**
   * 未读消息条数
   */
  private Long unReadCount;

  /**
   * 一开始未读的消息
   */
  @DBRef
  private Message startUnReadMessage;
  /**
   * 最后发送的消息
   */
  @DBRef
  private Message message;


  public Message getStartUnReadMessage() {
    return startUnReadMessage;
  }

  public void setStartUnReadMessage(Message startUnReadMessage) {
    this.startUnReadMessage = startUnReadMessage;
  }

  public Long getUnReadCount() {
    return unReadCount;
  }

  public void setUnReadCount(Long unReadCount) {
    this.unReadCount = unReadCount;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public ChatGroup getChatGroup() {
    return chatGroup;
  }

  public void setChatGroup(ChatGroup chatGroup) {
    this.chatGroup = chatGroup;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }
}
