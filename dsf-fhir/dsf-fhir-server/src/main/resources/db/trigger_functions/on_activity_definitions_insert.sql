CREATE OR REPLACE FUNCTION on_activity_definitions_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.activity_definition_id, NEW.version, NEW.activity_definition);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL