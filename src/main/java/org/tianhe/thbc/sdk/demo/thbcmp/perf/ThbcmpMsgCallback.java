package org.tianhe.thbc.sdk.demo.thbcmp.perf;

import org.tianhe.thbc.sdk.thbcmp.ThbcmpCallback;
import org.tianhe.thbc.sdk.thbcmp.topic.ThbcmpMsgIn;

public class ThbcmpMsgCallback extends ThbcmpCallback {
    private Long startTime = System.currentTimeMillis();

    private ThbcmpMsgCollector collector = new ThbcmpMsgCollector();

    public ThbcmpMsgCollector getCollector() {
        return collector;
    }

    @Override
    public byte[] receiveAmopMsg(ThbcmpMsgIn msg) {
        Long cost = System.currentTimeMillis() - startTime;
        collector.onSubscribedTopicMsg(msg, cost);
        return "Yes, I received!".getBytes();
    }
}
