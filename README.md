OLE-INST
========

Build Instructions

cd ${OLE_DEVELOPMENT_WORKSPACE_ROOT}

mvn clean install -DskipTests=true

cd ${OLE_DEVELOPMENT_WORKSPACE_ROOT}/ole-app/ole-db/ole-liquibase/ole-liquibase-changeset

# inst-data
mvn clean install -Pinst-mysql,mysql -Dimpex.scm.phase=none

cd ${OLE_DEVELOPMENT_WORKSPACE_ROOT}
mvn clean install -DskipTests=true

cd ${OLE_DEVELOPMENT_WORKSPACE_ROOT}/ole-app/olefs
mvn clean install -DskipTests=true

cd ${OLE_DEVELOPMENT_WORKSPACE_ROOT}/ole-app/olefs
mvn initialize -Pdb -Djdbc.dba.username=dbuser -Djdbc.dba.password=dbpassword

cp ./ole-app/olefs/target/olefs.war /usr/local/tomcat/webapps/
cp ./ole-docstore/ole-docstore-webapp/target/oledocstore.war /usr/local/tomcat/webapps/

