package cn.dunn.mode;

import java.util.List;

/**
 * Created by Administrator on 2016/9/23.
 */
public class GroupUser {
  private String mark;
  private List<User> chats;

  public GroupUser() {
  }

  public GroupUser(String mark, List<User> chats) {
    this.mark = mark;
    this.chats = chats;
  }

  public String getMark() {
    return mark;
  }

  public void setMark(String mark) {
    this.mark = mark;
  }

  public List<User> getChats() {
    return chats;
  }

  public void setChats(List<User> chats) {
    this.chats = chats;
  }
}
