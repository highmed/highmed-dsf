CREATE OR REPLACE FUNCTION on_bundles_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.bundle_id, NEW.version, NEW.bundle);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL