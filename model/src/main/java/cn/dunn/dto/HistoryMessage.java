package cn.dunn.dto;

import cn.dunn.mode.Message;

import java.util.List;

/**
 * Created by Administrator on 2016/9/24.
 */
public class HistoryMessage {

  public HistoryMessage() {

  }

  public HistoryMessage(String id, List<Message> messages) {
    this.id = id;
    this.messages = messages;
  }

  public String id;
  public List<Message> messages;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }
}
