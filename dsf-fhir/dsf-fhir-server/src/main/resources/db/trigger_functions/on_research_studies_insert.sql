CREATE OR REPLACE FUNCTION on_research_studies_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.research_study_id, NEW.version, NEW.research_study);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL