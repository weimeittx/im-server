package cn.dunn.mongo;

import cn.dunn.mode.ChatHistory;
import cn.dunn.mode.User;
import cn.dunn.mode.UserChatHistory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Administrator on 2016/9/24.
 */
public interface UserChatHistoryRepository extends MongoRepository<UserChatHistory, String> {

  UserChatHistory findByUser(User user);

  default void updateChatHistorys(User loginUser, Function<ChatHistory, Boolean> f, Consumer<LinkedList<ChatHistory>> c) {
    UserChatHistory userChatHistory = findByUser(new User(loginUser.getId()));
    if (userChatHistory != null) {
      LinkedList<ChatHistory> chatHistorys = userChatHistory.getChatHistorys();
      if (chatHistorys != null && chatHistorys.size() > 0) {
        boolean update = false;
        for (ChatHistory chatHistory : chatHistorys) {
          if (f.apply(chatHistory)) {
            update = true;
            break;
          }
        }
        if (update) {
          c.accept(chatHistorys);
        }
      }
    }
  }
}
