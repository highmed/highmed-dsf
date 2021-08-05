CREATE OR REPLACE FUNCTION on_endpoints_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.endpoint_id, NEW.version, NEW.endpoint);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL