CREATE OR REPLACE FUNCTION on_measure_reports_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.measure_report_id, NEW.version, NEW.measure_report);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL