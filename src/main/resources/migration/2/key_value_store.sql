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

create sequence stamp;

create table stamp_time
(
        stamp           bigint,
        universal_time  timestamp
);
