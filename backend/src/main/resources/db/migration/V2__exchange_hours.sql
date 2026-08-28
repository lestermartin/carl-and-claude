-- Regular trading session per exchange. Single continuous session (lunch breaks, holidays,
-- and half-days are intentionally not modelled). Times are local to time_zone.

ALTER TABLE exchanges ADD COLUMN time_zone   VARCHAR(64);
ALTER TABLE exchanges ADD COLUMN open_local  TIME;
ALTER TABLE exchanges ADD COLUMN close_local TIME;
ALTER TABLE exchanges ADD COLUMN open_days   VARCHAR(32);

-- Backfill for databases that were already seeded before this migration.
-- (On a fresh database the rows are inserted by DataSeeder with the same values.)
UPDATE exchanges SET time_zone = 'America/New_York', open_local = '09:30', close_local = '16:00',
       open_days = 'MON,TUE,WED,THU,FRI' WHERE code IN ('NASDAQ', 'NYSE');
UPDATE exchanges SET time_zone = 'Asia/Shanghai', open_local = '09:30', close_local = '15:00',
       open_days = 'MON,TUE,WED,THU,FRI' WHERE code = 'SSE';
UPDATE exchanges SET time_zone = 'Europe/London', open_local = '08:00', close_local = '16:30',
       open_days = 'MON,TUE,WED,THU,FRI' WHERE code = 'LSE';
