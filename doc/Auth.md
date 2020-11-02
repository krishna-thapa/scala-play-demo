## Authorization and Authentication

### Requirements
1. There will be three roles and permissions for now:
    - Admin that can see all the created user accounts and can make any user to admin
    - User that have an account can carry all the UI displayed functions: can fav the quote and can create their own quotes
    - User that have not created an account can only have read-only mode: can view the random quote but cannot fav the quote
2. User first have to sign up with user details and will be created as false for admin role
3. Admin role will be created at first when the project is loaded in the database with `admin@com` as email and `admin` as password
4. Admin can change the boolean parameter to make any user as an admin
5. Will have following api end-points:
    - /signIn: sign in for an existing user with email and password form
    - /signUp: sign up for a new user with user details form
    - /signOut: delete the session token for the signed user
    - /updateDetail: let the user update the details(only the user can do)
    - /allUser: get all the users that have an account in the database(only admin can do)
    - /adminRole: make the user as an admin or remove from an admin role(only admin can do)
    - /removeUser: only the admin can do
    
### Hashing the password
Postgres allows the functionality to add hashing passwords in the database. Read more [here](https://www.postgresql.org/docs/9.0/pgcrypto.html).
Since the Slick doesn't allow adding password hashing in database level, we can just hash them and insert the hashed password in the database.

It can be done implemented by [bcrypt](https://en.wikipedia.org/wiki/Bcrypt). 

- Java implementation of [jBCrypt](https://www.mindrot.org/projects/jBCrypt/)
- Scala wrapper of [Java jBCrypt](https://github.com/t3hnar/scala-bcrypt)
- Articles:
    - [Better password hashing in Play 2.2 (Java)](http://rny.io/playframework/bcrypt/2013/10/22/better-password-hashing-in-play-2.html)
    - [Passwords & hash functions (Simply Explained)](https://www.youtube.com/watch?v=cczlpiiu42M&ab_channel=SimplyExplained)
    
### Have used JWT to generate user auth token
- scala-jwt library used:
    - [jwt-scala](https://github.com/pauldijou/jwt-scala) and [example](http://pauldijou.fr/jwt-scala/)
    - https://jwt.io
    - Another library - haven't used: [Silhouette](https://github.com/adamzareba/play-silhouette-rest-slick)

### Keep in mind
- Have to implement the functionality where the user forgets the password (might need to implement email callback system)
- Have to enter the admin password as hashing when the database is initially created, run by play evolution in database migration.