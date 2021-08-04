CREATE OR REPLACE FUNCTION on_patients_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.patient_id, NEW.version, NEW.patient);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL