angular.module('imApp', ['imService'])
  .controller('bodyController', function ($scope) {

    $scope.viewUserInfo = function (user) {
      console.log(user);
    }
    $scope.isLogin = false;
    $scope.min = function () {
      console.log("最小化")
    }
    $scope.exit = function () {
      console.log("关闭")
    }
    $scope.logout = function () {
      $scope.isLogin = false;
    }
  })
  .controller('navController', function ($scope, userService) {
    $scope.$watch('isLogin', function (isLogin) {
      if (isLogin) {
        $scope.user = userService.user;
        $scope.$parent.currentNav = 1;
      } else {
        $scope.user = undefined;
        $scope.$parent.currentNav = undefined;
      }
    });
    $scope.switchNav = function (index) {
      $scope.$parent.currentNav = index;
    }
  })
  .controller('loginController', function ($scope, loginService, userService, eventBusService) {
    $scope.username = "422450455@qq.com";
    $scope.password = "123456";
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
  .controller('chatController', function ($scope, $sce, chatHistory, inputService, userService, messageService, eventBusService, chatService, chatGroupService, scrollService, utilService) {

    var loadInfo = true;

    var lastMessage = undefined;
    $(".message-view").scroll(function () {
      if ($(".message-view").scrollTop() < 30 && loadInfo) {
        loadInfo = false;
        if (lastMessage) {
          pullHistoryMessages(lastMessage)
        }
        loadInfo = true;
      }
    });


    $scope.isViewMessageHint = false;
    $scope.markInfo = '';
    $scope.shut = function () {
      $scope.isViewMessageHint = false;
    };
    $scope.messageId = 1;
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
          firstHistory.unReadCount = delHistory.unReadCount;
          break;
        }
      }
      //从列表中找到需要删除的聊天对象
      if (delHistory) {
        //执行删除
        $scope.del(delHistory, false);
        if ($scope.currentChat && $scope.currentChat.id == firstHistory.id) {
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
      if (flag) {
        chatHistory.delHistory(chat.user ? chat.user.id : chat.chatGroup.id);
      }
      var chatId = chat.user ? chat.user.id : chat.chatGroup.id
      if ($scope.currentChat && $scope.currentChat.id == chatId && flag) {
        $scope.currentChat = undefined;
      }
    };

    //拉取历史消息
    var pullHistoryMessages = function (message) {
      //拉取好友消息
      if (message.type == 'chat') {
        chatHistory.getChatHistoryMessage({id: $scope.currentChat.id}, message.createTime, function (result) {//拉取历史聊天记录
          if (!$scope.isLogin) {
            return;
          }
          if (result.success) {
            if (result.result.messages.length > 0) {//如果有该好友的历史消息
              if (result.result.messages.length < 20) {
                $scope.loadInfo = false;
              }
              var proMessageId = $scope.messages[0].id;
              angular.forEach(result.result.messages, function (m) {
                $scope.messages.splice(0, 0, m);
              });
              lastMessage = $scope.messages[0]
              setTimeout(function () {
                scrollService.scrollBottom(proMessageId);
              }, 100)
            } else {
              $scope.loadInfo = true;
              $scope.loadFinish = true;
              setTimeout(function () {
                $scope.loadInfo = false;
                $scope.$apply();
              }, 2000)
            }
          }
        })
      } else {//选中的是群聊
        chatHistory.getChatGroupHistoryMessage({id: $scope.currentChat.id}, message.createTime, function (result) {
          if (!$scope.isLogin) {
            return;
          }
          if (result.success) {
            if (result.result.messages.length > 0) {//如果有该好友的历史消息
              if (result.result.messages.length < 20) {
                $scope.loadInfo = false;
              }
              //$scope.messages = result.result.messages.reverse();
              var proMessageId = $scope.messages[0].id;
              angular.forEach(result.result.messages, function (m) {
                $scope.messages.splice(0, 0, m);
              });
              lastMessage = result.result.messages[result.result.messages.length - 1];
              setTimeout(function () {
                scrollService.scrollBottom(proMessageId);
              }, 100)
            } else {
              $scope.loadFinish = true;
              $scope.loadInfo = true;
              setTimeout(function () {
                $scope.loadInfo = false;
                $scope.$apply();
              }, 2000)
            }
          }
        });
      }
    };
    $scope.continueChat = function (history) {
      $scope.loadInfo = true;
      $scope.loadFinish = false;
      inputService.setFocus();
      var unReadCount = history.unReadCount;
      if (unReadCount > 0 && history.startUnReadMessage) {//显示未读标记
        $scope.messageId = "#" + history.startUnReadMessage.id
        $scope.isUnRead = true;
        $scope.markInfo = unReadCount + "条未读消息";
        $scope.isViewMessageHint = true;
      } else {
        $scope.isViewMessageHint = false;
      }
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
        return;
      }
      $scope.messages = [];
      $scope.currentChat = chat;//切换到当前聊天
      var topChat = undefined;
      angular.forEach($scope.historys, function (temp) {
        var tempChat = temp.user ? temp.user : temp.chatGroup;
        if (chat.id == tempChat.id) {
          topChat = tempChat;
        }
      });
      if (!topChat) {//没有在临时列表中找到则创建一个
        $scope.historys.splice(0, 0, history);
        chatHistory.moveEmptyMessageChatTop(chat.id, $scope.currentChatType);
      }
      if (history.user) {//选中的是好友
        chatHistory.getChatHistoryMessage(chat, new Date().getTime(), function (result) {//拉取历史聊天记录
          if (result.success) {
            if (result.result.messages.length > 0) {//如果有该好友的历史消息
              if (result.result.messages.length < 20) {
                $scope.loadInfo = false;
              }
              $scope.messages = result.result.messages.reverse();
              lastMessage = $scope.messages[0];
              history.message = $scope.messages[$scope.messages.length - 1];
              setTimeout(function () {
                scrollService.scrollBottom(history.message.id);
              }, 100);//移动滚动条
            } else {
              $scope.loadInfo = false;
            }
          }
        })
      } else {//选中的是群聊
        chatHistory.getChatGroupHistoryMessage(chat, new Date().getTime(), function (result) {
          if (result.success) {
            if (result.result.messages.length > 0) {
              if (result.result.messages.length < 20) {
                $scope.loadInfo = false;
              }
              $scope.messages = result.result.messages.reverse();
              lastMessage = $scope.messages[0]
              history.message = $scope.messages[$scope.messages.length - 1];
              setTimeout(function () {
                scrollService.scrollBottom(history.message.id);
              }, 100);
            } else {
              $scope.loadInfo = false;
            }
          }
        });
      }

    };


    $scope.send = function () {
      var content = inputService.getContent();
      inputService.setFocus();
      if (!content) {
        return;
      }
      try {
        var _content = $(content);
        if (_content.find(".loadingclass").length > 0) {
          alert("请等待上传完毕后发送消息!")
          return;
        }
        _content.find("img").attr("onclick", "viewImg(this)");
        content = _content[0].outerHTML;
      } catch (e) {
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
      message.id = utilService.UUID();
      $scope.messages.push(message);
      inputService.clearContent();
      messageService.sendMessage(message, function () {
      });


      setTimeout(function () {
        scrollService.scrollBottom(message.id);
      }, 100);


    };


    $scope.$watch('isLogin', function (isLogin) {
      //如果已经登录
      if (isLogin) {
        $scope.loginUser = userService.user;
        chatHistory.getHistory(function (result) {
          if (result.success) {
            $scope.historys = result.result ? result.result : [];
          }
        });


        eventBusService.addOpenListener(function () {
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
                      //msg.from = user;
                      moveTop(user, msg);
                      if ($scope.currentChat && $scope.currentChat.id == msg.from.id) {
                        $scope.messages.push(msg)
                        chatHistory.userMessageReaded(msg.id, msg.from.id);
                        setTimeout(function () {
                          scrollService.scrollBottom(msg.id)
                        }, 100)
                      } else {
                        //增加未读标识
                        chatHistory.setUnReadCount($scope.historys, msg.from.id, function (count) {
                          if (!count) {
                            count = 0
                          }
                          return count + 1;
                        });
                      }
                      $scope.$apply();
                    }

                  }
                });
                break;
              case 'chatGroup':
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
                    moveTop(groupChat, msg);
                    if (groupChat) {
                      if ($scope.currentChat && $scope.currentChat.id == groupChat.id) {
                        $scope.messages.push(msg);
                        chatHistory.chatGroupMessageReaded(msg.id, msg.to);
                        setTimeout(function () {
                          scrollService.scrollBottom(msg.id)
                        }, 100)
                      } else {
                        chatHistory.setUnReadCount($scope.historys, msg.to, function (count) {
                          return count + 1;
                        });
                      }
                      $scope.$apply();
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


      } else {
        eventBusService.closeEvent();
        $scope.messages = [];
        $scope.historys = [];
        $scope.loginUser = undefined;
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
      } else {
        $scope.chats = undefined;
        $scope.chatGroups = undefined;
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
      $scope.$parent.currentNav = 1;
      chatGroup.type = "chatGroup";
      $scope.$parent.continueChat({
        chatGroup: chatGroup
      })
    }

  })