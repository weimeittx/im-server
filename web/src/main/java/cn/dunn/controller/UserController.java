package cn.dunn.controller;

import cn.dunn.mode.*;
import cn.dunn.mongo.FriendNexusRepository;
import cn.dunn.mongo.GroupMemberRepository;
import cn.dunn.util.FormatUtil;
import cn.dunn.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/9/22.
 */
@Controller
@RequestMapping("/user")
public class UserController {
  @Resource
  private FriendNexusRepository friendNexusRepository;

  @Resource
  private GroupMemberRepository groupMemberRepository;

  @RequestMapping("/getFriends")
  @ResponseBody
  public HttpResult getFriends(HttpServletRequest request) {
    List<GroupUser> result = FormatUtil.groupUser(friendNexusRepository.findBySelf(WebUtil.loginUser(request)).stream().map(FriendNexus::getFriend).collect(Collectors.toList()));
    return new HttpResult(result);
  }

}
