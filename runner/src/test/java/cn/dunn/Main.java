package cn.dunn;

import cn.dunn.mode.*;
import cn.dunn.mongo.*;
import cn.dunn.service.FileService;
import cn.dunn.util.MD5Util;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBList;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.datagram.impl.InternetProtocolFamily;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;
import org.junit.internal.runners.statements.RunAfters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/9/19.
 */
public class Main {
  ApplicationContext context;
  ChatGroupRepository chatGroupRepository;
  FriendNexusRepository friendNexusRepository;
  GroupMemberRepository groupMemberRepository;
  MessageRepository messageRepository;
  UserRepository userRepository;
  MongoTemplate mongoTemplate;
  FileService fileService;
  UserChatHistoryRepository userChatHistoryRepository;

  {
    context = new ClassPathXmlApplicationContext("applicationContext.xml");
    chatGroupRepository = context.getBean(ChatGroupRepository.class);
    friendNexusRepository = context.getBean(FriendNexusRepository.class);
    groupMemberRepository = context.getBean(GroupMemberRepository.class);
    messageRepository = context.getBean(MessageRepository.class);
    userRepository = context.getBean(UserRepository.class);
    mongoTemplate = context.getBean(MongoTemplate.class);
    fileService = context.getBean(FileService.class);
    userChatHistoryRepository = context.getBean(UserChatHistoryRepository.class);
  }

  @Test
  public void testAddUser() {
    User user = new User();
    user.setUsername("422450455@qq.com");
    user.setPassword(MD5Util.MD5("123456"));
    userRepository.save(user);

    user.setUsername("522168033@qq.com");
    user.setPassword(MD5Util.MD5("123456"));
    user.setId(null);
    userRepository.save(user);

    user.setUsername("865795615@qq.com");
    user.setPassword(MD5Util.MD5("123456"));
    user.setId(null);
    userRepository.save(user);
  }

  @Test
  public void testAddfriend() {
    FriendNexus friend = new FriendNexus();
    friend.setSelf(new User("57dfff487294a0af27bd7426"));
    friend.setFriend(new User("57dfff487294a0af27bd7427"));
    friend.setLastReadTime(System.currentTimeMillis());
    friendNexusRepository.save(friend);


    friend.setId(null);
    friend.setSelf(new User("57dfff487294a0af27bd7426"));
    friend.setFriend(new User("57dfff487294a0af27bd7428"));
    friend.setLastReadTime(System.currentTimeMillis());
    friendNexusRepository.save(friend);

    friend.setId(null);
    friend.setSelf(new User("57dfff487294a0af27bd7427"));
    friend.setFriend(new User("57dfff487294a0af27bd7426"));
    friend.setLastReadTime(System.currentTimeMillis());
    friendNexusRepository.save(friend);


    friend.setId(null);
    friend.setSelf(new User("57dfff487294a0af27bd7428"));
    friend.setFriend(new User("57dfff487294a0af27bd7426"));
    friend.setLastReadTime(System.currentTimeMillis());
    friendNexusRepository.save(friend);
  }

  @Test
  public void testgetFriends() {
    List<FriendNexus> friends = friendNexusRepository.findBySelf(new User("57dfff487294a0af27bd7426"));
    List<User> collect = friends.stream().map(FriendNexus::getFriend).collect(Collectors.toList());
    collect.stream().forEach(u -> {
      System.out.println(u.getUsername());
    });
  }


  @Test
  public void testAddChatGroup() {
    ChatGroup chatGroup = new ChatGroup();
    chatGroup.setAffiche("这是麻将群");
    chatGroup.setChatGroupName("麻将群");
    chatGroup = chatGroupRepository.save(chatGroup);

    GroupMember groupMember = new GroupMember();
    groupMember.setChatGroup(chatGroup);
    groupMember.setMember(new User("57dfff487294a0af27bd7426"));
    groupMemberRepository.save(groupMember);


    chatGroup.setId(null);
    chatGroup.setAffiche("这是游戏群");
    chatGroup.setChatGroupName("游戏群");
    chatGroupRepository.save(chatGroup);

    groupMember = new GroupMember();
    groupMember.setChatGroup(chatGroup);
    groupMember.setMember(new User("57dfff487294a0af27bd7426"));
    groupMemberRepository.save(groupMember);
  }


  @Test
  public void testGetChatGroups() {
    List<ChatGroup> chatGroups = groupMemberRepository.findByMember(new User("57dfff487294a0af27bd7426")).stream().map(GroupMember::getChatGroup).collect(Collectors.toList());
    chatGroups.stream().forEach(chatGroup -> {
      System.out.println(chatGroup.getChatGroupName());
    });
  }

  @Test
  public void testAddFile() throws FileNotFoundException {
    Map<String, Object> map = new HashMap<>();
    fileService.saveFile(new FileInputStream("C:\\Users\\Administrator\\Desktop\\测试.txt"), "测试.txt", "txt", map);
  }

  @Test
  public void testDeleteFile() {
    fileService.deleteFile("57e1247372942202601ba652");
  }

  @Test
  public void testGetOneFile() throws Exception {
    fileService.getOneFile("57e1247372942202601ba652");
  }

  @Test
  public void testMessageSend() {
    String[] users = {"57dfff487294a0af27bd7426", "57dfff487294a0af27bd7427", "57dfff487294a0af27bd7428"};
    String[] groups = {"57e00817729418413bb32f1e", "57e00817729418413bb32f20"};
    String[] usersAndGroups = {"57dfff487294a0af27bd7426", "57dfff487294a0af27bd7427", "57dfff487294a0af27bd7428", "57e00817729418413bb32f1e", "57e00817729418413bb32f20"};

    Random random = new Random();
    long l = System.currentTimeMillis();
    for (int i = 0; i < 200000; i++) {
      Message message = new Message();
      message.setContent(UUID.randomUUID().toString());
      message.setFrom(new User(users[i % users.length]));
      message.setCreateTime(random.nextInt(Integer.MAX_VALUE) + 1L);
      message.setTo(usersAndGroups[i % usersAndGroups.length]);
      messageRepository.save(message);
    }
    System.out.println("一共耗时 => " + (System.currentTimeMillis() - l));

  }

  @Test
  public void testMessageGet() {
    String[] users = {"57dfff487294a0af27bd7426", "57dfff487294a0af27bd7427"};
    String[] groups = {"57e00817729418413bb32f1e", "57e00817729418413bb32f20"};

//
//    Criteria criteria = Criteria.where("from").is(new User("57dfff487294a0af27bd7426"))
//      .orOperator(Criteria.where("to").is("57dfff487294a0af27bd7427"))
//      .orOperator(Criteria.where("from").is(new User("57dfff487294a0af27bd7427")))
//      .orOperator(Criteria.where("to").is("57dfff487294a0af27bd7426")).andOperator(Criteria.where("to").is("57dfff487294a0af27bd7427").orOperator(Criteria.where("from").is(new User("57dfff487294a0af27bd7427"))));


    Criteria criteria = Criteria.where("from").in(new User("57dfff487294a0af27bd7426"), new User("57dfff487294a0af27bd7427"))
      .andOperator(Criteria.where("to").in("57dfff487294a0af27bd7426", "57dfff487294a0af27bd7427"), Criteria.where("createTime").lt(System.currentTimeMillis()));
    Query query = Query.query(criteria).with(new PageRequest(1, 20)).with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
    List<Message> messages = mongoTemplate.find(query, Message.class);
    System.out.println(JSONObject.toJSONString(messages));
  }

  @Test
  public void testAddUserChatHistory() {
    UserChatHistory userChatHistory = new UserChatHistory();
    userChatHistory.setUser(new User("57dfff487294a0af27bd7427"));
    LinkedList<ChatHistory> chatHistorys = new LinkedList<>();

    ChatHistory chatHistory = new ChatHistory(new User("57dfff487294a0af27bd7426"), new Message("57e562ca7294fbe24e1015b6"));
    chatHistorys.add(chatHistory);

    userChatHistory.setChatHistorys(chatHistorys);
    userChatHistoryRepository.save(userChatHistory);
  }

  @Test
  public void testGetUserChatHistory() {
    UserChatHistory userChatHistory = mongoTemplate.findOne(Query.query(Criteria.where("user").is(new User("57dfff487294a0af27bd7426"))), UserChatHistory.class);
    System.out.println(JSONObject.toJSONString(userChatHistory));
  }

  @Test
  public void testVertxPublish() throws InterruptedException {
    Vertx vertx = context.getBean(Vertx.class);
    Map<String, Object> map = new HashMap<>();
    map.put("username", "zhangsan");
    vertx.eventBus().publish("57dfff487294a0af27bd7426", new JsonObject(map));
    Thread.sleep(Integer.MAX_VALUE);
  }

  @Test
  public void testVertxConsume() throws InterruptedException {
    Vertx vertx = context.getBean(Vertx.class);
    vertx.eventBus().<JsonObject>consumer("57dfff487294a0af27bd7427", event -> {
        JsonObject body = event.body();
        System.err.println(body);
      }
    );
    System.out.println("监听开启成功 ... ");
    Thread.sleep(Integer.MAX_VALUE);
  }

}

