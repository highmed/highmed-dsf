CREATE OR REPLACE FUNCTION on_provenances_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.provenance_id, NEW.version, NEW.provenance);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL