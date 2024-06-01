CREATE TABLE IF NOT EXISTS sequences_orders(
  seq_id INT NOT NULL AUTO_INCREMENT,
  last_number INT NOT NULL,
  PRIMARY KEY (seq_id)
);

CREATE TABLE IF NOT EXISTS sequences_vendors(
  seq_id INT NOT NULL AUTO_INCREMENT,
  last_number INT NOT NULL,
  reference_id BIGINT NOT NULL UNIQUE,
  PRIMARY KEY (seq_id),
  FOREIGN KEY (reference_id) REFERENCES vendors(vendor_id)
);

CREATE TABLE IF NOT EXISTS sequences_customers(
  seq_id INT NOT NULL AUTO_INCREMENT,
  last_number INT NOT NULL,
  reference_id BIGINT NOT NULL UNIQUE,
  PRIMARY KEY (seq_id),
  FOREIGN KEY (reference_id) REFERENCES customers(customer_id)
);