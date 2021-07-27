CREATE OR REPLACE FUNCTION on_healthcare_services_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.healthcare_service_id, NEW.healthcare_service);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL