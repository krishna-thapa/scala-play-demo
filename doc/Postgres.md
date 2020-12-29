## Notes in Postgres

### Installation notes in Ubuntu:

[Steps to follow](https://www.digitalocean.com/community/tutorials/how-to-install-postgresql-on-ubuntu-20-04-quickstart)

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

### Connect to the postgres server locally:
```
sudo su - postgres
psql
\c inspiration_db 
```

### Create a database:
```
CREATE DATABASE inspiration_db;
```

### Connect to the postgres server running in docker container:
- First start the docker container with postgres image up and running
`docker-compose up`
- Connect to the postgres through terminal
`psql -h localhost -p 5432 -U  admin postgres`
- Password for admin role is *admin*
- Connect to the right database
`\c inspiration_db`
- See all the tables 
`\c dt`


### How to import CSV into the Docker Postgres database
- [Stackoverflow article](https://stackoverflow.com/questions/46849539/how-can-i-set-path-to-load-data-from-csv-file-into-postgresql-database-in-docker)
- In the docker-compose yml file, under the volumes:
`/var/lib/postgresql/data/:/var/lib/postgresql/data/pgdata`
- Need to mount the path of the csv file in the db container if you are running the command in that container.
- Have to login as root: `sudo su`
- First create (if not already created) a folder in your local machine with the path 
`/var/lib/postgresql/data/` and copy csv file under that folder 
- When the docker is compose, it will mount the path in the db docker container under the path: `/var/lib/postgresql/data/pgdata`, from where we can read the csv file and copy to the database that has been initaliozed under the play migration evolutions sql script: 
```
COPY quotations(quote,author,genre) FROM '/var/lib/postgresql/data/pgdata/Quotes-test.csv' DELIMITER ';;' CSV HEADER;
```

### Know errors:
- Might need to change the local path in mac OS for storing the csv file to `-./pgdata/:/var/lib/postgresql/data/pgdata` see the comments on this s[tackoverflow for details](https://stackoverflow.com/questions/46849539/how-can-i-set-path-to-load-data-from-csv-file-into-postgresql-database-in-docker)
