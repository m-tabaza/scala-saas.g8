sudo docker exec $(sudo docker ps -aqf "ancestor=postgres") sh -c 'psql $project_name_cabab_case$-dev -U user -w pass < /seeds/seeds.sql'
