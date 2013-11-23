/* ---------------------------------------------------------------------- */
/* Script generated with: DeZign for Databases v4.1.3                     */
/* Target DBMS:           PostgreSQL 8                                    */
/* Project file:          Terminal.dez                                    */
/* Project name:                                                          */
/* Author:                                                                */
/* Script type:           Database creation script                        */
/* Created on:            2013-11-17 23:59                                */
/* ---------------------------------------------------------------------- */


/* ---------------------------------------------------------------------- */
/* Sequences                                                              */
/* ---------------------------------------------------------------------- */

CREATE SEQUENCE account_seq INCREMENT 1 MINVALUE 1 START 1;

CREATE SEQUENCE cash_transaction_seq INCREMENT 1 MINVALUE 1 START 1;

CREATE SEQUENCE terminal_seq INCREMENT 1 MINVALUE 1 START 1;

/* ---------------------------------------------------------------------- */
/* Tables                                                                 */
/* ---------------------------------------------------------------------- */

/* ---------------------------------------------------------------------- */
/* Add table "account"                                                    */
/* ---------------------------------------------------------------------- */

CREATE TABLE account (
    account_id INT4 DEFAULT nextval('account_seq')  NOT NULL,
    uuid VARCHAR(36)  NOT NULL,
    owner_name VARCHAR(64)  NOT NULL,
    balance NUMERIC  NOT NULL,
    PRIMARY KEY (account_id)
);

CREATE UNIQUE INDEX IDX_account_uuid ON account (uuid);

/* ---------------------------------------------------------------------- */
/* Add table "cash_transaction"                                           */
/* ---------------------------------------------------------------------- */

CREATE TABLE cash_transaction (
    cash_transaction_id INT8 DEFAULT nextval('cash_transaction_seq')  NOT NULL,
    account_id INT4  NOT NULL,
    terminal_id INT4  NOT NULL,
    withdrawn TIMESTAMP WITH TIME ZONE  NOT NULL,
    amount NUMERIC  NOT NULL,
    result_balance NUMERIC  NOT NULL,
    PRIMARY KEY (cash_transaction_id)
);

CREATE INDEX IDX_cash_transaction_account ON cash_transaction (account_id);

CREATE INDEX IDX_cash_transaction_terminal ON cash_transaction (terminal_id);

/* ---------------------------------------------------------------------- */
/* Add table "terminal"                                                   */
/* ---------------------------------------------------------------------- */

CREATE TABLE terminal (
    terminal_id INT4 DEFAULT nextval('terminal_seq')  NOT NULL,
    uuid VARCHAR(36)  NOT NULL,
    name VARCHAR(32)  NOT NULL,
    address VARCHAR(64)  NOT NULL,
    PRIMARY KEY (terminal_id)
);

CREATE UNIQUE INDEX IDX_terminal_uuid ON terminal (uuid);

/* ---------------------------------------------------------------------- */
/* Foreign key constraints                                                */
/* ---------------------------------------------------------------------- */

ALTER TABLE cash_transaction ADD
    FOREIGN KEY (account_id) REFERENCES account (account_id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE cash_transaction ADD
    FOREIGN KEY (terminal_id) REFERENCES terminal (terminal_id) ON DELETE RESTRICT ON UPDATE CASCADE;
