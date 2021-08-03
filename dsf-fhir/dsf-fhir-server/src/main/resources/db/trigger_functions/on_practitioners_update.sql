CREATE OR REPLACE FUNCTION on_practitioners_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.practitioner_id, NEW.version, NEW.practitioner);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL