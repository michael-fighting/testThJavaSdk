package org.tianhe.thbc.sdk.demo.amop.tool;

import java.net.URL;
import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.thbcmp.Thbcmp;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpCallback;

public class AmopSubscriber {

    public static void main(String[] args) throws Exception {
        URL configUrl =
                AmopSubscriber.class
                        .getClassLoader()
                        .getResource("amop/config-subscriber-for-test.toml");
        if (args.length < 1) {
            System.out.println("Param: topic");
            return;
        }
        String topic = args[0];
        // Construct a ThbcSDK instance
        ThbcSDK sdk = ThbcSDK.build(configUrl.getPath());

        // Get the amop module instance
        Thbcmp amop = sdk.getThbcmp();

        // Set callback
        ThbcmpCallback cb = new DemoAmopCallback();
        // Set a default callback
        amop.setCallback(cb);
        // Subscriber a normal topic
        amop.subscribeTopic(topic, cb);
        System.out.println("Start test");
    }
}
