create table users (
 id text primary key,
 name text not null unique,
 created_on timestamp with time zone not null default now()
);

create table pet_types (
 name text primary key,
 creator text references users(id),
 created_on timestamp with time zone not null default now()
);

create table pets (
 id text primary key,
 owner text references users(id),
 pet_type text references pet_types(name),
 name text not null,
 created_on timestamp with time zone not null default now()
);
