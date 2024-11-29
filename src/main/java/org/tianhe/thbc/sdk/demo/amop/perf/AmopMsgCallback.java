package org.tianhe.thbc.sdk.demo.amop.perf;

import org.tianhe.thbc.sdk.thbcmp.ThbcmpCallback;
import org.tianhe.thbc.sdk.thbcmp.topic.ThbcmpMsgIn;

public class AmopMsgCallback extends AmopCallback {
    private Long startTime = System.currentTimeMillis();

    private AmopMsgCollector collector = new AmopMsgCollector();

    public AmopMsgCollector getCollector() {
        return collector;
    }

    @Override
    public byte[] receiveAmopMsg(AmopMsgIn msg) {
        Long cost = System.currentTimeMillis() - startTime;
        collector.onSubscribedTopicMsg(msg, cost);
        return "Yes, I received!".getBytes();
    }
}
