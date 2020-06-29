package org.highmed.pseudonymization.domain;

import org.highmed.mpi.client.Idat;

public class IdatImpl implements Idat
{
	final String medicId;
	final String firstName;
	final String lastName;
	final String birthday;
	final String sex;
	final String street;
	final String zipCode;
	final String city;
	final String country;
	final String insuranceNumber;

	public IdatImpl(String medicId, String firstName, String lastName, String birthday, String sex, String street,
			String zipCode, String city, String country, String insuranceNumber)
	{
		this.medicId = medicId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = birthday;
		this.sex = sex;
		this.street = street;
		this.zipCode = zipCode;
		this.city = city;
		this.country = country;
		this.insuranceNumber = insuranceNumber;
	}

	@Override
	public String getMedicId()
	{
		return medicId;
	}

	@Override
	public String getFirstName()
	{
		return firstName;
	}

	@Override
	public String getLastName()
	{
		return lastName;
	}

	@Override
	public String getBirthday()
	{
		return birthday;
	}

	@Override
	public String getSex()
	{
		return sex;
	}

	@Override
	public String getStreet()
	{
		return street;
	}

	@Override
	public String getZipCode()
	{
		return zipCode;
	}

	@Override
	public String getCity()
	{
		return city;
	}

	@Override
	public String getCountry()
	{
		return country;
	}

	@Override
	public String getInsuranceNumber()
	{
		return insuranceNumber;
	}
}