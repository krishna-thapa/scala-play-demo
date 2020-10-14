## Notes in Postgres

### Installation notes in Ubuntu:

[Steps to follow](https://www.digitalocean.com/community/tutorials/how-to-install-postgresql-on-ubuntu-20-04-quickstart)

Connect to the postgres server:
```
sudo su - postgres
```
Create a database:
```
CREATE DATABASE inspiration_db;
```
### Create a user admin and make it as a super user
```
CREATE USER admin WITH PASSWORD 'admin';
ALTER USER admin WITH SUPERUSER;
```