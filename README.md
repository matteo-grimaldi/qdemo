# QUARKUS demo Project

Un progetto demo per mostrare alcune funzionalita' di Quarkus.

In particolare la facilita' con cui si avviano i progetti, la facilita' con cui si aggiungono librerie (extensions) e la facilita' con cui si da persistenza agli oggetti tramite Panache.

La demo è divisa in 3 parti:

- **Prima parte**
    - Download di un archivio di quickstar
    - Build e Run di un primo MVP di applicazione
    - Overview del Dev Mode
- **Seconda Parte**
    - Aggiunta estensioni per persistenza e messaging
    - Overview sui Dev Services
    - Overview su Continous Testing
- **Terza Parte**
    - Overview su Migration Toolkit for Application per il passaggio da Spring Boot a Quarkus

## Per eseguire la demo

Dato lo scopo della demo, nessuna configurazione o tool specifico è necessario per completare i diversi passi, in particolare sono necessari solamente:
- Connessione ad Internet 
- Terminale
- Editor di Testo
- Docker in esecuzione

### Prima Parte

#### PASSO 1

Genera il pacchetto per cominciare un progetto da https://code.quarkus.io/

per iniziare aggiungi solo le estensioni RESTEasy JAX-RS e RESTEasy jackson.

scarica lo zip, esplodi il file in una directory locale, apri un terminale ed esegui il comando:

```shell script
./mvnw compile quarkus:dev
```

apri il browser e collegati con http://localhost:8080, accedi anche al servizio http://localhost:8080/hello e verifica che tutto funzioni, welcome page nel primo link e il classico *Hello RESTEasy* nel secondo.

#### PASSO 2

In **dev mode** edita la classe *GreetingResource.java* e cambia il saluto, (se non hai l'auto save abilitato salva il file), e ricarica sara' visibile il nuovo messaggio.

**NOTA:** aggiornare di conseguenza anche la classe di test altrimenti la compilazione in fase di build non funziona.

### Seconda Parte

#### PASSO 1 - aggiungere le estensioni per MongoDB

In questa parte diamo persistenza a degli oggetti usando MongoDB e Panache, per questa demo non è necessario che sia disponibile un'istanza locale, in quanto i Dev Services provvederanno ad avviare i container necessari all'avvio del Dev Mode (supportato da Docker out-of-the-box, ma è possibile utilizzare podman con alcuni workaround).

sempre in **dev mode** da un'altra finestra del terminale aggiungi le estensioni per Panache usando il comando: 

```shell script
./mvnw quarkus:add-extension -Dextensions="quarkus-mongodb-client, quarkus-mongodb-panache"
```

aggiungi un paio di classi Java, NOTA: per la demo non e' importante usare i package ma nel caso la classe Car va messa in un package denominato *model* e la classe CarResource va messa in un package denominato *rest*.

**Car.java**

```shell script
package org.acme;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection="cars")
public class Car extends PanacheMongoEntity {

    public String brand;
    public String model;

}
```

**CarResource.java**

```shell script
package org.acme;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.types.ObjectId;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;

@Path("/cars")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarResource {
    
    @GET
    public List<PanacheMongoEntityBase> list() {
        return Car.listAll();
    }

    @POST
    public Car create(Car car) {
        car.persist();
        return car;
    }

    @PUT
    public Car update(Car car) {
        car.update();
        return car;
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id) {
        Car car = Car.findById(new ObjectId(id));
        if(car == null) {
            throw new NotFoundException();
        }
        car.delete();
    }

}
```

#### PASSO 2 - lavorare con Panache

Solo al fine di mostrare l'utilizzo di Panache si può a questo punto aggiungere una specifica query al database modificando le due classi precedenti in questo modo:

**Car.java**

aggiungere le seguenti righe:

```shell script
import java.util.List;

...

    public static List<Car> listByBrand(String brand){
        return Car.find("brand = ?1", brand).list();
    }

...
```

**CarResource.java**

aggiungere le seguenti righe:

```shell script
...

    @GET
    @Path("/{brand}")
    public List<Car> getByBrand(@PathParam("brand") String brand) {
        return Car.listByBrand(brand);
    }

...
```

inserire o cancellare oggetti dal database ed utilizzare la ricerca *By Brand*.

#### PASSO 3 - mettere l'applicazione in un container

Aggiungere il dockerfile (spiegare il motivo del multi-stage build)

```shell script
# Build stage using Red Hat UBI 8 base image to compile and package application
FROM registry.access.redhat.com/ubi8/openjdk-11 as build

ENV S2I_SOURCE_DEPLOYMENTS_FILTER="*-runner.jar lib"
ENV MAVEN_ARGS_APPEND="-Dquarkus.package.type=uber-jar"

COPY --chown=jboss:0 ./ /tmp/src

RUN /usr/local/s2i/assemble

# Create final image
FROM registry.access.redhat.com/ubi8/openjdk-11
COPY --chown=jboss:0 --from=build /deployments /deployments
```

eseguire la build dell'immagine.
**NOTA:** controllare se nella directory di lavoro e' presente il file *.dockerignore*, se e' presente rimuoverlo perché impedisce il corretto funzionamento del tool S2I.

```shell script
> podman build -t qdemo .
```

eseguire l'applicazione tramite podman 

```shell script
> podman run --rm --name demoapp -d -p 8080:8080 -e MONGOCONNSTRING=mongodb://192.168.1.100:27017/demo qdemo
```

quando l'applicazione viene eseguita dentro un container l'indirizzo *localhost* non è più valido occorre quindi fornire l'apposita variabile di riferimento, nell'esempio occorre sostituire l'indirizzo con quello ip della macchina dove è in esecuzione il database MongoDB.


#### PASSO 4 - creare repo GIT

Aggiungere il progetto ad un repo GIT - [documentation]{https://docs.github.com/en/github/importing-your-projects-to-github/importing-source-code-to-github/adding-an-existing-project-to-github-using-the-command-line}.

questi passaggi richiedono che sia installato sul pc il tool *gh* (GitHub interactive CLI).

```shell script
> git init -b main

> git add . && git commit -m "initial commit"

> gh repo create
```

seguire il processo interattivo.

#### PASSO 5 - Red Hat OpenShift

Se è stato creato il repo su GitHub a questo punto è possibile installare l'applicazione su una piattaforma OpenShift.

### Extras

##### Deploy tramite developer console

Accedere ad openshift usando la vista *developer*.

1. creare un nuovo progetto;
2. dall'opzione **+Add** scegliere la strategia *GIT Repository - import from Git*;
3. specificare la URL del repo Git;
4. specificare gli ulteriori parametri (nome, deployment, ecc.). Si consiglia l'opzione Deployment;
5. completare la creazione con il comando *Create*.

accedere all'applicazione usando l'opportuno link disponibile nella vista *Topology* del progetto.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/qdemo-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- RESTEasy JAX-RS ([guide](https://quarkus.io/guides/rest-json)): REST endpoint framework implementing JAX-RS and more

- Dev Services (([guide](https://quarkus.io/guides/dev-services))

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
