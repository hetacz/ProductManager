insert into CATEGORY (id, name) values
(20001, 'Electronics'),
(20002, 'Clothing'),
(20003, 'Home Appliances'),
(20004, 'Books'),
(20005, 'Grocery');

insert into PRODUCT (id, name, description, price, created, modified) values
(10001, 'Smartphone', 'Latest model with high resolution camera', 69999, now(), now()),
(10002, 'T-Shirt', '100% Cotton', 1999, now(), now()),
(10003, 'Refrigerator', 'Energy efficient with fast cooling', 55000, now(), now()),
(10004, 'Novel', 'Bestselling fiction book', 1500, now(), now()),
(10005, 'Bread', 'Freshly baked whole grain bread', 299, now(), now()),
(10006, 'Laptop', 'High performance laptop', 120000, now(), now()),
(10020, 'Cereal', 'Healthy and nutritious breakfast', 1300, now(), now()),
(10021, 'Chocolate Cereal', 'Less healthy and nutritious breakfast', 1499, now(), now());

insert into PRODUCT_CATEGORIES (PRODUCT_ID, CATEGORY_ID) values
(10001, 20001),
(10001, 20005),
(10002, 20002),
(10003, 20003),
(10003, 20001),
(10004, 20004),
(10005, 20005),
(10006, 20001),
(10006, 20002),
(10006, 20003),
(10020, 20005),
(10020, 20001),
(10021, 20005),
(10021, 20001);
