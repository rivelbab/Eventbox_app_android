## Eventbox_app_android

### Organisation du projet

Le projet contient deux modules principaux : Main et Playstore

* Le module playstore contient uniquement quelques infos relatives à l'authentification et 
  à la géolocalisation. 
  
* Le module main contient donc tout le code. voici son organisation : 

- package adapters: Contient tous les adapters utilisés dans l'appli pour des RecycleView ou autre.

- package config: Contient les configs de base (Network, Preference, Ressource) et exceptions personalisées

- package data: Contient les DAO, les DataSource et DatasourceFactory puis le fichier principal de la BD

- package di (pour dependency injection): contient le module de koin en charge de l'injection de dependance

- package fragments: Contient tous les fragments de l'appli. 

NB. Il faut noter que l'appli utilise le "Single activity app", ce qui veut dire qu'elle ne contient
qu'une activity et plusieurs fragments. cela est une bonne pratique recommandée par Google pour 
avoir de meilleur performance.

- package models: Contient tous les models (data class et entity)

- package networks : Contient toute la logique de communication avec l'API (endpoints) et les payloads

- package service : Contient les service pour chaque module, intéragit directement entre l'API et DAO

- package ui : Contient les View et ViewModel, élément de l'architecture MVVM utilisée

- package utils: Contient des fichiers utilitaires et des converters

### Installation 

Il n'y a pas de prérequis ou config spécifique pour faire tourner l'appli. Idéalement une connexion internet


