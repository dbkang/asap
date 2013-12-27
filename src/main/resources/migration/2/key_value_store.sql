create table key_value_history
(
        collection      text,
        bucket          text,
        key             text,
        stamp           bigint,
        value           json,
        primary key (collection, bucket, key, stamp)
);

create index on key_value_history (stamp);

create table last_stamp
(
        last_stamp      bigint
);

insert into last_stamp (last_stamp) values (0);

create table stamp_time
(
        stamp           bigint,
        universal_time  timestamp
);
