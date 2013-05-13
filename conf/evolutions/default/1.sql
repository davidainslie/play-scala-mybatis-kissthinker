# -- Schema

# -- !Ups

create sequence users_id_seq;

create table users (
    id integer not null default nextval('users_id_seq'),
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    primary key (id)
);

# -- !Downs

drop table users;

drop sequence users_id_seq;