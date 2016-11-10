CREATE TABLE sessions (
  id                 SERIAL NOT NULL, 
  owner_id           int4 NOT NULL, 
  name               varchar(127) NOT NULL, 
  doc_name           varchar(255) NOT NULL, 
  description        varchar(1023), 
  default_privileges BIT(3), 
  passwd             varchar(255), 
  PRIMARY KEY (id));
CREATE TABLE access_privileges (
  users_id    int4 NOT NULL, 
  sessions_id int4 NOT NULL, 
  privileges  BIT(3) NOT NULL, 
  PRIMARY KEY (users_id, 
  sessions_id));
CREATE TABLE users (
  id        SERIAL NOT NULL, 
  full_name varchar(127) NOT NULL, 
  uname     varchar(127) NOT NULL, 
  passwd    varchar(255) NOT NULL, 
  email     varchar(255) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE anotations (
  id            SERIAL NOT NULL, 
  owner_id      int4 NOT NULL, 
  doc_parts_id  int4 NOT NULL, 
  categories_id int4 NOT NULL, 
  "start"       int4 NOT NULL CHECK("start" >= 0), 
  len           int4 NOT NULL CHECK(len > 0), 
  links         varchar(255), 
  PRIMARY KEY (id));
CREATE TABLE "references" (
  id            SERIAL NOT NULL, 
  anotations_id int4 NOT NULL, 
  owner_id      int4 NOT NULL, 
  "start"         int4 NOT NULL CHECK("start" >= 0), 
  len           int4 NOT NULL CHECK(len > 0), 
  PRIMARY KEY (id));
CREATE TABLE comments (
  id            SERIAL NOT NULL, 
  owner_id      int4 NOT NULL, 
  anotations_id int4 NOT NULL, 
  text          varchar(255) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE categories (
  id          SERIAL NOT NULL, 
  name        int4 NOT NULL, 
  "tag"       int4 NOT NULL, 
  color       varchar(7) NOT NULL, 
  parent_id   int4, 
  sessions_id int4 NOT NULL, 
  PRIMARY KEY (id), 
  CONSTRAINT uniq_session_tag 
    UNIQUE (sessions_id, "tag"), 
  CONSTRAINT uniq_session_name 
    UNIQUE (sessions_id, name));
CREATE TABLE doc_parts (
  id          SERIAL NOT NULL, 
  sessions_id int4 NOT NULL, 
  page_num    int4 NOT NULL CHECK(page_num > 0), 
  text        text NOT NULL, 
  PRIMARY KEY (id), 
  CONSTRAINT uniq_session_page_num 
    UNIQUE (page_num, sessions_id));
ALTER TABLE "references" ADD CONSTRAINT FKreferences721789 FOREIGN KEY (anotations_id) REFERENCES anotations (id);
ALTER TABLE "references" ADD CONSTRAINT FKreferences288477 FOREIGN KEY (owner_id) REFERENCES users (id);
ALTER TABLE anotations ADD CONSTRAINT FKanotations320100 FOREIGN KEY (owner_id) REFERENCES users (id);
ALTER TABLE categories ADD CONSTRAINT FKcategories397203 FOREIGN KEY (parent_id) REFERENCES categories (id);
ALTER TABLE anotations ADD CONSTRAINT FKanotations414751 FOREIGN KEY (categories_id) REFERENCES categories (id);
ALTER TABLE categories ADD CONSTRAINT FKcategories930557 FOREIGN KEY (sessions_id) REFERENCES sessions (id);
ALTER TABLE access_privileges ADD CONSTRAINT FKaccess_pri179347 FOREIGN KEY (sessions_id) REFERENCES sessions (id);
ALTER TABLE access_privileges ADD CONSTRAINT FKaccess_pri671537 FOREIGN KEY (users_id) REFERENCES users (id);
ALTER TABLE doc_parts ADD CONSTRAINT FKdoc_parts880305 FOREIGN KEY (sessions_id) REFERENCES sessions (id);
ALTER TABLE anotations ADD CONSTRAINT FKanotations878194 FOREIGN KEY (doc_parts_id) REFERENCES doc_parts (id);
ALTER TABLE comments ADD CONSTRAINT FKcomments315090 FOREIGN KEY (owner_id) REFERENCES users (id);
ALTER TABLE sessions ADD CONSTRAINT FKsessions159156 FOREIGN KEY (owner_id) REFERENCES users (id);
ALTER TABLE comments ADD CONSTRAINT FKcomments881777 FOREIGN KEY (anotations_id) REFERENCES anotations (id);
