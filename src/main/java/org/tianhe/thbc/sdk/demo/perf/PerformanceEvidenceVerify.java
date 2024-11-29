/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tianhe.thbc.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.tianhe.thbc.sdk.ThbcSDK;
import org.tianhe.thbc.sdk.ThbcSDKException;
import org.tianhe.thbc.sdk.client.Client;
import org.tianhe.thbc.sdk.crypto.CryptoSuite;
import org.tianhe.thbc.sdk.crypto.keypair.CryptoKeyPair;
import org.tianhe.thbc.sdk.crypto.signature.ECDSASignatureResult;
import org.tianhe.thbc.sdk.demo.contract.EvidenceVerify;
import org.tianhe.thbc.sdk.demo.perf.callback.PerformanceCallback;
import org.tianhe.thbc.sdk.demo.perf.collector.PerformanceCollector;
import org.tianhe.thbc.sdk.model.ConstantConfig;
import org.tianhe.thbc.sdk.model.CryptoType;
import org.tianhe.thbc.sdk.model.TransactionReceipt;
import org.tianhe.thbc.sdk.transaction.model.exception.ContractException;
import org.tianhe.thbc.sdk.utils.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceEvidenceVerify {
    private static Logger logger = LoggerFactory.getLogger(PerformanceEvidenceVerify.class);
    private static AtomicInteger sendedTransactions = new AtomicInteger(0);

    private static void Usage() {
        System.out.println(" Usage:");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.tianhe.thbc.sdk.demo.perf.PerformanceEvidenceVerify [count] [tps] [groupId].");
    }

    public static void main(String[] args) {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl =
                    PerformanceEvidenceVerify.class.getClassLoader().getResource(configFileName);

            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 3) {
                Usage();
                return;
            }
            Integer count = Integer.valueOf(args[0]);
            Integer qps = Integer.valueOf(args[1]);
            Integer groupId = Integer.valueOf(args[2]);
            System.out.println(
                    "====== PerformanceEvidenceVerify trans, count: "
                            + count
                            + ", qps:"
                            + qps
                            + ", groupId: "
                            + groupId);

            String configFile = configUrl.getPath();
            ThbcSDK sdk = ThbcSDK.build(configFile);

            // build the client
            Client client = sdk.getClient(groupId);

            // deploy the HelloWorld
            System.out.println("====== Deploy EvidenceVerify ====== ");
            EvidenceVerify evidenceVerify =
                    EvidenceVerify.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
            System.out.println(
                    "====== Deploy EvidenceVerify success, address: "
                            + evidenceVerify.getContractAddress()
                            + " ====== ");

            PerformanceCollector collector = new PerformanceCollector();
            collector.setTotal(count);
            RateLimiter limiter = RateLimiter.create(qps);
            Integer area = count / 10;
            final Integer total = count;

            System.out.println("====== PerformanceEvidenceVerify trans start ======");

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceEvidenceVerify",
                            sdk.getConfig().getThreadPoolConfig().getMaxBlockingQueueSize());

            CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
            for (Integer i = 0; i < count; ++i) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        PerformanceCallback callback = new PerformanceCallback();
                                        callback.setTimeout(0);
                                        callback.setCollector(collector);
                                        try {
                                            String evi = "test";
                                            String evInfo = "test_info";
                                            int random = new SecureRandom().nextInt(50000);
                                            String eviId = String.valueOf(random);
                                            // sign to evi
                                            byte[] message = ecdsaCryptoSuite.hash(evi.getBytes());
                                            CryptoKeyPair cryptoKeyPair =
                                                    ecdsaCryptoSuite.createKeyPair();
                                            // sign with secp256k1
                                            ECDSASignatureResult signatureResult =
                                                    (ECDSASignatureResult)
                                                            ecdsaCryptoSuite.sign(
                                                                    message, cryptoKeyPair);
                                            String signAddr = cryptoKeyPair.getAddress();
                                            evidenceVerify.insertEvidence(
                                                    evi,
                                                    evInfo,
                                                    eviId,
                                                    signAddr,
                                                    message,
                                                    BigInteger.valueOf(signatureResult.getV() + 27),
                                                    signatureResult.getR(),
                                                    signatureResult.getS(),
                                                    callback);
                                        } catch (Exception e) {
                                            TransactionReceipt receipt = new TransactionReceipt();
                                            receipt.setStatus("-1");
                                            callback.onResponse(receipt);
                                            logger.info(e.getMessage());
                                        }
                                        int current = sendedTransactions.incrementAndGet();
                                        if (current >= area && ((current % area) == 0)) {
                                            System.out.println(
                                                    "Already send: "
                                                            + current
                                                            + "/"
                                                            + total
                                                            + " transactions");
                                        }
                                    }
                                });
            }
            // wait to collect all the receipts
            while (!collector.getReceived().equals(count)) {
                Thread.sleep(1000);
            }
            threadPoolService.stop();
            System.exit(0);
        } catch (ThbcSDKException | ContractException | InterruptedException e) {
            System.out.println(
                    "====== PerformanceEvidenceVerify test failed, error message: "
                            + e.getMessage());
            System.exit(0);
        }
    }
}
