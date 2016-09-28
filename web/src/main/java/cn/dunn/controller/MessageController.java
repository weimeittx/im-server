package cn.dunn.controller;


import cn.dunn.constant.Constant;
import cn.dunn.dto.HistoryMessage;
import cn.dunn.dto.MessageQueryPara;
import cn.dunn.mode.*;
import cn.dunn.mongo.ChatGroupRepository;
import cn.dunn.mongo.GroupMemberRepository;
import cn.dunn.mongo.MessageRepository;
import cn.dunn.mongo.UserChatHistoryRepository;
import cn.dunn.util.WebUtil;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.jsoup.Jsoup;
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

  @Resource
  private GroupMemberRepository groupMemberRepository;

  @Resource
  private ChatGroupRepository chatGroupRepository;

  @Resource
  private UserChatHistoryRepository userChatHistoryRepository;

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
    Criteria criteria3 = Criteria.where("createTime").lt(messageQueryPara.getTime()).and("type").is("chat").orOperator(criteria1, criteria2);
    Query query = Query.query(criteria3).with(new PageRequest(0, 20)).with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
    List<Message> messages = mongoTemplate.find(query, Message.class);


    userChatHistoryRepository.updateChatHistorys(new User(loginUser.getId()), chatHistory1 -> {
      if (chatHistory1.getUser() != null && chatHistory1.getUser().getId().equals(messageQueryPara.getId())) {
        chatHistory1.setUnReadCount(0L);
        return true;
      }
      return false;
    }, chatHistories -> mongoTemplate.updateFirst(Query.query(Criteria.where("user").is(new User(loginUser.getId()))), Update.update("chatHistorys", chatHistories), UserChatHistory.class));
    return new HttpResult(new HistoryMessage(id, messages));
  }

  /**
   * 获取群组聊天记录
   *
   * @return
   */
  @RequestMapping("getChatGroupHistoryMessage")
  @ResponseBody
  public HttpResult getChatGroupHistoryMessage(@RequestBody MessageQueryPara messageQueryPara, HttpServletRequest request) {
    Criteria criteria = Criteria.where("createTime").lt(messageQueryPara.getTime()).and("to").is(messageQueryPara.getId()).and("type").is("chatGroup");
    Query query = Query.query(criteria).with(new PageRequest(0, 20)).with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
    List<Message> messages = mongoTemplate.find(query, Message.class);

    User user = WebUtil.loginUser(request);

    //更新成员拉取消息时间
    UserChatHistory userChatHistory = userChatHistoryRepository.findByUser(new User(user.getId()));
    if (userChatHistory != null) {
      LinkedList<ChatHistory> chatHistorys = userChatHistory.getChatHistorys();
      if (chatHistorys != null && chatHistorys.size() > 0) {
        for (ChatHistory chatHistory : chatHistorys) {
          ChatGroup chatGroup = chatHistory.getChatGroup();
          if (chatGroup != null) {
            if (chatGroup.getId().equals(messageQueryPara.getId())) {
              chatHistory.setUnReadCount(0L);
            }
            break;
          }
        }
      }
      mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(userChatHistory.getId())), Update.update("chatHistorys", chatHistorys), UserChatHistory.class);
    }
    return new HttpResult(new HistoryMessage(messageQueryPara.getId(), messages));
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
    message.setId(null);
    Message saveMessage = messageRepository.save(message);
    String type = saveMessage.getType();
    EventBus eventBus = vertx.eventBus();
    JsonObject jsonMessage = new JsonObject(JSONObject.toJSONString(saveMessage));
    switch (type) {
      //发送好友消息
      case Constant.MESSAGE_TYPE_CHAT: {
        //将两个用户的对话记录前置
        moveChatTop(saveMessage, user.getId(), saveMessage.getTo(), Constant.MESSAGE_TYPE_CHAT);
        moveChatTop(saveMessage, saveMessage.getTo(), user.getId(), Constant.MESSAGE_TYPE_CHAT);
        eventBus.publish(saveMessage.getTo(), jsonMessage);
        userChatHistoryRepository.updateChatHistorys(new User(user.getId()), chatHistory -> {
          if (chatHistory.getUser() != null && chatHistory.getUser().getId().equals(saveMessage.getTo())) {
            chatHistory.setUnReadCount(0L);
            chatHistory.setMessage(saveMessage);
            return true;
          }
          return false;
        }, chatHistories -> mongoTemplate.updateFirst(Query.query(Criteria.where("user").is(new User(user.getId()))), Update.update("chatHistorys", chatHistories), UserChatHistory.class));
        break;
      }
      //发送群组消息
      case Constant.MESSAGE_TYPE_GROUP: {
        //1.获取群成员
        ChatGroup chatGroup = mongoTemplate.findById(saveMessage.getTo(), ChatGroup.class);
        //2.对每个群成员推送群消息
        if (chatGroup != null) {
          chatGroup.getMembers().stream().parallel().forEach(member -> {
            if (!member.getId().equals(user.getId())) {
              eventBus.publish(member.getId(), jsonMessage);
            }
            moveChatTop(saveMessage, member.getId(), chatGroup.getId(), Constant.MESSAGE_TYPE_GROUP);
          });
        }
        userChatHistoryRepository.updateChatHistorys(new User(user.getId()), chatHistory -> {
          if (chatHistory.getChatGroup() != null && chatHistory.getChatGroup().getId().equals(saveMessage.getTo())) {
            chatHistory.setUnReadCount(0L);
            chatHistory.setMessage(saveMessage);
            return true;
          }
          return false;
        }, chatHistories -> mongoTemplate.updateFirst(Query.query(Criteria.where("user").is(new User(user.getId()))), Update.update("chatHistorys", chatHistories), UserChatHistory.class));


        break;
      }
    }
  }

  @RequestMapping("moveEmptyMessageChatTop")
  @ResponseBody
  public HttpResult moveEmptyMessageChatTop(String chatId, String type, HttpServletRequest request) {
    String userId = WebUtil.loginUser(request).getId();
    moveChatTop(null, userId, chatId, type);
    return new HttpResult();
  }

  /**
   * 移动会话到顶部
   *
   * @param message 最后一条消息
   * @param userId  会话所属人
   * @param chatId  会话ID (userId or chatGroupId)
   * @param type    类型 chat or chatGroup
   */
  private void moveChatTop(Message message, String userId, String chatId, String type) {
    ChatHistory first = null;
    //查询最后一条消息
    if (Constant.MESSAGE_TYPE_CHAT.equals(type)) {
      if (message == null) {
        Criteria criteria1 = Criteria.where("from").is(new User(userId)).and("to").is(chatId);
        Criteria criteria2 = Criteria.where("from").is(new User(chatId)).and("to").is(userId);
        Criteria criteria3 = Criteria.where("type").is("chat").orOperator(criteria1, criteria2);
        Query query = Query.query(criteria3).with(new PageRequest(0, 1)).with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
        message = mongoTemplate.findOne(query, Message.class);
      }
      first = new ChatHistory(new User(chatId), message);
    } else if (Constant.MESSAGE_TYPE_GROUP.equals(type)) {
      if (message == null) {
        message = mongoTemplate.findOne(Query.query(Criteria.where("to").is(chatId)), Message.class);
      }
      first = new ChatHistory(new ChatGroup(chatId), message);
    }// TODO


    UserChatHistory userChatHistory = mongoTemplate.findOne(Query.query(Criteria.where("user").is(new User(userId))), UserChatHistory.class);
    if (userChatHistory != null) {
      LinkedList<ChatHistory> chatHistorys = userChatHistory.getChatHistorys();
      if (chatHistorys == null) {
        chatHistorys = new LinkedList<>();
      }
      if (chatHistorys.size() > 100) {
        chatHistorys.removeLast();
      }
      Iterator<ChatHistory> iterator = chatHistorys.iterator();
      while (iterator.hasNext()) {
        ChatHistory next = iterator.next();
        String oldChatId = next.getUser() != null ? next.getUser().getId() : next.getChatGroup().getId();
        if (oldChatId.equals(chatId)) {
          Long unReadCont = next.getUnReadCount() == null ? 0 : next.getUnReadCount();
          if(unReadCont == 0){
            first.setStartUnReadMessage(message);
          }
          first.setUnReadCount(++unReadCont);
          iterator.remove();
          break;
        }
      }
      chatHistorys.addFirst(first);
      mongoTemplate.updateFirst(Query.query(Criteria.where("user").is(new User(userId))), Update.update("chatHistorys", chatHistorys), UserChatHistory.class);
    } else {
      userChatHistory = new UserChatHistory();
      userChatHistory.setUser(new User(userId));
      LinkedList<ChatHistory> chatHistorys = new LinkedList<>();
      first.setStartUnReadMessage(message);
      first.setUnReadCount(1L);
      chatHistorys.add(first);
      userChatHistory.setChatHistorys(chatHistorys);
      mongoTemplate.save(userChatHistory);
    }
  }
}
