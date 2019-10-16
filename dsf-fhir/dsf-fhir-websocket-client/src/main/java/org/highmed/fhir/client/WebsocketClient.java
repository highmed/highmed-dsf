package org.highmed.fhir.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hl7.fhir.r4.model.DomainResource;

import ca.uhn.fhir.parser.IParser;

public interface WebsocketClient
{
	void connect();

	void disconnect();

	void setDomainResourceHandler(Consumer<DomainResource> handler, Supplier<IParser> parserFactory);

	void setPingHandler(Consumer<String> handler);
}