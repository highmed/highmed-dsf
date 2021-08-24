CREATE OR REPLACE FUNCTION on_research_studies_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.research_study_id, NEW.version, NEW.research_study);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL