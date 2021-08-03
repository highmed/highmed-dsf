CREATE OR REPLACE FUNCTION on_patients_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.patient_id, NEW.version, NEW.patient);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL