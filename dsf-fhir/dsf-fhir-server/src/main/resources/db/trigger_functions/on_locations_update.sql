CREATE OR REPLACE FUNCTION on_locations_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.location_id, NEW.version, NEW.location);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL