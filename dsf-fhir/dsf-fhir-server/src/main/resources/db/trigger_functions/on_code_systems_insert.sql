CREATE OR REPLACE FUNCTION on_code_systems_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.code_system_id, NEW.version, NEW.code_system);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL