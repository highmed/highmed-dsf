CREATE OR REPLACE FUNCTION on_structure_definitions_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.structure_definition_id, NEW.version, NEW.structure_definition);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL