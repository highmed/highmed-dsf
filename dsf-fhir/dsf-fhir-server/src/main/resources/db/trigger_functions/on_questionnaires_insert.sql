CREATE OR REPLACE FUNCTION on_questionnaires_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.questionnaire_id, NEW.version, NEW.questionnaire);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL