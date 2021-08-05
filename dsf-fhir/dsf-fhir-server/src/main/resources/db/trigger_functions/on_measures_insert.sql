CREATE OR REPLACE FUNCTION on_measures_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.measure_id, NEW.version, NEW.measure);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL