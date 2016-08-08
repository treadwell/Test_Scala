create table users (
  id binary(16) primary key,
  name varchar(32) not null unique,
  created_on datetime not null default now()
);

create table pets (
  id binary(16) primary key,
  owner binary(16) references users(id),
  pet_type varchar(32) references pet_types(name),
  name varchar(32) not null,
  created_on datetime not null default now()
);

create table pet_types (
  name varchar(32) primary key,
  creator binary(16) references users(id),
  created_on datetime not null default now()
);
