package org.highmed.pseudonymization.translation;

import org.highmed.mpi.client.Idat;

class IdatTestImpl implements Idat
{
	private final String medicId;
	private final String firstName;
	private final String lastName;
	private final String birthday;
	private final String sex;
	private final String street;
	private final String zipCode;
	private final String city;
	private final String country;
	private final String insuranceNumber;

	IdatTestImpl(String medicId, String firstName, String lastName, String birthday, String sex, String street,
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