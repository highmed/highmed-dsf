package org.highmed.mpi.client;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Container for all of a subject's identifying data points as consented upon within the German medical informatics
 * initiative.
 */
public interface Idat
{
	DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY);

	/**
	 * @return long-term patient identifier used inside the medical data integration center (MeDIC)
	 */
	String getMedicId();

	String getFirstName();

	String getLastName();

	/**
	 * @return birthday formated as German date aka yyyy.MM.dd
	 * @see Idat#GERMAN_DATE
	 */
	String getBirthday();

	/**
	 * D - diverse (http://fhir.de/CodeSystem/gender-amtlich-de divers)<br>
	 * F - female (http://hl7.org/fhir/administrative-gender female)<br>
	 * M - male (http://hl7.org/fhir/administrative-gender male)<br>
	 * O - other (http://hl7.org/fhir/administrative-gender other)<br>
	 * U - unknown (http://hl7.org/fhir/administrative-gender unknown)<br>
	 * X - undefined (http://fhir.de/CodeSystem/gender-amtlich-de unbestimmt)
	 *
	 * @return {D, F, M, O, U, X}
	 */
	String getSex();

	/**
	 * @return street name including street number
	 */
	String getStreet();

	String getZipCode();

	String getCity();

	/**
	 * https://en.wikipedia.org/wiki/ISO_3166-2
	 *
	 * @return two character code based on ISO 3166-2
	 */
	String getCountry();

	/**
	 * @return 10 character immutable part of the German eGK number
	 */
	String getInsuranceNumber();
}
