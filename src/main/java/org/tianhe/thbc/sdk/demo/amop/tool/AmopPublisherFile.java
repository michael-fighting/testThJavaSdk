package org.tianhe.thbc.sdk.demo.amop.tool;

import static org.tianhe.thbc.sdk.demo.amop.tool.FileToByteArrayHelper.byteCat;
import static org.tianhe.thbc.sdk.demo.amop.tool.FileToByteArrayHelper.getFileByteArray;
import static org.tianhe.thbc.sdk.demo.amop.tool.FileToByteArrayHelper.intToByteArray;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpMsgOut;
import org.tianhe.thbc.sdk.thbcmp.topic.TopicType;
import org.tianhe.thbc.sdk.client.Client;
import org.tianhe.thbc.sdk.client.protocol.response.Peers;

public class AmopPublisherFile {
    private static final int parameterNum = 4;
    private static String publisherFile =
            AmopPublisherFile.class
                    .getClassLoader()
                    .getResource("amop/config-publisher-for-test.toml")
                    .getPath();

    /**
     * @param args topicName, isBroadcast: true/false, fileName, count, ipAndPort(one of the node
     *     you connected)
     * @throws Exception AMOP publish exceptioned
     */
    public static void main(String[] args) throws Exception {
        if (args.length < parameterNum) {
            System.out.println("param: target topic total number of request");
            return;
        }
        String topicName = args[0];
        Boolean isBroadcast = Boolean.valueOf(args[1]);
        String fileName = args[2];
        Integer count = Integer.parseInt(args[3]);
        ThbcSDK sdk = ThbcSDK.build(publisherFile);
        Amop amop = sdk.getAmop();
        Integer timeout = 6000;
        if (args.length > 4) {
            timeout = Integer.parseInt(args[4]);
        }
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

        int flag = -128;
        byte[] byteflag = intToByteArray(flag);
        int filelength = fileName.length();
        byte[] bytelength = intToByteArray(filelength);
        byte[] bytefilename = fileName.getBytes();
        byte[] contentfile = getFileByteArray(new File(fileName));
        byte[] content = byteCat(byteCat(byteCat(byteflag, bytelength), bytefilename), contentfile);

        for (Integer i = 0; i < count; ++i) {
            Thread.sleep(2000);
            AmopMsgOut out = new AmopMsgOut();
            out.setType(TopicType.NORMAL_TOPIC);
            out.setContent(content);
            out.setTimeout(timeout);
            out.setTopic(topicName);
            DemoAmopResponseCallback cb = new DemoAmopResponseCallback();
            if (isBroadcast) {
                amop.broadcastAmopMsg(out);
            } else {
                amop.sendAmopMsg(out, cb);
            }
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println(
                    "Step 1: Send out msg, time: "
                            + df.format(LocalDateTime.now())
                            + " topic:"
                            + out.getTopic()
                            + " content: file "
                            + fileName);
        }
    }

    public static boolean subscribed(ThbcSDK sdk, String topicName) throws InterruptedException {
        Client client = sdk.getClient(Integer.valueOf(1));
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
