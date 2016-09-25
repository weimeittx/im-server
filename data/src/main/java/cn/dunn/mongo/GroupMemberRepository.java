package cn.dunn.mongo;

import cn.dunn.mode.ChatGroup;
import cn.dunn.mode.GroupMember;
import cn.dunn.mode.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupMemberRepository extends MongoRepository<GroupMember, String> {
  List<GroupMember> findByMember(User member);


  /**
   * 按照群成员和群组 查找
   *
   * @param member
   * @param chatGroup
   * @return
   */
  GroupMember findByMemberAndChatGroup(User member, ChatGroup chatGroup);
}
