package cn.dunn.mongo;

import cn.dunn.mode.User;
import cn.dunn.mode.UserChatHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Administrator on 2016/9/24.
 */
public interface UserChatHistoryRepository extends MongoRepository<UserChatHistory, String> {

  UserChatHistory findByUser(User user);

}
