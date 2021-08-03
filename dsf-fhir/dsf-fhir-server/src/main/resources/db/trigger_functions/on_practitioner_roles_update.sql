CREATE OR REPLACE FUNCTION on_practitioner_roles_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.practitioner_role_id, NEW.version, NEW.practitioner_role);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL