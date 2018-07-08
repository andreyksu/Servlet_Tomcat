--Через командную строку.
createuser -PE -l -d -e -l testov
--Через psql
create database testdb WITH OWNER = testov ENCODING = 'UTF8';
--Через psql
CREATE TABLE principal
(
  email character(255) NOT NULL,
  password character(255) NOT NULL,
  name character(255) NOT NULL,
  regdata timestamp without time zone DEFAULT now(),
  isactive boolean DEFAULT true,
  isadmin boolean DEFAULT false,
  CONSTRAINT principal_pkey PRIMARY KEY (email),
  CONSTRAINT principal_login_key UNIQUE (email)
);

CREATE TABLE room
(
  name character(255) NOT NULL,
  regdata timestamp without time zone DEFAULT now(),
  isactive boolean DEFAULT true,
  uuidroom character(255) NOT NULL,
  CONSTRAINT room_pkey PRIMARY KEY (name),
  CONSTRAINT room_uuidroom_key UNIQUE (uuidroom)
);

CREATE TABLE messages
(
  author character(255) NOT NULL,
  uuid character(255) NOT NULL,
  message text,
  messagedate timestamp without time zone DEFAULT now(),
  delited boolean DEFAULT false,
  CONSTRAINT author_principal FOREIGN KEY (author)
      REFERENCES principal (email) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT messages_author_key UNIQUE (author),
  CONSTRAINT messages_uuid UNIQUE (uuid)
);

CREATE TABLE matrix_room_user
(
  uuid_room character(255) NOT NULL,
  email_user character(255) NOT NULL,
  CONSTRAINT email_user_constr FOREIGN KEY (email_user)
      REFERENCES principal (email) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uuid_rooms_constr FOREIGN KEY (uuid_room)
      REFERENCES room (uuidroom) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE matrix_message_user
(
  email_recipient character(255),
  uuid_message character(255),
  CONSTRAINT messageuuid_messages FOREIGN KEY (uuid_message)
      REFERENCES messages (uuid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT recipient_principal FOREIGN KEY (email_recipient)
      REFERENCES principal (email) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE matrix_message_room
(
  uuid_room character(255),
  uuid_message character(255),
  CONSTRAINT uuid_message_constr_first FOREIGN KEY (uuid_message)
      REFERENCES messages (uuid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uuid_room_constr_second FOREIGN KEY (uuid_room)
      REFERENCES room (uuidroom) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

insert into principal(email, password, name) values ('test5@yzndex.ru', '123456789', 'test5');
insert into messages(author, uuid, message) values ('test2@yzndex.ru', 'a9207b7d-9c1e-4e5c-acff-99168e0987b7', 'Сообщение от test2 для test1');
insert into matrix_message_user (email_recipient, uuid_message) values ('test1@yandex.ru', 'a9207b7d-9c1e-4e5c-acff-99168e0987b7');

select m.message, m.messagedate from matrix_message_user as mmu inner join messages as m on m.uuid = mmu.uuid_message where mmu.email_recipient = 'test11@yandex.ru' and m.author = 'test1@yandex.ru';




