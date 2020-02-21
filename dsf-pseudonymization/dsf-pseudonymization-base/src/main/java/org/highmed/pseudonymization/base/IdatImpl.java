package org.highmed.pseudonymization.base;

public class IdatImpl implements Idat
{

	private String localPsn, firstName, lastName, sex, birthday, zipCode, city, street, country, insuranceNr;

	/**
	 * Contains all consented fields for subject record linkage
	 *
	 * @param firstName
	 * @param lastName
	 * @param sex
	 * @param birthday    Format: [ddmmyyyy]
	 * @param zipCode
	 * @param city
	 * @param country
	 * @param insuranceNr The immutable part of a Pt's/Subject's EGK Nr
	 */
	public IdatImpl(String localPsn, String firstName, String lastName, String sex, String birthday,
			String zipCode, String city, String street, String country, String insuranceNr)
	{
		this.localPsn = localPsn;
		this.firstName = firstName;
		this.lastName = lastName;
		this.sex = sex;
		this.birthday = birthday;
		this.zipCode = zipCode;
		this.city = city;
		this.street = street;
		this.country = country;
		this.insuranceNr = insuranceNr;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getSex()
	{
		return sex;
	}

	public void setSex(String sex)
	{
		this.sex = sex;
	}

	public String getBirthday()
	{
		return birthday;
	}

	public void setBirthday(String birthday)
	{
		this.birthday = birthday;
	}

	public String getZipCode()
	{
		return zipCode;
	}

	public void setZipCode(String zipCode)
	{
		this.zipCode = zipCode;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getInsuranceNr()
	{
		return insuranceNr;
	}

	public void setInsuranceNr(String insuranceNr)
	{
		this.insuranceNr = insuranceNr;
	}

	public String getLocalPsn()
	{
		return localPsn;
	}

	public void setLocalPsn(String localPsn)
	{
		this.localPsn = localPsn;
	}

	public String getStreet()
	{
		return street;
	}

	public void setStreet(String street)
	{
		this.street = street;
	}
}
