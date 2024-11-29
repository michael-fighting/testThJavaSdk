package org.tianhe.thbc.sdk.demo.thbcmp.tool;

import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpCallback;
import org.tianhe.thbc.sdk.crypto.keystore.KeyTool;
import org.tianhe.thbc.sdk.crypto.keystore.P12KeyStore;
import org.tianhe.thbc.sdk.crypto.keystore.PEMKeyStore;

public class ThbcmpSubscriberPrivate {
    private static String subscriberConfigFile =
            ThbcmpSubscriberPrivate.class
                    .getClassLoader()
                    .getResource("amop/config-subscriber-for-test.toml")
                    .getPath();

    /**
     * @param args topic, privateKeyFile, password(Option)
     * @throws Exception AMOP exceptioned
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println(
                    "java -cp 'conf/:lib/*:apps/*' org.tianhe.thbc.sdk.demo.amop.tool.ThbcmpSubscriberPrivate <topicName> <privateKeyFile> <password>");
            return;
        }
        String topic = args[0];
        String privateKeyFile = args[1];
        ThbcSDK sdk = ThbcSDK.build(subscriberConfigFile);
        Thbcmp amop = sdk.getThbcmp();
        ThbcmpCallback cb = new DemoThbcmpCallback();

        System.out.println("Start test");
        amop.setCallback(cb);

        // Read a private key file
        KeyTool km;
        if (privateKeyFile.endsWith("p12")) {
            String password = args[2];
            km = new P12KeyStore(privateKeyFile, password);
        } else {
            km = new PEMKeyStore(privateKeyFile);
        }

        // Subscriber a private topic.
        amop.subscribePrivateTopics(topic, km, cb);
    }
}
