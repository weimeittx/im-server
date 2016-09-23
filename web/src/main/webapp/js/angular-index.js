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
          //eventBusService.init();
          //eventBusService.registerHandler(userService.user.id, function (msg) {
          // })
        } else {
          alert("登录失败")
        }
      })
    }
  })
  .controller('chatController', function ($scope, chatHistory, inputService, userService) {
    $scope.historys = [];
    //当前聊天对象
    var currentChat = undefined;
    $scope.isLeft = function (message) {
      return message.chatId != userService.user.id
    };
    $scope.del = function (chat) {
      $scope.historys.splice($scope.historys.indexOf(chat), 1)
    };
    $scope.continueChat = function (chat) {
      chatHistory.getHistoryMessage(chat, function (result) {
        $scope.title = chat.chatName;
        $scope.messages = result.messages;
      })
    };


    $scope.send = function () {
      var content = inputService.getContent();
      if (!content) {
        console.log('请输入文本!')
        return;
      }
      var user = userService.user;
      var message = {
        chatId: user.id,
        head: user.head,
        content: content
      }
      $scope.messages.push(message)
      inputService.clearContent()
      inputService.setFocus()
    }


    $scope.$watch('isLogin', function (isLogin) {
      //如果已经登录
      if (isLogin) {
        chatHistory.getHistory(function (result) {
          if (result.success) {
            $scope.historys = result.result
          }
        });
      }
    })
  })
  .controller('contactlistController', function ($scope, chatGroupService, chatService) {

    $scope.viewChatInfo = function (chatItem) {
      $scope.viewInfo = 1;
      $scope.chatInfo = chatItem;
    };
    $scope.viewChatGroupInfo = function (chatGroup) {
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
    })

    $scope.enterChat = function (chat) {
      $scope.$parent.currentNav = 1;
      console.log(chat)
      console.log($scope)
      console.log($scope.$parent.continueChat({
        chatName:chat.nickname,
        id:chat.id,
        type:"chat"
      }))
    }
    $scope.enterGroupChat = function (chatGroup) {
      console.log(chatGroup)
    }

  })