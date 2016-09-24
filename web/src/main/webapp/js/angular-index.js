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
  .controller('chatController', function ($scope, chatHistory, inputService, userService, messageService, eventBusService, chatService) {
    var moveTop = function () {
      console.log("需要移动");
      var temp = undefined;
      angular.forEach($scope.historys, function (t) {
        var chat = t.user ? t.user : t.chatGroup;
        if (chat.id == $scope.currentChat.id) {
          $scope.del($scope.currentChat);
          $scope.currentChat = chat
          temp = t
        }
      });

      $scope.historys.splice(0, 0, temp);
    };
    var isTop = function () {
      if ($scope.historys.length > 1) {
        var temp = $scope.historys[0];
        var chat = temp.user ? temp.user : temp.chatGroup;
        return chat.id == $scope.currentChat.id
      }
      return false;
    };


    var isTopBy = function (userOrChatGroup) {
      if ($scope.historys.length > 1) {
        var first = $scope.historys[0];
        var chat = first.user ? first.user : first.chatGroup;
        return chat.id == userOrChatGroup.id
      }
      return false;
    };


    var moveTopBy = function (history) {
      var temp = undefined;
      var id = history.user ? history.user.id : history.chatGroup.id;
      angular.forEach($scope.historys, function (t) {
        var chat = t.user ? t.user : t.chatGroup;
        if (chat.id == id) {
          temp = t;
        }
      });

      if (temp) {
        $scope.del(temp);
        $scope.historys.splice(0, 0, history);
      }
    }

    $scope.historys = [];
    //当前聊天对象
    $scope.currentChat = undefined;
    $scope.isLeft = function (message) {
      return message.from.id != userService.user.id
    };
    //删除最近聊天对象
    $scope.del = function (chat) {
      $scope.historys.splice($scope.historys.indexOf(chat), 1)
    };
    $scope.continueChat = function (history) {
      var chat = undefined;
      if (history.user) {
        chat = history.user;
        $scope.title = chat.nickname;
      } else {
        chat = history.chatGroup;
        $scope.title = chat.chatGroupName;
      }
      $scope.messages = [];
      $scope.currentChat = chat;
      inputService.setFocus();
      chatHistory.getHistoryMessage(chat, new Date().getTime(), function (result) {
        if (result.success) {
          console.log(result.result.messages)
          $scope.messages = result.result.messages.reverse();
        }
      })
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
        head: user.head,
        type: 'chat',//TODO
        to: $scope.currentChat.id,
        from: user,
        content: content,
        createTime: new Date().getTime()
      };
      if (!isTop()) {
        moveTop();
      }
      $scope.messages.push(message);
      $scope.historys[0] = {
        user: $scope.currentChat,
        message: message
      }
      inputService.clearContent();
      messageService.sendChatMessage(message, function () {
        console.log("发送成功!")
      })
      //$scope.$apply();
    };


    $scope.$watch('isLogin', function (isLogin) {
      //如果已经登录
      if (isLogin) {
        $scope.loginUser = userService.user;
        chatHistory.getHistory(function (result) {
          if (result.success) {
            console.log(result);
            $scope.historys = result.result
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
                      var history = {
                        user: user,
                        message: msg
                      };
                      if (!isTopBy(user)) {
                        moveTopBy(history);
                      } else {
                        $scope.historys[0] = history;
                      }
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
      $scope.currentContact = chatGroup
      $scope.viewInfo = 2
      $scope.contactTitle = chatGroup.chatGroupName + " (" + chatGroup.size + "人)";
      chatGroupService.getChatGroupMembers(chatGroup.id, function (result) {
        if (result.success) {
          $scope.chatGroup = result.result
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
      $scope.$parent.continueChat({
        chatName: chat.nickname,
        id: chat.id,
        type: "chat",
        head: chat.head
      })
    };
    $scope.enterGroupChat = function (chatGroup) {
      console.log(chatGroup)
    }

  })