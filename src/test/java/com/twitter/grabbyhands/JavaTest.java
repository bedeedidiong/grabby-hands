/*
 * Copyright 2010 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.grabbyhands;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;

public class JavaTest {
    public JavaTest() {
        System.out.println("java test");
    }

    protected List<String> servers = Arrays.asList("localhost:22133");
    protected String queue = "grabby_javatest";
    protected List<String> queues = Arrays.asList(queue);

    public void run() {
        testCreate();
        testWriteRead();
    }

    protected void testCreate() {
        System.out.println("run testCreate");

        Config config = new Config(servers);
        HashMap<String, ConfigQueue> queueConfigs = config.addQueues(queues);
        System.out.println("1");
        assert(queueConfigs.containsKey(queues.get(0)));

        config.setRecvNumConnections(4);
        config.setSendNumConnections(5);
        System.out.println("2");
        assert(config.recvNumConnections() == 4);
        assert(config.getRecvNumConnections() == 4);
        System.out.println("2a");
        assert(config.sendNumConnections() == 5);
        assert(config.getSendNumConnections() == 5);

        config.setMaxMessageBytes(100);
        System.out.println("2z");
        assert(config.getMaxMessageBytes() == 100);

        ConfigQueue configQueue = queueConfigs.get(queues.get(0));
        System.out.println("3");
        assert(configQueue.recvNumConnections() == 4);
        System.out.println("3a");
        assert(configQueue.getRecvNumConnections() == 4);

        System.out.println("4");
        assert(configQueue.recvQueueDepth() == 4);
        assert(configQueue.getRecvQueueDepth() == 4);

        System.out.println("5");
        assert(configQueue.sendNumConnections() == 5);
        assert(configQueue.getSendNumConnections() == 5);

        System.out.println("6");
        assert(configQueue.sendQueueDepth() == 5);
        assert(configQueue.getSendQueueDepth() == 5);

        GrabbyHands grabbyHands = new GrabbyHands(config);
        System.out.println("pass testCreate");
    }

    protected void testWriteRead() {
        System.out.println("rw 1");
        Config config = new Config(servers);
        config.addQueues(queues);
        GrabbyHands grabbyHands = new GrabbyHands(config);
        BlockingQueue<Write> send = grabbyHands.getSendQueue(queue);
        BlockingQueue<ByteBuffer> recv = grabbyHands.getRecvQueue(queue);
        System.out.println("rw 2");

        String sendText = "text";
        Write write = new Write(sendText);
        assert(!write.written());
        assert(!write.cancelled());
        System.out.println("rw 3");
        try {
            send.put(write);
            ByteBuffer buffer = recv.poll(2, TimeUnit.SECONDS);
            assert(buffer != null);

            String recvText = new String(buffer.array());
            assert(recvText.equals(sendText));

            assert(write.written());
        } catch (InterruptedException e) {
            assert(false);
        }
        System.out.println("pass testWriteRead");
    }

    public static void main(String[] args) {
        JavaTest javaTest = new JavaTest();
        javaTest.run();
    }
}
