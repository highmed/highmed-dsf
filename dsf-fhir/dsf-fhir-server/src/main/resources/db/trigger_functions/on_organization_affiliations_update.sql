CREATE OR REPLACE FUNCTION on_organization_affiliations_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.organization_affiliation_id, NEW.version, NEW.organization_affiliation);

	IF (NEW.deleted IS NOT NULL) THEN
		DELETE FROM read_access
		WHERE access_type = 'ROLE'
		AND organization_affiliation_id = NEW.organization_affiliation_id;
	END IF;
	
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL