CREATE OR REPLACE FUNCTION on_practitioners_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.practitioner_id, NEW.version, NEW.practitioner);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL