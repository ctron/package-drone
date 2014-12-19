CREATE TABLE PROPERTIES ("KEY" VARCHAR(255) NOT NULL, "VALUE" TEXT, PRIMARY KEY("KEY"));
INSERT INTO PROPERTIES ( "KEY", "VALUE" ) VALUES ( 'database-schema-version', '1' );

CREATE TABLE CHANNELS (
    ID            VARCHAR(36) NOT NULL,
    NAME          VARCHAR(255) UNIQUE,
    
    PRIMARY KEY (ID)
);

CREATE TABLE ARTIFACTS (
    ID            VARCHAR(36) NOT NULL,
    CHANNEL_ID    VARCHAR(36) NOT NULL,
    "TYPE"        VARCHAR(8) NOT NULL,
    
    PARENT        VARCHAR(36),
    
    NAME          VARCHAR(255) NOT NULL,
    
    DATA          LONGBLOB,
    SIZE          NUMERIC NOT NULL,
    
    CREATION_TS   DATETIME NOT NULL,
    
    PRIMARY KEY (ID),
    FOREIGN KEY (CHANNEL_ID) REFERENCES CHANNELS(ID) ON DELETE CASCADE,
    FOREIGN KEY (PARENT) REFERENCES ARTIFACTS(ID) ON DELETE CASCADE
);

CREATE TABLE VIRTUAL_ARTIFACTS (
    ID            VARCHAR(36) NOT NULL,
    NS            VARCHAR(255) NOT NULL,
    
    PRIMARY KEY (ID),
    
    FOREIGN KEY (ID) REFERENCES ARTIFACTS(ID) ON DELETE CASCADE
);

CREATE TABLE GENERATED_ARTIFACTS (
    ID            VARCHAR(36) NOT NULL,
    
    GENERATOR_ID    VARCHAR(255) NOT NULL,
    
    PRIMARY KEY (ID),
    
    FOREIGN KEY (ID) REFERENCES ARTIFACTS(ID) ON DELETE CASCADE
);

CREATE TABLE EXT_ART_PROPS (
    ART_ID        VARCHAR(36) NOT NULL,
    "NS"          VARCHAR(255) NOT NULL,
    "KEY"         VARCHAR(255) NOT NULL,
    "VALUE"       TEXT,
    
    PRIMARY KEY (ART_ID, "NS", "KEY" ),
    
    FOREIGN KEY (ART_ID) REFERENCES ARTIFACTS(ID) ON DELETE CASCADE
);

CREATE TABLE PROV_ART_PROPS (
    ART_ID        VARCHAR(36) NOT NULL,
    "NS"          VARCHAR(255) NOT NULL,
    "KEY"         VARCHAR(255) NOT NULL,
    "VALUE"       TEXT,
    
    PRIMARY KEY ( ART_ID, `NS`, `KEY` ),
    
    FOREIGN KEY ( ART_ID ) REFERENCES ARTIFACTS(ID) ON DELETE CASCADE
);

CREATE TABLE CHANNEL_ASPECTS (
    CHANNEL_ID    VARCHAR(36) NOT NULL,
    ASPECT        VARCHAR(255) NOT NULL,
    
    PRIMARY KEY ( CHANNEL_ID, ASPECT ),
    
    FOREIGN KEY (CHANNEL_ID) REFERENCES CHANNELS(ID) ON DELETE CASCADE
);