# SimulatingFUAS-frontend

Running on local machine
------------------------------

* Install [Service Manager](https://confluence.tools.tax.service.gov.uk/display/DTRG/04+Service+Manager+Setup)
* Install Mongodb 3.0.8
* Run
    1. mongod
    2. clamd
    3. sm --start ASSETS_FRONTEND -f
    4. sm --start FILE_UPLOAD_ALL -f
    5. local sbt run
    6. http://localhost:8897/fuaas-simulator/
    

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")# SimulatingFUAS-frontend
