-- Table: public.student_archive

-- DROP TABLE IF EXISTS public.student_archive;

CREATE TABLE IF NOT EXISTS public.student_archive (
                                                      id BIGINT NOT NULL,
                                                      name CHARACTER VARYING(255) COLLATE pg_catalog."default" NOT NULL,
                                                      email CHARACTER VARYING(255) COLLATE pg_catalog."default" NOT NULL,
                                                      date_of_birth DATE NOT NULL,
                                                      archived_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                                      created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                                      CONSTRAINT student_archive_pkey PRIMARY KEY (id)
) TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.student_archive OWNER TO postgres;
