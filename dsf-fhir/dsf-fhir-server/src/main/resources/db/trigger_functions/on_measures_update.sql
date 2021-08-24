CREATE OR REPLACE FUNCTION on_measures_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.measure_id, NEW.version, NEW.measure);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL