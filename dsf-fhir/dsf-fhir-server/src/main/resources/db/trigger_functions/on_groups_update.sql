CREATE OR REPLACE FUNCTION on_groups_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.group_id, NEW.version, NEW.group_json);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL