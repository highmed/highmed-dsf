package org.highmed.pseudonymization.encoding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.base.Idat;
import org.highmed.pseudonymization.base.IdatEncoded;
import org.highmed.pseudonymization.io.MpiLookup;

import org.highmed.pseudonymization.util.BitSetSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ResultSetEncoder
{

	private IdatEncoder idEnc;
	private MdatEncoder medEnc;
	private MpiLookup mpi;
	private ObjectMapper mapper;
	private static final Logger logger = LoggerFactory.getLogger(ResultSetEncoder.class);

	/**
	 * @param projectKey 32 byte medic-specific key for AES Encryption of identifying data
	 * @param mdatKey    32 byte project-specific key for AES Encryption of medical data
	 * @param studyID    Study ID
	 * @param seed       Seed for RBF Permutation
	 * @param origin     Institution where the encoded IDAT hail from
	 */
	public ResultSetEncoder(SecretKey projectKey, SecretKey mdatKey, String studyID, byte[] seed, String origin,
			MpiLookup mpi)
	{
		this.idEnc = new IdatEncoder(projectKey, studyID, seed, origin);
		this.medEnc = new MdatEncoder(mdatKey, studyID);
		setMpi(mpi);

		mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(BitSet.class, new BitSetSerializer());
	}

	public ResultSet encodeResultSet(ResultSet input) throws NoSuchFieldException
	{
		int idColIdx = getIdColumn(input);
		if (idColIdx == -1)
			throw new NoSuchFieldException("The provided ResultSet does not contain ehrIDs.");

		List<List<RowElement>> encodedRows = new ArrayList<>();

		for (List<RowElement> row : input.getRows())
		{
			encodedRows.add(encodeRow(row, idColIdx));
		}

		ResultSet output = new ResultSet(input.getMeta(), input.getName(), input.getQuery(), input.getColumns(),
				encodedRows);
		return output;
	}

	public ResultSet decodeForeignResultSet(ResultSet input) throws NoSuchFieldException
	{
		int idColIdx = getIdColumn(input);
		if (idColIdx == -1)
			throw new NoSuchFieldException("The provided ResultSet does not contain ehrIDs.");

		List<List<RowElement>> decodedRows = new ArrayList<>();

		for (List<RowElement> row : input.getRows())
		{
			decodedRows.add(medEnc.decryptRow(row, idColIdx));
		}

		ResultSet output = new ResultSet(input.getMeta(), input.getName(), input.getQuery(), input.getColumns(),
				decodedRows);
		return null;
	}

	/**
	 * Encode a single row of an openEHR-Resultset, turning IDAT into bloom
	 * filters and encrypting MDAT
	 *
	 * @param row      One Row of a {@link ResultSet}, List of {@link RowElement}
	 * @param idColIdx Index of the column containing ehrIDs
	 * @return List of encoded {@link RowElement}s.
	 */
	private List<RowElement> encodeRow(List<RowElement> row, int idColIdx)
	{

		// Fetch subject's IDAT from MPI
		String ehrID = row.get(idColIdx).getValueAsString();
		Idat idat = performMpiLookup(ehrID);

		// Encode IDAT and MDAT separately
		IdatEncoded idatEncoded = idEnc.encodeContainer(idat);
		List<RowElement> encodedRow = medEnc.encryptRow(row, idColIdx);

		// Serialize encoded IDAT and add them to the encoded row at right position
		try
		{
			String serializedIdat = mapper.writeValueAsString(idatEncoded);
			serializedIdat = Base64.getEncoder().encodeToString(serializedIdat.getBytes(StandardCharsets.UTF_8));
			encodedRow.add(idColIdx, new StringRowElement(serializedIdat));
		}
		catch (JsonProcessingException e)
		{
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}

		return Collections.unmodifiableList(encodedRow);
	}

	private Idat performMpiLookup(String ehrID)
	{
		return mpi.fetchIdat(ehrID);
	}

	private int getIdColumn(ResultSet input)
	{
		int idColIdx = -1;
		for (Column col : input.getColumns())
		{
			if (col.getPath().trim().toLowerCase().equals("/ehr_id/value"))
			{
				idColIdx = input.getColumns().indexOf(col);
			}
		}
		return idColIdx;
	}

	// Init methods
	private void setMpi(MpiLookup mpi)
	{
		if (mpi == null)
		{
			throw new IllegalArgumentException(
					"An MPI Interface must be provided for patient lookups. A null object " + "was provided.");
		}
		this.mpi = mpi;
	}
}