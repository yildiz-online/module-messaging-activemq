/*
 *
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 * Copyright (c) 2019 Grégory Van den Borre
 *
 * More infos available: https://engine.yildiz-games.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 *
 *
 */

package be.yildizgames.module.messaging.activemq;

import be.yildizgames.module.messaging.BrokerMessage;
import be.yildizgames.module.messaging.BrokerMessageDestination;
import be.yildizgames.module.messaging.BrokerProperties;
import be.yildizgames.module.messaging.BrokerPropertiesStandard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class ActivemqBrokerTest {

    @Nested
    public class InitializeInternal {

        @Test
        public void happyFlow() throws InterruptedException {
            Properties properties = new Properties();
            properties.setProperty("broker.host", "localhost");
            properties.setProperty("broker.port", "61616");
            properties.setProperty("broker.internal", "true");
            properties.setProperty("broker.data", System.getProperty("user.home") + "/broker/data");
            BrokerProperties brokerProperties = BrokerPropertiesStandard.fromProperties(properties);
            ActivemqBroker broker = ActivemqBroker.initializeInternal("test", brokerProperties);
            BrokerMessageDestination destination = broker.registerQueue("test");
            Wrapper wrapper = new Wrapper();
            destination.createConsumer(m -> wrapper.setMessage(m));
            destination.createProducer().sendMessage("test");
            Thread.sleep(1000);
            Assertions.assertNotNull(wrapper.message);
            Assertions.assertEquals("test", wrapper.message.getText());
        }
    }

    private static class Wrapper {

        private BrokerMessage message;

        void setMessage(BrokerMessage message) {
            this.message = message;
        }

    }

}
