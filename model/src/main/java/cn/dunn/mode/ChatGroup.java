package cn.dunn.mode;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 聊天组
 */
@Document
public class ChatGroup {

  public ChatGroup() {

  }

  public ChatGroup(String id) {
    this.id = id;
  }

  @Id
  private String id;
  /**
   * 群聊名称
   */
  private String chatGroupName;
  /**
   * 公告
   */
  private String affiche;
  /**
   * 头像
   */
  private String head;
  /**
   * 用户在该群组中未读的消息条数
   */
  private Long unReadMessageCount;

  /**
   * 群成员个数
   */
  private Integer size;
  private Long createTime;
  /**
   * 创建时间
   */


  /**
   * 群成员
   */
  @DBRef
  private List<User> members;

  public List<User> getMembers() {
    return members;
  }

  public void setMembers(List<User> members) {
    this.members = members;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public Long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }

  public String getChatGroupName() {
    return chatGroupName;
  }

  public void setChatGroupName(String chatGroupName) {
    this.chatGroupName = chatGroupName;
  }

  public String getAffiche() {
    return affiche;
  }

  public void setAffiche(String affiche) {
    this.affiche = affiche;
  }

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public Long getUnReadMessageCount() {
    return unReadMessageCount;
  }

  public void setUnReadMessageCount(Long unReadMessageCount) {
    this.unReadMessageCount = unReadMessageCount;
  }
}
