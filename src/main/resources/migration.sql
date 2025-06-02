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

-- ============================================================================
-- FEE TYPE SIMPLIFICATION MIGRATION
-- ============================================================================

-- Step 5: Create floor_area_fee_configs table if not exists
CREATE TABLE IF NOT EXISTS floor_area_fee_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fee_name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    fee_type_enum VARCHAR(50) NOT NULL,
    unit_price_per_sqm DECIMAL(15,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_auto_generated BOOLEAN NOT NULL DEFAULT FALSE,
    effective_from DATE,
    effective_to DATE,
    scheduled_day INT,
    scheduled_hour INT,
    scheduled_minute INT,
    created_at DATE,
    updated_at DATE,
    
    INDEX idx_fee_name (fee_name),
    INDEX idx_fee_type (fee_type_enum),
    INDEX idx_active (is_active),
    INDEX idx_auto_generated (is_auto_generated),
    INDEX idx_schedule (scheduled_day, scheduled_hour, scheduled_minute)
);

-- Step 6: Update existing fee types to simplified ones
-- This maps old fee types to new simplified ones
UPDATE fees SET fee_type_enum = 'MANDATORY' WHERE fee_type_enum = 'MANDATORY_GENERAL';
UPDATE fees SET fee_type_enum = 'VOLUNTARY' WHERE fee_type_enum = 'VOLUNTARY_GENERAL';
UPDATE fees SET fee_type_enum = 'FLOOR_AREA' WHERE fee_type_enum IN (
    'MANAGEMENT_FEE', 
    'MAINTENANCE_FEE', 
    'SECURITY_FEE', 
    'CLEANING_FEE',
    'WATER_FEE',
    'ELECTRICITY_FEE',
    'INTERNET_FEE'
);

-- Update FloorAreaFeeConfig table if it exists
UPDATE floor_area_fee_configs SET fee_type_enum = 'FLOOR_AREA' WHERE fee_type_enum IN (
    'MANAGEMENT_FEE', 
    'MAINTENANCE_FEE', 
    'SECURITY_FEE', 
    'CLEANING_FEE',
    'WATER_FEE',
    'ELECTRICITY_FEE',
    'INTERNET_FEE'
);

-- Step 7: Insert default floor area fee configurations if table is empty
INSERT INTO floor_area_fee_configs (
    fee_name, description, fee_type_enum, unit_price_per_sqm, 
    is_active, is_auto_generated, effective_from, 
    scheduled_day, scheduled_hour, scheduled_minute, created_at
)
SELECT 
    'Phí quản lý', 'Phí quản lý chung cư hàng tháng theo diện tích', 'FLOOR_AREA', 15000.00,
    TRUE, TRUE, CURDATE(),
    1, 2, 0, CURDATE()
WHERE NOT EXISTS (SELECT 1 FROM floor_area_fee_configs LIMIT 1)

UNION ALL

SELECT 
    'Phí bảo trì', 'Phí bảo trì hệ thống và thiết bị chung cư theo diện tích', 'FLOOR_AREA', 8000.00,
    TRUE, TRUE, CURDATE(),
    1, 2, 15, CURDATE()
WHERE NOT EXISTS (SELECT 1 FROM floor_area_fee_configs LIMIT 1)

UNION ALL

SELECT 
    'Phí vệ sinh', 'Phí vệ sinh khu vực chung theo diện tích', 'FLOOR_AREA', 5000.00,
    TRUE, TRUE, CURDATE(),
    1, 2, 30, CURDATE()
WHERE NOT EXISTS (SELECT 1 FROM floor_area_fee_configs LIMIT 1)

UNION ALL

SELECT 
    'Phí bảo vệ', 'Phí dịch vụ bảo vệ 24/7 theo diện tích', 'FLOOR_AREA', 3000.00,
    TRUE, TRUE, CURDATE(),
    1, 2, 45, CURDATE()
WHERE NOT EXISTS (SELECT 1 FROM floor_area_fee_configs LIMIT 1)

UNION ALL

SELECT 
    'Phí tiện ích', 'Phí tiện ích chung (điện, nước khu vực chung) theo diện tích', 'FLOOR_AREA', 2000.00,
    TRUE, FALSE, CURDATE(),
    NULL, NULL, NULL, CURDATE()
WHERE NOT EXISTS (SELECT 1 FROM floor_area_fee_configs LIMIT 1);

-- Step 8: Add constraints and final verification
-- Note: Run this after confirming the migration worked correctly
-- ALTER TABLE fees ADD CONSTRAINT chk_fee_type_enum 
--     CHECK (fee_type_enum IN ('MANDATORY', 'VOLUNTARY', 'VEHICLE_PARKING', 'FLOOR_AREA'));

-- Verification queries
-- SELECT fee_type_enum, COUNT(*) as count FROM fees GROUP BY fee_type_enum;
-- SELECT fee_name, fee_type_enum, unit_price_per_sqm FROM floor_area_fee_configs WHERE is_active = TRUE;
