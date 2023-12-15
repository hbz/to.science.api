# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table regal_users (
  username                  varchar(255) not null,
  password                  varchar(255),
  email                     varchar(255),
  role                      integer,
  created                   varchar(255),
  constraint ck_regal_users_role check (role in (0,1,2,3,4,5)),
  constraint pk_regal_users primary key (username))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table regal_users;

SET FOREIGN_KEY_CHECKS=1;

