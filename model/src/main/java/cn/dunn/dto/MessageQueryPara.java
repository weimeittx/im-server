package cn.dunn.dto;

/**
 * 消息查询参数
 */
public class MessageQueryPara {
  /**
   * 聊天对象的ID
   */
  private String id;
  /**
   * 最后拉取消息时间
   */
  private Long time;

  public MessageQueryPara() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getTime() {
    return time;
  }

  public void setTime(Long time) {
    this.time = time;
  }
}
