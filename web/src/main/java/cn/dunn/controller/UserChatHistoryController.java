package cn.dunn.controller;

import cn.dunn.mode.ChatHistory;
import cn.dunn.mode.HttpResult;
import cn.dunn.mode.User;
import cn.dunn.mode.UserChatHistory;
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
        if(id.equals(dbId)){
          iterator.remove();
          break;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    mongoTemplate.updateFirst(Query.query(Criteria.where("user").is(new User(user.getId()))), Update.update("chatHistorys",chatHistorys),UserChatHistory.class);
    return new HttpResult();
  }
}
