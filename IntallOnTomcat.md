
```
SOURCE_DIR=~/svn/superfly-read-only
```

## Create database ##

Grant for user **sso** all privileges on **sso** database
```
create database `sso` default character set utf8 collate utf8_general_ci;

GRANT ALL PRIVILEGES ON sso.* TO 'sso'@'localhost' IDENTIFIED BY '123sso123' WITH GRANT OPTION;
```

## Run master install scripts ##
```
export SSO_PASSWORD='123sso123'
cd $SOURCE_DIR
mysql -u sso -p${SSO_PASSWORD} sso < superfly-sql/mi/R1.0.0/R1.0.0_SSO.sql \
 && mysql -u sso -p${SSO_PASSWORD} sso < superfly-sql/mi/R1.0.0/R1.0.0_SSO_DML.sql \
 && mysql -u sso -p${SSO_PASSWORD} sso < superfly-sql/mi/R1.1.0/R1.1.0_SSO.sql  \
 && mysql -u sso -p${SSO_PASSWORD} sso < superfly-sql/mi/R1.1.0/R1.1.0_SSO_DML.sql \
 && cd superfly-sql/src \
 && mysql -u sso -p${SSO_PASSWORD} sso < all-proc.sql
```

## Intall tomcat ##
```
mkdir -p ~/opt \
 && cd ~/opt \
 && wget -c http://archive.apache.org/dist/tomcat/tomcat-6/v6.0.26/bin/apache-tomcat-6.0.26.tar.gz \
 && tar xzf apache-tomcat-6.0.26.tar.gz \
 && mv apache-tomcat-6.0.26 tomcat-sso \
 && rm -fr tomcat-sso/webapps/*
```

## Configure tomcat ##
Add /superfly context to host section in conf/server.xml
```
<?xml version='1.0' encoding='utf-8'?>
<Server port="8005" shutdown="SHUTDOWN">

  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
  <Listener className="org.apache.catalina.core.JasperListener" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.ServerLifecycleListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />

  <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

  <Service name="Catalina">

    <Connector port="8446" protocol="HTTP/1.1" SSLEnabled="true" 
               maxThreads="150" scheme="https" secure="true" 
               clientAuth="true" sslProtocol="TLS" 
	       keystoreFile="conf/store/keystore"         keystorePass="changeit"   
               truststoreFile="conf/store/truststore.p12" truststorePass="changeit"/>

    <Engine name="Catalina" defaultHost="localhost">

      <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
             resourceName="UserDatabase"/>

      <Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true"
            xmlValidation="false" xmlNamespaceAware="false">

<Context  
                path = "/superfly" 
             docBase = "superfly.war"
 antiResourceLocking = "false" 
      antiJARLocking = "false">

    
    <!-- Defines JNDI Datasource -->
    <Resource
                name = "jdbc/superfly"
                auth = "Container" 
                type = "javax.sql.DataSource"
           maxActive = "100"
             maxIdle = "30"
             maxWait = "10000"
            username = "sso"
            password = "123sso123"
     driverClassName = "com.mysql.jdbc.Driver"
                 url = "jdbc:mysql://localhost/sso?autoReconnect=false&amp;characterEncoding=utf8"
       testWhileIdle = "true"
        testOnBorrow = "true"
     validationQuery = "{call create_collections()}"
    />
</Context>
      </Host>
    </Engine>
  </Service>
</Server>

```

## Copy keystore and truststore to tomcat ##
```
cp $SOURCE_DIR/superfly-web/src/test/resources/superfly_server_ks ~/opt/tomcat-sso/conf/store/keystore
cp $SOURCE_DIR/superfly-web/src/test/resources/ca_ts ~/opt/tomcat-sso/conf/store/truststore.p12

```

## Add mysql driver to tomcat ##
```
cd ~/opt/tomcat-sso/lib \
 && wget -c http://mirrors.ibiblio.org/pub/mirrors/maven2/mysql/mysql-connector-java/5.0.8/mysql-connector-java-5.0.8.jar
```

## Put superfly.war into webapps dir ##
```
cp $SOURCE_DIR/superfly-web/target/superfly.war ~/opt/tomcat-sso/webapps/
```