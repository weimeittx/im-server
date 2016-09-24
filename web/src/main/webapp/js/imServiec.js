angular.module('imService', [])
  .filter('fromartShortContent', function () {
    return function (history, loginUser) {
      if (!history.message) {
        return "";
      }
      if (history.user) {
        return history.message.content;
      } else {
        var result = undefined;
        if (history.message.from.id == loginUser.id) {
          result = history.message.content
        } else {
          result = history.message.from.nickname + ":" + history.message.content;
        }
        return result
      }
    }
  })
  .service("userService", function () {
    this.user = undefined;
  })
  .service('eventBusService', function () {
      //服务必须未开启
      var mustUnRun = function () {
        if (isRun) {
          throw "服务已经开启."
        }
      };
      //服务必须开启
      var mustRun = function () {
        if (!isRun) {
          throw "服务未开启."
        }
      }
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
          angular.forEach(onCloses, function (listener) {
            listener();
          })
        }
      };
      this.addOpenListener = function (listener) {
        mustUnRun();
        onOpens.push(listener)
      };
      this.removeOpenListener = function (listener) {
        mustUnRun();
        onOpens.splice(onOpens.indexOf(listener), 1)
      };
      this.addCloseListener = function (listener) {
        mustUnRun();
        onCloses.push(listener)
      };
      this.removeCloseListener = function (listener) {
        mustUnRun();
        onCloses.splice(onCloses.indexOf(listener), 1)
      };

      this.registerHandler = function (address, f) {
        mustRun()
        eventBus.registerHandler(address, f)
      }
    }
  )
  .service('inputService', function () {
      var ue = UE.getEditor('editor');
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
     * 获取历史聊天消息
     */
    this.getHistoryMessage = function (history,lastTime, f) {
      console.log(history)
      $http({
        url: "/message/getChatHistoryMessage",
        data: {
          id: history.id,
          time: lastTime
        },
        method:"post"
      }).success(f)
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
          cache.getChat = result;
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
    //获取聊天组
    this.getChatGroup = function (f) {
      if (userService.user) {
        $http({
          url: "/chatGroup/getChatGroup",
          method: "post"
        }).success(f)
      }
    }
    this.getChatGroupMembers = function (chatGroupId, f) {
      if (userService.user) {
        $http({
          url: "/im-server/web/src/main/webapp/data/chatGroupMembers.json",
          data: {
            id: chatGroupId
          }
        }).success(f)
      }
    }
  })
  .service('messageService', function ($http, eventBusService) {
    this.sendChatMessage = function (message, f) {
      $http({
        url: "/message/sendMessage",
        method: "post",
        data: message
      }).success(f)
    }
    this.sendChatGroupMessage = function (message) {
      $http({
        url: ""
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
  });