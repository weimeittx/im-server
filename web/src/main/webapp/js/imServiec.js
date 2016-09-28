angular.module('imService', [])
  .filter('fromatShortContent', function () {
    return function (history, loginUser) {
      if (!history.message) {
        return "";
      }
      if (history.user) {
        return history.message.content.replace(/<\s?img[^>]*>/gi, ' 【图片】 ').replace(/<[^>]+>/g, "");
      } else {
        var result = undefined;
        if (history.message.from.id == loginUser.id) {
          result = history.message.content
        } else {
          result = history.message.from.nickname + ":" + history.message.content;
        }
        result = result.replace(/<\s?img[^>]*>/gi, ' 【图片】 ');
        return result.replace(/<[^>]+>/g, "");
      }
    }
  })
  .filter('fromatAsHtml', function ($sce) {
    return function (html) {
      return $sce.trustAsHtml(html);
    }
  })
  .filter('filterHtml', function () {
    return function (html) {
      return html.replace(/<[^>]+>/g, "");
    }
  })
  .service("userService", function () {
    this.user = undefined;
  })
  .service('eventBusService', function () {
      var onCloses = [];
      var onOpens = [];
      var eventBus = undefined;
      var isRun = false;
      this.start = function () {
        try {
          eventBus = new vertx.EventBus("http://localhost:8080/eventbus");
        } catch (e) {
          return;
        }
        isRun = true;
        eventBus.onopen = function () {
          console.log("启动推送服务成功")
          angular.forEach(onOpens, function (listener) {
            listener();
          })
        };
        eventBus.onclose = function () {
          console.log("服务关闭成功");
          angular.forEach(onCloses, function (listener) {
            listener();
          })
        }
      };
      this.addOpenListener = function (listener) {
        onOpens.push(listener)
      };
      this.removeOpenListener = function (listener) {
        onOpens.splice(onOpens.indexOf(listener), 1)
      };
      this.addCloseListener = function (listener) {
        onCloses.push(listener)
      };
      this.removeCloseListener = function (listener) {
        onCloses.splice(onCloses.indexOf(listener), 1)
      };

      this.registerHandler = function (address, f) {
        eventBus.registerHandler(address, f)
      };
      this.closeEvent = function () {
        if (eventBus) {
          onOpens = [];
          onCloses = [];

          eventBus.close();
        }
      }
    }
  )
  .service('inputService', function () {
      var ue = UE.getEditor('editor');

      //var ue = new UE.ui.Editor();
      //ue.render("textarea");


      this.getContent = function () {
        return ue.getContent();
      };
      this.setContent = function (message) {
        ue.setContent(message, false)
      };
      this.clearContent = function () {
        ue.setContent("", false)
      };
      //获取焦点
      this.setFocus = function () {
        ue.focus()
      };
    }
  )
  .service('chatHistory', function ($http, userService) {

    /**
     * 群消息已读
     * @param chatGroupId
     */
    this.chatGroupMessageReaded = function (messageId, chatGroupId) {
      $http({
        url: "/userChatHistory/chatGroupMessageReaded?id=" + chatGroupId + "&messageId=" + messageId
      }).success(function () {
        console.log("chatGroupMessageReaded is success")
      })
    };

    /**
     * 好友消息已读
     * @param userId
     */
    this.userMessageReaded = function (userId, messageId) {
      $http({
        url: "userChatHistory/userMessageReaded?id=" + userId + "&messageId=" + messageId
      }).success(function () {
        console.log("userMessageReaded is success")
      })
    }
    /**
     * 获取历史聊天对象
     * @param f
     */
    this.getHistory = function (f) {
      if (userService.user) {
        $http({
          url: '/userChatHistory/getUserChatHistory'
        }).success(f)
      }
    };

    /**
     * 删除数据库中的某条历史聊天记录
     * @param id
     */
    this.delDbHistory = function (id) {
      $http({
        url: "/userChatHistory/delHistory?id=" + id
      }).success(function (result) {
        if (result.success) {
          console.log("删除历史聊天成功")
        } else {
          console.log("删除历史聊天失败")
        }
      })
    };

    /**
     * 删除内存中的历史聊天记录
     * @param historys
     * @param id
     */
    this.delHistory = function (historys, id) {
      if (historys.length) {
        var deleteIndex = undefined;
        for (var index in historys) {
          var history = historys[index]
          var tempChatId = history.user ? history.user.id : history.chatGroup.id
          if (tempChatId == id) {
            deleteIndex = index;
          }
        }
        if (deleteIndex) {
          historys.splice(deleteIndex, 1)
        }
      }
    }
    /**
     * 将该会话移动到历史聊天最顶部
     * @param historys
     * @param userOrChatGroup
     * @param message
     */
    this.moveTopChat = function (historys, userOrChatGroup, message) {


      var delHistory = undefined;
      var firstHistory = {
        message: message
      };
      if (message.type == "chat") {
        firstHistory.user = userOrChatGroup;
      } else {
        firstHistory.chatGroup = userOrChatGroup;
      }
      for (var i = 0; i < historys.length; i++) {
        var tempHistory = historys[i];
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
        if ($scope.currentChat) {
          $scope.currentChat = userOrChatGroup;
        }

      }


      $scope.historys.splice(0, 0, firstHistory);

    };

    this.delHistory = function (id) {
      $http({
        url: "/userChatHistory/delHistory?id=" + id
      }).success(function (result) {
        if (result.success) {
          console.log("删除历史聊天成功")
        } else {
          console.log("删除历史聊天失败")
        }
      })
    }
    /**
     * 获取好友历史聊天消息
     */
    this.getChatHistoryMessage = function (history, lastTime, f) {
      $http({
        url: "/message/getChatHistoryMessage",
        data: {
          id: history.id,
          time: lastTime
        },
        method: "post"
      }).success(f)
    };
    /**
     * 获取聊天群组的历史消息
     * @param groupChat
     * @param lastTime
     * @param f
     */
    this.getChatGroupHistoryMessage = function (groupChat, lastTime, f) {
      $http({
        url: "/message/getChatGroupHistoryMessage",
        data: {
          id: groupChat.id,
          time: lastTime
        },
        method: "post"
      }).success(f)
    };


    /**
     * 移动空消息对话到顶部
     * @param chatId 会话ID userId or chatGroupID
     * @param type chat or chatGroup
     */
    this.moveEmptyMessageChatTop = function (chatId, type) {

      $http({
        url: "/message/moveEmptyMessageChatTop?chatId=" + chatId + "&type=" + type,
        method: "get"
      }).success(function () {
        console.log("success")
      })
    };

    this.setUnReadCount = function (historys, chat_id, f) {
      for (var index in historys) {
        var history = historys[index];
        var chatId = history.user ? history.user.id : history.chatGroup.id
        if (chatId == chat_id) {
          history.unReadCount = f(history.unReadCount);
          break;
        }
      }
    }
  })
  .service('chatService', function ($http, userService) {
    var cache = {};
    //获取好友
    this.getChat = function (f) {

      if (userService.user) {

        if (cache.getChat) {
          f(cache.getChat);
          return;
        }

        $http({
          url: "/user/getFriends",
          method: "post"
        }).success(function (result) {
          if (result.success) {
            cache.getChat = result;
          }
          f(result);
        })
      }
    };
    //删除指定缓存
    this.removeCache = function (key) {
      cache[key] = undefined
    }
    //删除所有缓存
    this.clearCache = function () {
      cache = {}
    }

  })
  .service('chatGroupService', function ($http, userService) {
    var cache = {};
    //删除指定缓存
    this.removeCache = function (key) {
      cache[key] = undefined
    }
    //删除所有缓存
    this.clearCache = function () {
      cache = {}
    }
    //获取聊天组
    this.getChatGroup = function (f) {
      if (userService.user) {
        if (cache.getChatGroup) {
          f(cache.getChatGroup);
          return;
        }
        $http({
          url: "/chatGroup/getChatGroup",
          method: "post"
        }).success(function (result) {
          if (result.success) {
            cache.getChatGroup = result;
          }
          f(result);
        })
      }
    }
    this.getChatGroupMembers = function (chatGroupId, f) {
      if (userService.user) {
        $http({
          url: "/chatGroup/getChatGroupMembers?id=" + chatGroupId
        }).success(f)
      }
    }
  })
  .service('messageService', function ($http, eventBusService) {
    this.sendMessage = function (message, f) {
      $http({
        url: "/message/sendMessage",
        method: "post",
        data: message
      }).success(f)
    }
  })
  .service("loginService", function ($http) {
    this.login = function (username, password, f) {
      $http({
        url: "/user/login",
        method: "post",
        data: {
          username: username,
          password: password
        }
      }).success(f)
    }
  }).service('utilService', function () {

  /**
   * 获取user或者chatGroup
   * @param chatHistory
   * @returns {*|undefined}
   */
  this.getUserOrChatGroup = function (chatHistory) {
    return chatHistory.user ? chatHistory.user : chatHistory.chatGroup;
  }

  this.UUID = function () {
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
      s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
  }
}).service('scrollService', function () {
  /**
   *滚动到底部
   * @param select
   */
  this.scrollBottom = function (messageId) {
    document.getElementById(messageId).scrollIntoView()
  };
});