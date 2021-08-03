CREATE OR REPLACE FUNCTION on_value_sets_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.value_set_id, NEW.version, NEW.value_set);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL