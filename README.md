https://api.vefa.solimus.sn/swagger-ui/index.html#/


SPRING_FLYWAY_ENABLED=false ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev







cd /opt/solimus-vefa
sudo docker build --no-cache -t solimus-vefa:latest .
sudo docker-compose up -d


sudo docker ps --format '{{.Names}}  {{.Status}}'
sudo docker logs solimus-vefa-backend 2>&1 | grep -E "Started VefaApplication|APPLICATION FAILED|baseline" | tail -5
curl http://localhost:8086/actuator/health




regalce s3
git rm src/main/java/com/wakana/vefa/config/S3Config.java
./mvnw -q -DskipTests package && echo "Compilation OK"
git commit -m "fix(storage): supprime S3Config (bean S3 en double)"
git push origin prod-aws




terraform fmt && terraform init && terraform validate
terraform plan