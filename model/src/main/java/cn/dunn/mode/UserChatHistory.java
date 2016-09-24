package cn.dunn.mode;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;

/**
 * 用户聊天历史
 */
@Document
public class UserChatHistory {
  @Id
  private String id;
  /**
   * 用户
   */
  @DBRef
  private User user;

  private LinkedList<ChatHistory> chatHistorys;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LinkedList<ChatHistory> getChatHistorys() {
    return chatHistorys;
  }

  public void setChatHistorys(LinkedList<ChatHistory> chatHistorys) {
    this.chatHistorys = chatHistorys;
  }
}
