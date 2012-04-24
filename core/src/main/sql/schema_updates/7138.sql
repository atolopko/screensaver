BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
7138,
current_timestamp,
'add "Non-screening" attached_file_type';

/* TODO: ICCBL-specific update */
insert into attached_file_type (attached_file_type_id, for_entity_type, value) values (nextval('attached_file_type_id_seq'), 'user', 'Non-Screening');

COMMIT;