CREATE OR REPLACE FUNCTION on_groups_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.group_id, NEW.version, NEW.group_json);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL