CREATE OR REPLACE FUNCTION on_document_references_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.document_reference_id, NEW.version, NEW.document_reference);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL