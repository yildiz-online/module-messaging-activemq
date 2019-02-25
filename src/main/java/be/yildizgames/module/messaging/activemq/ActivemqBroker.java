/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 * Copyright (c) 2019 Grégory Van den Borre
 *
 * More infos available: https://engine.yildiz-games.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS  OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE  SOFTWARE.
 */

package be.yildizgames.module.messaging.activemq;


import be.yildizgames.module.messaging.Broker;
import be.yildizgames.module.messaging.BrokerAddress;
import be.yildizgames.module.messaging.BrokerProperties;
import be.yildizgames.module.messaging.exception.MessagingException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Activemq implementation for the broker.
 * @author Grégory Van den Borre
 */
public class ActivemqBroker extends Broker {

    private final BrokerService brokerService = new BrokerService();

    private ActivemqBroker() {
        super();
    }

    static ActivemqBroker initialize(BrokerProperties properties) {
        if(properties.getBrokerInternal()) {
            return ActivemqBroker.initializeInternal("Default_broker", properties);
        }
        return ActivemqBroker.initialize(properties.getBrokerHost(), properties.getBrokerPort());
    }

    static ActivemqBroker initialize(String host, int port) {
        try {
            ActivemqBroker broker = new ActivemqBroker();
            BrokerAddress address = BrokerAddress.failover(List.of(BrokerAddress.tcp(host, port)));
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(address.getUri());
            broker.initializeConnection(connectionFactory.createConnection());
            broker.start();
            return broker;
        } catch (Exception e) {
            throw new MessagingException(e);
        }
    }

    static ActivemqBroker initializeInternal(String name, BrokerProperties properties) {
        return ActivemqBroker.initializeInternal(name, Paths.get(properties.getBrokerDataFolder()), properties.getBrokerHost());
    }

    private static ActivemqBroker initializeInternal(String name, Path dataDirectory, String host) {
        try {
            ActivemqBroker broker = new ActivemqBroker();
            broker.brokerService.setBrokerName(name);
            if(Files.notExists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            broker.brokerService.setDataDirectoryFile(dataDirectory.toFile());
            BrokerAddress address = BrokerAddress.vm(host);
            broker.brokerService.addConnector(address.getUri());
            broker.brokerService.start();
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(address.getUri());
            broker.initializeConnection(connectionFactory.createConnection());
            broker.start();
            return broker;
        } catch (Exception e) {
            throw new MessagingException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.closeConnection();
            this.brokerService.stop();
        } catch (Exception e) {
            throw new MessagingException(e);
        }
    }
}
