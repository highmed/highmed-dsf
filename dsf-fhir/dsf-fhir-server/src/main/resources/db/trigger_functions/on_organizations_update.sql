CREATE OR REPLACE FUNCTION on_organizations_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.organization_id, NEW.organization);

	IF (NEW.deleted IS NOT NULL) THEN
		DELETE FROM read_access
		WHERE access_type = 'ORGANIZATION'
		AND organization_id = NEW.organization_id;
	END IF;
	
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL