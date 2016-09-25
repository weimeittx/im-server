angular.module('imApp', ['imService'])
  .controller('bodyController', function ($scope) {
    $scope.isLogin = false;
    $scope.min = function () {
      console.log("最小化")
    }
    $scope.exit = function () {
      console.log("关闭")
    }
  })
  .controller('navController', function ($scope, userService) {
    $scope.$watch('isLogin', function (isLogin) {
      if (isLogin) {
        $scope.user = userService.user
      }
    })
    $scope.switchNav = function (index) {
      $scope.$parent.currentNav = index;
    }
  })
  .controller('loginController', function ($scope, loginService, userService, eventBusService) {
    $scope.login = function () {
      loginService.login($scope.username, $scope.password, function (result) {
        if (result.success) {
          $scope.$parent.isLogin = true;
          $scope.$parent.currentNav = 1;
          userService.user = result.result;
        } else {
          alert("密码帐号错误!")
        }
      })
    }
  })
  .controller('chatController', function ($scope, chatHistory, inputService, userService, messageService, eventBusService, chatService, chatGroupService) {

    /**
     * 移动对话到最前
     * @param userOrChatGroup
     * @param message
     */
    var moveTop = function (userOrChatGroup, message) {
      var delHistory = undefined;
      var firstHistory = {
        message: message
      };
      if (message.type == "chat") {
        firstHistory.user = userOrChatGroup;
      } else {
        firstHistory.chatGroup = userOrChatGroup;
      }
      for (var i = 0; i < $scope.historys.length; i++) {
        var tempHistory = $scope.historys[i];
        var chat = tempHistory.user ? tempHistory.user : tempHistory.chatGroup;
        if (chat.id == userOrChatGroup.id) {
          delHistory = tempHistory;
          delHistory.message = message;
          break;
        }
      }
      //从列表中找到需要删除的聊天对象
      if (delHistory) {
        //执行删除
        $scope.del(delHistory, false);
        if ($scope.currentChat) {
          $scope.currentChat = userOrChatGroup;

        }

      }
      $scope.historys.splice(0, 0, firstHistory);
    };

    $scope.historys = [];
    //当前聊天对象
    $scope.currentChat = undefined;
    $scope.isLeft = function (message) {
      return message.from.id != userService.user.id
    };
    //删除最近聊天对象
    $scope.del = function (chat, flag) {
      $scope.historys.splice($scope.historys.indexOf(chat), 1);
      var id = chat.user ? chat.user.id : chat.chatGroup.id
      if (!flag) {
        chatHistory.delHistory(id);
      }
      //$scope.currentChat = undefined
    };
    $scope.continueChat = function (history) {
      inputService.setFocus();
      var unReadCount = history.unReadCount;
      history.unReadCount = 0;
      var chat = undefined;
      if (history.user) {
        chat = history.user;
        $scope.title = chat.nickname;
        $scope.currentChatType = "chat"// TODO
      } else {
        chat = history.chatGroup;
        $scope.title = chat.chatGroupName;
        $scope.currentChatType = "chatGroup"// TODO
      }

      if ($scope.currentChat && $scope.currentChat.id == chat.id) {
        console.log("重复点击");
        return;
      }
      $scope.messages = [];
      $scope.currentChat = chat;
      var topChat = undefined;
      angular.forEach($scope.historys, function (temp) {
        var tempChat = temp.user ? temp.user : temp.chatGroup;
        if (chat.id == tempChat.id) {
          topChat = tempChat;
        }
      });
      if (!topChat) {
        $scope.historys.splice(0, 0, history);
        chatHistory.moveEmptyMessageChatTop(chat.id, $scope.currentChatType);
      }
      if (history.user) {
        chatHistory.getChatHistoryMessage(chat, new Date().getTime(), function (result) {
          if (result.success) {
            if (result.result.messages.length > 0) {
              $scope.messages = result.result.messages.reverse();
              for (var index in $scope.historys) {
                var history = $scope.historys[index];
                var chat = history.user ? history.user : history.chatGroup;
                if (chat.id == $scope.currentChat.id) {
                  history.message = $scope.messages[$scope.messages.length - 1]
                  break;
                }
              }
            }
          }
        })
      } else {
        chatHistory.getChatGroupHistoryMessage(chat, new Date().getTime(), function (result) {
          if (result.success) {
            if (result.result.messages.length > 0) {
              $scope.messages = result.result.messages.reverse();
            }
          }
        });
      }

    };


    $scope.send = function () {
      var content = inputService.getContent();
      inputService.setFocus();
      if (!content) {
        console.log('请输入文本!');
        return;
      }
      var user = userService.user;
      var message = {
        type: $scope.currentChatType,//TODO
        to: $scope.currentChat.id,
        from: user,
        content: content,
        createTime: new Date().getTime()
      };
      moveTop($scope.currentChat, message)
      $scope.messages.push(message);
      inputService.clearContent();
      messageService.sendMessage(message, function () {
        console.log("发送成功!")
      });
    };


    $scope.$watch('isLogin', function (isLogin) {
      //如果已经登录
      if (isLogin) {
        $scope.loginUser = userService.user;
        chatHistory.getHistory(function (result) {
          if (result.success) {
            console.log(result);
            $scope.historys = result.result ? result.result : [];
          }
        });


        eventBusService.addOpenListener(function () {
          console.log("监听消息成功 -> " + userService.user.id);
          eventBusService.registerHandler(userService.user.id, function (msg) {
            switch (msg.type) {
              case 'chat':
                chatService.getChat(function (result) {
                  if (result.success) {
                    var user = undefined;
                    angular.forEach(result.result, function (tempChats) {
                      angular.forEach(tempChats.chats, function (tempChat) {
                        if (tempChat.id == msg.from.id) {
                          user = tempChat;
                        }
                      });
                    });
                    if (user) {
                      msg.from = user;
                      moveTop(user, msg);
                      if ($scope.currentChat && $scope.currentChat.id == msg.from.id) {
                        $scope.messages.push(msg)
                      } else {
                        console.log("缓存消息")
                      }
                      $scope.$apply();
                    }

                  }
                });
                break;
              case 'chatGroup':
                console.log("接收到群消息 -->   ", msg);
                chatGroupService.getChatGroup(function (result) {
                  if (result.success) {
                    var groupChat = undefined;
                    for (var i = 0; i < result.result.length; i++) {
                      var tempGroupChat = result.result[i];
                      if (tempGroupChat.id == msg.to) {
                        groupChat = tempGroupChat;
                        break;
                      }
                    }
                    //找到
                    if (groupChat) {
                      moveTop(groupChat, msg)
                      console.log($scope.currentChat);
                      if ($scope.currentChat && $scope.currentChat.id == groupChat.id) {
                        console.log($scope.messages)
                        if ($scope.messages) {
                          $scope.messages.push(msg)
                        } else {
                          console.log("缓存消息1")
                        }

                      } else {
                        console.log("缓存群消息2")
                      }
                      $scope.$apply()
                      console.log($scope.currentChat);
                    }
                  }
                });
                break;
              default:
                break;
            }
          })
        });

        eventBusService.start();


      }
    })
  })
  .controller('contactlistController', function ($scope, chatGroupService, chatService) {

    $scope.viewChatInfo = function (chatItem) {
      $scope.viewInfo = 1;
      $scope.contactTitle = undefined;
      $scope.chatInfo = chatItem;
      $scope.currentContact = chatItem;
    };
    $scope.viewChatGroupInfo = function (chatGroup) {
      $scope.currentContact = chatGroup;
      $scope.viewInfo = 2;
      $scope.contactTitle = chatGroup.chatGroupName;
      chatGroupService.getChatGroupMembers(chatGroup.id, function (result) {
        if (result.success) {
          $scope.chatGroup = result.result;
          $scope.contactTitle = chatGroup.chatGroupName + " (" + chatGroup.members.length + "人)";
        }
      })
    };
    $scope.$watch('isLogin', function (isLogin) {
      //如果已经登录
      if (isLogin) {
        chatGroupService.getChatGroup(function (result) {
          if (result.success) {
            $scope.chatGroups = result.result;
          }
        });
        chatService.getChat(function (result) {
          if (result.success) {
            $scope.chats = result.result;
          }
        })
      }
    });

    $scope.enterChat = function (chat) {
      $scope.$parent.currentNav = 1;
      chat.type = "chat";
      $scope.$parent.continueChat({
        user: chat
      })
    };
    $scope.enterGroupChat = function (chatGroup) {
      console.log(chatGroup);
      $scope.$parent.currentNav = 1;
      chatGroup.type = "chatGroup";
      $scope.$parent.continueChat({
        chatGroup: chatGroup
      })
    }

  })