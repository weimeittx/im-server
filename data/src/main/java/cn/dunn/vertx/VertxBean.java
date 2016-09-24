package cn.dunn.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class VertxBean implements FactoryBean<Vertx> {
  @Value("${zk.url}")
  private String zkUrl;
  @Value("${vertx.namespace}")
  private String namespace;

  private class Result {
    Vertx vertx = null;
  }

  @Override
  public Vertx getObject() throws Exception {
    Result result = new Result();
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 1);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString(zkUrl).namespace(namespace).sessionTimeoutMs(100).connectionTimeoutMs(100)
      .retryPolicy(retryPolicy).build();
    curatorFramework.start();
    ClusterManager mgr = new ZookeeperClusterManager(curatorFramework);
    VertxOptions options = new VertxOptions().setClusterManager(mgr);
    Vertx.clusteredVertx(options, res -> {
      lock.lock();
      try {
        if (res.succeeded()) {
          result.vertx = res.result();
        } else {
          res.cause().printStackTrace();
        }
        condition.signalAll();
      } finally {
        lock.unlock();
      }
    });
    lock.lock();
    try {
      while (result.vertx == null) {
        condition.await();
      }
    } finally {
      lock.unlock();
    }
    return result.vertx;
  }

  @Override
  public Class<?> getObjectType() {
    return Vertx.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
