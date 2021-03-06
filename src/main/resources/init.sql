DROP TABLE IF EXISTS Account;
CREATE TABLE Account (
AccountId LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
Balance DECIMAL(19,4)
);

DROP TABLE IF EXISTS AccountTransaction;
CREATE TABLE AccountTransaction (
TransactionId LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
Amount DECIMAL(19,4) NOT NULL,
TrxTimeStamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
FromAccountId LONG,
ToAccountId LONG,
Purpose VARCHAR(500) NOT NULL
);
CREATE UNIQUE INDEX idx_accTrx on AccountTransaction(FromAccountId,TrxTimeStamp);
