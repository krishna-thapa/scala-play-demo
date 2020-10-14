## Authorization and Authentication

### Do-to
1. Do SignUp with isAdmin as a false boolean parameter
2. Only the admin can see the list of users
3. Admin can change the boolean parameter to make any user as an admin
4. Have to encode password in database, [here](https://stackoverflow.com/questions/18656528/how-do-i-encrypt-passwords-with-postgresql)

### Resources
- scala-jwt library used:
    - [jwt-scala](https://github.com/pauldijou/jwt-scala) and [example](http://pauldijou.fr/jwt-scala/)
    - https://jwt.io
    - Another library - haven't used: [Silhouette](https://github.com/adamzareba/play-silhouette-rest-slick)
    