package org.highmed.pseudonymization.base;

/**
 * Container for all of a subject's identifying data points
 * as consented upon by HiGHmed partners.
 */
public interface Idat
{

	String getFirstName();


	String getLastName();


	String getSex();


	String getBirthday();


	String getZipCode();


	String getCity();


	String getCountry();


	String getInsuranceNr();


	String getLocalPsn();


	String getStreet();


}
