ALTER TABLE product_category
ADD CONSTRAINT uq_category_bt_name UNIQUE (business_type_id, name);
