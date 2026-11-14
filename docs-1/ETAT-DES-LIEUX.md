# Etat des lieux réel de Solimus VEFA

**Date :** 2026-10-11  
**Périmètre :** `solimus-vefa-front` et `solimus-vefa-last`  
**Méthode :** lecture du code présent dans le workspace avant correction.  
**Historique Git :** non vérifiable dans cet environnement, le binaire `git` n'est pas disponible dans le terminal.

## 1. Enums réels

### Profils

Frontend, dans `solimus-vefa-front/src/app/enums/ProfilEnum.ts` :

```text
PROMOTEUR, NOTAIRE, ADMIN, RESERVATAIRE, AGENCY, BANK,
SYNDIC, PROPRIETAIRE, TENANT, COPROPRIETAIRE
```

Backend, dans `solimus-vefa-last/src/main/java/com/wakana/vefa/enums/ProfilEnum.java` :

```text
PROMOTEUR, NOTAIRE, RESERVATAIRE, BANK, AGENCY, ADMIN,
PROPRIETAIRE, SYNDIC, LOCATAIRE, PRESTATAIRE, TOM,
COPROPRIETAIRE, TENANT
```

Ecart : le frontend utilise `TENANT`, alors que le backend expose aussi `LOCATAIRE`, `PRESTATAIRE` et `TOM`; la valeur backend de `TENANT` est `TENENT`.

### Statut d'un bien

`RealEstatePropertyStatusEnum.java` :

```text
AVAILABLE, RESERVED, SOLD
```

Le modèle `RealEstateProperty` contient également un booléen `available`, en plus de l'enum `status`.

### Statut d'une réservation

`ReservationStatus.java` :

```text
PROSPECT, RESERVED
```

Le champ est stocké avec `@Enumerated(EnumType.STRING)`. Les statuts `REJECTED` et `CANCELLED` n'existent pas encore.

### Statut d'un appel de fonds

`PaymentCallStatusEnum.java` :

```text
PENDING, PAID, OVERDUE
```

Le modèle `PaymentCall` utilise également `@Enumerated(EnumType.STRING)`.

## 2. Migration et exceptions

- Aucun usage de Flyway ou Liquibase n'a été trouvé dans le backend.
- Les propriétés présentes sont dans `src/main/resources/application.properties`.
- Le projet utilise donc actuellement le schéma existant et ne possède pas de migration versionnée détectée.
- Un gestionnaire global existe : `com.wakana.vefa.exception.GlobalExceptionHandler`, annoté `@RestControllerAdvice`.
- Le gestionnaire renvoie actuellement plusieurs formats : `ApiErrorResponse` pour la plupart des erreurs et `Map<String,Object>` pour certaines erreurs d'authentification.
- `@EnableMethodSecurity` n'est pas présent dans `SecurityConfiguration.java`.

Toute modification de schéma devra donc être fournie comme script SQL manuel dans `solimus-vefa-last/src/main/resources/db/manual/` et ne devra pas être exécutée automatiquement.

## 3. Regles d'acces actuelles

`SecurityConfiguration.java` applique les règles suivantes :

- accès sans authentification : inscription, connexion, refresh, reset de mot de passe, Swagger et OpenAPI ;
- GET publics : `/api/files/**`, `/api/realestate/**`, `/api/property-types/**`, `/api/v1/parameters/**`, statistiques publiques utilisateur et `/api/subscriptions-plans/**` ;
- POST publics : OTP, recherches portail immobilier, recherche de lots par parent ;
- tous les autres endpoints : authentification JWT obligatoire ;
- aucune règle `hasRole` ou `@PreAuthorize` n'est active dans cette configuration ;
- le filtre d'abonnement est présent mais commenté.

Conséquence : l'authentification est globalement requise hors exceptions, mais le profil et la propriété de la ressource ne sont pas imposés de manière centralisée par Spring Security.

## 4. Endpoints immobilier

Contrôleur : `RealEstatePropertyController`, préfixe `/api/realestate`.

| Verbe | Chemin | Acces actuel | Modifie les donnees |
|---|---|---|---|
| POST | `/save` | authentifie | Oui |
| PUT | `/update/{id}` | authentifie | Oui |
| DELETE | `/delete/{id}` | authentifie | Oui |
| POST | `/search-by-promoter` | public par regle GET seulement? | Non |
| POST | `/search-by-bank` | authentifie | Non |
| POST | `/search-by-agency` | authentifie | Non |
| POST | `/search-by-parent` | authentifie | Non |
| POST | `/available-search-by-parent` | authentifie | Non |
| POST | `/search-portail` | public | Non |
| POST | `/search-lot-by-parent` | public | Non |
| GET | `/change-status/{id}/{status}` | public via `/api/realestate/**` | **Oui, mutation GET** |
| GET | `/change-recipient/{propertyId}` | public via `/api/realestate/**` | **Oui, mutation GET** |
| GET | `/details/{id}` | public via `/api/realestate/**` | Non |
| GET | `/recipient/{recipientId}` | public via `/api/realestate/**` | Non |
| GET | `/recipient/{recipientId}/all` | public via `/api/realestate/**` | Non |
| GET | `/notary/{notaryId}` | public via `/api/realestate/**` | Non |
| GET | `/sold/{id}` | public via `/api/realestate/**` | **Oui, mutation GET** |
| GET | `/count-no-parent` | public via `/api/realestate/**` | Non |
| GET | `/dash/promoter/{id}/{planName}` | public via `/api/realestate/**` | Non |
| GET | `/{id}/{ownerId}/{area}` | public via `/api/realestate/**` | **Oui, mutation GET probable** |
| POST | `/search-by-syndic` | authentifie | Non |
| GET | `/{id}/proreties-by-owner` | public via `/api/realestate/**` | Non |
| GET | `/budget/{id}/{budget}` | public via `/api/realestate/**` | **Oui, mutation GET** |
| GET | `/allocate-budget/{id}` | public via `/api/realestate/**` | **Oui, mutation GET** |
| POST | `/rental/search-by-promoter` | authentifie | Non |
| POST | `/rental/search-portail` | public | Non |
| GET | `/sales/dashboard` | public via `/api/realestate/**` | Non |
| GET | `/lots/dashboard` | public via `/api/realestate/**` | Non |
| GET | `/cancel/res/{id}` | public via `/api/realestate/**` | **Oui, mutation GET** |
| POST | `/{id}/subscription` | authentifie | Oui |

Remarque : la méthode `search-by-promoter` est un POST et n'est donc pas couverte par la règle GET publique; elle tombe sur `authenticated()`.

## 5. Endpoints reservations

Contrôleur : `ReservationController`, préfixe `/api/reservations`.

| Verbe | Chemin | Acces actuel | Modifie les donnees |
|---|---|---|---|
| POST | `/` | authentifie | Oui, création |
| PUT | `/{id}` | authentifie | Oui |
| DELETE | `/{id}` | authentifie | Oui |
| GET | `/{id}` | authentifie | Non |
| GET | `/` | authentifie | Non |
| GET | `/user/{userId}` | authentifie | Non |
| GET | `/lot/{lotId}` | authentifie | Non |
| GET | `/valid/{id}` | authentifie | **Oui, mutation GET** |
| GET | `/property/{parentPropertyId}` | authentifie | Non |
| GET | `/dashboard` | authentifie | Non |
| GET | `/user/{userId}/rental` | authentifie | Non |

Le endpoint `/valid/{id}` appelle `reservationService.valideReservation(id, true)` et modifie donc une réservation malgré son verbe GET.

## 6. Endpoints appels de fonds

Contrôleur : `PaymentCallController`, préfixe `/api/payment-calls`.

| Verbe | Chemin | Acces actuel | Modifie les donnees |
|---|---|---|---|
| GET | `/property/{propertyId}` | authentifie | Non |
| GET | `/me` | authentifie | Non |
| POST | `/` | authentifie | Oui |
| PUT | `/{id}` | authentifie | Oui |
| GET | `/simulate-payments/{id}` | authentifie | **A verifier; service simule un paiement** |
| DELETE | `/{id}` | authentifie | Oui |
| POST | `/{id}/file` | authentifie | Oui |
| GET | `/kpi` | authentifie | Non |
| PUT | `/{id}/status` | authentifie | Oui |
| GET | `/{propertyId}/kpi` | authentifie | Non |

`/simulate-payments/{id}` est marqué GET mais appelle une méthode nommée `simulatePayments`; il doit être vérifié avant de conclure s'il modifie réellement l'état.

## 7. Endpoints abonnements

Contrôleur : `SubscriptionsController`, préfixe `/api/user-subscriptions`.

| Verbe | Chemin | Acces actuel | Modifie les donnees |
|---|---|---|---|
| GET | `/create/{userId}/{planId}/{months}` | authentifie | **Oui, création après paiement** |
| GET | `/is-active/{userId}/{profil}` | authentifie | Non |
| GET | `/can-create-project/{userId}/{profil}` | authentifie | Non |
| GET | `/` | authentifie | Non |
| GET | `/invoices/{userId}/plan/{planName}` | authentifie | Non |
| GET | `/dashbord` | authentifie | Non |
| GET | `/evolution` | authentifie | Non |
| GET | `/revenues/evolution` | authentifie | Non |
| GET | `/plan-distribution` | authentifie | Non |
| GET | `/invoices-lastest` | authentifie | Non |
| GET | `/user/{userId}` | authentifie | Non |
| GET | `/user/{userId}/plan/{planName}` | authentifie | Non |

`/create/{userId}/{planId}/{months}` est une mutation GET : en cas de paiement valide, il crée un abonnement, une facture et marque un historique de paiement. Il doit être converti ou isolé comme callback de paiement explicitement idempotent.

## 8. Routes Angular et guards actuels

### Routes de premier niveau

| Route | Chargement | Guard actuel |
|---|---|---|
| `/dashboard` | dashboard lazy | `authGuard` |
| `/gestion-vente-vefa` | gestion VEFA lazy | `authGuard` |
| `/gestion-syndic` | syndic lazy | `authGuard` |
| `/simulateur-acquisiton` | simulateur lazy | `authGuard` |
| `/agenda` | calendrier lazy | `authGuard` |
| `/settings` | paramètres lazy | `authGuard` |
| `/dashbord` | dashboard lazy | `authGuard` |
| `/dashbord-admin` | dashboard admin lazy | `authGuard` |
| `/subscription-plan` | plans lazy | `authGuard` |
| `/plan-type` | types de plans lazy | `authGuard` |
| `/payment-history` | historique paiements lazy | `authGuard` |
| `/property-types` | types de biens lazy | `authGuard` |
| `/utilisateurs` | utilisateurs lazy | `authGuard` |
| `/signature-history` | signatures lazy | `authGuard` |
| `/pdf/sign` | composant PDF | `authGuard` |
| `/auth` | authentification lazy | Aucun |
| routes portail à la racine | front office lazy | Aucun |
| routes de paiement direct | composants paiement | Aucun visible dans `app.routes.ts` |

### Routes VEFA enfants

`gestion-vefa-routes.ts` ne possède pas de guard de profil :

```text
/gestion-vente-vefa
/gestion-vente-vefa/list-bien-syndic
/gestion-vente-vefa/create-bien
/gestion-vente-vefa/create-bien-syndic
/gestion-vente-vefa/:dataId/edit-bien
/gestion-vente-vefa/:dataId/detail-bien
/gestion-vente-vefa/:dataId/detail-bien/:dataId/create-lot
/gestion-vente-vefa/:dataId/detail-bien/:dataId/edit-lot
/gestion-vente-vefa/:dataId/detail-bien/:dataId/detail-lot
/gestion-vente-vefa/:dataId/detail-lot
```

La protection actuelle est donc l'authentification du parent, avec filtrage de menus et contrôles de profil dispersés dans les composants.

## 9. Constats prioritaires avant correction

1. Les mutations GET confirmées sont `/realestate/change-status`, `/realestate/change-recipient`, `/realestate/sold`, `/realestate/{id}/{ownerId}/{area}`, `/realestate/budget`, `/realestate/allocate-budget`, `/realestate/cancel/res`, `/reservations/valid` et `/user-subscriptions/create`.
2. `/payment-calls/simulate-payments/{id}` doit être vérifié dans son service avant conversion.
3. `/api/realestate/**` est public pour tous les GET, y compris les détails, les dashboards et la validation de vente.
4. Il existe déjà un `GlobalExceptionHandler`, mais il ne fournit pas encore un format d'erreur métier unique avec `code`, `message`, `status`, `path` et `timestamp` dans tous les cas.
5. La validation de vente utilise encore `GET /api/realestate/sold/{id}` dans le contrôleur réel.
6. La validation de réservation utilise encore `GET /api/reservations/valid/{id}`.
7. Le profil n'est pas vérifié par annotations de méthode Spring Security.
8. Les migrations automatiques ne sont pas présentes; toute évolution de schéma nécessitera un script SQL manuel.
9. Le code contient encore `System.out.println("hii")` dans `ReservationController`.
10. Le `pom.xml` contient deux déclarations de `jave-core`.
