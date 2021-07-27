CREATE OR REPLACE FUNCTION on_locations_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.location_id, NEW.location);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL