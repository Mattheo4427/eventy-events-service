# 🎪 Eventy Events Service

Le **Events Service** est le microservice central de la plateforme Eventy. Il est responsable de la gestion du catalogue des événements, de leur cycle de vie, ainsi que de la classification (types, catégories) et des favoris utilisateurs.

## 🚀 Fonctionnalités

* **Gestion des Événements** : Création, modification, annulation et suppression d'événements.
* **Recherche & Filtres** : Recherche par mots-clés, lieu, date, catégorie et type.
* **Classification** : Gestion des types d'événements (Concert, Festival...) et catégories (Musique, Sport...).
* **Favoris** : Gestion des événements favoris par utilisateur.
* **Intégration** : Enregistrement automatique auprès d'Eureka et exposition d'API REST.

## 🛠️ Stack Technique

* **Langage** : Java 21
* **Framework** : Spring Boot 3.5.x
* **Base de données** : PostgreSQL 15
* **Migration BDD** : Flyway
* **Découverte** : Netflix Eureka Client
* **Outils** : Lombok, Maven, Docker

## ⚙️ Installation et Démarrage

### Prérequis
* JDK 21 installé
* Docker et Docker Compose (pour l'infrastructure)
* Maven

### Démarrage en local (avec Docker Compose)

Ce service est conçu pour tourner au sein de la stack globale Eventy.

# Depuis la racine du projet backend global
docker-compose up -d --build eventy-events-service

Le service sera accessible sur le port **8082**.

### Démarrage autonome (Développement)

1.  Assurez-vous qu'une base PostgreSQL est accessible.
    
2.  Configurez les variables d'environnement ou le fichier application.properties.
    
3.  Lancez l'application :
    

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   ./mvnw spring-boot:run   `

🔧 Configuration
----------------

Les variables d'environnement principales (définies dans docker-compose.yml) :

📚 API Reference
----------------

### Événements (/events)

*   GET /events : Liste filtrée (paramètres : search, location, categoryId).
    
*   GET /events/{id} : Détail d'un événement.
    
*   GET /events/upcoming : Événements à venir.
    
*   GET /events/creator/{creatorId} : Événements créés par un utilisateur.
    
*   POST /events : Créer un événement.
    
*   PUT /events/{id} : Mettre à jour un événement.
    
*   PATCH /events/{id}/status : Changer le statut (active, canceled, full).
    

### Référentiel

*   GET /event-categories : Liste des catégories.
    
*   GET /event-types : Liste des types.
    

### Favoris (/favorites)

*   GET /favorites/user/{userId} : Favoris d'un utilisateur.
    
*   POST /favorites : Ajouter un favori.
    
*   DELETE /favorites/user/{userId}/event/{eventId} : Retirer un favori.
    

🗄️ Base de Données
-------------------

Le schéma est géré par **Flyway**. Les scripts de migration se trouvent dans src/main/resources/db/migration.

*   **V1\_\_init\_schema.sql** : Structure initiale (Tables event, event\_type, event\_category, favorite).
