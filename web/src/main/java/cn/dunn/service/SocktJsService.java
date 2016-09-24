package cn.dunn.service;

import akka.actor.ActorRef;
import cn.dunn.front.Script;
import cn.dunn.front.impl.ScriptImpl;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SocktJsService implements InitializingBean {
  @Resource
  private Vertx vertx;

  public Script start(Vertx vertx, String eventBusPath, int port, ActorRef actorRef) {
    Router router = Router.router(vertx);
    BridgeOptions opts = new BridgeOptions()
      .addInboundPermitted(new PermittedOptions())
      .addOutboundPermitted(new PermittedOptions());

    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
    router.route(eventBusPath).handler(ebHandler);

    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router::accept).listen(port);

    EventBus eb = vertx.eventBus();
//
//        eb.<JsonObject>consumer(ChatMessage.class.getName()).handler(message -> {
//            JsonObject body = message.body();
//            actorRef.tell(ChatMessage.apply(body.getString("from"), body.getString("to"), body.getString("content"), new Date(), Source.WEB()), ActorRef.noSender());
//        });
//        eb.<JsonObject>consumer(GroupMessage.class.getName()).handler(message -> {
//            JsonObject body = message.body();
//            actorRef.tell(GroupMessage.apply(body.getString("from"), body.getString("to"), body.getString("content"), new Date(), Source.WEB(), null), ActorRef.noSender());
//        });
    return new ScriptImpl(eb);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    start(vertx,"/eventbus/*",8080,null);
  }
}
