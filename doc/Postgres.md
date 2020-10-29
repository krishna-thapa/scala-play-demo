## Notes in Postgres

### Installation notes in Ubuntu:

[Steps to follow](https://www.digitalocean.com/community/tutorials/how-to-install-postgresql-on-ubuntu-20-04-quickstart)

Connect to the postgres server:
```
sudo su - postgres
psql
\c inspiration_db 
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

### Use systemctl command to manage postgresql service:

1. stop service:
`systemctl stop postgresql`

2. start service:
`systemctl start postgresql`

3. show status of service:
`systemctl status postgresql`
4. disable service(not auto-start any more)
`systemctl disable postgresql`

5. enable service postgresql(auto-start)
`systemctl enable postgresql`

## Postgres using Docker
- [DOCKER COMPOSE UP WITH POSTGRES QUICK TIPS](https://hashinteractive.com/blog/docker-compose-up-with-postgres-quick-tips/)
- [How to import csv into Docker Postgresql database](https://medium.com/@sherryhsu/how-to-import-csv-into-docker-postgresql-database-22d56e2a1117)