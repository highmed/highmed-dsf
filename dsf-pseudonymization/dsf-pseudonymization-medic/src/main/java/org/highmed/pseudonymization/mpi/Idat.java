package org.highmed.pseudonymization.mpi;

/**
 * Container for all of a subject's identifying data points as consented upon within the German medical informatics
 * initiative.
 */
public interface Idat
{
	String getMedicId();

	String getFirstName();

	String getLastName();

	String getBirthday();

	String getSex();

	String getStreet();

	String getZipCode();

	String getCity();

	String getCountry();

	String getInsuranceNumber();
}
