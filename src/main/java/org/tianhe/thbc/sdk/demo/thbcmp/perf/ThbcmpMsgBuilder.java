package org.tianhe.thbc.sdk.demo.thbcmp.perf;

import java.util.Random;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpMsgOut;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpResponse;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpResponseCallback;
import org.tianhe.thbc.sdk.thbcmp.topic.TopicType;

public class ThbcmpMsgBuilder {

    public void sendMsg(
            ThbcmpMsgCollector collector,
            Thbcmp sender,
            String topic,
            TopicType type,
            int contentLen) {
        ThbcmpMsgOut out = new ThbcmpMsgOut();
        out.setTopic(topic);
        out.setType(type);
        out.setTimeout(5000);
        out.setContent(getRandomBytes(contentLen));

        ThbcmpResponseCallback callback =
                new ThbcmpResponseCallback() {
                    @Override
                    public void onResponse(ThbcmpResponse response) {
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
        sender.sendThbcmpMsg(out, callback);
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
