CREATE OR REPLACE FUNCTION on_bundles_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.bundle_id, NEW.version, NEW.bundle);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL