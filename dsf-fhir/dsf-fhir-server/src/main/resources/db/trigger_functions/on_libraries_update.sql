CREATE OR REPLACE FUNCTION on_libraries_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.library_id, NEW.version, NEW.library);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL