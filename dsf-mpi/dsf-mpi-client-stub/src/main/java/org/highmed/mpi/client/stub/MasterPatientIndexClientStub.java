package org.highmed.mpi.client.stub;

import java.util.HashMap;
import java.util.Map;

import org.highmed.mpi.client.Idat;
import org.highmed.mpi.client.IdatNotFoundException;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterPatientIndexClientStub implements MasterPatientIndexClient
{
	private static final Logger logger = LoggerFactory.getLogger(MasterPatientIndexClientStub.IdatImpl.class);

	private static final class IdatImpl implements Idat
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

		IdatImpl(String medicId, String firstName, String lastName, String birthday, String sex, String street,
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

	private final Map<String, Idat> patients = new HashMap<>();

	protected MasterPatientIndexClientStub()
	{
		patients.put("0", new IdatImpl("medicId-0", "Bodomar", "Backer", "12.03.1910", "M", "Mühlenbergstraße 121",
				"25840", "Friedrichstadt an der Eider", "DE", "A068266155"));
		patients.put("1", new IdatImpl("medicId-1", "Ehrenreich", "Knott", "18.07.1996", "M", "Auf der Holl 11",
				"25557", "Oldenbüttel", "DE", "A043847459"));
		patients.put("2", new IdatImpl("medicId-2", "Dagomar", "Schewe", "06.06.1906", "M", "In der Buchwiese 157",
				"74226", "Nordheim", "DE", "A004177703"));
		patients.put("3", new IdatImpl("medicId-3", "Golo", "Spanier", "13.02.1979", "M", "Burgstraße 181", "67157",
				"Wachenheim an der Weinstraße", "DE", "A080265441"));
		patients.put("4", new IdatImpl("medicId-4", "Heide", "Bäder", "10.10.1905", "F", "Alte Turmstraße 29", "57399",
				"Kirchhundem", "DE", "A023205020"));
		patients.put("5", new IdatImpl("medicId-5", "Juri", "Kober", "14.03.1908", "M", "Seilbahnweg 147", "38518",
				"Gifhorn", "DE", "A078179335"));
		patients.put("6", new IdatImpl("medicId-6", "Peggy", "Lorz", "18.09.1943", "F", "Regensburger Straße 193",
				"88433", "Schemmerhofen", "DE", "A083154051"));
		patients.put("7", new IdatImpl("medicId-7", "Ruppert", "Nopper", "12.05.1985", "M", "An den Hülsen 180",
				"23911", "Buchholz", "DE", "A001511377"));
		patients.put("8", new IdatImpl("medicId-8", "Sissy", "Diener", "04.09.1985", "F", "Markenweg 130", "46149",
				"Oberhausen", "DE", "A064297871"));
		patients.put("9", new IdatImpl("medicId-9", "Chantalle", "Hacke", "08.03.1979", "F", "Gaterstraße 56", "60323",
				"Frankfurt am Main", "DE", "A078625203"));
		patients.put("10", new IdatImpl("medicId-10", "Alissa", "Nadler", "04.11.1904", "F", "Roxeler Straße 74",
				"34439", "Willebadessen", "DE", "A099794475"));
		patients.put("11", new IdatImpl("medicId-11", "Emmeran", "Engel", "16.03.1977", "M", "Lange Hecke 189", "85435",
				"Erding", "DE", "A037040696"));
		patients.put("12", new IdatImpl("medicId-12", "Reimund", "Owens", "20.07.1902", "M", "Glück-Auf-Straße 57",
				"96135", "Stegaurach", "DE", "A035007141"));
		patients.put("13", new IdatImpl("medicId-13", "Rolf", "Storm", "22.03.1970", "M", "Papenburger Straße 123",
				"88416", "Steinhausen an der Rottum", "DE", "A023693897"));
		patients.put("14", new IdatImpl("medicId-14", "Alice", "Klingelhöfer", "01.04.1988", "F", "Wichernstraße 34",
				"25926", "Karlum", "DE", "A029733037"));
	}

	@Override
	public Idat fetchIdat(String ehrId)
	{
		Idat idat = patients.get(ehrId);
		if (idat == null)
			throw new IdatNotFoundException("IDAT for ehrId " + ehrId + " not found");

		logger.debug("Returning demo IDAT for ehrId {}", ehrId);

		return idat;
	}
}
