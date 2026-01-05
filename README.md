# GestionDepartements
Application JavaFX pour centraliser la gestion des départements et ressources humaines. Remplace les fichiers Excel et documents non structurés par un système sécurisé permettant aux managers de gérer les employés, budgets annuels et informations départementales de manière efficiente et transparente.

![Diagramme USE CASE](https://github.com/user-attachments/assets/367d6855-5c5f-4bd8-a76a-e636dd390a36)

*Objectifs du projet:

Diminuer le temps de gestion administrative: 

>-50% sur les CRUD employés/départements >> Gain de productivité pour les chefs de département.

Centraliser les données RH:

>100% des données dans un système unique >> Fiabilité et traçabilité des informations.

Sécuriser l'accès aux données:

>Gestion des rôles (Admin, Chef, Utilisateur) >> Conformité RGPD et sécurité des données.

Améliorer la visibilité sur les budgets:

>Consultation en temps réel du budget par département >> Meilleure allocation des ressources financières.

Faciliter la prise de décision:

>Tableaux de bord départementaux accessibles instantanément >> Réactivité managériale.




*Description fonctionnelle:

>>Gestion des utilisateurs:

-Authentification sécurisée (email/mot de passe).

-Trois rôles distincts : Administrateur, Chef de département, Utilisateur simple.

-Chaque utilisateur est associé à un profil employé unique.

-Gestion des sessions et déconnexion automatique.

>>Module Administrateur:

-Gestion des départements : Ajouter, modifier, supprimer des départements avec leur description.

-Gestion des utilisateurs : Créer/modifier/supprimer des comptes, attribuer des rôles.

-Supervision globale : Afficher l'ensemble des départements et leurs effectifs.

>>Module Chef de Département:

-Gestion des employés : CRUD complet sur les employés de son département.

-Consultation du budget : Affichier le budget annuel alloué à son département.

-Gestion des tâches/projets : Ajouter des tâches/projets aux employés.

-Validation des informations : Modifier les informations internes du département.

-Supervision : Consulter la liste complète de ses employés avec leurs postes.

>>Module Utilisateur Standard:

-Profil personnel: Consulter ses propres informations : nom, poste, salaire.

-Consultation de son département : Voir les détails de son département (nom, chef, budget).

-Mise à jour profil : Modifier ses coordonnées email et mot de passe.
