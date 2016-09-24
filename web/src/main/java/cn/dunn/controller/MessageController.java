package cn.dunn.controller;


import cn.dunn.constant.Constant;
import cn.dunn.mode.HttpResult;
import cn.dunn.mode.Message;
import cn.dunn.mode.User;
import cn.dunn.mongo.MessageRepository;
import cn.dunn.util.WebUtil;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("message")
public class MessageController{
  @Resource
  private MessageRepository messageRepository;

  @Resource
  private MongoTemplate mongoTemplate;

  @Resource
  private Vertx vertx;

  private class HistoryMessage {
    public HistoryMessage(String id, List<Message> messages) {
      this.id = id;
      this.messages = messages;
    }

    String id;
    List<Message> messages;
  }

  /**
   * 获取好友聊天记录
   *
   * @param request
   * @return
   */
  @RequestMapping("getChatHistoryMessage")
  @ResponseBody
  public HttpResult getChatHistoryMessage(@RequestBody String id, HttpServletRequest request) {
    User user = WebUtil.LoginUser(request);
    Criteria criteria = Criteria.where("from").in(user, new User(id))
      .andOperator(Criteria.where("to").in(user.getId(), id), Criteria.where("type").is(Constant.MESSAGE_TYPE_CHAT), Criteria.where("createTime").lt(System.currentTimeMillis()));
    Query query = Query.query(criteria).with(new PageRequest(1, 20)).with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
    List<Message> messages = mongoTemplate.find(query, Message.class);
    return new HttpResult(new HistoryMessage(id, messages));
  }

  /**
   * 获取群组聊天记录
   *
   * @return
   */
  @RequestMapping("getChatGroupHistoryMessage")
  @ResponseBody
  public HttpResult getChatGroupHistoryMessage(@RequestBody String id) {
    Criteria criteria = Criteria.where("to").in(id)
      .andOperator(Criteria.where("type").is(Constant.MESSAGE_TYPE_GROUP), Criteria.where("createTime").lt(System.currentTimeMillis()));
    Query query = Query.query(criteria).with(new PageRequest(1, 20)).with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
    List<Message> messages = mongoTemplate.find(query, Message.class);
    return new HttpResult(new HistoryMessage(id, messages));
  }

  /**
   * 发送消息
   *
   * @param message
   */
  @RequestMapping("sendMessage")
  @ResponseBody
  public void sendMessage(@RequestBody Message message) {
    message = messageRepository.save(message);
    String type = message.getType();
    switch (type) {
      //发送好友消息
      case Constant.MESSAGE_TYPE_CHAT: {
        vertx.eventBus().publish(message.getTo(), new JsonObject(JSONObject.toJSONString(message)));
        break;
      }
      //发送群组消息
      case Constant.MESSAGE_TYPE_GROUP: {
        break;
      }
    }
  }
}
