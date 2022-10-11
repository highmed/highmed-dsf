CREATE OR REPLACE FUNCTION on_questionnaire_responses_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.questionnaire_response_id, NEW.version, NEW.questionnaire_response);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL