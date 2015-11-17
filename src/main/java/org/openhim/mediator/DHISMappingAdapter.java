/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openhim.mediator;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.ExceptError;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import org.openhim.mediator.enrichers.DXFEnricher;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;

public class DHISMappingAdapter extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final MediatorConfig config;
    private ActorRef requestHandler;
    private ActorRef respondTo;


    public DHISMappingAdapter(MediatorConfig config) {
        this.config = config;
    }

    private DXFEnricher getEnricher() {
        return new DXFEnricher(
                (Map<String, String>)config.getDynamicConfig().get("mappings-datasets"),
                (Map<String, String>)config.getDynamicConfig().get("mappings-dataelements"),
                (Map<String, String>)config.getDynamicConfig().get("mappings-orgunits"),
                (Map<String, String>)config.getDynamicConfig().get("mappings-programs")
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
                (String)config.getDynamicConfig().get("target-scheme"),
                (String)config.getDynamicConfig().get("target-host"),
                ((Double)config.getDynamicConfig().get("target-port")).intValue(),
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
                body = getEnricher().enrich(request.getBody());
            }

            forwardRequest(request, body);
        } catch (XMLStreamException ex) {
            requestHandler.tell(new ExceptError(ex), getSelf());
        }
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) { //inbound request
            requestHandler = ((MediatorHTTPRequest) msg).getRequestHandler();
            respondTo = ((MediatorHTTPRequest) msg).getRespondTo();
            processRequest((MediatorHTTPRequest) msg);

        } else if (msg instanceof MediatorHTTPResponse) { //response from target server
            respondTo.tell(((MediatorHTTPResponse) msg).toFinishRequest(), getSelf());

        } else {
            unhandled(msg);
        }
    }
}
