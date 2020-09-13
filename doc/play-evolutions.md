## Play Database management

- [Use of the database evolutions](https://www.playframework.com/documentation/2.8.x/Evolutions#Managing-database-evolutions)
- [Use of Play Slick](https://www.playframework.com/documentation/2.8.x/PlaySlick)
- [Play slick library](https://github.com/playframework/play-slick)

Play tracks your database evolutions using several evolutions script. These scripts are written in plain old SQL and should be located in the `conf/evolutions/{database name}` directory of your application. If the evolutions apply to your default database, this path is `conf/evolutions/default`.

The first script is named `1.sql`, the second script `2.sql`, and so on…

Each script contains two parts:
- The Ups part that describes the required transformations.
- The Downs part that describes how to revert them.

Play splits your `.sql` files into a series of semicolon-delimited statements before executing them one-by-one against the database. So if you need to use a semicolon within a statement, escape it by entering `;;` instead of `;`

To enable autoApply for all evolutions, you might set `play.evolutions.autoApply=true` in application.conf or in a system property.


1599523900990   3
1599606000000   5
1599692400000   3
1599778800000   3
1599865200000   6
1599951600000   3