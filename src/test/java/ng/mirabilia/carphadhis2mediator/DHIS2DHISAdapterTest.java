/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ng.mirabilia.carphadhis2mediator;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import java.util.Collections;
import org.junit.*;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;

import static org.junit.Assert.*;

public class DHIS2DHISAdapterTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore
    public void testMediatorHTTPRequest() throws Exception {
        new JavaTestKit(system) {{
            final MediatorConfig testConfig = new MediatorConfig("mediator-dhis2-mapper", "localhost", 8600);
            final ActorRef defaultOrchestrator = system.actorOf(Props.create(DHIS2DHISAdapter.class, testConfig));

            MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    "/dhis",
                    "test message",
                    Collections.<String, String>singletonMap("Content-Type", "text/plain"),
                    Collections.<String, String>emptyMap()
            );

            defaultOrchestrator.tell(POST_Request, getRef());

            final Object[] out =
                    new ReceiveWhile<Object>(Object.class, duration("1 second")) {
                        @Override
                        protected Object match(Object msg) throws Exception {
                            if (msg instanceof FinishRequest) {
                                return msg;
                            }
                            throw noMatch();
                        }
                    }.get();

            boolean foundResponse = false;

            for (Object o : out) {
                if (o instanceof FinishRequest) {
                    foundResponse = true;
                }
            }

            assertTrue("Must send FinishRequest", foundResponse);
        }};
    }
}
