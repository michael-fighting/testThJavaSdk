package org.tianhe.thbc.sdk.demo.thbcmp.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.atomic.AtomicInteger;
import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.topic.TopicType;
import org.tianhe.thbc.sdk.utils.ThreadPoolService;

public class PerformanceThbcmp {
    private static final String senderConfig =
            PerformanceThbcmp.class
                    .getClassLoader()
                    .getResource("thbcmp/config-publisher-for-test.toml")
                    .getPath();
    private static final String subscriberConfig =
            PerformanceThbcmp.class
                    .getClassLoader()
                    .getResource("thbcmp/config-subscriber-for-test.toml")
                    .getPath();
    private static AtomicInteger sendedMsg = new AtomicInteger(0);
    private static ThbcmpMsgBuilder msgBuilder = new ThbcmpMsgBuilder();

    /** @param args count, qps, msgSize */
    public static void main(String[] args) {
        try {
            Integer count = Integer.valueOf(args[0]);
            Integer qps = Integer.valueOf(args[1]);
            Integer msgSize = Integer.valueOf(args[2]);

            // Init subscriber
            String topic = "normalTopic";
            Thbcmp subscriber = ThbcSDK.build(subscriberConfig).getThbcmp();
            ThbcmpMsgCallback cb = new ThbcmpMsgCallback();
            ThbcmpMsgCollector collector = cb.getCollector();
            collector.setTotal(count);
            subscriber.subscribeTopic(topic, cb);
            subscriber.setCallback(cb);

            // Init publisher
            Thbcmp sender = ThbcSDK.build(senderConfig).getThbcmp();

            System.out.println("Start test");
            Thread.sleep(2000);
            System.out.println("3s ...");
            Thread.sleep(1000);
            System.out.println("2s ...");
            Thread.sleep(1000);
            System.out.println("1s ...");
            Thread.sleep(1000);
            System.out.println(
                    "====== PerformanceThbcmp Thbcmp public topic text message performance start ======");
            RateLimiter limiter = RateLimiter.create(qps);
            Integer area = count / 10;
            final Integer total = count;
            ThreadPoolService threadPoolService = new ThreadPoolService("Performancethbcmp", 102400);

            for (Integer i = 0; i < count; i++) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        msgBuilder.sendMsg(
                                                collector,
                                                sender,
                                                "normalTopic",
                                                TopicType.NORMAL_TOPIC,
                                                msgSize);
                                        int current = sendedMsg.incrementAndGet();
                                        if (current >= area && ((current % area) == 0)) {
                                            System.out.println(
                                                    "Already sended: "
                                                            + current
                                                            + "/"
                                                            + total
                                                            + " thbcmp text message");
                                        }
                                    }
                                });
            }
            // wait to send all msg
            while (sendedMsg.get() != count) {
                Thread.sleep(1000);
            }
            threadPoolService.stop();
        } catch (Exception e) {
            System.out.println(
                    "====== PerformanceThbcmp test failed, error message: " + e.getMessage());
        }
    }
}
