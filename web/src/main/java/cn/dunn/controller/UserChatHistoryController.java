package cn.dunn.controller;

import cn.dunn.mode.*;
import cn.dunn.mongo.UserChatHistoryRepository;
import cn.dunn.util.WebUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Administrator on 2016/9/24.
 */
@Controller
@RequestMapping("userChatHistory")
public class UserChatHistoryController {
  @Resource
  private UserChatHistoryRepository userChatHistoryRepository;

  @Resource
  private MongoTemplate mongoTemplate;

  @RequestMapping("getUserChatHistory")
  @ResponseBody
  public HttpResult getUserChatHistory(HttpServletRequest request) {
    UserChatHistory result = mongoTemplate.findOne(Query.query(Criteria.where("user").is(WebUtil.loginUser(request))), UserChatHistory.class);
    if (result == null) {
      return new HttpResult(new LinkedList<ChatHistory>());
    }
    return new HttpResult(result.getChatHistorys());
  }

  /**
   * 删除历史聊天对象
   *
   * @param id
   * @return
   */
  @RequestMapping("delHistory")
  @ResponseBody
  public HttpResult delHistory(String id, HttpServletRequest request) {
    User user = WebUtil.loginUser(request);
    LinkedList<ChatHistory> chatHistorys = mongoTemplate.findOne(Query.query(Criteria.where("user").is(new User(user.getId()))), UserChatHistory.class).getChatHistorys();
    Iterator<ChatHistory> iterator = chatHistorys.iterator();
    while (iterator.hasNext()) {
      try {
        ChatHistory next = iterator.next();
        String dbId = next.getUser() == null ? next.getChatGroup().getId() : next.getUser().getId();
        if (id.equals(dbId)) {
          iterator.remove();
          break;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    mongoTemplate.updateFirst(Query.query(Criteria.where("user").is(new User(user.getId()))), Update.update("chatHistorys", chatHistorys), UserChatHistory.class);
    return new HttpResult();
  }

  /**
   * 更新群消息已读状态
   *
   * @param id      群ID
   * @param request
   * @return
   */
  @RequestMapping("chatGroupMessageReaded")
  @ResponseBody
  public HttpResult chatGroupMessageReaded(String id, String messageId, HttpServletRequest request) {
    User loginUser = WebUtil.loginUser(request);

    updateChatHistorys(loginUser, chatHistory -> {
      if (chatHistory.getChatGroup() != null && chatHistory.getChatGroup().getId().equals(id)) {
        chatHistory.setUnReadCount(0L);
        chatHistory.setMessage(new Message(messageId));
        return true;
      }
      return false;
    });

    return new HttpResult();
  }

  /**
   * 更新好友消息已读状态
   *
   * @param id      好友ID
   * @param request
   * @return
   */
  @RequestMapping("userMessageReaded")
  @ResponseBody
  public HttpResult userMessageReaded(String id, String messageId, HttpServletRequest request) {
    User loginUser = WebUtil.loginUser(request);

    updateChatHistorys(loginUser, chatHistory -> {
      if (chatHistory.getUser() != null && chatHistory.getUser().getId().equals(id)) {
        chatHistory.setUnReadCount(0L);
        chatHistory.setMessage(new Message(messageId));
        return true;
      }
      return false;
    });
    return new HttpResult();
  }

  private void updateChatHistorys(User loginUser, Function<ChatHistory, Boolean> f) {
    UserChatHistory userChatHistory = userChatHistoryRepository.findByUser(new User(loginUser.getId()));
    if (userChatHistory != null) {
      LinkedList<ChatHistory> chatHistorys = userChatHistory.getChatHistorys();
      if (chatHistorys != null && chatHistorys.size() > 0) {
        boolean update = false;
        for (ChatHistory chatHistory : chatHistorys) {
          if (f.apply(chatHistory)) {
            update = true;
          }
        }
        if (update) {
          mongoTemplate.updateFirst(Query.query(Criteria.where("user").is(new User(loginUser.getId()))), Update.update("chatHistorys", chatHistorys), UserChatHistory.class);
        }
      }
    }
  }
}
