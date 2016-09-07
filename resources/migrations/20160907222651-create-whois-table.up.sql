CREATE TABLE WHOIS
(
  nick character varying(255) NOT NULL,
  channel character varying(255) NOT NULL,
  data text,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT "PK_WHOIS" PRIMARY KEY (nick, channel)
);
