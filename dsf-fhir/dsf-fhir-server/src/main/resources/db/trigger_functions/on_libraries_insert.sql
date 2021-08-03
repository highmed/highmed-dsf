CREATE OR REPLACE FUNCTION on_libraries_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.library_id, NEW.version, NEW.library);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL