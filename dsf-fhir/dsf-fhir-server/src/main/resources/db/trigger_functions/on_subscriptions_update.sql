CREATE OR REPLACE FUNCTION on_subscriptions_update() RETURNS TRIGGER AS $$
BEGIN
	PERFORM on_resources_update(NEW.deleted, NEW.subscription_id, NEW.version, NEW.subscription);
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL