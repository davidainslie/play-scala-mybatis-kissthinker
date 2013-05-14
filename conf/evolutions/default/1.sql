# --- Schema

# --- !Ups

create table users (
    id integer identity not null,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    primary key (id)
);

# --- !Downs

drop table users;