alter table employees
  add column login_password varchar(128) not null default '123456';

update employees
set login_password = '123456'
where login_password is null or login_password = '';
