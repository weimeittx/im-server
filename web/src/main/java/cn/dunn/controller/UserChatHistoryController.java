package cn.dunn.controller;

import cn.dunn.mode.HttpResult;
import cn.dunn.mode.UserChatHistory;
import cn.dunn.mongo.UserChatHistoryRepository;
import cn.dunn.util.WebUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
}
