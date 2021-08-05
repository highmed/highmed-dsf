CREATE OR REPLACE FUNCTION on_subscriptions_insert() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_insert(NEW.subscription_id, NEW.version, NEW.subscription);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL