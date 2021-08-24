CREATE OR REPLACE FUNCTION on_value_sets_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.value_set_id, NEW.version, NEW.value_set);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL