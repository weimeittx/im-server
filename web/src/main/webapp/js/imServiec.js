angular.module('imService', [])
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
      this.init = function () {
        try {
          eventBus = new vertx.EventBus("http://localhost:8080/eventbus");
        } catch (e) {
          return;
        }
        isRun = true;
        eventBus.onopen = function () {
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
          url: '/IM/web/src/main/webapp/data/chatHistory.json'
        }).success(f)
      }
    };
    /**
     * 获取历史聊天消息
     */
    this.getHistoryMessage = function (history, f) {
      $http({
        url: "/IM/web/src/main/webapp/data/historyMessage.json",
        data: {
          to: history.id,
          type: history.type
          //chatName
        }
      }).success(f)
    }
  })
  .service('chatService', function ($http, userService) {
    this.getChat = function (f) {
      if (userService.user) {
        $http({
          url: "/IM/web/src/main/webapp/data/contactChat.json",
          method: "post"
        }).success(f)
      }
    };

  })
  .service('chatGroupService', function ($http, userService) {
    this.getChatGroup = function (f) {
      if (userService.user) {
        $http({
          url: "/IM/web/src/main/webapp/data/contactChatGroup.json",
          method: "post"
        }).success(f)
      }
    }
    this.getChatGroupMembers = function (chatGroupId, f) {
      if (userService.user) {
        $http({
          url: "/IM/web/src/main/webapp/data/chatGroupMembers.json",
          data: {
            id: chatGroupId
          }
        }).success(f)
      }
    }
  })
  .service("loginService", function ($http) {
    this.login = function (username, password, f) {
      $http({
        url: "/IM/web/src/main/webapp/data/login.json",
        method: "post",
        data: {
          username: username,
          password: password
        }
      }).success(f)
    }
  });