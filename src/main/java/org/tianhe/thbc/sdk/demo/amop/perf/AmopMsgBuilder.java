package org.tianhe.thbc.sdk.demo.amop.perf;

import java.util.Random;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpMsgOut;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpResponse;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpResponseCallback;
import org.tianhe.thbc.sdk.thbcmp.topic.TopicType;

public class AmopMsgBuilder {

    public void sendMsg(
            AmopMsgCollector collector, Amop sender, String topic, TopicType type, int contentLen) {
        AmopMsgOut out = new AmopMsgOut();
        out.setTopic(topic);
        out.setType(type);
        out.setTimeout(5000);
        out.setContent(getRandomBytes(contentLen));

        AmopResponseCallback callback =
                new AmopResponseCallback() {
                    @Override
                    public void onResponse(AmopResponse response) {
                        collector.addResponse();
                        if (response.getErrorCode() != 0) {
                            System.out.println(
                                    "error, code:"
                                            + response.getErrorCode()
                                            + " msg:"
                                            + response.getErrorMessage());
                            collector.addError();
                        }
                    }
                };
        sender.sendAmopMsg(out, callback);
    }

    public byte[] getRandomBytes(int len) {
        Random random = new Random();
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            Integer is = random.nextInt(9);
            b[i] = Byte.parseByte(is.toString());
        }
        return b;
    }
}
