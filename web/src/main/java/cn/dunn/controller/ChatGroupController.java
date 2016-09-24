package cn.dunn.controller;

import cn.dunn.mode.ChatGroup;
import cn.dunn.mode.GroupMember;
import cn.dunn.mode.HttpResult;
import cn.dunn.mongo.ChatGroupRepository;
import cn.dunn.mongo.GroupMemberRepository;
import cn.dunn.util.WebUtil;
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

  @RequestMapping("/getChatGroup")
  @ResponseBody
  public HttpResult getChatGroup(HttpServletRequest request) {
    List<GroupMember> groupMembers = groupMemberRepository.findByMember(WebUtil.LoginUser(request));
    List<ChatGroup> result = groupMembers.stream().map(GroupMember::getChatGroup).collect(Collectors.toList());
    return new HttpResult(result);
  }
}
