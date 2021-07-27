CREATE OR REPLACE FUNCTION on_naming_systems_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.naming_system_id, NEW.naming_system);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL