CREATE OR REPLACE FUNCTION on_measure_reports_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.measure_report_id, NEW.version, NEW.measure_report);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL