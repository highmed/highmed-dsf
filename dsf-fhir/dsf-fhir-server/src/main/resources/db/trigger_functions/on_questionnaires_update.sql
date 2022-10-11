CREATE OR REPLACE FUNCTION on_questionnaires_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.questionnaire_id, NEW.version, NEW.questionnaire);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL