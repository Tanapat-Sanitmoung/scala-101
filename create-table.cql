CREATE KEYSPACE poc
  WITH REPLICATION = {
   'class' : 'SimpleStrategy',
   'replication_factor' : 1
  };

CREATE TABLE IF NOT EXISTS poc.customer_data (
    customerid INT PRIMARY KEY,
    genre TEXT,
    age INT,
    annual_income_k INT,
    spending_score INT
);