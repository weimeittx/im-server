package cn.dunn.service.impl;

import cn.dunn.mode.ChatHistory;
import cn.dunn.mode.Message;
import cn.dunn.mode.User;
import cn.dunn.mode.UserChatHistory;
import cn.dunn.mongo.UserChatHistoryRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;

/**
 * Created by Administrator on 2016/9/26.
 */
@Service
public class UserChatHistoryService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private UserChatHistoryRepository userChatHistoryRepository;

    /**
     * 标记用户已经对该群读过消息
     *  @param userId
     * @param chatGroupId
     * @param message
     */
    public void markChatGroupReaded(String userId, String chatGroupId, Message message) {
        UserChatHistory userChatHistory = userChatHistoryRepository.findByUser(new User(userId));
        if (userChatHistory != null) {
            LinkedList<ChatHistory> chatHistorys = userChatHistory.getChatHistorys();
            if (chatHistorys != null && chatHistorys.size() > 0) {
                for (ChatHistory chatHistory : chatHistorys) {
                    if (chatHistory.getChatGroup() != null && chatHistory.getChatGroup().getId().equals(chatGroupId)) {
                        chatHistory.setUnReadCount(0L);
                        chatHistory.setMessage(message);
                        break;
                    }
                }
                mongoTemplate.updateFirst(Query.query(Criteria.where("user").is(new User(userId))), Update.update("chatHistorys", chatHistorys), UserChatHistory.class);
            }
        }
    }
}
