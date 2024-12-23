package org.tianhe.thbc.sdk.demo.thbcmp.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.client.Client;
import org.tianhe.thbc.sdk.client.protocol.response.Peers;
import org.tianhe.thbc.sdk.crypto.keypair.ECDSAKeyPair;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpCallback;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpMsgOut;
import org.tianhe.thbc.sdk.thbcmp.topic.TopicType;

public class ThbcmpSubscriberPrivateByKey {
    private static String subscriberConfigFile =
            ThbcmpSubscriberPrivate.class
                    .getClassLoader()
                    .getResource("thbcmp/config-subscriber-for-test.toml")
                    .getPath();

    private static String publisherFile =
            ThbcmpPublisherPrivate.class
                    .getClassLoader()
                    .getResource("thbcmp/config-publisher-for-test.toml")
                    .getPath();

    public static void Usage() {
        System.out.println(
                "java -cp 'conf/:lib/*:apps/*' org.tianhe.thbc.sdk.demo.thbcmp.tool.ThbcmpSubscriberPrivateByKey generateKeyFile keyFileName");
        System.out.println(
                "java -cp 'conf/:lib/*:apps/*' org.tianhe.thbc.sdk.demo.thbcmp.tool.ThbcmpSubscriberPrivateByKey subscribe topicName privateKeyFile");
        System.out.println(
                "java -cp 'conf/:lib/*:apps/*' org.tianhe.thbc.sdk.demo.thbcmp.tool.ThbcmpSubscriberPrivateByKey publish [topicName] [isBroadcast: true/false] [sendedContent] [count] [publicKeyFile1] [publicKeyFile2] ...");
        System.out.println();
        System.exit(0);
    }
    /**
     * @param args topic, privateKeyFile, password(Option)
     * @throws Exception THBCMP exceptioned
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            Usage();
        }
        String command = args[0];
        switch (command) {
            case "generateKeyFile":
                generateKeyFile(args);
                break;
            case "subscribe":
                subscribePrivateTopic(args);
                break;
            case "publish":
                publishPrivateTopics(args);
                break;
            default:
                System.out.println("Unknown command " + command);
                Usage();
        }
    }

    public static void generateKeyFile(String[] args) throws IOException {
        ECDSAKeyPair ecdsaKeyPair = new ECDSAKeyPair();
        String privateKey = ecdsaKeyPair.getHexPrivateKey();
        String publicKey = ecdsaKeyPair.getHexPublicKey();
        String fileName = args[1];
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        out.write(privateKey);
        out.close();
        System.out.println("Generate And save privateKey Success: " + privateKey);

        String publicKeyFileName = fileName + ".pub";
        BufferedWriter publicKeyOut = new BufferedWriter(new FileWriter(publicKeyFileName));
        publicKeyOut.write(publicKey);
        publicKeyOut.close();

        System.out.println("Generate And save publicKey Success: " + publicKey);
    }

    public static void subscribePrivateTopic(String[] args) throws IOException {
        if (args.length < 3) {
            Usage();
        }
        String topic = args[1];
        String hexPrivateKey = readContent(args[2]);
        if (hexPrivateKey == null) {
            return;
        }
        ThbcSDK sdk = ThbcSDK.build(subscriberConfigFile);
        Thbcmp thbcmp = sdk.getThbcmp();
        ThbcmpCallback cb = new DemoThbcmpCallback();

        System.out.println("Start test");
        thbcmp.setCallback(cb);
        // Subscriber a private topic.
        thbcmp.subscribePrivateTopics(topic, hexPrivateKey, cb);
    }

    public static String readContent(String filePath) throws IOException {
        File keyFile = new File(filePath);
        if (!keyFile.exists()) {
            System.out.println("The file " + filePath + " doesn't exist!");
            return null;
        }
        return FileUtils.readFileToString(keyFile);
    }

    public static void publishPrivateTopics(String[] args)
            throws InterruptedException, IOException {
        if (args.length < 6) {
            Usage();
        }
        String topicName = args[1];
        Boolean isBroadcast = Boolean.valueOf(args[2]);
        String content = args[3];
        Integer count = Integer.parseInt(args[4]);
        List<String> publicKeyList = new ArrayList<>();
        for (int i = 5; i < args.length; i++) {
            String publicKey = readContent(args[i]);
            if (publicKey == null) {
                System.out.println("public key file " + args[i] + " doesn't exist.");
                continue;
            }
            publicKeyList.add(publicKey);
            System.out.println("Load public key file " + args[i] + " success.");
        }
        if (publicKeyList.size() == 0) {
            System.out.println(
                    "The number of the public keys are 0, Please add at least one public key file");
            return;
        }
        ThbcSDK sdk = ThbcSDK.build(publisherFile);
        Thbcmp thbcmp = sdk.getThbcmp();
        if (!subscribed(sdk, topicName)) {
            System.out.println("No subscriber, exist.");
        }

        System.out.println("start test");
        System.out.println("===================================================================");
        System.out.println("set up private topic");
        // Publish a private topic
        thbcmp.publishPrivateTopicWithHexPublicKeyList(topicName, publicKeyList);
        System.out.println("wait until finish private topic verify");
        System.out.println("3s ...");
        Thread.sleep(1000);
        System.out.println("2s ...");
        Thread.sleep(1000);
        System.out.println("1s ...");
        Thread.sleep(1000);

        for (Integer i = 0; i < count; ++i) {
            Thread.sleep(2000);
            ThbcmpMsgOut out = new ThbcmpMsgOut();
            // It is a private topic.
            out.setType(TopicType.PRIVATE_TOPIC);
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
