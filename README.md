Classroom Spy
===
Spy on Classroom (number of seats, has course...) for Beijing Normal University.

Also is an HTTP Server. (Default Port: 9610)

- Get buildings list: `GET /buildings`  
    Format: `Building Id,Building Name,...,Building Id, Building Name`

- Get classroom info of the specific building: `GET /building/{Building Id}`  
    Format: `Room Id,Faked Updated DateTime,Faked Number of People,Faked Number of Remaining Seat,Number of Total Seat,Is No lecture for 1st class,no lecture for 2nd,...,no lecture for 12th`