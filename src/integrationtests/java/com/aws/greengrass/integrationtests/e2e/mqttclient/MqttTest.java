/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.integrationtests.e2e.mqttclient;

import com.aws.greengrass.deployment.exceptions.DeviceConfigurationException;
import com.aws.greengrass.integrationtests.e2e.BaseE2ETestCase;
import com.aws.greengrass.lifecyclemanager.Kernel;
import com.aws.greengrass.mqttclient.MqttClient;
import com.aws.greengrass.mqttclient.PublishRequest;
import com.aws.greengrass.mqttclient.SubscribeRequest;
import com.aws.greengrass.testcommons.testutilities.GGExtension;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.crt.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.CloseResource")
@ExtendWith(GGExtension.class)
@Tag("E2E")
class MqttTest extends BaseE2ETestCase {
    public static final int NUM_MESSAGES = 50;
    private Kernel kernel;

    protected MqttTest() throws Exception {
        super();
    }
    static {
        Log.initLoggingToFile(Log.LogLevel.Trace, "/tmp/crt-logs.log");
    }
    @AfterEach
    void afterEach() {
        try {
            kernel.shutdown();
        } finally {
            cleanup();
        }
    }

    @Test
    void GIVEN_mqttclient_WHEN_subscribe_and_publish_THEN_receives_all_messages()
            throws IOException, ExecutionException, InterruptedException, TimeoutException, DeviceConfigurationException {
        kernel = new Kernel().parseArgs("-r", tempRootDir.toAbsolutePath().toString());
        setDefaultRunWithUser(kernel);
        FileOutputStream fos1 = new FileOutputStream("/tmp/generated.p8");
        fos1.write(thingInfo.getKeyPair().privateKey().getBytes());
        fos1.flush();
        fos1.close();
        FileOutputStream fos2 = new FileOutputStream("/tmp/generated_pub.p8");
        fos2.write(thingInfo.getKeyPair().publicKey().getBytes());
        fos2.flush();
        fos2.close();

        Process proc1 = Runtime.getRuntime().exec("openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in /tmp/generated.p8 -out /tmp/pkcs8.key");
        Process proc2 = Runtime.getRuntime().exec("softhsm2-util --init-token --so-pin 0000 --pin 0000 --slot 0 --label test");
        Thread.sleep(1000L);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc2.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc2.getErrorStream()));

        // Read the output from the command
        System.err.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.err.println(s);
        }
        System.err.println("Here is the standard output of the command:\n");
        while ((s = stdError.readLine()) != null) {
            System.err.println(s);
        }
/*
        Process proc3 = Runtime.getRuntime().exec("softhsm2-util --init-token --so-pin 0000 --pin 0000 --slot 1 --label test1");
        Thread.sleep(1000L);

         stdInput = new BufferedReader(new
                InputStreamReader(proc3.getInputStream()));

         stdError = new BufferedReader(new
                InputStreamReader(proc3.getErrorStream()));
*/
        // Read the output from the command
        System.err.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.err.println(s);
        }

        System.err.println("Here is the standard output of the command:\n");
        while ((s = stdError.readLine()) != null) {
            System.err.println(s);
        }

        Process proc4 = Runtime.getRuntime().exec("softhsm2-util --import /tmp/pkcs8.key --slot 0 --label rsa-privkey --id 0000 --pin 0000");
        Thread.sleep(1000L);

        stdInput = new BufferedReader(new
                InputStreamReader(proc4.getInputStream()));

        stdError = new BufferedReader(new
                InputStreamReader(proc4.getErrorStream()));

        // Read the output from the command
        System.err.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.err.println(s);
        }

        System.err.println("Here is the standard output of the command:\n");
        while ((s = stdError.readLine()) != null) {
            System.err.println(s);
        }
        /*
        Process proc5 = Runtime.getRuntime().exec("softhsm2-util --import /tmp/pkcs8.key --slot 1 --label rsa-privkey --id 0001 --pin 0000");

        stdInput = new BufferedReader(new
                InputStreamReader(proc5.getInputStream()));

        stdError = new BufferedReader(new
                InputStreamReader(proc5.getErrorStream()));
*/
        // Read the output from the command
        System.err.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.err.println(s);
        }

        System.err.println("Here is the standard output of the command:\n");
        while ((s = stdError.readLine()) != null) {
            System.err.println(s);
        }

        Process proc = Runtime.getRuntime().exec("softhsm2-util --show-slots");
         stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

         stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        System.err.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.err.println(s);
        }


        deviceProvisioningHelper.updateKernelConfigWithIotConfiguration(kernel, thingInfo, GAMMA_REGION.toString(),
                TES_ROLE_ALIAS_NAME);

        MqttClient client = kernel.getContext().get(MqttClient.class);
        CountDownLatch cdl = new CountDownLatch(NUM_MESSAGES);
        client.subscribe(SubscribeRequest.builder().topic("A/B/C").callback((m) -> {
            cdl.countDown();
            logger.atInfo().kv("remaining", cdl.getCount()).log("Received 1 message from cloud.");
        }).build());

        for (int i = 0; i < NUM_MESSAGES; i++) {
            client.publish(PublishRequest.builder().topic("A/B/C").payload("What's up".getBytes(StandardCharsets.UTF_8))
                    .build()).get(5, TimeUnit.SECONDS);
            logger.atInfo().kv("total", i + 1).log("Added 1 message to spooler.");
        }
        assertTrue(cdl.await(1, TimeUnit.MINUTES), "All messages published and received");
    }

    @Test
    void GIVEN_mqttclient_WHEN_closes_new_connection_is_created_THEN_previous_session_is_invalidated()
            throws Throwable {
        kernel = new Kernel().parseArgs("-r", tempRootDir.toAbsolutePath().toString());
        setDefaultRunWithUser(kernel);
        deviceProvisioningHelper.updateKernelConfigWithIotConfiguration(kernel, thingInfo, GAMMA_REGION.toString(),
                TES_ROLE_ALIAS_NAME);

        MqttClient client = kernel.getContext().get(MqttClient.class);

        //subscribe to 50 topics using first connection.
        int numberOfTopics = 50;
        for (int i = 0; i < numberOfTopics; i++) {
            client.subscribe(SubscribeRequest.builder().topic("A/"+ i).callback((m) -> {}).build());
        }
        //close the first connections and create a second connection.
        client.close();
        client = kernel.getContext().newInstance(MqttClient.class);
        CountDownLatch cdl = new CountDownLatch(numberOfTopics);
        // Using the second connection to subscribes to another 50 topics, IoT core limits subscriptions to 50 topics per connection.
        // if the session from first connection is not terminated, subscribe operations made by second connection will not succeed.
        for (int i = 0; i < numberOfTopics; i++) {
            client.subscribe(SubscribeRequest.builder().topic("B/"+ i ).callback((m) -> {
                cdl.countDown();
                logger.atInfo().kv("remaining", cdl.getCount()).log("Received 1 message from cloud.");
            }).build());
        }
        for (int i = 0; i < numberOfTopics; i++) {
            client.publish(PublishRequest.builder().topic("B/"+ i ).payload("What's up".getBytes(StandardCharsets.UTF_8))
                    .build()).get(5, TimeUnit.SECONDS);
            logger.atInfo().kv("total", i + 1).log("Added 1 message to spooler.");
        }
        assertTrue(cdl.await(1, TimeUnit.MINUTES), "All messages published and received");
    }
}
