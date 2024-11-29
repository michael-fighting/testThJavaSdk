package org.tianhe.thbc.sdk.demo.thbcmp.tool;

import java.net.URL;
import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpCallback;

public class ThbcmpSubscriber {

    public static void main(String[] args) throws Exception {
        URL configUrl =
                ThbcmpSubscriber.class
                        .getClassLoader()
                        .getResource("thbcmp/config-subscriber-for-test.toml");
        if (args.length < 1) {
            System.out.println("Param: topic");
            return;
        }
        String topic = args[0];
        // Construct a ThbcSDK instance
        ThbcSDK sdk = ThbcSDK.build(configUrl.getPath());

        // Get the thbcmp module instance
        Thbcmp thbcmp = sdk.getThbcmp();

        // Set callback
        ThbcmpCallback cb = new DemoThbcmpCallback();
        // Set a default callback
        thbcmp.setCallback(cb);
        // Subscriber a normal topic
        v.subscribeTopic(topic, cb);
        System.out.println("Start test");
    }
}
