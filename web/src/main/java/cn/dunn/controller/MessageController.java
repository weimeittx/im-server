package cn.dunn.controller;


import cn.dunn.constant.Constant;
import cn.dunn.dto.HistoryMessage;
import cn.dunn.dto.MessageQueryPara;
import cn.dunn.mode.*;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("message")
public class MessageController {
  @Resource
  private MessageRepository messageRepository;

  @Resource
  private MongoTemplate mongoTemplate;

  @Resource
  private Vertx vertx;

  /**
   * 获取好友聊天记录
   *
   * @param request
   * @return
   */
  @RequestMapping("getChatHistoryMessage")
  @ResponseBody
  public HttpResult getChatHistoryMessage(@RequestBody MessageQueryPara messageQueryPara, HttpServletRequest request) {
    User loginUser = WebUtil.loginUser(request);
    String id = messageQueryPara.getId();
    Criteria criteria1 = Criteria.where("from").is(new User(loginUser.getId())).and("to").is(id);
    Criteria criteria2 = Criteria.where("from").is(new User(id)).and("to").is(loginUser.getId());
    Criteria criteria3 = Criteria.where("createTime").lt(messageQueryPara.getTime()).orOperator(criteria1, criteria2);
    Query query = Query.query(criteria3).with(new PageRequest(0, 20)).with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
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
    Criteria criteria1 = Criteria.where("from").is(new User("57dfff487294a0af27bd7426")).and("to").is("57dfff487294a0af27bd7427");
    Criteria criteria2 = Criteria.where("from").is(new User("57dfff487294a0af27bd7427")).and("to").is("57dfff487294a0af27bd7426");
    Criteria criteria3 = Criteria.where("createTime").lt(System.currentTimeMillis()).orOperator(criteria1, criteria2);
    Query query = Query.query(criteria3).with(new PageRequest(0, 20)).with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
    List<Message> messages = mongoTemplate.find(query, Message.class);
    return new HttpResult(new HistoryMessage(id, messages));
  }

  /**
   * 发送消息 TODO
   *
   * @param message
   */
  @RequestMapping("sendMessage")
  @ResponseBody
  public void sendMessage(@RequestBody Message message, HttpServletRequest request) {
    User user = WebUtil.loginUser(request);
    message = messageRepository.save(message);
    String type = message.getType();
    switch (type) {
      //发送好友消息
      case Constant.MESSAGE_TYPE_CHAT: {
        UserChatHistory one = mongoTemplate.findOne(Query.query(Criteria.where("user").is(user)), UserChatHistory.class);
        LinkedList<ChatHistory> chatHistorys = one.getChatHistorys();
        if (chatHistorys.size() > 100) {
          chatHistorys.removeLast();
        }
        boolean present = chatHistorys.stream().filter(chatHistory -> chatHistory.getUser().getId() == user.getId()).findAny().isPresent();
        ChatHistory first = new ChatHistory(user, message);

        //移位
        if (chatHistorys.size() > 0) {
          ChatHistory chatHistory = chatHistorys.get(0);
          if (chatHistory.getUser().getId() != user.getId()) {
            Iterator<ChatHistory> iterator = chatHistorys.iterator();
            while (iterator.hasNext()) {
              ChatHistory next = iterator.next();
              if (next.getUser().getId() == user.getId()) {
                iterator.remove();
                first = next;
                break;
              }
            }
            chatHistorys.addFirst(first);
          }
        } else {
          chatHistorys.addFirst(first);
        }
        mongoTemplate.updateFirst(Query.query(null), Update.update(null, null), UserChatHistory.class);


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
