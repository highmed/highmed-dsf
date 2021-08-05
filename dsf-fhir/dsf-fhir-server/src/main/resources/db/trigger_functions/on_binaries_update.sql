CREATE OR REPLACE FUNCTION on_binaries_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.binary_id, NEW.version, NEW.binary_json);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL