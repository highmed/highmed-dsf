package org.highmed.mpi.client.pdq;

import java.util.List;

import org.highmed.mpi.client.Idat;
import org.highmed.mpi.client.IdatNotFoundException;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.message.MessageHelper;
import org.highmed.mpi.client.message.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.AbstractMessage;
import ca.uhn.hl7v2.model.v25.message.QBP_Q21;
import ca.uhn.hl7v2.model.v25.message.RSP_K21;
import ca.uhn.hl7v2.util.SocketFactory;

public class MasterPatientIndexClientPdq extends AbstractHl7v2Client implements MasterPatientIndexClient
{
	private static final Logger logger = LoggerFactory.getLogger(MasterPatientIndexClientPdq.class);

	private final int port;

	private final String senderApplication;
	private final String senderFacility;
	private final String receiverApplication;
	private final String receiverFacility;

	private final String pidAssigningAuthorityNamespaceId;
	private final String pidAssigningAuthorityUniversalId;

	private final MessageHelper messageHelper;

	protected MasterPatientIndexClientPdq(String host, int port, String senderApplication, String senderFacility,
			String receiverApplication, String receiverFacility, String pidAssigningAuthorityNamespaceId,
			String pidAssigningAuthorityUniversalId, MessageHelper messageHelper, HapiContext context,
			SocketFactory socketFactory)
	{
		super(host, context, socketFactory);
		this.port = port;

		this.senderApplication = senderApplication;
		this.senderFacility = senderFacility;
		this.receiverApplication = receiverApplication;
		this.receiverFacility = receiverFacility;

		this.pidAssigningAuthorityNamespaceId = pidAssigningAuthorityNamespaceId;
		this.pidAssigningAuthorityUniversalId = pidAssigningAuthorityUniversalId;

		this.messageHelper = messageHelper;
	}

	@Override
	public Idat fetchIdat(String ehrId) throws IdatNotFoundException
	{
		List<QueryParameter> searchParameters = List.of(QueryParameter.createQueryParameterForQpd3("@PID.3.1", ehrId),
				QueryParameter.createQueryParameterForQpd3("@PID.3.4.1", pidAssigningAuthorityNamespaceId),
				QueryParameter.createQueryParameterForQpd3("@PID.3.4.2", pidAssigningAuthorityUniversalId));

		try
		{
			QBP_Q21 request = messageHelper.createPatientDemographicsQuery(senderApplication, senderFacility,
					receiverApplication, receiverFacility, searchParameters);

			logger.debug("Sending PDQ request: {}", encode(request));

			RSP_K21 response = (RSP_K21) send(port, request);

			logger.debug("Received PDQ response: {}", encode(response));

			Idat idat = messageHelper.extractPatientDemographics(response, pidAssigningAuthorityNamespaceId,
					pidAssigningAuthorityUniversalId);

			logger.debug("Found IDAT of EHR-ID='{}", ehrId);
			return idat;
		}
		catch (Exception exception)
		{
			logger.warn("Could not get IDAT of EHR-ID='{}', reason: {}", ehrId, exception.getMessage());
			throw new IdatNotFoundException(exception);
		}
	}

	private String encode(AbstractMessage message) throws HL7Exception
	{
		return message.encode().replace("\r", "\n");
	}
}
