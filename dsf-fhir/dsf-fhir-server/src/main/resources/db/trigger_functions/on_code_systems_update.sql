CREATE OR REPLACE FUNCTION on_code_systems_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.code_system_id, NEW.version, NEW.code_system);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL