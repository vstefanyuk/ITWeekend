/* ---------------------------------------------------------------------- */
/* Script generated with: DeZign for Databases v4.1.3                     */
/* Target DBMS:           PostgreSQL 8                                    */
/* Project file:          Terminal.dez                                    */
/* Project name:                                                          */
/* Author:                                                                */
/* Script type:           Database drop script                            */
/* Created on:            2013-11-17 23:59                                */
/* ---------------------------------------------------------------------- */


/* ---------------------------------------------------------------------- */
/* Drop sequences                                                         */
/* ---------------------------------------------------------------------- */

DROP SEQUENCE account_seq;

DROP SEQUENCE cash_transaction_seq;

DROP SEQUENCE terminal_seq;

/* ---------------------------------------------------------------------- */
/* Drop foreign key constraints                                           */
/* ---------------------------------------------------------------------- */

/* ---------------------------------------------------------------------- */
/* Drop table "account"                                                   */
/* ---------------------------------------------------------------------- */

/* Drop constraints */

/* Drop table */

DROP TABLE account;

/* ---------------------------------------------------------------------- */
/* Drop table "cash_transaction"                                          */
/* ---------------------------------------------------------------------- */

/* Drop constraints */

/* Drop table */

DROP TABLE cash_transaction;

/* ---------------------------------------------------------------------- */
/* Drop table "terminal"                                                  */
/* ---------------------------------------------------------------------- */

/* Drop constraints */

/* Drop table */

DROP TABLE terminal;
