package cn.dunn.controller;

import cn.dunn.mode.ChatGroup;
import cn.dunn.mode.GroupMember;
import cn.dunn.mode.HttpResult;
import cn.dunn.mode.User;
import cn.dunn.mongo.ChatGroupRepository;
import cn.dunn.mongo.GroupMemberRepository;
import cn.dunn.util.WebUtil;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chatGroup")
public class ChatGroupController {
  @Resource
  private ChatGroupRepository chatGroupRepository;

  @Resource
  private GroupMemberRepository groupMemberRepository;

  @Resource
  private MongoTemplate mongoTemplate;

  @RequestMapping("/getChatGroup")
  @ResponseBody
  public HttpResult getChatGroup(HttpServletRequest request) {
    List<ChatGroup> result = mongoTemplate.find(Query.query(Criteria.where("members").elemMatch(Criteria.where("$id").is(new ObjectId(WebUtil.loginUser(request).getId())))), ChatGroup.class);
    return new HttpResult(result);
  }


  @RequestMapping("getChatGroupMembers")
  @ResponseBody
  public HttpResult getChatGroupMembers(String id) {
    ChatGroup chatGroup = chatGroupRepository.findOne(id);
    return new HttpResult(chatGroup);
  }
}
