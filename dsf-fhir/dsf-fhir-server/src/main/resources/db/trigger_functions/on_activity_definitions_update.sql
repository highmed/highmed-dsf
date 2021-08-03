CREATE OR REPLACE FUNCTION on_activity_definitions_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.activity_definition_id, NEW.version, NEW.activity_definition);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL