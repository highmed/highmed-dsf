CREATE OR REPLACE FUNCTION on_endpoints_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.endpoint_id, NEW.version, NEW.endpoint);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL