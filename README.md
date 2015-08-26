# Service Loader Processor

## Fonctionnalités générales
Cette librairie permet de générer pour un fournisseur de service, un fichier de configuration dans le répertoire "**META-INF/services**" à partir d'une annotation.

- Facile d'utilisation, il suffit de rajouter la dépendance Maven avec le scope "**provided**" dans votre application.
- Disponible sur le repository central de Maven.
- Utilisable à partir de Java 6


## Utilisation rapide

- Ajouter la dépendance dans votre projet :

````xml
<dependency>
	<groupId>com.github.marcosemiao</groupId>
	<artifactId>serviceloader-processor</artifactId>
    <scope>provided</scope>
    <version>0.0.1</version>
</dependency>
````

- Dans votre implémentation rajouter l'annotation "**ServiceLoader**" à la compilation le fichier de configuration sera automatiquement crée dans le répertoire "**META-INF/services**".

Exemple :

Avec une classe "**Slf4jMojoLogger**" qui implémente une interface "**MojoLogger**"

il suffit de rajouter l'annotation "**ServiceLoader**" sur cette classe.
````java
@ServiceLoader
public class Slf4jMojoLogger implements MojoLogger {

    private final static StaticLoggerBinder staticLoggerBinder = StaticLoggerBinder.getSingleton();

    @Override
    public void setMavenLogger(final Log log) {
	staticLoggerBinder.setMavenLog(log);
    }
}
````

A la compilation, un message previent de la détection d'une implémentation :
````
[INFO] Service Provider detected
[INFO] ************************************
[INFO] Interface : fr.ms.maven.plugin.MojoLogger
[INFO]                 -> fr.ms.maven.slf4j.impl.Slf4jMojoLogger
````

A la suite de cela le fichier "**META-INF/services/fr.ms.maven.plugin.MojoLogger**" est crée avec le contenu suivant :
````
fr.ms.maven.slf4j.impl.Slf4jMojoLogger
````

## Utilisation avec plusieurs interfaces

Prenons le cas ou votre implémentation utilise plusieurs interfaces.

Re-prenons l'exemple précédent en rajoutant une interface :

````java
@ServiceLoader
public class Slf4jMojoLogger implements MojoLogger, Dummy {

    private final static StaticLoggerBinder staticLoggerBinder = StaticLoggerBinder.getSingleton();

    @Override
    public void setMavenLogger(final Log log) {
	staticLoggerBinder.setMavenLog(log);
    }
}
````

A la compilation, un message d'erreur previent de l'utilisation de plusieurs interfaces :
````
[ERROR] fr.ms.maven.slf4j.impl.Slf4jMojoLogger implements many interfaces, please define the interface in the ServiceProvider annotation
````

Il est donc nécessaire de spécifier la ou les interfaces à utiliser 

````java
@ServiceLoader(value = { MojoLogger.class, Dummy.class })
public class Slf4jMojoLogger implements MojoLogger, Serializable {

    private final static StaticLoggerBinder staticLoggerBinder = StaticLoggerBinder.getSingleton();

    @Override
    public void setMavenLogger(final Log log) {
	staticLoggerBinder.setMavenLog(log);
    }
}
````

A la compilation, un message previent de la détection de plusieurs implémentations :
````
[INFO] Service Provider detected
[INFO] ************************************
[INFO] Interface : fr.ms.maven.plugin.MojoLogger
[INFO]                 -> fr.ms.maven.slf4j.impl.Slf4jMojoLogger
[INFO] ************************************
[INFO] Interface : fr.ms.maven.plugin.Dummy
[INFO]                 -> fr.ms.maven.slf4j.impl.Slf4jMojoLogger
````

A la suite de cela deux fichiers sont crées :
"**META-INF/services/fr.ms.maven.plugin.MojoLogger**" est crée avec le contenu suivant :
````
fr.ms.maven.slf4j.impl.Slf4jMojoLogger
````

et

"**META-INF/services/fr.ms.maven.plugin.Dummy**" est crée avec le contenu suivant :
````
fr.ms.maven.slf4j.impl.Slf4jMojoLogger
````
