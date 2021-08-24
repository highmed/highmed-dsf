CREATE OR REPLACE FUNCTION on_naming_systems_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.naming_system_id, NEW.version, NEW.naming_system);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL