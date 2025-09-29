-- Allow null user_id in carts table for unauthenticated users
ALTER TABLE carts MODIFY COLUMN user_id INT NULL;

