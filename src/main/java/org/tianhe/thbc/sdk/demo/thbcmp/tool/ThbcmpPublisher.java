package org.tianhe.thbc.sdk.demo.thbcmp.tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.client.Client;
import org.tianhe.thbc.sdk.client.protocol.response.Peers;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpMsgOut;
import org.tianhe.thbc.sdk.thbcmp.topic.TopicType;

public class ThbcmpPublisher {
    private static final int parameterNum = 4;
    private static String publisherFile =
            ThbcmpPublisher.class
                    .getClassLoader()
                    .getResource("thbcmp/config-publisher-for-test.toml")
                    .getPath();

    /**
     * @param args topicName, isBroadcast, Content(Content you want to send out), Count(how many msg
     *     you want to send out)
     * @throws Exception THBCMP publish exceptioned
     */
    public static void main(String[] args) throws Exception {
        if (args.length < parameterNum) {
            System.out.println("param: target topic total number of request");
            return;
        }
        String topicName = args[0];
        Boolean isBroadcast = Boolean.valueOf(args[1]);
        String content = args[2];
        Integer count = Integer.parseInt(args[3]);
        ThbcSDK sdk = ThbcSDK.build(publisherFile);
        Thbcmp thbcmp = sdk.getThbcmp();

        System.out.println("3s ...");
        Thread.sleep(1000);
        System.out.println("2s ...");
        Thread.sleep(1000);
        System.out.println("1s ...");
        Thread.sleep(1000);

        if (!subscribed(sdk, topicName)) {
            System.out.println("No subscriber, exist.");
        }

        System.out.println("start test");
        System.out.println("===================================================================");

        for (Integer i = 0; i < count; ++i) {
            Thread.sleep(2000);
            ThbcmpMsgOut out = new ThbcmpMsgOut();
            out.setType(TopicType.NORMAL_TOPIC);
            out.setContent(content.getBytes());
            out.setTimeout(6000);
            out.setTopic(topicName);
            DemoThbcmpResponseCallback cb = new DemoThbcmpResponseCallback();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (isBroadcast) {
                thbcmp.broadcastThbcmpMsg(out);
                System.out.println(
                        "Step 1: Send out msg by broadcast,  time: "
                                + df.format(LocalDateTime.now())
                                + " topic:"
                                + out.getTopic()
                                + " content:"
                                + new String(out.getContent()));
            } else {
                thbcmp.sendThbcmpMsg(out, cb);
                System.out.println(
                        "Step 1: Send out msg,  time: "
                                + df.format(LocalDateTime.now())
                                + " topic:"
                                + out.getTopic()
                                + " content:"
                                + new String(out.getContent()));
            }
        }
    }

    public static boolean subscribed(ThbcSDK sdk, String topicName) throws InterruptedException {
        Client client = sdk.getClient(Integer.valueOf(287));
        Boolean hasSubscriber = false;
        Peers peers = client.getPeers();
        for (int i = 0; i < 10; i++) {
            for (Peers.PeerInfo info : peers.getPeers()) {
                for (String tp : info.getTopic()) {
                    if (tp.equals(topicName)) {
                        hasSubscriber = true;
                        return hasSubscriber;
                    }
                }
            }
            if (!hasSubscriber) {
                Thread.sleep(2000);
            }
        }
        return false;
    }
}
