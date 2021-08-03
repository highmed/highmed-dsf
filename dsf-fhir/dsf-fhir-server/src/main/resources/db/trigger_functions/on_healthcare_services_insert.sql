CREATE OR REPLACE FUNCTION on_healthcare_services_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.healthcare_service_id, NEW.version, NEW.healthcare_service);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL