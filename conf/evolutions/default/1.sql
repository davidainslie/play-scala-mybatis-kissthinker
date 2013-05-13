# -- Schema

# -- !Ups

create sequence user_id_seq;

create table user (
    id integer not null default nextval('user_id_seq'),
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    primary key (id)
);

# -- !Downs

drop table user;

drop sequence user_id_seq;