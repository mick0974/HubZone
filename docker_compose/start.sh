echo Start build and deploy

cd .. || exit

cd ./charger_manager_service || exit
docker build -t charger-manager-service .
cd ..

cd ./charger_gateway || exit
docker build -t charger-gateway .
cd ..

cd ./hub_manager_service || exit
docker build -t hub-manager-service .
cd ..

cd ./docker_compose || exit
docker compose -f docker-compose_for-tests.yml up -d




