package org.highmed.dsf.fhir.dao.command;

import org.highmed.dsf.fhir.validation.SnapshotGenerator;

public class TransactionResources
{
	private final ValidationHelper validationHelper;
	private final SnapshotGenerator snapshotGenerator;
	private final TransactionEventHandler transactionEventHandler;

	public TransactionResources(ValidationHelper validationHelper, SnapshotGenerator snapshotGenerator,
			TransactionEventHandler transactionEventHandler)
	{
		this.validationHelper = validationHelper;
		this.snapshotGenerator = snapshotGenerator;
		this.transactionEventHandler = transactionEventHandler;
	}

	public ValidationHelper getValidationHelper()
	{
		return validationHelper;
	}

	public SnapshotGenerator getSnapshotGenerator()
	{
		return snapshotGenerator;
	}

	public TransactionEventHandler getTransactionEventHandler()
	{
		return transactionEventHandler;
	}
}
