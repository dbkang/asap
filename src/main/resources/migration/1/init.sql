create table migration_history (
       migration               text,
       script_name             text,
       script_hash             text,
       script_content          text,
       ran_at                  timestamp
);
