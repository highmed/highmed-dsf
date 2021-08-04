CREATE OR REPLACE FUNCTION on_provenances_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.provenance_id, NEW.version, NEW.provenance);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL