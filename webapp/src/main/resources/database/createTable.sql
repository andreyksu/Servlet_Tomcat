--Через командную строку.
createuser -PE testov
createdb -O testov -E 'UTF-8' testdb
--Через psql
create database testdb WITH OWNER = testov ENCODING = 'UTF8';

create table principal (
	email		char(255) UNIQUE NOT NULL,
	password	char(255) NOT NULL,
	name		char(255) NOT NULL,	
	regdata		timestamp without time zone DEFAULT now(),
	isactive	boolean DEFAULT true,
	isadmin		boolean DEFAULT false	
);

create table messages (
	author		char(255) UNIQUE NOT NULL,
	uuid		char(255) UNIQUE NOT NULL,
	message		text,
	messagedate timestamp without time zone DEFAULT now(),
	delited		boolean DEFAULT false,
	CONSTRAINT author_principal FOREIGN KEY (author) REFERENCES principal (email)
);

create table recipients(
	recipient	char(255),
	messageuuid	char(255),
	CONSTRAINT recipient_principal FOREIGN KEY (recipient) REFERENCES principal (email),
	CONSTRAINT messageuuid_messages FOREIGN KEY (messageuuid) REFERENCES messages (uuid)	
);

