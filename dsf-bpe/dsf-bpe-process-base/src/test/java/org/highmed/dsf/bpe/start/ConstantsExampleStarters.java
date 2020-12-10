package org.highmed.dsf.bpe.start;

public interface ConstantsExampleStarters
{
	String CERTIFICATE_PATH = "../../dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12";
	char[] CERTIFICATE_PASSWORD = "password".toCharArray();

	String TTP_FHIR_BASE_URL = "https://ttp/fhir/";
	String ORGANIZATION_IDENTIFIER_VALUE_TTP = "Test_TTP";

	String MEDIC_1_FHIR_BASE_URL = "https://medic1/fhir/";
	String ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1 = "Test_MeDIC_1";
	String ORGANIZATION_IDENTIFIER_VALUE_MEDIC_2 = "Test_MeDIC_2";
	String ORGANIZATION_IDENTIFIER_VALUE_MEDIC_3 = "Test_MeDIC_3";
}
