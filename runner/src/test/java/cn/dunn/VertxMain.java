package cn.dunn;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

/**
 * Created by Administrator on 2016/9/24.
 */
public class VertxMain {
  @Test
  public void testVertx1() throws InterruptedException {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 1);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181").namespace("io.vertx")
      .sessionTimeoutMs(100).connectionTimeoutMs(100).retryPolicy(retryPolicy).build();
    curatorFramework.start();
    ClusterManager mgr = new ZookeeperClusterManager(curatorFramework);
    VertxOptions options = new VertxOptions().setClusterManager(mgr);
    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        System.out.println("启动成功");
        vertx.eventBus().<String>consumer("hehe1", message -> {
          String body = message.body();
          System.out.println("接受到消费的消息" + body);
        });
      } else {
        System.out.println("失败");
      }
    });
    Thread.sleep(Integer.MAX_VALUE);
  }

  @Test
  public void testVertx2() throws InterruptedException {

    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 1000);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181").namespace("io.vertx")
      .sessionTimeoutMs(20000).connectionTimeoutMs(3000).retryPolicy(retryPolicy).build();
    curatorFramework.start();
    ClusterManager mgr = new ZookeeperClusterManager(curatorFramework);
    VertxOptions options = new VertxOptions().setClusterManager(mgr);

    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        System.out.println("启动成功!");
        Vertx vertx = res.result();
        vertx.eventBus().send("hehe1", "这是一个字符串");
      } else {
        System.out.println("失败");
      }
    });

    Thread.sleep(Integer.MAX_VALUE);
  }
}
