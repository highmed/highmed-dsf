package org.highmed.dsf.fhir.variables;

import java.io.IOException;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

public class KeyDeserializer extends JsonDeserializer<Key>
{
	@Override
	public Key deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		TreeNode node = p.getCodec().readTree(p);

		String algorithm = ((TextNode) node.get("algorithm")).textValue();
		byte[] value = ((TextNode) node.get("value")).binaryValue();

		return new SecretKeySpec(value, algorithm);
	}
}
