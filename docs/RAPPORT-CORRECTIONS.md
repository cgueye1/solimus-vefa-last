# Rapport des corrections Solimus VEFA

## Etat

Le plan de correction a ete execute par tranches ciblees. Les parcours existants ont ete conserves autant que possible. Le chantier n'est pas declare termine : les builds Maven/Angular et les tests d'integration restent a executer dans un environnement outille.

## Corrections realisees

| Domaine | Resultat |
|---|---|
| Diagnostic | `DIAGNOSTIC-SOLIMUS-VEFA.md` et `docs/ETAT-DES-LIEUX.md` crees |
| Erreurs metier | `BusinessException` et reponse d'erreur structuree ajoutes |
| Vente | Validation migree de `GET /realestate/sold/{id}` vers `POST /realestate/{id}/sale-validation` |
| Autorisation vente | Profil notaire et notaire affecte au lot verifies, vente deja validee refusee |
| Reservation | Validation migree vers `POST /reservations/{id}/validation` |
| Reservation | Rejet promoteur et annulation reservataire ajoutes |
| Historique | Statuts `REJECTED` et `CANCELLED`, dates et motif de rejet ajoutes |
| Disponibilite | Statut enum utilise comme source de verite; transitions et restauration centralisees |
| Concurrence | `@Version` ajoute aux lots |
| Migration | `src/main/resources/db/manual/001_reservation_history_and_property_version.sql` ajoutee |
| Securite | Method security activee et annotations `@PreAuthorize` ajoutees sur les mutations critiques |
| Frontend securite | `roleGuard` et donnees de roles ajoutes aux routes principales |
| Identite | Endpoints `/me` ajoutes pour reservations, lots reservataire et lots notaire |
| Dashboard | Appels frontend principaux migres des identifiants client vers `/me` |
| Appels de fonds | Le backend expose le champ transient `overdue`; le dashboard ne recalcule plus la regle metier |
| HTTP | Mutateurs lot `change-status`, `change-recipient`, `budget`, `allocate-budget` et `cancel/res` migres de GET vers POST |
| Frontend HTTP | Les appels d'annulation de reservation utilisent POST |

## Points restant a traiter

- Remplacer les notifications synchrones de vente/reservation par des evenements apres commit.
- Finaliser le controle d'acces par ressource pour toutes les lectures et tous les endpoints historiques a identifiant.
- Convertir les autres GET mutateurs confirmes, notamment la creation d'abonnement/callback de paiement selon le contrat TouchPay.
- Finaliser la politique d'abonnement centralisee et les endpoints `/me` associes.
- Ajouter les DTO de dashboard et supprimer la dependance notaire a `size=100`.
- Remplacer les onglets conditionnels par une configuration declarative et centraliser la navigation des lots.
- Ajouter les tests backend d'autorisation, de transitions, de concurrence et de metier; ajouter les tests Angular du guard et des services.
- Supprimer les logs applicatifs non necessaires, le doublon `jave-core` et les warnings de qualite.
- Executer la migration SQL manuellement sur chaque base apres verification et sauvegarde.

## Migration manuelle

La migration n'a pas ete executee automatiquement. Elle ajoute les colonnes d'historique des reservations et `real_estate_properties.version`. Elle doit etre appliquee sur la base cible avant de deployer le backend modifie.

## Validation

Diagnostics IDE executes sur les fichiers touches : aucune erreur de syntaxe ou de typage Angular/Java liee aux changements fonctionnels. Des warnings Java preexistants persistent, principalement les injections `@Autowired`, imports/champs inutilises et variables locales inutilisees.

Les commandes Maven n'ont pas pu atteindre la compilation dans l'environnement courant : le wrapper ne trouvait pas `uname`, `dirname` et le launcher Plexus Maven. Les commandes `mvn clean verify`, `ng build` et `ng test` doivent donc encore etre rejouees dans un terminal disposant de Java/Maven/Node correctement exposes.

## Questions metier a confirmer

- Le bouton d'annulation promoteur doit-il remettre toutes les reservations `REJECTED` en `PROSPECT`, ou conserver l'historique immuable ?
- Une reservation `CANCELLED` doit-elle liberer automatiquement le lot lorsqu'elle est deja `RESERVED` ? Le code actuel refuse l'annulation d'une reservation validee pour eviter une vente incoherente.
- Le callback de paiement doit-il rester accessible sans JWT comme endpoint IPN signe par le prestataire ?
- Les lots doivent-ils etre visibles publiquement, ou uniquement apres authentification ?

## Commits

Aucun commit n'a ete cree dans cette session. L'outillage Git n'etait pas disponible lors des tentatives de validation.
