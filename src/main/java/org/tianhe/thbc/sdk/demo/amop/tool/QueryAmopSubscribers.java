package org.tianhe.thbc.sdk.demo.amop.tool;

import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.client.Client;
import org.tianhe.thbc.sdk.client.protocol.response.NodeInfo;
import org.tianhe.thbc.sdk.client.protocol.response.Peers;

public class QueryAmopSubscribers {
    private static final int parameterNum = 1;
    private static String publisherFile =
            QueryAmopSubscribers.class
                    .getClassLoader()
                    .getResource("amop/config-full.toml")
                    .getPath();

    /** @param args topic, ipAndPort */
    public static void main(String[] args) {
        if (args.length < parameterNum) {
            System.out.println("Param: ipAndPort");
            return;
        }
        String ipAndPort = args[0];
        ThbcSDK sdk = ThbcSDK.build(publisherFile);
        System.out.println("Query topic subscribers. Peer:" + ipAndPort);
        Client client = sdk.getClient(Integer.valueOf(1));
        NodeInfo nodeInfo = client.getNodeInfo(ipAndPort);
        Peers peers = client.getPeers();
        System.out.println(nodeInfo.getNodeInfo().toString());
        System.out.println(peers.getPeers().toString());
    }
}
