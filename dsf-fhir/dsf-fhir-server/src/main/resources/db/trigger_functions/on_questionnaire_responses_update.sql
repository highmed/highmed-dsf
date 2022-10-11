CREATE OR REPLACE FUNCTION on_questionnaire_responses_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.questionnaire_response_id, NEW.version, NEW.questionnaire_response);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL