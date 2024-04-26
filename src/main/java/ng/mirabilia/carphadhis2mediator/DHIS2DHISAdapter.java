/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ng.mirabilia.carphadhis2mediator;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ng.mirabilia.carphadhis2mediator.converter.DataProcessor;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.ExceptError;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DHIS2DHISAdapter extends UntypedActor {
    private final MediatorConfig config;
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef requestHandler;
    private ActorRef respondTo;


    public DHIS2DHISAdapter(MediatorConfig config) {
        this.config = config;
    }

    private DataProcessor getDataProcessor() {
        return new DataProcessor(
                (Map<String, String>) config.getDynamicConfig().get("mappings-datasets"),
                (Map<String, String>) config.getDynamicConfig().get("mappings-dataelements"),
                (Map<String, String>) config.getDynamicConfig().get("mappings-orgunits"),
                (Map<String, String>) config.getDynamicConfig().get("mappings-programs")
        );
    }

    private Map<String, String> copyHeaders(Map<String, String> headers) {
        Map<String, String> copy = new HashMap<>();
        copy.put("content-type", headers.get("content-type"));
        copy.put("authorization", headers.get("authorization"));
        copy.put("x-openhim-transactionid", headers.get("x-openhim-transactionid"));
        copy.put("x-forwarded-for", headers.get("x-forwarded-for"));
        copy.put("x-forwarded-host", headers.get("x-forwarded-host"));
        return copy;
    }

    private void forwardRequest(MediatorHTTPRequest originalRequest, String body) {
        MediatorHTTPRequest newRequest = new MediatorHTTPRequest(
                requestHandler,
                getSelf(),
                "Forward Request",
                originalRequest.getMethod(),
                (String) config.getDynamicConfig().get("target-scheme"),
                (String) config.getDynamicConfig().get("target-host"),
                ((Double) config.getDynamicConfig().get("target-port")).intValue(),
                originalRequest.getPath(),
                body,
                copyHeaders(originalRequest.getHeaders()),
                originalRequest.getParams()
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(newRequest, getSelf());
    }

    private void processRequest(MediatorHTTPRequest request) {
        try {
            String body = null;

            if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("PUT")) {
                body = getDataProcessor().dataProcess(request.getBody());
            }

            forwardRequest(request, body);
        } catch (XMLStreamException ex) {
            requestHandler.tell(new ExceptError(ex), getSelf());
        }
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) { //inbound request
            System.out.println("111111111111111111111111111111111111");
            String method = ((MediatorHTTPRequest) msg).getMethod();
            String path = ((MediatorHTTPRequest) msg).getPath();

            // Check if polling is configure for this mediator
            if (method.equals("GET") && path.equals("/trigger")) {

                // Connect to CMS endpoint retrieve body and send to dataexchanger for adaptation
                try {
                    URL url = new URL("https://github.com/omoluabidotcom/trace-odk-to-dhis2/blob/master/testing.json");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        StringBuilder response = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        System.out.println("Responseeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee:");
                        System.out.println(response.toString());
                    } else {
                        System.out.println("Failed to fetch data. Response codeeeeeeeeee: " + responseCode);
                    }

                    connection.disconnect();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                requestHandler = ((MediatorHTTPRequest) msg).getRequestHandler();
                respondTo = ((MediatorHTTPRequest) msg).getRespondTo();
                processRequest((MediatorHTTPRequest) msg);
            }


        } else if (msg instanceof MediatorHTTPResponse) { //response from target server
            System.out.println("2222222222222222222222222222222222222222");
            respondTo.tell(((MediatorHTTPResponse) msg).toFinishRequest(), getSelf());
        } else {
            System.out.println("3333333333333333333333333333333333333");
            unhandled(msg);
        }
    }
}
