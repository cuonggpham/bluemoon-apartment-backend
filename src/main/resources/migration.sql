-- Migration script to convert from Many-to-One to Many-to-Many relationship
-- between Resident and Apartment entities

-- Step 1: Create the join table for Many-to-Many relationship
CREATE TABLE IF NOT EXISTS apartment_residents (
    apartment_id BIGINT NOT NULL,
    resident_id BIGINT NOT NULL,
    PRIMARY KEY (apartment_id, resident_id),
    FOREIGN KEY (apartment_id) REFERENCES apartments(address_number) ON DELETE CASCADE,
    FOREIGN KEY (resident_id) REFERENCES residents(id) ON DELETE CASCADE
);

-- Step 2: Migrate existing data from the old relationship
-- Insert existing resident-apartment relationships into the join table
INSERT INTO apartment_residents (apartment_id, resident_id)
SELECT a.address_number, r.id
FROM residents r
INNER JOIN apartments a ON r.apartment_id = a.address_number
WHERE r.apartment_id IS NOT NULL
ON DUPLICATE KEY UPDATE apartment_id = apartment_id;

-- Step 3: Remove the old foreign key column from residentList table
-- Note: Uncomment these lines after verifying the migration is successful
-- ALTER TABLE residentList DROP FOREIGN KEY IF EXISTS fk_resident_apartment;
-- ALTER TABLE residentList DROP COLUMN apartment_id;

-- Step 4: Add indexes for better performance
CREATE INDEX idx_apartment_residents_apartment ON apartment_residents(apartment_id);
CREATE INDEX idx_apartment_residents_resident ON apartment_residents(resident_id);

-- Verification query - check that all relationships were migrated
-- SELECT 
--     COUNT(*) as old_relationships 
-- FROM residentList
-- WHERE apartment_id IS NOT NULL;

-- SELECT 
--     COUNT(*) as new_relationships 
-- FROM apartment_residents;
